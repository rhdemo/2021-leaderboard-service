package com.redhat.model;

import org.infinispan.protostream.SerializationContextInitializer;
import org.infinispan.protostream.annotations.AutoProtoSchemaBuilder;

@AutoProtoSchemaBuilder(schemaPackageName = "com.redhat",
      includeClasses = { PlayerScore.class, Shot.class, ShotType.class, ShipType.class, GameStatus.class})
public interface LeaderboardServiceContextInitializer extends SerializationContextInitializer {
}
