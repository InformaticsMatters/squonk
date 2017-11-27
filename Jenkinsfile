// this is just for testing

pipeline {
    agent {
        label 'gradle'
    }

    openshift.withCluster() {

        stages {
            stage('Build App') {
                steps {
                    echo "Hello from the project running Jenkins: ${openshift.project()}"
                    dir ('components') {
                        script {
                            sh "gradlew build"
                        }
                    }
                }
            }
        }
    }

}