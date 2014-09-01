package com.messaging.processor;

import org.apache.log4j.Logger;

public class GetActiveConvUsers {

	private static final Logger logger = Logger.getLogger(GetActiveConvUsers.class);
	
	public void getActiveUSers(String toUser, String fromUser) {
		logger.info("Get active users for to:from " + toUser +" : "+fromUser);
		
	}

}
