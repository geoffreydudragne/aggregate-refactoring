package kata;

import kata.persistence.MeetupEventDao;
import kata.persistence.MeetupEventRepository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class MeetupSubscribe {

    private final MeetupEventDao meetupEventDao;
    private final MeetupEventRepository meetupEventRepository;

    public MeetupSubscribe(MeetupEventDao meetupEventDao, MeetupEventRepository meetupEventRepository) {
        this.meetupEventDao = meetupEventDao;
        this.meetupEventRepository = meetupEventRepository;
    }

    public Long registerMeetupEvent(String eventName, Integer eventCapacity, LocalDateTime startTime) {
        long id = meetupEventDao.generateId();
        MeetupEvent meetupEvent = new MeetupEvent(id, eventCapacity, eventName, startTime);
        meetupEventRepository.save(meetupEvent);
        return id;
    }

    public void subscribeUserToMeetupEvent(String userId, Long meetupEventId) {
        MeetupEvent meetupEvent = meetupEventRepository.findById(meetupEventId);
        if (meetupEvent.getSubscription(userId) != null) {
            throw new RuntimeException(String.format("User %s already has a subscription", userId));
        }

        List<Subscription> participants = meetupEvent.getParticipants();
        boolean addToWaitingList = participants.size() == meetupEvent.getCapacity();
        Subscription subscription = new Subscription(userId, Instant.now(), addToWaitingList);
        meetupEvent.addToSubscriptions(subscription);
        meetupEventRepository.save(meetupEvent);
    }

    public void cancelUserSubscription(String userId, Long meetupEventId) {
        MeetupEvent meetupEvent = meetupEventRepository.findById(meetupEventId);
        Boolean inWaitingList = meetupEvent.isUserSubscriptionInWaitingList(userId);
        meetupEvent.deleteSubscription(userId);

        if (!inWaitingList) {
            List<Subscription> waitingList = meetupEvent.getWaitingList();
            if (!waitingList.isEmpty()) {
                Subscription firstInWaitingList = waitingList.get(0);
                meetupEvent.changeFromWaitingListToParticipants(firstInWaitingList.getUserId());
            }
        }
        meetupEventRepository.save(meetupEvent);
    }

    public void increaseCapacity(Long meetupEventId, int newCapacity) {
        MeetupEvent meetupEvent = meetupEventRepository.findById(meetupEventId);
        int oldCapacity = meetupEvent.getCapacity();

        if (oldCapacity < newCapacity) {
            meetupEvent.setCapacity(newCapacity);
            int newSlots = newCapacity - oldCapacity;
            List<Subscription> waitingList = meetupEvent.getWaitingList();
            waitingList.stream()
                    .limit(newSlots)
                    .forEach(subscription -> meetupEvent.changeFromWaitingListToParticipants(subscription.getUserId()));
        }
        meetupEventRepository.save(meetupEvent);
    }

    public MeetupEventStatusDto getMeetupEventStatus(Long meetupEventId) {
        MeetupEvent meetupEvent = meetupEventRepository.findById(meetupEventId);
        List<Subscription> participants = meetupEvent.getParticipants();
        List<Subscription> waitingList = meetupEvent.getWaitingList();

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
