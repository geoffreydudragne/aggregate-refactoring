package kata;

import java.time.Instant;

public class Subscription {

    private String userId;

    private Instant registrationTime;

    private boolean isInWaitingList;

    public Subscription(String userId, Instant registrationTime, boolean isInWaitingList) {
        this.userId = userId;
        this.registrationTime = registrationTime;
        this.isInWaitingList = isInWaitingList;
    }

    public String getUserId() {
        return userId;
    }

    public Instant getRegistrationTime() {
        return registrationTime;
    }

    public boolean isInWaitingList() {
        return isInWaitingList;
    }
}
