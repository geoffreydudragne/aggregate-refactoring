package kata.persistence;

import kata.MeetupEvent;
import kata.Subscription;
import org.jdbi.v3.core.Jdbi;

import java.util.List;

public class MeetupSubscriptionDao {

    private final Jdbi jdbi;

    private final MeetupEventRepository meetupEventRepository;

    public MeetupSubscriptionDao(Jdbi jdbi) {
        this.jdbi = jdbi;
        meetupEventRepository = new MeetupEventRepository(jdbi);
    }

    public void addToSubscriptions(Subscription subscribtion, Long meetupEventId) {
        MeetupEvent meetupEvent = meetupEventRepository.findById(meetupEventId);
        meetupEvent.addToSubscriptions(subscribtion);
        meetupEventRepository.save(meetupEvent);
    }

    public void deleteSubscription(String userId, Long meetupEventId) {
        MeetupEvent meetupEvent = meetupEventRepository.findById(meetupEventId);
        meetupEvent.deleteSubscription(userId);
        meetupEventRepository.save(meetupEvent);
    }

    public void changeFromWaitingListToParticipants(String userId, Long meetupEventId) {
        MeetupEvent meetupEvent = meetupEventRepository.findById(meetupEventId);
        meetupEvent.changeFromWaitingListToParticipants(userId);
        meetupEventRepository.save(meetupEvent);
    }

    public List<Subscription> findSubscriptionsParticipants(Long meetupEventId) {
        MeetupEvent meetupEvent = meetupEventRepository.findById(meetupEventId);
        return meetupEvent.getParticipants();
    }

    public List<Subscription> findSubscriptionsInWaitingList(Long meetupEventId) {
        MeetupEvent meetupEvent = meetupEventRepository.findById(meetupEventId);
        return meetupEvent.getWaitingList();
    }

    public Boolean isUserSubscriptionInWaitingList(String userId, Long meetupEventId) {
        MeetupEvent meetupEvent = meetupEventRepository.findById(meetupEventId);
        return meetupEvent.isUserSubscriptionInWaitingList(userId);
    }

    public Subscription findById(String userId, Long meetupEventId) {
        MeetupEvent meetupEvent = meetupEventRepository.findById(meetupEventId);
        return meetupEvent.getSubscription(userId);
    }
}
