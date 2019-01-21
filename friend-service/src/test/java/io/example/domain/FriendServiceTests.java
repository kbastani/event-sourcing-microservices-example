package io.example.domain;

import io.example.AbstractUnitTest;
import io.example.domain.friend.Friend;
import io.example.domain.friend.FriendRepository;
import io.example.domain.friend.FriendService;
import io.example.domain.user.User;
import io.example.domain.user.UserClient;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.r2dbc.function.DatabaseClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.function.Function;

/**
 * Tests the transactional guarantees of the {@link FriendService}.
 *
 * @author Kenny Bastani
 */
@EnableAutoConfiguration
public class FriendServiceTests extends AbstractUnitTest {

	private Function<Friend, Publisher<Void>> friendToError = (friend) -> Mono.just(friend)
			.doOnNext((f) -> {
				throw new RuntimeException();
			}).then();

	@Autowired
	private DatabaseClient databaseClient;

	@Autowired
	private FriendService friendService;

	@Autowired
	private FriendRepository friendRepository;

	@MockBean
	private UserClient userClient;

	@Before
	public void setUp() {
		// Clear friend table
		databaseClient.execute().sql("DELETE FROM friend").then().block();

		Friend f1 = new Friend(1L, 2L),
				f2 = new Friend(3L, 5L),
				f3 = new Friend(3L, 2L),
				f4 = new Friend(3L, 1L),
				f5 = new Friend(4L, 2L),
				f6 = new Friend(5L, 2L),
				f7 = new Friend(300L, 1L, 5L),
				f8 = new Friend(400L, 22L, 37L),
				f9 = new Friend(500L, 33L, 48L),
				f10 = new Friend(700L, 21L, 46L),
				f11 = new Friend(800L, 35L, 49L);

		friendRepository.saveAll(Arrays.asList(f1, f2, f3, f4, f5, f6, f7, f8, f9, f10, f11)).blockLast();
	}

	@After
	public void tearDown() {
		// Clear the friend table
		databaseClient.execute().sql("DELETE FROM friend").then().block();
	}

	@Test
	public void transactionCreateSucceeds() {
		Friend expected = new Friend(917L, 68L, 45L);

		Mockito.when(userClient.getUser(68L)).thenReturn(Mono.just(new User(68L, "Kenny", "Bastani")));
		Mockito.when(userClient.getUser(45L)).thenReturn(Mono.just(new User(45L, "Jean", "Gray")));

		// Create a new friend transaction without throwing an error in the callback
		StepVerifier.create(friendService.create(expected,
				friend -> Mono.fromRunnable(() -> System.out.println(friend.toString()))))
				.expectSubscription()
				.assertNext(u -> {
					Assert.assertEquals("Actual id match expected", expected.getId(), u.getId());
					Assert.assertEquals("Actual userId match expected", expected.getUserId(), u.getUserId());
					Assert.assertEquals("Actual friendId match expected", expected.getFriendId(), u.getFriendId());
				}).expectComplete().log().verify();

		// Read the result of the previous transaction and see that it succeeded
		StepVerifier.create(friendService.find(917L))
				.expectSubscription()
				.assertNext(u -> {
					Assert.assertEquals("Actual id match expected", expected.getId(), u.getId());
					Assert.assertEquals("Actual userId match expected", expected.getUserId(), u.getUserId());
					Assert.assertEquals("Actual friendId match expected", expected.getFriendId(), u.getFriendId());
				}).expectComplete().log().verify();
	}

	@Test
	public void transactionCreateFails() {
		Friend expected = new Friend(2L, 46L, 23L);

		Mockito.when(userClient.getUser(46L)).thenReturn(Mono.just(new User(46L, "Kenny", "Bastani")));
		Mockito.when(userClient.getUser(23L)).thenReturn(Mono.just(new User(23L, "Jean", "Gray")));

		// Create a new friend transaction and throw a runtime exception in the callback
		StepVerifier.create(friendService.create(expected, friendToError)).expectSubscription()
				.expectError()
				.log()
				.verify();

		// Read the result of the previous transaction and expect that no friends are emitted from the publisher
		StepVerifier.create(friendService.find(2L)).expectSubscription()
				.expectComplete()
				.verify();
	}

