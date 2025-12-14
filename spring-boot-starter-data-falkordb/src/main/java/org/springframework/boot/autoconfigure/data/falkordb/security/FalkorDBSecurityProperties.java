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

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for FalkorDB RBAC security features.
 *
 * @author FalkorDB Team
 * @since 1.0
 */
@ConfigurationProperties(prefix = "spring.data.falkordb.security")
public class FalkorDBSecurityProperties {

	/**
	 * Whether to enable RBAC security features.
	 */
	private boolean enabled = false;

	/**
	 * Name of the admin role required for RBAC management operations.
	 */
	private String adminRole = "admin";

	/**
	 * Default role assigned to all users.
	 */
	private String defaultRole = "PUBLIC";

	/**
	 * Whether to enable audit logging of security events.
	 */
	private boolean auditEnabled = true;

	/**
	 * Whether to enable query rewriting for row-level security.
	 */
	private boolean queryRewriteEnabled = false;

	public boolean isEnabled() {
		return this.enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public String getAdminRole() {
		return this.adminRole;
	}

	public void setAdminRole(String adminRole) {
		this.adminRole = adminRole;
	}

	public String getDefaultRole() {
		return this.defaultRole;
	}

	public void setDefaultRole(String defaultRole) {
		this.defaultRole = defaultRole;
	}

	public boolean isAuditEnabled() {
		return this.auditEnabled;
	}

	public void setAuditEnabled(boolean auditEnabled) {
		this.auditEnabled = auditEnabled;
	}

	public boolean isQueryRewriteEnabled() {
		return this.queryRewriteEnabled;
	}

	public void setQueryRewriteEnabled(boolean queryRewriteEnabled) {
		this.queryRewriteEnabled = queryRewriteEnabled;
	}

}
