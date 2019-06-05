IRIS Reasoner
=============
[![Build Status](https://travis-ci.org/scalahub/iris-reasoner.svg?branch=master)](https://travis-ci.org/scalahub/iris-reasoner)

_Text copied from [iris-reasoner.org](http://iris-reasoner.org)._  
_Code copied from [sourceforge](https://sourceforge.net/projects/iris-reasoner/)._

IRIS - Integrated Rule Inference System is an extensible reasoning engine for expressive rule-based languages.

Currently IRIS supports the following features:

* safe or [un-safe Datalog](http://iris-reasoner.org/saferules)
* with [(locally) stratified](http://iris-reasoner.org/stratification) or well-founded 'negation as failure'
* function symbols
* equality in rule heads
* comprehensive and extensible set of built-in predicates
* support for all the primitive [XML schema data types](http://www.w3.org/TR/xmlschema-2/#built-in-datatypes)

The following bottom-up rule evaluation algorithms are supported:

* Naive
* Semi-naive

The following top-down evaluation strategies are supported:

* SLDNF
* OLDT

The following program evaluation strategies are supported:

* Stratified bottom-up
* Well-founded semantics using alternating fixed point

The following program optimisations are supported:

* Rule filtering (removing rules that do not contribute to answering a query)
* Magic sets and sideways information passing strategy (SIPS)

To learn more about the theoretical results that the reasoner is based upon have a look at the [theoretical results](http://iris-reasoner.org/foundations).

The continued development of IRIS has been funded in part by 
[SOA4All](http://www.soa4all.eu/) a [European Framework 7](http://cordis.europa.eu/fp7/home_en.html) research project.

IRIS Applications
-----------------

IRIS is a available under the 
[GNU lesser general public licence (LGPL)](https://www.gnu.org/licenses/lgpl-2.1.en.html). It has been developed with the aim of supporting reasoning over [WSML](http://www.wsmo.org/wsml/wsml-syntax) ontologies, but can also be used in many other contexts. See below for the use cases we know of:

* [WSML Reasoner](http://iris-reasoner.org/wsml2reasoner)
* [RDFS Reasoner](http://iris-reasoner.org/rdfsreasoner)