	@Test
	public void transactionCreateFailsOnFkConstraint() {
		Friend expected = new Friend(89L, 11L);

		Mockito.when(userClient.getUser(89L)).thenReturn(Mono.just(new User(89L, "Kenny", "Bastani")));
		Mockito.when(userClient.getUser(11L)).thenReturn(Mono.empty());

		StepVerifier.create(friendService.create(expected,
				friend -> Mono.fromRunnable(() -> System.out.println(friend.toString())))).expectSubscription()
				.expectError()
				.log()
				.verify();
	}

	@Test
	public void transactionCreateFailsWhenConflict() {
		Friend expected = new Friend(89L, 11L);

		Mockito.when(userClient.getUser(89L)).thenReturn(Mono.just(new User(89L, "Kenny", "Bastani")));
		Mockito.when(userClient.getUser(11L)).thenReturn(Mono.just(new User(11L, "Jean", "Gray")));

		friendService.create(expected, friend -> Mono.fromRunnable(() -> System.out.println(friend.toString())))
				.block();

		// Attempt to create a new friend relationship when the relationship already exists
		StepVerifier.create(friendService.create(expected,
				friend -> Mono.fromRunnable(() -> System.out.println(friend.toString())))).expectSubscription()
				.expectError()
				.log()
				.verify();
	}

	@Test
	public void transactionGetSucceeds() {
		Friend expected = new Friend(500L, 33L, 48L);

		Mockito.when(userClient.getUser(33L)).thenReturn(Mono.just(new User(33L, "Kenny", "Bastani")));
		Mockito.when(userClient.getUser(48L)).thenReturn(Mono.just(new User(48L, "Jean", "Gray")));

		friendService.create(expected, friend -> Mono.fromRunnable(() -> System.out.println(friend.toString())))
				.block();

		StepVerifier.create(friendService.find(500L)).expectSubscription()
				.assertNext(u -> {
					System.out.println(u);
					Assert.assertEquals("Actual id match expected", expected.getId(), u.getId());
					Assert.assertEquals("Actual userId match expected", expected.getUserId(), u.getUserId());
					Assert.assertEquals("Actual friendId match expected", expected.getFriendId(), u.getFriendId());
					Assert.assertNotNull("Created at should not be null", u.getCreatedAt());
					Assert.assertNotNull("Updated at at should not be null", u.getUpdatedAt());
				}).expectComplete().log().verify();
	}

	@Test
	public void transactionGetFails() {
		StepVerifier.create(friendService.find(700L)).expectSubscription().expectComplete().log().verify();
	}

	@Test
	public void transactionUpdateSucceeds() {
		Friend expected = new Friend(700L, 21L, 46L);

		Mockito.when(userClient.getUser(21L)).thenReturn(Mono.just(new User(21L, "Kenny", "Bastani")));
		Mockito.when(userClient.getUser(46L)).thenReturn(Mono.just(new User(46L, "Jean", "Gray")));

		// Create a new friend transaction without throwing an error in the callback
		StepVerifier.create(friendService.create(expected,
				friend -> Mono.fromRunnable(() -> System.out.println(friend.toString()))))
				.expectSubscription()
				.assertNext(u -> {
					Assert.assertEquals("Actual id match expected", expected.getId(), u.getId());
					Assert.assertEquals("Actual userId match expected", expected.getUserId(), u.getUserId());
					Assert.assertEquals("Actual friendId match expected", expected.getFriendId(), u.getFriendId());
				}).expectComplete().log().verify();

		// Execute an update by changing the friend's name from John Doe to Johnny Appleseed
		expected.setFriendId(33L);
		expected.setUserId(55L);

		// Read the result of the previous transaction and see that it succeeded
		StepVerifier.create(friendService.update(expected, System.out::println))
				.expectSubscription()
				.assertNext(u -> {
					Assert.assertEquals("Actual id match expected", expected.getId(), u.getId());
					Assert.assertEquals("Actual userId match expected", expected.getUserId(), u.getUserId());
					Assert.assertEquals("Actual friendId match expected", expected.getFriendId(), u.getFriendId());
				}).expectComplete().log().verify();
	}

