package com.ticketone;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.util.Optional;
import java.util.concurrent.CountDownLatch;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.ticketone.exceptions.NoHoldFoundException;
import com.ticketone.exceptions.SeatIsNotEnoughException;
import com.ticketone.exceptions.WrongLevelException;

public class TicketServiceImplTest {

  public static final String CUSTOMER_EMAIL = "customer@email.com";
  private TicketServiceImpl ticketService;
  private CountDownLatch startLatch, stopLatch, allDoneLatch;

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Before
  public void before() {
    ticketService = new TicketServiceImpl();
  }

  @After
  public void after() {
    ticketService = null;
  }

  @Test
  public void testNumSeatsAvailable_givenWrongLevelNumber_shouldThrowWrongLevelException() {
    // Given
    expectedException.expect(WrongLevelException.class);
    expectedException.expectMessage("Wrong level number: 0. Level number should be integer 1, 2, 3, 4!");

    // When
    ticketService.numSeatsAvailable(Optional.of(0));
  }

  @Test
  public void testNumSeatsAvailable_givenNullLevelNumber_shouldReturnTotalNumberForAllLevels() {
    // Given

    // When
    int totalNumber = ticketService.numSeatsAvailable(Optional.<Integer>empty());

    // Then
    assertThat(totalNumber, equalTo(6250));
  }

  @Test
  public void testNumSeatsAvailable_givenALevelNumber_shouldReturnNumberForTheLevel() {
    // Given

    // WhenString customerEmail,
    int totalNumber = ticketService.numSeatsAvailable(Optional.of(2));

    // Then
    assertThat(totalNumber, equalTo(2000));
  }

  @Test
  public void testFindAndHoldSeats_givenNotEnoughTicket_shouldThrowSeatIsNotEnoughException()
      throws Exception {
    // Given
    expectedException.expect(SeatIsNotEnoughException.class);
    expectedException.expectMessage("There is no enough seats to hold for: 3000 seats");

    // When
    ticketService.findAndHoldSeats(3000, Optional.ofNullable(1), Optional.ofNullable(1), CUSTOMER_EMAIL);
  }

  @Test
  public void testFindAndHoldSeats_givenWrongLevelNumber_shouldThrowLevelIsNotValidException()
      throws Exception {
    // Given
    expectedException.expect(IllegalArgumentException.class);
    expectedException.expectMessage("Unexpected number for min level: 5, expected 1, 2, 3, 4 or null");

    // When
    ticketService.findAndHoldSeats(200, Optional.ofNullable(5), Optional.empty(), CUSTOMER_EMAIL);
  }

  @Test
  public void testFindAndHoldSeats_givenLevelNumberMinIsGreaterThanMax_shouldThrowLevelIsNotValidException()
      throws Exception {
    // Given
    expectedException.expect(IllegalArgumentException.class);
    expectedException.expectMessage(
        "Max level must be equal or greater than min level! Now the max level is: 1 ane the min level is: 3");

    // When
    ticketService.findAndHoldSeats(200, Optional.ofNullable(3), Optional.ofNullable(1), CUSTOMER_EMAIL);
  }

  @Test
  public void testFindAndHoldSeats_givenSingleThread_shouldReturnSeatHold() throws Exception {
    // Given

    // When
    SeatHold seatHold = ticketService.findAndHoldSeats(200, Optional.ofNullable(1), Optional.ofNullable(1),
        CUSTOMER_EMAIL);
    Thread.sleep(11 * 1000);

    // Then
    assertThat(seatHold.getHoldSeatList().size(), equalTo(200));
    assertThat(ticketService.numSeatsAvailable(Optional.of(1)), equalTo(1050));

    // When
    Thread.sleep(31 * 1000);
    assertThat(ticketService.numSeatsAvailable(Optional.of(1)), equalTo(1250));
  }

  @Test
  public void testFindAndHoldSeats_givenMultipleThreads_shouldReturnCorrectValue() throws Exception {
    // Given
    SeatHoldAttempt[] seatHoldAttempts = new SeatHoldAttempt[4];
    stopLatch = new CountDownLatch(4);
    startLatch = new CountDownLatch(1);

    for (int i = 0; i < 4; i++) {
      seatHoldAttempts[i] = new SeatHoldAttempt(ticketService);
      seatHoldAttempts[i].start();
    }

    // When
    startLatch.countDown();
    stopLatch.await();

    Thread.sleep(10 * 1000);

    // Then
    assertThat(ticketService.numSeatsAvailable(Optional.of(3)), equalTo(0));
    assertThat(ticketService.numSeatsAvailable(Optional.of(4)), equalTo(1000));
    assertThat(ticketService.numSeatsAvailable(Optional.of(1)), equalTo(1250));
    assertThat(ticketService.numSeatsAvailable(Optional.of(2)), equalTo(2000));
  }

  @Test
  public void testReserveSeats_givenValidSeatHoldId_shouldReserveSeats() throws Exception {
    // Given
    SeatHold seatHold_1 = ticketService.findAndHoldSeats(300, Optional.of(1), Optional.of(2), CUSTOMER_EMAIL);
    SeatHold seatHold_2 = ticketService.findAndHoldSeats(400, Optional.of(2), Optional.of(2), CUSTOMER_EMAIL);
    SeatHold seatHold_3 = ticketService.findAndHoldSeats(500, Optional.of(3), Optional.of(3), CUSTOMER_EMAIL);

    // When
    String confirmNum = ticketService.reserveSeats(seatHold_1.getHoldId(), CUSTOMER_EMAIL);

    // Then
    assertThat(ticketService.numSeatsAvailable(Optional.of(1)), equalTo(950));
    SeatHold seatConfirm = ticketService.getPurchaseMap().get(confirmNum);
    assertThat(seatConfirm, notNullValue());
    assertThat(seatConfirm.getHoldSeatList().size(), equalTo(300));
  }

  @Test
  public void testReserveSeats_givenInValidSeatHoldId_shouldThrowNoHoldFoundException() throws Exception {
    // Given
    expectedException.expect(NoHoldFoundException.class);
    expectedException.expectMessage("There is no hold for hold number : 10 exists!");

    // When
    ticketService.reserveSeats(10, CUSTOMER_EMAIL);
  }

  private final class SeatHoldAttempt extends Thread {

    private final TicketService ticketService;

    private SeatHoldAttempt(final TicketService ticketService) {
      this.ticketService = ticketService;
    }

    @Override
    public void run() {
      try {
        startLatch.await();
        ticketService.findAndHoldSeats(500, Optional.of(3), Optional.of(4), CUSTOMER_EMAIL);
      } catch (InterruptedException e) {
        e.printStackTrace();
      } finally {
        stopLatch.countDown();
      }
    }
  }
}
