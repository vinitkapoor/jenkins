import org.yaml.snakeyaml.*
import groovy.json.*

def myname = "/Users/vinitkapoor"
def products_dir = "products"
def environment_dir = "environment"
def plan_success = 'true'

def secrets = [
        [path: 'secret/hello', engineVersion: 2, secretValues: [
                [envVar: 'testing', vaultKey: 'value_one']]]
]

def configuration = [vaultUrl: 'http://127.0.0.1:8200',
                     vaultCredentialId: 'vault-approle',
                     engineVersion: 2]


pipeline {
    agent any
    parameters{
        string(defaultValue: 'vinit', description: 'Product', name: 'myname', trim: false)
        choice(choices: 'analytics\ncallstats', description: 'Products', name: 'products')
        choice(choices: 'production\nacceptance', description: 'Environment', name: 'environment')

    }
    environment {
        PATH="/usr/local/bin:${PATH}"
    }
    stages {
        stage('Preregs&Checkout') {
            steps {
                git branch: 'main', changelog: false, poll: false, url: 'https://github.com/vinitkapoor/jenkins.git'
            }
        }

        stage('Plan'){
            steps {
                withVault([vaultSecrets: secrets]) {
                    sh 'echo $testing'
                }
                sh "cd ${myname}; pwd"
                sh "pwd"
                dir("${params.products}") {
                    dir("${params.environment}") {
                        sh 'export PATH=/usr/local/bin:$PATH'
                        sh 'echo $PATH'
                        sh 'pwd'
                        sh 'export PATH=$PATH; terragrunt plan'

                    }
                }

            }
            post {
                failure {
                    script {
                        plan_success = 'false'
                    }
                    sh "echo 'Result of Plan stage execution = ${plan_success}'"
                }
                success {
                    script {
                        plan_success = 'true'
                    }
                    sh "echo 'Result of Plan stage execution = ${plan_success}'"
                }

            }
        }
    }
}