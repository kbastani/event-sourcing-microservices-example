package io.example;

import io.example.domain.User;
import io.example.domain.UserRepository;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.data.r2dbc.function.DatabaseClient;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

@AutoConfigureWebTestClient
public class UserControllerTest extends AbstractIntegrationTest {

    @Autowired
    public WebTestClient webClient;

    @Autowired
    public UserRepository userRepository;

    @Autowired
    public DatabaseClient databaseClient;

    @Test
    public void testCreateUser() {
        User expected = new User("Kenny", "Bastani");

        User actual = this.webClient.post().uri("/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .syncBody(expected)
                .exchange()
                .returnResult(User.class)
                .getResponseBody().blockFirst();

        assert actual != null;
        System.out.println(actual.toString());
        Assert.assertEquals("Expected user must match actual", expected.getFirstName(), actual.getFirstName());
    }

    @Test
    public void testUpdateUser() {
        User expected = new User(1L, "Ken", "Bastani");

        Mono<Void> result = databaseClient.insert().into(User.class)
                .table("users")
                .using(expected)
                .then();

        result.block();

        expected = userRepository.getUser(1L).block();

        expected.setFirstName("Kenny");

        User actual = this.webClient.put().uri("/v1/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .syncBody(expected)
                .exchange()
                .returnResult(User.class)
                .getResponseBody().blockFirst();

        assert actual != null;
        System.out.println(actual.toString());
        Assert.assertEquals("Expected user must match actual", expected.getFirstName(), actual.getFirstName());
    }
}