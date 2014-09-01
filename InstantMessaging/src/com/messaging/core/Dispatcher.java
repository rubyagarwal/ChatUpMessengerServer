package com.messaging.core;

import java.io.IOException;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;

import com.messaging.data.CallName;
import com.messaging.data.Payload;
import com.messaging.processor.ChatMessageProcessor;
import com.messaging.processor.DownloadMediaProcessor;
import com.messaging.processor.GetActiveConvUsers;
import com.messaging.processor.GetConversationHistoryProcesor;
import com.messaging.processor.GetFriendsProcessor;
import com.messaging.processor.SignUpUserProcessor;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;

public class Dispatcher {

	private static final Logger logger = Logger.getLogger(Dispatcher.class);

	public void start() throws IOException, InterruptedException {
		BasicConfigurator.configure();
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost(Config.IP);
		factory.setUsername(Config.USERNAME);
		factory.setPassword(Config.PASSWORD);
		Connection connection = factory.newConnection();
		Channel channel = connection.createChannel();

		channel.exchangeDeclare(Config.ALL_MSGS_EXCHANGE, "fanout", true);
		String queueName = channel.queueDeclare().getQueue();
		channel.queueBind(queueName, Config.ALL_MSGS_EXCHANGE, "");
		logger.info("[Consumer] Waiting for messages on exchange:queue "
				+ Config.ALL_MSGS_EXCHANGE + " : " + queueName);

		QueueingConsumer consumer = new QueueingConsumer(channel);
		channel.basicConsume(queueName, true, consumer);

		while (true) {
			QueueingConsumer.Delivery delivery = consumer.nextDelivery();
			String message = new String(delivery.getBody());
			ObjectMapper objectMapper = new ObjectMapper();
			Payload p = (Payload) objectMapper
					.readValue(message, Payload.class);
			logger.info("RECEIVED : " + p.toString());
			// String[] slpits = message.split(":");
			CallName callName = p.getCallName();
			if (CallName.SignUp.equals(callName)) {
				new SignUpUserProcessor().signUpUser(p.getFromUser());
			} else if (CallName.GetActiveUsers.equals(callName)) {
				new GetFriendsProcessor().getActiveUsersForSignUpUser(p
						.getFromUser());
			} else if (CallName.Chat.equals(callName)) {
				new ChatMessageProcessor().directChatMessage(p.getToUser(),
						p.getFromUser(), p.getIsMedia(), p.getMessage(),
						p.getMediaFileName(), p.getMsgType());
			} else if (CallName.GetActiveUsers.equals(callName)) {
				new GetActiveConvUsers().getActiveUSers(p.getToUser(),
						p.getFromUser());
			} else if (CallName.DownloadMedia.equals(callName)) {
				new DownloadMediaProcessor().sendRequestedMediaToUser(
						p.getToUser(), p.getMediaFileName(), p.getMsgType());
			} else if (CallName.GetConversationHistory.equals(p.getCallName())) {
				new GetConversationHistoryProcesor().getHistory(
						p.getFromUser(), p.getToUser());
			}
		}
	}
}
