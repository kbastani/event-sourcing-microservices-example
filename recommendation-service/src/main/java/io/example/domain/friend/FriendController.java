package io.example.domain.friend;

import io.example.domain.user.entity.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/recommendation")
public class FriendController {

    private final FriendRepository friendRepository;

    public FriendController(FriendRepository friendRepository) {
        this.friendRepository = friendRepository;
    }

    @GetMapping("friends")
    List<User> getFriendOfFriend(@RequestParam("userId") Long userId,
                                 @RequestParam("friendId") Long friendId) {
        return friendRepository.friendOfFriend(userId, friendId);
    }
}
