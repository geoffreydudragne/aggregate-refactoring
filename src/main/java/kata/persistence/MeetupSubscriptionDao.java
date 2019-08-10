package kata.persistence;

import kata.MeetupEvent;
import kata.Subscription;
import org.jdbi.v3.core.Jdbi;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class MeetupSubscriptionDao {

    private final Jdbi jdbi;

    private final MeetupEventRepository meetupEventRepository;

    public MeetupSubscriptionDao(Jdbi jdbi) {
        this.jdbi = jdbi;
        meetupEventRepository = new MeetupEventRepository(jdbi);
    }

    public void addToSubscriptions(Subscription subscribtion, Long meetupEventId) {
        MeetupEvent meetupEvent = meetupEventRepository.findById(meetupEventId);
        meetupEvent.getSubscriptions().add(subscribtion);
        meetupEventRepository.save(meetupEvent);
    }

    public void deleteSubscription(String userId, Long meetupEventId) {
        MeetupEvent meetupEvent = meetupEventRepository.findById(meetupEventId);
        meetupEvent.getSubscriptions().removeIf(subscription -> subscription.getUserId().equals(userId));
        meetupEventRepository.save(meetupEvent);
    }

    public void changeFromWaitingListToParticipants(String userId, Long meetupEventId) {
        MeetupEvent meetupEvent = meetupEventRepository.findById(meetupEventId);
        Subscription subscription1 = meetupEvent.getSubscriptions().stream()
                .filter(subscription -> subscription.getUserId().equals(userId))
                .findAny()
                .orElseThrow(() -> new RuntimeException("No user"));
        meetupEvent.getSubscriptions().remove(subscription1);
        meetupEvent.getSubscriptions().add(subscription1.toParticipant());
        meetupEventRepository.save(meetupEvent);
    }

    public List<Subscription> findSubscriptionsParticipants(Long meetupEventId) {
        MeetupEvent meetupEvent = meetupEventRepository.findById(meetupEventId);
        return meetupEvent.getSubscriptions().stream()
                .filter(subscription -> !subscription.isInWaitingList())
                .collect(Collectors.toList());
    }

    public List<Subscription> findSubscriptionsInWaitingList(Long meetupEventId) {
        MeetupEvent meetupEvent = meetupEventRepository.findById(meetupEventId);
        return meetupEvent.getSubscriptions().stream()
                .filter(Subscription::isInWaitingList)
                .collect(Collectors.toList());
    }

    public Boolean isUserSubscriptionInWaitingList(String userId, Long meetupEventId) {
        MeetupEvent meetupEvent = meetupEventRepository.findById(meetupEventId);
        Optional<Subscription> subscription = meetupEvent.getSubscriptions().stream()
                .filter(sub -> sub.getUserId().equals(userId))
                .findAny();
        return subscription.filter(Subscription::isInWaitingList).isPresent();
    }

    public Subscription findById(String userId, Long meetupEventId) {
        MeetupEvent meetupEvent = meetupEventRepository.findById(meetupEventId);
        Optional<Subscription> subscription = meetupEvent.getSubscriptions().stream()
                .filter(sub -> sub.getUserId().equals(userId))
                .findAny();
        return subscription.orElse(null);
    }
}
