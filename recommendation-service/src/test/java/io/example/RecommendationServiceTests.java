package io.example;

import io.example.domain.friend.FriendRepository;
import io.example.domain.friend.entity.RankedUser;
import io.example.domain.user.UserRepository;
import io.example.domain.user.entity.User;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.rule.KafkaEmbedded;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.Arrays;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class RecommendationServiceTests {

    private static final String OUTPUT_TOPIC = "embeddedOutputTest";

    @ClassRule
    public static KafkaEmbedded embeddedKafka = new KafkaEmbedded(1, true, OUTPUT_TOPIC);

    @BeforeClass
    public static void setup() {
        System.setProperty("spring.cloud.stream.kafka.binder.brokers", embeddedKafka.getBrokersAsString());
        System.setProperty("eureka.client.enabled", "false");
        System.setProperty("spring.data.neo4j.uri", "file:///var/tmp/neo4j-test.db");
        System.setProperty("spring.profiles", "test");
    }

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FriendRepository friendRepository;

    @Test
    @Transactional
    public void testSendReceive() {
        userRepository.saveAll(Arrays.asList(new User(1L, "Kenny", "Bastani"),
                new User(2L, "John", "Doe"),
                new User(3L, "Paul", "Doe"),
                new User(4L, "Ringo", "Doe"),
                new User(5L, "George", "Doe"),
                new User(6L, "Alice", "Doe")));

        friendRepository.addFriend(1L, 2L);
        friendRepository.addFriend(2L, 3L);
        friendRepository.addFriend(3L, 1L);
        friendRepository.addFriend(2L, 4L);
        friendRepository.addFriend(3L, 4L);
        friendRepository.addFriend(2L, 5L);
        friendRepository.addFriend(2L, 6L);
        friendRepository.addFriend(3L, 6L);

        List<RankedUser> rankedUser = friendRepository.recommendedFriends(1L);

        System.out.println(Arrays.toString(rankedUser.toArray()));

        Assert.notEmpty(rankedUser, "Friend recommendation must not return an empty list or null");
    }

}
