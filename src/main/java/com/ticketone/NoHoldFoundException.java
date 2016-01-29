package com.ticketone;

public class NoHoldFoundException extends RuntimeException {
  public NoHoldFoundException(int seatHoldId) {
    super("There is no hold " +  seatHoldId + " exists!");
  }
}
