package io.example.domain;

import io.example.AbstractUnitTest;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.data.r2dbc.function.DatabaseClient;
import reactor.test.StepVerifier;

/**
 * Tests the transactional guarantees of the {@link UserService}.
 *
 * @author Kenny Bastani
 */
@EnableAutoConfiguration
public class UserServiceTests extends AbstractUnitTest {

	@Autowired
	private DatabaseClient databaseClient;

	@Autowired
	private UserService userService;

	@Before
	public void setUp() {
		// Clear user table
		databaseClient.execute().sql("DELETE FROM users").then().block();

		// Insert test data for transactionGetSucceeds()
		databaseClient.insert().into(User.class).using(new User(3L, "Jane", "Doe")).then().block();
	}

	@After
	public void tearDown() {
		// Clear the user table
		databaseClient.execute().sql("DELETE FROM users").then().block();
	}

	@Test
	public void transactionCreateSucceeds() {
		User expected = new User(1L, "Kenny", "Bastani");

		// Create a new user transaction without throwing an error in the callback
		StepVerifier.create(userService.create(expected, user -> System.out.println(user.toString())))
				.expectSubscription()
				.assertNext(u -> {
					Assert.assertEquals("Actual userId match expected", expected.getId(), u.getId());
					Assert.assertEquals("Actual firstName match expected", expected.getFirstName(), u.getFirstName());
					Assert.assertEquals("Actual lastName match expected", expected.getLastName(), u.getLastName());
				}).expectComplete().log().verify();

		// Read the result of the previous transaction and see that it succeeded
		StepVerifier.create(userService.find(1L))
				.expectSubscription()
				.assertNext(u -> {
					Assert.assertEquals("Actual userId match expected", expected.getId(), u.getId());
					Assert.assertEquals("Actual firstName match expected", expected.getFirstName(), u.getFirstName());
					Assert.assertEquals("Actual lastName match expected", expected.getLastName(), u.getLastName());
				}).expectComplete().log().verify();
	}

	@Test
	public void transactionCreateFails() {
		User expected = new User(2L, "Kenny", "Bastani");

		// Create a new user transaction and throw a runtime exception in the callback
		StepVerifier.create(userService.create(expected, user -> {
			// This error should rollback the transaction
			throw new RuntimeException();
		})).expectSubscription()
				.expectError()
				.log()
				.verify();

		// Read the result of the previous transaction and expect that no users are emitted from the publisher
		StepVerifier.create(userService.find(2L)).expectSubscription()
				.expectError()
				.verify();
	}

	@Test
	public void transactionGetSucceeds() {
		User expected = new User(3L, "Jane", "Doe");

		StepVerifier.create(userService.find(3L)).expectSubscription()
				.assertNext(u -> {
					Assert.assertEquals("Actual userId match expected", expected.getId(), u.getId());
					Assert.assertEquals("Actual firstName match expected", expected.getFirstName(), u.getFirstName());
					Assert.assertEquals("Actual lastName match expected", expected.getLastName(), u.getLastName());
				}).expectComplete().log().verify();
	}

	@Test
	public void transactionGetFails() {
		StepVerifier.create(userService.find(4L)).expectSubscription().expectError().log().verify();
	}

	@Test
	public void transactionUpdateSucceeds() {
		User expected = new User(5L, "John", "Doe");

		// Create a new user transaction without throwing an error in the callback
		StepVerifier.create(userService.create(expected, user -> System.out.println(user.toString())))
				.expectSubscription()
				.assertNext(u -> {
					Assert.assertEquals("Actual userId match expected", expected.getId(), u.getId());
					Assert.assertEquals("Actual firstName match expected", expected.getFirstName(), u.getFirstName());
					Assert.assertEquals("Actual lastName match expected", expected.getLastName(), u.getLastName());
				}).expectComplete().log().verify();

		// Execute an update by changing the user's name from John Doe to Johnny Appleseed
		expected.setFirstName("Johnny");
		expected.setLastName("Appleseed");

		// Read the result of the previous transaction and see that it succeeded
		StepVerifier.create(userService.update(expected, System.out::println))
				.expectSubscription()
				.assertNext(u -> {
					Assert.assertEquals("Actual userId match expected", expected.getId(), u.getId());
					Assert.assertEquals("Actual firstName match expected", expected.getFirstName(), u.getFirstName());
					Assert.assertEquals("Actual lastName match expected", expected.getLastName(), u.getLastName());
				}).expectComplete().log().verify();
	}

	@Test
	public void transactionUpdateFails() {
		User expected = new User(6L, "John", "Doe");

		// Create a new user transaction without throwing an error in the callback
		StepVerifier.create(userService.create(expected, user -> System.out.println(user.toString())))
				.expectSubscription()
				.assertNext(u -> {
					Assert.assertEquals("Actual userId match expected", u.getId(), expected.getId());
					Assert.assertEquals("Actual firstName match expected", u.getFirstName(), expected.getFirstName());
					Assert.assertEquals("Actual lastName match expected", u.getLastName(), expected.getLastName());
				}).expectComplete().log().verify();

		// Execute an update by changing the user's name from John Doe to Johnny Appleseed
		expected.setFirstName("Johnny");
		expected.setLastName("Appleseed");

		// Read the result of the previous transaction and see that it succeeded
		StepVerifier.create(userService.update(expected, user -> {
			// Throw a new runtime exception and cause this transaction to rollback
			throw new RuntimeException(String.format("Rollback the transaction for: %s", user.toString()));
		})).expectSubscription().thenRequest(1L).expectError().log().verify();

		// Reset the expected user to the original state
		expected.setFirstName("John");
		expected.setLastName("Doe");

		// Verify that the user was not updated
		StepVerifier.create(userService.find(6L)).expectSubscription()
				.assertNext(u -> {
					Assert.assertEquals("Actual userId match expected", u.getId(), expected.getId());
					Assert.assertEquals("Actual firstName match expected", u.getFirstName(), expected.getFirstName());
					Assert.assertEquals("Actual lastName match expected", u.getLastName(), expected.getLastName());
				}).expectComplete().log().verify();
	}
}
