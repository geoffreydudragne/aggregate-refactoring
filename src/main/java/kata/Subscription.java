package kata;

import java.time.Instant;
import java.util.Objects;

public class Subscription {

    private final String userId;

    private final Instant registrationTime;

    private final boolean isInWaitingList;

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

    Subscription toParticipant() {
        return new Subscription(userId, registrationTime, false);
    }

    boolean isParticipant() {
        return !isInWaitingList();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Subscription that = (Subscription) o;
        return isInWaitingList == that.isInWaitingList &&
                Objects.equals(userId, that.userId) &&
                Objects.equals(registrationTime, that.registrationTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, registrationTime, isInWaitingList);
    }

    @Override
    public String toString() {
        return "Subscription{" +
                "userId='" + userId + '\'' +
                ", registrationTime=" + registrationTime +
                ", isInWaitingList=" + isInWaitingList +
                '}';
    }
}
