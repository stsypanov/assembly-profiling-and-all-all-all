#!/bin/bash
for i in {1..100}
do
  touch "$i".txt
  java -jar ../target/benchmarks.jar \
  CRHMBenchmark -f 1 -wi 5 -i 1 -w 1s -r 2s -prof perfasm \
  -jvmArgsAppend "-XX:+UnlockDiagnosticVMOptions -XX:+PrintAssembly" > "$i".txt
done
