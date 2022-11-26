package io.example;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Assert;

@SpringBootTest
@ActiveProfiles("test")
public class DiscoveryServiceApplicationTests {

    @Test
    public void testsVoid() {
        Assert.state(true, "The tests for this service are turned off for this example.");
    }

}
