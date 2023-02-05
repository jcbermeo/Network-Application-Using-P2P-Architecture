
import java.io.* ;
import java.net.* ;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;


//||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||
public class Directory_Server { // A multi-threaded Server that will deal with multiple Clients concurrently.

	// Below is a HashMap where KEY=Username and the VALUE is a HashMap of files stored at the given
	// Username where the key is just an arbitrary fileID, and the value is the name of the file.
	// E.g. 
	// Martin_01 			fileID:1  	value:Lorem_Ipsum.txt
	//						fileID:2  	value:exampl_pic.jpg
	//
	// Juan_01				fileID:1  	value:example_file.txt
	//						fileID:2  	value:cloud-computing.jpg
	//						fileID:3  	value:hacker_1.jpg
	//						fileID:4  	value:hacker_2.jpg
	public static HashMap< String, HashMap< Integer,String >> INDEX =  new HashMap< String, HashMap< Integer,String >>() ;	


	public static HashMap< String,String > PEERs =  new HashMap< String,String >() ;
	// Above we have a HashMap of KEY=UserName , VALUE=IPAddress_Port ; IPAddress and Ports may change by the peerName 
	// will be treated as a Username. For this simple demo I will not use passwords--So not providing security YET.
	// E.g.
	// KEY="Martin_01"  VALUE="192.168.1.186_6004" 

	public static HashMap< String,String > USERs =  new HashMap< String,String >() ;
	// the above HashMap store the listening posts according to their corresponding usernames
	// KEY=username VALUE= client-side-port# server-side-port#
	// E.g.
	// KEY="Martin_01"  VALUE="6003 6004" 


	// The persistence is provided by a very simple .txt file. In the future it would be better to use Java Serialization
	// or even an SQL, but for this simple project this will do. 
	public static Path path_idx = Paths.get("index.txt") ;	// a simple out/input path to store/persist the above
	// INDEX as .txt files same location as *.java files
	// WARNING! index.txt must be present (even if empty)
	
	
	public static Path path_peers = Paths.get("peers.txt") ;


	//public static BufferedReader input = null ;
	public static DataInputStream input = null ;
	public static DataOutputStream output = null ;
	
	
	
	// Usually the range of available Port numbers is: (2^10)+1 ... (2^16)-1 i.e. 1023 ... 65537
	public static int port = 6000 ; // Let's listen at port 6000
	// alternatively we can also generate it at random e.g.: 
	//int port = ((int) ( 5000.0 * Math.random() )) + 5000 ;


