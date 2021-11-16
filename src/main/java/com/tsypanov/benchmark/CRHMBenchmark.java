package com.tsypanov.benchmark;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.springframework.util.ConcurrentReferenceHashMap;

import java.util.concurrent.TimeUnit;

@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode(value = Mode.AverageTime)
public class CRHMBenchmark {

	@Benchmark
	public Object instantiate() {
		return new ConcurrentReferenceHashMap<>();
	}

}
