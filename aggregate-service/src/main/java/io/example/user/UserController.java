package io.example.user;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/graph")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("mutualFriends")
    List<User> getFriendOfFriend(@RequestParam("userId") Long userId, @RequestParam("friendId") Long friendId) {
        return userRepository.friendOfFriend(userId, friendId);
    }
}
