import org.yaml.snakeyaml.*
import groovy.json.*

def myname = "/Users/vinitkapoor"
pipeline {
    agent any
    parameters{
        string(defaultValue: 'vinit', description: '', name: 'myname', trim: false)
    }
    stages {
        stage('Hello') {
            steps {
                sh "cd ${myname}; pwd"
                sh "pwd"
                dir("/Users/vinitkapoor/Learning/jenkins"){
                    sh 'echo "this is a message"'
                    sh 'pwd'
                }
                echo "My name is == ${params.myname}"
                git branch: 'main', changelog: false, poll: false, url: 'https://github.com/vinitkapoor/jenkins.git'
            }
        }
    }
}