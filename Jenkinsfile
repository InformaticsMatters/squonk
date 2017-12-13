// The Squonk/OepnShift CI/CD Jenkins Pipeline.
//
// We expext a Jenkins server that supports pipelines
// running inside an OpenShift environment with
// a built-in server knwon bny the label `maven`.
//
// In order to use this pipeline you will need
// to have setup corresponding secrets using the following IDs and (types):
//      - cxnMavenUser (text)
//      - cxnMavenPassword (text)
//      - cpSignLicense (file)
//      - chemAxonLicense (file)
//      - chemAxonReactionLibrary (file)
//
// You will also need an AWS EC2 slave configured using the following label.
// The server should be running an AMI that is using Linux (probably ubuntu)
// that also has Java, Docker and Docker-Compose installed (the one I built
// with Packer is ami-353eb05a).
//
// You are unlikely to be able to get away with anything less than an
// `m3.large` for the testing stage instance type.
//      - aws-im-t2large
//      - aws-im-m3large

pipeline {

    // As we need different flavours of agent,
    // the agent definition is deferred to each stage.
    agent none
    
    environment {
        ORG_GRADLE_PROJECT_cxnMavenUser = credentials('cxnMavenUser')
        ORG_GRADLE_PROJECT_cxnMavenPassword = credentials('cxnMavenPassword')
    }

    stages {

        // --------------------------------------------------------------------
        // Build (Docker)
        // --------------------------------------------------------------------
        stage ('Build (Docker)') {

            // Here we build the docker images.
            // Again, the standard agents provided by OpenShift are not
            // enough, we need an agaent that's capable of building
            // Docker images.
            agent {
                label 'docker-slave'
            }

            steps {

                sh 'git submodule update --init'
                dir('pipelines') {
                    sh 'git checkout openshift'
                    sh './copy.dirs.sh'
                }
                dir('components') {
                    withCredentials([file(credentialsId: 'cpSignLicense', variable: 'CP_FILE'),
                                     file(credentialsId: 'chemAxonLicense', variable: 'CX_FILE'),
                                     file(credentialsId: 'chemAxonReactionLibrary', variable: 'CX_LIB')]) {

                        sh 'export DOCKER_HOST=tcp://${KUBERNETES_SERVICE_HOST}:2375'
                        sh 'echo ${DOCKER_HOST}'

                        sh 'chmod u+w $CP_FILE'
                        sh 'chmod u+w $CX_FILE'
                        sh 'chmod u+w $CX_LIB'
                        sh 'mkdir -p ../data/licenses'
                        sh 'mkdir -p ~/.chemaxon'
                        sh 'mv -n $CP_FILE ../data/licenses'
                        sh 'mv -n $CX_FILE ../data/licenses'
                        sh 'mv -n $CX_LIB ../docker/deploy/images/chemservices'

                        sh 'echo ${DOCKER_HOST}'

                        sh './gradlew buildDockerImages -x test'

                    }
                }

            }

        }

    }

    post {
        failure {
            mail to: 'achristie@informaticsmatters.com tdudgeon@informaticsmatters.com',
            subject: "Failed Pipeline",
            body: "Something is wrong with ${env.BUILD_URL}"
        }
    }
    
}
