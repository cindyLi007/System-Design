Ticket Service System
--------------------------------------------------------------------------------
Welcome to Ticket Service System

>- Ticket Service system is a Web Service System that processes ticket search, hold and reservation using Web API.

To build project and run test, just simply run **"mvn clean install"**, you can also skip tests by add **"-DskipTests"**.

>- My Implementation and Assumptions: 

>- To save time, I used a template to setup project. Please just review code in **/src/main/java/com/ticketone/** and **/src/test/java/com/ticketone/**, all other code is rendered by template. 

>- To implement the given interface **TicketService**, the main engine of the application is **TicketServiceImpl**. I used ``Guava Multimap`` to store available seats grouped by level, and 2 ```ConcurrentMaps``` to store held and reserved orders.

>- Based on the given interface, for method ```findAndHoldSeats```, it finds and holds the best available seats for a customer, so I always search the "minLevel" seats first, no matter whether the seats are continuous or not.

>- If there is no enought seats  for a hold request, I will return a ```SeatIsNotEnoughException``` to indicate this hold failed instead of assigning part of the seats.

>- **SeatHold** is an object to keep the hold or reserved seat list. It has a ```Status``` field to identify it is in "held" status or "reserved" status. If it is in "held" status,  I use ```holdId``` to search it; if it is in "reserved" status, I use ```confirmCode``` to search. In it, I use a Java ```Timer``` and ```TimerTask``` to implement expiration, when timeout, will return its seats to ```TicketServiceImpl```.

>- My implementation support **Multi-threading**, I also including Unit test to test multiple threads.

