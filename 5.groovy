@Grapes([
    @Grab(group='org.ow2.asm', module='asm', version='4.2'),
    @Grab(group='com.orientechnologies', module='orientdb-core', version='1.6.3'),
    @Grab(group='com.orientechnologies', module='orient-commons', version='1.6.3'),
    @Grab(group='com.orientechnologies', module='orientdb-tools', version='1.6.4')
])

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx
import com.orientechnologies.orient.core.record.impl.ODocument


def ORIENTDB_URI = "local:./orientDB"
def ORIENTDB_USER = "admin"
def ORIENTDB_PASS = "admin"

ODatabaseDocumentTx db = new ODatabaseDocumentTx(ORIENTDB_URI)
try {
    db.open(ORIENTDB_USER, ORIENTDB_PASS)
} catch (Exception e) {
    db.create()
}

class MyVisitor extends org.objectweb.asm.ClassVisitor {

    def _file
    def _class
    def _methods = []
    def _fields = []
  
    MyVisitor() {
	    super(org.objectweb.asm.Opcodes.ASM4, null)
	  }
	  
	  void visit(int version, int access, String name, String signature, String superName, String[] interfaces){
	      _class = new ODocument("Class")  
	    
	      _class.field("file", _file)
	      _class.field("varsion", version)
        _class.field("access", access)
        _class.field("name", name.replace("/", "."))
        try{ 
            def (_pkg, _cls) = name.split(/[.](?=[^.]+$)/)
            _class.field("package", _pkg)
            _class.field("class", _cls)
        } catch(Exception e){}
        _class.field("super", superName.replace("/",".")) 
        _class.field("interfaces", interfaces?.collect({ it.replace("/",".") }).join(", ")) 
    }
    
    void visitEnd() {
        _class.field("methods", _methods)
        _class.field("fields", _fields)
        _class.save()
    }
    
    org.objectweb.asm.FieldVisitor visitField(int access, String name, String desc, String signature, Object value){
        def _field = new ODocument("Field")
        
        _field.field("class", _class)
        _field.field("name", name)
        _field.field("type", org.objectweb.asm.Type.getReturnType(desc).getClassName() )
        _field.field("access", access)
        _field.field("value", value)
        
        _fields.push(_field)
        
        return super.visitField(access, name, desc, signature, value)
    }

    org.objectweb.asm.MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions){
        def _method = new ODocument("Method")  
        
        _method.field("class", _class)
        _method.field("name", name)
        _method.field("method", name + "(" + org.objectweb.asm.Type.getArgumentTypes(desc).collect({ it.getClassName() }).join(", ") + ")")
        _method.field("return", org.objectweb.asm.Type.getReturnType(desc).getClassName() )
        _method.field("access", access)
        _method.field("exceptions", exceptions?.join(", "))
        
        _methods.push(_method)
        
        return super.visitMethod(access, name, desc, signature, exceptions)
    }
    
    def scanDirectory(File dir){
        dir.eachFileRecurse(groovy.io.FileType.FILES) { f ->
          if(f.name.endsWith('.jar')) {
            //println("File: " + f)
            def zip = new java.util.zip.ZipFile(f) 
            zip.entries().each { entry->
              if (entry.name.endsWith(".class")) {
                //println("Class file: " + entry)
                _file = entry.toString()
                
                def is = zip.getInputStream(entry)
                def cr = new org.objectweb.asm.ClassReader(is)
                cr.accept(this, 0)
                is.close()
        } } } }
    }
}

def visitor = new MyVisitor()
visitor.scanDirectory( new File("D:\\hybris\\bin\\platform\\bootstrap\\") )

db.close()


import com.orientechnologies.orient.console.OConsoleDatabaseApp

def console = new OConsoleDatabaseApp()
console.connect(ORIENTDB_URI, ORIENTDB_USER, ORIENTDB_PASS)
console.run()
