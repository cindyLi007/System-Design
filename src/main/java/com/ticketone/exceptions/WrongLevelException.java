package com.ticketone.exceptions;

public class WrongLevelException extends RuntimeException {
  public WrongLevelException(int level){
    super("Wrong level number: " +  level + ". Level number should be integer 1, 2, 3, 4!");
  }
}
