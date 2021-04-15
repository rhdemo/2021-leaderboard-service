# 2021-leaderboard-service

Leaderboard Service, baked with Quarkus and Infinispan

Run Infinispan with Docker

`docker run -v $(pwd):/user-config  -p 11222:11222 -e USER="admin" -e PASS="pass" infinispan/server:12.0.1.Final`

## Scheduling properties for deployments

Default values are 4s and 1s.

```properties
leaderboard.schedule=4s
stats.schedule=1s
```

Change them in the deployment in containers using the following env variables:

```bash
LEADERBOARD_SCHEDULE='3s'
STATS_SCHEDULE='0.5s''
```

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
    "userName": "Michael",
    "score":12,
    "timestamp":9090898,
    "gameStatus":"PLAYING"
  }, 
  {
    "userId":"u5",
    "matchId":"m1",
    "gameId":"g1",
    "human":true,
    "userName": "Jennifer",
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
  "games-played": 190,
  "total-hits": 199,
  "total-misses": 30,
  "total-sunk": 120,
  "human-shots": 300,
  "human-active": 21,
  "human-win": 0,
  "human-loss": 0,
  "human-hits": 8,
  "human-misses": 15,
  "human-sunks": 12,
  "human-carrier-sunks": 4,
  "human-submarine-sunks": 5,
  "human-destroyer-sunks": 2,
  "human-battleship-sunks": 1,
  "human-bonus": 0,
  "ai-shots": 500,
  "ai-active": 21,
  "ai-win": 0,
  "ai-loss": 0,
  "ai-hits": 0,
  "ai-misses": 0,
  "ai-sunks": 0,
  "ai-carrier-sunks": 0,
  "ai-submarine-sunks": 0,
  "ai-destroyer-sunks": 0,
  "ai-battleship-sunks": 0,
  "ai-bonus": 0
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
  

`docker commit 8459465e4795 quay.io/redhatdemo/2021-leaderboard-service`
`docker push quay.io/redhatdemo/2021-leaderboard-service`