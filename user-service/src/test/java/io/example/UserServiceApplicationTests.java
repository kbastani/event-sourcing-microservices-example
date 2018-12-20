package io.example;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.kafka.test.rule.EmbeddedKafkaRule;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Assert;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class UserServiceApplicationTests {

    private static final String OUTPUT_TOPIC = "testEmbeddedOut";

    @MockBean
    private UserRepository userRepository;

    @Autowired
    private ApplicationContext applicationContext;

    @ClassRule
    public static EmbeddedKafkaRule embeddedKafka = new EmbeddedKafkaRule(1, true, OUTPUT_TOPIC);

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

}
