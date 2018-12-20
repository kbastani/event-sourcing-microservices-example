package io.example;

import io.example.domain.friend.FriendRepository;
import io.example.domain.friend.entity.RankedUser;
import io.example.domain.user.UserRepository;
import io.example.domain.user.entity.User;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.kafka.test.rule.EmbeddedKafkaRule;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Arrays;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class RecommendationServiceTests {

    private static final String OUTPUT_TOPIC = "embeddedOutputTest";

    private static final Logger LOG = LoggerFactory.getLogger(RecommendationServiceTests.class);

    @Autowired
    private ApplicationContext applicationContext;

    @ClassRule
    public static EmbeddedKafkaRule embeddedKafka = new EmbeddedKafkaRule(1, true, OUTPUT_TOPIC);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FriendRepository friendRepository;

    @BeforeClass
    public static void setup() {
        System.setProperty("spring.cloud.stream.kafka.binder.brokers", embeddedKafka.getEmbeddedKafka().getBrokersAsString());
        System.setProperty("eureka.client.enabled", "false");
    }

    @After
    public void after() {
        embeddedKafka.after();
    }

    @Test
    public void contextLoads() {
        Assert.notNull(applicationContext, "The application context should not be null");
        Assert.notNull(userRepository, "The user repository should not be null");
        Assert.notEmpty(embeddedKafka.getEmbeddedKafka().getKafkaServers(),
                "The kafka server list should not be empty");
    }

    @Test
    @Transactional
    public void testSendReceive() {
        User kenny = new User(1L, "Kenny", "Bastani"),
                john = new User(2L, "John", "Doe"),
                paul = new User(3L, "Paul", "Doe"),
                ringo = new User(4L, "Ringo", "Doe"),
                george = new User(5L, "George", "Doe"),
                alice = new User(6L, "Alice", "Doe");

        userRepository.saveAll(Arrays.asList(kenny, john, paul, ringo, george, alice));

        friendRepository.addFriend(kenny.getId(), john.getId());
        friendRepository.addFriend(john.getId(), paul.getId());
        friendRepository.addFriend(paul.getId(), kenny.getId());
        friendRepository.addFriend(john.getId(), ringo.getId());
        friendRepository.addFriend(paul.getId(), ringo.getId());
        friendRepository.addFriend(john.getId(), george.getId());
        friendRepository.addFriend(john.getId(), alice.getId());
        friendRepository.addFriend(paul.getId(), alice.getId());

        ArrayList<RankedUser> rankedUser = friendRepository.recommendedFriends(1L);

        LOG.info(Arrays.toString(rankedUser.toArray()), "Friend recommendation output");

        Assert.notEmpty(rankedUser, "Friend recommendation must not return an empty list or null");

        org.junit.Assert.assertArrayEquals(rankedUser.toArray(),
                new RankedUser[]{new RankedUser(ringo, 2),
                        new RankedUser(alice, 2),
                        new RankedUser(george, 1)});
    }

}
