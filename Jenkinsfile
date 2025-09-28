pipeline {
    agent any

    tools {
        maven 'Maven3'   // configure Maven in Jenkins settings
        jdk 'JDK17'      // configure JDK in Jenkins settings
    }

    environment {
        DOCKER_IMAGE = "prats5/validation-app"
        DOCKER_TAG = "latest"
    }

    stages {
        stage('Checkout') {
            steps {
                git branch: 'main', url: 'https://github.com/pratiks5/validation-app.git'
            }
        }

        stage('Maven Build') {
            steps {
                sh 'mvn clean install -DskipTests'
            }
        }

        stage('Test') {
             steps {
                  sh 'mvn test'
             }
       }

        stage('Build Docker Image') {
            steps {
                sh """
                docker build -t $DOCKER_IMAGE:$DOCKER_TAG .
                """
            }
        }

        stage('Push Docker Image') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'dockerhub-creds', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
                    sh """
                    echo $DOCKER_PASS | docker login -u $DOCKER_USER --password-stdin
                    docker push $DOCKER_IMAGE:$DOCKER_TAG
                    """
                }
            }
        }
    }

    post {
        success {
            echo 'Build and Docker push completed successfully!'
        }
        failure {
            echo 'Build failed. Please check logs.'
        }
    }
}
