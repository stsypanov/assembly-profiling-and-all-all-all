package com.tsypanov;

import com.tsypanov.repository.ManyFieldsRepository;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@ComponentScan
@EnableAutoConfiguration
@EnableJpaRepositories(basePackageClasses = ManyFieldsRepository.class)
public class Application {
}
