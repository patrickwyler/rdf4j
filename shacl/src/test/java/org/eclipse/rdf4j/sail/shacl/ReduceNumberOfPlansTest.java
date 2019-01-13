/*******************************************************************************
 * Copyright (c) 2018 Eclipse RDF4J contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Distribution License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *******************************************************************************/

package org.eclipse.rdf4j.sail.shacl;

import static junit.framework.TestCase.assertEquals;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.rdf4j.RDF4JException;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.eclipse.rdf4j.rio.UnsupportedRDFormatException;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.eclipse.rdf4j.sail.shacl.planNodes.PlanNode;
import org.junit.Test;

/**
 * @author Håvard Ottestad
 */
public class ReduceNumberOfPlansTest {

	@Test
	public void testAddingTypeStatement()
		throws RDFParseException, UnsupportedRDFormatException, IOException
	{
		ShaclSail shaclSail = new ShaclSail(new MemoryStore());
		shaclSail.initialize();
		Utils.loadShapeData(shaclSail, "reduceNumberOfPlansTest/shacl.ttl");

		try (ShaclSailConnection connection = (ShaclSailConnection)shaclSail.getConnection()) {
			connection.begin();

			connection.fillAddedAndRemovedStatementRepositories();
			List<PlanNode> collect = shaclSail.nodeShapes.stream().flatMap(
					shape -> shape.generatePlans(connection, shape, false).stream()).collect(
							Collectors.toList());

			assertEquals(0, collect.size());

			IRI person1 = Utils.Ex.createIri();
			connection.addStatement(person1, RDF.TYPE, Utils.Ex.Person);
			connection.fillAddedAndRemovedStatementRepositories();

			List<PlanNode> collect2 = shaclSail.nodeShapes.stream().flatMap(
					shape -> shape.generatePlans(connection, shape, false).stream()).collect(
							Collectors.toList());

			assertEquals(2, collect2.size());
			ValueFactory vf = shaclSail.getValueFactory();
			connection.addStatement(person1, Utils.Ex.ssn, vf.createLiteral("a"));
			connection.addStatement(person1, Utils.Ex.ssn, vf.createLiteral("b"));
			connection.addStatement(person1, Utils.Ex.name, vf.createLiteral("c"));

			connection.commit();

		}

	}

	@Test
	public void testRemovingPredicate() throws RDF4JException, UnsupportedRDFormatException, IOException {
		ShaclSail shaclSail = new ShaclSail(new MemoryStore());
		shaclSail.initialize();
		Utils.loadShapeData(shaclSail, "reduceNumberOfPlansTest/shacl.ttl");

		try (ShaclSailConnection connection = (ShaclSailConnection)shaclSail.getConnection()) {

			connection.begin();

			IRI person1 = Utils.Ex.createIri();

			ValueFactory vf = shaclSail.getValueFactory();
			connection.addStatement(person1, RDF.TYPE, Utils.Ex.Person);
			connection.addStatement(person1, Utils.Ex.ssn, vf.createLiteral("a"));
			connection.addStatement(person1, Utils.Ex.ssn, vf.createLiteral("b"));
			connection.addStatement(person1, Utils.Ex.name, vf.createLiteral("c"));
			connection.commit();

			connection.begin();

			connection.removeStatements(person1, Utils.Ex.ssn, vf.createLiteral("b"));

			connection.fillAddedAndRemovedStatementRepositories();

			List<PlanNode> collect1 = shaclSail.nodeShapes.stream().flatMap(
					shape -> shape.generatePlans(connection, shape, false).stream()).collect(
							Collectors.toList());
			assertEquals(1, collect1.size());

			connection.removeStatements(person1, Utils.Ex.ssn, vf.createLiteral("a"));

			connection.fillAddedAndRemovedStatementRepositories();

			List<PlanNode> collect2 = shaclSail.nodeShapes.stream().flatMap(
					shape -> shape.generatePlans(connection, shape, false).stream()).collect(
							Collectors.toList());
			assertEquals(1, collect2.size());

			connection.removeStatements(person1, Utils.Ex.name, vf.createLiteral("c"));
			connection.fillAddedAndRemovedStatementRepositories();

			List<PlanNode> collect3 = shaclSail.nodeShapes.stream().flatMap(
					shape -> shape.generatePlans(connection, shape, false).stream()).collect(
							Collectors.toList());
			assertEquals(2, collect3.size());

			connection.rollback();

		}

	}

}
