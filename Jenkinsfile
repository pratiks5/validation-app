pipeline {
    agent any

    tools {
        maven 'Maven3'
        jdk 'JDK17'
    }

    environment {
        DOCKER_IMAGE = "prats5/validation-app"
        DOCKER_TAG = "latest"
        FIXMATE_SERVICE_URL = "http://localhost:9090/api/logs/analyze"
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
                sh "docker build -t $DOCKER_IMAGE:$DOCKER_TAG ."
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
            echo '✅ Build and Docker push completed successfully!'
        }

        failure {
            script {
                echo "❌ Build failed — collecting logs and calling FixMate..."
                def logs = currentBuild.rawBuild.getLog(2000).join('\n')
                def buildId = env.BUILD_NUMBER
                def jobName = env.JOB_NAME

                // Prepare request payload
                def requestBody = groovy.json.JsonOutput.toJson([
                    logs    : logs,
                    buildId : buildId,
                    jobName : jobName
                ])

                // Call FixMate analysis service
                def response = httpRequest(
                    httpMode: 'POST',
                    contentType: 'APPLICATION_JSON',
                    requestBody: requestBody,
                    url: env.FIXMATE_SERVICE_URL,
                    validResponseCodes: '200:499'
                )

                echo "📩 FixMate responded with status: ${response.status}"
                echo "🧠 Suggested Fix/Analysis:\n${response.content}"

                // Save FixMate analysis report
                sh 'mkdir -p target/fix-mate-reports'
                writeFile file: 'target/fix-mate-reports/analysis.html', text: """
                <html>
                <body>
                    <h2>FixMate Build Analysis</h2>
                    <pre>${response.content}</pre>
                </body>
                </html>
                """

                publishHTML([
                    allowMissing: false,
                    alwaysLinkToLastBuild: true,
                    keepAll: true,
                    reportDir: 'target/fix-mate-reports',
                    reportFiles: 'analysis.html',
                    reportName: 'FixMate Build Analysis'
                ])
            }
        }

        always {
            archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
            junit 'target/surefire-reports/*.xml'
        }
    }
}
