/*
 * Copyright (c) 2026 FalkorDB Ltd.
 */

package org.springframework.data.falkordb.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.falkordb.core.DefaultFalkorDBClient;
import org.springframework.data.falkordb.core.FalkorDBClient;
import org.springframework.data.falkordb.core.FalkorDBTemplate;
import org.springframework.data.falkordb.core.mapping.DefaultFalkorDBEntityConverter;
import org.springframework.data.falkordb.core.mapping.DefaultFalkorDBMappingContext;
import org.springframework.data.falkordb.core.mapping.FalkorDBMappingContext;
import org.springframework.data.falkordb.core.schema.Id;
import org.springframework.data.falkordb.core.schema.Node;
import org.springframework.data.falkordb.repository.FalkorDBRepository;
import org.springframework.data.falkordb.repository.config.EnableFalkorDBRepositories;
import org.springframework.data.falkordb.repository.query.Query;
import org.springframework.data.mapping.model.EntityInstantiators;

import com.falkordb.Driver;
import com.falkordb.impl.api.DriverImpl;

class DtoProjectionRepositoryIntegrationTest {

	private AnnotationConfigApplicationContext context;
	private FalkorDBTemplate template;
	private SkillProjectionRepository repository;

	@BeforeEach
	void setUp() {
		context = new AnnotationConfigApplicationContext(TestConfig.class);
		template = context.getBean(FalkorDBTemplate.class);
		repository = context.getBean(SkillProjectionRepository.class);

		// Clean slate
		template.query("MATCH (n) DETACH DELETE n", Collections.emptyMap(), r -> null);

		// Seed data
		template.query("CREATE (:Skill {name: 'Java'})-[:IS_PART_OF]->(:Field {name: 'Backend'})",
				Collections.emptyMap(), r -> null);
		template.query("MATCH (s:Skill {name: 'Java'}) CREATE (s)-[:IS_PART_OF]->(:Field {name: 'JVM'})",
				Collections.emptyMap(), r -> null);
	}

	@AfterEach
	void tearDown() {
		try {
			template.query("MATCH (n) DETACH DELETE n", Collections.emptyMap(), r -> null);
		}
		finally {
			if (context != null) {
				context.close();
			}
		}
	}

	@Test
	void shouldMapClassBasedDtoProjectionFromCustomQuery() {
		List<SkillField> results = repository.findSkillsWithFields();

		assertThat(results).hasSize(1);
		SkillField dto = results.get(0);
		assertThat(dto.getSkill()).isNotNull();
		assertThat(dto.getSkill().getName()).isEqualTo("Java");
		assertThat(dto.getField()).extracting(Field::getName).containsExactlyInAnyOrder("Backend", "JVM");
	}

	interface SkillProjectionRepository extends FalkorDBRepository<Skill, String> {

		@Query("""
			MATCH (s:Skill)
			OPTIONAL MATCH (s)-[:IS_PART_OF]->(f:Field)
			RETURN s AS skill, collect(f) AS field
		""")
		List<SkillField> findSkillsWithFields();
	}

	@Node("Skill")
	static class Skill {
		@Id
		private String name;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}
	}

	@Node("Field")
	static class Field {
		@Id
		private String name;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}
	}

	static class SkillField {
		private Skill skill;
		private List<Field> field;

		public Skill getSkill() {
			return skill;
		}

		public void setSkill(Skill skill) {
			this.skill = skill;
		}

		public List<Field> getField() {
			return field;
		}

		public void setField(List<Field> field) {
			this.field = field;
		}
	}

	@Configuration
	@EnableFalkorDBRepositories(basePackageClasses = SkillProjectionRepository.class, considerNestedRepositories = true)
	static class TestConfig {

		@Bean
		public Driver falkorDBDriver() {
			return new DriverImpl("localhost", 6379);
		}

		@Bean
		public FalkorDBClient falkorDBClient(Driver driver) {
			return new DefaultFalkorDBClient(driver, "test_dto_projection");
		}

		@Bean
		public FalkorDBMappingContext falkorDBMappingContext() {
			return new DefaultFalkorDBMappingContext();
		}

		@Bean
		public FalkorDBTemplate falkorDBTemplate(FalkorDBClient client, FalkorDBMappingContext mappingContext) {
			EntityInstantiators instantiators = new EntityInstantiators();
			DefaultFalkorDBEntityConverter converter = new DefaultFalkorDBEntityConverter(mappingContext,
					instantiators, client);
			return new FalkorDBTemplate(client, mappingContext, converter);
		}
	}
}
