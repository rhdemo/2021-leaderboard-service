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
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint("/stats")
@ApplicationScoped
public class StatsEndpoint {
   private static final Logger LOGGER = LoggerFactory.getLogger(StatsEndpoint.class.getName());

   private Map<String, Session> sessions = new ConcurrentHashMap<>();
   private Map<String, Object> stats = new ConcurrentHashMap<>();

   RemoteCache<String, PlayerScore> playersScores;
   RemoteCache<String, Shot> shots;
   RemoteCache<String, String> game;
   RemoteCacheManager remoteCacheManager;
   GameUtils gameUtils;
   QueryFactory queryFactoryPlayerScores;
   QueryFactory queryFactoryShots;

   @Inject
   public StatsEndpoint(RemoteCacheManager remoteCacheManager, GameUtils gameUtils) {
      this.remoteCacheManager = remoteCacheManager;
      this.gameUtils = gameUtils;
      resetStats();
   }

   @OnOpen
   public void onOpen(Session session) {
      sessions.put(session.getId(), session);
      LOGGER.info("Stats service socket opened");
      calculateStats();
   }



   @Scheduled(every = "{stats.schedule}")
   public void calculateStats() {
      if (sessions.isEmpty()) {
         return;
      }
      if (cachesNotReady())
         return;

      GameUtils.Game gameData = gameUtils.getGameData();

      Long humanHits = calculateStat(shotsTypesCountQuery(true, ShotType.HIT, gameData.getId()));
      Long humanMisses = calculateStat(shotsTypesCountQuery(true, ShotType.MISS, gameData.getId()));
      Long humanSunks = calculateStat(shotsTypesCountQuery(true, ShotType.SUNK, gameData.getId()));

      Long aiHits = calculateStat(shotsTypesCountQuery(false, ShotType.HIT, gameData.getId()));
      Long aiMisses = calculateStat(shotsTypesCountQuery(false, ShotType.MISS, gameData.getId()));
      Long aiSunks = calculateStat(shotsTypesCountQuery(false, ShotType.SUNK, gameData.getId()));

      Long humanWins = calculateStat(gameStatusCountQuery(true, GameStatus.WIN, gameData.getId()));
      Long humanLoss = calculateStat(gameStatusCountQuery(true, GameStatus.LOSS, gameData.getId()));

      stats.put("human-shots", calculateStat(gameShotsCountQuery(true, gameData.getId())));
      stats.put("human-active", calculateStat(gameStatusCountQuery(true, GameStatus.PLAYING, gameData.getId())));
      stats.put("human-win", humanWins);
      stats.put("human-loss", humanLoss);
      stats.put("human-hits", humanHits);
      stats.put("human-misses", humanMisses);
      stats.put("human-sunks", humanSunks);
      stats.put("human-submarine-sunks", calculateStat(shipTypeCountQuery(true, ShipType.SUBMARINE, gameData.getId())));
      stats.put("human-carrier-sunks", calculateStat(shipTypeCountQuery(true, ShipType.CARRIER, gameData.getId())));
      stats.put("human-battleship-sunks", calculateStat(shipTypeCountQuery(true, ShipType.BATTLESHIP, gameData.getId())));
      stats.put("human-destroyer-sunks", calculateStat(shipTypeCountQuery(true, ShipType.DESTROYER, gameData.getId())));
      stats.put("human-bonus", calculateBonus(bonusSumQuery(true, gameData.getId())));

      stats.put("ai-shots", calculateStat(gameShotsCountQuery(false, gameData.getId())));
      stats.put("ai-active", calculateStat(gameStatusCountQuery(false, GameStatus.PLAYING, gameData.getId())));
      stats.put("ai-win", calculateStat(gameStatusCountQuery(false, GameStatus.WIN, gameData.getId())));
      stats.put("ai-loss", calculateStat(gameStatusCountQuery(false, GameStatus.LOSS, gameData.getId())));
      stats.put("ai-hits", aiHits);
      stats.put("ai-misses", aiMisses);
      stats.put("ai-sunks", aiSunks);
      stats.put("ai-submarine-sunks", calculateStat(shipTypeCountQuery(false, ShipType.SUBMARINE, gameData.getId())));
      stats.put("ai-carrier-sunks", calculateStat(shipTypeCountQuery(false, ShipType.CARRIER, gameData.getId())));
      stats.put("ai-battleship-sunks", calculateStat(shipTypeCountQuery(false, ShipType.BATTLESHIP, gameData.getId())));
      stats.put("ai-destroyer-sunks", calculateStat(shipTypeCountQuery(false, ShipType.DESTROYER, gameData.getId())));
      stats.put("ai-bonus", calculateBonus(bonusSumQuery(false, gameData.getId())));

      stats.put("total-hits", humanHits + aiHits);
      stats.put("total-misses", humanMisses + aiMisses);
      stats.put("total-sunk", humanSunks + aiSunks);

      playersScores.sizeAsync().thenApply(s -> stats.put("games-played", Double.valueOf(Math.ceil(s / 2)).longValue()));
      stats.put("games-complete", humanWins + humanLoss);
      stats.put("game-state", gameData.getState());
      stats.put("game-id", gameData.getId());
   }



