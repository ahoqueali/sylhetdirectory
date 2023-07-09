#!/usr/bin/env bash
echo "building..."
#export CLASSPATH=commons-lang3-3.10.jar
$JAVA_HOME/bin/javac DirectoryBuilder.java --enable-preview -source 20
