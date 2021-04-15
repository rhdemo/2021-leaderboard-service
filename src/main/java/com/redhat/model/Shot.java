package com.redhat.model;

import org.infinispan.protostream.annotations.ProtoDoc;
import org.infinispan.protostream.annotations.ProtoFactory;
import org.infinispan.protostream.annotations.ProtoField;

import java.util.Objects;

@ProtoDoc("@Indexed")
public class Shot {
   private String userId;
   private String matchId;
   private String gameId;
   private Boolean human;
   private Long timestamp;
   private ShotType shotType;
   private ShipType shipType;

   public static final String PLAYERS_SHOTS = "players-shots";

   @ProtoFactory
   public Shot(String userId, String matchId, String gameId, Boolean human, Long timestamp, ShotType shotType,
               ShipType shipType) {
      this.userId = userId;
      this.matchId = matchId;
      this.gameId = gameId;
      this.human = human;
      this.timestamp = timestamp;
      this.shotType = shotType;
      this.shipType = shipType;
   }

   @ProtoField(number = 1, required = true)
   @ProtoDoc("@Field(index=Index.YES, analyze = Analyze.NO, store = Store.YES)")
   public String getUserId() {
      return userId;
   }

   public void setUserId(String userId) {
      this.userId = userId;
   }

   @ProtoField(number = 2, required = true)
   public String getMatchId() {
      return matchId;
   }

   public void setMatchId(String matchId) {
      this.matchId = matchId;
   }

   @ProtoField(number = 3)
   @ProtoDoc("@Field(index=Index.YES, analyze = Analyze.NO, store = Store.YES)")
   public String getGameId() {
      return gameId;
   }

   public void setGameId(String gameId) {
      this.gameId = gameId;
   }

   @ProtoField(number = 4)
   @ProtoDoc("@Field(index=Index.YES, analyze = Analyze.NO, store = Store.YES)")
   public Boolean isHuman() {
      return human;
   }

   public void setHuman(Boolean human) {
      this.human = human;
   }

   @ProtoField(number = 5)
   @ProtoDoc("@Field(index=Index.YES, analyze = Analyze.NO, store = Store.YES)")
   public Long getTimestamp() {
      return timestamp;
   }

   public void setTimestamp(Long timestamp) {
      this.timestamp = timestamp;
   }

   @ProtoField(number = 6)
   @ProtoDoc("@Field(index=Index.YES, analyze = Analyze.NO, store = Store.YES)")
   public ShotType getShotType() {
      return shotType;
   }

   public void setShotType(ShotType shotType) {
      this.shotType = shotType;
   }

   @ProtoField(number = 7)
   @ProtoDoc("@Field(index=Index.YES, analyze = Analyze.NO, store = Store.YES)")
   public ShipType getShipType() {
      return shipType;
   }

   public void setShipType(ShipType shipType) {
      this.shipType = shipType;
   }

   @Override
   public String toString() {
      return "Shot{" + "userId='" + userId + '\'' + ", matchId='" + matchId + '\'' + ", gameId='" + gameId + '\''
            + ", human=" + human + ", timestamp=" + timestamp + ", shotType=" + shotType + ", shipType=" + shipType
            + '}';
   }

   @Override
   public boolean equals(Object o) {
      if (this == o)
         return true;
      if (o == null || getClass() != o.getClass())
         return false;
      Shot shot = (Shot) o;
      return human == shot.human && timestamp == shot.timestamp && Objects.equals(userId, shot.userId) && Objects
            .equals(matchId, shot.matchId) && Objects.equals(gameId, shot.gameId) && shotType == shot.shotType
            && shipType == shot.shipType;
   }

   @Override
   public int hashCode() {
      return Objects.hash(userId, matchId, gameId, human, timestamp, shotType, shipType);
   }
}
