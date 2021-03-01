# 2021-leaderboard-service

Leaderboard Service, baked with Quarkus and Infinispan

Run Infinispan with Docker

`docker run -v $(pwd):/user-config  -p 11222:11222 -e USER="admin" -e PASS="pass" infinispan/server:12.0.1.Final`

## Web Sockets

### Leaderboard

`ws://localhost:8080/leaderboard`

Payload 

```json
[
  {
    "userId":"u6",
    "matchId":"m1",
    "gameId":"g1",
    "human":true,
    "username": "Michael",
    "score":12,
    "timestamp":9090898,
    "gameStatus":"PLAYING"
  }, 
  {
    "userId":"u5",
    "matchId":"m1",
    "gameId":"g1",
    "human":true,
    "username": "Jennifer",
    "score":12,
    "timestamp":9090898,
    "gameStatus":"PLAYING"
  }
  ...
]
```
### Stats
`ws://localhost:8080/stats`

Payload

```json
{
  "human-active":0,
  "human-hits":4,
  "human-misses":5,
  "human-sunks":0,
  "human-submarine-sunks":0,
  "human-carrier-sunks":0,
  "ai-active":0,
  "ai-hits":0,
  "ai-misses":0,
  "ai-sunks":0,
  "ai-carrier-sunks":0,
  "ai-submarine-sunks":0
}
```

## Run images (native or jvm)

1. Run Infinispan

```shell script
./run-infinispan.sh
```

2. Build following the instructions the native or the jvm image (instructions in Dockerfile or Dockerfile.jvm)

3. Run the application

```shell script
./run-app.sh
```
Access 
* Infinispan Console in `http://localhost:11222`. Log using admin/pass credentials
* Leaderboard: `http://localhost:8080`


`docker commit 93cee6062ce6 quay.io/redhatdemo/2021-leaderboard-service`
`docker push quay.io/redhatdemo/2021-leaderboard-service`