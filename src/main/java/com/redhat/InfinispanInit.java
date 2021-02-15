package com.redhat;

import com.redhat.model.PlayerScore;
import com.redhat.model.Shot;
import io.quarkus.runtime.StartupEvent;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.commons.configuration.XMLStringConfiguration;
import org.infinispan.counter.api.CounterConfiguration;
import org.infinispan.counter.api.CounterManager;
import org.infinispan.counter.api.CounterType;

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
         if(!cacheManager.getCacheNames().contains(INDEXED_PROTOBUF)){
            cacheManager.administration()
                  .createTemplate(INDEXED_PROTOBUF, new XMLStringConfiguration(TEST_CACHE_XML_CONFIG));
         }
         cacheManager.administration().getOrCreateCache(PlayerScore.PLAYERS_SCORES, INDEXED_PROTOBUF);
         cacheManager.administration().getOrCreateCache(Shot.PLAYERS_SHOTS, "example.PROTOBUF_DIST");

         counterManager.defineCounter("human-hits", CounterConfiguration.builder(CounterType.UNBOUNDED_STRONG).build());
         counterManager.defineCounter("human-misses", CounterConfiguration.builder(CounterType.UNBOUNDED_STRONG).build());
         counterManager.defineCounter("human-sunks", CounterConfiguration.builder(CounterType.UNBOUNDED_STRONG).build());
         counterManager.defineCounter("human-carrier-sunks", CounterConfiguration.builder(CounterType.UNBOUNDED_STRONG).build());
         counterManager.defineCounter("human-submarine-sunks", CounterConfiguration.builder(CounterType.UNBOUNDED_STRONG).build());

         counterManager.defineCounter("ai-hits", CounterConfiguration.builder(CounterType.UNBOUNDED_STRONG).build());
         counterManager.defineCounter("ai-misses", CounterConfiguration.builder(CounterType.UNBOUNDED_STRONG).build());
         counterManager.defineCounter("ai-sunks", CounterConfiguration.builder(CounterType.UNBOUNDED_STRONG).build());
         counterManager.defineCounter("ai-carrier-sunks", CounterConfiguration.builder(CounterType.UNBOUNDED_STRONG).build());
         counterManager.defineCounter("ai-submarine-sunks", CounterConfiguration.builder(CounterType.UNBOUNDED_STRONG).build());
      }
   }

   private static final String TEST_CACHE_XML_CONFIG =
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
}
