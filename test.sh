docker stop back
docker rm back
docker rmi barrierfree/back
docker build -t barrierfree/back /home/jenkins/workspace/BarrierFree/backend
docker run -d --name back -p 3030:3030 barrierfree/back

docker stop front
docker rm front
docker rmi barrierfree/front
docker build -t barrierfree/front /home/jenkins/workspace/BarrierFree/frontend
docker run -d --name front -p 80:80 barrierfree/front
