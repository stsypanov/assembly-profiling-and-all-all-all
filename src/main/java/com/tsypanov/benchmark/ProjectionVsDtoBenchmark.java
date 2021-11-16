package com.tsypanov.benchmark;

import com.tsypanov.Application;
import com.tsypanov.domain.HasIdAndName;
import com.tsypanov.domain.entity.ManyFieldsEntity;
import com.tsypanov.repository.ManyFieldsRepository;
import org.openjdk.jmh.annotations.*;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@State(Scope.Thread)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@BenchmarkMode(value = Mode.AverageTime)
public class ProjectionVsDtoBenchmark {

	private ManyFieldsRepository repository;

	@Param({"1", "100"})
	private int count;

	@Setup
	public void init() {
		ConfigurableApplicationContext context = SpringApplication.run(Application.class);
		context.registerShutdownHook();

		repository = context.getBean(ManyFieldsRepository.class);
		List<ManyFieldsEntity> entities = ThreadLocalRandom.current()
				.longs(count)
				.boxed()
				.map(randomLong -> new ManyFieldsEntity(randomLong, String.valueOf(randomLong), "ivan"))
				.collect(Collectors.toList());

		repository.saveAll(entities);
	}

	@Benchmark
	public Collection<HasIdAndName> findAllByNameUsingDto() {
		return repository.findAllByNameUsingDto("ivan");
	}

	@Benchmark
	public Collection<HasIdAndName> findAllByName() {
		return repository.findAllByName("ivan");
	}

}
