package kata;

import kata.persistence.MeetupEventDao;
import kata.persistence.MeetupEventRepository;

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
        meetupEvent.subscribeUser(userId);
        meetupEventRepository.save(meetupEvent);
    }

    public void cancelUserSubscription(String userId, Long meetupEventId) {
        MeetupEvent meetupEvent = meetupEventRepository.findById(meetupEventId);
        meetupEvent.canceUserSubscription(userId);
        meetupEventRepository.save(meetupEvent);
    }

    public void increaseCapacity(Long meetupEventId, int newCapacity) {
        MeetupEvent meetupEvent = meetupEventRepository.findById(meetupEventId);
        meetupEvent.increaseCapacity(newCapacity);
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
