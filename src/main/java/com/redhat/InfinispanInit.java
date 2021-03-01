package com.redhat;

import com.redhat.model.PlayerScore;
import com.redhat.model.Shot;
import io.quarkus.runtime.StartupEvent;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.commons.configuration.XMLStringConfiguration;
import org.infinispan.counter.api.CounterManager;

import javax.annotation.Priority;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

@ApplicationScoped
public class InfinispanInit {

   public static final String INDEXED_PROTOBUF = "indexed-protobuf";
   @Inject
   RemoteCacheManager cacheManager;

   @Inject
   CounterManager counterManager;

   @ConfigProperty(name = "configureInfinispan")
   Boolean configureInfinispan;

   void onStart(@Observes @Priority(value = 1) StartupEvent ev) {
      if(configureInfinispan) {
         cacheManager.administration().getOrCreateCache(PlayerScore.PLAYERS_SCORES, new XMLStringConfiguration(TEST_CACHE_PLAYER_CONFIG));
         cacheManager.administration()
               .getOrCreateCache(Shot.PLAYERS_SHOTS, new XMLStringConfiguration(TEST_CACHE_SHOTS_CONFIG));
      }
   }

   private static final String TEST_CACHE_PLAYER_CONFIG =
               "<infinispan><cache-container>" +
               "  <distributed-cache-configuration name=\"indexed-protobuf\" statistics=\"true\">" +
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
               "  </distributed-cache-configuration>" +
               "</cache-container></infinispan>";

   private static final String TEST_CACHE_SHOTS_CONFIG =
         "<infinispan><cache-container>" +
               "  <distributed-cache-configuration name=\"indexed-protobuf\" statistics=\"true\">" +
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
               "  </distributed-cache-configuration>" +
               "</cache-container></infinispan>";
}
