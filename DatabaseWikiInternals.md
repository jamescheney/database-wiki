# Introduction #

This page contains a high-level description of the data structures used in the DatabaseWiki system.

# Core data structures #

## Database classes ##

The `org.dbwiki.data.database` package contains classes for DatabaseNodes and related classes for the content of a DatabaseWiki.

### The `Database` interface ###

The `Database` interface provides an abstraction of the basic behavior of the system.  The interface is as follows:

### `DatabaseNode` hierarchy ###

`DatabaseNode`s are one of the main concepts of DatabaseWiki.  The class hiearchy is as follows:

  * `DatabaseNode` provides basic functionality for nodes that have a label.  `DatabaseNode`s have a `DatabaseElementNode` parent and an `AnnotationList`
    * `DatabaseElementNode` provides common functionality for so-called Element nodes that can be parents of other nodes and that have a label and type (Entity).
      * `DatabaseAttributeNode`s are attribute nodes that have a label and a `DatabaseNodeValue` child (which is essentially a timestamped sequence of `DatabaseTextNode`s)
      * `DatabaseGroupNode`s are nodes that group other nodes, having a list of children all of which must be `DatabaseElementNode`s.
    * `DatabaseTextNode`s are nodes whose content is a text string.

The `DatabaseElementList` class is used in `DatabaseGroupNode` and in some other places to hold a list of elements.

All `DatabaseNode`s are `TimestampedObject`s.  All of the above classes are abstract, and have to be instantiated using concrete subclasses, e.g. `RDBMSDatabaseNode`.  However, code for rendering data as HTML can be written to the `DatabaseNode` interface, avoiding implementation-dependence.

### Updates ###

The `Update` and `NodeUpdate` classes are used to colelct updates to nodes as part of an edit form.

## Identity ##

The `org.dbwiki.data.resource` package contains classes for managing identities that link database IDs and URLs, including data nodes, wiki page identities, and schema nodes.

The `ResourceIdentifier` interface provides methods to compare identifiers,
Its instances are:
  * `NodeIdentifier` = identifies data nodes
  * `PageIdentifier` - identifies wiki pages
  * `EntityIdentifier` - identifies schema nodes
  * `PID` - Obsolete.  Identifies a node via a vector of label:index steps.  Uses the `LID` class, which is also obsolete.

In addition, the `WRI` class contains a `DatabaseIdentifier` and a `ResourceIdentifier`, i.e. it is a pair linking

## Schemas ##

The `org.dbwiki.data.schema` package contains classes for DatabaseSchema (and its components).  A `DatabaseSchema` is basically a mapping from names to schema nodes, which are currently called `Entities`.  Entities include `AttributeEntity`, a schema node representing an attribute data node, and `GroupEntity`, a schema node representing a group data node.
The `SchemaParser` class parses a schema in from a string.

## Versioning and timestamp intervals ##

The `org.dbwiki.data.time` package contains classes and interfaces for temporal aspects of the data.  The `TimeInterval` class is essentially a struct containing a pair of version numbers representing a n interval of time.  The `TimeSequence` class is a vector of (nonoverlapping, increasing) `TimeInterval`s.  The `TimestampedObject` class provides basic functionality for objects that have associated `TimeInterval`s and are arranged in a tree (with a `TimestampedObject` parent from which timestamps can be inherited).

## Provenance ##

The  `org.dbwiki.data.provenance` package contains the abstract `Provenance` class and subclasses representing Activate, Copy, Delete, Import, Insert, Unknown and Update operations.


## Annotations ##

The module `org.dbwiki.data.annotation` contains the `Annotation` and `AnnotationList` classes.


## Queries ##

The `org.dbwiki.data.query` package contains classes defining path queries and related data structures.

## Wiki pages ##

The `org.dbwiki.data.wiki` package contains classes that provide a simple HTML Wiki functionality, with wiki pages that can contain path queries into the data.

## Security ##

The `org.dbwiki.data.security` package provides fine-grained security policies.  Currently unused/under construction.

# Drivers #

There is currently only one driver, in package `org.dbwiki.driver.rdbms`.  This implements the `DatabaseNode` classes and `Database` interface.

# Exceptions #

The `org.dbwiki.exception.*` packages and classes define some DatabaseWiki-specific exceptions.

# Main programs #

The `org.dbwiki.main` package includes command-line programs that create databases, import data, delete databases, and start the server.

# Web interface #

The `org.dbwiki.web.*` packages define the Web interface to DatabaseWiki.  The key components are the `WikiServer` and `DatabaseWiki` classes that receive requests, geenrate page templates and content generators, and render pages, and the many small classes in `org.dbwiki.web.ui` that handle printing out different parts of each page.

## HTML ##

Package `org.dbwiki.web.html` contains some utility classes for HTML pages.

## UI and HTML/form generation ##

Package `org.dbwiki.web.ui` contains classes to deal with CSS, HTML templates, and content generation.  Basically, a template is a HTML file with some macro calls in it, and a content generator is a symbol table containing functions to call for each macro to fill in its content.

### Layout ###

Package `org.dbwiki.web.ui.layout` defines teh `DatabaseLayouter` class and associated data structures that provides a way to customize the way a data tree is rendered as HTML.

### Printing ###

The `org.dbwiki.web.ui.printer` package and its subpackages define an interface `HtmlContentPrinter` and many implementations that can be used to fill in the content macros in templates.