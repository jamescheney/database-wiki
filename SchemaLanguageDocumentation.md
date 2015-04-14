# Introduction #

This page documents the schema language used by Database Wiki.


# Details #

When loading data in through the Web form, the following syntax is used to describe its schema:

```
  <schema_root> ::= $<group>

  <group_or_attribute_list> ::= <attribute> | <element> (, <group_or_attribute_list>)*
  <group> ::= $label {  
                  <group_or_attribute_list>
                }
  <attribute> ::= @label
```

Attributes represent single nodes with text content.  Groups have  (unordered) groups of nodes as content.  Groups cannot have any immediate text content.  Also, an group matches zero, one or multiple data nodes having the corresponding label.