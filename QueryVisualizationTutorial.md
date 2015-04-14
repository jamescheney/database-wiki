# Introduction #

This page gives a short tutorial on how to write queries and visualizations, using the CIAWFB\_CLEAN data as an example.  The last section gives an extended example that can be copied straight into a DBWiki wiki page and that illustrates many of the available features.

# Loading CIAWFB\_CLEAN #

For these examples, you can use a cleaned-up version of the CIAWFB data that is easier to use to write queries.  This is simple using the database import scripts (assuming you already have a working server).

```
$ sh database-import.sh CIAWFB_CLEAN CIA_World_Factbook /CIAWFB_CLEAN/COUNTRY data/CIAWFB_CLEAN/CIAWFB_CLEAN_20110506.xml.gz Admin
```

or, using Windows:

```
> database-import.cmd CIAWFB_CLEAN CIA_World_Factbook /CIAWFB_CLEAN/COUNTRY data/CIAWFB_CLEAN/CIAWFB_CLEAN_20110506.xml.gz Admin
```

In either case, do this from the main database wiki directory.

It's also probably a good idea to install the presentation files, so that the data will be easier to browse:

```
$ sh import-presentation-files.sh CIAWFB_CLEAN data/CIAWFB_CLEAN/presentation/ Admin
```

or on Windows:

```
> import-presentation-files.cmd CIAWFB_CLEAN data/CIAWFB_CLEAN/presentation/ Admin
```
# Queries #

Queries can be embedded in wiki pages.  Go to the "View" menu and then pick the "Wiki" entry.  This should show you an index of wiki pages.  If you click on "Front page" and then "Edit" you should be able to enter text.

Text can include queries on the structured database.  For example, to display the countries in South America write the following and click "Save":

```
 ?SELECT $c/NAME as country FROM $c IN /COUNTRY
 WHERE $c/CATEGORY[NAME='Geography']/PROPERTY[NAME='Map references']/VALUE='South America'?
```

Note that the two question marks are important: they tell the wiki markup parse to parse the text between them as a query, not as ordinary wiki text.

Here's another query, and there is an extended example at the end of this page.

Capital cities of South America:

```
?SELECT $c/CATEGORY[NAME='Government']/PROPERTY[NAME='Capital']/SUBPROP[NAME='name']/VALUE AS capital,
 $c/NAME AS country
 FROM $c IN /COUNTRY
 WHERE $c/CATEGORY[NAME='Geography']/PROPERTY[NAME='Map references']/VALUE='South America'?
```

# Charts #

DBWiki currently supports bar charts and pie charts, using the Google Charts API service.

## Bar charts ##

To make a chart, first write a query that returns at least two columns.  The first column can be any text, while the rest should return numeric data.  For example, to get the population of each South American country do this:

```
 ?CHART:SELECT $c/NAME as country, $p/VALUE as population
 FROM $c IN /COUNTRY, $p IN $c/CATEGORY[NAME='People']/PROPERTY[NAME='Population']
 WHERE $c/CATEGORY[NAME='Geography']/PROPERTY[NAME='Map references']/VALUE='South America'?
```


Then, add the prefix "CHART:" to the beginning of the query, as follows:

```
 ?CHART:SELECT $c/NAME as country, $p/VALUE as population
 FROM $c IN /COUNTRY, $p IN $c/CATEGORY[NAME='People']/PROPERTY[NAME='Population']
 WHERE $c/CATEGORY[NAME='Geography']/PROPERTY[NAME='Map references']/VALUE='South America'?
```

Here is another example:



## Pie charts ##

Similarly, DBWiki also supports pie charts.  For these, there should be one key field and one numeric field.  For example:

```
 ?PIE:SELECT $c/NAME as country, $p/VALUE as population
 FROM $c IN /COUNTRY, $p IN $c/CATEGORY[NAME='People']/PROPERTY[NAME='Population']
 WHERE $c/CATEGORY[NAME='Geography']/PROPERTY[NAME='Map references']/VALUE='South America'?
```

Here is another example, communications users by country:

```
 ?CHART:SELECT $c/NAME AS country, $u/VALUE AS internet, $f/VALUE AS fixed_line, $m/VALUE AS mobile
 FROM $c IN /COUNTRY,
 $u IN $c/CATEGORY[NAME='Communications']/PROPERTY[NAME='Internet users'],
 $f IN $c/CATEGORY[NAME='Communications']/PROPERTY[NAME='Telephones - main lines in use'],
 $m IN $c/CATEGORY[NAME='Communications']/PROPERTY[NAME='Telephones - mobile cellular']
 WHERE $c/CATEGORY[NAME='Geography']/PROPERTY[NAME='Map references']/VALUE='South America'?
```

# Maps #

Finally, to generate points on a map, write a query that returns one or more columns that make sense as locations.  If you add the prefix "MAP:" then each row will be turned into a single string and sent off to Google Maps (after an attempt to resolve the location to a geographic reference).  For example:

