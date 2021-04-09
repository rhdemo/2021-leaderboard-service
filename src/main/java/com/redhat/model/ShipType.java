package com.redhat.model;

import org.infinispan.protostream.annotations.ProtoEnumValue;

public enum ShipType {
   @ProtoEnumValue(number = 1)
   CARRIER,
   @ProtoEnumValue(number = 2)
   SUBMARINE,
   @ProtoEnumValue(number = 3)
   BATTLESHIP,
   @ProtoEnumValue(number = 4)
   DESTROYER
}
