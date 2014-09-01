package com.messaging.processor;

import org.apache.log4j.Logger;

public class GetFriendsProcessor {

	private static final Logger logger = Logger
			.getLogger(GetFriendsProcessor.class);

	public void getActiveUsersForSignUpUser(String userName) {
		logger.info("Get friends for user " + userName);

	}

}
