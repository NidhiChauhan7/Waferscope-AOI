#!/usr/bin/env bash
set -e
cd "$(dirname "$0")"
mkdir -p out
echo "[1/2] Compiling Java Swing source..."
javac -encoding UTF-8 -d out -sourcepath src src/Main.java src/waferscope/model/*.java src/waferscope/io/*.java src/waferscope/classifier/*.java src/waferscope/ui/*.java
echo "[2/2] Launching WaferScope..."
java -cp out Main
