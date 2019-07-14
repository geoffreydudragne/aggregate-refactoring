package kata.persistence;

import kata.Subscription;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.RowMapper;

import java.time.Instant;
import java.util.List;

import static kata.persistence.JdbiMapperHelper.mapTo;

public class MeetupSubscriptionDao {

    private final Jdbi jdbi;

    public MeetupSubscriptionDao(Jdbi jdbi) {
        this.jdbi = jdbi;
    }

    private static final RowMapper<Subscription> SUBSCRIPTION_ROW_MAPPER = (rs, ctx) ->
            new Subscription(
                    rs.getString("user_id"),
                    mapTo(rs, "registration_time", Instant.class, ctx),
                    rs.getBoolean("waiting_list")
            );

    public void addToSubscriptions(Subscription subscribtion, Long meetupEventId) {
        String sql = "" +
                "INSERT INTO USER_SUBSCRIPTION (user_id, meetup_event_id, registration_time, waiting_list) " +
                "VALUES (:userId, :meetupEventId, :registrationTime, :waitingList)";

        jdbi.useHandle(handle -> handle.createUpdate(sql)
                .bind("userId", subscribtion.getUserId())
                .bind("meetupEventId", meetupEventId)
                .bind("registrationTime", subscribtion.getRegistrationTime())
                .bind("waitingList", subscribtion.isInWaitingList())
                .execute());
    }

    public void deleteSubscription(String userId, Long meetupEventId) {
        String sql = "" +
                "DELETE FROM USER_SUBSCRIPTION " +
                "WHERE meetup_event_id = :meetupEventId " +
                "AND user_id = :userId";

        jdbi.useHandle(handle -> handle.createUpdate(sql)
                .bind("meetupEventId", meetupEventId)
                .bind("userId", userId)
                .execute());
    }

    public void changeFromWaitingListToParticipants(String userId, Long meetupEventId) {
        String sql = "" +
                "UPDATE USER_SUBSCRIPTION " +
                "SET waiting_list = FALSE " +
                "WHERE meetup_event_id = :meetupEventId " +
                "AND user_id = :userId";

        jdbi.useHandle(handle -> handle.createUpdate(sql)
                .bind("meetupEventId", meetupEventId)
                .bind("userId", userId)
                .execute());
    }

    public List<Subscription> findSubscriptionsParticipants(Long meetupEventId) {
        String sql = "" +
                "SELECT * FROM USER_SUBSCRIPTION " +
                "WHERE meetup_event_id = :meetupEventId " +
                "AND waiting_list IS FALSE " +
                "ORDER BY registration_time ASC";

        return jdbi.withHandle(handle -> handle.createQuery(sql)
                .bind("meetupEventId", meetupEventId)
                .map(SUBSCRIPTION_ROW_MAPPER)
                .list());
    }

    public List<Subscription> findSubscriptionsInWaitingList(Long meetupEventId) {
        String sql = "" +
                "SELECT * FROM USER_SUBSCRIPTION " +
                "WHERE meetup_event_id = :meetupEventId " +
                "AND waiting_list IS TRUE " +
                "ORDER BY registration_time ASC";

        return jdbi.withHandle(handle -> handle.createQuery(sql)
                .bind("meetupEventId", meetupEventId)
                .map(SUBSCRIPTION_ROW_MAPPER)
                .list());
    }

    public Boolean isUserSubscriptionInWaitingList(String userId, Long meetupEventId) {
        String sql = "" +
                "SELECT waiting_list FROM USER_SUBSCRIPTION " +
                "WHERE meetup_event_id = :meetupEventId " +
                "AND user_id = :userId";

        return jdbi.withHandle(handle -> handle.createQuery(sql)
                .bind("meetupEventId", meetupEventId)
                .bind("userId", userId)
                .mapTo(Boolean.class)
                .one());
    }

    public Subscription findById(String userId, Long meetupEventId) {
        String sql = "" +
                "SELECT * FROM USER_SUBSCRIPTION " +
                "WHERE meetup_event_id = :meetupEventId " +
                "AND user_id = :userId";

        return jdbi.withHandle(handle -> handle.createQuery(sql)
                .bind("meetupEventId", meetupEventId)
                .bind("userId", userId)
                .map(SUBSCRIPTION_ROW_MAPPER)
                .findOne().orElse(null));
    }
}
