package com.tsypanov.benchmark;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class TurnOffInliningBenchmark {

  @Benchmark
  public int calculate() {
    return calculate(200);
  }

  @Benchmark
  @Fork(jvmArgsAppend = {"-XX:MaxInlineLevel=0"})
  public int calculateNoInlining() {
    return calculate(200);
  }

  private Integer calculate(int length) {
    Integer sum = 0;
    for (int i = 0; i <length; i++) {
      sum +=i;
    }
    return sum;
  }

}