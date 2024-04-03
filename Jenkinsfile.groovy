ANT_OPTS='''-quiet \
    -Djavac.compilerargs=-nowarn \
    -Dbuild.compiler.deprecation=false \
    -Dcluster.config=streamsim \
    -Dreleasejson=../netbeansrelease.json \
    -Dmetabuild.branch=release170-streamsim \
    -Dbuildnumber=17-dev-49085d2b40f229486143294657849c5966dd5162 \
    -Duse.pack200=false \
    -Dkeystore=${WORKSPACE}/certificate/private-16.0.ks \
'''
MAVEN_CLI_OPTS='''--batch-mode \
    --errors \
    --fail-at-end \
    --show-version \
    -DinstallAtEnd=true \
    -DdeployAtEnd=true \
    -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn \
'''

SETUP:
{
    def scmChange = !currentBuild.rawBuild.getCauses()[0].toString().contains('UserIdCause')
    def firstBuild = (params.DEPLOY == null)
    if (firstBuild || scmChange) {
        DEPLOY = false
    } else {
        DEPLOY = params.DEPLOY
    }
}

def checkoutGit(branch, subfolder, remote, url, isShallow = true) {
    checkout(
        [
            $class: 'GitSCM',
            branches: [
                [name: "$branch"]
            ],
            doGenerateSubmoduleConfigurations: false,
            extensions: [
                [$class: 'CloneOption', honorRefspec: true, noTags: false, reference: '', shallow: isShallow],
                [$class: 'WipeWorkspace'],
                [$class: 'RelativeTargetDirectory', relativeTargetDir: "$subfolder"],
                [$class: 'LocalBranch', localBranch: "$branch"],
                [$class: 'SubmoduleOption', disableSubmodules: false, parentCredentials: true, recursiveSubmodules: true, reference: '', trackingSubmodules: false]
            ],
            submoduleCfg: [],
            userRemoteConfigs: [
                [
                    credentialsId: 'streamsim.support.gitlab.com',
                    name:          "$remote",
                    url:           "$url",
                    refspec:       "+refs/heads/$branch:refs/remotes/$remote/$branch +refs/tags/*:refs/tags/*"
                ]
            ]
        ]
    )
}

pipeline {
    agent any

    parameters {
        booleanParam(name: 'DEPLOY', defaultValue: false)
    }

    tools {
        jdk 'JDK_11'
        ant 'Ant 1.10.12'
        maven 'Maven 3.8.7'
    }

    stages {

        stage('Checkout') {
            steps {
                script {
                    checkoutGit('master', 'certificate', 'origin', 'https://gitlab.com/streamsim/certificate.git')
                }
            }
        }

        stage('Build') {
            steps {
                // build
                withCredentials([usernamePassword(credentialsId: 'nbms-sign-private-keystore-16.0', usernameVariable: 'KEYSTORE_USERNAME', passwordVariable: 'KEYSTORE_PASSWORD')]) {
                    sh "ant $ANT_OPTS -Dnbm_alias=${KEYSTORE_USERNAME} -Dstorepass=${KEYSTORE_PASSWORD} build build-nbms build-source-zips"
                }
                // add dlight cluster from NetBeans 8.2
                sh 'wget -q https://s3.amazonaws.com/backup-streamsim.net/archive/netbeans-8.2-dlight-cluster-nbms-sourcezips.zip'
                sh 'unzip -q netbeans-8.2-dlight-cluster-nbms-sourcezips.zip'
                // add JavaHelp
                sh 'wget -q https://s3.amazonaws.com/backup-streamsim.net/archive/jsearch-2.0_05.jar.zip'
                sh 'unzip -q jsearch-2.0_05.jar.zip'
                sh 'cp jsearch-2.0_05.jar nbbuild/netbeans/harness/antlib/'
                // build zip
                sh 'cd nbbuild && zip -r netbeans-release170-streamsim.zip netbeans && cd ..'
                // build nbms zip
                sh 'cd nbbuild && zip -r netbeans-nbms-release170-streamsim.zip nbms && cd ..'
            }
        }
        stage('Deploy') {
            steps {
                script {
                    if (DEPLOY) {
                        sh "mvn $MAVEN_CLI_OPTS nb-repository:populate -s jenkins_settings.xml"
                    }
                }
            }
        }
    }
}
