package com.microdev.param;

import lombok.Data;

@Data
public class HrDeployApply extends HotelInfo{

	private String name;
	private String createTime;
	private String leader;
	private String leaderMobile;
	//人力需要招的人数
	private Integer hNeedWorkers;
	//人力已招的人数
	private Integer hConfirmedWorkers;
	//人力缺少的人数
	private Integer hLackWorkers;
	//申请理由
	private String reason;

}
