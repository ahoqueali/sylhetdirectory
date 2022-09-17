#!/usr/bin/env bash
export JAVA_HOME=$(/usr/libexec/java_home -v14)
#export deleteListings=true
export rootDir=docs
./compile.sh
$JAVA_HOME/bin/java --enable-preview DirectoryBuilder dataset-2.tsv
# $JAVA_HOME/bin/java --enable-preview DirectoryBuilder dataset-1.tsv dataset-2.tsv dataset-20200718.tsv dataset-20201227.tsv
