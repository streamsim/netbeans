ANT_OPTS='''-quiet \
    -Djavac.compilerargs=-nowarn \
    -Dbuild.compiler.deprecation=false \
    -Dcluster.config=streamsim \
    -Dreleasejson=../netbeansrelease.json \
    -Dmetabuild.branch=release113-streamsim \
'''
MAVEN_CLI_OPTS='''--batch-mode \
    --errors \
    --fail-at-end \
    --show-version \
    -DinstallAtEnd=true \
    -DdeployAtEnd=true \
    -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn \
 '''

pipeline {
    agent any

    tools {
        jdk 'OpenJDK_1.8.0_322'
        ant 'Ant 1.10.4 (NetBeans 11.3)'
        maven 'Maven 3.8.4'
    }

    stages {
        stage('Build') {
            steps {
                sh 'wget -q https://s3.amazonaws.com/backup-streamsim.net/archive/netbeans-8.2-201609300101-dlight.zip'
                sh 'wget -q https://s3.amazonaws.com/backup-streamsim.net/archive/jsearch-2.0_05.jar.zip'

                // build
                sh "ant $ANT_OPTS build build-nbms build-source-zips"
                // remove offending .xml
                sh 'rm nbbuild/netbeans/platform/config/Modules/org-netbeans-libs-batik-read.xml'
                // add dlight cluster from NetBeans 8.2
                sh 'unzip -q netbeans-8.2-201609300101-dlight.zip'
                sh 'cp -R dlight nbbuild/netbeans/'
                // add JavaHelp
                sh 'unzip -q jsearch-2.0_05.jar.zip'
                sh 'cp jsearch-2.0_05.jar nbbuild/netbeans/harness/antlib/'
                // build zip
                sh 'cd nbbuild && zip -r netbeans-release113-streamsim.zip netbeans && cd ..'
                // build nbms zip
                sh 'cd nbbuild && zip -r netbeans-nbms-release113-streamsim.zip nbms && cd ..'
            }
        }
        stage('Deploy') {
            steps {
                sh "mvn $MAVEN_CLI_OPTS nb-repository:populate -s jenkins_settings.xml"
            }
        }
    }
}
