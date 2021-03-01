package com.redhat.api.websockets;

import com.redhat.model.PlayerScore;
import com.redhat.model.Shot;
import io.quarkus.scheduler.Scheduled;
import io.vertx.core.json.JsonObject;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.Search;
import org.infinispan.query.dsl.Query;
import org.infinispan.query.dsl.QueryFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint("/stats")
@ApplicationScoped
public class StatsEndpoint {
   private static final Logger LOGGER = LoggerFactory.getLogger(StatsEndpoint.class.getName());

   private Map<String, Session> sessions = new ConcurrentHashMap<>();

   private Map<String, Long> stats = new ConcurrentHashMap<>();

   RemoteCache<String, PlayerScore> playersScores;
   RemoteCache<String, Shot> shots;

   RemoteCacheManager remoteCacheManager;

   @Inject
   public StatsEndpoint(RemoteCacheManager remoteCacheManager) {
      this.remoteCacheManager = remoteCacheManager;
      stats.put("human-active", 0L);
      stats.put("human-hits", 0L);
      stats.put("human-misses", 0L);
      stats.put("human-sunks", 0L);
      stats.put("human-submarine-sunks", 0L);
      stats.put("human-carrier-sunks", 0L);
      stats.put("ai-active", 0L);
      stats.put("ai-hits", 0L);
      stats.put("ai-misses", 0L);
      stats.put("ai-sunks", 0L);
      stats.put("ai-submarine-sunks", 0L);
      stats.put("ai-carrier-sunks",0L);
   }

   @OnOpen
   public void onOpen(Session session) {
      sessions.put(session.getId(), session);
      LOGGER.info("Leaderboard service socket opened");
   }

