package kata;

import java.time.LocalDateTime;

public class MeetupEvent {

    private final Long id;
    private final int capacity;
    private final String eventName;
    private final LocalDateTime startTime;

    public MeetupEvent(Long id, int capacity, String eventName, LocalDateTime startTime) {
        this.id = id;
        this.capacity = capacity;
        this.eventName = eventName;
        this.startTime = startTime;
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
}
