docker network create --driver bridge summit
docker rm -f infinispan
docker run --name=infinispan --net=summit -p 11222:11222 -e USER="admin" -e PASS="pass" infinispan/server:12.1.0.Final