#!/usr/bin/env bash
export JAVA_HOME=$(/usr/libexec/java_home -v20)
#export deleteListings=true
export rootDir=docs
rm -rf docs/categories
./compile.sh
$JAVA_HOME/bin/java --enable-preview DirectoryBuilder /Users/abdulhoque/Downloads/sylhetdirectory.tsv
#$JAVA_HOME/bin/java --enable-preview DirectoryBuilder dataset-2.tsv