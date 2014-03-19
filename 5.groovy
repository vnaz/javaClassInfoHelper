@Grapes([
    @Grab(group='com.orientechnologies', module='orientdb-core', version='1.6.3'),
    @Grab(group='com.orientechnologies', module='orient-commons', version='1.6.3'),
    @Grab(group='com.orientechnologies', module='orientdb-tools', version='1.6.4')
])

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx
import com.orientechnologies.orient.core.record.impl.ODocument

import com.orientechnologies.orient.console.OConsoleDatabaseApp

def ORIENTDB_URI = "local:./orientDB"
def ORIENTDB_USER = "admin"
def ORIENTDB_PASS = "admin"

ODatabaseDocumentTx db = new ODatabaseDocumentTx(ORIENTDB_URI)
try {
    db.open(ORIENTDB_USER, ORIENTDB_PASS)
} catch (Exception e) {
    db.create()
}

ODocument cls = new ODocument("Class");
cls.field( "name", "TestClass" );
cls.field( "file", "Some" );
cls.save();

db.close()


def console = new OConsoleDatabaseApp()
console.connect(ORIENTDB_URI, ORIENTDB_USER, ORIENTDB_PASS)
console.run()
