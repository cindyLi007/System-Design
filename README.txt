Ticket Service Web Service
--------------------------------------------------------------------------------
Welcome to Ticket Service Web Service

Ticket Service is an online Web Service that processes ticket search, hold and purchase using the Web Service API.

To build project and run test, just simply run "mvn clean install", you can also skip tests by add "-DskipTests"

The main engine of the application is TicketServiceImpl. I used Multimap to store available seats grouped by level, and 2 CurrentMaps to store held and reserved orders.

Since based on the given interface, for method findAndHoldSeats, it finds and holds the best available seats for a customer, so I always find search the "minLevel" first, no matter whether the seats are continuous or not.

