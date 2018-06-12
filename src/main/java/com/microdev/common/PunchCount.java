package com.microdev.common;

import com.microdev.mapper.HolidayMapper;
import com.microdev.model.Holiday;
import com.microdev.mapper.WorkerLogMapper;
import com.microdev.model.WorkLog;
import com.microdev.param.PunchDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.util.*;

@Component
public class PunchCount {

	private static final Logger log = LoggerFactory.getLogger(PunchCount.class);
	@Autowired
	private WorkerLogMapper workerLogMapper;
	@Autowired
	private HolidayMapper holidayMapper;

	//@Scheduled(cron = "0 0 4 * * ?")
	public void startCountPunchInfo() {
		Integer count = workerLogMapper.countPunchInfoNumber();
		if(count == null){
			count = 0 ;
		}
		if (count == 0) {
			log.info("定时扫描打卡信息没有扫描到数据。");
		} else {
			Map<String, Object> map = new HashMap<>();
			for (int i = 0; i < count; i += 500) {
				map.put("start", i);
				if (count > i + 500) {
					map.put("end", i + 500);
				} else {
					map.put("end", count);
				}
				List<PunchDTO> list = workerLogMapper.countPunchInfo(map);
				if (list != null && list.size() > 0) {
					Iterator<PunchDTO> it = list.iterator();
					List<WorkLog> allList = new ArrayList<>();
					while (it.hasNext()) {
						checkPunchInfo(it.next(), allList);

					}
					int len = allList.size();
					for (int j = 0; j < len; j = j + 50) {
						if ((j + 50) >= len) {
							workerLogMapper.updateBatch(allList.subList(j, len));
						} else {
							workerLogMapper.updateBatch(allList.subList(j, j + 50));
						}

					}
				}
			}
		}
	}

