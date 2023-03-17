ANT_OPTS='''-quiet \
    -Djavac.compilerargs=-nowarn \
    -Dbuild.compiler.deprecation=false \
    -Dcluster.config=streamsim \
    -Dreleasejson=../netbeansrelease.json \
    -Dmetabuild.branch=release170-streamsim \
    -Duse.pack200=false \
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
        jdk 'OpenJDK_11.0.17+8'
        ant 'Ant 1.10.12'
        maven 'Maven 3.8.7'
    }

    stages {
        stage('Build') {
            steps {
                // build
                sh "ant $ANT_OPTS build build-nbms build-source-zips"
                // remove offending .xml files
                sh 'rm -f nbbuild/netbeans/platform/config/Modules/org-netbeans-libs-batik-read.xml'
                sh 'rm -f nbbuild/netbeans/platform/config/Modules/org-openide-util-ui-svg.xml'
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
                sh "mvn $MAVEN_CLI_OPTS nb-repository:populate -s jenkins_settings.xml"
            }
        }
    }
}
