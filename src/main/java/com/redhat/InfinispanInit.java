package com.redhat;

import com.redhat.model.GameStatus;
import com.redhat.model.PlayerScore;
import com.redhat.model.ShipType;
import com.redhat.model.Shot;
import com.redhat.model.ShotType;
import io.quarkus.runtime.StartupEvent;
import io.quarkus.scheduler.Scheduled;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.commons.configuration.XMLStringConfiguration;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@ApplicationScoped
public class InfinispanInit {

   @Inject
   RemoteCacheManager cacheManager;

   @ConfigProperty(name = "leaderboard.configure-infinispan")
   Boolean configureInfinispan;
   boolean init = false;
   List<Player> randomPlayers = new ArrayList<>();
   Random random = new Random();

   void onStart(@Observes StartupEvent ev) {
      if(configureInfinispan) {
         List<String> randomNames = Arrays.asList(
               "burr", "michael", "liam", "veronica", "maria", "kike", "ramon",
               "auri", "laura", "elaia", "oihana", "yago", "julia", "igor",
               "ela", "felix", "pekas", "solomo", "xala3pa", "diana", "jorge"
         );

         RemoteCache<String, PlayerScore> playerScores = cacheManager.administration()
               .getOrCreateCache(PlayerScore.PLAYERS_SCORES, new XMLStringConfiguration(TEST_CACHE_PLAYER_CONFIG));
         cacheManager.administration()
               .getOrCreateCache(Shot.PLAYERS_SHOTS, new XMLStringConfiguration(TEST_CACHE_SHOTS_CONFIG));

         for (String name: randomNames) {
            String matchId = UUID.randomUUID().toString();
            Player playerHuman = Player.create(name, matchId, true);
            Player playerAI = Player.create("ai-" + name, matchId, false);
            randomPlayers.add(playerHuman);
            randomPlayers.add(playerAI);
            playerScores.put(playerHuman.getPlayerScoreId(), playerHuman.toPlayerScore());
            playerScores.put(playerAI.getPlayerScoreId(), playerAI.toPlayerScore());
         }

         init = true;
      }
   }

   static class Player {
      String userId;
      String name;
      String matchId;
      String gameId;
      boolean human;

      public static Player create(String name, String matchId, boolean human) {
         Player player = new Player();
         player.gameId =  "d488f0e8-7ade-11eb-9439-0242ac130002";
         player.name = name;
         player.matchId = matchId;
         player.userId = UUID.randomUUID().toString();
         player.human = human;
         return player;
      }

      String getPlayerScoreId() {
         return gameId + "-" + matchId + "-" + userId;
      }

      PlayerScore toPlayerScore() {
         return new PlayerScore(userId, matchId, gameId, name, human, 0,
                     Instant.now().toEpochMilli(), GameStatus.PLAYING);
      }
   }

   @Scheduled(every = "0.2s")
   public void createData() {
      if(configureInfinispan && init) {
         RemoteCache<String, PlayerScore> playerScores = cacheManager.getCache(PlayerScore.PLAYERS_SCORES);
         Player player = randomPlayers.get(random.nextInt(randomPlayers.size()));
         PlayerScore playerScore = playerScores.get(player.getPlayerScoreId());
         playerScore.setScore(random.nextInt(25));
         playerScores.put(player.getPlayerScoreId(), playerScore);
         RemoteCache<String, Shot> shots = cacheManager.getCache(Shot.PLAYERS_SHOTS);

         int shotType = random.nextInt(3);
         int shipType = random.nextInt(2);

         Shot shot = new Shot(player.userId, player.matchId, player.gameId, player.human, Instant.EPOCH.toEpochMilli(),
               ShotType.values()[shotType], ShipType.values()[shipType]);

         shots.put(UUID.randomUUID().toString(), shot);
      }
   }
   private static final String TEST_CACHE_PLAYER_CONFIG =
               "<infinispan><cache-container>" +
               "  <distributed-cache name=\"players-scores\" statistics=\"true\">" +
               "    <memory storage=\"HEAP\"/>" +
               "    <encoding>" +
               "        <key media-type=\"application/x-protostream\"/>" +
               "        <value media-type=\"application/x-protostream\"/>" +
               "    </encoding>" +
               "    <indexing enabled=\"true\">" +
               "        <indexed-entities>" +
               "           <indexed-entity> com.redhat.PlayerScore </indexed-entity>" +
               "        </indexed-entities>" +
               "    </indexing>" +
               "  </distributed-cache>" +
               "</cache-container></infinispan>";

   private static final String TEST_CACHE_SHOTS_CONFIG =
         "<infinispan><cache-container>" +
               "  <distributed-cache name=\"players-shots\" statistics=\"true\">" +
               "    <memory storage=\"HEAP\"/>" +
               "    <encoding>" +
               "        <key media-type=\"application/x-protostream\"/>" +
               "        <value media-type=\"application/x-protostream\"/>" +
               "    </encoding>" +
               "    <indexing enabled=\"true\">" +
               "        <indexed-entities>" +
               "           <indexed-entity> com.redhat.Shot</indexed-entity>" +
               "        </indexed-entities>" +
               "    </indexing>" +
               "  </distributed-cache>" +
               "</cache-container></infinispan>";
}
