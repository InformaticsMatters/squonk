#!groovy​

// Part of the Squonk/OepnShift CI/CD Jenkins Pipeline.
//
// This is the primary CI/CD pipeline, which provides basic assembly,
// unit testing and Docker image construction. Other pipelines may offer
// static analysis and code coverage for example.
//
// We expext to utilise a Jenkins server that supports pipelines
// running inside an OpenShift environment with a built-in server known
// by the label `maven`.
//
// In order to use this pipeline you will need
// to have setup corresponding secrets using the following IDs and (types):
//      - cxnMavenUser (text)
//      - cxnMavenPassword (text)
//      - cpSignLicense (file)
//      - chemAxonLicense (file)
//      - chemAxonReactionLibrary (file)

pipeline {

    // As we may need different flavours of agent,
    // the agent definition is deferred to each stage.
    agent none

    environment {
        ORG_GRADLE_PROJECT_cxnMavenUser = credentials('cxnMavenUser')
        ORG_GRADLE_PROJECT_cxnMavenPassword = credentials('cxnMavenPassword')
    }

    stages {

        // --------------------------------------------------------------------
        // Compilation stages
        // --------------------------------------------------------------------

        // Compile (Assemble) the code
        stage('Assemble') {

            // The assemble step is designed for jobs that execute rapidly.
            // This is not about testing or Docker,
            // This step is just about making sure the code compiles.
            agent {
                label 'maven'
            }

            steps {
                dir('components') {
                    sh './gradlew assemble --no-daemon'
                }
            }

        }

        // --------------------------------------------------------------------
        // Test (Unit)
        // --------------------------------------------------------------------

        // Unit test the code
//        stage('Unit Test') {
//
//            // The unit-test stage.
//            // Here we require the services of Docker for some tests
//            // so the built-in `maven` agent is not enough.
//            // For now we defer to AWS until we have a Docker build
//            // solution from within OpenShift.
//            agent {
//                label 'buildah-slave'
//            }
//
//            environment {
//                CPSIGN_MODEL_DIR = "${env.WORKSPACE}/tmp/cpsign"
//                CPSIGN_LICENSE_URL = "${env.WORKSPACE}/data/licenses/cpsign0.3pro.license"
//                SQUONK_DOCKER_WORK_DIR = "${env.WORKSPACE}/tmp"
//                SQUONK_NEXTFLOW_WORK_DIR = "${env.WORKSPACE}/tmp"
//            }
//
//            steps {
//                // Prepare the sub-projects
//                sh 'git submodule update --recursive --remote --init'
//                // Squonk...
//                dir('components') {
//                    withCredentials([file(credentialsId: 'cpSignLicense', variable: 'CP_FILE'),
//                                     file(credentialsId: 'chemAxonLicense', variable: 'CX_FILE'),
//                                     file(credentialsId: 'chemAxonReactionLibrary', variable: 'CX_LIB')]) {
//                        sh 'chmod u+w $CP_FILE'
//                        sh 'chmod u+w $CX_FILE'
//                        sh 'chmod u+w $CX_LIB'
//                        sh 'mkdir -p ../data/licenses'
//                        sh 'mkdir -p ../tmp/cpsign'
//                        sh 'mkdir -p ~/.chemaxon'
//                        sh 'mv -n $CP_FILE ../data/licenses'
//                        sh 'cp -n $CX_FILE ~/.chemaxon'
//                        sh 'mv -n $CX_FILE ../data/licenses'
//                        sh 'mv -n $CX_LIB ../docker/deploy/images/chemservices'
//                        sh './gradlew build --no-daemon'
//                    }
//                }
//            }
//
//        }

        // --------------------------------------------------------------------
        // Build Images
        // --------------------------------------------------------------------

        stage ('Build Images') {

            // Here we build the docker images.
            // Again, the standard agents provided by OpenShift are not
            // enough, we need an agaent that's capable of building
            // Docker images.
            agent {
                label 'buildah-slave'
            }

            environment {

                USER = 'jenkins'
                REGISTRY = 'docker-registry.default:5000'
                NAMESPACE = 'squonk-cicd'

                CHEM_IMAGE = "${NAMESPACE}/chemservices-basic:latest"
                CORE_IMAGE = "${NAMESPACE}/coreservices:latest"
                CELL_IMAGE = "${NAMESPACE}/cellexecutor:latest"

            }

            steps {

                // Prepare the sub-projects
                sh 'git submodule update --recursive --remote --init'

                dir('components') {
                    withCredentials([file(credentialsId: 'cpSignLicense', variable: 'CP_FILE'),
                                     file(credentialsId: 'chemAxonLicense', variable: 'CX_FILE'),
                                     file(credentialsId: 'chemAxonReactionLibrary', variable: 'CX_LIB')]) {

                        sh 'chmod u+w $CP_FILE'
                        sh 'chmod u+w $CX_FILE'
                        sh 'chmod u+w $CX_LIB'
                        sh 'mkdir -p ../data/licenses'
                        sh 'mkdir -p ~/.chemaxon'
                        sh 'mv -n $CP_FILE ../data/licenses'
                        sh 'cp -n $CX_FILE ~/.chemaxon'
                        sh 'mv -n $CX_FILE ../data/licenses'
                        sh 'mv -n $CX_LIB ../docker/deploy/images/chemservices'
                        sh './gradlew build --no-daemon -x test'

                        // Coreservices
                        sh './gradlew -b core-services-server/build.gradle buildDockerFile'
                        sh "buildah bud -t ${env.CORE_IMAGE} core-services-server/build"

                        // Chemservices
                        sh './gradlew chemServicesWars'
                        sh './gradlew buildChemServicesDockerfile'
                        sh "buildah bud -t ${env.CHEM_IMAGE} build/chemservices-basic"

                        // Push...
                        // With user login token
                        script {
                            TOKEN = sh(script: 'oc whoami -t', returnStdout: true).trim()
                        }
                        sh "podman login --tls-verify=false --username ${env.USER} --password ${TOKEN} ${env.REGISTRY}"
                        sh "buildah push --format=v2s2 --tls-verify=false ${env.CORE_IMAGE} docker://${env.REGISTRY}/${env.CORE_IMAGE}"
                        sh "buildah push --format=v2s2 --tls-verify=false ${env.CHEM_IMAGE} docker://${env.REGISTRY}/${env.CHEM_IMAGE}"
                        sh "podman logout ${env.REGISTRY}"

                    }

                }

            }

        }

    }

    // End-of-pipeline post-processing actions...
    post {
        failure {
            mail to: 'achristie@informaticsmatters.com tdudgeon@informaticsmatters.com',
            subject: "Failed Core Pipeline",
            body: "Something is wrong with ${env.BUILD_URL}"
        }
    }
    
}
