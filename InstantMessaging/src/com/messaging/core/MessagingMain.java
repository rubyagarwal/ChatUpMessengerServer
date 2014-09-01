package com.messaging.core;

import java.io.IOException;

import org.apache.log4j.Logger;

public class MessagingMain {
	
	private static final Logger logger = Logger.getLogger(MessagingMain.class);

	public static void main(String[] args) {
		try {
			new Dispatcher().start();
		} catch (IOException e) {
			logger.error("IOE",e);
		} catch (InterruptedException e) {
			logger.error("IE",e);
		}
	}
}