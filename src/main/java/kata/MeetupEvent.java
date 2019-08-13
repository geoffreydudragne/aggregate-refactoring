package kata;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class MeetupEvent {

    private final Long id;
    private int capacity;
    private final String eventName;
    private final LocalDateTime startTime;
    private List<Subscription> subscriptions;

    public MeetupEvent(Long id, int capacity, String eventName, LocalDateTime startTime) {
        this(id, capacity, eventName, startTime, new ArrayList<>());
    }

    public MeetupEvent(Long id, int capacity, String eventName, LocalDateTime startTime, List<Subscription> subscriptions) {
        this.id = id;
        this.capacity = capacity;
        this.eventName = eventName;
        this.startTime = startTime;
        this.subscriptions = subscriptions;
    }

    public Long getId() {
        return id;
    }

    public int getCapacity() {
        return capacity;
    }

    public String getEventName() {
        return eventName;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public List<Subscription> getSubscriptions() {
        return subscriptions;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public List<Subscription> getParticipants() {
        return getSubscriptions().stream()
                .filter(subscription -> !subscription.isInWaitingList())
                .collect(Collectors.toList());
    }

    public void changeFromWaitingListToParticipants(String userId) {
        Subscription subscription1 = getSubscriptions().stream()
                .filter(subscription -> subscription.getUserId().equals(userId))
                .findAny()
                .orElseThrow(() -> new RuntimeException("No user"));
        getSubscriptions().remove(subscription1);
        getSubscriptions().add(subscription1.toParticipant());
    }

    public List<Subscription> getWaitingList() {
        return getSubscriptions().stream()
                .filter(Subscription::isInWaitingList)
                .collect(Collectors.toList());
    }

    public void addToSubscriptions(Subscription subscribtion) {
        getSubscriptions().add(subscribtion);
    }

    public void deleteSubscription(String userId) {
        getSubscriptions().removeIf(subscription -> subscription.getUserId().equals(userId));
    }

    public boolean isUserSubscriptionInWaitingList(String userId) {
        Optional<Subscription> subscription = getSubscriptions().stream()
                .filter(sub -> sub.getUserId().equals(userId))
                .findAny();
        return subscription.filter(Subscription::isInWaitingList).isPresent();
    }

    public Subscription getSubscription(String userId) {
        Optional<Subscription> subscription = getSubscriptions().stream()
                .filter(sub -> sub.getUserId().equals(userId))
                .findAny();
        return subscription.orElse(null);
    }

    public void subscribeUser(String userId) {
        if (getSubscription(userId) != null) {
            throw new RuntimeException(String.format("User %s already has a subscription", userId));
        }

        List<Subscription> participants = getParticipants();
        boolean addToWaitingList = participants.size() == getCapacity();
        Subscription subscription = new Subscription(userId, Instant.now(), addToWaitingList);
        addToSubscriptions(subscription);
    }

    public void canceUserSubscription(String userId) {
        Boolean inWaitingList = isUserSubscriptionInWaitingList(userId);
        deleteSubscription(userId);

        if (!inWaitingList) {
            List<Subscription> waitingList = getWaitingList();
            if (!waitingList.isEmpty()) {
                Subscription firstInWaitingList = waitingList.get(0);
                changeFromWaitingListToParticipants(firstInWaitingList.getUserId());
            }
        }
    }

    public void increaseCapacity(int newCapacity) {
        int oldCapacity = getCapacity();

        if (oldCapacity < newCapacity) {
            setCapacity(newCapacity);
            int newSlots = newCapacity - oldCapacity;
            List<Subscription> waitingList = getWaitingList();
            waitingList.stream()
                    .limit(newSlots)
                    .forEach(subscription -> changeFromWaitingListToParticipants(subscription.getUserId()));
        }
    }
}
