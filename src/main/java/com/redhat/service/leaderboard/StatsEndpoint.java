package com.redhat.service.leaderboard;

import com.redhat.model.ConnectedPlayer;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.annotation.ClientCacheEntryCreated;
import org.infinispan.client.hotrod.annotation.ClientCacheEntryRemoved;
import org.infinispan.client.hotrod.annotation.ClientListener;
import org.infinispan.client.hotrod.event.ClientCacheEntryCreatedEvent;
import org.infinispan.client.hotrod.event.ClientCacheEntryRemovedEvent;
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
import java.util.concurrent.atomic.AtomicInteger;
import static com.redhat.model.ConnectedPlayer.CONNECTED_PLAYERS;

@ServerEndpoint("/stats")
@ApplicationScoped
public class StatsEndpoint {
   private static final Logger LOGGER = LoggerFactory.getLogger(StatsEndpoint.class.getName());

   private Map<String, Session> sessions = new ConcurrentHashMap<>();
   private AtomicInteger connectedUsersCount;
   private RemoteCache<String, ConnectedPlayer> connectedPlayers;

   @Inject
   public StatsEndpoint(RemoteCacheManager remoteCacheManager) {
      // TODO: use indexed cache to perform better
      connectedPlayers = remoteCacheManager.getCache(CONNECTED_PLAYERS);
      ConnectedPlayersListener listener = new ConnectedPlayersListener();
      connectedUsersCount = new AtomicInteger(connectedPlayers.size());
      connectedPlayers.addClientListener(listener);
   }

   @OnOpen
   public void onOpen(Session session) {
      sessions.put(session.getId(), session);
      LOGGER.info("Leaderboard service socket opened");
      session.getAsyncRemote().sendText(Integer.toString(connectedUsersCount.get()));
   }

   @ClientListener
   public class ConnectedPlayersListener {
      @ClientCacheEntryCreated
      public void entryCreated(ClientCacheEntryCreatedEvent<String> event) {
         broadcast(connectedUsersCount.incrementAndGet());
      }

      @ClientCacheEntryRemoved
      public void entryRemoved(ClientCacheEntryRemovedEvent<String> event) {
         broadcast(connectedUsersCount.decrementAndGet());
      }
   }

   private void broadcast(int currentCount) {
      sessions.values().forEach(s -> {
         s.getAsyncRemote().sendText(Integer.toString(currentCount), result ->  {
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
