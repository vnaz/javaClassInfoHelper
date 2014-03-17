@Grab(group='berkeleydb', module='je', version='3.2.44')


class BDBInterface {
	
	com.sleepycat.je.Database myDatabase = null 
	
	BDBInterface(){
		println("123")
		def envConfig = new com.sleepycat.je.EnvironmentConfig()
		envConfig.setAllowCreate(true)
		
		def myDbEnvironment = new com.sleepycat.je.Environment(new File('.'), envConfig)
		
		def dbConfig = new com.sleepycat.je.DatabaseConfig()
		dbConfig.setAllowCreate(true)
		dbConfig.setDeferredWrite(true)
	
		myDatabase = myDbEnvironment.openDatabase(null, 'sampleDatabase', dbConfig)
	}
	
	def close(){
		myDatabase.sync()
		myDatabase.close()
		myDbEnvironment.close()
	}
	
	def put(String key, String value){
		def theKey = new com.sleepycat.je.DatabaseEntry(key.getBytes("UTF-8"))
		def theData = new com.sleepycat.je.DatabaseEntry(value.getBytes("UTF-8"))
		myDatabase.put(null, theKey, theData)	
	}
	
	def get(String key){
		def theKey = new com.sleepycat.je.DatabaseEntry(key.getBytes("UTF-8"))
		def theData = new com.sleepycat.je.DatabaseEntry()
		myDatabase.get(null, theKey, theData, com.sleepycat.je.LockMode.DEFAULT)
		//if ( 	== com.sleepycat.je.OperationStatus.SUCCESS){}
		
		return theData
	}
}

db = new BDBInterface()
db.put("1", "123")
db.put("2", "333")
println db.get("2")
db.close()
