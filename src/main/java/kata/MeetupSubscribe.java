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
        MeetupEvent meetupEvent1 = meetupEventRepository.findById(meetupEventId);
        if (meetupEvent1.getSubscription(userId) != null) {
            throw new RuntimeException(String.format("User %s already has a subscription", userId));
        }

        MeetupEvent meetupEvent2 = meetupEventRepository.findById(meetupEventId);
        List<Subscription> participants = meetupEvent2.getParticipants();
        MeetupEvent meetupEvent = meetupEventRepository.findById(meetupEventId);
        boolean addToWaitingList = participants.size() == meetupEvent.getCapacity();
        Subscription subscription = new Subscription(userId, Instant.now(), addToWaitingList);
        MeetupEvent meetupEvent3 = meetupEventRepository.findById(meetupEventId);
        meetupEvent3.addToSubscriptions(subscription);
        meetupEventRepository.save(meetupEvent3);
    }

    public void cancelUserSubscription(String userId, Long meetupEventId) {
        MeetupEvent meetupEvent3 = meetupEventRepository.findById(meetupEventId);
        Boolean inWaitingList = meetupEvent3.isUserSubscriptionInWaitingList(userId);
        MeetupEvent meetupEvent2 = meetupEventRepository.findById(meetupEventId);
        meetupEvent2.deleteSubscription(userId);
        meetupEventRepository.save(meetupEvent2);

        if (!inWaitingList) {
            MeetupEvent meetupEvent = meetupEventRepository.findById(meetupEventId);
            List<Subscription> waitingList = meetupEvent.getWaitingList();
            if (!waitingList.isEmpty()) {
                Subscription firstInWaitingList = waitingList.get(0);
                MeetupEvent meetupEvent1 = meetupEventRepository.findById(meetupEventId);
                meetupEvent1.changeFromWaitingListToParticipants(firstInWaitingList.getUserId());
                meetupEventRepository.save(meetupEvent1);
            }
        }
    }

    public void increaseCapacity(Long meetupEventId, int newCapacity) {
        MeetupEvent meetupEvent = meetupEventRepository.findById(meetupEventId);
        int oldCapacity = meetupEvent.getCapacity();

        if (oldCapacity < newCapacity) {
            MeetupEvent meetupEvent1 = meetupEventRepository.findById(meetupEventId);
            meetupEvent1.setCapacity(newCapacity);
            meetupEventRepository.save(meetupEvent1);
            int newSlots = newCapacity - oldCapacity;
            MeetupEvent meetupEvent2 = meetupEventRepository.findById(meetupEventId);
            List<Subscription> waitingList = meetupEvent2.getWaitingList();
            waitingList.stream()
                    .limit(newSlots)
                    .forEach(subscription -> {
                        MeetupEvent meetupEvent3 = meetupEventRepository.findById(meetupEventId);
                        meetupEvent3.changeFromWaitingListToParticipants(subscription.getUserId());
                        meetupEventRepository.save(meetupEvent3);
                    });
        }
    }

    public MeetupEventStatusDto getMeetupEventStatus(Long meetupEventId) {
        MeetupEvent meetupEvent = meetupEventRepository.findById(meetupEventId);
        MeetupEvent meetupEvent1 = meetupEventRepository.findById(meetupEventId);
        List<Subscription> participants = meetupEvent1.getParticipants();
        MeetupEvent meetupEvent2 = meetupEventRepository.findById(meetupEventId);
        List<Subscription> waitingList = meetupEvent2.getWaitingList();

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
