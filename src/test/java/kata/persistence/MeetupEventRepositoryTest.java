package kata.persistence;

import kata.MeetupEvent;
import kata.Subscription;
import kata.dbtestutil.MemoryDbTestContext;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MeetupEventRepositoryTest {

    private MemoryDbTestContext memoryDbTestContext;
    private MeetupEventRepository meetupEventRepository;

    @BeforeEach
    void setUp() throws Exception {
        memoryDbTestContext = MemoryDbTestContext.openWithSql("/setup.sql");
        Jdbi jdbi = memoryDbTestContext.getJdbi();
        meetupEventRepository = new MeetupEventRepository(jdbi);
    }

    @AfterEach
    void tearDown() {
        memoryDbTestContext.close();
    }

    @Test
    void should_save_and_find_meetup_event_correctly() {
        long meetupEventId = 1;
        Instant time = Instant.now();

        List<Subscription> subscriptions = Arrays.asList(
                new Subscription("Alice", time, false),
                new Subscription("Bob", time, false),
                new Subscription("Charles", time, true)
        );
        meetupEventRepository.save(new MeetupEvent(
                meetupEventId,
                2,
                "Great Meetup",
                LocalDateTime.of(2019, 1, 1, 11, 0),
                subscriptions
        ));
        MeetupEvent meetupEvent = meetupEventRepository.findById(meetupEventId);
        assertThat(meetupEvent.getCapacity()).isEqualTo(2);
        assertThat(meetupEvent.getEventName()).isEqualTo("Great Meetup");
        assertThat(meetupEvent.getStartTime()).isEqualTo(LocalDateTime.of(2019, 1, 1, 11, 0));
        assertThat(meetupEvent.getSubscriptions()).containsExactlyElementsOf(subscriptions);

        List<Subscription> subscriptionsUpdated = Arrays.asList(
                new Subscription("Alice", time, false),
                new Subscription("Charles", time, false),
                new Subscription("David", time, false),
                new Subscription("Emily", time, true)
        );
        meetupEventRepository.save(new MeetupEvent(
                meetupEventId,
                3,
                "Really Great Meetup",
                LocalDateTime.of(2019, 1, 1, 11, 30),
                subscriptionsUpdated
        ));
        MeetupEvent meetupEventUpdated = meetupEventRepository.findById(meetupEventId);
        assertThat(meetupEventUpdated.getCapacity()).isEqualTo(3);
        assertThat(meetupEventUpdated.getEventName()).isEqualTo("Really Great Meetup");
        assertThat(meetupEventUpdated.getStartTime()).isEqualTo(LocalDateTime.of(2019, 1, 1, 11, 30));
        assertThat(meetupEventUpdated.getSubscriptions()).containsExactlyElementsOf(subscriptionsUpdated);
    }

    @Test
    void should_save_and_find_correctly_when_meetup_has_empty_subscriptions() {
        long meetupEventId = 1;
        meetupEventRepository.save(new MeetupEvent(
                meetupEventId,
                2,
                "Great Meetup",
                LocalDateTime.of(2019, 1, 1, 11, 0),
                Collections.emptyList()
        ));

        MeetupEvent meetupEvent = meetupEventRepository.findById(meetupEventId);

        assertThat(meetupEvent).isNotNull();
    }

    @Test
    void should_give_null_if_meetup_doesnt_exist() {
        MeetupEvent meetupEvent = meetupEventRepository.findById(99L);
        assertThat(meetupEvent).isNull();
    }
}