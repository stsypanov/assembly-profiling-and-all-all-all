package com.tsypanov.benchmark;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode(value = Mode.AverageTime)
public class AtomicZeroingBenchmark {

  @Benchmark
  public Object defaultValue() {
    return new AtomicInteger();
  }

  @Benchmark
  public Object explicitValue() {
    return new AtomicInteger(0);
  }

}