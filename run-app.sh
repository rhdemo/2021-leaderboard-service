docker rm -f 2021-leaderboard-service
docker run -i --rm -p 8080:8080 --net=summit -e QUARKUS_INFINISPAN_CLIENT_SERVER_LIST='infinispan:11222' 2021-leaderboard-service