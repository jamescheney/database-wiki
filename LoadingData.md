# Introduction #

Instructions for loading data into DatabaseWiki.


## Expected format ##

DatabaseWiki currently doesn't handle arbitrary XML, only a simple subset.  Basically, it expects that each element is of the form:

  * Text content only with no attributes (e.g. `<a>foo</a>`).  This will be translated to an "attribute" node.
  * Some attributes and element children, but no (nontrivial) text content.  This will be translated to an "element" node.
  * If multiple elements appear in the same child list, then they are expected to have similar (mergeable) content.  For example `<a><b>foo</b><b><c>bar</c></b></a>` would yield strange behavior since `b` acts like an attribute in one place and like an element in the other.

Note that this is a somewhat broken model.  We are working on cleaning it up.

# Loading data through the web interface #

To import data through the web interface, you need:

  * Some data, stored in an XML file meeting the above constraints, and hosted on the Web somewhere (say, `http://example.com/foo.xml`)
  * Or, a schema that describes the data you want to load (see SchemaLanguageDocumentation)
  * a running DatabaseWiki system

Click on `New` from the server root page.  This takes you to a form.  Fill in the short name, long name and URL fields.  The schema can be skipped if you have an XML document.  Fill in the XML document.  If no schema is specified, the response page will have tried to infer a reasonable schema and listed it above the schema textbox.


# Loading data through the import script #

To import data by loading in from the import script, you need:

  * Some data, stored in an XML file (e.g. `foo.xml`), meeting the above constraints, somewhere in your file system.  It is OK if the XML document is compressed using GZip; the system will figure this out from the extension and decompress.
  * a running DatabaseWiki system with a database already created called `DB`.  If you know the schema you will ultimately want, you can create an empty database using the schema, otherwise, loading a small XML file that shows the template of your data should be enough to get started.
  * a path in the XML document whose content gives all of the trees you want to insert into the root of the database.  (e.g. `/path/step`)
  * a valid `userid` indicating who should be recorded as having entered the data.  (TODO: This should require password checking eventually, but right now we assume if you are running from the command line then you are the administrator).

Suppose the data is in `foo.xml`.  Do:

```
sh database-import.sh DB DatabaseName /path/step foo.xml userid
```

This should load the data in much the same way as loading through the web form above, **provided** that it matches the existing schema (or silent schema extension is enabled).  Note that this is a wrapper on the `DatabaseImport` class that feeds in your config file by default (along with lots of classpath mumbo jumbo).

Importing should work while the server is running, but you need to restart the server to see the new database.

# Loading presentation files #

DatabaseWiki uses three presentation files to customize the HTML and CSS templates used for a data source and produce an easier-to-read view of the data.  These can be loaded from files using the `import-presentation-files.sh` script:

```
sh import-presentation-files DB /path/dir userid
```

This will load in the presentation files for database DB from a given path (it will expect files named DB.css, DB.html and DB.layout in directory `path/dir`), acting as user `userid`.

## Example ##

To load the data in CIAWFB as the Admin user do the following:

```
sh database-import.sh CIAWFB CIA_World_Factbook /CIAWFB/COUNTRY data/CIAWFB/CIAWFB_20100124_064501.xml.gz Admin
```

Similarly, to load the presentation for CIAWFB do the following:
```
sh import-presentation-files.sh CIAWFB data/CIAWFB/presentation/ Admin
```