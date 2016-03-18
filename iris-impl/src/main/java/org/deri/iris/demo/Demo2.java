/*
 * Integrated Rule Inference System (IRIS):
 * An extensible rule inference system for datalog with extensions.
 *
 * Copyright (C) 2008 Semantic Technology Institute (STI) Innsbruck,
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
package org.deri.iris.demo;

import java.io.FileReader;
import java.io.IOException;

import org.deri.iris.Configuration;
import org.deri.iris.KnowledgeBaseFactory;
import org.deri.iris.evaluation.stratifiedbottomup.StratifiedBottomUpEvaluationStrategyFactory;
import org.deri.iris.evaluation.stratifiedbottomup.naive.NaiveEvaluatorFactory;
import org.deri.iris.evaluation.stratifiedbottomup.seminaive.SemiNaiveEvaluatorFactory;
import org.deri.iris.evaluation.wellfounded.WellFoundedEvaluationStrategyFactory;
import org.deri.iris.optimisations.magicsets.MagicSets;
import org.deri.iris.optimisations.rulefilter.RuleFilter;
import org.deri.iris.rules.safety.AugmentingRuleSafetyProcessor;
import org.deri.iris.rules.safety.StandardRuleSafetyProcessor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.deri.iris.Configuration;
import org.deri.iris.KnowledgeBaseFactory;
import org.deri.iris.api.IKnowledgeBase;
import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.api.basics.IQuery;
import org.deri.iris.api.basics.IRule;
import org.deri.iris.api.basics.ITuple;
import org.deri.iris.api.terms.IVariable;
import org.deri.iris.compiler.Parser;
import org.deri.iris.storage.IRelation;

/**
 * A command line demonstrator for IRIS.
 */
public class Demo2
{

	/**
	 * Entry point.
	 * @param args program evaluation_method
	 * @throws Exception
	 */
	public static void main( String[] args )	{
		Configuration configuration = KnowledgeBaseFactory.getDefaultConfiguration();
		// configuration.evaluationTimeoutMilliseconds = Integer.parseInt( getParameter( argument ) );
		//configuration.evaluationStrategyFactory = new StratifiedBottomUpEvaluationStrategyFactory( new NaiveEvaluatorFactory() );
		configuration.evaluationStrategyFactory = new StratifiedBottomUpEvaluationStrategyFactory( new SemiNaiveEvaluatorFactory() );
		configuration.ruleSafetyProcessor = new StandardRuleSafetyProcessor();
		//configuration.ruleSafetyProcessor = new AugmentingRuleSafetyProcessor();
		// MAGIC_SETS
		configuration.programOptmimisers.add( new RuleFilter() );
		configuration.programOptmimisers.add( new MagicSets() );

		String program = "man('homer').\r\n" +
        "woman('marge').\r\n" +
        "x_hasSon('homer','bart').\r\n" +
        "isMale(?x) :- man(?x).\r\n" +
        "isFemale(?x) :- woman(?x).\r\n" +
        "isMale(?y) :- x_hasSon(?x,?y).\r\n" +
        "\r\n" +
        "?-isMale(?x).";

		program = "request('req:dcac5..').\r\n" +
		    "restAction('req:dcac5..', 'invokeGraph').\r\n" +
		    "invokeGraph('req:dcac5..', 'guesser').\r\n" +
		    "requesterOfRequest('req:dcac5..', 'person:mac9..').\r\n" +
		    "worksFor('person:mac9..', 'org:Data69', 'e:frontend').\r\n" +
        "\r\n" +
        "trustedOrg('org:Data61').\r\n" +
		    "trustedReferer('e:frontend').\r\n" +
		    "\r\n" +
		    "admit(?req) :- request(?req),\r\n" +
		    "               invokeGraph(?req, 'guesser'),\r\n" +
        "               requesterOfRequest(?req, ?requester),\r\n" +
        "               worksFor(?requester, ?org, ?x),\r\n" +
        "               trustedOrg(?org).\r\n" +
        "\r\n" +
        "?-requesterOfRequest('req:dcac5..', ?requester).\r\n" +
        "?-admit('req:dcac5..').\r\n";

		/**

request('req:dcac5..').
restAction('req:dcac5..', 'invokeGraph')
invokeGraph('req:dcac5..', 'guesser')
requesterOfRequest('dcac5..', 'person:mac9..')
worksFor('person:mac9..', 'Data61', 'e:frontend')
trusted('e:frontend')

admit(?req) :- true.


 Assertions: {
  "@type" : "https://schema.n1analytics.com/assertion/1/assertedEntity",
  "subject" : {
    "name" : "dcac5c56-b3bb-4999-b845-61db3dd1d60a",
    "type" : "https://schema.n1analytics.com/rest/1/request"
  },
  "attributes" : [ {
    "name" : "rest:action",
    "value" : "invokeGraph"
  }, {
    "name" : "rest:endpoint",
    "value" : "graph"
  }, {
    "name" : "http:Content-Type",
    "value" : "application/json"
  }, {
    "name" : "http:X-Requester",
    "value" : "max"
  }, {
    "name" : "http:remoteAddress",
    "value" : "127.0.0.1"
  }, {
    "name" : "http:isSecure",
    "value" : "true"
  }, {
    "name" : "rest:graphID",
    "value" : "guesser"
  }, {
    "name" : "rest:runID",
    "value" : "dcae203f-648c-4f44-8af4-6f6f7d100756"
  } ],
  "assertionContext" : {
    "@type" : "https://schema.n1analytics.com/assertion/1/assertionContext",
    "asserter" : {
      "name" : "__SYSTEM__"
    }
  }


		 */
		new ProgramExecutor().call(program, configuration );
	}

