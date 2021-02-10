package com.redhat.model;

import org.infinispan.protostream.SerializationContextInitializer;
import org.infinispan.protostream.annotations.AutoProtoSchemaBuilder;

@AutoProtoSchemaBuilder(schemaPackageName = "com.redhat",
      includeClasses = { ConnectedPlayer.class, Attack.class})
public interface LeaderboardServiceContextInitializer extends SerializationContextInitializer {
}
