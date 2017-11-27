// this is just for testing

pipeline {
    agent any

    stages {
        stage('Build App') {
            steps {

                script {
                    openshift.withCluster() {
                        echo "Hello from the project running Jenkins: ${openshift.project()}"
                    }
                    dir('components') {
                        sh "ls -l"
                        sh "./gradlew build"
                    }
                }
            }
        }
    }

}