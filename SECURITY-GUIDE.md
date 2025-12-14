# FalkorDB RBAC Security Guide

Comprehensive guide for implementing Role-Based Access Control (RBAC) in Spring Data FalkorDB applications.

## Table of Contents

- [Overview](#overview)
- [Quick Start](#quick-start)
- [Security Models](#security-models)
- [Annotations](#annotations)
- [RBAC Manager API](#rbac-manager-api)
- [Security Context](#security-context)
- [Examples](#examples)
- [Configuration](#configuration)
- [Best Practices](#best-practices)

## Overview

Spring Data FalkorDB's RBAC implementation provides comprehensive security features including:

- **Entity-level security**: Control READ, WRITE, CREATE, and DELETE operations
- **Property-level security**: Deny access to specific entity properties per role
- **Row-level security**: Filter entities based on SpEL expressions
- **Audit logging**: Track all security decisions
- **Role hierarchy**: Support for role inheritance
- **Admin API**: Complete management interface for users, roles, and privileges

## Quick Start

### 1. Enable Security in Spring Boot

Add to `application.properties`:

```properties
spring.data.falkordb.security.enabled=true
spring.data.falkordb.security.admin-role=admin
spring.data.falkordb.security.audit-enabled=true
```

### 2. Define a Secured Entity

```java
@Node("Document")
@Secured(
    read = {"reader", "admin"},
    write = {"editor", "admin"},
    create = {"editor", "admin"},
    delete = {"admin"},
    denyReadProperties = {
        @DenyProperty(property = "internalNotes", forRoles = {"reader"})
    }
)
public class Document {
    @Id @GeneratedValue
    private Long id;
    private String title;
    private String content;
    private String internalNotes;
    private String owner;
    
    // Getters and setters...
}
```

### 3. Use Secure Repository

```java
@Autowired
private FalkorDBSecuritySession securitySession;

@Autowired
private FalkorDBTemplate template;

public void accessDocument(String username, Long documentId) {
    // Load security context for user
    try (var scope = securitySession.runAs(username)) {
        // Create secure repository
        FalkorDBEntityInformation<Document, Long> entityInfo = 
            new MappingFalkorDBEntityInformation<>(Document.class, 
                template.getConverter().getMappingContext());
        SecureFalkorDBRepository<Document, Long> repo = 
            new SecureFalkorDBRepository<>(template, entityInfo, null);
        
        // Operations are now secured
        Optional<Document> doc = repo.findById(documentId);
    }
}
```

## Security Models

### User

Represents an authenticated user in the system.

```java
@Node("_Security_User")
public class User {
    private Long id;
    private String username;      // Unique username
    private String email;
    private boolean active;       // Enable/disable user
    private Instant createdAt;
    
    @Relationship(type = "HAS_ROLE", direction = OUTGOING)
    private Set<Role> roles;
}
```

### Role

Represents a security role with privileges.

```java
@Node("_Security_Role")
public class Role {
    private Long id;
    private String name;          // Unique role name
    private String description;
    private boolean immutable;    // Prevent modification
    private Instant createdAt;
    
    @Relationship(type = "INHERITS_FROM", direction = OUTGOING)
    private Set<Role> parentRoles; // Support role hierarchy
}
```

### Privilege

Represents a permission granted or denied to a role.

```java
@Node("_Security_Privilege")
public class Privilege {
    private Long id;
    private Action action;              // READ, WRITE, CREATE, DELETE
    private ResourceType resourceType;  // NODE, RELATIONSHIP, PROPERTY
    private String resourceLabel;       // Entity name (e.g., "Document")
    private String resourceProperty;    // Property name (optional)
    private boolean grant;              // true = GRANT, false = DENY
    private Instant createdAt;
    
    @Relationship(type = "GRANTED_TO", direction = OUTGOING)
    private Role role;
}
```

### AuditLog

Records security decisions for compliance and debugging.

```java
@Node("_Security_AuditLog")
public class AuditLog {
    private Long id;
    private Instant timestamp;
    private String username;
    private String action;
    private String resource;
    private boolean granted;       // Was access granted?
    private String reason;         // Why was access denied?
    private String ipAddress;
}
```

## Annotations

### @Secured

Entity-level security annotation.

```java
@Node("Person")
@Secured(
    read = {"reader", "admin"},          // Who can read
    write = {"editor", "admin"},         // Who can update
    create = {"editor", "admin"},        // Who can create
    delete = {"admin"},                  // Who can delete
    denyReadProperties = {               // Property-level read denials
        @DenyProperty(property = "ssn", forRoles = {"*"}),
        @DenyProperty(property = "salary", forRoles = {"reader"})
    },
    denyWriteProperties = {              // Property-level write denials
        @DenyProperty(property = "id", forRoles = {"*"})
    }
)
public class Person {
    // ...
}
```

### @DenyProperty

Deny access to specific properties.

```java
@DenyProperty(property = "ssn", forRoles = {"*"})      // Deny to all roles
@DenyProperty(property = "salary", forRoles = {"reader", "analyst"})
```

### @RowLevelSecurity

Filter entities using SpEL expressions.

```java
@Node("Document")
@RowLevelSecurity(filter = "owner == principal.username")
public class Document {
    private String owner;
    // Only returns documents where owner matches current user
}
```

Available SpEL variables:
- `principal` - Current security context user
- `entity` - The entity being checked

## RBAC Manager API

The `RBACManager` provides administrative operations (requires admin role).

### User Management

```java
@Autowired
private RBACManager rbacManager;

// Create user
User user = rbacManager.createUser("alice", "alice@example.com", 
    Arrays.asList("reader"));

// Update user
rbacManager.updateUser("alice", "alice.smith@example.com", true);

// List users
List<User> users = rbacManager.listUsers(true); // active only

// Get user
User alice = rbacManager.getUser("alice");

// Delete user
rbacManager.deleteUser("alice");
```

### Role Management

```java
// Create role
Role analyst = rbacManager.createRole("analyst", "Data Analyst", 
    Arrays.asList("reader"), // Parent roles
    false);                  // Not immutable

// Update role
rbacManager.updateRole("analyst", "Senior Analyst", 
    Arrays.asList("reader", "reporter"));

// List roles
List<Role> roles = rbacManager.listRoles();

// Get role
Role role = rbacManager.getRole("analyst");

// Delete role
rbacManager.deleteRole("analyst");
```

### Role Assignment

```java
// Assign role to user
rbacManager.assignRole("alice", "analyst");

// Revoke role from user
rbacManager.revokeRole("alice", "reader");

// Get user's effective roles (includes inherited)
List<String> roles = rbacManager.getUserRoles("alice");
```

### Privilege Management

```java
// Grant privilege
Privilege p = rbacManager.grantPrivilege("analyst", 
    Action.READ, ResourceType.NODE, "Document", null);

// Grant property-level privilege
rbacManager.grantPrivilege("analyst",
    Action.READ, ResourceType.PROPERTY, "Document", "summary");

// Deny privilege
rbacManager.denyPrivilege("reader", 
    Action.DELETE, ResourceType.NODE, "Document", null);

// List role privileges
List<Privilege> privileges = rbacManager.listPrivileges("analyst");

// Revoke privilege
rbacManager.revokePrivilege(privilegeId);

// Revoke specific privilege
rbacManager.revokePrivilege("analyst", Action.READ, 
    ResourceType.NODE, "Document", null);
```

### Audit Queries

```java
// Query audit logs
List<AuditLog> logs = rbacManager.queryAuditLogs(
    "alice",           // username (null for all)
    "READ",            // action (null for all)
    startInstant,      // start date
    endInstant,        // end date
    false,             // granted (null for all)
    100);              // limit

// Delete old audit logs
rbacManager.deleteAuditLogsOlderThan(Instant.now().minus(90, ChronoUnit.DAYS));
```

## Security Context

### FalkorSecurityContext

Thread-local security context holding user and permissions.

```java
FalkorSecurityContext ctx = FalkorSecurityContextHolder.getContext();

// Check permissions
boolean canRead = ctx.can(Action.READ, "Document");
boolean canWriteProperty = ctx.can(Action.WRITE, "Document.title");

// Check roles
boolean isAdmin = ctx.hasRole("admin");
Set<String> roles = ctx.getEffectiveRoles();

// Get user
User user = ctx.getUser();
```

### Using Context Scopes

```java
@Autowired
private FalkorDBSecuritySession session;

// Run code as specific user
try (var scope = session.runAs("alice")) {
    // All operations here use alice's permissions
    repository.findAll();
}

// Admin impersonation
try (var scope = session.impersonate("alice")) {
    // Admin testing alice's view
}
```

## Examples

See complete examples in:
- `BasicSecurityExample.java` - Entity-level and property-level security
- `RBACManagerExample.java` - Admin API usage

## Configuration

### application.properties

```properties
# Enable RBAC security
spring.data.falkordb.security.enabled=true

# Admin role name
spring.data.falkordb.security.admin-role=admin

# Default role for all users
spring.data.falkordb.security.default-role=PUBLIC

# Enable audit logging
spring.data.falkordb.security.audit-enabled=true

# Enable query rewriting for row-level security
spring.data.falkordb.security.query-rewrite-enabled=false
```

### Programmatic Configuration

```java
@Configuration
public class SecurityConfig {
    
    @Bean
    public RBACManager rbacManager(FalkorDBTemplate template) {
        return new RBACManager(template, "admin");
    }
    
    @Bean
    public FalkorDBSecuritySession securitySession(FalkorDBTemplate template) {
        return new FalkorDBSecuritySession(template, "admin", "PUBLIC");
    }
}
```

## Best Practices

### 1. Role Hierarchy

Use role inheritance to avoid duplication:

```java
// editor inherits from reader
rbacManager.createRole("editor", "Editor", Arrays.asList("reader"), false);

// No need to grant READ to editor - inherited from reader
```

### 2. Principle of Least Privilege

Grant minimum permissions needed:

```java
@Secured(
    read = {"viewer"},      // Most restrictive
    write = {"editor"},
    delete = {"admin"}      // Only admin can delete
)
```

### 3. Use DENY for Sensitive Data

```java
@DenyProperty(property = "ssn", forRoles = {"*"})  // Deny to everyone
@DenyProperty(property = "salary", forRoles = {"viewer", "reader"})
```

### 4. Immutable Core Roles

Protect system roles from modification:

```java
Role admin = rbacManager.createRole("admin", "Administrator", 
    null, true); // immutable = true
```

### 5. Regular Audit Reviews

```java
// Review failed access attempts
List<AuditLog> denied = rbacManager.queryAuditLogs(
    null, null, startDate, endDate, false, 1000);
```

### 6. Test Security with Different Users

```java
@Test
public void testReaderCannotDelete() {
    try (var scope = session.runAs("reader_user")) {
        assertThrows(AccessDeniedException.class, 
            () -> repository.deleteById(docId));
    }
}
```

### 7. Clean Up Old Audit Logs

```java
@Scheduled(cron = "0 0 2 * * *") // Daily at 2 AM
public void cleanupAuditLogs() {
    Instant cutoff = Instant.now().minus(90, ChronoUnit.DAYS);
    rbacManager.deleteAuditLogsOlderThan(cutoff);
}
```

## Performance Considerations

1. **Privilege Caching**: Security context caches privilege lookups
2. **Query Rewriting**: Enable only if needed for `count()`/`exists()`
3. **Audit Logging**: Consider async logging for high-throughput systems
4. **Role Hierarchy**: Compute at context creation, not per-check

## Troubleshooting

### "Access denied" errors

1. Check user has required role:
```java
List<String> roles = rbacManager.getUserRoles("username");
```

2. Verify privileges are granted:
```java
List<Privilege> privileges = rbacManager.listPrivileges("rolename");
```

3. Review audit logs:
```java
List<AuditLog> logs = rbacManager.queryAuditLogs("username", null, null, null, false, 10);
```

### Row-level security not working

1. Ensure `@RowLevelSecurity` annotation is present
2. Check SpEL expression syntax
3. Verify entity has required properties
4. Enable query rewriting if using `count()`/`exists()`

## Migration Guide

### From Unsecured to Secured

1. Add security annotations to entities
2. Create roles and users
3. Grant privileges
4. Replace repositories with secure versions
5. Test thoroughly with different users

## Additional Resources

- [Python ORM RBAC Documentation](https://github.com/FalkorDB/falkordb-py-orm)
- [Spring Security Documentation](https://docs.spring.io/spring-security/reference/)
- [FalkorDB Documentation](https://docs.falkordb.com/)
