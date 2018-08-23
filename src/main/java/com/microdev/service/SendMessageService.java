package com.microdev.service;


/**
 * This interface is created for RestAPI of Sending Message, it should be
 * synchronized with the API list.http://docs.easemob.com/
 * 
 * @author Eric23 2016-01-05
 */
public interface SendMessageService {

	Object sendMessage(Object payload);
}
