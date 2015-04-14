# Introduction #

The Database Wiki uses a relational schema to store tree-structured, versioned data, along with provenance and annotations.  This page contains a description of the current version of this schema, with notes on the fields and some typical values.

Note: This description is missing at least the pre and post columns in DB\_data, maybe more.

# Details #

We use [Links](http://groups.inf.ed.ac.uk/links/) notation to describe the tables, which maps to SQL tables/types in a hopefully obvious way.  If you don't know what that means, and it's not obvious, ask.

## Basic types ##

The basic types used by Database Wiki are as follows.  We use type abbreviations to aid documentation.

```
typename UserId         = Int;
typename DatabaseId     = Int;
typename ConfigFileId   = Int;
typename SchemaId       = Int;
typename VersionNumber  = Int;
typename TimesequenceId = Int;
typename NodeId	        = Int;
typename AnnotationId   = Int;
typename PageId         = Int;

typename Time           = Int; # This is a Unix/Java/RFC 1479 time
typename Date           = String; # This is a date/time represented as a string
```

## Enumerated types ##

```
typename SchemaType     = Int;
```
Used in `DB_schema`.  Meaning:
  * `ATTRIBUTE` = 0 - node is an attribute (text value only)
  * `GROUP` = 1 - node is a group (i.e. a collection of elements with the same name)

```
typename Provenance     = Int;
```
Used in `DB_version`. Meaning:
  * `ACTIVATE` = 0 - version was restored from a previous version (currently doesn't say which one?)
  * `COPY` = 1 - version was created by copying `node` from somewhere else in the DB (given by `source_url`)
  * `DELETE` = 2 - version created by deleting subtree `node`
  * `IMPORT` = 3 - version created by importing from external `source_url`
  * `INSERT` = 4 - version created by inserting subtree `node`
  * `UPDATE` = 5 - version created by updating subtree in-place.

```
typename Authentication = Int;
```
Used in `_database`. Meaning:
  * `ALWAYS` = 0 - always require authentication
  * `NEVER` = 1 - never require authentication
  * `WRITE_ONLY` = 2 - require authentication only on write

```
typename AutoSchemaChanges = Int;
```
Used in `wiki_server_database`. Meaning:
  * `ALLOW` = 0 - Automatically add new node types if necessary
  * `IGNORE` = 1 - Ignore unknown node types
  * `NEVER` = 2 - Never change schema automatically

## The server tables ##
Each instance of Database Wiki has three tables representing server information, for users, databases and configuration files:

### `_user` ###
```
  table "_user" with
    (id       : UserId, # key
     login    : String, 
     full_name : String, 
     password : String)
```
The `_user` table contains the users, ids and passwords.
  * `id` - unique integer key
  * `login` - username e.g. jcheney
  * `full_name` - e.g. "James Cheney"
  * `password` - unencrypted (!) password

### `_database` ###
```
table "_database" with 
    (id                   : DatabaseId, # key
     name                 : String,
     title                : String,
     authentication       : Authentication,
     auto_schema_changes  : AutoSchemaChanges, 
     css_version          : ConfigFileId,
     layout_version       : ConfigFileId,
     template_version     : ConfigFileId,
     url_decoding_version : ConfigFileId)
```

The `_database` table contains one row for each top-level database.

  * `id` - unique key
  * `name` - short, contiguous string used for names of tables for this  database, e.g. "CIAWFB"
  * `title` - long, human readable name e.g. "CIA World Factbook"
  * `authentication` - which Authentication mode to use
  * `auto_schema_changes`: which `AutoSchemaChanges` mode to use
  * `css_version`, `layout_version`, `template\_version - version of the config files used to render this database and to decode URL's (foreign key)
  * `url_decoding_version` - version of the config file containing the URL decoding rules (foreign key).

### `_presentation` ###
```
  table "_presentation" with
    (database       : DatabaseId, # key
     type          : Int,        # key
     version       : Int,        # key
     time          : Time,
     uid           : UserId,
     value         : String)
```
The `_presentation` table stores all versions of the three configuration files used to customize the look of a database.  These include a CSS file, a HTML template, and a layout file that controls how the entries and data trees are rendered.  These are editable through the Web interface.
  * `database` - the ID of the database to which this file belongs.
  * `type` - a number (CSS = 1, Template = 2, Layout = 3)
  * `version` - a version number
  * `time` - system time when file version was created.
  * `uid` - ID of the user that created the version
  * `value` - string value of the file

**Constraints:** The combination of `database`, `type` and `version` is unique.

## The database tables ##

For each database (e.g. DB), there are six tables (e.g. DB\_data, DB\_schema, etc.)

### `DB_data` ###

```
   table ("DB_data") with
      (id           : NodeId,  # key
       schema       : SchemaId,
       parent       : NodeId,
       entry        : Int,
       value        : String,
       timesequence : TimesequenceId,
       pre          : Int,
       post         : Int)
```
The `DB_data` table holds the tree nodes that constitute the primary data of the database.
  * `id` - unique key
  * `schema` - foreign key into Schema saying what the type of this node is
  * `parent` - foreign key into DB\_data pointing to parent node
  * `entry` - an integer tagging each top-level tree in the DB.  (That is, the DB is a set of entries, each a tree with a unique id).
  * `value` - the string value of the node (meaningful only for attributes)
  * `timesequence` - a timesequence ID which represents the set of intervals during which this node is valid. a timesquence ID of -1 should be interpreted as "inherit from parent"
  * `pre` - nth node visited in pre order traversal
  * `post` - nth node visited in post order traversal

**Note:** `pre` and `post` numbers are per `entry`. They are also not unique even within a single entry. Text nodes share their parent's pre and post number.

**Constraints:**
  1. For each `entry` value, the set of nodes with that value should form a tree.
  1. If `schema` is `Element` then the `value` string is ignored (i.e. should be NULL).
  1. `schema` can be -1. I (Stefan) believe this means we are dealing with a text node, so its `value` is non-null, and it shares its parent's pre and post number.

### `DB_timesequence` ###
```
    table ("DB_timesequence") with
      (id    : TimesequenceId,
       start : VersionNumber,
       stop  : VersionNumber)
```
The `DB_timesequence` table contains sets of intervals named by ids.
  * `id` - an id representing a set of intervals.  Not unique.
  * `start`, `stop` - start and stop times of an interval.  The value `-1` in `stop` stands for "now".

**Constraints**: No two intervals associated with the same `id` overlap.

### `DB_schema` ###
```
    table ("DB_schema") with 
      (id           : SchemaId, # key
       type         : SchemaType,
       name         : String,
       parent       : SchemaId,
       uid          : UserId,
       timesequence : TimesequenceId)
```

The `DB_schema` table contains the schema (type structure) of the database.
  * `id` - A unique key identifying the schema node
  * `type` - An integer representing Attribute (0) or Group (1) (also see above).
  * `name` - A string giving the name of the schema node.
  * `parent` - A foreign key reference to the schema id of the parent
  * `uid` - ??
  * `timesequence` - An identifier for the set of intervals when this schema node is valid. **Note:** This field is only used by the schema-versioning fork (it doesn't do any harm to have it in the trunk version though).  This will be folded in soon.

**Constraints:** The id-parent edges should form a tree.

### `DB_version` ###
```
    table ("DB_version") with 
      (id         : VersionNumber, # key
       name       : Date,
       provenance : Provenance,
       time       : Time,
       uid        : UserId,
       source     : String,
       node  	  : NodeId)
```
The `DB_version` table
  * `id` - a unique id (version number)
  * `name` - a string representation of the version's creation time.  Should be consistent with `time`.
  * `provenance` - an integer representing the type of `Provenance` action that created the version
  * `time` - system time of version creation.
  * `uid` - id of user that created the table.
  * `source` - a string (max 80 characters) describing the source of the version, if it was copied or imported.

**Note:** This table is likely to be factored into a separate "provenance" table.

**Constraints:**
  1. `source` is only meaningful for `COPY` and `IMPORT`

### `DB_annotation` ###
```
    table ("DB_annotation") with 
      (id     : AnnotationId, # key
       node : NodeId,
       parent : AnnotationId,
       date : Date,
       text : String,
       uid : UserId)
```
The `DB_annotation` table collects annotations on a given node.  These could in principle be nested but at the moment the UI does not support either behavior.  Nor can annotations be edited or deleted after being created.  Annotations are not versioned.
  * `id` - a unique annotation id
  * `node` - target node id of the annotation
  * `parent` - parent annotation (currently not used AFAIK)
  * `date` - string representation of date annotation was made.  (Should be replaced by system time)
  * `text` - the text of the annotation
  * `uid` - user id of the creator of the annotation

### `DB_pages` ###
```
    table ("DB_pages") with 
      (id : PageId, # key
       name : String,
       content : String,
       timesequence : Time,
       uid : UserId)
```
The `DB_pages` table collects wiki pages relevant to `DB`.  Each page is basically a string.  As with annotations, there is no versioning for these pages.
  * `id` - Wiki page unique id
  * `name` - title of the page, usually in `CamelCase`
  * `content` - markdown representation for the wiki page content (some embedded queries allowed)
  * `timestamp` - A system time indicating when the page was created.
  * `uid` - user ID of the creator of (this version of the) page.

## PostgreSQL schema ##

This section collects commands for creating the schema from scratch. using PostgreSQL.  The `create_server` script, followed by the GUI database creation commands or scripts `database-import` and `import-presentation-files` ought to do this for you, but the schema creation code provides more detailed documentation of the types used in PostgreSQL.  See also `PSQLDatabaseConnector.java`.

```
CREATE TABLE _presentation
(
  database integer NOT NULL,
  type integer NOT NULL,
  version integer NOT NULL,
  time bigint NOT NULL,
  uid integer NOT NULL,
  "value" text NOT NULL,
  CONSTRAINT _presentation_pkey PRIMARY KEY (database, type, version)
);

CREATE TABLE _database
(
  id serial NOT NULL,
  "name" character varying(16) NOT NULL,
  title character varying(80) NOT NULL,
  authentication integer NOT NULL,
  auto_schema_changes integer NOT NULL,
  css_version integer NOT NULL DEFAULT (-1),
  layout_version integer NOT NULL DEFAULT (-1),
  template_version integer NOT NULL DEFAULT (-1),
  uid integer NOT NULL,
  is_active integer NOT NULL DEFAULT 1,
  CONSTRAINT _database_pkey PRIMARY KEY (id),
  CONSTRAINT _database_name_key UNIQUE (name)
);

CREATE TABLE _user
(
  id serial NOT NULL,
  "login" character varying(80) NOT NULL,
  full_name character varying(80) NOT NULL,
  "password" character varying(80) NOT NULL,
  CONSTRAINT _user_pkey PRIMARY KEY (id),
  CONSTRAINT _user_login_key UNIQUE (login)
);

CREATE TABLE DB_annotation
(
  id serial NOT NULL,
  node integer NOT NULL,
  parent integer,
  date character varying(80) NOT NULL,
  "text" character varying(4000) NOT NULL,
  uid integer NOT NULL,
  CONSTRAINT DB_annotation_pkey PRIMARY KEY (id)
);

CREATE TABLE DB_data
(
  id serial NOT NULL,
  schema integer NOT NULL,
  parent integer NOT NULL,
  entry integer NOT NULL,
  "value" text,
  timesequence integer NOT NULL DEFAULT (-1),
  CONSTRAINT DB_data_pkey PRIMARY KEY (id)
);

CREATE TABLE DB_pages
(
  id serial NOT NULL,
  "name" character varying(255) NOT NULL,
  "content" text NOT NULL,
  "timestamp" bigint NOT NULL,
  uid integer NOT NULL,
  CONSTRAINT DB_pages_pkey PRIMARY KEY (id)
);

CREATE TABLE DB_schema
(
  id integer NOT NULL,
  "type" integer NOT NULL,
  "name" character varying(255) NOT NULL,
  parent integer NOT NULL,
  uid integer NOT NULL,
  CONSTRAINT DB_schema_pkey PRIMARY KEY (id),
  CONSTRAINT DB_schema_name_parent_key UNIQUE (name, parent)
);


CREATE TABLE DB_timesequence
(
  id serial NOT NULL,
  start integer NOT NULL,
  stop integer NOT NULL,
  CONSTRAINT DB_timesequence_pkey PRIMARY KEY (id, start)
);

```

### Views ###

There are also some views defined on the raw tables above: `DB_vdata` contains one row for each node, time interval and associated annotations.  This is used in the main function that fetches `DatabaseNode`s from the database by node id (`DatabaseReader.java`). `DB_veindex` collects the maximum number of nodes matching a given schema node for each (`schema`, `entry`) pair.  This is used to evaluate queries that involve node position constraints (`QueryEvaluator.java`).

```
CREATE  VIEW DB_vdata AS 
 SELECT d.id AS n_id, 
        d.parent AS n_parent, 
        d.schema AS n_schema, 
        d.entry AS n_entry, 
        d.value AS n_value, 
        t.start AS t_start, 
        t.stop AS t_stop, 
        a.id AS a_id, 
        a.date AS a_date, 
        a.uid AS a_uid, 
        a.text AS a_text
   FROM DB_data d
   LEFT JOIN DB_timesequence t ON d."timesequence" = t.id
   LEFT JOIN DB_annotation a ON d.id = a.node;

CREATE VIEW DB_veindex AS 
 SELECT q.entry, q.schema, max(q.cnt) AS max_count
   FROM ( SELECT DB_data.entry, 
                 DB_data.parent, 
                 DB_data.schema, 
                 count(*) AS cnt
           FROM DB_data
          WHERE DB_data.schema >= 0
          GROUP BY DB_data.entry, DB_data.parent, DB_data.schema) q
  GROUP BY q.entry, q.schema;
```

### MySQL schema ###

MySQL is also supported (but not extensively tested).  The schemas are similar but not identical to the above; see `MySQLDatabaseConnector.java` for the differences.