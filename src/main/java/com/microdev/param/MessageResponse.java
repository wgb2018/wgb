package com.microdev.param;

import lombok.Data;

@Data
public class MessageResponse {

	private String messageTitle;
	private String messageContent;
	//消息的id
	private String id;
	private String messageType;
	private String createTime;
	private String content;
	private int applicantType;
}
