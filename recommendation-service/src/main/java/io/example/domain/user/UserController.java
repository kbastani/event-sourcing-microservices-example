package io.example.domain.user;

import io.example.domain.friend.FriendRepository;
import io.example.domain.friend.entity.RankedUser;
import io.example.domain.user.entity.User;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/v1")
public class UserController {

    private final FriendRepository friendRepository;

    public UserController(FriendRepository friendRepository) {
        this.friendRepository = friendRepository;
    }

    @GetMapping(path = "/users/{userId}/commands/findMutualFriends")
    public Flux<User> getMutualFriends(@PathVariable Long userId, @RequestParam Long friendId) {
        return Flux.just(friendRepository.mutualFriends(userId, friendId).toArray(User[]::new));
    }

    @GetMapping(path = "/users/{userId}/commands/recommendFriends")
    public Flux<RankedUser> recommendFriends(@PathVariable Long userId) {
        return Flux.just(friendRepository.recommendedFriends(userId).toArray(RankedUser[]::new));
    }
}