	private void checkPunchInfo(PunchDTO p, List<WorkLog> allList) {

		OffsetDateTime start = p.getStartTime();
		OffsetDateTime end = p.getEndTime();
		long workStart = start.getLong(ChronoField.MINUTE_OF_DAY);
		long workEnd = end.getLong(ChronoField.MINUTE_OF_DAY);
		Map<String, String> log = p.getLog();
		String[] fromPunch = log.get("fromDate").split(",");
		String[] toPunch = null;
		if (log.get("toDate") != null) {
			toPunch = log.get("toDate").split(",");
		}
		DateTimeFormatter d = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		TemporalAccessor t = d.parse(fromPunch[0]);
		long startTime = t.getLong(ChronoField.MINUTE_OF_DAY);
		WorkLog updateInfo = new WorkLog();
		String[] ids = log.get("id").split(",");
		updateInfo.setPid(ids[ids.length - 1]);
		// 上班准时打卡
		if (startTime <= workStart) {
			if (toPunch != null && toPunch.length > 0) {
				long endTime = d.parse(toPunch[toPunch.length - 1]).getLong(ChronoField.MINUTE_OF_DAY);
				long time = d.parse(toPunch[0]).getLong(ChronoField.MINUTE_OF_DAY) - workStart;

				for (int i = 1; i < toPunch.length - 1; i++) {
					time += d.parse(toPunch[i]).getLong(ChronoField.MINUTE_OF_DAY)
							- d.parse(fromPunch[i]).getLong(ChronoField.MINUTE_OF_DAY);
				}
				
				if (endTime >= workEnd) {
					time += workEnd - d.parse(fromPunch[toPunch.length - 1]).getLong(ChronoField.MINUTE_OF_DAY);
					if (time < (workStart - workEnd)) {
						List<Holiday> holidayList = holidayMapper.selectByTaskWorkId(log.get("taskWorkId"));
						if (holidayList == null || holidayList.size() == 0) {
							updateInfo.setStatus(1);
						} else {
							long leaveTime = 0L;
							for (Holiday holiday : holidayList) {
								if (holiday.getFromDate().getLong(ChronoField.MINUTE_OF_DAY) < workStart) {
									leaveTime += holiday.getToDate().getLong(ChronoField.MINUTE_OF_DAY) - workStart;
								} else if (holiday.getFromDate().getLong(ChronoField.MINUTE_OF_DAY) > workEnd) {
									leaveTime += workEnd - holiday.getFromDate().getLong(ChronoField.MINUTE_OF_DAY);
								} else {
									leaveTime += holiday.getToDate().getLong(ChronoField.MINUTE_OF_DAY)
											- holiday.getFromDate().getLong(ChronoField.MINUTE_OF_DAY);
								}
							}
							if ((leaveTime + time) >= (workStart - workEnd)) {
								updateInfo.setStatus(8);
							} else {
								updateInfo.setStatus(5);
							}
						}
					} else {
						return;
					}
				} else {
					if (fromPunch.length == toPunch.length) {
						List<Holiday> holidayList = holidayMapper.selectByTaskWorkId(log.get("taskWorkId"));
						if (holidayList == null || holidayList.size() == 0) {
							updateInfo.setStatus(2);
						} else {
							time += d.parse(toPunch[toPunch.length - 1]).getLong(ChronoField.MINUTE_OF_DAY) - d.parse(fromPunch[toPunch.length - 1]).getLong(ChronoField.MINUTE_OF_DAY);
							long leaveTime = 0L;
							for (Holiday holiday : holidayList) {
								if (holiday.getFromDate().getLong(ChronoField.MINUTE_OF_DAY) < workStart) {
									leaveTime += holiday.getToDate().getLong(ChronoField.MINUTE_OF_DAY) - workStart;
								} else if (holiday.getFromDate().getLong(ChronoField.MINUTE_OF_DAY) > workEnd) {
									leaveTime += workEnd - holiday.getFromDate().getLong(ChronoField.MINUTE_OF_DAY);
								} else {
									leaveTime += holiday.getToDate().getLong(ChronoField.MINUTE_OF_DAY)
											- holiday.getFromDate().getLong(ChronoField.MINUTE_OF_DAY);
								}

							}

							if ((leaveTime + time) >= (workStart - workEnd)) {
								updateInfo.setStatus(8);
							} else {
								updateInfo.setStatus(5);
							}
						}
					} else {
						updateInfo.setStatus(4);
					}

				}
			} else {
				updateInfo.setStatus(4);
			}
		} else {
			List<Holiday> holidayList = holidayMapper.selectByTaskWorkId(log.get("taskWorkId"));
			if (toPunch != null && toPunch.length > 0) {
				long endTime = d.parse(toPunch[toPunch.length - 1]).getLong(ChronoField.MINUTE_OF_DAY);
				long time = d.parse(toPunch[0]).getLong(ChronoField.MINUTE_OF_DAY) - workStart;
				int i;
				for (i = 1; i < toPunch.length - 1; i++) {
					time += d.parse(toPunch[i]).getLong(ChronoField.MINUTE_OF_DAY)
							- d.parse(fromPunch[i]).getLong(ChronoField.MINUTE_OF_DAY);
				}
				if (fromPunch.length == toPunch.length) {
					if (holidayList == null) {
						if (endTime >= workEnd) {
							time += workEnd - d.parse(fromPunch[toPunch.length - 1]).getLong(ChronoField.MINUTE_OF_DAY);
							if (time < (workStart - workEnd)) {
								updateInfo.setStatus(5);
							} else {
								updateInfo.setStatus(1);
							}
						} else {
							if (fromPunch.length == toPunch.length) {
								updateInfo.setStatus(5);
							} else {
								updateInfo.setStatus(7);
							}
						}
					} else {
						long leaveTime = 0L;
						if (endTime >= workEnd) {
							time += workEnd - d.parse(fromPunch[toPunch.length - 1]).getLong(ChronoField.MINUTE_OF_DAY);
						} else {
							time += d.parse(toPunch[toPunch.length - 1]).getLong(ChronoField.MINUTE_OF_DAY) - d.parse(fromPunch[toPunch.length - 1]).getLong(ChronoField.MINUTE_OF_DAY);
						}
						
						for (Holiday holiday : holidayList) {
							if (holiday.getFromDate().getLong(ChronoField.MINUTE_OF_DAY) < workStart) {
								leaveTime += holiday.getToDate().getLong(ChronoField.MINUTE_OF_DAY) - workStart;
							} else if (holiday.getFromDate().getLong(ChronoField.MINUTE_OF_DAY) > workEnd) {
								leaveTime += workEnd - holiday.getFromDate().getLong(ChronoField.MINUTE_OF_DAY);
							} else {
								leaveTime += holiday.getToDate().getLong(ChronoField.MINUTE_OF_DAY)
										- holiday.getFromDate().getLong(ChronoField.MINUTE_OF_DAY);
							}
						}
						if ((leaveTime + time) >= (workStart - workEnd)) {
							updateInfo.setStatus(12);
						} else {
							if (fromPunch.length == toPunch.length) {
								updateInfo.setStatus(13);
							} else {
								updateInfo.setStatus(11);
							}
						}
					}
				} else {
					updateInfo.setStatus(7);
				}
				
			} else {
				if (holidayList == null) {
					updateInfo.setStatus(6);
				} else {
					updateInfo.setStatus(13);
				}
			}
		}
		allList.add(updateInfo);
	}
}