	@Test
	public void transactionUpdateFails() {
		Friend expected = new Friend(900L, 35L, 49L);

		Mockito.when(userClient.getUser(35L)).thenReturn(Mono.just(new User(35L, "Kenny", "Bastani")));
		Mockito.when(userClient.getUser(49L)).thenReturn(Mono.just(new User(49L, "Jean", "Gray")));

		// Create a new friend transaction without throwing an error in the callback
		StepVerifier.create(friendService.create(expected,
				friend -> Mono.fromRunnable(() -> System.out.println(friend.toString()))))
				.expectSubscription()
				.assertNext(u -> {
					Assert.assertEquals("Actual id match expected", u.getId(), expected.getId());
					Assert.assertEquals("Actual userId match expected", expected.getUserId(), u.getUserId());
					Assert.assertEquals("Actual friendId match expected", expected.getFriendId(), u.getFriendId());
				}).expectComplete().log().verify();

		// Execute an update by changing the friend's name from John Doe to Johnny Appleseed
		expected.setUserId(44L);
		expected.setFriendId(54L);

		// Read the result of the previous transaction and see that it succeeded
		StepVerifier.create(friendService.update(expected, friend -> {
			// Throw a new runtime exception and cause this transaction to rollback
			throw new RuntimeException(String.format("Rollback the transaction for: %s", friend.toString()));
		})).expectSubscription().thenRequest(1L).expectError().log().verify();

		// Reset the expected friend to the original state
		expected.setUserId(35L);
		expected.setFriendId(49L);

		// Verify that the friend was not updated
		StepVerifier.create(friendService.find(900L)).expectSubscription()
				.assertNext(u -> {
					Assert.assertEquals("Actual id match expected", u.getId(), expected.getId());
					Assert.assertEquals("Actual userId match expected", expected.getUserId(), u.getUserId());
					Assert.assertEquals("Actual friendId match expected", expected.getFriendId(), u.getFriendId());
				}).expectComplete().log().verify();
	}

	@Test
	public void transactionDeleteSucceeds() {
		Friend expected = new Friend(1000L, 53L, 93L);

		Mockito.when(userClient.getUser(53L)).thenReturn(Mono.just(new User(53L, "Kenny", "Bastani")));
		Mockito.when(userClient.getUser(93L)).thenReturn(Mono.just(new User(93L, "Jean", "Gray")));

		friendService.create(expected,
				friend -> Mono.fromRunnable(() -> System.out.println(friend.toString()))).block();

		// Delete the friendship where the userId is 53 and the friendId is 93
		StepVerifier.create(friendService.delete(expected, System.out::println))
				.expectSubscription()
				.assertNext(System.out::println).expectComplete().log().verify();

		// Check to see that the delete transaction was committed
		StepVerifier.create(friendService.find(1000L)).expectSubscription().expectComplete().log().verify();
	}

	@Test
	public void friendExistsSucceeds() {
		Friend expected = new Friend(421L, 221L);

		Mockito.when(userClient.getUser(421L)).thenReturn(Mono.just(new User(21L, "Kenny", "Bastani")));
		Mockito.when(userClient.getUser(221L)).thenReturn(Mono.just(new User(46L, "Jean", "Gray")));

		friendService.create(expected,
				friend -> Mono.fromRunnable(() -> System.out.println(friend.toString()))).block();

		// Delete the friendship where the userId is 421 and the friendId is 221
		StepVerifier.create(friendService.exists(expected.getUserId(), expected.getFriendId()))
				.expectSubscription()
				.expectNext(true).expectComplete().log().verify();
	}
}
