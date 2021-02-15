package com.redhat.api.rest;

import com.redhat.model.ShipType;
import com.redhat.model.Shot;
import com.redhat.model.ShotType;
import io.quarkus.infinispan.client.Remote;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.counter.api.CounterManager;
import org.infinispan.counter.api.StrongCounter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/shot")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class ShotsResource {
   private static final Logger LOGGER = LoggerFactory.getLogger(ShotsResource.class.getName());
   
   @Inject
   @Remote(Shot.PLAYERS_SHOTS)
   RemoteCache<String, Shot> shots;

   @Inject
   CounterManager counterManager;

   @POST
   @Path("/{gameId}/{matchId}/{userId}/{timestamp}")
   public Response trackShot(@PathParam("gameId") String gameId,
                       @PathParam("matchId") String matchId,
                       @PathParam("userId") String userId,
                       @PathParam("timestamp") long timestamp,
                       @QueryParam("type") String type,
                       @QueryParam("ship") String ship,
                       @QueryParam("human") boolean human) {
      String key =  gameId + '-' + matchId + '-' + userId +  '-' + timestamp;

      ShipType shipType = null;
      if(ship != null && !ship.isEmpty()) {
         shipType = ShipType.valueOf(ship);
      }
      ShotType shotType = ShotType.valueOf(type);

      shots.put(key, new Shot(userId, matchId, gameId, human, timestamp, shotType , shipType));

      // Use Counters too for stats
      if(human) {
         incrementCounters(shotType, shipType, "human-");
      } else {
         incrementCounters(shotType, shipType, "ia-");
      }

      return Response.accepted().build();
   }

   private void incrementCounters(ShotType shotType, ShipType shipType, String statType) {
      switch (shotType) {
         case HIT:
            StrongCounter humanHits = counterManager.getStrongCounter(statType + "hits");
            humanHits.incrementAndGet();
            break;
         case MISS:
            StrongCounter humanMiss = counterManager.getStrongCounter(statType + "misses");
            humanMiss.incrementAndGet();
            break;
         case SUNK:
            StrongCounter humanSink = counterManager.getStrongCounter(statType + "sunks");
            humanSink.incrementAndGet();
            switch (shipType) {
               case CARRIER:
                  StrongCounter humanCarrierSink = counterManager.getStrongCounter(statType + "carrier-sunks");
                  humanCarrierSink.incrementAndGet();
                  break;
               case SUBMARINE:
                  StrongCounter humanSubmarineSink = counterManager.getStrongCounter(statType + "submarine-sunks");
                  humanSubmarineSink.incrementAndGet();
                  break;
            }
            break;
      }
   }

   @GET
   public Response health() {
      return Response.ok("Shots Resource is ready").build();
   }
}
