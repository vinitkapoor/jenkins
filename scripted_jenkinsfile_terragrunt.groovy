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
                choice(choices: 'none\nvcn-standard\nsubnet', description: 'Resources', name: 'resource'),
                string(name: 'POLICY_GIT_PROJECT', defaultValue: 'git@github.com:8x8/auto_gitops_oci_opa_policies.git', description: ''),
                string(name: 'POLICY_BRANCH', defaultValue: 'master', description: '')
        ])
])

policy_repo = params.POLICY_GIT_PROJECT
default_policy_repo = "git@github.com:8x8/auto_gitops_oci_opa_policies.git"
// Using the git repo/branch
if (params.POLICY_GIT_PROJECT != null){
    policy_repo = params.POLICY_GIT_PROJECT
} else {
    policy_repo = default_policy_repo
}

policy_branch = ""
default_policy_branch = "master"
// Using the git repo/branch
if (params.POLICY_BRANCH != null){
    policy_branch = params.POLICY_BRANCH
} else {
    policy_branch = default_policy_branch
}


node() {

    WORKSPACE = sh (
        script: '''pwd''',
        returnStdout: true
    ).trim()

    policy_dir = "${WORKSPACE}/policy"
    policy_git_creds = "git-8x8-ssh"

    stage('Preregs&Checkout'){
        //
        //ws_cleanup()

        //checkout_all_repos(infra_git_creds, workflow_repo, workflow_repo_branch, infra_repo, infra_branch, infra_dir)

        //git branch: 'main', changelog: false, poll: false, url: 'https://github.com/vinitkapoor/jenkins.git'
        git branch: 'develop', changelog: false, credentialsId: 'git-8x8-ssh', poll: false, url: 'git@github.com:8x8Cloud/terraform-oci-required-tags.git'

        sh '''
            curl -L -o opa https://openpolicyagent.org/downloads/latest/opa_darwin_amd64;
            chmod +x ${WORKSPACE}/opa
        '''

        // Policy repo checkout
        sh "mkdir -p ${policy_dir}"
        dir(policy_dir) {
            // Policy repo
            echo "policy repo is : ${policy_repo}"
            echo "${policy_branch}"
            echo "${policy_git_creds}"
            git(
                    url: policy_repo,
                    branch: policy_branch,
                    credentialsId: policy_git_creds
            )
        }
    }

    stage('Plan'){
        /*
        withVault([configuration: configuration, vaultSecrets: secrets]) {
            sh 'echo ${testing}'
            echo '$testing'
        }*/

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
        //cd ${terragrunt_dir};

        sh '''   
            export PATH=/usr/local/bin:$PATH;
            cp test/provider.tf ./test-provider.tf;
             
            terraform init
            terraform plan -var-file="test/testing.auto.tfvars" --out tfplan.binary;
            terraform show -json tfplan.binary > tfplan.json
            rm ./test-provider.tf
        '''

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
        //sh "${WORKSPACE}/opa eval --format pretty --data ${policy_dir}/registered_tags.rego --input tfplan.json \"data.terraform.analysis.authz\""
        opaStatus = sh (
                script:
                    '${WORKSPACE}/opa eval --format pretty --data ${policy_dir}/registered_tags.rego --input tfplan.json "data.terraform.analysis.authz"',
                returnStdout: true
        ).trim()

        echo "OPA status == ${opaStatus}"

    }

    stage('Apply'){

    }

    stage('Verify Infra'){

    }

}