/*
 * ******************************************************************************
 *  * Copyright (c) 2021 Eclipse RDF4J contributors.
 *  * All rights reserved. This program and the accompanying materials
 *  * are made available under the terms of the Eclipse Distribution License v1.0
 *  * which accompanies this distribution, and is available at
 *  * http://www.eclipse.org/org/documents/edl-v10.php.
 *  ******************************************************************************
 */

package org.eclipse.rdf4j.sparqlbuilder.constraint.propertypath;

public class ZeroOrOnePath implements PropertyPath {
	private final PropertyPath path;

	public ZeroOrOnePath(PropertyPath path) {
		this.path = path;
	}

	@Override
	public String getQueryString() {
		return path.getQueryString() + " ?";
	}
}
