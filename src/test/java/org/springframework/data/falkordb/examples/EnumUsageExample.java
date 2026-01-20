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

import java.util.Arrays;
import java.util.List;

import org.springframework.data.falkordb.repository.FalkorDBRepository;
import org.springframework.data.falkordb.repository.query.Query;
import org.springframework.data.repository.query.Param;

/**
 * Example demonstrating enum usage in Spring Data FalkorDB.
 * 
 * <p>This example shows how Java enum types are automatically converted to/from strings
 * when persisting to FalkorDB, providing full compatibility with Spring Data Neo4j's
 * enum handling approach.</p>
 * 
 * <h2>Key Features</h2>
 * <ul>
 *   <li><b>Automatic Conversion</b>: Enums are stored as strings using {@code enum.name()}</li>
 *   <li><b>Seamless Queries</b>: Use enum types directly in repository methods</li>
 *   <li><b>Type Safety</b>: Full compile-time type checking for enum values</li>
 *   <li><b>Neo4j Compatible</b>: Works identically to Spring Data Neo4j</li>
 * </ul>
 * 
 * <h2>How It Works</h2>
 * <p><b>Writing (Enum → String):</b> When saving an entity, enum values are converted
 * to their string representation using {@code enum.name()}. For example:</p>
 * <pre>
 * JobType.FULL_TIME → stored as "FULL_TIME" in FalkorDB
 * </pre>
 * 
 * <p><b>Reading (String → Enum):</b> When reading from the database, strings are
 * automatically converted back to enum instances using {@code Enum.valueOf()}:</p>
 * <pre>
 * "FULL_TIME" from database → JobType.FULL_TIME in Java
 * </pre>
 * 
 * <h2>Usage Examples</h2>
 * 
 * <h3>1. Entity Definition</h3>
 * <p>Define your entity with enum properties - no special annotations needed:</p>
 * <pre>{@code
 * @Node("User")
 * public class User {
 *     @Id @GeneratedValue
 *     private Long id;
 *     
 *     private String name;
 *     
 *     // Enum property - stored as string "FULL_TIME", "PART_TIME", etc.
 *     private JobType jobType;
 *     
 *     // Multiple enum properties work perfectly
 *     private EmploymentStatus status;
 * }
 * }</pre>
 * 
 * <h3>2. Repository Methods</h3>
 * <p>Use enum parameters directly in derived query methods:</p>
 * <pre>{@code
 * public interface UserRepository extends FalkorDBRepository<User, Long> {
 *     // Derived query with enum parameter
 *     List<User> findByJobType(JobType jobType);
 *     
 *     // Multiple enum parameters
 *     List<User> findByJobTypeAndStatus(JobType jobType, EmploymentStatus status);
 *     
 *     // Collection of enums
 *     List<User> findByJobTypeIn(Collection<JobType> jobTypes);
 *     
 *     // Count with enum
 *     long countByJobType(JobType jobType);
 *     
 *     // Exists check
 *     boolean existsByStatus(EmploymentStatus status);
 * }
 * }</pre>
 * 
 * <h3>3. Custom Queries</h3>
 * <p>Enum parameters work seamlessly in {@code @Query} methods:</p>
 * <pre>{@code
 * public interface UserRepository extends FalkorDBRepository<User, Long> {
 *     // Single enum parameter
 *     @Query("MATCH (u:User) WHERE u.jobType = $jobType RETURN u")
 *     List<User> findByJobTypeCustom(@Param("jobType") JobType jobType);
 *     
 *     // Multiple parameters including enums
 *     @Query("MATCH (u:User) WHERE u.status = $status AND u.department = $dept RETURN u")
 *     List<User> findByStatusAndDept(@Param("status") EmploymentStatus status, 
 *                                     @Param("dept") String department);
 *     
 *     // Collection of enums
 *     @Query("MATCH (u:User) WHERE u.jobType IN $types RETURN u")
 *     List<User> findByTypes(@Param("types") List<JobType> types);
 * }
 * }</pre>
 * 
 * <h3>4. Service Layer Usage</h3>
 * <p>Using enums in your service is natural and type-safe:</p>
 * <pre>{@code
 * @Service
 * public class UserService {
 *     @Autowired
 *     private UserRepository repository;
 *     
 *     public void exampleUsage() {
 *         // Create and save with enums
 *         User user = new User();
 *         user.setName("John Doe");
 *         user.setJobType(JobType.FULL_TIME);  // Type-safe enum assignment
 *         user.setStatus(EmploymentStatus.ACTIVE);
 *         repository.save(user);
 *         
 *         // Query with enum parameters
 *         List<User> fullTime = repository.findByJobType(JobType.FULL_TIME);
 *         
 *         // Query with multiple types
 *         List<User> employees = repository.findByJobTypeIn(
 *             Arrays.asList(JobType.FULL_TIME, JobType.PART_TIME)
 *         );
 *         
 *         // Count by status
 *         long activeCount = repository.countByJobType(JobType.FULL_TIME);
 *     }
 * }
 * }</pre>
 * 
 * <h2>Best Practices</h2>
 * <ul>
 *   <li><b>Use enums for fixed sets of values</b>: Job types, statuses, priorities, categories</li>
 *   <li><b>Avoid using @Interned with enums</b>: Enums are already stored efficiently as strings</li>
 *   <li><b>Keep enum names consistent</b>: Use UPPER_CASE for enum constants (standard Java convention)</li>
 *   <li><b>Consider @Interned for enum-like strings</b>: If you can't use enums, use @Interned annotation</li>
 *   <li><b>Handle null values</b>: Make enum fields nullable if they're optional</li>
 * </ul>
 * 
 * <h2>When to Use Enums vs @Interned Strings</h2>
 * <table border="1">
 *   <tr><th>Use Enums When:</th><th>Use @Interned Strings When:</th></tr>
 *   <tr>
 *     <td>- Values are known at compile time<br/>
 *         - You want type safety<br/>
 *         - Values won't change frequently<br/>
 *         - Need IDE autocomplete support</td>
 *     <td>- Values are dynamic or user-defined<br/>
 *         - Values come from external sources<br/>
 *         - Need flexibility to add values<br/>
 *         - Legacy data uses strings</td>
 *   </tr>
 * </table>
 * 
 * <h2>Complete Example</h2>
 * <p>See the {@link Employee} entity, {@link EmployeeRepository} interface, and
 * {@link org.springframework.data.falkordb.integration.EnumMappingIntegrationTest} 
 * for complete working examples with comprehensive test coverage.</p>
 *
 * @author FalkorDB
 * @since 1.0
 * @see Employee
 * @see EmployeeRepository
 * @see JobType
 * @see EmploymentStatus
 */
public class EnumUsageExample {

	// This is a documentation-only class
	// See Employee entity and EmployeeRepository for actual implementation

	/**
	 * Example repository demonstrating all enum query patterns.
	 */
	public interface ExampleRepository extends FalkorDBRepository<Employee, Long> {

		// Derived queries
		List<Employee> findByJobType(JobType jobType);

		List<Employee> findByStatus(EmploymentStatus status);

		List<Employee> findByJobTypeAndStatus(JobType jobType, EmploymentStatus status);

		List<Employee> findByJobTypeIn(List<JobType> jobTypes);

		long countByJobType(JobType jobType);

		boolean existsByStatus(EmploymentStatus status);

		// Custom queries
		@Query("MATCH (e:Employee) WHERE e.jobType = $type RETURN e")
		List<Employee> customQuery(@Param("type") JobType jobType);

	}

}
