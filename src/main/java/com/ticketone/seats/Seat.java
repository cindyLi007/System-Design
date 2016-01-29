package com.ticketone.seats;

import com.google.common.base.Objects;

/**
 * Seat contains it's level, row number, column number.
 * I used enum Level instead of level number since we can retrieve level details such as price, name from Level
 */
public class Seat {

  private final Level level;
  private final int rowNumber;
  private final int columnNumber;

  public Seat(Level level, int rowNumber, int columnNumber) {
    this.level = level;
    this.rowNumber = rowNumber;
    this.columnNumber = columnNumber;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    Seat seat = (Seat)o;
    return Objects.equal(level, seat.level) &&
        Objects.equal(rowNumber, seat.rowNumber) &&
        Objects.equal(columnNumber, seat.columnNumber);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(level, rowNumber, columnNumber);
  }

  public Level getLevel() {
    return level;
  }

  public int getRowNumber() {
    return rowNumber;
  }

  public int getColumnNumber() {
    return columnNumber;
  }

}