   private void resetStats() {
      GameUtils.Game gameData = gameUtils.getGameData();
      stats.put("game-state", gameData.getState());
      stats.put("game-id", gameData.getId());
      stats.put("games-played", 0L);
      stats.put("games-complete", 0L);
      stats.put("human-shots", 0L);
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

      stats.put("ai-shots", 0L);
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
      stats.put("total-hits", 0L);
      stats.put("total-misses", 0L);
      stats.put("total-sunk", 0L);
   }

   private Query<Object[]> gameShotsCountQuery(boolean human, String gameId) {
      return queryFactoryShots.create(
            "SELECT s.userId FROM com.redhat.Shot s WHERE s.human=" + human + " AND s.gameId='" + gameId + "'");
   }

   private Query<Object[]> gameStatusCountQuery(boolean human, GameStatus gameStatus, String gameId) {
      return queryFactoryPlayerScores.create(
            "SELECT p.userId FROM com.redhat.PlayerScore p WHERE p.human=" + human + " AND p.gameStatus='"
                  + gameStatus.name() + "' AND p.gameId='" + gameId + "'");
   }

   private Query<Object[]> bonusSumQuery(boolean human, String gameId) {
      return queryFactoryPlayerScores.create(
            "SELECT p.bonus FROM com.redhat.PlayerScore p WHERE p.bonus > 0 AND p.human=" + human + " AND p.gameId='" + gameId + "'");
   }

   private Query<Object[]> shotsTypesCountQuery(boolean human, ShotType shotType, String gameId) {
      return queryFactoryShots.create(
            "SELECT s.userId FROM com.redhat.Shot s WHERE s.human=" + human + " AND s.shotType='" + shotType
                  .name() + "' AND s.gameId='" + gameId + "'");
   }

   private Query<Object[]> shipTypeCountQuery(boolean human, ShipType shipType, String gameId) {
      return queryFactoryShots.create("SELECT s.userId FROM com.redhat.Shot s WHERE s.human=" + human
            + " AND s.shotType='SUNK' AND s.shipType='" + shipType.name() + "' AND s.gameId='" + gameId + "'");
   }

   private Long calculateStat(Query<Object[]> statsQuery) {
      return Long.valueOf(statsQuery.maxResults(1).execute().hitCount().getAsLong());
   }

   private Long calculateBonus(Query<Object[]> bonusQuery) {
      List<Object[]> result = bonusQuery.maxResults(Integer.MAX_VALUE).execute().list();
      if (result != null && result.size() == 0) {
         return 0L;
      }

      return result.stream().mapToLong((r) ->  (int) r[0]).sum();
   }

   private boolean cachesNotReady() {
      if(gameUtils.isGameNotReady()) {
         return true;
      }

      if (!remoteCacheManager.getCacheNames().contains(PlayerScore.PLAYERS_SCORES)) {
         LOGGER.warn(String.format("%s cache does not exit", PlayerScore.PLAYERS_SCORES));
         return true;
      }

      if (!remoteCacheManager.getCacheNames().contains(Shot.PLAYERS_SHOTS)) {
         LOGGER.warn(String.format("%s cache does not exit", Shot.PLAYERS_SHOTS));
         return true;
      }

      if (playersScores == null) {
         playersScores = remoteCacheManager.getCache(PlayerScore.PLAYERS_SCORES);
      }
      if (shots == null) {
         shots = remoteCacheManager.getCache(Shot.PLAYERS_SHOTS);
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
      LOGGER.info("Stats Service session has been closed");
   }

   @OnError
   public void onError(Session session, Throwable throwable) {
      sessions.remove(session.getId());
      LOGGER.error("Stats Service session error", throwable);
   }
}
