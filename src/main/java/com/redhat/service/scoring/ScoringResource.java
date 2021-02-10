package com.redhat.service.scoring;

import com.redhat.model.ConnectedPlayer;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.Configuration;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.client.hotrod.configuration.TransactionMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static com.redhat.model.ConnectedPlayer.CONNECTED_PLAYERS;

@Path("/scoring")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class ScoringResource {
   private static final Logger LOGGER = LoggerFactory.getLogger(ScoringResource.class.getName());
   private RemoteCache<String, ConnectedPlayer> connectedPlayers;

   @Inject
   public ScoringResource(RemoteCacheManager remoteCacheManager) {
      connectedPlayers = remoteCacheManager.getCache(CONNECTED_PLAYERS, TransactionMode.NON_XA);
   }

   @POST
   public Response add(ScoringDelta scoringDelta) {
      String id = scoringDelta.getUserId() + '-' + scoringDelta.getMatchId();
      ConnectedPlayer connectedPlayer = connectedPlayers.get(id);
      if (connectedPlayer == null) {
         return Response.status(Response.Status.NOT_FOUND).build();
      }

      while (!writeDelta(scoringDelta, id)) {
         // Retry write
      }

      return Response.accepted().build();
   }

   private boolean writeDelta(ScoringDelta scoringDelta, String id) {
      try {
         TransactionManager transactionManager = connectedPlayers.getTransactionManager();
         transactionManager.begin();
         ConnectedPlayer connectedPlayer = connectedPlayers.get(id);
         connectedPlayer.setScore(connectedPlayer.getScore() + scoringDelta.getDelta());
         if (connectedPlayer.getTimestamp() < scoringDelta.getTimestamp()) {
            connectedPlayer.setTimestamp(scoringDelta.getTimestamp());
         }
         connectedPlayers.put(id, connectedPlayer);
         transactionManager.commit();
         return true;
      } catch ( NotSupportedException | SystemException ex) {
         throw new IllegalStateException("Not supported transactions ", ex);
      } catch (HeuristicMixedException | HeuristicRollbackException | RollbackException e) {
         return false;
      }
   }

   @GET
   public Response health() {
      int size = connectedPlayers.size();
      LOGGER.info("Connected players size " + size);
      return Response.ok("Scoring Resource is ready. Connected players size " + size).build();
   }

}
