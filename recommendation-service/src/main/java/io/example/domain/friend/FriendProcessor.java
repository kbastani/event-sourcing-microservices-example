package io.example.domain.friend;

import io.example.domain.user.UserRepository;
import io.example.domain.user.entity.User;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.logging.Logger;

/**
 * Message stream listener for Friend events. Maps types of events
 * to a graph operation that replicates a connected view of domain data
 * across microservices.
 *
 * @author Kenny Bastani
 */
@Configuration
@EnableBinding(FriendSink.class)
@Transactional
public class FriendProcessor {

	private final Logger log = Logger.getLogger(FriendProcessor.class.getName());
	private final UserRepository userRepository;

	public FriendProcessor(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@StreamListener(value = FriendSink.INPUT)
	public void apply(Message<FriendEvent> friendEvent) {

		log.info("Event received: " + friendEvent.toString());

		User user = userRepository.findUserByUserId(friendEvent.getPayload().getSubject().getUserId());
		User friend = userRepository.findUserByUserId(friendEvent.getPayload().getSubject().getFriendId());

		if (user == null || friend == null) {
			throw new RuntimeException("Invalid user identifier for " + friendEvent.getPayload().getEventType() +
					" operation on one or more users: " + Arrays.asList(user, friend).toString());
		}

		switch (friendEvent.getPayload().getEventType()) {
			case FRIEND_ADDED:
				userRepository.addFriend(user.getId(), friend.getId(),
						friendEvent.getPayload().getSubject().getCreatedAt().getTime(),
						friendEvent.getPayload().getSubject().getUpdatedAt().getTime());
				break;
			case FRIEND_REMOVED:
				userRepository.removeFriend(user.getId(), friend.getId());
				break;
		}
	}
}
