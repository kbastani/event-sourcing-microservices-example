package io.example.domain;

import io.example.AbstractIntegrationTest;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.data.r2dbc.function.DatabaseClient;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.test.StepVerifier;

@AutoConfigureWebTestClient
public class UserControllerTest extends AbstractIntegrationTest {

	@Autowired
	public WebTestClient webClient;

	@Autowired
	public UserRepository userRepository;

	@Autowired
	public DatabaseClient databaseClient;

	@Before
	public void setUp() {
		// Clear user table
		databaseClient.execute().sql("DELETE FROM users").then().block();

		// Insert test data for testUpdateUser()
		databaseClient.insert().into(User.class).using(new User(200L, "Jane", "Doe")).then().block();

		// Insert test data for testGetUser()
		databaseClient.insert().into(User.class).using(new User(300L, "Grace", "Hopper")).then().block();
	}

	@After
	public void tearDown() {
		// Clear the user table
		databaseClient.execute().sql("DELETE FROM users").then().block();
	}

	@Test
	public void testGetUserSucceeds() {
		// Test getting a user
		StepVerifier.create(this.webClient.get().uri("/v1/users/300")
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.returnResult(User.class).getResponseBody()).expectSubscription()
				.assertNext(u -> {
					Assert.assertEquals("Actual userId must match expected", Long.valueOf(300L), u.getId());
					Assert.assertEquals("Actual firstName match expected", "Grace", u.getFirstName());
					Assert.assertEquals("Actual lastName match expected", "Hopper", u.getLastName());
				}).expectComplete().log().verify();
	}

	@Test
	public void testCreateUserSucceeds() {
		User expected = new User("Kenny", "Bastani");

		// Test creating a new user using the transactional R2DBC client API
		StepVerifier.create(this.webClient.post().uri("/v1/users")
				.contentType(MediaType.APPLICATION_JSON)
				.syncBody(expected)
				.exchange()
				.returnResult(User.class).getResponseBody()).expectSubscription()
				.assertNext(u -> {
					Assert.assertThat("Actual userId must not be null", u.getId(), Matchers.notNullValue());
					Assert.assertEquals("Actual firstName match expected", expected.getFirstName(), u.getFirstName());
					Assert.assertEquals("Actual lastName match expected", expected.getLastName(), u.getLastName());
					expected.setId(u.getId());
				}).expectComplete().log().verify();

		// Test that the transaction was not rolled back
		StepVerifier.create(this.webClient.get().uri("/v1/users/" + expected.getId())
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.returnResult(User.class).getResponseBody()).expectSubscription()
				.assertNext(u -> {
					Assert.assertEquals("Actual userId must match expected", expected.getId(), u.getId());
					Assert.assertEquals("Actual firstName match expected", expected.getFirstName(), u.getFirstName());
					Assert.assertEquals("Actual lastName match expected", expected.getLastName(), u.getLastName());
				}).expectComplete().log().verify();
	}

	@Test
	public void testUpdateUserSucceeds() {
		User expected = new User(200L, "Jean", "Gray");

		// Test creating a new user using the transactional R2DBC client API
		StepVerifier.create(this.webClient.put().uri("/v1/users/200")
				.contentType(MediaType.APPLICATION_JSON)
				.syncBody(expected)
				.exchange()
				.returnResult(User.class).getResponseBody().single()).expectSubscription()
				.assertNext(u -> {
					System.out.println(u);
					Assert.assertEquals("Actual userId must match expected", expected.getId(), u.getId());
					Assert.assertEquals("Actual firstName match expected", expected.getFirstName(), u.getFirstName());
					Assert.assertEquals("Actual lastName match expected", expected.getLastName(), u.getLastName());
				}).expectComplete().log().verify();


		// Test that the transaction was not rolled back
		StepVerifier.create(this.webClient.get().uri("/v1/users/200")
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.returnResult(User.class).getResponseBody().single()).expectSubscription()
				.assertNext(u -> {
					Assert.assertEquals("Actual userId must match expected", expected.getId(), u.getId());
					Assert.assertEquals("Actual firstName match expected", expected.getFirstName(), u.getFirstName());
					Assert.assertEquals("Actual lastName match expected", expected.getLastName(), u.getLastName());
				}).expectComplete().log().verify();
	}
}