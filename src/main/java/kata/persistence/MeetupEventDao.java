package kata.persistence;

import kata.MeetupEvent;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.RowMapper;

import java.time.LocalDateTime;

import static kata.persistence.JdbiMapperHelper.mapTo;

public class MeetupEventDao {

    private final Jdbi jdbi;

    public MeetupEventDao(Jdbi jdbi) {
        this.jdbi = jdbi;
    }

    private static final RowMapper<MeetupEvent> MEETUP_EVENT_ROW_MAPPER = (rs, ctx) ->
            new MeetupEvent(
                    rs.getLong("id"),
                    rs.getInt("capacity"),
                    rs.getString("event_name"),
                    mapTo(rs, "start_time", LocalDateTime.class, ctx)
            );

    public void create(MeetupEvent meetupEvent) {
        String sql = "" +
                "INSERT INTO MEETUP_EVENT (id, event_name, start_time, capacity) " +
                "VALUES (:id, :event_name, :start_time, :capacity)";

        jdbi.withHandle(handle -> handle.createUpdate(sql)
                .bind("id", meetupEvent.getId())
                .bind("event_name", meetupEvent.getEventName())
                .bind("start_time", meetupEvent.getStartTime())
                .bind("capacity", meetupEvent.getCapacity())
                .execute());
    }

    public void updateCapacity(Long meetupEventId, int newCapacity) {
        String sql = "" +
                "UPDATE MEETUP_EVENT " +
                "SET capacity = :newCapacity " +
                "WHERE id = :id";

        jdbi.useHandle(handle -> handle.createUpdate(sql)
                .bind("newCapacity", newCapacity)
                .bind("id", meetupEventId)
                .execute());
    }

    public MeetupEvent findById(Long meetupEventId) {
        String sql = "SELECT * FROM MEETUP_EVENT WHERE id = :id";
        return jdbi.withHandle(handle -> handle.createQuery(sql)
                .bind("id", meetupEventId)
                .map(MEETUP_EVENT_ROW_MAPPER)
                .findOne()
                .orElse(null));
    }

    public long generateId() {
        return jdbi.withHandle(handle -> handle
                .createQuery("SELECT NEXTVAL('MEETUP_EVENT_ID_SEQ')")
                .mapTo(Long.class)
                .one()
        );
    }
}