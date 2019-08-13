package kata;

import kata.dbtestutil.MemoryDbTestContext;
import kata.persistence.MeetupEventDao;
import kata.persistence.MeetupEventRepository;
import org.assertj.core.api.Assertions;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

public class MeetupSubscribeTest {

    private MeetupSubscribe meetupSubscribe;
    private MemoryDbTestContext memoryDbTestContext;

    @BeforeEach
    void setUp() throws Exception {
        memoryDbTestContext = MemoryDbTestContext.openWithSql("/setup.sql");
        Jdbi jdbi = memoryDbTestContext.getJdbi();
        MeetupEventDao meetupEventDao = new MeetupEventDao(jdbi);
        MeetupEventRepository meetupEventRepository = new MeetupEventRepository(jdbi);
        meetupSubscribe = new MeetupSubscribe(meetupEventDao, meetupEventRepository);
    }

    @AfterEach
    void tearDown() {
        memoryDbTestContext.close();
    }

    @Test
    void should_be_able_to_give_state_of_meetup_event() {
        LocalDateTime startTime = LocalDateTime.of(2019, 6, 15, 20, 0);

        Long meetupEventId = meetupSubscribe.registerMeetupEvent("Coding dojo session 1", 50, startTime);

        MeetupEventStatusDto meetupEventStatus = meetupSubscribe.getMeetupEventStatus(meetupEventId);
        assertThat(meetupEventStatus.meetupId).isEqualTo(meetupEventId);
        assertThat(meetupEventStatus.eventCapacity).isEqualTo(50);
        assertThat(meetupEventStatus.eventName).isEqualTo("Coding dojo session 1");
        assertThat(meetupEventStatus.startTime).isEqualTo(startTime);
        assertThat(meetupEventStatus.participants).isEmpty();
        assertThat(meetupEventStatus.waitingList).isEmpty();
    }

    private Long registerAMeetupWithCapacity(int eventCapacity) {
        LocalDateTime startTime = LocalDateTime.of(2019, 6, 15, 20, 0);
        return meetupSubscribe.registerMeetupEvent("Coding dojo session 1", eventCapacity, startTime);
    }

    @Test
    void should_add_subscription_to_participants() {
        Long meetupEventId = registerAMeetupWithCapacity(50);

        meetupSubscribe.subscribeUserToMeetupEvent("Alice", meetupEventId);
        meetupSubscribe.subscribeUserToMeetupEvent("Bob", meetupEventId);
        meetupSubscribe.subscribeUserToMeetupEvent("Charles", meetupEventId);

        MeetupEventStatusDto meetupEventStatus = meetupSubscribe.getMeetupEventStatus(meetupEventId);
        assertThat(meetupEventStatus.participants).containsExactly("Alice", "Bob", "Charles");
        assertThat(meetupEventStatus.waitingList).isEmpty();
    }

    @Test
    void should_reject_subscription_with_aslready_sbscribed_user() {
        Long meetupEventId = registerAMeetupWithCapacity(50);
        meetupSubscribe.subscribeUserToMeetupEvent("Alice", meetupEventId);

        Assertions.assertThatThrownBy(() -> meetupSubscribe.subscribeUserToMeetupEvent("Alice", meetupEventId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("User Alice already has a subscription");
    }

    @Test
    void should_add_subscription_to_waiting_list_when_event_is_at_capacity() {
        Long meetupEventId = registerAMeetupWithCapacity(2);

        meetupSubscribe.subscribeUserToMeetupEvent("Alice", meetupEventId);
        meetupSubscribe.subscribeUserToMeetupEvent("Bob", meetupEventId);
        meetupSubscribe.subscribeUserToMeetupEvent("Charles", meetupEventId);
        meetupSubscribe.subscribeUserToMeetupEvent("David", meetupEventId);

        MeetupEventStatusDto meetupEventStatus = meetupSubscribe.getMeetupEventStatus(meetupEventId);
        assertThat(meetupEventStatus.participants).containsExactly("Alice", "Bob");
        assertThat(meetupEventStatus.waitingList).containsExactly("Charles", "David");
    }

    @Test
    void should_put_first_user_of_waiting_list_into_participants_when_a_participant_cancels() {
        Long meetupEventId = registerAMeetupWithCapacity(2);
        meetupSubscribe.subscribeUserToMeetupEvent("Alice", meetupEventId);
        meetupSubscribe.subscribeUserToMeetupEvent("Bob", meetupEventId);
        meetupSubscribe.subscribeUserToMeetupEvent("Charles", meetupEventId);
        meetupSubscribe.subscribeUserToMeetupEvent("David", meetupEventId);

        meetupSubscribe.cancelUserSubscription("Alice", meetupEventId);

        MeetupEventStatusDto meetupEventStatus = meetupSubscribe.getMeetupEventStatus(meetupEventId);
        assertThat(meetupEventStatus.participants).containsExactly("Bob", "Charles");
        assertThat(meetupEventStatus.waitingList).containsExactly("David");
    }

    @Test
    void should_not_change_participants_list_when_a_user_in_waiting_list_cancels() {
        Long meetupEventId = registerAMeetupWithCapacity(2);
        meetupSubscribe.subscribeUserToMeetupEvent("Alice", meetupEventId);
        meetupSubscribe.subscribeUserToMeetupEvent("Bob", meetupEventId);
        meetupSubscribe.subscribeUserToMeetupEvent("Charles", meetupEventId);
        meetupSubscribe.subscribeUserToMeetupEvent("David", meetupEventId);

        meetupSubscribe.cancelUserSubscription("Charles", meetupEventId);

        MeetupEventStatusDto meetupEventStatus = meetupSubscribe.getMeetupEventStatus(meetupEventId);
        assertThat(meetupEventStatus.participants).containsExactly("Alice", "Bob");
        assertThat(meetupEventStatus.waitingList).containsExactly("David");
    }

    @Test
    void should_add_participants_from_waiting_list_when_capacity_is_increased() {
        Long meetupEventId = registerAMeetupWithCapacity(2);
        meetupSubscribe.subscribeUserToMeetupEvent("Alice", meetupEventId);
        meetupSubscribe.subscribeUserToMeetupEvent("Bob", meetupEventId);
        meetupSubscribe.subscribeUserToMeetupEvent("Charles", meetupEventId);
        meetupSubscribe.subscribeUserToMeetupEvent("David", meetupEventId);
        meetupSubscribe.subscribeUserToMeetupEvent("Emily", meetupEventId);

        int newCapacity = 4;
        meetupSubscribe.increaseCapacity(meetupEventId, newCapacity);

        MeetupEventStatusDto meetupEventStatus = meetupSubscribe.getMeetupEventStatus(meetupEventId);
        assertThat(meetupEventStatus.eventCapacity).isEqualTo(4);
        assertThat(meetupEventStatus.participants).containsExactly("Alice", "Bob", "Charles", "David");
        assertThat(meetupEventStatus.waitingList).containsExactly("Emily");
    }

}
