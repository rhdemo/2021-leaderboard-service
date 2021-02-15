package com.redhat.model;

import org.infinispan.protostream.annotations.ProtoEnumValue;

public enum  ShotType {
   @ProtoEnumValue(number = 1)
   HIT,
   @ProtoEnumValue(number = 2)
   MISS,
   @ProtoEnumValue(number = 3)
   SUNK
}
