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
                        sh "echo User=${cxnMavenUser%:*}"
                        sh "./gradlew -PcxnMavenUser=${cxnMavenUser%:*} -PcxnMavenPassword=${cxnMavenUser#*:} build"
                    }
                }
            }
        }
    }

}