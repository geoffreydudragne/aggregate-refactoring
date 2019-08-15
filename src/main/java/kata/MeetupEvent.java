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

    public List<Subscription> getParticipants() {
        return subscriptions.stream()
                .filter(Subscription::isParticipant)
                .collect(Collectors.toList());
    }

    private void changeFromWaitingListToParticipants(Subscription subscription) {
        subscriptions.remove(subscription);
        subscriptions.add(subscription.toParticipant());
    }

    public List<Subscription> getWaitingList() {
        return subscriptions.stream()
                .filter(Subscription::isInWaitingList)
                .collect(Collectors.toList());
    }

    public Subscription getSubscription(String userId) {
        Optional<Subscription> subscription = subscriptions.stream()
                .filter(sub -> sub.getUserId().equals(userId))
                .findAny();
        return subscription.orElse(null);
    }

    public void subscribeUser(String userId) {
        if (getSubscription(userId) != null) {
            throw new RuntimeException(String.format("User %s already has a subscription", userId));
        }

        List<Subscription> participants = getParticipants();
        boolean addToWaitingList = participants.size() == capacity;
        Subscription subscription = new Subscription(userId, Instant.now(), addToWaitingList);
        subscriptions.add(subscription);
    }

    public void canceUserSubscription(String userId) {
        Subscription subscription = getSubscription(userId);
        subscriptions.remove(subscription);

        if (subscription.isParticipant()) {
            List<Subscription> waitingList = getWaitingList();
            if (!waitingList.isEmpty()) {
                Subscription firstInWaitingList = waitingList.get(0);
                changeFromWaitingListToParticipants(firstInWaitingList);
            }
        }
    }

    public void increaseCapacity(int newCapacity) {
        if (capacity < newCapacity) {
            int newSlots = newCapacity - capacity;
            capacity = newCapacity;
            getWaitingList().stream()
                    .limit(newSlots)
                    .forEach(this::changeFromWaitingListToParticipants);
        }
    }
}
