package io.example.domain.friend;

import io.example.domain.friend.entity.RankedUser;
import io.example.domain.user.entity.User;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1")
public class FriendController {

    private final FriendRepository friendRepository;

    public FriendController(FriendRepository friendRepository) {
        this.friendRepository = friendRepository;
    }

    @GetMapping(path = "/friends/{userId}/commands/findMutualFriends")
    List<User> getMutualFriends(@PathVariable Long userId, @RequestParam Long friendId) {
        return friendRepository.mutualFriends(userId, friendId);
    }

    @GetMapping(path = "/friends/{userId}/commands/recommendFriends")
    List<RankedUser> recommendFriends(@PathVariable Long userId) {
        return friendRepository.recommendedFriends(userId);
    }
}
