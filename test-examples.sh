#!/usr/bin/env bash

# This is not part of the build process. This is something that is run before a release to check
# if the release is compatible with known versions of java and gradle.
#
# The only prerequisite is that the code be checked out and sdkman be installed.
#

set -e

GROOVES_DIR=$(pwd)

# Given a file and a search pattern, edits the file to comment the line containing the pattern.
# This uses C style comments.
function commentLine() {
    cp $1 /tmp/temp.txt
    awk "/$2/{print \"\/\/ \"\$0;next}1" /tmp/temp.txt > $1
    rm /tmp/temp.txt
}
export -f commentLine

function replaceProject() {
    cat $1 | sed -E "s/project\('(.+)'\)/'com.github.rahulsom\1:0.5.0-SNAPSHOT'/g" > /tmp/temp.txt
    cp /tmp/temp.txt $1
}
export -f replaceProject

testApp() {
    echo "Testing $1 on gradle $2 with java $3"

    cd /tmp/examples
    cd $1
    gradle clean -I /tmp/init.gradle 2>&1 > ${LOG_FILE} && echo "Clean: OK" || echo "Clean: Fail"
    gradle check -I /tmp/init.gradle 2>&1 >> ${LOG_FILE} && echo "Check: OK" || echo "Clean: Fail"
}

source $HOME/.sdkman/bin/sdkman-init.sh
./gradlew clean
rsync -rzh examples /tmp/

cat > /tmp/init.gradle << EOF
allprojects {
    repositories {
        mavenCentral()
        jcenter()
        maven { url 'http://oss.sonatype.org/content/repositories/snapshots/' }
    }

    configurations.all {
        resolutionStrategy.cacheChangingModulesFor 30, 'minutes'
    }
}
EOF

find /tmp/examples -name build.gradle \
        | xargs -n 1 dirname \
        | xargs -n 1 cp ${GROOVES_DIR}/gradle.properties

find /tmp/examples -name build.gradle \
        | xargs -n 1 -I {} bash -c 'commentLine "$@"' _ {} "apply from"

find /tmp/examples -name build.gradle \
        | xargs -n 1 -I {} bash -c 'replaceProject "$@"' _ {}

declare -a javaVersions=("10.0.1-zulu" "8.0.172-zulu")
declare -a gradleVersions=("4.9")
declare -a projects=("grails/rdbms" "pushstyle" "springboot/jpa" "springboot/kotlin" "javaee")

for j in "${javaVersions[@]}"; do
    sdk use java ${j}
    for g in "${gradleVersions[@]}"; do
        sdk use gradle ${g}
        for p in "${projects[@]}"; do
            LOG_FILE=/tmp/$(echo ${p} | sed -e "s/\//-/g")-j${j}-g${g}.log
            testApp ${p} ${g} ${j}
        done
    done
done
