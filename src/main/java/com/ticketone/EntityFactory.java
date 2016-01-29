package com.ticketone;

import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.base.Preconditions;

/**
 * <code>EntityFactory</code> is a factory class for instantiating entities
 */
public class EntityFactory {

  /* This is the singleton instance of this class */
  private static EntityFactory instance = null;

  private int expiredTime;
  private TicketServiceImpl ticketService;
  private AtomicInteger idGenerator = new AtomicInteger(0);

  private EntityFactory(TicketServiceImpl ticketService, int expiredTime) {
    this.expiredTime = expiredTime;
    this.ticketService = ticketService;
  }

  public SeatHold create(String customerEmail) {
    Preconditions.checkNotNull(customerEmail);

    return new SeatHold(customerEmail, idGenerator.getAndIncrement(), expiredTime, ticketService);
  }

  public synchronized static EntityFactory getInstance(TicketServiceImpl ticketService, int expiredTime) {
    if (instance == null) {
      instance = new EntityFactory(ticketService, expiredTime);
    }
    return instance;
  }
}
