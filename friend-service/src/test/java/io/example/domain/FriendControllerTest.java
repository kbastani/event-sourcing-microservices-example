package io.example.domain;

import io.example.AbstractIntegrationTest;
import io.example.domain.friend.Friend;
import io.example.domain.friend.FriendRepository;
import io.example.domain.friend.FriendService;
import io.example.domain.user.User;
import io.example.domain.user.UserClient;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.r2dbc.function.DatabaseClient;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.text.MessageFormat;

@AutoConfigureWebTestClient
public class FriendControllerTest extends AbstractIntegrationTest {

	@Autowired
	public WebTestClient webClient;

	@Autowired
	public FriendRepository friendRepository;

	@Autowired
	public FriendService friendService;

	@Autowired
	public DatabaseClient databaseClient;

	@MockBean
	private UserClient userClient;

	@Before
	public void setUp() {
		// Clear friend table
		databaseClient.execute().sql("DELETE FROM friend").then().block();
	}

	@After
	public void tearDown() {
		// Clear the friend table
		databaseClient.execute().sql("DELETE FROM friend").then().block();
	}

	@Test
	public void testGetFriendSucceeds() {

		Mockito.when(userClient.getUser(1L)).thenReturn(Mono.just(new User(1L, "Major", "Tom")));
		Mockito.when(userClient.getUser(6L)).thenReturn(Mono.just(new User(6L, "David", "Bowie")));

		friendService.create(new Friend(300L, 1L, 6L), System.out::println).then().block();

		// Test getting a friend
		StepVerifier.create(this.webClient.get().uri("/v1/friends/300")
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.returnResult(Friend.class).getResponseBody()).expectSubscription()
				.assertNext(u -> {
					Assert.assertEquals("Actual id must match expected", Long.valueOf(300L), u.getId());
					Assert.assertEquals("Actual userId match expected", 1L, u.getUserId().longValue());
					Assert.assertEquals("Actual friendId match expected", 6L, u.getFriendId().longValue());
				}).expectComplete().log().verify();
	}

	@Test
	public void testGetFriendsSucceeds() {

		Mockito.when(userClient.getUser(1L)).thenReturn(Mono.just(new User(1L, "Tom", "Sauyer")));
		Mockito.when(userClient.getUser(5L)).thenReturn(Mono.just(new User(5L, "Sally", "Jean")));
		Mockito.when(userClient.getUser(4L)).thenReturn(Mono.just(new User(4L, "Lady", "Gaga")));
		Mockito.when(userClient.getUser(3L)).thenReturn(Mono.just(new User(3L, "Major", "Tom")));

		friendService.create(new Friend(1L, 5L), System.out::println).then().block();
		friendService.create(new Friend(5L, 3L), System.out::println).then().block();
		friendService.create(new Friend(4L, 5L), System.out::println).then().block();

		// Test getting a friend
		StepVerifier.create(this.webClient.get().uri("/v1/users/5/friends")
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.returnResult(Friend.class).getResponseBody()).expectSubscription()
				.expectNextCount(1L).expectComplete().log().verify();
	}

	@Test
	public void testAddFriendSucceeds() {
		Friend expected = new Friend(20L, 36L);

		Mockito.when(userClient.getUser(20L)).thenReturn(Mono.just(new User(20L, "Major", "Tom")));
		Mockito.when(userClient.getUser(36L)).thenReturn(Mono.just(new User(36L, "David", "Bowie")));

		// Test creating a new friend using the transactional R2DBC client API
		StepVerifier.create(this.webClient.post().uri("/v1/users/20/commands/addFriend?friendId=36")
				.contentType(MediaType.APPLICATION_JSON)
				.exchange()
				.returnResult(Friend.class).getResponseBody()).expectSubscription()
				.assertNext(u -> {
					Assert.assertThat("Actual id must not be null", u.getId(), Matchers.notNullValue());
					Assert.assertEquals("Actual userId match expected", expected.getUserId(), u.getUserId());
					Assert.assertEquals("Actual friendId match expected", expected.getFriendId(), u.getFriendId());
					expected.setId(u.getId());
				}).expectComplete().log().verify();

		// Test that the transaction was not rolled back
		StepVerifier.create(this.webClient.get().uri("/v1/friends/" + expected.getId())
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.returnResult(Friend.class).getResponseBody()).expectSubscription()
				.assertNext(u -> {
					Assert.assertEquals("Actual id must match expected", expected.getId(), u.getId());
					Assert.assertEquals("Actual userId match expected", expected.getUserId(), u.getUserId());
					Assert.assertEquals("Actual friendId match expected", expected.getFriendId(), u.getFriendId());
				}).expectComplete().log().verify();
	}

	@Test
	public void testAddFriendFailsWhenConflict() {
		Friend expected = new Friend(77L, 66L);

		Mockito.when(userClient.getUser(77L)).thenReturn(Mono.just(new User(77L, "Major", "Tom")));
		Mockito.when(userClient.getUser(66L)).thenReturn(Mono.just(new User(66L, "David", "Bowie")));

		// Test creating a new friend using the transactional R2DBC client API
		StepVerifier.create(this.webClient.post()
				.uri(MessageFormat
						.format("/v1/users/{0}/commands/addFriend?friendId={1}", expected.getUserId(),
								expected.getFriendId()))
				.contentType(MediaType.APPLICATION_JSON)
				.exchange()
				.returnResult(Friend.class).getResponseBody()).expectSubscription()
				.assertNext(u -> {
					Assert.assertThat("Actual id must not be null", u.getId(), Matchers.notNullValue());
					Assert.assertEquals("Actual userId match expected", expected.getUserId(), u.getUserId());
					Assert.assertEquals("Actual friendId match expected", expected.getFriendId(), u.getFriendId());
					expected.setId(u.getId());
				}).expectComplete().log().verify();

		// Test creating the same friend relationship and expect a conflict failure
		StepVerifier.create(this.webClient.post()
				.uri(MessageFormat
						.format("/v1/users/{0}/commands/addFriend?friendId={1}", expected.getUserId(),
								expected.getFriendId()))
				.exchange().returnResult(RuntimeException.class)
				.getResponseBody().single()).expectSubscription()
				.assertNext(e -> Assert.assertEquals("409 The friendship already exists", e.getMessage()))
				.expectComplete().log().verify();
	}

	@Test
	public void testRemoveFriendSucceeds() {
		Friend expected = new Friend(74L, 51L, 26L);

		Mockito.when(userClient.getUser(51L)).thenReturn(Mono.just(new User(51L, "Major", "Tom")));
		Mockito.when(userClient.getUser(26L)).thenReturn(Mono.just(new User(26L, "David", "Bowie")));

		friendService.create(expected, System.out::println).then().block();

		// Test creating a new friend using the transactional R2DBC client API
		StepVerifier.create(this.webClient.post().uri("/v1/users/51/commands/removeFriend?friendId=26")
				.contentType(MediaType.APPLICATION_JSON)
				.exchange()
				.returnResult(Friend.class).getResponseBody()).expectSubscription()
				.assertNext(System.out::print)
				.expectComplete().log().verify();

		// Test that the transaction was not rolled back
		StepVerifier.create(this.webClient.get().uri("/v1/friends/" + expected.getId())
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.returnResult(Friend.class).getResponseBody()).expectSubscription()
				.expectComplete().log().verify();
	}
}