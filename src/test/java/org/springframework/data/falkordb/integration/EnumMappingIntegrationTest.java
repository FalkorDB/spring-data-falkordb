/*
 * Copyright (c) 2023-2025 FalkorDB Ltd.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.springframework.data.falkordb.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

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
import org.springframework.data.falkordb.examples.Employee;
import org.springframework.data.falkordb.examples.EmployeeRepository;
import org.springframework.data.falkordb.examples.EmploymentStatus;
import org.springframework.data.falkordb.examples.JobType;
import org.springframework.data.falkordb.repository.config.EnableFalkorDBRepositories;
import org.springframework.data.mapping.model.EntityInstantiators;

import com.falkordb.Driver;
import com.falkordb.impl.api.DriverImpl;

/**
 * Integration test for enum property mapping in Spring Data FalkorDB.
 * 
 * <p>This test verifies that Java enum types are correctly converted to/from strings
 * when persisting to FalkorDB, providing compatibility with Spring Data Neo4j.</p>
 * 
 * <p>Tests cover:</p>
 * <ul>
 *   <li>Basic save and retrieve with enum properties</li>
 *   <li>Derived query methods with enum parameters</li>
 *   <li>Custom @Query methods with enum parameters</li>
 *   <li>Collection operations with enums</li>
 *   <li>Count and exists queries with enums</li>
 *   <li>Multiple enum properties on same entity</li>
 * </ul>
 *
 * @author FalkorDB
 * @since 1.0
 */
class EnumMappingIntegrationTest {

	private AnnotationConfigApplicationContext context;

	private EmployeeRepository repository;

	private FalkorDBTemplate template;

	@BeforeEach
	void setUp() {
		context = new AnnotationConfigApplicationContext(TestConfig.class);
		repository = context.getBean(EmployeeRepository.class);
		template = context.getBean(FalkorDBTemplate.class);

		// Clean up any existing test data
		cleanDatabase();
	}

	@AfterEach
	void tearDown() {
		cleanDatabase();
		if (context != null) {
			context.close();
		}
	}

	private void cleanDatabase() {
		try {
			template.query("MATCH (e:Employee) DELETE e", new java.util.HashMap<>(), result -> null);
		}
		catch (Exception e) {
			// Ignore errors during cleanup
		}
	}

	/**
	 * Test that enum properties are correctly saved and retrieved.
	 * Verifies bidirectional conversion: Enum -> String (save) and String -> Enum (read).
	 */
	@Test
	void shouldSaveAndRetrieveEmployeeWithEnumProperties() {
		// Given: Create an employee with enum properties
		Employee employee = new Employee("John Doe", "john@example.com", JobType.FULL_TIME, EmploymentStatus.ACTIVE);
		employee.setSalary(75000.0);
		employee.setDepartment("Engineering");

		// When: Save the employee
		Employee saved = repository.save(employee);

		// Then: Verify the employee was saved with an ID
		assertThat(saved.getId()).isNotNull();

		// When: Retrieve the employee by ID
		Optional<Employee> retrieved = repository.findById(saved.getId());

		// Then: Verify enum properties are correctly converted back
		assertThat(retrieved).isPresent();
		Employee emp = retrieved.get();
		assertThat(emp.getName()).isEqualTo("John Doe");
		assertThat(emp.getJobType()).isEqualTo(JobType.FULL_TIME);
		assertThat(emp.getStatus()).isEqualTo(EmploymentStatus.ACTIVE);
		assertThat(emp.getSalary()).isEqualTo(75000.0);
	}

	/**
	 * Test derived query method with enum parameter.
	 */
	@Test
	void shouldFindEmployeesByJobTypeUsingDerivedQuery() {
		// Given: Create employees with different job types
		repository.save(new Employee("Alice", "alice@example.com", JobType.FULL_TIME, EmploymentStatus.ACTIVE));
		repository.save(new Employee("Bob", "bob@example.com", JobType.PART_TIME, EmploymentStatus.ACTIVE));
		repository.save(new Employee("Charlie", "charlie@example.com", JobType.FULL_TIME, EmploymentStatus.ACTIVE));

		// When: Query by job type using derived query
		List<Employee> fullTimeEmployees = repository.findByJobType(JobType.FULL_TIME);

		// Then: Should find only full-time employees
		assertThat(fullTimeEmployees).hasSize(2);
		assertThat(fullTimeEmployees).allMatch(e -> e.getJobType() == JobType.FULL_TIME);
		assertThat(fullTimeEmployees).extracting(Employee::getName).containsExactlyInAnyOrder("Alice", "Charlie");
	}

