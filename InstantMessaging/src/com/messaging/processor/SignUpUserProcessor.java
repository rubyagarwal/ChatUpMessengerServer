package com.messaging.processor;

import org.apache.log4j.Logger;

public class SignUpUserProcessor {

	private static final Logger logger = Logger
			.getLogger(SignUpUserProcessor.class);

	public void signUpUser(String userName) {
		logger.info("Sign up the user " + userName);
	}

}
