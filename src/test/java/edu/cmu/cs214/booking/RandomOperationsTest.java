package edu.cmu.cs214.booking;

import static edu.cmu.cs214.booking.InvariantChecks.assertInvariantHolds;

import edu.cmu.cs214.booking.domain.Room;
import edu.cmu.cs214.booking.domain.TimeInterval;
import edu.cmu.cs214.booking.domain.User;
import edu.cmu.cs214.booking.repo.InMemoryBookingStore;
import edu.cmu.cs214.booking.service.BookingResult;
import edu.cmu.cs214.booking.service.BookingService;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;

/**
 * Extension 2: a property test. A fixed seed drives a few hundred random book and
 * cancelBooking calls; after every single one the core invariant must still hold. A
 * fixed seed keeps the run reproducible, so a failure can be replayed.
 */
class RandomOperationsTest {

    private static final Room roomA = new Room("A", "Alpha", 10);
    private static final Room roomB = new Room("B", "Beta", 4);
    private static final User[] users = {
        new User("u1", "Alice"), new User("u2", "Bob"), new User("u3", "Carol")
    };

    @Test
    void randomBookAndCancelKeepTheInvariant() {
        InMemoryBookingStore store = new InMemoryBookingStore();
        BookingService svc = new BookingService(store);
        Random random = new Random(42);
        List<String> confirmedIds = new ArrayList<>();

        for (int i = 0; i < 500; i++) {
            Room room = random.nextBoolean() ? roomA : roomB;
            if (!confirmedIds.isEmpty() && random.nextInt(3) == 0) {
                svc.cancelBooking(confirmedIds.remove(random.nextInt(confirmedIds.size())));
            } else {
                User user = users[random.nextInt(users.length)];
                int start = 600 + 30 * random.nextInt(6);
                BookingResult result = svc.book(room, user, new TimeInterval(start, start + 60));
                if (result instanceof BookingResult.Confirmed confirmed) {
                    confirmedIds.add(confirmed.booking().id());
                }
            }
            assertInvariantHolds(store, roomA, roomB);
        }
    }
}