	/**
	 * Test derived query with multiple enum parameters.
	 */
	@Test
	void shouldFindEmployeesByJobTypeAndStatus() {
		// Given: Create employees with various combinations
		repository.save(new Employee("Alice", "alice@example.com", JobType.FULL_TIME, EmploymentStatus.ACTIVE));
		repository.save(new Employee("Bob", "bob@example.com", JobType.FULL_TIME, EmploymentStatus.ON_LEAVE));
		repository.save(new Employee("Charlie", "charlie@example.com", JobType.PART_TIME, EmploymentStatus.ACTIVE));

		// When: Query by job type AND status
		List<Employee> result = repository.findByJobTypeAndStatus(JobType.FULL_TIME, EmploymentStatus.ACTIVE);

		// Then: Should find only matching combination
		assertThat(result).hasSize(1);
		assertThat(result.get(0).getName()).isEqualTo("Alice");
	}

	/**
	 * Test IN query with collection of enum values.
	 */
	@Test
	void shouldFindEmployeesByJobTypeIn() {
		// Given: Create employees with various job types
		repository.save(new Employee("Alice", "alice@example.com", JobType.FULL_TIME, EmploymentStatus.ACTIVE));
		repository.save(new Employee("Bob", "bob@example.com", JobType.PART_TIME, EmploymentStatus.ACTIVE));
		repository.save(new Employee("Charlie", "charlie@example.com", JobType.CONTRACT, EmploymentStatus.ACTIVE));
		repository.save(new Employee("David", "david@example.com", JobType.INTERN, EmploymentStatus.ACTIVE));

		// When: Query with collection of job types
		List<Employee> result = repository.findByJobTypeIn(Arrays.asList(JobType.FULL_TIME, JobType.CONTRACT));

		// Then: Should find employees with either job type
		assertThat(result).hasSize(2);
		assertThat(result).extracting(Employee::getName).containsExactlyInAnyOrder("Alice", "Charlie");
	}

	/**
	 * Test count query with enum parameter.
	 */
	@Test
	void shouldCountEmployeesByJobType() {
		// Given: Create employees
		repository.save(new Employee("Alice", "alice@example.com", JobType.FULL_TIME, EmploymentStatus.ACTIVE));
		repository.save(new Employee("Bob", "bob@example.com", JobType.FULL_TIME, EmploymentStatus.ACTIVE));
		repository.save(new Employee("Charlie", "charlie@example.com", JobType.PART_TIME, EmploymentStatus.ACTIVE));

		// When: Count by job type
		long fullTimeCount = repository.countByJobType(JobType.FULL_TIME);
		long partTimeCount = repository.countByJobType(JobType.PART_TIME);

		// Then: Should return correct counts
		assertThat(fullTimeCount).isEqualTo(2);
		assertThat(partTimeCount).isEqualTo(1);
	}

	/**
	 * Test exists query with enum parameter.
	 */
	@Test
	void shouldCheckExistenceByStatus() {
		// Given: Create employee with specific status
		repository.save(new Employee("Alice", "alice@example.com", JobType.FULL_TIME, EmploymentStatus.ACTIVE));

		// When: Check existence
		boolean activeExists = repository.existsByStatus(EmploymentStatus.ACTIVE);
		boolean retiredExists = repository.existsByStatus(EmploymentStatus.RETIRED);

		// Then: Should return correct existence status
		assertThat(activeExists).isTrue();
		assertThat(retiredExists).isFalse();
	}

	/**
	 * Test custom @Query method with enum parameter.
	 */
	@Test
	void shouldFindEmployeesUsingCustomQueryWithEnum() {
		// Given: Create employees
		repository.save(new Employee("Alice", "alice@example.com", JobType.FULL_TIME, EmploymentStatus.ACTIVE));
		repository.save(new Employee("Bob", "bob@example.com", JobType.PART_TIME, EmploymentStatus.ACTIVE));

		// When: Use custom query with enum parameter
		List<Employee> result = repository.findEmployeesByJobType(JobType.FULL_TIME);

		// Then: Should find matching employees
		assertThat(result).hasSize(1);
		assertThat(result.get(0).getName()).isEqualTo("Alice");
	}

	/**
	 * Test custom @Query method with multiple parameters including enums.
	 */
	@Test
	void shouldFindEmployeesByStatusAndDepartment() {
		// Given: Create employees
		Employee emp1 = new Employee("Alice", "alice@example.com", JobType.FULL_TIME, EmploymentStatus.ACTIVE);
		emp1.setDepartment("Engineering");
		repository.save(emp1);

		Employee emp2 = new Employee("Bob", "bob@example.com", JobType.FULL_TIME, EmploymentStatus.ACTIVE);
		emp2.setDepartment("Sales");
		repository.save(emp2);

		Employee emp3 = new Employee("Charlie", "charlie@example.com", JobType.FULL_TIME, EmploymentStatus.ON_LEAVE);
		emp3.setDepartment("Engineering");
		repository.save(emp3);

		// When: Query with enum and string parameters
		List<Employee> result = repository.findByStatusAndDepartment(EmploymentStatus.ACTIVE, "Engineering");

		// Then: Should find only matching combination
		assertThat(result).hasSize(1);
		assertThat(result.get(0).getName()).isEqualTo("Alice");
	}

