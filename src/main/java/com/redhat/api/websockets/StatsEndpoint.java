package com.redhat.api.websockets;

import com.redhat.model.GameStatus;
import com.redhat.model.PlayerScore;
import com.redhat.model.ShipType;
import com.redhat.model.Shot;
import com.redhat.model.ShotType;
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
   RemoteCache<String, String> game;
   RemoteCacheManager remoteCacheManager;
   QueryFactory queryFactoryPlayerScores;
   QueryFactory queryFactoryShots;

   @Inject
   public StatsEndpoint(RemoteCacheManager remoteCacheManager) {
      this.remoteCacheManager = remoteCacheManager;
      stats.put("human-active", 0L);
      stats.put("human-win", 0L);
      stats.put("human-loss", 0L);
      stats.put("human-hits", 0L);
      stats.put("human-misses", 0L);
      stats.put("human-sunks", 0L);
      stats.put("human-submarine-sunks", 0L);
      stats.put("human-carrier-sunks", 0L);
      stats.put("human-battleship-sunks", 0L);
      stats.put("human-destroyer-sunks", 0L);
      stats.put("human-bonus", 0L);
      stats.put("ai-active", 0L);
      stats.put("ai-win", 0L);
      stats.put("ai-loss", 0L);
      stats.put("ai-hits", 0L);
      stats.put("ai-misses", 0L);
      stats.put("ai-sunks", 0L);
      stats.put("ai-submarine-sunks", 0L);
      stats.put("ai-carrier-sunks", 0L);
      stats.put("ai-battleship-sunks", 0L);
      stats.put("ai-destroyer-sunks", 0L);
      stats.put("ai-bonus", 0L);
   }

   @OnOpen
   public void onOpen(Session session) {
      sessions.put(session.getId(), session);
      LOGGER.info("Leaderboard service socket opened");
   }

   @Scheduled(every = "1s")
   public void calculateStats() {
      if (sessions.isEmpty()) {
         return;
      }
      if (checkAvailabilityOfCaches())
         return;

      String currentGame = game.get("current-game");
      String gameId = new JsonObject(currentGame).getString("uuid");

      stats.put("human-active", calculateStat(gameStatusCountQuery(true, GameStatus.PLAYING, gameId)));
      stats.put("human-win", calculateStat(gameStatusCountQuery(true, GameStatus.WIN, gameId)));
      stats.put("human-loss", calculateStat(gameStatusCountQuery(true, GameStatus.LOSS, gameId)));
      stats.put("human-hits", calculateStat(shotsTypesCountQuery(true, ShotType.HIT, gameId)));
      stats.put("human-misses", calculateStat(shotsTypesCountQuery(true, ShotType.MISS, gameId)));
      stats.put("human-sunks", calculateStat(shotsTypesCountQuery(true, ShotType.SUNK, gameId)));
      stats.put("human-submarine-sunks", calculateStat(shipTypeCountQuery(true, ShipType.SUBMARINE, gameId)));
      stats.put("human-carrier-sunks", calculateStat(shipTypeCountQuery(true, ShipType.CARRIER, gameId)));
      stats.put("human-battleship-sunks", calculateStat(shipTypeCountQuery(true, ShipType.BATTLESHIP, gameId)));
      stats.put("human-destroyer-sunks", calculateStat(shipTypeCountQuery(true, ShipType.DESTROYER, gameId)));
      stats.put("human-bonus", calculateStat(bonusSumQuery(true, gameId)));
      stats.put("ai-active", calculateStat(gameStatusCountQuery(false, GameStatus.PLAYING, gameId)));
      stats.put("ai-win", calculateStat(gameStatusCountQuery(false, GameStatus.WIN, gameId)));
      stats.put("ai-loss", calculateStat(gameStatusCountQuery(false, GameStatus.LOSS, gameId)));
      stats.put("ai-hits", calculateStat(shotsTypesCountQuery(false, ShotType.HIT, gameId)));
      stats.put("ai-misses", calculateStat(shotsTypesCountQuery(false, ShotType.MISS, gameId)));
      stats.put("ai-sunks", calculateStat(shotsTypesCountQuery(false, ShotType.SUNK, gameId)));
      stats.put("ai-submarine-sunks", calculateStat(shipTypeCountQuery(false, ShipType.SUBMARINE, gameId)));
      stats.put("ai-carrier-sunks", calculateStat(shipTypeCountQuery(false, ShipType.CARRIER, gameId)));
      stats.put("ai-battleship-sunks", calculateStat(shipTypeCountQuery(false, ShipType.BATTLESHIP, gameId)));
      stats.put("ai-destroyer-sunks", calculateStat(shipTypeCountQuery(false, ShipType.DESTROYER, gameId)));
      stats.put("ai-bonus", calculateStat(bonusSumQuery(false, gameId)));
   }

   private Query<Object[]> gameStatusCountQuery(boolean human, GameStatus gameStatus, String gameId) {
      return queryFactoryPlayerScores.create(
            "SELECT COUNT(p.userId) FROM com.redhat.PlayerScore p WHERE p.human=" + human + " AND p.gameStatus='"
                  + gameStatus.name() + "' AND p.gameId='" + gameId + "'");
   }

   private Query<Object[]> bonusSumQuery(boolean human, String gameId) {
      return queryFactoryPlayerScores.create(
            "SELECT SUM(p.bonus) FROM com.redhat.PlayerScore p WHERE p.human=" + human + " AND p.gameId='" + gameId + "'");
   }

   private Query<Object[]> shotsTypesCountQuery(boolean human, ShotType shotType, String gameId) {
      return queryFactoryShots.create(
            "SELECT COUNT(s.userId) FROM com.redhat.Shot s WHERE s.human=" + human + " AND s.shotType='" + shotType
                  .name() + "' AND s.gameId='" + gameId + "'");
   }

   private Query<Object[]> shipTypeCountQuery(boolean human, ShipType shipType, String gameId) {
      return queryFactoryShots.create("SELECT COUNT(s.userId) FROM com.redhat.Shot s WHERE s.human=" + human
            + " AND s.shotType='SUNK' AND s.shipType='" + shipType.name() + "' AND s.gameId='" + gameId + "'");
   }

   private Long calculateStat(Query<Object[]> statsQuery) {
      return Long.valueOf(statsQuery.execute().list().get(0)[0].toString());
   }

   private boolean checkAvailabilityOfCaches() {
      if (!remoteCacheManager.getCacheNames().contains(PlayerScore.PLAYERS_SCORES)) {
         LOGGER.warn(String.format("%s cache does not exit", PlayerScore.PLAYERS_SCORES));
         return true;
      }

      if (!remoteCacheManager.getCacheNames().contains(Shot.PLAYERS_SHOTS)) {
         LOGGER.warn(String.format("%s cache does not exit", Shot.PLAYERS_SHOTS));
         return true;
      }

      if (!remoteCacheManager.getCacheNames().contains("game")) {
         LOGGER.warn(String.format("%s cache does not exit", "game"));
         return true;
      }

      if (playersScores == null) {
         playersScores = remoteCacheManager.getCache(PlayerScore.PLAYERS_SCORES);
      }
      if (shots == null) {
         shots = remoteCacheManager.getCache(Shot.PLAYERS_SHOTS);
      }

      if (game == null) {
         game = remoteCacheManager.getCache("game");
      }

      queryFactoryPlayerScores = Search.getQueryFactory(playersScores);
      queryFactoryShots = Search.getQueryFactory(shots);

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
