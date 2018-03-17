package io.example;

import io.example.user.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/graph")
public class GraphController {

    private final AggregateRepository aggregateRepository;

    public GraphController(AggregateRepository aggregateRepository) {
        this.aggregateRepository = aggregateRepository;
    }

    @GetMapping("mutualFriends")
    List<User> getFriendOfFriend(@RequestParam("userId") Long userId,
                                 @RequestParam("friendId") Long friendId) {
        return aggregateRepository.friendOfFriend(userId, friendId);
    }
}
