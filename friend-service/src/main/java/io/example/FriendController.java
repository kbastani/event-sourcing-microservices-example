package io.example;

import org.springframework.cloud.stream.messaging.Source;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.transaction.Transactional;

/**
 *
 */
@RestController
@RequestMapping("/v1")
@Transactional
public class FriendController {

    private final Source source;
    private final FriendRepository friendRepository;

    public FriendController(Source source, FriendRepository friendRepository) {
        this.source = source;
        this.friendRepository = friendRepository;
    }

    @RequestMapping(path = "/friends/add", method = RequestMethod.POST)
    public Friend addFriend(@RequestBody Friend friend) {
        friendRepository.save(friend);
        source.output().send(MessageBuilder
                .withPayload(new FriendEvent(friend, EventType.FRIEND_ADDED)).build());
        return friend;
    }

    @RequestMapping(path = "/friends/remove", method = RequestMethod.POST)
    public Friend removeFriend(@RequestBody Friend friend) {
        friendRepository.save(friend);
        source.output().send(MessageBuilder
                .withPayload(new FriendEvent(friend, EventType.FRIEND_REMOVED)).build());
        return friend;
    }
}
