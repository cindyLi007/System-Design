package com.ticketone;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;

import javax.ws.rs.Path;

import org.apache.log4j.Logger;

import com.google.common.base.Preconditions;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.ticketone.exceptions.NoHoldFoundException;
import com.ticketone.exceptions.SeatIsNotEnoughException;
import com.ticketone.exceptions.WrongLevelException;
import com.ticketone.seats.Level;
import com.ticketone.seats.Seat;

/**
 * The main implementation of the application.
 * It holds an in-memory data for all available seats, all held order and all reserved order.
 */
@Path("/")
public class TicketServiceImpl implements TicketService {
  private final int expiredTime;
  /**
   * {@link Multimap} is a collection that maps keys to values, similar to Map, but in which each key may be associated with multiple values.
   * So you can treat it as <Integer, List<Seat>>
   **/
  private final Multimap<Integer,Seat> availableSeatsByLevel;
  /**
   * use {@link ConcurrentMap} to provide thread safe for held and reserve map.
   */
  private final ConcurrentMap<Integer,SeatHold> seatHoldMap;
  private final ConcurrentMap<String,SeatHold> purchaseMap;

  Logger LOG = Logger.getLogger(TicketServiceImpl.class);

  public TicketServiceImpl() {
    availableSeatsByLevel = LinkedListMultimap.create();
    seatHoldMap = Maps.newConcurrentMap();
    purchaseMap = Maps.newConcurrentMap();
    expiredTime = 30;

    for (Level level : Level.values()) {
      for (int i=0; i<level.getRows(); i++) {
        for (int j=0; j<level.getSeatsPerRow(); j++) {
          Seat seat = new Seat(level, i, j);
          availableSeatsByLevel.put(level.getId(), seat);
        }
      }
    }
  }

  public int numSeatsAvailable(Optional<Integer> venueLevel) {
    if (venueLevel.isPresent() && venueLevel.get() >= 1 && venueLevel.get() <= 4) {
      return availableSeatsByLevel.get(venueLevel.get()).size();
    }

    if (!venueLevel.isPresent()) {
      int count =0;
      for (Integer key: availableSeatsByLevel.keySet()) {
        count += availableSeatsByLevel.get(key).size();
      }
      return count;
    }

    throw new WrongLevelException(venueLevel.get());
  }

  public SeatHold findAndHoldSeats(
      int numSeats, Optional<Integer> minLevel, Optional<Integer> maxLevel, String customerEmail) {
    validateLevelNumber(minLevel);
    validateLevelNumber(maxLevel);

    int startLevel = minLevel.orElse(1);
    int endLevel = maxLevel.orElse(4);
    // need lock available seats map when doing check and hold
    synchronized (availableSeatsByLevel) {
      if(!checkSeatsIsEnough(startLevel, endLevel, numSeats)) {
        throw new SeatIsNotEnoughException(numSeats);
      }

      SeatHold seatHold = EntityFactory.getInstance(this, expiredTime).create(customerEmail);
      for (int level = startLevel; level <= endLevel; level++) {
        Collection<Seat> seatsInCurrentLevle = availableSeatsByLevel.get(level);
        Iterator<Seat> iterator = seatsInCurrentLevle.iterator();
        List<Seat> list = Lists.newArrayList();
        while (numSeats > 0 && iterator.hasNext()) {
          numSeats--;
          Seat seat = iterator.next();
          list.add(seat);
        }
        seatsInCurrentLevle.removeAll(list);
        seatHold.add(list);
        if (numSeats == 0)
          break;
      }
      seatHoldMap.put(seatHold.getHoldId(), seatHold);
      return seatHold;
    }
  }

  private boolean checkSeatsIsEnough(int startLevel, int endLevel, int numSeats) {
    Preconditions.checkArgument(startLevel <= endLevel,
        "Max level must be equal or greater than min level! Now the max level is: " + endLevel +
            " ane the min level is: " + startLevel);

    int count=0;
    for (int level=startLevel; level<=endLevel; level++) {
      count += availableSeatsByLevel.get(level).size();
      if (count >= numSeats) return true;
    }
    return false;
  }

  private void validateLevelNumber(Optional<Integer> minLevel) {
    Preconditions.checkArgument(
        (minLevel.isPresent() && minLevel.get() <= 4 && minLevel.get() >= 1) || !minLevel.isPresent(),
        "Unexpected number for min level: " + minLevel.get() + ", expected 1, 2, 3, 4 or null");
  }

  public String reserveSeats(int seatHoldId, String customerEmail) {
    SeatHold seatHold = seatHoldMap.remove(seatHoldId);
    if (seatHold == null) {
      throw new NoHoldFoundException(seatHoldId);
    }
    String uuid = UUID.randomUUID().toString();
    seatHold.confirm(customerEmail, uuid);
    purchaseMap.put(uuid, seatHold);
    return uuid;
  }

  /**
   * Invoked from {@link SeatHold} when timer expires.
   * @param id id of the expired SeatHold
   */
  void recycleSeatHold(int id) {
    SeatHold seatHold = seatHoldMap.remove(id);
    // need lock available seats map when doing recycle
    synchronized (availableSeatsByLevel) {
      List<Seat> holdSeatList = seatHold.getHoldSeatList();
      holdSeatList.forEach((o) -> {
        availableSeatsByLevel.put(o.getLevel().getId(), o);
      });
    }
  }

  /**
   * Send an Email to customer
   * @param customerEmail customer Email
   * @param message message send to customer
   */
  public void sendEmail(String customerEmail, String message) {
    if (LOG.isInfoEnabled()) {
      LOG.warn(message);
    }
    System.out.println(message);
  }

  public ConcurrentMap<String, SeatHold> getPurchaseMap() {
    return purchaseMap;
  }
}
