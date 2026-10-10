package edu.cmu.cs214.booking.repo;

import static org.junit.jupiter.api.Assertions.assertEquals;

import edu.cmu.cs214.booking.domain.Booking;
import edu.cmu.cs214.booking.domain.Room;
import edu.cmu.cs214.booking.domain.TimeInterval;
import edu.cmu.cs214.booking.domain.User;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Pins the contract of {@link BookingStore#bookingsForRoom(Room)}: it returns the
 * confirmed bookings held for that one room, and nothing else. Before the Milestone 1
 * fix, {@link InMemoryBookingStore} returned every booking regardless of room, so the
 * second assertion below failed.
 */
class InMemoryBookingStoreTest {

    private final Room roomA = new Room("A", "Alpha", 10);
    private final Room roomB = new Room("B", "Beta", 4);
    private final User alice = new User("u1", "Alice");
    private final User bob = new User("u2", "Bob");

    @Test
    void bookingsForRoomReturnsOnlyThatRoomsBookings() {
        InMemoryBookingStore store = new InMemoryBookingStore();
        store.addBooking(new Booking("b1", roomA, alice, new TimeInterval(600, 660)));
        store.addBooking(new Booking("b2", roomB, bob, new TimeInterval(600, 660)));

        assertEquals(List.of("b1"), store.bookingsForRoom(roomA).stream().map(Booking::id).toList());
        assertEquals(List.of("b2"), store.bookingsForRoom(roomB).stream().map(Booking::id).toList());
    }
}
