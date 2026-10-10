package edu.cmu.cs214.booking.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static edu.cmu.cs214.booking.InvariantChecks.assertInvariantHolds;

import edu.cmu.cs214.booking.domain.Booking;
import edu.cmu.cs214.booking.domain.Room;
import edu.cmu.cs214.booking.domain.TimeInterval;
import edu.cmu.cs214.booking.domain.User;
import edu.cmu.cs214.booking.domain.WaitlistEntry;
import edu.cmu.cs214.booking.repo.InMemoryBookingStore;
import java.util.List;
import org.junit.jupiter.api.Test;

class CancelBookingTest {

    private final Room roomA = new Room("A", "Alpha", 10);
    private final Room roomB = new Room("B", "Beta", 4);
    private final User alice = new User("u1", "Alice");
    private final User bob = new User("u2", "Bob");
    private final User carol = new User("u3", "Carol");
    private final User dave = new User("u4", "Dave");

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
        assertInvariantHolds(store, roomA, roomB);
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
        assertInvariantHolds(store, roomA, roomB);
    }

    /** S3: cancelling promotes the waiting user whose slot is now free. */
    @Test
    void cancellingPromotesTheWaitlistedUser() {
        newService();
        Booking a = confirmed(svc.book(roomA, alice, new TimeInterval(600, 660)));
        assertInstanceOf(BookingResult.Waitlisted.class,
                svc.book(roomA, bob, new TimeInterval(630, 700)));

        svc.cancelBooking(a.id());

        List<Booking> bookings = svc.listBookings(roomA);
        assertEquals(1, bookings.size());
        assertEquals(bob, bookings.get(0).user());
        assertEquals(new TimeInterval(630, 700), bookings.get(0).interval());
        assertTrue(store.waitlistForRoom(roomA).isEmpty());
        assertInvariantHolds(store, roomA, roomB);
    }

    /** S4: first-come-first-served, and at most one promotion per cancellation. */
    @Test
    void cancellationPromotesEarliestWaiterOnly() {
        newService();
        Booking a = confirmed(svc.book(roomA, alice, new TimeInterval(600, 720)));
        assertInstanceOf(BookingResult.Waitlisted.class,
                svc.book(roomA, bob, new TimeInterval(600, 660)));
        assertInstanceOf(BookingResult.Waitlisted.class,
                svc.book(roomA, carol, new TimeInterval(660, 700)));

        svc.cancelBooking(a.id());

        List<Booking> bookings = svc.listBookings(roomA);
        assertEquals(1, bookings.size());
        assertEquals(bob, bookings.get(0).user());
        List<WaitlistEntry> waiters = store.waitlistForRoom(roomA);
        assertEquals(1, waiters.size());
        assertEquals(carol, waiters.get(0).user());
        assertInvariantHolds(store, roomA, roomB);
    }

    /** S5: skip a waiter whose interval still conflicts with another booking. */
    @Test
    void cancellationSkipsWaiterWhoStillConflicts() {
        newService();
        Booking a = confirmed(svc.book(roomA, alice, new TimeInterval(600, 660)));
        confirmed(svc.book(roomA, dave, new TimeInterval(700, 760)));
        assertInstanceOf(BookingResult.Waitlisted.class,
                svc.book(roomA, bob, new TimeInterval(630, 730)));
        assertInstanceOf(BookingResult.Waitlisted.class,
                svc.book(roomA, carol, new TimeInterval(610, 650)));

        svc.cancelBooking(a.id());

        List<Booking> bookings = svc.listBookings(roomA);
        assertEquals(2, bookings.size());
        assertTrue(bookings.stream().anyMatch(
                b -> b.user().equals(carol) && b.interval().equals(new TimeInterval(610, 650))));
        List<WaitlistEntry> waiters = store.waitlistForRoom(roomA);
        assertEquals(1, waiters.size());
        assertEquals(bob, waiters.get(0).user());
        assertInvariantHolds(store, roomA, roomB);
    }

    /** S6: promotion never crosses rooms. */
    @Test
    void cancellationDoesNotPromoteAcrossRooms() {
        newService();
        Booking a = confirmed(svc.book(roomA, alice, new TimeInterval(600, 660)));
        confirmed(svc.book(roomB, dave, new TimeInterval(600, 660)));
        assertInstanceOf(BookingResult.Waitlisted.class,
                svc.book(roomB, bob, new TimeInterval(630, 700)));

        svc.cancelBooking(a.id());

        assertTrue(svc.listBookings(roomA).isEmpty());
        assertEquals(1, svc.listBookings(roomB).size());
        assertEquals(1, store.waitlistForRoom(roomB).size());
        assertInvariantHolds(store, roomA, roomB);
    }
}
