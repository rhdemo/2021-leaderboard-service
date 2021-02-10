# 2021-leaderboard-service - WIP
Leaderboard Service, baked with Quarkus and Infinispan

Run Infinispan

`docker run -v $(pwd):/user-config  -p 11222:11222 -e USER="admin" -e PASS="pass" infinispan/server:12.0.1.Final`

Create the cache in the console using the `cacheConfig.json`

## Scoring Service

Health of the service

`http POST http://localhost:8081/scoring/ < new-score.json`

Health of the service

`http GET http://localhost:8081/scoring`