	//..............................................................................................................
	public static void main(String[] args) {
		
		Helper helper = new Helper() ;


// STEP 1 : Broadcast its own IP ADDRESS and PORT number

		System.out.println( "\nDirectory Server is ON" ) ;

		// Server's PUBLIC IP ADDRESS
		//String address_public = whatsMyPublicIPAddress() ;
		//System.out.println( "Server Public IP Address is: " + address_public ) ;   // Server's Public IP Address

		// Servers's LOCAL IP ADDRESS
		String address_local = whatsMyLocalIPAddress() ;
		System.out.println( "IP Address: " + address_local ) ;   // Directory Server's Local IP Address
		System.out.println( "Port Numb : " + port ) ;


// STEP 2.1 : Retrieve the previously composed INDEX from index.txt file stored in the main directory (check if any)

		String path = path_idx.toString() ;
		File idx_file = new File( path ) ;
		
		if( idx_file.exists() ) { helper.load_Index( idx_file,INDEX ) ; }  show_INDEX() ;
		//if( idx_file.exists() ) { load_Index( idx_file ) ; }
				
// STEP 2.2 : Retrieve the previously composed PEERs from peers.txt (if any)
		
		String path_p = path_peers.toString() ;
		File file_peers = new File( path_p ) ;
		
		if( file_peers.exists() ){ helper.load_Peers( file_peers , PEERs ) ; } ; show_PEERs() ;
		
		
		
// STEP 3.1 : Initialize the listening Socket at port 6000
		
		//String client_addr_port = "",username_addr_port="" ; 
		String fileOwner="", owner_addr_port = "";
		
		String client_username="", client_address="", client_port="", index_client="", desired_filename="" ;

		try {

			ServerSocket serverSocket = new ServerSocket( port ) ; //Initialize the Server Socket object with Port=6000

			boolean isStopped = false ;	// the Server stays looping waiting for the clients
			while( !isStopped ) {
				// while-loop is needed so that the Server can accept multiple Clients where each Client gets its
				// separate thread--i.e. the Server can respond to (service) multiple clients concurrently.
				
				// reset all the above variables (populated by the previous client) for a new incoming client
				client_username=""; index_client=""; desired_filename=""; 
				fileOwner=""; owner_addr_port = ""; client_address="";
				
//				client_username=""; client_addr_port = ""; index_client=""; desired_filename=""; 
//				fileOwner=""; owner_addr_port = ""; username_addr_port=""; client_address="";

				
				
				
// STEP 3.2 : Wait for connections and Service the Clients:
//				registering their INDEX for the first time
//				checking what file they want and telling them where to go to download the desired file
//	 			updating INDEX after the given client received a file.
				
				System.out.println( "waiting for clients...  (to exit type: CTRL+C )" ) ;

				//Initialize Client Socket object via the serverSocket
				Socket clientSocket = serverSocket.accept() ;
				// After the ServerSocket object is created we call the accept() method. When ServerSocket accept()
				// method is called the Server WAITS for any Client requests (coming from remote machines). Once 
				// accept() returns an incoming socket the connection is established and the Server uses this socket to
				// communicate with the client. Ergo the clientSocket represents the the connection to/with the Client.
				// Each time a new Client connects to the server, the accept() method creates a new Socket object for that Client.

				InetAddress add = clientSocket.getInetAddress();	// get the IP Address of the client
				client_address = add.getHostAddress();

				System.out.println( "Client "+client_address+" is connected." ) ;

				
				//DataInputStream input = null ;
				//DataOutputStream output = null ;
				try {
				
					
// STEP 4:	Unpack DATA-Package from the client 
					

					// The DataInputStream will read the data from the above created clientSocket connection.
					// This data should contain a content akin to the following example:
					//      Martin_01 192.168.1.186_6004
					//      Martin_01 example.txt hacker1.jpg hacker2.jpg 
					//      examplePic.jpg
					input = new DataInputStream( clientSocket.getInputStream() ) ;				
					String data_from_client = input.readUTF() ;
					//System.out.println( data_from_client ) ;
					
					String[] data_from_client_ARR = data_from_client.split("\n",5) ; // expecting a 5 liner DATA-Package
					
					System.out.println( "\nClient sends the following DATA-Package: " ) ;
					for( String line: data_from_client_ARR )
						System.out.println( line ) ;
					
					
					// get the output stream to be able to reply to the Client
					output = new DataOutputStream( clientSocket.getOutputStream() );
					
					client_username = data_from_client_ARR[ 0 ] ; 	// e.g. Martin_01
					client_address  = data_from_client_ARR[ 1 ] ; 	// e.g. 192.168.1.187 	(PEER Server-Side Address)
					client_port     = data_from_client_ARR[ 2 ] ; 	// e.g. 6004			(PEER Server-Side Port Number)
					index_client	= data_from_client_ARR[ 3 ] ;	// e.g. example.txt hacker1.jpg hacker2.jpg
					desired_filename= data_from_client_ARR[ 4 ] ;	// e.g. examplePic.jpg
					
					desired_filename = desired_filename.trim(); // String.trim() method removes whitespaces (spaces, new lines etc.)
																// from the beginning and end of the string. There may be a "\n" here.
					
// STEP 5 : Update the INDEX (for that client) from the received DATA-Package
				
					
					helper.update_Index_line( client_username+" "+index_client, INDEX ) ;
					
					persist_INDEX( path_idx.toString() ) ;	// store INDEX into "index.txt"
			
					
// STEP 6:	Update PEERs for the entire network from the received DATA-Package
					
					
					helper.update_PEERs_line( client_username+" "+client_address+" "+client_port, PEERs ) ;
					
					helper.persist_PEERs( path_peers.toString(), PEERs ) ;	// store PEERs into "peers.txt"
						
					
// STEP 7 : Broadcast the current status of the P2P Network

					
					System.out.println( "Current Status of our P2P Network:" ) ;			
					show_INDEX() ;
					show_PEERs() ;


// STEP 8 	Check who is the OWNER of the requested file.
					
					if( !desired_filename.equals("DONE") ) {	// otherwise Client ONLY connected to update the INDEX 
																// but if NOT then...
						
						fileOwner = search_INDEX( desired_filename ) ; 		// fileOwner ← e.g. Juan_01

						if( fileOwner.equals( "" ) || fileOwner==null ){	// the requested file may NOT be present 

							System.out.println( "NO PEER has the file named: "+desired_filename ) ;
							System.out.println( "Thank you for registering your index_client.\n" ) ;
							
							output.writeUTF( "DONE" ) ;
							output.flush(); // send the message
							

							//continue ; // loop-up and wait for another client
							
						} else { // otherwise we found the requested file 

							owner_addr_port = PEERs.get( fileOwner ) ;	// owner_addr_port ← "192.168.1.186 6032"	

							if( owner_addr_port==null || owner_addr_port.equals("") ) { // if peers got corrupted

								System.out.println( "NO PEER has the file named: "+ desired_filename ) ;
								System.out.println( "Thank you for registering your index_client!\n" ) ;
								
								output.writeUTF( "DONE" ) ;
								output.flush(); // send the message
								

							} else { // otherwise tell the Client who the OWNER of the requested file is 

// STEP 9  	Create a Thread for the incoming client to service the request for file by telling the PEER who the OWNER of the file is
								
								//System.out.println( "\nGreat news! I found the file you requested." ) ;
								//System.out.println( "fileOwner:"+fileOwner+" owner_addr_port:"+owner_addr_port+"\n" ) ;

								// ClientThreadDir clientThread = new ClientThreadDir( clientSocket,desired_filename,client_username,fileOwner,owner_addr_port ) ;
								// clientThread.start() ;
								// After giving the OWNER info The Thread will terminate the Client connection by using socket_client.close() ;
								// Then another loop starts where the Server waits for another client.
								
								
// STEP 10 	Reply to the client with the file owner’s information

								
								// OR do it locally 
								System.out.println( "File owner found! The owner is: "+ fileOwner+" "+owner_addr_port ) ;

								output.writeUTF( fileOwner+" "+owner_addr_port ) ;	// owner_addr_port = Juan_01 192.168.1.187 6004
								output.flush(); // send the message
							}
						}
					} else { // The Client is ONLY updating the INDEX
						//clientSocket.close() ;	// close the connection since NOONE has the requested file
					}
					
					//input.close();
					//output.close(); // close the output stream when we're done.
					//clientSocket.close() ;

				} catch (Exception e) {
					e.printStackTrace() ;
				}	
			}// end of while-loop
		} catch( UnknownHostException e1 ) {
			System.out.println( "Unknown Host Exception "+ e1.toString() ) ;

		} catch( IOException e2 ) {
			//System.out.println( "IO Exception "+ e2.toString() ) ;
			System.out.println( "port number: "+port+" is already opened! use a different port number." ) ;

		} catch( IllegalArgumentException e3 ) {
			System.out.println( "Illegal Argument Exception "+ e3.toString() ) ;

		} catch( Exception e4 ) {
			System.out.println( "Other Exceptions "+ e4.toString() ) ;
		}
	}//'''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''
	//..............................................................................................................
	// This function pigs one of the AWS's servers which provides "whatIsMyIP" service. I.e. upon receiving the ping
	//  from the given client, the server returns the PUBLIC IP ADDRESS from which the ping originated.
	private static String whatsMyPublicIPAddress() {

		URL whatismyip ;
		String ip = "" ;

		try {
			whatismyip = new URL( "http://checkip.amazonaws.com" ) ;

			BufferedReader in = new BufferedReader(new InputStreamReader( whatismyip.openStream() ) ) ;

			ip = in.readLine(); //you get the Public IP Address as a String
			//System.out.println( ip ) ;

		} catch (MalformedURLException e) {			
			e.printStackTrace();

		} catch( IOException e2 ) {
			System.out.println( "IO Exception "+ e2.toString() ) ;	
		}
		return ip ;
	}//'''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''
	//..............................................................................................................
	// This function return the Local IP address of one of the networking interfaces. More can be found at:
	// https://stackoverflow.com/questions/9481865/getting-the-ip-address-of-the-current-machine-using-java
	private static String whatsMyLocalIPAddress() {

		String ip = "" ;

		InetAddress address_local;
		try {

			address_local = InetAddress.getLocalHost();

			ip = address_local.getHostAddress() ; 

			//System.out.println("IP of my system is := " + address_local.getHostAddress() ) ;

		} catch (UnknownHostException e) {

			e.printStackTrace();
		}
		return ip ;
	}//'''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''
	//..............................................................................................................
	// This function write the "users.txt" file by iterating via USERs HashMap
	// Write users.txt
	public static void write_users_file() { 

		String content = "" ;

		Path locpath = Paths.get("users.txt") ;
		String path = locpath.toString() ;
		File file = new File( locpath.toString() ) ;

		if( file.exists() ) {

			System.out.println( "\nUSERss content:" ) ;
			for ( Map.Entry< String,String > entry : USERs.entrySet() ) {	

				content = "" ;

				String key = entry.getKey() ;
				String val = entry.getValue() ;

				System.out.println( "KEY:"+key+" VAL:"+val+"\n" ) ;	

				content = key+" "+val ; 

				outputToFile( path, "" , content ) ;  
			}
		}
	}//'''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''
	//..............................................................................................................
	// This methods prints PEERs HashMap to the console
	private static void show_PEERs() {

		System.out.println( "\nPEERs content:" ) ;
		String key="",val="",content="" ;
		for ( Map.Entry< String,String > entry : PEERs.entrySet() ) {	

			key = entry.getKey() ;
			val = entry.getValue() ;
			
			content += key+" "+val+"\n" ;
		}
		System.out.println( content ) ;	
	}//'''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''
	//..............................................................................................................
	// This function prints to the console the list of ALL available PEERs and ALL their files. 
	public static String show_INDEX() {

		String content = "" ;

		System.out.println("\nINDEX SIZE = " + INDEX.size() ) ;

		// Iterating via HashTable using for-loop
		for ( Map.Entry< String, HashMap< Integer,String > > entry : INDEX.entrySet() ) {	

			String key = entry.getKey() ;
			HashMap< Integer,String > val = entry.getValue() ;

			content += "Client: "+key + ":\n" ;

			for( Map.Entry< Integer,String > member : val.entrySet() ) {

				Integer docID = member.getKey() ;
				String value = member.getValue() ;

				content += " "+docID+" "+value+"\n" ;	
			}	
		}
		System.out.println( content ) ;
		return content ;
	}//'''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''
	//..............................................................................................................
	// This function persists the INDEX into index.txt file--in case the Directory Server crashes 
	public static void persist_INDEX( String path) {

		String content = "", output = "" ;

		for ( Map.Entry< String,HashMap< Integer,String >> entry : INDEX.entrySet() ) {	// Iterating via Hashtable using for loop

			String key = entry.getKey() ;
			HashMap< Integer,String > val = entry.getValue() ;

			content += key ;

			for( Map.Entry< Integer,String > member : val.entrySet() ) {

				//Integer docID = member.getKey() ;
				String value = member.getValue() ;

				content += " "+value ;
			}
			output += content+"\n" ;
			content = "" ;
		}

		outputToFile( path, "", output ) ; // path= "/index.txt"
	}//'''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''
	//..............................................................................................................
	public static void outputToFile( String path, String fileName , String content ) {

		File theDir = new File( path ) ;
		if( !theDir.exists() ){
			theDir.mkdirs() ;
		}

		//BufferedWriter output ;
		try {
			String outputName = path + fileName ;					//(1) get the output file name e.g. "output.txt"
			FileWriter outputFile = new FileWriter( outputName,false ) ;	//(2) instantiate a FileWriter object and tell it which file to write to 
			//output = new BufferedWriter( outputFile ) ;				//(3) instantiate a BufferedWriter object and assign it the FileWriter object

			outputFile.write( content ) ;
			//output.write( content ) ;

			//output.close();
			outputFile.close();
			
		} catch ( FileNotFoundException e ){
			System.out.println( "file read error" ) ; e.printStackTrace() ;
		} catch ( IOException e ){
			System.out.println( "file write/read error" ) ; e.printStackTrace() ;
		}
	}//'''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''
	//..............................................................................................................
	public static String search_INDEX( String filename ) { 

		System.out.println( "Searching INDEX for filename: "+filename ) ;
		
		String owner = "", key="", file_name="" ; HashMap< Integer,String > val=null ;

		for ( Map.Entry< String,HashMap< Integer,String >> entry : INDEX.entrySet() ) {	// Iterating via Hashtable using for loop

			key = entry.getKey() ;
			val = entry.getValue() ;

			for( Map.Entry< Integer,String > member : val.entrySet() ) {

				file_name = member.getValue().trim() ;

				if( file_name.equals( filename.trim() ) ) {
					owner = key ;
				}
			}
		}
		return owner ;
	}//'''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''
	//..............................................................................................................
	public static String get_owner_addr_port( String owner ) { 

		String addr_port = "" ;

		for ( Map.Entry< String,String > entry : PEERs.entrySet() ) {	// Iterating via Hashtable using for loop

			String username = entry.getKey() ;
			String val = entry.getValue() ;

			if( owner.equals( username ) )
				addr_port = val ;
		}
		return addr_port ;
	}//'''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''
//	//..............................................................................................................  
//	public static boolean isNumeric( String str ) { 
//		try {  
//			Double.parseDouble(str);  
//			return true;	
//
//		} catch(NumberFormatException e) {  
//			return false;  
//		}  
//	}//'''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''
//	//..............................................................................................................  
//	// This function loads the index list of ALL available PEERs and ALL their files. 
//	// Below is the read-in reference
//	// E.g. 
//	// Martin_01 			fileID:1  	value:Lorem_Ipsum.txt
//	//						fileID:2  	value:exampl_pic.jpg
//	//
//	// Juan_01	fileID:1  	value:example_file.txt
//	//						fileID:2  	value:cloud-computing.jpg
//	//						fileID:3  	value:hacker_1.jpg
//	//						fileID:4  	value:hacker_2.jpg
//	// So text file looks like e.g.:
//	// Martin_01 1 Lorem_Ipsum.txt 2 example_pic.jpg
//	// Juan_01 1 example_file.txt 2 cloud.jpg 3 hacker_1.jpg 4 hacker_2.jpg  
//	//
//	// INDEX is HashMap< String, HashMap< Integer,String >>
//	public static void load_Index( File idx_file ) {
//
//		BufferedReader br = null ;
//		FileReader fr = null ;
//		Scanner iscan = null ;
//		try {
//			fr = new FileReader( idx_file ) ; 
//			br = new BufferedReader( fr  ) ;
//			iscan = new Scanner( br ) ;
//
//			String line = "" ;
//			while( iscan.hasNextLine() ) {
//
//				line = iscan.nextLine() ; 
//
//				if( !line.equals(""))
//					update_Index_line( line ) ;
//			} 
//			br.close() ;
//			fr.close() ;
//			iscan.close() ;
//		}
//		catch (Exception ex) { // Catch block to handle exceptions
//			System.out.println(ex.getMessage());
//		}
//	}//'''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''
//	//.............................................................................................................. 
//	// This is a helper function to the function above. BUT it can also double as a single line reader, i.e. when 
//	// the CLIENT sends its index_client.txt file as a stream of bytes into the local String variable 
//	// index_client.txt is just a one liner:
//	//													linearg = Martin_01 1 Lorem_Ipsum.txt 2 exampl_pic.jpg
//	public static void update_Index_line( String linearg ) {
//
//		String line=linearg, username="", file="" ; int key = 0  ;
//		boolean isIP = true ;
//
//		HashMap< Integer,String > temp = new HashMap< Integer,String >() ;
//
//		for( String val: line.split(" ") ) {
//
//			if( isIP ){ 
//
//				username = val ;
//				isIP = false ; //System.out.println( "Got IPAddress+Port : "+ip ) ;
//			} else {
//
//				if( isNumeric( val ) )
//
//					key = Integer.parseInt( val ) ;
//
//				else {
//					file = val ;
//					temp.put( key,file ) ; //System.out.println( "Got fileID:"+key+" file:"+file ) ;
//					key=0 ; file="" ;
//				}
//			}
//		}
//		INDEX.put( username,temp ) ;
//	}//'''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''
//	//..............................................................................................................
//	// This function reads the already existing "peers.txt" file into PEERs HashMap.
//	// E.g.
//	// Martin_01 192.168.1.186 6004
//	// Juan_01 192.168.1.186 6007
//	// Tristan_01 192.168.1.186 6010
//	private static void retrieve_PEERS_file() {
//
//		String ip_address = "" ;
//
//		Path locpath = Paths.get("peers.txt") ;
//		File file = new File( locpath.toString() ) ;
//		if( file.exists() ) {
//
//			String content = "", user="", line="" ;
//
//			BufferedReader br = null ;
//			FileReader fr = null ;
//			Scanner iscan = null ;
//			try {
//				fr = new FileReader( file ) ; 
//				br = new BufferedReader( fr  ) ;
//				iscan = new Scanner( br ) ;
//
//				while( iscan.hasNextLine() ) {
//
//					line = iscan.nextLine() ; boolean isUserName = true ;
//
//					for( String val: line.split(" ") ) {
//
//						if( isUserName ){ 
//
//							user = val ; isUserName = false ;
//
//						} else {
//							content += val +" " ;
//						}
//					}
//					PEERs.put( user,content.trim() );   	// PEERs ← "Martin_01" "192.168.1.186 6004"
//				}
//				br.close() ;
//				fr.close() ;
//				iscan.close() ;
//			}
//			catch (Exception ex) { // Catch block to handle exceptions
//				System.out.println(ex.getMessage());
//			}
//		}
//	}//'''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''
//	//..............................................................................................................
//	// This function updates the PEERs HashMap where KEY=username, VALUE=IPAddress+Port
//	// The 1st line of communication from a PEER contains 2 words: Username and ipAddress_Port# 
//	// e.g. Martin_01 192.168.1.186_6032
//	private static String update_PEERs( String username, String addr_port ) {   // client_message = Martin_01 192.168.1.186_6032
//
//		PEERs.put( username,addr_port ) ;
//
//		String content="",key="",val="" ; 	//System.out.println( "\nPEERs content:" ) ;
//		for ( Map.Entry< String,String > entry : PEERs.entrySet() ) {	
//
//			key = entry.getKey() ;
//			val = entry.getValue() ;   		//System.out.println( "KEY:"+key+" VAL:"+val+"\n" ) ;	
//
//			content += key+" "+val+"\n" ; 
//		}
//		
//		Path locpath = Paths.get("peers.txt") ;
//		String path = locpath.toString() ; 
//		outputToFile( path, "" , content ) ; 
//		
//		return username ; 
//	}//'''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''
//	//..............................................................................................................
//	// This function populates USERs HashMap from previously created "users.txt" file.
//	// 
//	// Ports: 
//	//		Directory Server Port#: 6000
//	//
//	//		Username	: client-side port#		: server-side port#
//	//		Martin_01 	: 6003   				: 6004
//	//		Juan_01 	: 6006   				: 6007
//	//		Tristan_01 	: 6009 					: 6010
//	//
//	// So the users.txt looks like:
//	//		Martin_01 6003 6004
//	//		Juan_01 6006 6007
//	//		Tristan_01 6009 6010
//	private static void retrieve_USERS_file() {
//
//		String server_port = "" ;
//
//		Path locpath = Paths.get("users.txt") ;
//		File file_users = new File( locpath.toString() ) ;
//		if( file_users.exists() ) {
//
//			String content = "", user="", line="" ;
//
//			BufferedReader br = null ;
//			FileReader fr = null ;
//			Scanner iscan = null ;
//			try {
//				fr = new FileReader( file_users ) ; 
//				br = new BufferedReader( fr  ) ;
//				iscan = new Scanner( br ) ;
//
//				while( iscan.hasNextLine() ) {
//
//					line = iscan.nextLine() ; boolean isUserName = true ;
//
//					for( String val: line.split(" ") ) {
//
//						if( isUserName ){ 
//
//							user = val ; isUserName = false ;
//
//						} else {
//							content += val +" " ;
//						}
//					}
//					USERs.put( user,content.trim() );   				// USERs ← "Martin_01"  "6003 6004"
//				}
//				br.close() ;
//				fr.close() ;
//				iscan.close() ;
//			}
//			catch (Exception ex) { // Catch block to handle exceptions
//				System.out.println(ex.getMessage());
//			}
//		}
//	}//'''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''

}//|||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||


















