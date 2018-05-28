package com.microdev.param;

import java.time.OffsetDateTime;

public class LeaveApply extends HotelInfo{

	private String username;
	private String sex;
	private OffsetDateTime birthday;
	private String mobile;
	private OffsetDateTime supplementTimeEnd;
	private OffsetDateTime supplementTime;
	private OffsetDateTime createTime;
	private String content;
	//转换成字符的日期
	private String leaveTime;
	//具体时间
	private String specificTime;
	private Integer minutes;
	
	public Integer getMinutes() {
		return minutes;
	}
	public void setMinutes(Integer minutes) {
		this.minutes = minutes;
	}
	public String getLeaveTime() {
		return leaveTime;
	}
	public void setLeaveTime(String leaveTime) {
		this.leaveTime = leaveTime;
	}
	public String getSpecificTime() {
		return specificTime;
	}
	public void setSpecificTime(String specificTime) {
		this.specificTime = specificTime;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getSex() {
		return sex;
	}
	public void setSex(String sex) {
		this.sex = sex;
	}
	public OffsetDateTime getBirthday() {
		return birthday;
	}
	public void setBirthday(OffsetDateTime birthday) {
		this.birthday = birthday;
	}
	public String getMobile() {
		return mobile;
	}
	public void setMobile(String mobile) {
		this.mobile = mobile;
	}
	public OffsetDateTime getSupplementTimeEnd() {
		return supplementTimeEnd;
	}
	public void setSupplementTimeEnd(OffsetDateTime supplementTimeEnd) {
		this.supplementTimeEnd = supplementTimeEnd;
	}
	public OffsetDateTime getSupplementTime() {
		return supplementTime;
	}
	public void setSupplementTime(OffsetDateTime supplementTime) {
		this.supplementTime = supplementTime;
	}
	public OffsetDateTime getCreateTime() {
		return createTime;
	}
	public void setCreateTime(OffsetDateTime createTime) {
		this.createTime = createTime;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	
}
