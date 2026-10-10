package edu.cmu.cs214.booking;

import static org.junit.jupiter.api.Assertions.assertFalse;

import edu.cmu.cs214.booking.domain.Booking;
import edu.cmu.cs214.booking.domain.Room;
import edu.cmu.cs214.booking.repo.BookingStore;
import java.util.List;

/**
 * Extension 1: the core invariant ("a room never holds two confirmed bookings whose
 * intervals overlap") written once as a reusable check.
 */
public final class InvariantChecks {

    private InvariantChecks() {
    }

    /**
     * Asserts the invariant for each room. Called after every public operation in the
     * service-level tests, because the invariant must hold before and after every such
     * call — checking only at the end of a test would miss a transient violation that a
     * later operation happens to undo.
     */
    public static void assertInvariantHolds(BookingStore store, Room... rooms) {
        for (Room room : rooms) {
            List<Booking> bookings = store.bookingsForRoom(room);
            for (int i = 0; i < bookings.size(); i++) {
                for (int j = i + 1; j < bookings.size(); j++) {
                    assertFalse(bookings.get(i).interval().overlaps(bookings.get(j).interval()),
                            "Overlapping confirmed bookings in room " + room.id() + ": "
                                    + bookings.get(i) + " and " + bookings.get(j));
                }
            }
        }
    }
}
