package com.messaging.processor;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

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

public class GetConversationHistoryProcesor {

	private static final Logger logger = Logger
			.getLogger(GetConversationHistoryProcesor.class);

	public void getHistory(String fromUser, String toUser) throws IOException {
		logger.info("Getting conversation history for users : " + fromUser
				+ " and " + toUser);
		// 1. locate file by name fromusertouser.txt
		// 2. read from it and create a list.
		// 3. send payload.

		ConversationHistoryHelper chh = new ConversationHistoryHelper();
		String fileName = chh.getFileName(toUser, fromUser);
		List<String> history = new ArrayList<String>();
		history = chh.readConversationHistory(fileName);

		StringBuffer chatBuff = new StringBuffer();
		for (String chat : history) {
			chatBuff.append(chat);
			chatBuff.append("&");
		}

		logger.info("chat buff " + chatBuff.toString());
		Payload p = new Payload();
		p.setCallName(CallName.GetConversationHistory);
		p.setFromUser(fromUser);
		p.setIsMedia(Config.FALSE);
		p.setMediaFileName("");
		p.setMessage(chatBuff.toString().getBytes());
		p.setMsgType(MessageType.Text);
		p.setTimeStamp(new Timestamp(System.currentTimeMillis()));
		p.setToUser(fromUser);

		ObjectWriter ow = new ObjectMapper().writer()
				.withDefaultPrettyPrinter();
		String json = ow.writeValueAsString(p);

		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost(Config.IP);
		factory.setUsername(Config.USERNAME);
		factory.setPassword(Config.PASSWORD);
		Connection connection = factory.newConnection();
		Channel channel = connection.createChannel();

		String binding = fromUser.toLowerCase().replace(" ", "");
		channel.exchangeDeclare(Config.CHAT_ONLY_EXCHANGE, "direct", true);
		channel.queueDeclare(binding, true, false, false, null);
		channel.queueBind(binding, Config.CHAT_ONLY_EXCHANGE, binding);
		channel.basicPublish(Config.CHAT_ONLY_EXCHANGE, binding, null,
				json.getBytes("utf-8"));

		logger.info("Sent Chat back on queue " + binding + " and exchange "
				+ Config.CHAT_ONLY_EXCHANGE + " binding " + binding);

		channel.close();
		connection.close();
	}
	
	public static void main(String[] args) {
		try {
			new GetConversationHistoryProcesor().getHistory("ruby", "nitin");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

