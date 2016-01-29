package com.ticketone.exceptions;

public class NoHoldFoundException extends RuntimeException {
  public NoHoldFoundException(int seatHoldId) {
    super("There is no hold for hold number : " +  seatHoldId + " exists!");
  }
}
