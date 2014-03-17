@GrabConfig(systemClassLoader=true)
@Grab(group='com.h2database', module='h2', version='1.3.175')

import groovy.sql.Sql

def sql = Sql.newInstance("jdbc:h2:h2.db", "sa", "", "org.h2.Driver")
sql.execute("CREATE TABLE classes (id bigint auto_increment, class varchar, package varchar, PRIMARY KEY (id) )")