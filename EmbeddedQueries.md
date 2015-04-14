This page describes the different query formats and how the results can be embedded in wiki pages.

# Introduction #

Database wiki currently supports three different query formats. The first format uses the node identifier to retrieve a node (and its subtree) from the database. The second query format is a special form of path expression, i.e., sequences of node labels with optional constraints. The third query format is an adaptation of the XAQL query language that was implemented with XARCH. To embed query results in a wiki page include the query statement between question marks, i.e, ?query-def?, in the wiki page markup. The tree query formats are described in more detail in the following.


# Node ID Queries #

Node ID queries retrieve a database node (and its subtree) based on the node ID (in hexadecimal encoding). The query syntax is:

```
nid://<node-id>

Example:
nid://D1
```

# XPath Queries #

The path expressions in our queries correspond to a simple fragment of XPath. Path expressions allow positional references as well as constraints on values of a node’s children. Note that the order of nodes is defined by the order of their node identifiers which in turn reflects the order in which the nodes have been inserted into the database. The general syntax is:

```
wpath://<schema-node>{<condition>}/...

where <condition> is either ':int' or '[<constraint>]'.

The constraint is a conjunction of sub-path conditions of the form:

{NOT} {FOR ALL | FOR ANY} <path-expression> {<value-constraint> | <change-constraint>}

FOR ALL | FOR ANY: Determines whether all nodes that match the path-expression have to satisfy the constraint or at least
one (FOR ANY is the default).

value-constraint: {ALL VALUES} [[<, <=, >, >=, =, !=, <>, LIKE, MATCHES] [<int-value>|<string-value>] | IN (<value-list>)]

ALL VALUES: All values of a node have to match the constraint.

change-constraint: [EXISTS | HAS CHANGES | WAS MODIFIED]

HAS CHANGES: The node has a timestamp that differs from it's parent timestamp.

WAS MODIFIED: The node or any part of it's subtree has been modified.

Example:
Return the names of all European countries in the CIA World Factbook 
wiki that have had a population of over 1.000.000 in all versions.

?wpath://COUNTRY[
   CATEGORY[NAME = 'Geography']/PROPERTY[NAME = 'Map references']/TEXT = 'Europe'
  AND
   CATEGORY[NAME = 'People']/PROPERTY[NAME= 'Population']/TEXT ALL VALUES > 1000000
]/NAME?
```

# XAQL Queries #

XAQL queries make use of XPath expressions as defined above. Each query has a FROM clause, a SELECT clause, and optional WHERE clause and an optional VERSION clause. The FROM clause allows to define variables. For each binding of the variables, the WHERE clause is evaluated and if true the SELECT clause returns the specified values. The WHERE clause not only allows to express constraints on node values but also (in limited form) on the provenance of a node. The following are some example XAQL queries:

```
The name and population of all European countries:

SELECT $c/NAME, $p/TEXT FROM $c IN /COUNTRY,
$p IN $c/CATEGORY[NAME='People']/PROPERTY[NAME='Population']
WHERE $c/CATEGORY[NAME='Geography']/PROPERTY[NAME = 'Map references']/TEXT = 'Europe'

The name and GDP of all countries for which exports and imports are above $1 billion  in the current version:

SELECT $c/NAME FROM $c IN /COUNTRY,
$e IN $c/CATEGORY[NAME='Economy'],
$i IN $e/PROPERTY[NAME = 'Imports'], $x IN $e/PROPERTY[NAME = 'Exports']
WHERE COINCIDES($i/TEXT > 1000000000 AND $x/TEXT > 1000000000)

The name of all countries that have been updated by user admin since the 1. Nov 2011:

SELECT $c/NAME FROM $c IN /COUNTRY
WHERE $c WAS MODIFIED SINCE 2011-11-01 BY 'admin' USING UPDATE

```

## Syntax ##

