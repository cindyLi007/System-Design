package com.ticketone;

import java.util.Optional;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import com.ticketone.exceptions.WrongLevelException;

@Path("/tickets")
@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
public class TicketResource {

  @GET
  @Path("/level/{level}")
  @Produces({MediaType.TEXT_PLAIN})
  public Response getSeats(
      @Context TicketService ticketService,
      @PathParam("level") int level) {
    ResponseBuilder responseBuilder;
    try{
      int numOfSeatsAvailable = ticketService.numSeatsAvailable(Optional.of(level));
      responseBuilder = Response.ok(numOfSeatsAvailable);
    } catch (WrongLevelException e){
      responseBuilder = Response.status(Response.Status.NOT_FOUND);
    }
    return responseBuilder.build();
  }

  @PUT
  @Path("/hold")
  public Response holdSeats(
      @Context TicketService ticketService,
      @QueryParam("numSeats") String numuber,
      @QueryParam("minLevel") String minLevel,
      @QueryParam("maxLevel") String maxLevel,
      @QueryParam("email") String customerEmail
  ) {
    try{
      ticketService.findAndHoldSeats(Integer.parseInt(numuber), Optional.of(Integer.parseInt(minLevel)),
          Optional.of(Integer.parseInt(maxLevel)), customerEmail);
      return Response.ok().build();
    } catch (Exception e) {
      return Response.status(Response.Status.BAD_REQUEST).build();
    }
  }

  @POST
  @Path("/reserve")
  public Response reserveSeats(
      @Context TicketService ticketService,
      @QueryParam("holdId") String seatHoldId,
      @QueryParam("email") String customerEmail) {
   try{
     String confirmCode = ticketService.reserveSeats(Integer.parseInt(seatHoldId), customerEmail);
     return Response.ok(confirmCode).build();
   } catch (Exception e) {
     return Response.status(Response.Status.NOT_FOUND).build();
   }
  }
}
