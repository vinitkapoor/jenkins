import org.yaml.snakeyaml.*
import groovy.json.*

def myname = "/Users/vinitkapoor"
def products_dir = "products"
def environment_dir = "environment"
def plan_success = true
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
                try {
                    sh "cd ${myname}; pwd"
                    sh "pwd"
                    dir("${params.products}"){
                        dir("${params.environment}"){
                            sh 'export PATH=/usr/local/bin:$PATH'
                            sh 'echo $PATH'
                            sh 'pwd'
                            sh 'export PATH=$PATH; terragrunt plan'

                        }

                    }
                    echo "My name is == ${params.myname}"
                } catch (Excpetion e){
                    plan_success = false
                    echo 'plan failed'
                }
                echo 'plan success'
            }

        }
    }
}