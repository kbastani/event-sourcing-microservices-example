package io.example.domain.friend;

import io.example.domain.friend.entity.RankedUser;
import io.example.domain.user.entity.User;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/recommendation")
public class FriendController {

    private final FriendRepository friendRepository;

    public FriendController(FriendRepository friendRepository) {
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
