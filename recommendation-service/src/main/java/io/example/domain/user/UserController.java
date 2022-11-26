package io.example.domain.user;

import io.example.domain.friend.entity.RankedUser;
import io.example.domain.user.entity.User;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/v1")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping(path = "/users/{userId}/commands/findMutualFriends")
    public Flux<User> getMutualFriends(@PathVariable Long userId, @RequestParam Long friendId) {
        return Flux.fromIterable(userRepository.mutualFriends(userId, friendId));
    }

    @GetMapping(path = "/users/{userId}/commands/recommendFriends")
    public Flux<RankedUser> recommendFriends(@PathVariable Long userId) {
        return Flux.fromIterable(userRepository.recommendedFriends(userId, RankedUser.class));
    }
}
