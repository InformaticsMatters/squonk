// The Squonk/OepnShift CI/CD Jenkins Pipeline.
//
//         THIS IS A WORK-IN-PROGRESS
//       AND SHOULD BE CONSIDERED 'ALPHA'
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
        // Compilation and analysis stages
        // --------------------------------------------------------------------
        stage ('Compile') {
            parallel {

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

                // Static analysis (FindBugs)
                stage('Static Analyis') {

                    // The assemble step is designed for jobs that execute rapidly.
                    // This is not about testing or Docker,
                    // This step is just about making sure the code compiles.
                    agent {
                        label 'maven'
                    }

                    steps {
                        dir('components') {
                            sh './gradlew findbugsMain --no-daemon'
                        }
                    }

                }

            }
        }

        // --------------------------------------------------------------------
        // Test
        // --------------------------------------------------------------------
        stage ('Test') {

            // The testing stage.
            // Here we require the services of Docker for some tests
            // so the built-in `maven` agent is not enough.
            // For now we defer to AWS until we have a Docker build
            // solution from within OpenShift.
            agent {
                label 'aws-im-m3large'
            }

            // Only run these steps on the openshift branch.
            when {
                branch '*/openshift'
            }

            environment {
                CPSIGN_MODEL_DIR = "${env.WORKSPACE}/tmp/cpsign"
                CPSIGN_LICENSE_URL = "${env.WORKSPACE}/data/licenses/cpsign0.3pro.license"
                SQUONK_DOCKER_WORK_DIR = "${env.WORKSPACE}/tmp"
                SQUONK_NEXTFLOW_WORK_DIR = "${env.WORKSPACE}/tmp"
            }

            parrallel {

                stage ('Unit Test') {

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
                                sh './gradlew build --no-daemon'
                            }
                        }
                    }
                }

                stage ('Code Coverage') {

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
                                sh './gradlew test jacocoTestReport --no-daemon'
                            }
                        }
                    }
                }

            }
        }

        // --------------------------------------------------------------------
        // Build (Docker)
        // --------------------------------------------------------------------
        stage ('Build (Docker)') {

            // Only run these steps on the openshift branch.
            when {
                branch '*/openshift'
            }

            // Here we build the docker images.
            // Again, the standard agents provided by OpenShift are not
            // enough, we need an agaent that's capable of building
            // Docker images.
            agent {
                label 'aws-im-t2large'
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
                        sh 'chmod u+w $CP_FILE'
                        sh 'chmod u+w $CX_FILE'
                        sh 'chmod u+w $CX_LIB'
                        sh 'mkdir -p ../data/licenses'
                        sh 'mkdir -p ~/.chemaxon'
                        sh 'mv -n $CP_FILE ../data/licenses'
                        sh 'mv -n $CX_FILE ../data/licenses'
                        sh 'mv -n $CX_LIB ../docker/deploy/images/chemservices'
                        sh './gradlew buildDockerImages -x test --no-daemon'
                    }
                }   
            }

        }

    }

    post {
        failure {
            mail to: 'achristie@informaticsmatters.com',
            subject: "Failed Pipeline",
            body: "Something is wrong with ${env.BUILD_URL}"
        }
    }
    
}
