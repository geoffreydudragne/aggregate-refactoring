package kata;

import java.time.LocalDateTime;
import java.util.List;

public class MeetupEventStatusDto {

    public Long meetupId;
    public String eventName;
    public Integer eventCapacity;
    public LocalDateTime startTime;
    public List<String> participants;
    public List<String> waitingList;

}
