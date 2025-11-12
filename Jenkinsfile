pipeline {
    agent any

    tools {
        maven 'Maven3'
        jdk 'JDK17'
    }

    environment {
        DOCKER_IMAGE = "prats5/validation-app"
        DOCKER_TAG = "latest"
        FIXMATE_SERVICE_URL = "http://host.docker.internal:9090/api/logs/analyze"
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

                // Step 1: Define the HTML content
                def htmlContent = """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                <meta charset="UTF-8">
                <title>FixMate Build Analysis Dashboard</title>
                <style>
                @import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;600&display=swap');
                body { font-family: 'Inter', sans-serif; background: #f4f6fc; margin:0; padding:0;}
                header { background: linear-gradient(90deg,#6a11cb,#2575fc); color:white; padding:20px; text-align:center;}
                h1 { margin:0; font-size:28px;}
                .container { max-width:900px; margin:30px auto; padding:0 15px;}
                .card { background:white; border-radius:10px; box-shadow:0 4px 12px rgba(0,0,0,0.1); margin-bottom:20px; padding:20px; }
                .card h2 { margin-top:0; font-size:22px; color:#333;}
                .card p { font-size:16px; color:#555;}
                .badge { display:inline-block; padding:5px 10px; border-radius:6px; font-size:14px; font-weight:600; margin-right:10px; color:white;}
                .error   { background:#dc3545; }
                .info    { background:#17a2b8; }
                .collapsible { background-color:#eee; color:#444; cursor:pointer; padding:10px 15px; width:100%; border:none; text-align:left; outline:none; font-size:16px; border-radius:6px; margin-bottom:5px;}
                .active, .collapsible:hover { background-color:#ddd;}
                .content { padding:0 15px; display:none; overflow:hidden; background-color:#f9f9f9; border-radius:6px; margin-bottom:10px;}
                pre { background:#f0f2f5; padding:10px; border-radius:6px; overflow-x:auto;}
                footer { text-align:center; padding:15px; font-size:14px; color:#888;}
                </style>
                </head>
                <body>
                <header>
                <h1>FixMate Build Analysis Dashboard</h1>
                </header>
                <div class="container">
                <div class="card">
                <h2>Build Info</h2>
                <p><span class="badge info">Job</span> ${env.JOB_NAME}</p>
                <p><span class="badge info">Build ID</span> ${env.BUILD_NUMBER}</p>
                <p><span class="badge info">Time</span> ${new Date()}</p>
                </div>
                <div class="card">
                <h2>Error Summary</h2>
                <p><span class="badge error">Root Cause</span> ${response.content}</p>
                </div>
                <div class="card">
                <button class="collapsible">Show Raw Response</button>
                <div class="content">
                <pre>${response.content}</pre>
                </div>
                </div>
                </div>
                <footer>Powered by FixMate &copy; 2025</footer>
                <script>
                const coll = document.getElementsByClassName("collapsible");
                for (let i = 0; i < coll.length; i++) {
                    coll[i].addEventListener("click", function() {
                        this.classList.toggle("active");
                        const content = this.nextElementSibling;
                        content.style.display = content.style.display === "block" ? "none" : "block";
                    });
                }
                </script>
                </body>
                </html>
                """

                // Step 2: Write it to disk
                writeFile file: 'target/fix-mate-reports/dashboard.html', text: htmlContent

                // Step 3: Publish it in Jenkins
                publishHTML([
                    allowMissing: false,
                    alwaysLinkToLastBuild: true,
                    keepAll: true,
                    reportDir: 'target/fix-mate-reports',
                    reportFiles: 'dashboard.html',
                    reportName: 'FixMate Build Analysis Dashboard'
                ])


                writeFile file: 'target/fix-mate-reports/dashboard.html', text: htmlContent

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
