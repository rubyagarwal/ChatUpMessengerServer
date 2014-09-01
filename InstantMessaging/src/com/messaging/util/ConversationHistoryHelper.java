package com.messaging.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.messaging.core.Config;

public class ConversationHistoryHelper {

	private static final Logger logger = Logger
			.getLogger(ConversationHistoryHelper.class);

	public String getFileName(String toUser, String fromUser) {
		logger.info("to : " + toUser + " from : " + fromUser);
		String fileName = "";
		String from = fromUser.replace(" ", "").toLowerCase();
		String to = toUser.replace(" ", "").toLowerCase();
		if (from.compareTo(to) < 0) {
			// fromuser < toUSer
			fileName = from.concat(to);
		} else {
			fileName = to.concat(from);
		}
		logger.info("Returning conv history filename : " + fileName);
		return fileName;
	}

	private String getConvHistoryFileLocation(String fileName) throws IOException {
		// create media folder.
		String mediaFile = Config.DEST_FILE_LOCATION;
		File mFile = new File(mediaFile);
		if(!mFile.exists()) {
			mFile.mkdir();
		}
		
		// create convHist folder.
		String convFile = Config.DEST_FILE_LOCATION.concat(Config.CONV_HISTORY_LOCATION);
		File cFile = new File(convFile);
		if(!cFile.exists()) {
			cFile.mkdir();
		}
		
		// create text file.
		String fileLocation = Config.DEST_FILE_LOCATION
				.concat(Config.CONV_HISTORY_LOCATION).concat(fileName)
				.concat(Config.TEXT_FILE_SUFFIX);
		return fileLocation;
	}

	public void saveMessage(String fileName, String message, String fromUser)
			throws IOException {
		String fileLocation = getConvHistoryFileLocation(fileName);
		File file = new File(fileLocation);
		if (!file.exists()) {
			logger.info("Creating new file "+file.getAbsolutePath());
			file.createNewFile();
		}

		// true = append file
		FileWriter fileWritter = new FileWriter(file.getAbsolutePath(), true);
		BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
		bufferWritter.write(fromUser.concat(":").concat(message));
		bufferWritter.newLine();
		bufferWritter.close();
	}

	public List<String> readConversationHistory(String fileName)
			throws IOException {
		List<String> history = new ArrayList<String>();
		String fileLocation = getConvHistoryFileLocation(fileName);
		File file = new File(fileLocation);
		if (!file.exists()) {
			logger.info("Creating new file "+file.getAbsolutePath());
			file.createNewFile();
		}
		FileReader fr = new FileReader(fileLocation);
		BufferedReader br = new BufferedReader(fr);
		String s;
		while ((s = br.readLine()) != null) {
			history.add(s);
		}
		fr.close();
		logger.info("returning history " + history.size());
		return history;
	}
}
