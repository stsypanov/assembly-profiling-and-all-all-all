package com.tsypanov.benchmark;

import org.openjdk.jmh.profile.AsyncProfiler;
import org.openjdk.jmh.profile.GCProfiler;
import org.openjdk.jmh.profile.JavaFlightRecorderProfiler;
import org.openjdk.jmh.profile.LinuxPerfAsmProfiler;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

public class BenchmarkRunner {

  public static void main(String[] args) throws RunnerException {
    Options opt = new OptionsBuilder()
          .include(ProjectionVsDtoBenchmark.class.getSimpleName())
//				.include(ProjectionProfilingBenchmark.class.getSimpleName())
//				.include(CRHMBenchmark.class.getSimpleName())
//				.include(VolatileFieldZeroingBenchmark.class.getSimpleName())
//				.include(AtomicZeroingBenchmark.class.getSimpleName())
//				.include(TurnOffInliningBenchmark.class.getSimpleName())
            .warmupIterations(10)
            .warmupTime(TimeValue.seconds(1))
            .measurementIterations(10)
            .measurementTime(TimeValue.seconds(1))
            .addProfiler(GCProfiler.class)
//						.addProfiler(JavaFlightRecorderProfiler.class)
//						.addProfiler(AsyncProfiler.class)
//						.addProfiler(LinuxPerfAsmProfiler.class)
            .forks(2)
            .shouldFailOnError(true)
//            .jvmArgsAppend(
//						"-XX:+UnlockDiagnosticVMOptions"
//						,"-XX:+DebugNonSafepoints"
//						,"-XX:+PrintAssembly"
//            )
            .build();

    new Runner(opt).run();
  }
}