	/**
	 * Test count query using custom @Query with enum.
	 */
	@Test
	void shouldCountEmployeesUsingCustomQueryWithEnum() {
		// Given: Create employees
		repository.save(new Employee("Alice", "alice@example.com", JobType.FULL_TIME, EmploymentStatus.ACTIVE));
		repository.save(new Employee("Bob", "bob@example.com", JobType.FULL_TIME, EmploymentStatus.ACTIVE));
		repository.save(new Employee("Charlie", "charlie@example.com", JobType.PART_TIME, EmploymentStatus.ACTIVE));

		// When: Count using custom query
		long count = repository.countEmployeesByJobType(JobType.FULL_TIME);

		// Then: Should return correct count
		assertThat(count).isEqualTo(2);
	}

	/**
	 * Test @Query with collection of enums.
	 */
	@Test
	void shouldFindEmployeesWithMultipleJobTypesUsingCustomQuery() {
		// Given: Create employees
		repository.save(new Employee("Alice", "alice@example.com", JobType.FULL_TIME, EmploymentStatus.ACTIVE));
		repository.save(new Employee("Bob", "bob@example.com", JobType.PART_TIME, EmploymentStatus.ACTIVE));
		repository.save(new Employee("Charlie", "charlie@example.com", JobType.CONTRACT, EmploymentStatus.ACTIVE));

		// When: Query with list of enums
		List<Employee> result = repository.findByMultipleJobTypes(Arrays.asList(JobType.FULL_TIME, JobType.CONTRACT));

		// Then: Should find matching employees
		assertThat(result).hasSize(2);
		assertThat(result).extracting(Employee::getName).containsExactlyInAnyOrder("Alice", "Charlie");
	}

	/**
	 * Test that enum values are stored as strings in the database.
	 * This verifies the write path: Enum -> String using enum.name()
	 */
	@Test
	void shouldStoreEnumAsStringInDatabase() {
		// Given: Create and save an employee
		Employee employee = new Employee("Alice", "alice@example.com", JobType.FULL_TIME, EmploymentStatus.ACTIVE);
		Employee saved = repository.save(employee);

		// When: Query the database directly for the stored value
		String storedJobType = template.query(
				"MATCH (e:Employee) WHERE id(e) = $id RETURN e.jobType as jobType",
				java.util.Map.of("id", saved.getId()),
				result -> {
					for (FalkorDBClient.Record record : result.records()) {
						return (String) record.get("jobType");
					}
					return null;
				});

		// Then: Should be stored as the enum name string
		assertThat(storedJobType).isEqualTo("FULL_TIME");
	}

	/**
	 * Test derived query combining enum with string parameter.
	 */
	@Test
	void shouldFindByNameAndJobType() {
		// Given: Create employees
		repository.save(new Employee("Alice", "alice@example.com", JobType.FULL_TIME, EmploymentStatus.ACTIVE));
		repository.save(new Employee("Alice", "alice2@example.com", JobType.PART_TIME, EmploymentStatus.ACTIVE));

		// When: Query by name and job type
		Optional<Employee> result = repository.findByNameAndJobType("Alice", JobType.FULL_TIME);

		// Then: Should find the correct employee
		assertThat(result).isPresent();
		assertThat(result.get().getEmail()).isEqualTo("alice@example.com");
	}

	/**
	 * Test handling of all enum values.
	 */
	@Test
	void shouldHandleAllJobTypeValues() {
		// Given: Create employees with all job types
		repository.save(new Employee("Alice", "alice@example.com", JobType.FULL_TIME, EmploymentStatus.ACTIVE));
		repository.save(new Employee("Bob", "bob@example.com", JobType.PART_TIME, EmploymentStatus.ACTIVE));
		repository.save(new Employee("Charlie", "charlie@example.com", JobType.CONTRACT, EmploymentStatus.ACTIVE));
		repository.save(new Employee("David", "david@example.com", JobType.INTERN, EmploymentStatus.ACTIVE));
		repository.save(new Employee("Eve", "eve@example.com", JobType.FREELANCE, EmploymentStatus.ACTIVE));

		// Then: Should be able to query each type
		assertThat(repository.findByJobType(JobType.FULL_TIME)).hasSize(1);
		assertThat(repository.findByJobType(JobType.PART_TIME)).hasSize(1);
		assertThat(repository.findByJobType(JobType.CONTRACT)).hasSize(1);
		assertThat(repository.findByJobType(JobType.INTERN)).hasSize(1);
		assertThat(repository.findByJobType(JobType.FREELANCE)).hasSize(1);
	}

	/**
	 * Configuration for the test context.
	 */
	@Configuration
	@EnableFalkorDBRepositories(basePackageClasses = EmployeeRepository.class)
	static class TestConfig {

		@Bean
		public Driver falkorDBDriver() {
			return new DriverImpl("localhost", 6379);
		}

		@Bean
		public FalkorDBClient falkorDBClient(Driver driver) {
			return new DefaultFalkorDBClient(driver, "test_enum_mapping");
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
