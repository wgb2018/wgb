package com.microdev.param;

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
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getCreateTime() {
		return createTime;
	}
	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}
	public String getLeader() {
		return leader;
	}
	public void setLeader(String leader) {
		this.leader = leader;
	}
	public String getLeaderMobile() {
		return leaderMobile;
	}
	public void setLeaderMobile(String leaderMobile) {
		this.leaderMobile = leaderMobile;
	}
	public Integer gethNeedWorkers() {
		return hNeedWorkers;
	}
	public void sethNeedWorkers(Integer hNeedWorkers) {
		this.hNeedWorkers = hNeedWorkers;
	}
	public Integer gethConfirmedWorkers() {
		return hConfirmedWorkers;
	}
	public void sethConfirmedWorkers(Integer hConfirmedWorkers) {
		this.hConfirmedWorkers = hConfirmedWorkers;
	}
	public Integer gethLackWorkers() {
		return hLackWorkers;
	}
	public void sethLackWorkers(Integer hLackWorkers) {
		this.hLackWorkers = hLackWorkers;
	}
	public String getReason() {
		return reason;
	}
	public void setReason(String reason) {
		this.reason = reason;
	}
	
}
