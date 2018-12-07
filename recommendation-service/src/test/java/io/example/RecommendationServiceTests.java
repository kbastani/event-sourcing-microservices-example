package io.example;

import io.example.domain.user.entity.User;
import io.example.domain.user.UserRepository;
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

    @Test
    @Transactional
    public void testSendReceive() {
        userRepository.save(new User(1234L, "Kenny", "Bastani"));
    }

}
