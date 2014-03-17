@Grapes([
    @Grab(group='org.ow2.asm', module='asm', version='4.2'),
    @Grab(group='com.h2database', module='h2', version='1.3.175'),
    @GrabConfig(systemClassLoader=true)
])
    
import groovy.io.FileType
import groovy.sql.Sql

class MyVisitor extends org.objectweb.asm.ClassVisitor {
    
    def cur_class
    def sql
    
	MyVisitor() {
	    super(org.objectweb.asm.Opcodes.ASM4, null)
	    
	    sql = Sql.newInstance("jdbc:h2:h2.db", "sa", "", "org.h2.Driver")

        def query = """CREATE TABLE IF NOT EXISTS classes (
                     id bigint auto_increment, 
                     class varchar, 
                     package varchar, 
                     name varchar, 
                     type varchar, 
                     PRIMARY KEY (id) );""" 
        sql.execute(query)
	}
	
	void visit(int version, int access, String name, String signature, String superName, String[] interfaces){
		def cls = name.replace("/", ".") + " extends " + superName.replace("/",".")
		if (interfaces.length >0){
			cls += " implements " + interfaces.collect({ it.replace("/",".") }). join(", ")
		}
		cur_class = cls
		println("Class: " + cls)
	}
	
	org.objectweb.asm.FieldVisitor visitField(int access, String name, String desc, String signature, Object value){
		def _name = org.objectweb.asm.Type.getReturnType(desc).getClassName() + " " + name + " = " + value
		
		//println("Field: " + out)
		sql.execute("INSERT INTO classes(class, name, type) VALUES (${cur_class}, ${_name}, 'field');")
		
		return super.visitField(access, name, desc, signature, value)
	}
	
	org.objectweb.asm.MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions){
		//def _name = org.objectweb.asm.Type.getReturnType(desc).getClassName() + " " + 
		def _name = name + "(" + org.objectweb.asm.Type.getArgumentTypes(desc).collect({ it.getClassName() }).join(", ") + ")"
		
        //println("Method: " + out)
        sql.execute("INSERT INTO classes(class, name, type) VALUES (${cur_class}, ${_name}, 'method');")
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