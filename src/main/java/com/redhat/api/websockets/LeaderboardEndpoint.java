package com.redhat.api.websockets;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.redhat.model.PlayerScore;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint("/leaderboard")
@ApplicationScoped
public class LeaderboardEndpoint {
   private static final Logger LOGGER = LoggerFactory.getLogger(LeaderboardEndpoint.class.getName());

   private Map<String, Session> sessions = new ConcurrentHashMap<>();

   @Inject
   RemoteCacheManager remoteCacheManager;

   RemoteCache<String, PlayerScore> playersScores;
   Query topTenQuery;

   @OnOpen
   public void onOpen(Session session) {
      sessions.put(session.getId(), session);
      LOGGER.info("Leaderboard service socket opened");
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

   @Scheduled(every = "0.5s")
   public void broadcast() throws JsonProcessingException {
      if(sessions.isEmpty()) {
         return;
      }

      if(!remoteCacheManager.getCacheNames().contains(PlayerScore.PLAYERS_SCORES)) {
         LOGGER.warn(String.format("%s cache does not exit", PlayerScore.PLAYERS_SCORES));
         return;
      }

      if(playersScores == null) {
         playersScores = remoteCacheManager.getCache(PlayerScore.PLAYERS_SCORES);
      }

      if(topTenQuery == null) {
         QueryFactory queryFactory = Search.getQueryFactory(playersScores);
         // TODO: filter by gameID if we have more than one game ?
         topTenQuery = queryFactory.create("from com.redhat.PlayerScore p WHERE p.human=true ORDER BY p.score DESC, p.timestamp ASC").maxResults(10);
      }

      List<PlayerScore> topTen = topTenQuery.execute().list();

      List<String> topTenJson = new ArrayList<>();
      for(PlayerScore p : topTen) {
         JsonObject object = new JsonObject();
         object.put("userId", p.getUserId());
         object.put("matchId", p.getMatchId());
         object.put("gameId", p.getGameId());
         object.put("human", p.isHuman());
         object.put("userName", p.getUsername());
         object.put("score", p.getScore());
         object.put("timestamp", p.getTimestamp());
         object.put("gameStatus", p.getGameStatus());
         topTenJson.add(object.toString());
      }

      sessions.values().forEach(s -> s.getAsyncRemote().sendObject(topTenJson.toString(), result -> {
         if (result.getException() != null) {
            LOGGER.error("Leaderboard service got interrupted", result.getException());
         }
      }));
   }
}