	/**
	 * Helper for the demo applications.
	 */
	public static class ProgramExecutor	{

	  /** The new line separator to use when formatting output. */
	  public static final String NEW_LINE = System.getProperty( "line.separator" );

	  /** Output helper. */
	  public static final String BAR = "----------------------------------";

	  /** Flag for how to format the output. */
	  public static final boolean SHOW_VARIABLE_BINDINGS = true;

	  /** Flag for how to format the output. */
	  public static final boolean SHOW_QUERY_TIME = true;

	  /** Flag for how to format the output. */
	  public static final boolean SHOW_ROW_COUNT = true;

	  /** Flag for how to format the output. */
	  public static final boolean SHOW_RELATION = true;

	  /**
	   * Constructor.
	   * This is where the program is actually evaluated.
	   * @param program The Datalog program to evaluate.
	   * @param configuration The configuration object.
	   */
	  public ProgramExecutor() {}

	  public void call (String program, Configuration configuration ) {
	    try {
	      Parser parser = new Parser();
	      parser.parse( program );
	      Map<IPredicate,IRelation> facts = parser.getFacts();
	      List<IRule> rules = parser.getRules();

	      StringBuilder output = new StringBuilder();

	      long duration = -System.currentTimeMillis();
	      IKnowledgeBase knowledgeBase = KnowledgeBaseFactory.createKnowledgeBase( facts, rules, configuration );
	      duration += System.currentTimeMillis();

	      if( SHOW_QUERY_TIME )
	      {
	        output.append( "Init time: " ).append( duration ).append( "ms" ).append( NEW_LINE );
	      }

	      List<IVariable> variableBindings = new ArrayList<IVariable>();

	      for( IQuery query : parser.getQueries() )
	      {
	        // Execute the query
	        duration = -System.currentTimeMillis();
	        IRelation results = knowledgeBase.execute( query, variableBindings );
	        duration += System.currentTimeMillis();

	        output.append( BAR ).append( NEW_LINE );
	        output.append( "Query:      " ).append( query );
	        if( SHOW_ROW_COUNT )
	        {
	          output.append( " ==>> " ).append( results.size() );
	          if( results.size() == 1 )
	            output.append( " row" );
	          else
	            output.append( " rows" );
	        }
	        if( SHOW_QUERY_TIME )
	          output.append( " in " ).append( duration ).append( "ms" );

	        output.append( NEW_LINE );

	        if( SHOW_VARIABLE_BINDINGS )
	        {
	          output.append( "Variables:  " );
	          boolean first = true;
	          for( IVariable variable : variableBindings )
	          {
	            if( first )
	              first = false;
	            else
	              output.append( ", " );
	            output.append( variable );
	          }
	          output.append( NEW_LINE );
	        }

	        if (SHOW_RELATION) {
	          formatResults( output, results );
	        }
	      }

	      mOutput = output.toString();
	    }
	    catch( Exception e )
	    {
	      mOutput = e.toString();
	    }
	    System.out.println(mOutput);
	  }

	  /**
	   * Get the formatted program output.
	   * @return The formatted program output.
	   */
	  public String getOutput()
	  {
	    return mOutput;
	  }

	  /**
	   * Format the actual query results (tuples).
	   * @param builder
	   * @param m
	   */
	  private void formatResults( StringBuilder builder, IRelation m )
	  {
	    for(int t = 0; t < m.size(); ++t )
	    {
	      ITuple tuple = m.get( t );
	      builder.append( tuple.toString() ).append( NEW_LINE );
	    }
	    }

	  /** The output (or error) from the program execution. */
	  private String mOutput;
	}

}
