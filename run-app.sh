docker rm -f 2021-leaderboard-service
docker run -i --rm -p 8080:8080 --net=infinispan-docker-compose_summit -e QUARKUS_INFINISPAN_CLIENT_SERVER_LIST='infinispan:11222' 2021-leaderboard-service

docker run -i --rm -p 8080:8080 --net=infinispan-docker-compose_summit -e QUARKUS_INFINISPAN_CLIENT_SERVER_LIST='infinispan:11222' -e LEADERBOARD_CONFIGURE_INFINISPAN='true' quay.io/redhatdemo/2021-leaderboard-service