```
<query>           ::= <select_clause> <from_clause> <version_clause>? <where_clause>? 

<select_clause>   ::= SELECT <variable_path> (AS <id>)? (, <variable_path (AS <id>)?)*

<from_clause>     ::= FROM <variable> IN <absolute_path> (, <variable> IN <variable_path>)*

<version_clause>  ::= VERSION <timestamp>
<timestamp>       ::= (<numeric_version> | <date_version> | now) (',' | '-') <timestamp>) * 
<date_version>    ::= <date>
<numeric_version> ::= <number>

<where_clause>    ::= WHERE <where_exp> (AND <where_exp>)*
<where_exp>       ::= (NOT)? (<quantifier> <variable_path> (<node_value_exp> | <node_exp>)) 
                   |  COINCIDES '(' <coincide_exp> (AND <coincide_exp>)* ')'
<coincide_exp>    ::= <path> <node_value_exp>

<variable>        ::= '$' <id>
<absolute_path>   ::= '/' <relative_path>
<relative_path>   ::= <step> <absolute_step>* '/'?
<variable_path>   ::= <variable> <absolute_step>+ '/'?
<variable_optional_path>
                  ::= <variable> <absolute_step>* '/'?
<absolute_step>   ::= '/' <step>
<step>            ::= <id> (<index_condition> | <path_condition> )?
<index_condition> ::= ':' <integer>
<path_condition>  ::= '[' <path_test> (AND <path_test>)* ']'
<path_test>       ::= (NOT)? <quantifier>? <relative_path> (<node_exp> | <node_value_exp>)?

<node_value_exp>  ::= ALL_VALUES? (<value_exp> | <string_match_exp>)
<value_exp>       ::= ( '=' | '!=' | '>' | '<' | '<=' | '>=') (<string> | <number>)
                    | IN '(' <string> (',' <string>)* ')'
<string_match_exp> ::= (LIKE | MATCHES) <string>
<node_exp>        ::= EXISTS 
                   | HAS_CHANGES <provenance_expr>?
                   | WAS_MODIFIED <provenance_expr>?
<quantifier>      ::= FOR_ALL | FOR_ANY


<provenance_expr> ::= <provenance_date_expr> | <provenance_user_expr>
<provenance_date_expr> ::= (BEFORE | AFTER | SINCE | UNTIL ) <date>
                         | BETWEEN <date> AND <date>
<provenance_user_expr> ::= BY <user> (USING (COPY | DELETE | INSERT | UPDATE))?
```

# Visualising query results #

Basic support is provided for rendering data returned by queries in a chart or in Google Maps. A `CHART:` prefix on a query expects a collection of (string, number, ..., number) rows. The results are rendered as a column chart. The chart can be parameterised by its dimentions. A prefix `CHART(<xsize>,<ysize>):` specifies a chart of size `<xsize> * <ysize>` pixels. (The default size is 800\*600 pixels.)

The prefixes `PIE:` and `PIE(<xsize>,<ysize>):` do the analogous thing for pie charts. The Google visualization API ignores all but the first number column.

The 'MAP:' prefix expects a collection of (string, ..., string) rows. The results are rendered in Google Maps. For each row, the strings from each column are concatenated together separated by commas to form an address.

Examples:

A map showing the countries from the CIA World Fact Book:

```
?MAP:SELECT $c/NAME FROM $c IN /COUNTRY?
```

Telecommunications users in South America in 2009:

```
?CHART:SELECT $c/NAME AS Country, $u/VALUE AS internet, $f/VALUE AS fixed_line, $m/VALUE AS mobile
 FROM $c IN /COUNTRY,
      $u IN $c/CATEGORY[NAME='Communications']/PROPERTY[NAME='Internet users'],
      $f IN $c/CATEGORY[NAME='Communications']/PROPERTY[NAME='Telephones - main lines in use'],
      $m IN $c/CATEGORY[NAME='Communications']/PROPERTY[NAME='Telephones - mobile cellular']
 WHERE $c/CATEGORY[NAME='Geography']/PROPERTY[NAME='Map references']/VALUE='South America'
   AND $u/DATE=2009 AND $f/DATE=2009 AND $m/DATE=2009?
```

Composition of GDP ($) by country in South America:

```
?PIE:SELECT $c/NAME AS country, $g/VALUE AS GDP
 FROM $c IN /COUNTRY,
      $g IN $c/CATEGORY[NAME='Economy']/PROPERTY[NAME='GDP (official exchange rate)']
 WHERE $c/CATEGORY[NAME='Geography']/PROPERTY[NAME='Map references']/VALUE='South America'?
```