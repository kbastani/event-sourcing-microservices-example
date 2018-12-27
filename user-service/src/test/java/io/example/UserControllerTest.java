package io.example;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

@AutoConfigureWebTestClient
public class UserControllerTest extends AbstractIntegrationTest {

    @Autowired
    private WebTestClient webClient;

    @Autowired
    private UserRepository userRepository;

    @Test
    public void testCreateUser() {
        User expected = new User("Kenny", "Bastani");
        expected.setId(1L);

        this.webClient.post().uri("/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .syncBody(expected)
                .exchange()
                .returnResult(User.class)
                .getResponseBody()
                .single()
                .doOnNext(f -> Matchers.equalTo(f).matches(expected))
                .subscribe();
    }
}