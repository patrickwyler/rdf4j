package org.eclipse.rdf4j.spring.operationlog.log;

import org.eclipse.rdf4j.query.Operation;

public class OperationExecutionStats {
	private int bindingsHashCode;
	private String operation;
	private long start;
	private Long end = null;
	private boolean failed = false;

	public OperationExecutionStats(String operation, int bindingsHashCode) {
		this.bindingsHashCode = bindingsHashCode;
		this.operation = operation;
		this.start = System.currentTimeMillis();
	}

	public static OperationExecutionStats of(Operation operation) {
		return new OperationExecutionStats(
				operation.toString(), operation.getBindings().hashCode());
	}

	public static OperationExecutionStats of(PseudoOperation operation) {
		return new OperationExecutionStats(operation.getOperation(), operation.getValuesHash());
	}

	public void operationSuccessful() {
		this.end = System.currentTimeMillis();
	}

	public void operationFailed() {
		this.end = System.currentTimeMillis();
		this.failed = true;
	}

	public String getOperation() {
		return operation;
	}

	public int getBindingsHashCode() {
		return bindingsHashCode;
	}

	public long getQueryDuration() {
		if (this.end == null) {
			throw new IllegalStateException("Cannot calculate duration - end is null");
		}
		return end - start;
	}

	public boolean isFailed() {
		return failed;
	}
}