```
 ?MAP:SELECT $c/CATEGORY[NAME='Government']/PROPERTY[NAME='Capital']/SUBPROP[NAME='name']/VALUE AS capital,
 $c/NAME AS country
 FROM $c IN /COUNTRY
 WHERE $c/CATEGORY[NAME='Geography']/PROPERTY[NAME='Map references']/VALUE='South America'?
```


# Performance #

Note that many of the queries discussed above are currently implemented by loading a lot of data from the database.  Hence, they are slow.

# Extended example #

```
# Facts about South America
 
## Countries of South America:
 
 ?SELECT $c/NAME as country FROM $c IN /COUNTRY
 WHERE $c/CATEGORY[NAME='Geography']/PROPERTY[NAME='Map references']/VALUE='South America'?
 
## Capital cities in South America:
 
 ?MAP:SELECT $c/CATEGORY[NAME='Government']/PROPERTY[NAME='Capital']/SUBPROP[NAME='name']/VALUE AS capital,
 $c/NAME AS country
 FROM $c IN /COUNTRY
 WHERE $c/CATEGORY[NAME='Geography']/PROPERTY[NAME='Map references']/VALUE='South America'?
 
## Population of countries in South America:
 
 ?CHART:SELECT $c/NAME as country, $p/VALUE as population
 FROM $c IN /COUNTRY, $p IN $c/CATEGORY[NAME='People']/PROPERTY[NAME='Population']
 WHERE $c/CATEGORY[NAME='Geography']/PROPERTY[NAME='Map references']/VALUE='South America'?
 
 
 
 
## Telecommunications users in South America:
 
 ?CHART:SELECT $c/NAME AS country, $u/VALUE AS internet, $f/VALUE AS fixed_line, $m/VALUE AS mobile
 FROM $c IN /COUNTRY,
 $u IN $c/CATEGORY[NAME='Communications']/PROPERTY[NAME='Internet users'],
 $f IN $c/CATEGORY[NAME='Communications']/PROPERTY[NAME='Telephones - main lines in use'],
 $m IN $c/CATEGORY[NAME='Communications']/PROPERTY[NAME='Telephones - mobile cellular']
 WHERE $c/CATEGORY[NAME='Geography']/PROPERTY[NAME='Map references']/VALUE='South America'?
 
 
## Composition of Internet users in South America:
 
 ?PIE:SELECT $c/NAME AS country, $u/VALUE AS internet
 FROM $c IN /COUNTRY,
 $u IN $c/CATEGORY[NAME='Communications']/PROPERTY[NAME='Internet users']
 WHERE $c/CATEGORY[NAME='Geography']/PROPERTY[NAME='Map references']/VALUE='South America'?
 
 
## Countries modified since 1pm on the 1st of December:
 
 ?SELECT $c/NAME as country FROM $c IN /COUNTRY
 WHERE $c/CATEGORY[NAME='Geography']/PROPERTY[NAME='Map references']/VALUE='South America'
 AND $c WAS MODIFIED SINCE 2011-12-01 13:00 BY 'jcheney'?
 GDP per capita ($):
 
 ?CHART:SELECT $c/NAME as country, $g/VALUE AS GDP_per_capita
 FROM $c IN /COUNTRY,
 $g IN $c/CATEGORY[NAME='Economy']/PROPERTY[NAME='GDP - per capita (PPP)']
 WHERE $c/CATEGORY[NAME='Geography']/PROPERTY[NAME='Map references']/VALUE='South America'?
 
## Composition of GDP ($) by country in South America:
 
 ?PIE:SELECT $c/NAME AS country, $g/VALUE AS GDP
 FROM $c IN /COUNTRY,
 $g IN $c/CATEGORY[NAME='Economy']/PROPERTY[NAME='GDP (official exchange rate)']
 WHERE $c/CATEGORY[NAME='Geography']/PROPERTY[NAME='Map references']/VALUE='South America'?
 
## Composition of GDP ($) by sector in Brazil:
 
 ?PIE:SELECT $s/NAME AS sector, $s/VALUE AS percent
 FROM $g IN /COUNTRY[NAME='Brazil']/CATEGORY[NAME='Economy']/PROPERTY[NAME='GDP - composition by sector'],
 $s IN $g/SUBPROP?
 
 
## Composition of GDP ($) by sector in South America:
 
 ?CHART:SELECT $c/NAME AS country, $a/VALUE AS agriculture, $i/VALUE AS industry, $s/VALUE AS services
 FROM $c IN /COUNTRY,
 $g IN $c/CATEGORY[NAME='Economy']/PROPERTY[NAME='GDP - composition by sector'],
 $a IN $g/SUBPROP[NAME='agriculture'],
 $i IN $g/SUBPROP[NAME='industry'],
 $s IN $g/SUBPROP[NAME='services']
 WHERE $c/CATEGORY[NAME='Geography']/PROPERTY[NAME='Map references']/VALUE='South America'?
```