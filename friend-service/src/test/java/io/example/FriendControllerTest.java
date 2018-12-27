package io.example;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.Arrays;

@AutoConfigureWebTestClient
public class FriendControllerTest extends AbstractIntegrationTest {

    @Autowired
    private WebTestClient webClient;

    @Autowired
    private FriendRepository friendRepository;

    @Before
    public void setUp() {
        Friend f1 = new Friend(1L, 2L),
                f2 = new Friend(3L, 3L),
                f3 = new Friend(3L, 2L),
                f4 = new Friend(3L, 1L),
                f5 = new Friend(4L, 2L),
                f6 = new Friend(5L, 2L);

        friendRepository.saveAll(Arrays.asList(f1, f2, f3, f4, f5, f6)).subscribe();
    }

    @Test
    public void testCreateFriend() {
        Friend expected = new Friend(5L, 1L);
        this.webClient.post().uri("/v1/friends")
                .accept(MediaType.APPLICATION_JSON)
                .body(s -> Mono.just(expected), Friend.class)
                .exchange().returnResult(Friend.class).getResponseBody().single()
                .doOnNext(f -> Matchers.equalTo(f).matches(expected))
                .subscribe();
    }

    @Test
    public void testDeleteFriend() {
        this.webClient.delete().uri("/v1/friends/1")
                .accept(MediaType.APPLICATION_JSON)
                .exchange().returnResult(Friend.class).getResponseBody().then()
                .subscribe();

        friendRepository.getFriend(5L, 1L).map(f -> f)
                .doOnNext(f -> Matchers.isEmptyOrNullString().matches(f))
                .subscribe();
    }
}