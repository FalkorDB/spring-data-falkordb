/*
 * Copyright (c) 2025 FalkorDB Ltd.
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

package org.springframework.boot.autoconfigure.data.falkordb.security;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.falkordb.core.FalkorDBTemplate;
import org.springframework.data.falkordb.security.audit.AuditLogger;
import org.springframework.data.falkordb.security.manager.RBACManager;
import org.springframework.data.falkordb.security.session.FalkorDBSecuritySession;

/**
 * Auto-configuration for FalkorDB RBAC security features.
 *
 * @author FalkorDB Team
 * @since 1.0
 */
@AutoConfiguration
@ConditionalOnClass(FalkorDBTemplate.class)
@ConditionalOnProperty(prefix = "spring.data.falkordb.security", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(FalkorDBSecurityProperties.class)
public class FalkorDBSecurityAutoConfiguration {

	/**
	 * Creates an AuditLogger bean if audit logging is enabled.
	 *
	 * @param template the FalkorDB template
	 * @param properties security properties
	 * @return audit logger instance
	 */
	@Bean
	@ConditionalOnProperty(prefix = "spring.data.falkordb.security", name = "audit-enabled", havingValue = "true",
			matchIfMissing = true)
	public AuditLogger auditLogger(FalkorDBTemplate template, FalkorDBSecurityProperties properties) {
		return new AuditLogger(template);
	}

	/**
	 * Creates an RBACManager bean for admin operations.
	 *
	 * @param template the FalkorDB template
	 * @param properties security properties
	 * @return RBAC manager instance
	 */
	@Bean
	public RBACManager rbacManager(FalkorDBTemplate template, FalkorDBSecurityProperties properties) {
		return new RBACManager(template, properties.getAdminRole());
	}

	/**
	 * Creates a FalkorDBSecuritySession bean for context management.
	 *
	 * @param template the FalkorDB template
	 * @param properties security properties
	 * @return security session instance
	 */
	@Bean
	public FalkorDBSecuritySession falkorDBSecuritySession(FalkorDBTemplate template,
			FalkorDBSecurityProperties properties) {
		return new FalkorDBSecuritySession(template, properties.getAdminRole(), properties.getDefaultRole());
	}

}
