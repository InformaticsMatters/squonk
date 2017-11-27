// this is just for testing

pipeline {
    agent {
        label 'gradle'
    }

    openshift.withCluster() {
        echo "Hello from the project running Jenkins: ${openshift.project()}"

        stages {
            stage('Build App') {
                steps {
                    dir ('components') {
                        sh "gradlew build"
                    }
                }
            }
        }
    }

}