@Grapes(@Grab(group='com.db4o', module='com.db4o', version='7.7.67'))

import com.db4o.*

class Cls {
  def a
  def b
}
//tmp = new Cls(a:1, b:22)
configuration = Db4oEmbedded.newConfiguration()
//configuration.objectClass(Cls).objectField("a").indexed(true);

DB4OFILENAME = "my_db4o"
ObjectContainer db = Db4oEmbedded.openFile(configuration, DB4OFILENAME)
//db.store(tmp)
db.query(Cls.class).each({println it.a})
db.close()