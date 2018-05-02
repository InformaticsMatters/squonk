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
        // Testing
        // --------------------------------------------------------------------

        // Unit test the code
        stage('Test') {

            parallel {

                stage('Unit Test') {

                    agent {
                        label 'maven'
                    }

                    environment {
                        CPSIGN_MODEL_DIR = "${env.WORKSPACE}/tmp/cpsign"
                        CPSIGN_LICENSE_URL = "${env.WORKSPACE}/data/licenses/cpsign0.3pro.license"
                        SQUONK_DOCKER_WORK_DIR = "${env.WORKSPACE}/tmp"
                        SQUONK_NEXTFLOW_WORK_DIR = "${env.WORKSPACE}/tmp"
                    }

                    steps {
                        dir('components') {
                            withCredentials([file(credentialsId: 'cpSignLicense', variable: 'CP_FILE'),
                                             file(credentialsId: 'chemAxonLicense', variable: 'CX_FILE'),
                                             file(credentialsId: 'chemAxonReactionLibrary', variable: 'CX_LIB')]) {

                                sh 'chmod u+w $CP_FILE'
                                sh 'chmod u+w $CX_FILE'
                                sh 'chmod u+w $CX_LIB'
                                sh 'mkdir -p ../data/licenses'
                                sh 'mkdir -p ../tmp/cpsign'
                                sh 'mkdir -p ~/.chemaxon'
                                sh 'mv -n $CP_FILE ../data/licenses'
                                sh 'cp -n $CX_FILE ~/.chemaxon'
                                sh 'mv -n $CX_FILE ../data/licenses'
                                sh 'mv -n $CX_LIB ../docker/deploy/images/chemservices'

                                sh 'env'
                                sh 'ls -a ~/.chemaxon'
                                sh 'cat ~/.chemaxon/license.cxl'

                                // Run tests using code-coverage
                                //sh './gradlew build --no-daemon'
                                sh './gradlew test jacocoTestReport --no-daemon'

                                // Analyse and present the results...
                                jacoco sourcePattern: '**/src/main/groovy'

                            }
                        }
                    }

                }

                stage('FindBugs') {

                    agent {
                        label 'maven'
                    }

                    steps {
                        dir('components') {

                            // Run FindBugs on the code...
                            sh './gradlew findbugsMain --no-daemon'

                            // Analyse and present the results...
                            findbugs canComputeNew: false,
                                isRankActivated: true,
                                pattern: '**/findbugsReports/main.xml'

                        }
                    }

                }

            }

        }

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

                        // CoreServices
                        sh './gradlew -b core-services-server/build.gradle buildDockerFile'
                        sh "buildah bud --format docker -t ${env.CORE_IMAGE} core-services-server/build"

                        // ChemServices
                        sh './gradlew chemServicesWars'
                        sh './gradlew buildChemServicesDockerfile'
                        sh "buildah bud --format docker -t ${env.CHEM_IMAGE} build/chemservices-basic"

                        // CellExecutor
                        sh "buildah bud --format docker -f Dockerfile_buildah -t ${env.CELL_IMAGE} cell-executor"

                        // Push...
                        // With user login token
                        script {
                            TOKEN = sh(script: 'oc whoami -t', returnStdout: true).trim()
                        }
                        sh "podman login --tls-verify=false --username ${env.USER} --password ${TOKEN} ${env.REGISTRY}"
//                        sh "buildah push --tls-verify=false ${env.CORE_IMAGE} docker://${env.REGISTRY}/${env.CORE_IMAGE}"
//                        sh "buildah push --tls-verify=false ${env.CHEM_IMAGE} docker://${env.REGISTRY}/${env.CHEM_IMAGE}"
//                        sh "buildah push --tls-verify=false ${env.CELL_IMAGE} docker://${env.REGISTRY}/${env.CELL_IMAGE}"
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
            subject: "Failed Squonk Pipeline",
            body: "Something is wrong with the Squonk CI/CD SQUONK build ${env.BUILD_URL}"
        }
    }
    
}
