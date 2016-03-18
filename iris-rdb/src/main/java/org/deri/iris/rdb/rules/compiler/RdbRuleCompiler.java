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
import java.util.Collection;
import java.util.List;

import org.deri.iris.EvaluationException;
import org.deri.iris.api.basics.IAtom;
import org.deri.iris.api.basics.ILiteral;
import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.api.basics.IQuery;
import org.deri.iris.api.basics.IRule;
import org.deri.iris.api.basics.ITuple;
import org.deri.iris.api.builtins.IBuiltinAtom;
import org.deri.iris.api.terms.IConstructedTerm;
import org.deri.iris.api.terms.ITerm;
import org.deri.iris.facts.IFacts;
import org.deri.iris.rdb.storage.IRdbRelation;
import org.deri.iris.rdb.utils.EquivalentTermsAdapter;
import org.deri.iris.rules.RuleHeadEqualityRewriter;
import org.deri.iris.storage.IRelation;
import org.deri.iris.utils.equivalence.IEquivalentTerms;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RdbRuleCompiler {

	private static final Logger logger = LoggerFactory
			.getLogger(RdbRuleCompiler.class);

	/** The knowledge-base facts used to attach to the compiled rule elements. */
	private final IFacts facts;

	private Connection connection;

	public RdbRuleCompiler(Connection connection, IFacts facts) {
		this.connection = connection;
		this.facts = facts;
	}

	public RdbCompiledRule compile(IRule rule) throws SQLException,
			EvaluationException {
		List<RdbRuleElement> elements = compileBody(rule.getBody());

		RdbRuleElement lastElement = elements.get(elements.size() - 1);
		ITuple outputTuple = lastElement.getOutputTuple();

		IAtom headAtom = rule.getHead().get(0).getAtom();
		ITuple headTuple = headAtom.getTuple();

		RdbHeadSubstituter substituter = new RdbHeadSubstituter(connection,
				outputTuple, headTuple);

		elements.add(substituter);

		return new RdbCompiledRule(connection, elements,
				headAtom.getPredicate());
	}

	/**
	 * Compile a query. No optimisations of any kind are attempted.
	 * 
	 * @param query
	 *            The query to be compiled
	 * @return The compiled query, ready to be evaluated
	 * @throws EvaluationException
	 *             If the query can not be compiled for any reason.
	 */
	public RdbCompiledRule compile(IQuery query) throws SQLException,
			EvaluationException {
		List<RdbRuleElement> elements = compileBody(query.getLiterals());

		return new RdbCompiledRule(connection, elements, null);
	}

	/**
	 * Compile a rule body (or query). The literals are compiled in the order
	 * given. However, if one literal can not be compiled, because one or more
	 * of its variables are not bound from the proceeding literal, then it is
	 * skipped an re-tried later.
	 * 
	 * @param bodyLiterals
	 *            The list of literals to compile
	 * @return The compiled rule elements.
	 * @throws EvaluationException
	 *             If a rule construct can not be compiled (e.g. a built-in has
	 *             constructed terms)
	 */
	private List<RdbRuleElement> compileBody(Collection<ILiteral> bodyLiterals)
			throws SQLException, EvaluationException {
		List<ILiteral> literals = new ArrayList<ILiteral>(bodyLiterals);

		List<RdbRuleElement> elements = new ArrayList<RdbRuleElement>();

		// An empty rule body makes the rule always true.
		if (bodyLiterals.size() == 0) {
			RdbRuleElement element = new RdbTrue(connection);
			elements.add(element);
		}

		IRelation equivalenceRelation = facts
				.get(RuleHeadEqualityRewriter.PREDICATE);
		
		IEquivalentTerms equivalentTerms = new EquivalentTermsAdapter(
				equivalenceRelation);

		ITuple previousTuple = null;

		while (elements.size() < bodyLiterals.size()) {
			SQLException lastSqlException = null;
			EvaluationException lastEvalException = null;

			boolean added = false;
			for (int l = 0; l < literals.size(); l++) {
				ILiteral literal = literals.get(l);
				IAtom atom = literal.getAtom();
				boolean positive = literal.isPositive();

				RdbRuleElement element = null;

				try {
					if (atom instanceof IBuiltinAtom) {
						IBuiltinAtom builtinAtom = (IBuiltinAtom) atom;
						ITuple builtinTuple = atom.getTuple();

						boolean constructedTerms = false;
						for (ITerm term : builtinTuple) {
							if (term instanceof IConstructedTerm) {
								constructedTerms = true;
								break;
							}
						}

						if (constructedTerms) {
							logger.warn("Found a constructed term");
						} else {
							element = new RdbBuiltin(connection,
									equivalentTerms, builtinAtom,
									previousTuple, positive);
						}
					} else {
						IPredicate predicate = atom.getPredicate();
						IRelation factRelation = facts.get(predicate);
						ITuple viewCriteria = atom.getTuple();

						if (!(factRelation instanceof IRdbRelation)) {
							throw new IllegalArgumentException(
									"Found a non-RDB relation for " + predicate);
						}

						IRdbRelation relation = (IRdbRelation) factRelation;

						if (positive) {
							if (previousTuple == null) {
								// First sub-goal
								element = new RdbFirstSubgoal(connection,
										predicate, relation, viewCriteria);
							} else {
								element = new RdbJoiner(connection, predicate,
										relation, viewCriteria, previousTuple);
							}
						} else {
							// This *is* allowed to be the first literal for
							// rules such as:
							// p('a') :- not q('b')
							// or even:
							// p('a') :- not q(?X)

							element = new RdbDiffer(connection, predicate,
									relation, viewCriteria, previousTuple);
						}
					}

					previousTuple = element.getOutputTuple();

					elements.add(element);

					literals.remove(l);
					added = true;
					break;
				} catch (SQLException e) {
					// Oh dear. Store the exception and try the next literal.
					lastSqlException = e;
				} catch (EvaluationException e) {
					// Oh dear. Store the exception and try the next literal.
					lastEvalException = e;
				}
			}
			if (!added) {
				// No more literals, so the last error really was serious.
				if (lastSqlException != null) {
					throw lastSqlException;
				}

				if (lastEvalException != null) {
					throw lastEvalException;
				}
			}
		}

		return elements;
	}

}
