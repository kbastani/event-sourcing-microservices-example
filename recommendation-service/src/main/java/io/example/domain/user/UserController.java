package io.example.domain.user;

import io.example.domain.friend.FriendRepository;
import io.example.domain.friend.entity.RankedUser;
import io.example.domain.user.entity.User;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1")
public class UserController {

    private final FriendRepository friendRepository;

    public UserController(FriendRepository friendRepository) {
        this.friendRepository = friendRepository;
    }

    @GetMapping(path = "/users/{userId}/commands/findMutualFriends")
    List<User> getMutualFriends(@PathVariable Long userId, @RequestParam Long friendId) {
        return friendRepository.mutualFriends(userId, friendId);
    }

    @GetMapping(path = "/users/{userId}/commands/recommendFriends")
    List<RankedUser> recommendFriends(@PathVariable Long userId) {
        return friendRepository.recommendedFriends(userId);
    }
}
