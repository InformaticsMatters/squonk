To setup:

cp setenv-default.sh  setenv.sh    # create the file that defines the environment variables
# edit setenv.sh as needed changing passwords and docker gateway address
source setenv.sh                   # to set the environment variables
cd ..
./build-services.sh                # build our services images
cd deploy
./build-portal.sh                  # and the portal image 
docker-compose build
./setup-containers.sh              # one-off setup and configuration
Crtl-C out of this once the setup is complete # nasty - hope to improve this



To run:

docker-compose -d





