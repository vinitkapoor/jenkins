import org.yaml.snakeyaml.*
import groovy.json.*


def plan_success = 'true'
def terragrunt_dir = ""

def secrets = [
        [path: 'secret/test', engineVersion: 2, secretValues: [
                [envVar: 'testing', vaultKey: 'city']]]
]

def configuration = [vaultUrl: 'http://127.0.0.1:8200',
                     vaultCredentialId: 'vault-app-role',
                     engineVersion: 2]


pipeline {
    agent any
    parameters{
        choice(choices: 'analytics\ncallstats', description: 'Products', name: 'products')
        choice(choices: 'production\nacceptance', description: 'Environment', name: 'environment')
        choice(choices: 'ap-mumbai-1\nap-melburne-1', description: 'Region', name: 'region')
        choice(choices: 'vcn-standard\nsubnet', description: 'Resources', name: 'resource')
    }
    environment {
        PATH="/usr/local/bin:${PATH}"
        VAULT_TOKEN="s.F5pQov8apty53YCW1rf3gmmu"
        VAULT_ADDR="http://127.0.0.1:8200"
    }
    stages {
        stage('Preregs&Checkout') {
            steps {
                git branch: 'main', changelog: false, poll: false, url: 'https://github.com/vinitkapoor/jenkins.git'
            }
        }

        stage('Plan'){
            steps {
                /*
                withVault([configuration: configuration, vaultSecrets: secrets]) {
                    sh 'echo ${testing}'
                    echo '$testing'
                }*/
                script {
                    terragrunt_dir = "${params.products}"

                    if ("${params.environment}" != "none") {
                        terragrunt_dir = terragrunt_dir + "/"+ "${params.environment}"
                        //echo "${terragrunt_dir}"
                        if("${params.region}" != "none"){
                            terragrunt_dir = terragrunt_dir + "/"+ "${params.region}"
                            if("${params.resource}" != "none"){
                                terragrunt_dir = terragrunt_dir + "/"+ "${params.resource}"
                            }
                        }
                    }

                }
                echo "terragrunt dir = ${terragrunt_dir}"

                sh 'export PATH=/usr/local/bin:$PATH'
                sh 'echo $PATH'
                sh 'export PATH=$PATH; terragrunt plan'

                sh "cd ${terragrunt_dir}; export PATH=/usr/local/bin:$PATH; sterragrunt plan"

                /*
                dir("${params.products}") {
                    dir("${params.environment}") {
                        sh 'export PATH=/usr/local/bin:$PATH'
                        sh 'echo $PATH'
                        sh 'pwd'
                        sh 'export PATH=$PATH; terragrunt plan'

                    }
                }

                 */

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