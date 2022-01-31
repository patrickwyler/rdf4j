/*******************************************************************************
 * Copyright (c) 2022 Eclipse RDF4J contributors.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Distribution License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/org/documents/edl-v10.php.
 ******************************************************************************/

package org.eclipse.rdf4j.sail.features;

/**
 * And interface used to signal that the SailConnection supports concurrent reads on the same connection.
 */
public interface ConcurrentReadsSailConnection {

	/**
	 * All sails supports concurrent reads as long as each reader uses their own connection. Some connections also
	 * support concurrent reads on the same connection.
	 *
	 * @return true if this connection supports concurrent reads
	 */
	default boolean supportsConcurrentReads() {
		return true;
	}

}
