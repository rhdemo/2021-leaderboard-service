package com.redhat.service.scoring;

import java.util.Objects;

public class ScoringDelta {
   private String matchId;
   private String userId;
   private Integer delta;
   private Long timestamp;

   @Override
   public String toString() {
      return "ScoringDelta{" + "matchId='" + matchId + '\'' + ", userId='" + userId + '\'' + ", delta=" + delta
            + ", timestamp=" + timestamp + '}';
   }

   @Override
   public boolean equals(Object o) {
      if (this == o)
         return true;
      if (o == null || getClass() != o.getClass())
         return false;
      ScoringDelta that = (ScoringDelta) o;
      return Objects.equals(matchId, that.matchId) && Objects.equals(userId, that.userId) && Objects
            .equals(delta, that.delta) && Objects.equals(timestamp, that.timestamp);
   }

   @Override
   public int hashCode() {
      return Objects.hash(matchId, userId, delta, timestamp);
   }

   public String getMatchId() {
      return matchId;
   }

   public void setMatchId(String matchId) {
      this.matchId = matchId;
   }

   public String getUserId() {
      return userId;
   }

   public void setUserId(String userId) {
      this.userId = userId;
   }

   public Integer getDelta() {
      return delta;
   }

   public void setDelta(Integer delta) {
      this.delta = delta;
   }

   public Long getTimestamp() {
      return timestamp;
   }

   public void setTimestamp(Long timestamp) {
      this.timestamp = timestamp;
   }
}
