/**
 * (c) 2003-2020 MuleSoft, Inc. The software in this package is published under the terms of the Commercial Free Software license V.1 a copy of which has been included with this distribution in the LICENSE.md file.
 */
package com.mulesoft.connector.smb.internal.connection.provider;

import static java.lang.String.format;

import java.util.Objects;

import javax.inject.Inject;

import com.mulesoft.connector.smb.internal.connection.SmbFileSystemConnection;
import org.mule.extension.file.common.api.FileSystemProvider;
import com.mulesoft.connector.smb.api.LogLevel;
import com.mulesoft.connector.smb.internal.connection.SmbClient;
import com.mulesoft.connector.smb.internal.connection.SmbClientFactory;
import com.mulesoft.connector.smb.internal.extension.SmbConnector;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.connection.ConnectionValidationResult;
import org.mule.runtime.api.connection.PoolingConnectionProvider;
import org.mule.runtime.api.lock.LockFactory;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Password;
import org.mule.runtime.extension.api.annotation.param.display.Placement;
import org.mule.runtime.extension.api.annotation.param.display.Summary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An {@link FileSystemProvider} which provides instances of
 * {@link SmbFileSystemConnection} from instances of {@link SmbConnector}
 *
 * @since 1.0
 */
@DisplayName("SMB Connection")
public class SmbConnectionProvider extends FileSystemProvider<SmbFileSystemConnection>
		implements PoolingConnectionProvider<SmbFileSystemConnection> {

	private static final Logger LOGGER = LoggerFactory.getLogger(SmbConnectionProvider.class);

	private static final String SMB_ERROR_MESSAGE_MASK = "Could not establish SMB connection (host: '%s', domain: %s, user: %s, share root: '%s', logLevel: '%s'): %s";

	@Inject
	private LockFactory lockFactory;

	/**
	 * The SMB server hostname or ip address
	 */
	@Parameter
	@Placement(order = 1)
	private String host;

	/**
	 * The user domain. Required if the server uses NTLM authentication
	 */
	@Parameter
	@Optional
	@Placement(order = 2)
	private String domain;

	/**
	 * Username. Required if the server uses NTLM authentication.
	 */
	@Parameter
	@Optional
	@Placement(order = 3)
	protected String username;

	/**
	 * Password. Required if the server uses NTLM authentication.
	 */
	@Parameter
	@Optional
	@Password
	@Placement(order = 4)
	private String password;

	/**
	 * The share root
	 */
	@Parameter
	@Optional
	@Summary("The SMB share to be considered as the root of every path" +
			" (relative or absolute) used with this connector")
	@Placement(order = 5)
	private String shareRoot;

	/**
	 * The log level
	 */
	@Parameter
	@Optional(defaultValue = "WARN")
	@Summary("Log level. Used by Logger operation to determine if messages " +
			 "whether log messages should be written or not")
	@Placement(order = 6)
	private LogLevel logLevel;

	private SmbClientFactory clientFactory = new SmbClientFactory();

	@Override
	public SmbFileSystemConnection connect() throws ConnectionException {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(format("Connecting to SMB server (host: '%s', domain: '%s', user: '%s', share Root: '%s')", host,
					domain, username, shareRoot));
		}
		SmbClient client = clientFactory.createInstance(host, shareRoot, logLevel);
		try {
			client.login(domain, username, password);
		} catch (Exception e) {
			throw new ConnectionException(getErrorMessage(e.getMessage()), e);
		}

		return new SmbFileSystemConnection(client, lockFactory);
	}

	@Override
	public void disconnect(SmbFileSystemConnection fileSystem) {
		fileSystem.disconnect();
	}

	@Override
	public ConnectionValidationResult validate(SmbFileSystemConnection fileSystem) {
		return fileSystem.validateConnection();
	}

	public void setClientFactory(SmbClientFactory clientFactory) {
		this.clientFactory = clientFactory;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getWorkingDir() {
		// TODO: verify if it's valid to assume the share root as the working directory
		return this.shareRoot;
	}

	private String getErrorMessage(String message) {
		return format(SMB_ERROR_MESSAGE_MASK, this.host, this.domain, this.username, this.shareRoot, this.logLevel, message);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}

		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		if (!super.equals(o)) {
			return false;
		}

		SmbConnectionProvider that = (SmbConnectionProvider) o;
		return Objects.equals(host, that.host) && Objects.equals(domain, that.domain)
				&& Objects.equals(username, that.username) && Objects.equals(password, that.password)
				&& Objects.equals(shareRoot, that.shareRoot)
				&& Objects.equals(logLevel, logLevel);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), host, domain, username, password, shareRoot, logLevel);
	}

	// This validation needs to be done because of the bug explained in MULE-15197
	@Override
	public void onReturn(SmbFileSystemConnection connection) {
		if (!connection.validateConnection().isValid()) {
			LOGGER.debug("Connection is not valid, it is destroyed and not returned to the pool.");
			throw new IllegalStateException("Connection that is being returned to the pool is invalid.");
		}
	}

	public void setHost(String host) {
		this.host = host;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setShareRoot(String shareRoot) {
		this.shareRoot = shareRoot;
	}

	public void setLogger(LogLevel logLevel) {this.logLevel = logLevel;}

}