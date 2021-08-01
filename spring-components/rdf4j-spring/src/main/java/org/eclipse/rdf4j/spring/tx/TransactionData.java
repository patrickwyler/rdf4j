package org.eclipse.rdf4j.spring.tx;

import static org.springframework.transaction.TransactionDefinition.*;

import java.util.function.Function;

import org.eclipse.rdf4j.repository.RepositoryConnection;

public class TransactionData {

	private RepositoryConnection connection;

	private boolean existing;

	private String name = "";

	private boolean rollbackOnly = false;

	private int timeout = TIMEOUT_DEFAULT;

	private int isolationLevel = ISOLATION_DEFAULT;

	private int propagationBehavior = PROPAGATION_REQUIRED;

	private boolean readOnly = false;

	public TransactionData(RepositoryConnection connection) {
		this.connection = connection;
	}

	public RepositoryConnection getConnection() {
		return connection;
	}

	public void wrapConnection(Function<RepositoryConnection, RepositoryConnection> wrapper) {
		this.connection = wrapper.apply(connection);
	}

	public void setExisting(boolean existing) {
		this.existing = existing;
	}

	public boolean isExisting() {
		return existing;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isRollbackOnly() {
		return rollbackOnly;
	}

	public void setRollbackOnly(boolean rollbackOnly) {
		this.rollbackOnly = rollbackOnly;
	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public int getIsolationLevel() {
		return isolationLevel;
	}

	public void setIsolationLevel(int isolationLevel) {
		this.isolationLevel = isolationLevel;
	}

	public int getPropagationBehavior() {
		return propagationBehavior;
	}

	public void setPropagationBehavior(int propagationBehavior) {
		this.propagationBehavior = propagationBehavior;
	}

	public boolean isReadOnly() {
		return readOnly;
	}

	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}

	@Override
	public String toString() {
		return "TransactionData{"
				+ "connection="
				+ connection
				+ ", existing="
				+ existing
				+ ", name='"
				+ name
				+ '\''
				+ ", rollbackOnly="
				+ rollbackOnly
				+ ", timeout="
				+ timeout
				+ ", isolationLevel="
				+ isolationLevel
				+ ", propagationBehavior="
				+ propagationBehavior
				+ ", readOnly="
				+ readOnly
				+ '}';
	}
}
