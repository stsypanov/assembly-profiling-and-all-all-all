package com.tsypanov.benchmark;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;

import java.util.concurrent.TimeUnit;

@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode(value = Mode.AverageTime)
public class VolatileFieldZeroingBenchmark {

  @Benchmark
  public Object explicitInit() {
    return new Class1();
  }

  @Benchmark
  public Object defaultInit() {
    return new Class2();
  }

  private static class Class1 {
    private volatile boolean field = false;
  }

  private static class Class2 {
    private volatile boolean field;
  }

}