////..............................................................................................................
//// This function prints to the console the list of ALL available PEERs and ALL their files. 
//// Below is the read-in reference
//// E.g. 
//// 192.168.1.187+5001 	fileID:1  	value:Lorem_Ipsum.txt
////						fileID:2  	value:exampl_pic.jpg
////
//// 192.168.1.186+6032	fileID:1  	value:example_file.txt
////						fileID:2  	value:cloud-computing.jpg
////						fileID:3  	value:hacker_1.jpg
////						fileID:4  	value:hacker_2.jpg
////
//// So the index.txt file looks like:
////	
//// 192.168.1.187+5001 1 Lorem_Ipsum.txt 2 example_pic.jpg
//// 192.168.1.186+6032 1 example_file.txt 2 cloud.jpg 3 hacker_1.jpg 4 hacker_2.jpg  
////
//// INDEX is HashMap< String, HashMap< Integer,String >>
//public static void load_Index( File idx_file ) {
//	
//	BufferedReader br = null ;
//	FileReader fr = null ;
//	Scanner iscan = null ;
//	try {
//		fr = new FileReader( idx_file ) ; 
//		br = new BufferedReader( fr  ) ;
//		iscan = new Scanner( br ) ;
//
//		String line="", ip="", file="" ; int key = 0  ;
//		boolean isIP = true ;
//		
//
//		while( iscan.hasNextLine() ) {
//
//			line = iscan.nextLine() ;
//			
//			HashMap< Integer,String > temp = new HashMap< Integer,String >() ;
//
//			for( String val: line.split(" ") ) {
//				
//				if( isIP ){ 
//					
//					ip = val ;
//					isIP = false ;
//					
//					//System.out.println( "Got IPAddress+Port : "+ip ) ;
//					
//				} else {
//					
//					if( isNumeric( val ) )
//						
//						key = Integer.parseInt( val ) ;
//					
//					else {
//						file = val ;
//						temp.put( key,file ) ;
//						
//						//System.out.println( "Got fileID:"+key+" file:"+file ) ;
//						key=0 ; file="" ;
//					}
//				}
//			}
//			INDEX.put( ip,temp ) ;
//			
//			isIP = true ; ip = "" ; //temp.clear() ;	
//		}
//		br.close() ;
//		fr.close() ;
//		iscan.close() ;
//	}
//	catch (Exception ex) { // Catch block to handle exceptions
//		System.out.println(ex.getMessage());
//	}
//}//'''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''



//String owner_addr_porty = "192.168.1.186_6032" ; String ip="" ; String porty="" ; 
//boolean isAddr = true ;
//for( String word : owner_addr_porty.split("_") ) {
//	
//	System.out.println( "HERE HERE HERE HERE"+ word ) ;
//	
//	if( isAddr ) { ip = word ; isAddr = false ; }
//	porty = word ;	
//}
//
//System.out.println( "IP:"+ip+" port:"+porty+"\n" ) ;















