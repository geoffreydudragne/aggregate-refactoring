package kata.persistence;

import kata.MeetupEvent;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.RowMapper;

import java.time.LocalDateTime;

import static kata.persistence.JdbiMapperHelper.mapTo;

public class MeetupEventDao {

    private final Jdbi jdbi;
    private final MeetupEventRepository meetupEventRepository;

    public MeetupEventDao(Jdbi jdbi) {
        this.jdbi = jdbi;
        meetupEventRepository = new MeetupEventRepository(jdbi);
    }

    public void create(MeetupEvent meetupEvent) {
        meetupEventRepository.save(meetupEvent);
    }

    public void updateCapacity(Long meetupEventId, int newCapacity) {
        MeetupEvent meetupEvent = meetupEventRepository.findById(meetupEventId);
        meetupEvent.setCapacity(newCapacity);
        meetupEventRepository.save(meetupEvent);
    }

    public MeetupEvent findById(Long meetupEventId) {
        return meetupEventRepository.findById(meetupEventId);
    }

    public long generateId() {
        return jdbi.withHandle(handle -> handle
                .createQuery("SELECT NEXTVAL('MEETUP_EVENT_ID_SEQ')")
                .mapTo(Long.class)
                .one()
        );
    }
}