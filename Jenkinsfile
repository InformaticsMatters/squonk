// this is just for testing

pipeline {
    agent {
        label 'gradle'
    }

    stages {
        stage('Build App') {
            steps {

                script {
                    openshift.withCluster() {
                        echo "Hello from the project running Jenkins: ${openshift.project()}"
                    }
                    dir('components') {
                        sh "gradlew build"
                    }
                }
            }
        }
    }

}