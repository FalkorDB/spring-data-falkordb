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

import org.springframework.data.falkordb.core.schema.GeneratedValue;
import org.springframework.data.falkordb.core.schema.Id;
import org.springframework.data.falkordb.core.schema.Node;

/**
 * Example entity demonstrating enum property mapping in Spring Data FalkorDB.
 * 
 * <p>This entity shows how Java enum types are automatically converted to/from strings
 * when persisting to FalkorDB, providing compatibility with Spring Data Neo4j.</p>
 * 
 * <p><b>Key points:</b></p>
 * <ul>
 *   <li>Properties use the enum type directly (e.g., {@code JobType}, not {@code String})</li>
 *   <li>When saving: {@code JobType.FULL_TIME} → stored as string {@code "FULL_TIME"}</li>
 *   <li>When reading: string {@code "FULL_TIME"} → converted to {@code JobType.FULL_TIME}</li>
 *   <li>Works seamlessly with derived queries and @Query methods</li>
 * </ul>
 *
 * @author FalkorDB
 * @since 1.0
 */
@Node("Employee")
public class Employee {

	@Id
	@GeneratedValue
	private Long id;

	private String name;

	private String email;

	/**
	 * Employment type enum - stored as string in FalkorDB.
	 * Example: JobType.FULL_TIME is stored as "FULL_TIME"
	 */
	private JobType jobType;

	/**
	 * Employment status enum - stored as string in FalkorDB.
	 * Example: EmploymentStatus.ACTIVE is stored as "ACTIVE"
	 */
	private EmploymentStatus status;

	private Double salary;

	private String department;

	// Constructors

	public Employee() {
	}

	public Employee(String name, String email, JobType jobType, EmploymentStatus status) {
		this.name = name;
		this.email = email;
		this.jobType = jobType;
		this.status = status;
	}

	public Employee(String name, String email, JobType jobType, EmploymentStatus status, Double salary,
			String department) {
		this.name = name;
		this.email = email;
		this.jobType = jobType;
		this.status = status;
		this.salary = salary;
		this.department = department;
	}

	// Getters and setters

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public JobType getJobType() {
		return jobType;
	}

	public void setJobType(JobType jobType) {
		this.jobType = jobType;
	}

	public EmploymentStatus getStatus() {
		return status;
	}

	public void setStatus(EmploymentStatus status) {
		this.status = status;
	}

	public Double getSalary() {
		return salary;
	}

	public void setSalary(Double salary) {
		this.salary = salary;
	}

	public String getDepartment() {
		return department;
	}

	public void setDepartment(String department) {
		this.department = department;
	}

	@Override
	public String toString() {
		return "Employee{" + "id=" + id + ", name='" + name + '\'' + ", email='" + email + '\'' + ", jobType="
				+ jobType + ", status=" + status + ", salary=" + salary + ", department='" + department + '\'' + '}';
	}

}
