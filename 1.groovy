@Grapes([
    @Grab(group='org.ow2.asm', module='asm', version='4.2'),
    @Grab(group='com.h2database', module='h2', version='1.3.175'),
    @GrabConfig(systemClassLoader=true)
])
    
import groovy.io.FileType
import groovy.sql.Sql

class MyVisitor extends org.objectweb.asm.ClassVisitor {
    
    def sql
    def md5 = java.security.MessageDigest.getInstance("MD5")
  
    def _cls
    def _file
    
    Integer i = 0;
    
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
        
        query = """CREATE TABLE IF NOT EXISTS methods (
                     id         varchar,
                     class      varchar, 
                     method     varchar,
                     returns    varchar,
                     access     varchar,
                     exceptions varchar,
                     PRIMARY KEY (id) );""" 
        sql.execute(query)
        
        query = """CREATE TABLE IF NOT EXISTS fields (
                     id         varchar, 
                     class      varchar, 
                     field      varchar, 
                     type       varchar, 
                     access     varchar, 
                     value      varchar,
                     PRIMARY KEY (id) );"""
        sql.execute(query)
	}
	
	
	
	void visit(int version, int access, String name, String signature, String superName, String[] interfaces){
        def _version = version
        def _access = access
        def _package = name.replace("/", ".")
        def _class = name.replace("/", ".")    
              try{ (_package, _class) = name.split(/[.](?=[^.]+$)/) }catch(Exception e){}
        def _super = superName.replace("/",".")
        def _interfaces = interfaces?.collect({ it.replace("/",".") }).join(", ")
        
        //md5.reset()
        //def _hash = (new BigInteger(md5.digest(name.bytes))).toString(16)
        def _hash = i++;
        
        _cls = _hash
        
        sql.execute("INSERT INTO classes(id, version, access, file, package, class, super, interfaces) VALUES ( ${_hash}, ${_version}, ${_access}, ${_file}, ${_package}, ${_class}, ${_super}, ${_interfaces} );")
	}
	
	org.objectweb.asm.FieldVisitor visitField(int access, String name, String desc, String signature, Object value){
        def _method = org.objectweb.asm.Type.getReturnType(desc).getClassName() + " " + name + " = " + value
        
        def _field = name
        def _type = desc
        def _access = access
        def _value = value.toString()
    
        //md5.reset()
        //def _hash = (new BigInteger(md5.digest(name.bytes))).toString(16)
        def _hash = i++;
        
        sql.execute("INSERT INTO fields(id, class, field, type, access, value) VALUES ( ${_hash}, ${_cls}, ${_field}, ${_type}, ${_access}, ${_value} );")
        
        return super.visitField(access, name, desc, signature, value)
	}
	
	org.objectweb.asm.MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions){
		    def _method = name + "(" + org.objectweb.asm.Type.getArgumentTypes(desc).collect({ it.getClassName() }).join(", ") + ")"
		    def _returns = org.objectweb.asm.Type.getReturnType(desc).getClassName()
		    def _access = access
		    def _exceptions = exceptions?.join(", ")

		    //md5.reset()
		    //def _hash = (new BigInteger(md5.digest((name + desc + signature).bytes))).toString(16)
		    def _hash = i++;
		    
        sql.execute("INSERT INTO methods(id, class, method, returns, access, exceptions) VALUES ( ${_hash}, ${_cls}, ${_method}, ${_returns}, ${_access}, ${_exceptions} );")
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
				visitor._file = entry.toString()
				def is = zip.getInputStream(entry)
				cr = new org.objectweb.asm.ClassReader(is)
				cr.accept(visitor, 0)
				is.close()
			}
		}
	}
}

org.h2.tools.Console.main()