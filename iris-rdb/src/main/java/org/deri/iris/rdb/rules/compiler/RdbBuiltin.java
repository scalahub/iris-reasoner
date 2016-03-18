/*
 * Integrated Rule Inference System (IRIS):
 * An extensible rule inference system for datalog with extensions.
 * 
 * Copyright (C) 2011 Semantic Technology Institute (STI) Innsbruck, 
 * University of Innsbruck, Technikerstrasse 21a, 6020 Innsbruck, Austria.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, 
 * MA  02110-1301, USA.
 */
package org.deri.iris.rdb.rules.compiler;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.deri.iris.EvaluationException;
import org.deri.iris.api.basics.ITuple;
import org.deri.iris.api.builtins.IBuiltinAtom;
import org.deri.iris.api.terms.ITerm;
import org.deri.iris.api.terms.IVariable;
import org.deri.iris.factory.Factory;
import org.deri.iris.facts.IFacts;
import org.deri.iris.rdb.storage.CloseableIterator;
import org.deri.iris.rdb.storage.IRdbRelation;
import org.deri.iris.rdb.storage.RdbEmptyTupleRelation;
import org.deri.iris.rdb.storage.RdbTempRelation;
import org.deri.iris.utils.equivalence.IEquivalentTerms;

/**
 * A compiled rule element representing a built-in predicate.
 */
public class RdbBuiltin extends RdbRuleElement {

	/** The connection to the database. */
	private Connection connection;

	/** The tuple of the previous rule elements. */
	private ITuple inputTuple;

	/** The built-in atom at this position in the rule. */
	private IBuiltinAtom builtinAtom;

	/** Indicator of this literal is positive or negated. */
	private boolean isPositive;

	/** Indices from the input relation to pick term values from. */
	private Integer[] indicesFromInputRelationToMakeInputTuple;

	/**
	 * Indices from the built-in atom to put in to the rule element's output
	 * tuple.
	 */
	private Integer[] indicesFromBuiltInOutputTupleToCopyToOutputRelation;

	private List<Integer> unboundVariableIndicesInBuiltinTuple;

	private ITuple builtinTuple;

	private Integer[] indicesFromInputRelationToMakeOutputTuple;

	/**
	 * Constructor.
	 * 
	 * @param inputVariables
	 *            The variables from proceeding literals. Can be null if this is
	 *            the first literal.
	 * @param builtinAtom
	 *            The built-in atom object at this position in the rule.
	 * @param positive
	 *            true, if the built-in is positive, false if it is negative.
	 * @param equivalentTerms
	 *            The equivalent terms.
	 * @throws EvaluationException
	 *             If constructed terms are used with a built-in or there are
	 *             unbound variables.
	 */
	public RdbBuiltin(Connection connection, IEquivalentTerms equivalentTerms,
			IBuiltinAtom builtinAtom, ITuple inputTuple, boolean isPositive)
			throws EvaluationException {
		this.connection = connection;
		this.builtinAtom = builtinAtom;
		this.inputTuple = inputTuple;
		this.isPositive = isPositive;
		this.builtinTuple = builtinAtom.getTuple();

		builtinAtom.setEquivalenceClasses(equivalentTerms);

		// Get variables in built-in tuple.
		List<IVariable> unboundBuiltInVariables = new ArrayList<IVariable>();
		unboundVariableIndicesInBuiltinTuple = new ArrayList<Integer>();

		// The indexes of terms from inputRelation to use to populate the tuple
		// for the built-in predicate
		this.indicesFromInputRelationToMakeInputTuple = new Integer[builtinTuple
				.size()];
		indicesFromInputRelationToMakeOutputTuple = new Integer[builtinTuple
				.size()];

		List<Integer> indicesFromBuiltinOutputTupleToCopyToOutputRelation = new ArrayList<Integer>();
		int indexOfBuiltinOutputTuple = 0;

		for (int t = 0; t < builtinTuple.size(); t++) {
			// Assume not in input relation.
			int indexFromInput = -1;

			ITerm term = builtinTuple.get(t);
			indicesFromInputRelationToMakeOutputTuple[t] = -1;

			if (term instanceof IVariable) {
				IVariable builtinVariable = (IVariable) term;

				indexFromInput = inputTuple.indexOf(builtinVariable);

				// Is this variable unbound?
				if (indexFromInput == -1) {
					unboundBuiltInVariables.add(builtinVariable);
					unboundVariableIndicesInBuiltinTuple.add(t);

					indicesFromBuiltinOutputTupleToCopyToOutputRelation
							.add(indexOfBuiltinOutputTuple++);
				}

				indicesFromInputRelationToMakeOutputTuple[t] = indexFromInput;
			}

			this.indicesFromInputRelationToMakeInputTuple[t] = indexFromInput;
		}

		Set<IVariable> uniqueUnboundBuiltInVariables = new HashSet<IVariable>(
				unboundBuiltInVariables);

		if (uniqueUnboundBuiltInVariables.size() > builtinAtom
				.maxUnknownVariables()) {
			throw new EvaluationException(
					"Too many unbound variables for built-in '" + builtinAtom
							+ "' unbound variables: " + unboundBuiltInVariables);
		}

		// The indexes of terms in the built-in output tuple to copy to the
		// output relation.
		this.indicesFromBuiltInOutputTupleToCopyToOutputRelation = new Integer[0];
		this.indicesFromBuiltInOutputTupleToCopyToOutputRelation = indicesFromBuiltinOutputTupleToCopyToOutputRelation
				.toArray(indicesFromBuiltInOutputTupleToCopyToOutputRelation);
	}

