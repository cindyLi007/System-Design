package com.ticketone;

public class SeatIsNotEnoughException extends RuntimeException {
  public SeatIsNotEnoughException(int numSeats) {
    super("There is no enough seats to hold for: " +  numSeats + " seats");
  }
}
