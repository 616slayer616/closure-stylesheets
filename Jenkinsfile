pipeline {

    options {
        timeout(time: 1, unit: 'HOURS')
        buildDiscarder(logRotator(numToKeepStr: '10', artifactNumToKeepStr: '10'))
    }

    agent {
        docker {
            image '616slayer616/jdk-selenium'
            alwaysPull true
            args '-v /var/run/docker.sock:/var/run/docker.sock'
            args '--group-add 999'
        }
    }

    stages {

        stage('build') {
            steps {
                sh 'java -version'
                sh 'mvn clean compile'
            }
        }
        stage('Test') {
            steps {
                sh 'mvn test'
            }
        }

        stage('Package') {
            steps {
                sh 'mvn package'
            }
        }

        stage('Archive .jar') {
            steps {
                archiveArtifacts 'target/*.jar'
            }
        }

        stage('release') {
            when {
                branch 'master'
            }
            steps {
                withCredentials([
                        file(credentialsId: 'maven_settings_ossrh', variable: 'FILE_PROPERTIES'),
                        file(credentialsId: 'closure-stylesheets_keyfile', variable: 'FILE_KEY')
                ]) {
                    sh 'cp $FILE_PROPERTIES .'
                    sh 'cp $FILE_KEY .'
                }
                sh 'gpg --batch --import closure-stylesheets.asc'
                sh 'mvn clean deploy -P release'
            }
        }
    }

    post {
        success {
            echo "Build successful"
        }
        always {
            junit 'target/surefire-reports/*.xml'
            cleanWs()
        }
    }
}
