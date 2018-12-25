package io.example.util;

import io.example.User;
import org.springframework.stereotype.Component;

@Component
public class KafkaDualWriter extends AbstractDualWriter<User> {
}
