package com.redhat.model;

import org.infinispan.protostream.annotations.ProtoEnumValue;

public enum GameStatus {
   @ProtoEnumValue(number = 1)
   PLAYING,
   @ProtoEnumValue(number = 2)
   WIN,
   @ProtoEnumValue(number = 3)
   LOSS
}
