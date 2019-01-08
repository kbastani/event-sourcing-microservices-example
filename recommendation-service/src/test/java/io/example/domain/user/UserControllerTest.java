package io.example.domain.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.example.domain.friend.FriendRepository;
import io.example.domain.user.entity.User;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.kafka.test.rule.EmbeddedKafkaRule;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Arrays;
import java.util.Date;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureWebTestClient
@ActiveProfiles("test")
public class UserControllerTest {

	private static final String OUTPUT_TOPIC = "users";

	@ClassRule
	public static EmbeddedKafkaRule embeddedKafka =
			new EmbeddedKafkaRule(1, true, OUTPUT_TOPIC);

	@Autowired
	private WebTestClient webClient;

	@Autowired
	private FriendRepository friendRepository;

	@Autowired
	private UserRepository userRepository;

	@BeforeClass
	public static void setup() {
		System.setProperty("spring.cloud.stream.kafka.binder.brokers", embeddedKafka.getEmbeddedKafka().getBrokersAsString());
		System.setProperty("eureka.client.enabled", "false");
	}

	@Before
	public void setUp() {
		User kenny = new User(1L, "Kenny", "Bastani"),
				john = new User(2L, "John", "Doe"),
				paul = new User(3L, "Paul", "Doe"),
				ringo = new User(4L, "Ringo", "Doe"),
				george = new User(5L, "George", "Doe"),
				alice = new User(6L, "Alice", "Doe");

		userRepository.saveAll(Arrays.asList(kenny, john, paul, ringo, george, alice));

		friendRepository.addFriend(kenny.getId(), john.getId(), new Date().getTime(), new Date().getTime());
		friendRepository.addFriend(john.getId(), paul.getId(), new Date().getTime(), new Date().getTime());
		friendRepository.addFriend(paul.getId(), kenny.getId(), new Date().getTime(), new Date().getTime());
		friendRepository.addFriend(john.getId(), ringo.getId(), new Date().getTime(), new Date().getTime());
		friendRepository.addFriend(paul.getId(), ringo.getId(), new Date().getTime(), new Date().getTime());
		friendRepository.addFriend(john.getId(), george.getId(), new Date().getTime(), new Date().getTime());
		friendRepository.addFriend(john.getId(), alice.getId(), new Date().getTime(), new Date().getTime());
		friendRepository.addFriend(paul.getId(), alice.getId(), new Date().getTime(), new Date().getTime());
	}

	@After
	public void after() {
		embeddedKafka.after();
	}


	@Test
	public void testFindMutualFriends() throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		this.webClient.get().uri("/v1/users/1/commands/findMutualFriends?friendId=3")
				.accept(MediaType.APPLICATION_JSON)
				.exchange().expectBody()
				.json(mapper.writeValueAsString(friendRepository.mutualFriends(1L, 3L).toList().toArray()));
	}

	@Test
	public void testRecommendFriends() throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		this.webClient.get().uri("/v1/users/1/commands/recommendFriends")
				.accept(MediaType.APPLICATION_JSON)
				.exchange().expectBody()
				.json(mapper.writeValueAsString(friendRepository.recommendedFriends(1L).toList().toArray()));
	}
}