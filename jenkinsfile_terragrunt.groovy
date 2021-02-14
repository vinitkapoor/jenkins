import org.yaml.snakeyaml.*
import groovy.json.*

def myname = "/Users/vinitkapoor"
def products_dir = "products"
def environment_dir = "environment"
pipeline {
    agent any
    parameters{
        string(defaultValue: 'vinit', description: 'Product', name: 'myname', trim: false)
        choice(choices: 'analytics\ncallstats', description: 'Products', name: 'products')
    }
    stages {
        stage('Preregs&Checkout') {
            steps {
                git branch: 'main', changelog: false, poll: false, url: 'https://github.com/vinitkapoor/jenkins.git'
            }
        }

        stage('Plan'){
            sh "cd ${myname}; pwd"
            sh "pwd"
            dir("${params.products}"){
                sh 'pwd'
            }
            echo "My name is == ${params.myname}"

        }
    }
}