Requirements:
1. Docker http://docs.docker.com/engine/installation/
2. Docker-compose http://docs.docker.com/machine/install-machine/
3. Latest code from https://github.com/InformaticsMatters/lac (includes this document)

To run:
cd <lac_root>/components
./gradlew --daemon core-services/core-services-notebook:dockerFile
cd <lac_root>/docker/notebook
docker-compose up -d
