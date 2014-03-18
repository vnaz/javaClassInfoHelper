@Grapes([
    @Grab(group='org.ow2.asm', module='asm', version='4.2'),
    @Grab(group='com.h2database', module='h2', version='1.3.175'),
    @GrabConfig(systemClassLoader=true)
])
    
import groovy.io.FileType
import groovy.sql.Sql

class MyVisitor extends org.objectweb.asm.ClassVisitor {
    
    String _hash
    String _file
    String _package
    String _class
    String _version
    String _access
    String _super
    String _interfaces
    
    def sql
    
	MyVisitor() {
	    super(org.objectweb.asm.Opcodes.ASM4, null)
	    
	    sql = Sql.newInstance("jdbc:h2:h2.db", "sa", "", "org.h2.Driver")

        def query = """CREATE TABLE IF NOT EXISTS classes (
                     id         varchar, 
                     version    varchar,
                     access     varchar,
                     file       varchar,
                     package    varchar,
                     class      varchar,
                     super      varchar,
                     interfaces varchar, 
                     PRIMARY KEY (id) );""" 
        sql.execute(query)
        
        def query = """CREATE TABLE IF NOT EXISTS methods (
                     id         varchar,
                     class      varchar, 
                     method     varchar,
                     access     varchar,
                     exceptions varchar,
                     PRIMARY KEY (id) );""" 
        sql.execute(query)
	}
	
	
	
	void visit(int version, int access, String name, String signature, String superName, String[] interfaces){
		_version = version
		_access = access
		_package = name.replace("/", ".")
	    _class = name.replace("/", ".")    
          try{ (_package, _class) = name.split(/[.](?=[^.]+$)/) }catch(Exception e){}
		_super = superName.replace("/",".")
		_interfaces = interfaces.collect({ it.replace("/",".") }).join(", ")
		
		sql.execute("INSERT INTO classes(id, version, access, file, package, class, super, interfaces) VALUES ( ${_hash}, ${_version}, ${_access}, ${_file}, ${_package}, ${_class}, ${_super}, ${_interfaces} );")
	}
	
	org.objectweb.asm.FieldVisitor visitField(int access, String name, String desc, String signature, Object value){
		def _method = org.objectweb.asm.Type.getReturnType(desc).getClassName() + " " + name + " = " + value
		
		//println("Field: " + out)
		sql.execute("INSERT INTO classes(id, class, method, access, exceptions) VALUES ( ${_hash}, ${_access}, ${_file}, ${_package}, ${_class}, ${_super}, ${_interfaces} );")
		
		return super.visitField(access, name, desc, signature, value)
	}
	
	org.objectweb.asm.MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions){
		//def _name = org.objectweb.asm.Type.getReturnType(desc).getClassName() + " " + 
		def _name = name + "(" + org.objectweb.asm.Type.getArgumentTypes(desc).collect({ it.getClassName() }).join(", ") + ")"
		
        //println("Method: " + out)
        sql.execute("INSERT INTO classes(id, class, method, access, exceptions) VALUES ( ${_hash}, ${_access}, ${_file}, ${_package}, ${_class}, ${_super}, ${_interfaces} );")
        return super.visitMethod(access, name, desc, signature, exceptions)
    }
}





def visitor = new MyVisitor()
def dir = new File("D:\\hybris\\bin\\platform\\bootstrap\\")
dir.eachFileRecurse(FileType.FILES) { f ->
	if(f.name.endsWith('.jar')) {
		//println("File: " + f)
		zip = new java.util.zip.ZipFile(f)
		entries = zip.entries()
		entries.each { entry->
			if (entry.name.endsWith(".class")) {
				//println("Class file: " + entry)
				def is = zip.getInputStream(entry)
				cr = new org.objectweb.asm.ClassReader(is)
				cr.accept(visitor, 0)
				is.close()
			}
		}
	}
}

org.h2.tools.Console.main()