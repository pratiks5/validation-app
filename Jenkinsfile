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

                // Parse JSON response safely
                def responseContent = response.content
                def jsonResponse = readJSON text: responseContent
                def rootCause = jsonResponse.rootCause ?: "No root cause identified"
                def suggestedFix = jsonResponse.suggestedFix ?: "No suggested fix provided"
                def buildSummary = jsonResponse.buildSummary ?: "The build failed during execution. Check the logs for detailed error information."

                // Save FixMate analysis report
                sh 'mkdir -p target/fix-mate-reports'

                // Create enhanced HTML content with the new design
                def htmlContent = """
                <!doctype html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>FixMate Build Analysis Dashboard</title>
                    <style>
                       html, body {
                           height: 100%;
                           margin: 0;
                           padding: 0;
                       }

                       body {
                           box-sizing: border-box;
                           font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, sans-serif;
                           background: #f0f0f0;
                           display: flex;
                           align-items: center;
                           justify-content: center;
                           padding: 2rem;
                       }

                       * {
                           box-sizing: border-box;
                       }

                       .dashboard-container {
                           width: 100%;
                           max-width: 1200px;
                           background: white;
                           border-radius: 16px;
                           box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
                           overflow: hidden;
                       }

                       .dashboard-header {
                           background: #335061;
                           padding: 2rem;
                           color: white;
                           position: relative;
                       }

                       .header-content {
                           display: flex;
                           align-items: flex-start;
                           justify-content: space-between;
                           flex-wrap: wrap;
                           gap: 1rem;
                       }

                       .header-title {
                           display: flex;
                           align-items: center;
                           gap: 1.5rem;
                       }

                       .robot-icon {
                           font-size: 3.5rem;
                           filter: drop-shadow(0 4px 8px rgba(0, 0, 0, 0.3));
                           animation: float 3s ease-in-out infinite;
                       }

                       @keyframes float {
                           0%, 100% { transform: translateY(0px); }
                           50% { transform: translateY(-5px); }
                       }

                       .title-content h1 {
                           margin: 0;
                           font-size: 2.5rem;
                           font-weight: 800;
                           line-height: 1.2;
                       }

                       .title-content p {
                           margin: 0.5rem 0 0 0;
                           opacity: 0.9;
                           font-size: 1.1rem;
                       }

                       .build-info-right {
                           text-align: right;
                       }

                       .build-id {
                           font-size: 1.5rem;
                           font-weight: 700;
                           margin: 0 0 0.5rem 0;
                           opacity: 0.95;
                       }

                       .branch-name {
                           font-size: 1.1rem;
                           opacity: 0.8;
                           margin: 0;
                       }

                       .status-badge {
                           display: inline-flex;
                           align-items: center;
                           gap: 0.5rem;
                           background: #fc8181;
                           padding: 0.75rem 1.5rem;
                           border-radius: 9999px;
                           font-weight: 600;
                           font-size: 1rem;
                           margin-top: 1rem;
                       }

                       .status-icon {
                           width: 10px;
                           height: 10px;
                           background: white;
                           border-radius: 50%;
                           animation: pulse 2s infinite;
                       }

                       @keyframes pulse {
                           0%, 100% { opacity: 1; }
                           50% { opacity: 0.5; }
                       }

                       .build-info {
                           display: grid;
                           grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
                           gap: 1rem;
                           padding: 2rem;
                           background: #ffffff;
                           border-bottom: 1px solid #d3d7cf;
                       }

                       .summary-card {
                           background: #f8f8f8;
                           padding: 1.5rem;
                           border-radius: 4px;
                           border-left: 4px solid #d33833;
                       }

                       .summary-card h3 {
                           margin: 0 0 1rem 0;
                           color: #2d3748;
                           font-size: 1.25rem;
                       }

                       .summary-card p {
                           margin: 0;
                           color: #4a5568;
                           line-height: 1.6;
                       }

                       .tabs-container {
                           display: flex;
                           background: #ffffff;
                           border-bottom: 2px solid #d3d7cf;
                           padding: 0 2rem;
                           overflow-x: auto;
                       }

                       .tab {
                           padding: 1rem 1.5rem;
                           background: none;
                           border: none;
                           color: #718096;
                           font-weight: 600;
                           font-size: 0.875rem;
                           cursor: pointer;
                           border-bottom: 3px solid transparent;
                           transition: all 0.3s ease;
                           white-space: nowrap;
                       }

                       .tab:hover {
                           color: #335061;
                           background: rgba(51, 80, 97, 0.05);
                       }

                       .tab.active {
                           color: #335061;
                           border-bottom-color: #335061;
                       }

                       .content-area {
                           padding: 2rem;
                           min-height: 400px;
                       }

                       .tab-content {
                           display: none;
                           animation: fadeIn 0.3s ease;
                       }

                       .tab-content.active {
                           display: block;
                       }

                       @keyframes fadeIn {
                           from { opacity: 0; transform: translateY(10px); }
                           to { opacity: 1; transform: translateY(0); }
                       }

                       .content-section {
                           background: #f8f8f8;
                           padding: 1.5rem;
                           border-radius: 4px;
                           margin-bottom: 1.5rem;
                           border-left: 4px solid #335061;
                       }

                       .content-section h3 {
                           margin: 0 0 1rem 0;
                           color: #2d3748;
                           font-size: 1.125rem;
                       }

                       .content-section p {
                           margin: 0;
                           color: #4a5568;
                           line-height: 1.6;
                       }

                       .fix-steps {
                           list-style: none;
                           padding: 0;
                           margin: 0;
                       }

                       .fix-steps li {
                           background: #f8f8f8;
                           padding: 1rem;
                           margin-bottom: 0.75rem;
                           border-radius: 4px;
                           border-left: 4px solid #6d9dc5;
                           display: flex;
                           align-items: start;
                           gap: 1rem;
                       }

                       .step-number {
                           background: #6d9dc5;
                           color: white;
                           width: 28px;
                           height: 28px;
                           border-radius: 50%;
                           display: flex;
                           align-items: center;
                           justify-content: center;
                           font-weight: 700;
                           font-size: 0.875rem;
                           flex-shrink: 0;
                       }

                       .step-content {
                           flex: 1;
                           color: #2d3748;
                       }

                       .json-viewer {
                           background: #1a202c;
                           color: #e2e8f0;
                           padding: 1.5rem;
                           border-radius: 8px;
                           font-family: 'Courier New', monospace;
                           font-size: 0.875rem;
                           overflow-x: auto;
                           line-height: 1.6;
                       }

                       @media (max-width: 768px) {
                           body {
                               padding: 1rem;
                           }

                           .dashboard-header {
                               padding: 1.5rem;
                           }

                           .header-title {
                               flex-direction: column;
                               align-items: flex-start;
                               gap: 1rem;
                           }

                           .robot-icon {
                               font-size: 2.5rem;
                           }

                           .title-content h1 {
                               font-size: 2rem;
                           }

                           .build-info-right {
                               text-align: left;
                           }

                           .build-info {
                               padding: 1.5rem;
                           }

                           .content-area {
                               padding: 1.5rem;
                           }

                           .tabs-container {
                               padding: 0 1rem;
                           }
                       }
                    </style>
                </head>
                <body>
                    <div class="dashboard-container">
                        <div class="dashboard-header">
                            <div class="header-content">
                                <div class="header-title">
                                    <h1 id="dashboard-title"><span style="font-size: 2rem; margin-right: 0.5rem;">🤖</span> FixMate Analysis</h1>
                                    <p id="build-name">Build #${env.BUILD_NUMBER} - ${env.JOB_NAME}</p>
                                </div>
                                <div class="status-badge">
                                    <span class="status-icon"></span> Build Failed
                                </div>
                            </div>
                        </div>

                        <div class="build-info">
                            <div class="summary-card">
                                <h3>Build Summary</h3>
                                <p>${buildSummary}</p>
                            </div>
                        </div>

                        <div class="tabs-container">
                            <button class="tab active" data-tab="root-cause">Root Cause</button>
                            <button class="tab" data-tab="fix">Fix</button>
                            <button class="tab" data-tab="raw-logs">Raw Response</button>
                        </div>

                        <div class="content-area">
                            <div class="tab-content active" id="content-root-cause">
                                <div class="content-section">
                                    <h3>🔍 Root Cause</h3>
                                    <p>${rootCause}</p>
                                </div>
                            </div>

                            <div class="tab-content" id="content-fix">
                                <h3 style="margin-top: 0; margin-bottom: 1.5rem; color: #2d3748;">🔧 Recommended Fix</h3>
                                <ul class="fix-steps" id="fix-steps-list">
                                    <!-- Fix steps will be populated by JavaScript -->
                                </ul>
                            </div>

                            <div class="tab-content" id="content-raw-logs">
                                <div class="content-section">
                                    <h3>📋 Raw JSON Response</h3>
                                    <div class="json-viewer">
                                        <pre>${response.content}</pre>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>

                    <script>
                        // Parse suggested fix and create steps
                        function parseFixSteps(fixText) {
                            // Escape HTML to prevent XSS
                            const escapeHtml = (text) => {
                                const div = document.createElement('div');
                                div.textContent = text;
                                return div.innerHTML;
                            };

                            const steps = fixText.split(/\\\\d+\\\\./).filter(step => step.trim());
                            const stepsList = document.getElementById('fix-steps-list');
                            stepsList.innerHTML = '';

                            if (steps.length > 0) {
                                steps.forEach((step, index) => {
                                    const li = document.createElement('li');
                                    li.innerHTML = '<div class="step-number">' + (index + 1) + '</div><div class="step-content">' + escapeHtml(step.trim()) + '</div>';
                                    stepsList.appendChild(li);
                                });
                            } else {
                                // If no numbered steps found, create a single step
                                const li = document.createElement('li');
                                li.innerHTML = '<div class="step-number">1</div><div class="step-content">' + escapeHtml(fixText) + '</div>';
                                stepsList.appendChild(li);
                            }
                        }

                        // Initialize with the suggested fix from Jenkins
                        parseFixSteps("${suggestedFix}");

                        // Tab switching functionality
                        const tabs = document.querySelectorAll('.tab');
                        const tabContents = document.querySelectorAll('.tab-content');

                        tabs.forEach(tab => {
                            tab.addEventListener('click', () => {
                                const targetTab = tab.dataset.tab;

                                tabs.forEach(t => t.classList.remove('active'));
                                tabContents.forEach(c => c.classList.remove('active'));

                                tab.classList.add('active');
                                document.getElementById('content-' + targetTab).classList.add('active');
                            });
                        });

                        // Copy fix to clipboard functionality


                        // Add copy button to fix tab
                        document.addEventListener('DOMContentLoaded', function() {
                            const fixTab = document.querySelector('[data-tab="fix"]');
                            if (fixTab) {
                                const copyButton = document.createElement('button');
                                copyButton.textContent = 'Copy Fix';
                                copyButton.style.marginLeft = '10px';
                                copyButton.style.padding = '5px 10px';
                                copyButton.style.background = '#6d9dc5';
                                copyButton.style.color = 'white';
                                copyButton.style.border = 'none';
                                copyButton.style.borderRadius = '4px';
                                copyButton.style.cursor = 'pointer';
                            }
                        });
                    </script>
                </body>
                </html>
                """

                writeFile file: 'target/fix-mate-reports/dashboard.html', text: htmlContent

                // Publish HTML report
                publishHTML([
                    allowMissing: false,
                    alwaysLinkToLastBuild: true,
                    keepAll: true,
                    reportDir: 'target/fix-mate-reports',
                    reportFiles: 'dashboard.html',
                    reportName: 'FixMate Build Analysis Dashboard'
                ])

                echo "📊 FixMate analysis dashboard generated successfully!"
            }
        }

        always {
            archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
            junit 'target/surefire-reports/*.xml'
        }
    }
}