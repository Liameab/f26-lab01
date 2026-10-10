package edu.cmu.cs214.booking.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.cmu.cs214.booking.domain.Booking;
import edu.cmu.cs214.booking.domain.Room;
import edu.cmu.cs214.booking.domain.TimeInterval;
import edu.cmu.cs214.booking.domain.User;
import edu.cmu.cs214.booking.repo.InMemoryBookingStore;
import org.junit.jupiter.api.Test;

class CancelBookingTest {

    private final Room roomA = new Room("A", "Alpha", 10);
    private final User alice = new User("u1", "Alice");
    private final User bob = new User("u2", "Bob");

    private InMemoryBookingStore store;
    private BookingService svc;

    private void newService() {
        store = new InMemoryBookingStore();
        svc = new BookingService(store);
    }

    private Booking confirmed(BookingResult result) {
        return assertInstanceOf(BookingResult.Confirmed.class, result).booking();
    }

    /** S1: cancelling a booking removes it. */
    @Test
    void cancelRemovesTheBooking() {
        newService();
        Booking a = confirmed(svc.book(roomA, alice, new TimeInterval(600, 660)));

        svc.cancelBooking(a.id());

        assertTrue(svc.listBookings(roomA).isEmpty());
    }

    /** S2: cancelling an unknown id changes nothing. */
    @Test
    void cancelWithUnknownIdDoesNothing() {
        newService();
        confirmed(svc.book(roomA, alice, new TimeInterval(600, 660)));
        assertInstanceOf(BookingResult.Waitlisted.class,
                svc.book(roomA, bob, new TimeInterval(630, 700)));

        svc.cancelBooking("does-not-exist");

        assertEquals(1, svc.listBookings(roomA).size());
        assertEquals(1, store.waitlistForRoom(roomA).size());
    }
}
