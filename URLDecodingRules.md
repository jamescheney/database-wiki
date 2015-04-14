# Introduction #

To allow more meaningful URL's (e.g., /CIAWFB/Chile/People/Population) a set of URL decoding rules can be specified. These rules define how to translate an URL into a node identifier. Note that each URL has to point to exactly to one node, otherwise an exception is thrown.

# Rules #

The general format of URL decoding rules is

```
IDENTIFY <absolute-path> BY <relative-path>
```

Absolute-path and relative-path are path expressions of node labels. An examples for the CIA World Factbook is:

```
IDENTIFY /COUNTRY BY NAME,
IDENTIFY /COUNTRY/CATEGORY BY NAME,
IDENTIFY /COUNTRY/CATEGORY/PROPERTY BY NAME,
IDENTIFY /COUNTRY/CATEGORY/PROPERTY/SUBPROP BY NAME
```

At most one rule can be defined per schema node. The rules can be specified in the user interface using menu item Settings/URL Decoding.

# URLs #

The supported URL format is

```
http::/server-url//db-name/[label | {label:}key-value]/...
```

For each path component, if the node label is given(i.e., _indexOf(':') != -1_) the decoding rule for that schema node is retrieved. Otherwise, we first test whether the given value is a valid node label for a child of the current node. If not, the current schema node has to have exactly one child with an URL decoding rule defined. An example URL is:

```
http::/localhost:8080/CIAWFB/Chile/People/Population
```

URL decoding starts at the root. The path component `Chile` points to the country node having exactly one NAME sub-node with value `Chile`. `People` then points to the CATEGORY node (only schema node child of COUNTRY with a defined rule) with a NAME sub-node having value `People`. `Population` refers to the PROPERTY with a NAME node `Population'. An alternative (long) form of the above URL is:

```
http::/localhost:8080/CIAWFB/COUNTRY:Chile/CATEGORY:People/PROPERTY:Population
```