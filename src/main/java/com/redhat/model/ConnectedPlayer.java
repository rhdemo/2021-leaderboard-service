package com.redhat.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.infinispan.protostream.annotations.ProtoDoc;
import org.infinispan.protostream.annotations.ProtoFactory;
import org.infinispan.protostream.annotations.ProtoField;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
@ProtoDoc("@Indexed")
public class ConnectedPlayer implements Comparable {

   public static final String CONNECTED_PLAYERS = "players";

   String uuid;
   String userName;
   String match;
   List<Attack> attacks;
   Integer score;
   Long timestamp;

   @ProtoFactory
   public ConnectedPlayer(String uuid, String userName, String match, List<Attack> attacks, Integer score, Long timestamp) {
      this.uuid = uuid;
      this.userName = userName;
      this.match = match;
      this.attacks = attacks;
      this.score = score;
      this.timestamp = timestamp;
   }

   @ProtoField(number = 1)
   public String getUuid() {
      return uuid;
   }

   public void setUuid(String uuid) {
      this.uuid = uuid;
   }

   @ProtoField(number = 2)
   public String getUserName() {
      return userName;
   }

   public void setUserName(String userName) {
      this.userName = userName;
   }

   @ProtoField(number = 3)
   public String getMatch() {
      return match;
   }

   public void setMatch(String match) {
      this.match = match;
   }

   @ProtoField(number = 4, collectionImplementation = ArrayList.class)
   public List<Attack> getAttacks() {
      return attacks;
   }

   public void setAttacks(List<Attack> attacks) {
      this.attacks = attacks;
   }

   @ProtoField(number = 5)
   @ProtoDoc("@Field(index=Index.YES, analyze = Analyze.NO, store = Store.NO)")
   public Integer getScore() {
      return score;
   }

   public void setScore(Integer score) {
      this.score = score;
   }

   @ProtoField(number = 6)
   public Long getTimestamp() {
      return timestamp;
   }

   public void setTimestamp(Long timestamp) {
      this.timestamp = timestamp;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o)
         return true;
      if (o == null || getClass() != o.getClass())
         return false;
      ConnectedPlayer that = (ConnectedPlayer) o;
      return Objects.equals(uuid, that.uuid) && Objects.equals(userName, that.userName) && Objects
            .equals(match, that.match);
   }

   @Override
   public int compareTo(Object o) {
      ConnectedPlayer cp = (ConnectedPlayer)o;
      if(this.getScore() == cp.getScore()) {
         if (this.getTimestamp() == null || cp.getTimestamp() == null) {
            return 0;
         }
         return this.getTimestamp().compareTo(cp.getTimestamp());
      }
      return this.getScore().compareTo(cp.getScore());
   }

   @Override
   public int hashCode() {
      return Objects.hash(uuid, userName, match);
   }

   @Override
   public String toString() {
      return "ConnectedPlayer{" + "uuid='" + uuid + '\'' + ", userName='" + userName + '\'' + ", match='" + match + '\''
            + ", attacks=" + attacks + ", score=" + score + ", timestamp=" + timestamp + '}';
   }
}
