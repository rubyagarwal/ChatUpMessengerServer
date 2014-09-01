package com.messaging.processor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Timestamp;

import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;

import com.messaging.core.Config;
import com.messaging.data.CallName;
import com.messaging.data.MessageType;
import com.messaging.data.Payload;
import com.messaging.util.ConversationHistoryHelper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class ChatMessageProcessor {
	
	private static final Logger logger = Logger.getLogger(ChatMessageProcessor.class);

	public void directChatMessage(String toUser, String fromUser,
			String isMedia, byte[] comBs, String fileName, MessageType msgType)
			throws IOException, InterruptedException {
		// queue name should be the name of the receiver.

		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost(Config.IP);
		factory.setUsername(Config.USERNAME);
		factory.setPassword(Config.PASSWORD);
		Connection connection = factory.newConnection();
		Channel channel = connection.createChannel();

		String binding = toUser.toLowerCase().replace(" ", "");
		channel.exchangeDeclare(Config.CHAT_ONLY_EXCHANGE, "direct", true);
		String message;

		if (Config.TRUE.equalsIgnoreCase(isMedia)) {
			// media message

			byte[] decomBs = new DataCompressionHelper().decompressData(comBs);

			FileOutputStream out = null;
			String filePath = getMediaFileLocation(msgType.name(),fileName);
			try {

				out = new FileOutputStream(filePath);
				logger.info(decomBs.length);
				out.write(decomBs);
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			message = fileName;
		} else {
			// text only.
			message = new String(comBs, "UTF-8");
		}

		Payload p = new Payload();
		p.setCallName(CallName.Chat);
		p.setFromUser(fromUser);
		p.setIsMedia(isMedia);
		p.setMediaFileName(fileName);
		p.setMessage(message.getBytes());
		p.setMsgType(msgType);
		p.setTimeStamp(new Timestamp(System.currentTimeMillis()));
		p.setToUser(toUser);

		ObjectWriter ow = new ObjectMapper().writer()
				.withDefaultPrettyPrinter();
		String json = ow.writeValueAsString(p);
		channel.queueDeclare(binding, true, false, false, null);
		channel.queueBind(binding, Config.CHAT_ONLY_EXCHANGE, binding);
		channel.basicPublish(Config.CHAT_ONLY_EXCHANGE, binding, null,
				json.getBytes("utf-8"));

		logger.info("Sent Chat back on queue " + binding
				+ " and exchange " + Config.CHAT_ONLY_EXCHANGE + " binding "
				+ binding);

		channel.close();
		connection.close();

		saveConversationHistory(fromUser, toUser, message);

	}

	private String getMediaFileLocation(String msgType, String fileName) {
		// create media folder.
		String mediaFile = Config.DEST_FILE_LOCATION;
		File mFile = new File(mediaFile);
		if(!mFile.exists()) {
			mFile.mkdir();
		}

		// create msgType folder.
		String msgTypeFile = Config.DEST_FILE_LOCATION.concat(msgType.toLowerCase()).concat("/");
		File mtFile = new File(msgTypeFile);
		if(!mtFile.exists()) {
			mtFile.mkdir();
		}

		String filePath = Config.DEST_FILE_LOCATION
				.concat(msgType.toLowerCase()).concat("/")
				.concat(fileName);
		
		return filePath;
	}

	private void saveConversationHistory(String fromUser, String toUser,
			String message) throws IOException {
		ConversationHistoryHelper chh = new ConversationHistoryHelper();
		String fileName = chh.getFileName(toUser, fromUser);
		chh.saveMessage(fileName, message, fromUser);

	}
}