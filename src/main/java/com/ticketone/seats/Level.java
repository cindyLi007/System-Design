package com.ticketone.seats;

public enum Level {
  ORCHESTRA(1, "Orchestra", 100.00, 25, 50),
  MAIN(2, "Main", 75.00, 20, 100),
  BALCONY_ONE(3, "Balcony 1", 50.00, 15, 100), 
  BALCONY_TWO(4, "Balcony 2", 40.00, 15, 100);

  private int id;
  private String name;
  private double price;
  private int rows;
  private int seatsPerRow;

  Level(int id, String name, double price, int rows, int seatsPerRow){
    this.id = id;
    this.name = name;
    this.price = price;
    this.rows = rows;
    this.seatsPerRow = seatsPerRow;
  }

  public int getRows() {
    return rows;
  }

  public int getSeatsPerRow() {
    return seatsPerRow;
  }

  public int getId() {
    return id;
  }

  public double getPrice() {
    return price;
  }

  public String getName() {
    return name;
  }

  @Override
  public String toString() {
    return name;
  }
}
