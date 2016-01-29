package com.ticketone;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;

import com.google.common.collect.Lists;
import com.ticketone.seats.Seat;

/**
 * This is the data structure identifying the specific seats and related information for hold and reservation
 * If it is in held status, its identifier is holdId
 * If it is in reserved status, its identifier is confirmCode
 * I use {@link EntityFactory} to render it
 */
public class SeatHold {

  private int holdId;
  private String confirmCode;
  private final List<Seat> holdSeatList;
  private String customerEmail;
  private Timer expireTimer;
  private TicketServiceImpl ticketService;
  private Status status;

  Logger LOG = Logger.getLogger(SeatHold.class);

  SeatHold(String customerEmail, int holdId, int seconds, TicketServiceImpl ticketService) {
    this.holdId = holdId;
    this.customerEmail = customerEmail;
    holdSeatList = Lists.newLinkedList();
    this.ticketService = ticketService;
    initCleanupTimer(seconds);
    status = Status.HELD;
  }

  private void initCleanupTimer(int expireInterval) {
    expireTimer = new Timer();
    long timerInterval = expireInterval * 1000;

    expireTimer.schedule(new TimerTask() {
      @Override
      public void run() {
        ticketService.recycleSeatHold(holdId);
        // avoid to use finalize(), JVM will decide when to garbage collection this object
      }
    }, timerInterval);
  }

  void confirm(String customerEmail, String confirmCode) {
    expireTimer.cancel();
    this.confirmCode = confirmCode;
    this.customerEmail = customerEmail;
    this.status = Status.RESERVED;
  }

  List<Seat> getHoldSeatList() {
    return holdSeatList;
  }

  String getCustomerEmail() {
    return customerEmail;
  }

  int getHoldId() {
    return holdId;
  }

  void add(List<Seat> list) {
    holdSeatList.addAll(list);
  }

  public Status getStatus() {
    return status;
  }

  public String getConfirmCode() {
    return confirmCode;
  }

}
