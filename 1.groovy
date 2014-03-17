@Grab(group='org.ow2.asm', module='asm', version='4.2')
import groovy.io.FileType

class MyVisitor extends org.objectweb.asm.ClassVisitor {
	MyVisitor() { super(org.objectweb.asm.Opcodes.ASM4, null) }

	
	void visit(int version, int access, String name, String signature, String superName, String[] interfaces){
		def out = name.replace("/", ".") + " extends " + superName.replace("/",".")
		if (interfaces.length >0){
			out += " implements " + interfaces.collect({ it.replace("/",".") }). join(", ")
		}
		println("Class: " + out)
	}
	
	org.objectweb.asm.FieldVisitor visitField(int access, String name, String desc, String signature, Object value){
		def out = org.objectweb.asm.Type.getReturnType(desc).getClassName() + " " + name + " = " + value
		//println("Field: " + out)
		return super.visitField(access, name, desc, signature, value)
	}
	
	org.objectweb.asm.MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions){
		def out = org.objectweb.asm.Type.getReturnType(desc).getClassName() + " " + name + "(" +
		 	      org.objectweb.asm.Type.getArgumentTypes(desc).collect({ it.getClassName() }).join(", ") + ")"
        //println("Method: " + out)
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