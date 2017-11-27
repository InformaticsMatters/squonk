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
                    USERNAME=${cxnMavenUser%:*}
                    PASSWORD=${cxnMavenUser#*:}

                    dir('components') {
                        sh "echo $USERANME "
                        sh "./gradlew -PcxnMavenUser=$USERNAME -PcxnMavenPassword=$PASSWORD build"
                    }
                }
            }
        }
    }

}