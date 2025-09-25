pipeline {
    agent any
    parameters {
        choice(
            name: 'SCAN_TYPE',
            choices: ['Baseline', 'API', 'Full'],
            description: 'Give the type of scan to be performed'
        )
        string(
            name: 'TARGET',
            defaultValue: 'https://medium.com',
            description: 'The target URL to scan'
        )
    }
    stages {
        stage('Setting up ZAP Container') {
            steps {
                echo "Pulling docker ZAP image"
                sh 'docker pull ghcr.io/zaproxy/zaproxy:stable'
                echo "Downloading of image completed"
                echo "Running Image ==> zap container"
                sh "docker run -d --name owasp-${BUILD_NUMBER} ghcr.io/zaproxy/zaproxy:stable sleep infinity"
            }
        }
        stage('Loginto container for ZAP to start the scan') {
            steps {
                echo "here we give container name and execute command"
                sh "docker exec owasp-${BUILD_NUMBER} mkdir -p /zap/wrk"
            }
        }
        stage('Scan target on OWASP container') {
            steps {
                script {
                    def scan_type = params.SCAN_TYPE
                    echo "---->SCAN_TYPE: ${scan_type}"
                    def target = params.TARGET
                    if (scan_type == 'Baseline') {
                        sh """
                            docker exec owasp-${BUILD_NUMBER} sh -c "cd /zap/wrk && zap-baseline.py -t ${target} -r report.html -I"
                        """
                    } else if (scan_type == 'API') {
                        sh """
                            docker exec owasp-${BUILD_NUMBER} sh -c "cd /zap/wrk && zap-api-scan.py -t ${target} -r report.html -I"
                        """
                    } else if (scan_type == 'Full') {
                        sh """
                            docker exec owasp-${BUILD_NUMBER} sh -c "cd /zap/wrk && zap-full-scan.py -t ${target} -r report.html -I"
                        """
                    } else {
                        echo 'something went wrong...'
                    }
                }
            }
        }
        stage('Copy Report to workspace') {
            steps {
                script {
                    sh "docker cp owasp-${BUILD_NUMBER}:/zap/wrk/report.html ${WORKSPACE}/report.html"
                }
            }
        }
    }
    post {
        always {
            echo 'Remove container'
            sh """
                docker stop owasp-${BUILD_NUMBER} || true
                docker rm owasp-${BUILD_NUMBER} || true
            """
        }
    }
}