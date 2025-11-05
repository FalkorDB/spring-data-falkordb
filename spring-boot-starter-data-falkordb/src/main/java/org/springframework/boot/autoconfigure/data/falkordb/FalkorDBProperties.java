package org.springframework.boot.autoconfigure.data.falkordb;

import java.time.Duration;

import jakarta.validation.constraints.NotBlank;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.convert.DurationUnit;
import org.springframework.validation.annotation.Validated;

import java.time.temporal.ChronoUnit;

/**
 * Configuration properties for FalkorDB.
 *
 * @author Shahar Biron
 * @since 1.0
 */
@ConfigurationProperties(prefix = "spring.data.falkordb")
@Validated
public class FalkorDBProperties {

	/**
	 * FalkorDB server URI. Default is falkordb://localhost:6379
	 */
	private String uri = "falkordb://localhost:6379";

	/**
	 * FalkorDB database name (required).
	 */
	@NotBlank(message = "spring.data.falkordb.database must be configured")
	private String database;

	/**
	 * Connection timeout. Default is 2 seconds.
	 */
	@DurationUnit(ChronoUnit.MILLIS)
	private Duration connectionTimeout = Duration.ofSeconds(2);

	/**
	 * Socket timeout. Default is 2 seconds.
	 */
	@DurationUnit(ChronoUnit.MILLIS)
	private Duration socketTimeout = Duration.ofSeconds(2);

	/**
	 * Username for authentication (if required).
	 */
	private String username;

	/**
	 * Password for authentication (if required).
	 */
	private String password;

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public String getDatabase() {
		return database;
	}

	public void setDatabase(String database) {
		this.database = database;
	}

	public Duration getConnectionTimeout() {
		return connectionTimeout;
	}

	public void setConnectionTimeout(Duration connectionTimeout) {
		this.connectionTimeout = connectionTimeout;
	}

	public Duration getSocketTimeout() {
		return socketTimeout;
	}

	public void setSocketTimeout(Duration socketTimeout) {
		this.socketTimeout = socketTimeout;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}
