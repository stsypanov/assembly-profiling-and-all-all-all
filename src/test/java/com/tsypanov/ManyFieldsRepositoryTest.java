package com.tsypanov;

import com.tsypanov.domain.HasIdAndName;
import com.tsypanov.domain.entity.ManyFieldsEntity;
import com.tsypanov.repository.ManyFieldsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@SpringBootTest(classes = Application.class)
class ManyFieldsRepositoryTest {

  @Autowired
  private ManyFieldsRepository repository;

  @BeforeEach
  void setUp() {
    repository.save(new ManyFieldsEntity(1L, "1", "ivan"));
  }

  @Test
  void findAllByName() {
    HasIdAndName dto = repository.findAllByNameUsingDto("ivan").iterator().next();

    HasIdAndName projection = repository.findAllByName("ivan").iterator().next();
  }

}