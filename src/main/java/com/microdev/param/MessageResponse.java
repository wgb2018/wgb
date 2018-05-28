package com.microdev.param;

import lombok.Data;

@Data
public class MessageResponse {

	private String messageTitle;
	//消息到达的时间
	private String days;
	private String messageContent;
	//消息的id
	private String id;

}
