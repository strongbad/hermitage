Isolation Test Suite
====================
A java implementation of the isolation tests documented at https://github.com/ept/hermitage. This is still a work in progress but can be used to experiment with and test different isolation levels. 

Setup:

```sql
create table test (id int primary key, value int);
```

Configuration:

Please adjust the data source properties to match your database's configuration. These are the three property files that can be tweaked for different configurations:
> application.properties\
> application-mysql.properties\
> application-postgres.properties