   @Scheduled(every = "1s")
   public void calculateStats() throws IOException {
      if(sessions.isEmpty()) {
         return;
      }
      if (checkAvailabilityOfCaches())
         return;

      QueryFactory queryFactoryPlayerScores = Search.getQueryFactory(playersScores);
      QueryFactory queryFactoryShots = Search.getQueryFactory(shots);
      Query<Object[]> countActiveHumanPlayers = queryFactoryPlayerScores
            .create("SELECT COUNT(p.userId) FROM com.redhat.PlayerScore p WHERE p.human=true AND p.gameStatus='PLAYING'");

      Query<Object[]> countActiveAIPlayers = queryFactoryPlayerScores
            .create("SELECT COUNT(p.userId) FROM com.redhat.PlayerScore p WHERE p.human=false AND p.gameStatus='PLAYING'");

      Query<Object[]> countHumanHits = queryFactoryShots
            .create("SELECT COUNT(s.userId) FROM com.redhat.Shot s WHERE s.human=true AND s.shotType='HIT'");

      Query<Object[]> countAiHits = queryFactoryShots
            .create("SELECT COUNT(s.userId) FROM com.redhat.Shot s WHERE s.human=false AND s.shotType='HIT'");

      Query<Object[]> countHumanMisses = queryFactoryShots
            .create("SELECT COUNT(s.userId) FROM com.redhat.Shot s WHERE s.human=true AND s.shotType='MISS'");

      Query<Object[]> countAiMisses = queryFactoryShots
            .create("SELECT COUNT(s.userId) FROM com.redhat.Shot s WHERE s.human=false AND s.shotType='MISS'");

      Query<Object[]> countHumanSunks = queryFactoryShots
            .create("SELECT COUNT(s.userId) FROM com.redhat.Shot s WHERE s.human=true AND s.shotType='SUNK'");

      Query<Object[]> countAiSunks = queryFactoryShots
            .create("SELECT COUNT(s.userId) FROM com.redhat.Shot s WHERE s.human=false AND s.shotType='SUNK'");

      Query<Object[]> countHumanCarrierSunks = queryFactoryShots
            .create("SELECT COUNT(s.userId) FROM com.redhat.Shot s WHERE s.human=true AND s.shotType='SUNK' and s.shipType='CARRIER'");

      Query<Object[]> countAiCarrierSunks = queryFactoryShots
            .create("SELECT COUNT(s.userId) FROM com.redhat.Shot s WHERE s.human=false AND s.shotType='SUNK' and s.shipType='CARRIER'");

      Query<Object[]> countHumanSubmarineSunks = queryFactoryShots
            .create("SELECT COUNT(s.userId) FROM com.redhat.Shot s WHERE s.human=true AND s.shotType='SUNK' and s.shipType='SUBMARINE'");

      Query<Object[]> countAiSubmarineSunks = queryFactoryShots
            .create("SELECT COUNT(s.userId) FROM com.redhat.Shot s WHERE s.human=false AND s.shotType='SUNK' and s.shipType='SUBMARINE'");


      long humanActive = Long.valueOf(countActiveHumanPlayers.execute().list().get(0)[0].toString());
      long aiActive = Long.valueOf(countActiveAIPlayers.execute().list().get(0)[0].toString());
      long humanHits = Long.valueOf(countHumanHits.execute().list().get(0)[0].toString());
      long aiHits = Long.valueOf(countAiHits.execute().list().get(0)[0].toString());
      long humanMisses = Long.valueOf(countHumanMisses.execute().list().get(0)[0].toString());
      long aiMisses = Long.valueOf(countAiMisses.execute().list().get(0)[0].toString());
      long humanSunks = Long.valueOf(countHumanSunks.execute().list().get(0)[0].toString());
      long aiSunks = Long.valueOf(countAiSunks.execute().list().get(0)[0].toString());
      long humanCarrierSunks = Long.valueOf(countHumanCarrierSunks.execute().list().get(0)[0].toString());
      long aiCarrierSunks = Long.valueOf(countAiCarrierSunks.execute().list().get(0)[0].toString());
      long humanSubmarineSunks = Long.valueOf(countHumanSubmarineSunks.execute().list().get(0)[0].toString());
      long aiSubmarineSunks = Long.valueOf(countAiSubmarineSunks.execute().list().get(0)[0].toString());

      stats.put("human-active", humanActive);
      stats.put("human-hits", humanHits);
      stats.put("human-misses", humanMisses);
      stats.put("human-sunks", humanSunks);
      stats.put("human-submarine-sunks", humanSubmarineSunks);
      stats.put("human-carrier-sunks", humanCarrierSunks);
      stats.put("ai-active", aiActive);
      stats.put("ai-hits", aiHits);
      stats.put("ai-misses", aiMisses);
      stats.put("ai-sunks", aiSunks);
      stats.put("ai-submarine-sunks", aiSubmarineSunks);
      stats.put("ai-carrier-sunks",aiCarrierSunks);
   }

   private boolean checkAvailabilityOfCaches() {
      if(!remoteCacheManager.getCacheNames().contains(PlayerScore.PLAYERS_SCORES)) {
         LOGGER.warn(String.format("%s cache does not exit", PlayerScore.PLAYERS_SCORES));
         return true;
      }

      if(!remoteCacheManager.getCacheNames().contains(Shot.PLAYERS_SHOTS)) {
         LOGGER.warn(String.format("%s cache does not exit", Shot.PLAYERS_SHOTS));
         return true;
      }

      if(playersScores == null) {
         playersScores = remoteCacheManager.getCache(PlayerScore.PLAYERS_SCORES);
      }
      if(shots == null) {
         shots = remoteCacheManager.getCache(Shot.PLAYERS_SHOTS);
      }
      return false;
   }

   @Scheduled(every = "1s")
   public void broadcast() {
      sessions.values().forEach(s -> {
         s.getAsyncRemote().sendText(JsonObject.mapFrom(stats).toString(), result -> {
            if (result.getException() != null) {
               LOGGER.error("Unable to send message", result.getException());
            }
         });
      });
   }

   @OnClose
   public void onClose(Session session) {
      sessions.remove(session.getId());
      LOGGER.info("Leaderboard Service session has been closed");
   }

   @OnError
   public void onError(Session session, Throwable throwable) {
      sessions.remove(session.getId());
      LOGGER.error("Leaderboard Service session error", throwable);
   }
}
