package com.tsypanov.repository;

import com.tsypanov.domain.HasIdAndName;
import com.tsypanov.domain.entity.ManyFieldsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;

public interface ManyFieldsRepository extends JpaRepository<ManyFieldsEntity, Long> {

	Collection<HasIdAndName> findAllByName(String name);

	@Query("select new com.tsypanov.domain.HasIdAndNameDto(e.id, e.name)" +
			" from ManyFieldsEntity e " +
			"where e.name = :name")
	Collection<HasIdAndName> findAllByNameUsingDto(@Param("name") String name);
	
}
