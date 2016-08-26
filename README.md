# hibernate-mapping-enhancer
Hibernate mapping enhancing service. Allows to automatically create indexes on Foreign Keys and to index enum columns

Key features:

Automatically create indexes on all generated Foreign Keys including join tables
Exclude specific foreign keys from indexing
Automatically create indexes on columns representing enums
Exclude specific enum columns from indexing
The library works independent of the actual DBMS utilizing existing Schema change capabilities to create indexes as required.

The library works "out of box" by indexing all foreign keys and enums - simply put it on the classpath. In order to have fine grained control of the columns indexes please refer to 
https://github.com/kibork/hibernate-mapping-enhancer/wiki/Sample-configuration-file
