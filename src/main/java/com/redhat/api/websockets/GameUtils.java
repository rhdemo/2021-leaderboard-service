package com.redhat.api.websockets;

import io.vertx.core.json.JsonObject;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class GameUtils {
   private static final Logger LOGGER = LoggerFactory.getLogger(GameUtils.class.getName());
   public static final String GAME_CACHE = "game";
   public static final String CURRENT_GAME_KEY = "current-game";

   @Inject
   RemoteCacheManager remoteCacheManager;

   RemoteCache<String, String> game;

   public class Game {
      private String id;
      private String state;
      public Game(String id, String state) {
         this.id = id;
         this.state = state;
      }

      public String getId() {
         return id;
      }

      public String getState() {
         return state;
      }
   }

   public boolean isGameNotReady() {
      if(game != null) {
         return false;
      }

      if(game == null && !remoteCacheManager.getCacheNames().contains(GAME_CACHE)) {
         LOGGER.warn(String.format("%s cache does not exit", GAME_CACHE));
         return true;
      }

      game = remoteCacheManager.getCache(GAME_CACHE);
      return game == null;
   }


   public GameUtils.Game getGameData() {
      Game emptyGame = new Game("", "");
      if (isGameNotReady()) {
         return emptyGame;
      }

      String currentGame = this.game.get(CURRENT_GAME_KEY);
      if (currentGame == null) {
         LOGGER.warn(String.format("%s cache does not contain %s", GAME_CACHE, CURRENT_GAME_KEY));
         return emptyGame;
      }

      JsonObject gameJson = new JsonObject(currentGame);
      String gameId = gameJson.getString("uuid");
      String gameState = gameJson.getString("state");
      return new GameUtils.Game(gameId, gameState);
   }
}
