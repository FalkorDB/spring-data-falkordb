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

package org.springframework.data.falkordb.examples;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.falkordb.repository.FalkorDBRepository;
import org.springframework.data.falkordb.repository.query.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repository interface demonstrating enum support in Spring Data FalkorDB.
 * 
 * <p>This repository shows how enum parameters work seamlessly in both
 * derived query methods and custom @Query methods, providing full compatibility
 * with Spring Data Neo4j's enum handling.</p>
 *
 * @author FalkorDB
 * @since 1.0
 */
public interface EmployeeRepository extends FalkorDBRepository<Employee, Long> {

	// Derived query methods with enum parameters

	/**
	 * Find employees by job type.
	 * The enum parameter is automatically converted to its string representation.
	 * @param jobType the job type to search for
	 * @return list of employees with the specified job type
	 */
	List<Employee> findByJobType(JobType jobType);

	/**
	 * Find employees by employment status.
	 * @param status the employment status
	 * @return list of employees with the specified status
	 */
	List<Employee> findByStatus(EmploymentStatus status);

	/**
	 * Find employees by job type and status.
	 * Both enum parameters are handled automatically.
	 * @param jobType the job type
	 * @param status the employment status
	 * @return list of matching employees
	 */
	List<Employee> findByJobTypeAndStatus(JobType jobType, EmploymentStatus status);

	/**
	 * Find employees with job type in a collection.
	 * @param jobTypes collection of job types to match
	 * @return list of employees with any of the specified job types
	 */
	List<Employee> findByJobTypeIn(Collection<JobType> jobTypes);

	/**
	 * Find a single employee by name and job type.
	 * @param name the employee name
	 * @param jobType the job type
	 * @return optional employee
	 */
	Optional<Employee> findByNameAndJobType(String name, JobType jobType);

	/**
	 * Count employees by job type.
	 * @param jobType the job type to count
	 * @return number of employees with the specified job type
	 */
	long countByJobType(JobType jobType);

	/**
	 * Check if any employee exists with the given status.
	 * @param status the employment status
	 * @return true if at least one employee has the specified status
	 */
	boolean existsByStatus(EmploymentStatus status);

	// Custom @Query methods with enum parameters

	/**
	 * Find employees by job type using a custom Cypher query.
	 * The enum parameter is automatically converted to string.
	 * @param jobType the job type to search for
	 * @return list of employees
	 */
	@Query("MATCH (e:Employee) WHERE e.jobType = $jobType RETURN e")
	List<Employee> findEmployeesByJobType(@Param("jobType") JobType jobType);

	/**
	 * Find employees by status and department using custom query.
	 * @param status the employment status
	 * @param department the department name
	 * @return list of matching employees
	 */
	@Query("MATCH (e:Employee) WHERE e.status = $status AND e.department = $department RETURN e")
	List<Employee> findByStatusAndDepartment(@Param("status") EmploymentStatus status,
			@Param("department") String department);

	/**
	 * Count employees by job type using custom query.
	 * @param jobType the job type
	 * @return count of employees
	 */
	@Query(value = "MATCH (e:Employee) WHERE e.jobType = $jobType RETURN count(e)", count = true)
	long countEmployeesByJobType(@Param("jobType") JobType jobType);

	/**
	 * Find employees with multiple job types using custom query.
	 * @param jobTypes list of job types
	 * @return list of employees
	 */
	@Query("MATCH (e:Employee) WHERE e.jobType IN $jobTypes RETURN e")
	List<Employee> findByMultipleJobTypes(@Param("jobTypes") List<JobType> jobTypes);

}