	@Override
	public IRdbRelation process(IRdbRelation leftRelation)
			throws EvaluationException {
		int inputArity = inputTuple == null ? 0 : inputTuple.size();
		int builtinArity = builtinTuple.size();
		int arity = inputArity + builtinArity;

		IRdbRelation result;

		try {
			result = new RdbTempRelation(connection, arity);
		} catch (SQLException e) {
			throw new EvaluationException(
					"Could not create temporary RDB relation ("
							+ e.getLocalizedMessage() + ")");
		}

		// This can only happen if the built-in is the first literal in the rule
		// body. In that case, the ground tuple of the built-in should be added
		// to the relation.
		if (leftRelation == null) {
			try {
				leftRelation = new RdbEmptyTupleRelation(connection, 1);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		CloseableIterator<ITuple> iterator = leftRelation.iterator();
		
		while (iterator.hasNext()) {
			ITuple input =iterator.next();

			// Make the tuple for input to the built-in predicate
			ITerm[] terms = new ITerm[indicesFromInputRelationToMakeInputTuple.length];

			for (int t = 0; t < indicesFromInputRelationToMakeInputTuple.length; t++) {
				int index = indicesFromInputRelationToMakeInputTuple[t];
				terms[t] = index == -1 ? builtinAtom.getTuple().get(t) : input
						.get(index);
			}

			ITuple builtinInputTuple = Factory.BASIC.createTuple(terms);
			ITuple builtinOutputTuple = builtinAtom.evaluate(builtinInputTuple);

			if (isPositive) {
				if (builtinOutputTuple != null) {
					ITuple concatenated = makeResultTuple(input,
							builtinOutputTuple);

					result.add(concatenated);
				}
			} else {
				if (builtinOutputTuple == null) {
					result.add(input);
				}
			}
		}
		
		iterator.close();

		return result;
	}

	/**
	 * Transform the input tuple (from previous rule elements) and the tuple
	 * produced by the built-in atom in to a tuple to pass on to the next rule
	 * element.
	 * 
	 * @param inputTuple
	 *            The tuple produced b previous literals.
	 * @param builtinOutputTuple
	 *            The output of the built-in atom.
	 * @return The tuple to pass on to the next rule element.
	 */
	protected ITuple makeResultTuple(ITuple inputTuple,
			ITuple builtinOutputTuple) {
		assert builtinOutputTuple != null;

		List<ITerm> terms = new ArrayList<ITerm>();
		terms.addAll(inputTuple);

		int j = 0;
		for (int i = 0; i < builtinTuple.size(); i++) {
			ITerm term = builtinTuple.get(i);

			if (term instanceof IVariable) {
				if (j < unboundVariableIndicesInBuiltinTuple.size()
						&& unboundVariableIndicesInBuiltinTuple.get(j) == i) {
					ITerm computedTerm = builtinOutputTuple.get(j);
					terms.add(computedTerm);
					j++;
				} else if (indicesFromInputRelationToMakeOutputTuple[i] >= 0) {
					ITerm inputTerm = inputTuple
							.get(indicesFromInputRelationToMakeOutputTuple[i]);
					terms.add(inputTerm);
				}
			} else {
				terms.add(builtinTuple.get(i));
			}
		}

		return Factory.BASIC.createTuple(terms);
	}

	@Override
	public RdbRuleElement getDeltaSubstitution(IFacts deltas) {
		return null;
	}

	@Override
	public ITuple getOutputTuple() {
		List<ITerm> terms = new ArrayList<ITerm>();

		// The input tuple is null if the built-in is the first literal in the
		// rule body.
		if (inputTuple != null) {
			terms.addAll(inputTuple);
		}

		terms.addAll(builtinTuple);

		return Factory.BASIC.createTuple(terms);
	}

	@Override
	public void dispose() {
		// Do nothing.
	}
}
