package com.messaging.processor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;

import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;

import com.messaging.core.Config;
import com.messaging.data.CallName;
import com.messaging.data.MessageType;
import com.messaging.data.Payload;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class DownloadMediaProcessor {

	private static final Logger logger = Logger
			.getLogger(DownloadMediaProcessor.class);

	public void sendRequestedMediaToUser(String toUser, String mediaFileName,
			MessageType msgType) throws IOException {
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost(Config.IP);
		factory.setUsername(Config.USERNAME);
		factory.setPassword(Config.PASSWORD);
		Connection connection = factory.newConnection();
		Channel channel = connection.createChannel();

		String binding = toUser.toLowerCase().replace(" ", "");
		channel.exchangeDeclare(Config.CHAT_ONLY_EXCHANGE, "direct", true);
		String fileLocation = Config.DEST_FILE_LOCATION.concat(msgType.name()
				.toLowerCase().concat("\\").concat(mediaFileName));
		byte[] decomBs = read(fileLocation);

		byte[] comBs = new DataCompressionHelper().compressData(decomBs);

		Payload p = new Payload();
		p.setCallName(CallName.DownloadMedia);
		p.setFromUser(toUser);
		p.setIsMedia(Config.TRUE);
		p.setMediaFileName(mediaFileName);
		p.setMessage(comBs);
		p.setMsgType(msgType);
		p.setTimeStamp(new Timestamp(System.currentTimeMillis()));
		p.setToUser(toUser);

		ObjectWriter ow = new ObjectMapper().writer()
				.withDefaultPrettyPrinter();
		String json = ow.writeValueAsString(p);

		AMQP.BasicProperties.Builder bob = new AMQP.BasicProperties.Builder();
		Integer prio = new Integer(0);
		AMQP.BasicProperties persistentBasic = bob.priority(prio)
				.contentType("application/octet-stream")
				.contentEncoding("UTF-8").build();

		channel.queueDeclare(binding, true, false, false, null);
		channel.queueBind(binding, Config.CHAT_ONLY_EXCHANGE, binding);
		channel.basicPublish(Config.CHAT_ONLY_EXCHANGE, binding,
				persistentBasic, json.getBytes("utf-8"));
		logger.info("Sent media back on queue " + binding + " and exchange "
				+ Config.CHAT_ONLY_EXCHANGE + " binding " + binding);

		channel.close();
		connection.close();

	}

	private byte[] read(String aInputFileName) {
		logger.info("Reading in binary file named : " + aInputFileName);
		File file = new File(aInputFileName);
		logger.info("File size: " + file.length());
		byte[] result = new byte[(int) file.length()];
		try {
			InputStream input = null;
			input = new FileInputStream(aInputFileName);
			input.read(result);
			input.close();
		} catch (FileNotFoundException ex) {
			logger.error("FNFE");
			ex.printStackTrace();
		} catch (IOException ex) {
			logger.error("IOE");
			ex.printStackTrace();
		}
		return result;
	}

}
