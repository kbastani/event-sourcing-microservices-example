package io.example.friend;

/**
 * A projection of the {@link Friend} domain object that is owned by the
 * friend service.
 *
 * @author Kenny Bastani
 */
public class Friend {
    private Long id;

    private Long userA;
    private Long userB;

    public Friend() {
    }

    public Friend(Long userA, Long userB) {
        this.userA = userA;
        this.userB = userB;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserA() {
        return userA;
    }

    public void setUserA(Long userA) {
        this.userA = userA;
    }

    public Long getUserB() {
        return userB;
    }

    public void setUserB(Long userB) {
        this.userB = userB;
    }
}
