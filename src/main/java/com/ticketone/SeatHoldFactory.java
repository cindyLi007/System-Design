package com.ticketone;

import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.base.Preconditions;

public class SeatHoldFactory {

  private static SeatHoldFactory instance = null;

  private int expiredTime;
  private TicketServiceImpl ticketService;
  private AtomicInteger idGenerator = new AtomicInteger(0);

  private SeatHoldFactory(TicketServiceImpl ticketService, int expiredTime) {
    this.expiredTime = expiredTime;
    this.ticketService = ticketService;
  }

  public SeatHold create(String customerEmail) {
    Preconditions.checkNotNull(customerEmail);

    return new SeatHold(customerEmail, idGenerator.getAndIncrement(), expiredTime, ticketService);
  }

  public synchronized static SeatHoldFactory getInstance(TicketServiceImpl ticketService, int expiredTime) {
    if (instance == null) {
      instance = new SeatHoldFactory(ticketService, expiredTime);
    }
    return instance;
  }
}
