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

properties([
        parameters([
                choice(choices: ['list1', 'list2'], description: '''list one is sample''', name: 'my choice'),
                choice(choices: 'analytics\ncallstats', description: 'Products', name: 'products'),
                choice(choices: 'production\nacceptance', description: 'Environment', name: 'environment'),
                choice(choices: 'none\nap-mumbai-1\nap-melburne-1', description: 'Region', name: 'region'),
                choice(choices: 'none\nvcn-standard\nsubnet', description: 'Resources', name: 'resource')
        ])
])


node('executor_1_label') {

    stage('Preregs&Checkout'){
        //
        //ws_cleanup()

        //checkout_all_repos(infra_git_creds, workflow_repo, workflow_repo_branch, infra_repo, infra_branch, infra_dir)

        git branch: 'main', changelog: false, poll: false, url: 'https://github.com/vinitkapoor/jenkins.git'

        //replace above git repo with the one from auto_hashicorp.. define Gitcredentails to access it

    }

    stage('Plan'){
        withVault([configuration: configuration, vaultSecrets: secrets]) {
            sh 'echo ${testing}'
            echo '$testing'
        }

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


        echo "terragrunt dir = ${terragrunt_dir}"

        sh 'export PATH=/usr/local/bin:$PATH'
        sh 'echo $PATH'
        //sh 'export PATH=$PATH; terragrunt plan'

        sh "cd ${terragrunt_dir}; export PATH=/usr/local/bin:$PATH; terragrunt plan"

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

    stage('Check Policies'){

    }

    stage('Apply'){

    }

    stage('Verify Infra'){

    }

}