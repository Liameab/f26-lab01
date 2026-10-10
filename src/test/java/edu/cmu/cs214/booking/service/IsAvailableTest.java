package edu.cmu.cs214.booking.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import edu.cmu.cs214.booking.domain.Room;
import edu.cmu.cs214.booking.domain.TimeInterval;
import edu.cmu.cs214.booking.domain.User;
import edu.cmu.cs214.booking.repo.InMemoryBookingStore;
import org.junit.jupiter.api.Test;

/**
 * These tests encode what {@code isAvailable} must mean — "the room is free over this
 * interval" — and fail against the agent's patch. Its guard only inspects the existing
 * booking's start, so a booking that begins before the request and runs into it is
 * reported as free.
 */
class IsAvailableTest {

    private final Room roomA = new Room("A", "Alpha", 10);
    private final User alice = new User("u1", "Alice");

    private BookingService newService() {
        return new BookingService(new InMemoryBookingStore());
    }

    /** Existing [570,630) runs into request [600,660): they conflict. */
    @Test
    void notAvailableWhenAnEarlierBookingRunsIntoTheRequest() {
        BookingService svc = newService();
        svc.book(roomA, alice, new TimeInterval(570, 630));

        assertFalse(svc.isAvailable(roomA, new TimeInterval(600, 660)));
    }

    /** Existing [540,720) encloses request [600,660): they conflict. */
    @Test
    void notAvailableWhenABookingEnclosesTheRequest() {
        BookingService svc = newService();
        svc.book(roomA, alice, new TimeInterval(540, 720));

        assertFalse(svc.isAvailable(roomA, new TimeInterval(600, 660)));
    }

    /** isAvailable must agree with what book actually decides. */
    @Test
    void isAvailableAgreesWithBook() {
        BookingService svc = newService();
        svc.book(roomA, alice, new TimeInterval(570, 630));
        TimeInterval request = new TimeInterval(600, 660);

        boolean free = svc.isAvailable(roomA, request);
        BookingResult result = svc.book(roomA, alice, request);

        assertEquals(free, result instanceof BookingResult.Confirmed);
    }
}
