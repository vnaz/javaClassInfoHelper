@GrabConfig(systemClassLoader=true)
@Grab(group='com.h2database', module='h2', version='1.3.175')

import groovy.sql.Sql

def sql = Sql.newInstance("jdbc:h2:h2.db", "sa", "", "org.h2.Driver")

query = """CREATE TABLE IF NOT EXISTSS classes (
             id bigint auto_increment, 
             class varchar, 
             package varchar, 
             name varchar, 
             type varchar, 
             PRIMARY KEY (id) )""" 
sql.execute(query)

sql.execute("INSERT INTO classes(class, package, name, type) VALUES (${firstName}, ${lastName})")