package org.deri.iris.rdb.evaluation.orb;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.deri.iris.Configuration;
import org.deri.iris.EvaluationException;
import org.deri.iris.api.IKnowledgeBase;
import org.deri.iris.api.basics.IRule;
import org.deri.iris.facts.Facts;
import org.deri.iris.facts.IFacts;
import org.deri.iris.optimisations.magicsets.MagicSets;
import org.deri.iris.optimisations.rulefilter.RuleFilter;
import org.deri.iris.rdb.RdbKnowledgeBase;
import org.deri.iris.rdb.facts.RdbFacts;
import org.deri.iris.rdb.utils.RdbUtils;
import org.deri.iris.storage.simple.SimpleRelationFactory;

public class IrisRdbLargeJoin extends LargeJoin {

	public static void main(String[] args) throws Exception {
		IrisRdbLargeJoin largeJoinTest = new IrisRdbLargeJoin();

		// largeJoinTest.join1(LargeJoin.Join1Query.BF_A,
		// LargeJoin.Join1Data.DATA0);
		largeJoinTest.dblp();
	}

	private Connection connection;

	private File tempDirectory;

	@Override
	protected IFacts createFacts() {
		try {
			tempDirectory = RdbUtils.createTempDirectory();
			connection = RdbUtils.createConnection(tempDirectory);

//			return new RdbFacts(connection);
			return new Facts(new SimpleRelationFactory());
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return null;
	}

	@Override
	protected IKnowledgeBase createKnowledgeBase(IFacts facts, List<IRule> rules)
			throws EvaluationException {
		Configuration configuration = new Configuration();

		configuration.programOptmimisers.add(new RuleFilter());
		configuration.programOptmimisers.add(new MagicSets());

		try {
			return new RdbKnowledgeBase(facts, rules, configuration);
		} catch (IOException e) {
			throw new EvaluationException(
					"Failed to create the database directory");
		} catch (ClassNotFoundException e) {
			throw new EvaluationException("Failed to load database driver");
		} catch (SQLException e) {
			throw new EvaluationException("An SQL error occurred");
		}
	}

	@Override
	protected void dispose() {
		if (connection != null) {
			try {
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		if (tempDirectory != null) {
			try {
				FileUtils.deleteDirectory(tempDirectory);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
