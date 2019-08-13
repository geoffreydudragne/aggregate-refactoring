package kata.persistence;

import org.jdbi.v3.core.Jdbi;

public class MeetupEventDao {

    private final Jdbi jdbi;

    public MeetupEventDao(Jdbi jdbi) {
        this.jdbi = jdbi;
    }

    public long generateId() {
        return jdbi.withHandle(handle -> handle
                .createQuery("SELECT NEXTVAL('MEETUP_EVENT_ID_SEQ')")
                .mapTo(Long.class)
                .one()
        );
    }
}