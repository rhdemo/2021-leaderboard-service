package com.redhat.model;

import org.infinispan.protostream.annotations.ProtoEnumValue;

public enum ShipType {
   @ProtoEnumValue(number = 1)
   CARRIER,
   @ProtoEnumValue(number = 2)
   SUBMARINE,
}
