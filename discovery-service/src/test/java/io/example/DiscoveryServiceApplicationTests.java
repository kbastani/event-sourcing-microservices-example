package io.example;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Assert;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
public class DiscoveryServiceApplicationTests {

    @Test
    public void testsVoid() {
        Assert.state(true, "The tests for this service are turned off for this example.");
    }

}
