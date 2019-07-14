package kata;

import kata.persistence.MeetupEventDao;
import kata.persistence.MeetupSubscriptionDao;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class MeetupSubscribe {

    private final MeetupSubscriptionDao meetupSubscriptionDao;
    private final MeetupEventDao meetupEventDao;

    public MeetupSubscribe(MeetupSubscriptionDao meetupSubscriptionDao, MeetupEventDao meetupEventDao) {
        this.meetupSubscriptionDao = meetupSubscriptionDao;
        this.meetupEventDao = meetupEventDao;
    }

    public Long registerMeetupEvent(String eventName, Integer eventCapacity, LocalDateTime startTime) {
        long id = meetupEventDao.generateId();
        MeetupEvent meetupEvent = new MeetupEvent(id, eventCapacity, eventName, startTime);
        meetupEventDao.create(meetupEvent);
        return id;
    }

    public void subscribeUserToMeetupEvent(String userId, Long meetupEventId) {
        if (meetupSubscriptionDao.findById(userId, meetupEventId) != null) {
            throw new RuntimeException(String.format("User %s already has a subscription", userId));
        }

        List<Subscription> participants = meetupSubscriptionDao.findSubscriptionsParticipants(meetupEventId);
        MeetupEvent meetupEvent = meetupEventDao.findById(meetupEventId);
        boolean addToWaitingList = participants.size() == meetupEvent.getCapacity();
        Subscription subscription = new Subscription(userId, Instant.now(), addToWaitingList);
        meetupSubscriptionDao.addToSubscriptions(subscription, meetupEventId);
    }

    public void cancelUserSubscription(String userId, Long meetupEventId) {
        Boolean inWaitingList = meetupSubscriptionDao.isUserSubscriptionInWaitingList(userId, meetupEventId);
        meetupSubscriptionDao.deleteSubscription(userId, meetupEventId);

        if (!inWaitingList) {
            List<Subscription> waitingList = meetupSubscriptionDao.findSubscriptionsInWaitingList(meetupEventId);
            if (!waitingList.isEmpty()) {
                Subscription firstInWaitingList = waitingList.get(0);
                meetupSubscriptionDao.changeFromWaitingListToParticipants(firstInWaitingList.getUserId(), meetupEventId);
            }
        }
    }

    public void increaseCapacity(Long meetupEventId, int newCapacity) {
        MeetupEvent meetupEvent = meetupEventDao.findById(meetupEventId);
        int oldCapacity = meetupEvent.getCapacity();

        if (oldCapacity < newCapacity) {
            meetupEventDao.updateCapacity(meetupEventId, newCapacity);
            int newSlots = newCapacity - oldCapacity;
            List<Subscription> waitingList = meetupSubscriptionDao.findSubscriptionsInWaitingList(meetupEventId);
            waitingList.stream()
                    .limit(newSlots)
                    .forEach(subscription -> meetupSubscriptionDao.changeFromWaitingListToParticipants(subscription.getUserId(), meetupEventId));
        }
    }

    public MeetupEventStatusDto getMeetupEventStatus(Long meetupEventId) {
        MeetupEvent meetupEvent = meetupEventDao.findById(meetupEventId);
        List<Subscription> participants = meetupSubscriptionDao.findSubscriptionsParticipants(meetupEventId);
        List<Subscription> waitingList = meetupSubscriptionDao.findSubscriptionsInWaitingList(meetupEventId);

        MeetupEventStatusDto meetupEventStatusDto = new MeetupEventStatusDto();
        meetupEventStatusDto.meetupId = meetupEvent.getId();
        meetupEventStatusDto.eventCapacity = meetupEvent.getCapacity();
        meetupEventStatusDto.startTime = meetupEvent.getStartTime();
        meetupEventStatusDto.eventName = meetupEvent.getEventName();
        meetupEventStatusDto.waitingList = waitingList.stream().map(Subscription::getUserId).collect(toList());
        meetupEventStatusDto.participants = participants.stream().map(Subscription::getUserId).collect(toList());
        return meetupEventStatusDto;
    }
}
