package com.redhat.model;

import org.infinispan.protostream.annotations.ProtoFactory;
import org.infinispan.protostream.annotations.ProtoField;

import java.util.Objects;

public class Attack {
   String type;

   @ProtoFactory
   public Attack(String type) {
      this.type = type;
   }

   @ProtoField(number = 1)
   public String getType() {
      return type;
   }

   public void setType(String type) {
      this.type = type;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o)
         return true;
      if (o == null || getClass() != o.getClass())
         return false;
      Attack attack = (Attack) o;
      return Objects.equals(type, attack.type);
   }

   @Override
   public int hashCode() {
      return Objects.hash(type);
   }

   @Override
   public String toString() {
      return "Attack{" + "type='" + type + '\'' + '}';
   }
}
