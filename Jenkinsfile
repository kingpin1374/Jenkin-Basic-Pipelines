pipeline {
    agent any
    tools {
        maven 'mvn3.9.9'
    }
    stages {
        stage('Clone Repo') {
            steps {
                git 'https://github.com/Sonal0409/DevOpsCodeDemo.git'
            }
        }
        stage('clean') {
            steps {
                sh 'mvn clean'
            }
        }
        stage('Compile Code') {
            steps {
                sh 'mvn compile'
            }
        }
        stage('Review Code') {
            steps {
                sh 'mvn pmd:pmd'
            }
        }
        stage('Test code') {
            steps {
                sh 'mvn test'
            }
        }
        stage('Parallel Execution') {
            parallel {
                stage('Package code') {
                    steps {
                        sh 'mvn package'
                    }
                }
                stage('install') {
                    steps {
                        sh 'mvn install'
                    }
                    post {
                        always {
                            echo "successfully run the pipeline"
                        }
                        success {
                            recordIssues sourceCodeRetention: 'LAST_BUILD', tools: [pmdParser(pattern: '**/pmd.xml')]
                        }
                    }
                }
            }
        }
    }
}
