package com.redhat.api.websockets;

import com.redhat.model.PlayerScore;
import com.redhat.model.Shot;
import io.quarkus.scheduler.Scheduled;
import io.vertx.core.json.JsonObject;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.Search;
import org.infinispan.counter.api.CounterManager;
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
import java.util.Collection;
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

   @Inject
   CounterManager counterManager;

   @Inject
   RemoteCacheManager remoteCacheManager;

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

      // TODO: Decide with the Infinispan team if use counters or do queries for everything
      QueryFactory queryFactory = Search.getQueryFactory(playersScores);
      Query<Object[]> countActiveHumanPlayers = queryFactory
            .create("SELECT COUNT(p.userId) FROM com.redhat.PlayerScore p WHERE p.human=true AND p.gameStatus='PLAYING'");

      Query<Object[]> countActiveAIPlayers = queryFactory
            .create("SELECT COUNT(p.userId) FROM com.redhat.PlayerScore p WHERE p.human=false AND p.gameStatus='PLAYING'");

      long humanActive = Long.valueOf(countActiveHumanPlayers.execute().list().get(0)[0].toString());
      long aiActive = Long.valueOf(countActiveAIPlayers.execute().list().get(0)[0].toString());

      stats.put("human-active", humanActive);
      stats.put("ai-active", aiActive);

      Collection<String> counterNames = counterManager.getCounterNames();
      for (String name : counterNames) {
         counterManager.getStrongCounter(name).getValue().thenAccept(value -> {
            stats.put(name, value);
         });
      }
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
      if(playersScores == null) {
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
