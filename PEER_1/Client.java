
import java.io.*;
import java.net.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Scanner;


//||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||
public class Client {


	public static String username = "" ;

	public static DataInputStream input = null ;
	public static DataOutputStream output = null ;

	public static HashMap< String,String > USERs =  new HashMap< String,String >() ;


	//..............................................................................................................
	public static void main(String [] args) {
		
		Helper helper = new Helper() ;
		
		boolean isDONE = false ;	// means that NOT ONE of the PEERs has the file you asked for
		String ownerName="", ownerIP = "", ownerPort = ""; 			
		String dir_serv_ipaddress = "" ;		// The Server IP Address
		String fileName = helper.read_file( "request.txt" ).trim() ;	// The to be requested (from the Server) File Name
		String port = "" ;
		String name_user = "" ;
		String address_local = "";
		String server_side_port = "" ;
		Path locpath = null ;
		Socket dir_server_socket = null ;
		
		
		Path path_idx_client = Paths.get("index_client.txt") ;
    	String path_idxcl = path_idx_client.toString() ;
    	File file_idx_client = new File( path_idxcl ) ;
		

		try {

			System.out.println( "\nP2P Client-Side is ON" ) ;

			// The BufferedReader will read the data from the console.
			InputStreamReader in = new InputStreamReader( System.in ) ;     
			BufferedReader reader  = new BufferedReader( in ) ;

			System.out.print( "please enter your username: " ) ;
			name_user = reader.readLine() ;
			username = name_user ;

			address_local = whatsMyLocalIPAddress() ;
			System.out.println( "Server-Side IP Address: " + address_local ) ;			

			
// STEP 0.1	Load the USER data from the "users.txt" file

			
			locpath = Paths.get("users.txt") ;
			File file_users = new File( locpath.toString() ) ;
			if( file_users.exists() ) {
				read_users_file( file_users ) ; // populates USERs HashMap from the given users.txt file 
			}
			
			server_side_port = USERs.get( name_user ) ;
			String lastWord = server_side_port.substring( server_side_port.lastIndexOf(" ")+1 ) ;
			server_side_port = lastWord ;
						
			System.out.println( "Server-Side Port Numb : " + server_side_port ) ;

			
// STEP 0.2	Load the Directory Server IPAddress and Port number

			
			boolean isValid = false ;
			while( !isValid ) {
				System.out.print( "please enter directory server ip address : " ) ;
				dir_serv_ipaddress = reader.readLine() ; // Read-In the provided Directory Server's IP Address

				isValid = validateIPv4Address( dir_serv_ipaddress ) ;
			}
			System.out.print( "please enter directory server port number: " ) ;
			port = reader.readLine() ;			// Read-In the provided Directory Server's IP Port Number 

			in.close();
			reader.close();

			
// STEP 0.3	Prepare the DATA-Package for the Directory Server			

			
			String for_dir_server = username+"\n"+address_local+"\n"+server_side_port+"\n" ; 
			for_dir_server += helper.read_file( "index_client.txt" ) ;
			for_dir_server += helper.read_file( "request.txt" ) ;
			
			System.out.println("\nHere is the DATA-Package for Directory Server:");
			System.out.println( for_dir_server ) ;
			// You should see something akin to:
			//     Martin_01
			//     192.168.1.187
			//     6004
			//     example.txt hacker1.jpg hacker2.jpg 0 
			//     examplePic.jpg			

			
// STEP 1	Establish the connection with the DIRECTORY SERVER
			
			
			dir_server_socket = new Socket( dir_serv_ipaddress, Integer.parseInt(port) ) ;
			
			output = new DataOutputStream( dir_server_socket.getOutputStream() );

			
// STEP 2 	Send the above created DATA-Package to the Directory Server

			
			output.writeUTF( for_dir_server ) ;	// owner_addr_port = 192.168.1.187_6004
			output.flush(); // send the message and wait for the response

			
// STEP 3	Wait for the response from the Directory Server

			
			// The DataInputStream will read the reply from the above created server socket connection.
			// The reply is the FileOwner’s IPAddress and Port number e.g: 192.168.1.186_6032
			input = new DataInputStream( dir_server_socket.getInputStream() ) ;

			
// STEP 4	Unpack the received (from Directory Server) information about the desired file's owner

			
			String data_from_DirServer = input.readUTF() ;	// this line contains e.g: "Juan_01 192.168.1.187 6004" OR "DONE"
			
			data_from_DirServer = data_from_DirServer.trim() ;
			
			if( !data_from_DirServer.equals("DONE") ) { // OK we may have some valid information
			
				if( !data_from_DirServer.equals("") || data_from_DirServer!=null ){

					String[] data_from_DirServer_ARR = data_from_DirServer.split(" ",3) ; // expecting e.g. Juan_01 192.168.1.186 6007 

					ownerName = data_from_DirServer_ARR[ 0 ] ; 
					ownerIP   = data_from_DirServer_ARR[ 1 ] ; 
					ownerPort = data_from_DirServer_ARR[ 2 ] ;

					System.out.println( "Directory Server replied with the following DATA-Package: " ) ;

					System.out.println( "The requested file OWNER is: "+ownerName ) ;
					System.out.println( "IP Address : "+ownerIP ) ;
					System.out.println( "Port Number: "+ownerPort+"\n" ) ;
				}	
			} else {
				
				isDONE = true ;
				
				System.out.println( "Directory Server could NOT locate file: "+fileName ) ;
			}

			
			input.close() ;	// This will close the Socket as well, we NO longer need it, a new one will be opened below
			output.close();	// Also TCP allows for only one connection
			
		} catch( Exception e ) {
			System.out.println( e.toString() ) ;
		}
				
// STEP 5 	Contact the PEER Server-Side (i.e. The OWNER of the desired file)

		
		if( !isDONE ) {
			
			System.out.println( "Contacting "+ownerName+" to retrieve the file.\n" ) ;

			try (Socket peer_server_socket = new Socket( ownerIP, Integer.parseInt(ownerPort) )) {

				output = new DataOutputStream( peer_server_socket.getOutputStream() );
				
				output.writeUTF( fileName ) ;	// e.g. "examplePic.jpg" 
				output.flush(); 				// send the message to the PEER and wait for the response
				
				InputStream inputByte = peer_server_socket.getInputStream() ; // get the input byte stream from the Peer-Server 
				BufferedInputStream input = new BufferedInputStream( inputByte ) ;


				// If the requested Filename is found/exists, the Server responds with CODE=1, otherwise with CODE=0
				int code = input.read() ;	// get the CODE from the Server. 

				
// STEP 6	Download the file from the PEER-Server (owner of the requested file)

				
				if (code == 1) {			// great the requested file exists

					// The BufferedOutputStream object will write the received file into the local directory. 
					// BufferedOutputStream outputFile = new BufferedOutputStream(new FileOutputStream("D:\\download\\" + fileName)) ;
					String dest_path = "Downloads/" ;
					BufferedOutputStream outputFile = new BufferedOutputStream( new FileOutputStream( dest_path + fileName ) ) ;

					byte[] buffer = new byte[ 1024 ] ;	// The Server sends the File in chunks of 1KB, here we also read them 
					// in chunks of 1KB
					int bytesRead = 0;

					while ((bytesRead = input.read(buffer)) != -1) {

						System.out.print( "." ); 			// Denote to the console that the File is being downloaded 

						outputFile.write( buffer,0,bytesRead ) ;

						outputFile.flush() ;
					}

					System.out.println() ;
					System.out.println( "File: "+fileName+" was downloaded successfully!" ) ;

					outputFile.close();

				} else {
					System.out.println( "File: "+fileName+" is NOT present on the P2P-Server!" ) ;
				}

				//peer_server_socket.close();
				//input.close();
				//output_2.close() ;


				//out.close();
				//input.close() ;
				//output.close();
			} catch( Exception e ) {
				System.out.println( e.toString() ) ;
			}


// STEP 7	Update the Directory Server's INDEX about having new file		

			update_local_index_client_file( fileName ) ;

			// Prepare a message DATA-Package for the Directory Server
			String update_content = "" ;
			update_content += username +"\n"+address_local+"\n"+server_side_port+"\n" ;
			
			if( file_idx_client.exists() ) { 
				update_content += read_index_client( file_idx_client ) + "\n" ; 
			}
			update_content += "DONE" ;
			// The content should look like the following
			//          Martin_01
			//          192.168.1.186
			//          6004
			//          example.txt hacker1.jpg hacker2.jpg examplePic.jpg 
			//          DONE
			
			System.out.println( "Contacting Directory Server to update the global INDEX with:\n" ) ;
			System.out.println( update_content ) ;
			
			try {
				dir_server_socket = new Socket( dir_serv_ipaddress , Integer.parseInt(port) ) ;
				
				output = new DataOutputStream( dir_server_socket.getOutputStream() );

				output.writeUTF( update_content ) ;	// owner_addr_port = 192.168.1.187_6004
				output.flush(); // send the message and wait for the response
				
				dir_server_socket.close() ;
				output.close() ;

			} catch( Exception e ) {
				System.out.println( e.toString() ) ;
			}
		}
	}//'''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''
	//..............................................................................................................
	// The IPv4 address is a 32-bit number that uniquely identifies a network interface on a machine. An IPv4 address is
	// typically written in decimal digits, formatted as four 8-bit fields that are separated by periods. Each 8-bit
	// field represents a byte of the IPv4 address. 
	// E.g. valid IP addresses are:
	//								192.168.0.12
	//								10.10.10.10
	//								86.123.6.230
	//An invalid IP addresses are:
	//								192.168.0.321
	//								10.10.10.10.10
	//								232.0.0.ac
	public static boolean validateIPv4Address( String ipAddress ) {

		String[] numbers = ipAddress.split( "\\." ) ;

		if( numbers.length != 4 ) return false ;	// IPv4 address must have 4 8-bit fields


		for( String str: numbers ) {

			int i = Integer.parseInt( str ) ;

			if( (i<0) || (i>255) ) return false ;	// An 8-bit (or 1B) has a range of 0-255

		}
		return true ;
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
	//..............................................................................................................  ********************
	// This function reads the index_client.txt file into a string. Then returns that string
	// Below is an example of a possible content:
	//           example.txt hacker1.jpg hacker2.jpg
	public static String read_index_client( File idx_file ) {

		String content = "";

		BufferedReader br = null ;
		FileReader fr = null ;
		Scanner iscan = null ;
		try {
			fr = new FileReader( idx_file ) ; 
			br = new BufferedReader( fr  ) ;
			iscan = new Scanner( br ) ;

			while( iscan.hasNextLine() ) {		// there should only be one line
				content += iscan.nextLine() ;
			}
			br.close() ;
			fr.close() ;
			iscan.close() ;
		}
		catch (Exception ex) { // Catch block to handle exceptions
			System.out.println(ex.getMessage());
		}
		return content.trim() ;
	}//'''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''
	//..............................................................................................................
	// This function updates the "index_client.txt" file with the new file name
	// E.g.:
	//      before:   example.txt hacker1.jpg hacker2.jpg
	//      after :   example.txt hacker1.jpg hacker2.jpg examplePic.jpg
	public static void update_local_index_client_file( String filename ) { 
		
		String content = "" ;
		
		Path path = Paths.get("index_client.txt") ;
		File file = new File( path.toString() ) ;
		
		
		if( file.exists() ) {
			content = read_index_client( file ) ; 
		}
		
		// check if the index_client already contains the given filename
		String[] line_ARR = content.split(" ") ; 

		String check="" ;
		boolean doNothing = false ;
		for( String token: line_ARR ) {
			
			check = token.trim(); 
			
			if( check.equals( filename.trim())){ 
				doNothing = true ; 
				break ;
			}
		}
		if( !doNothing ) {

			content += " "+filename ;

			outputToFile( path.toString(), "" , content ) ; // path = "index_client.txt"
		}
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
	// This function reads the username.txt file
	// Below is the read-in reference
	// Martin_01
	public static String read_single_word_file( File file ) {

		String content = "" ;

		BufferedReader br = null ;
		FileReader fr = null ;
		Scanner iscan = null ;
		try {
			fr = new FileReader( file ) ; 
			br = new BufferedReader( fr  ) ;
			iscan = new Scanner( br ) ;

			while( iscan.hasNextLine() ) {

				content = iscan.nextLine() ;	
			}
			br.close() ;
			fr.close() ;
			iscan.close() ;
		}
		catch (Exception ex) { // Catch block to handle exceptions
			System.out.println(ex.getMessage());
		}
		return content ;
	}//'''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''
	//..............................................................................................................
	public static boolean isNumeric( String str ) { 
		try {  
			Double.parseDouble(str);  
			return true;	

		} catch(NumberFormatException e) {  
			return false;  
		}  
	}//'''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''
	//..............................................................................................................
	// This function reads the users.txt file
	// E.g.
	//	Martin_01 6003 6004
	//	Juan_01 6006 6007
	// meaning
	// Martin_01 	: client-side port: 6003   server-side port: 6004
	// Juan_01 		: client-side port: 6006   server-side port: 6007

	public static void read_users_file( File file ) {

		String content = "", user="", line="" ;

		BufferedReader br = null ;
		FileReader fr = null ;
		Scanner iscan = null ;
		try {
			fr = new FileReader( file ) ; 
			br = new BufferedReader( fr  ) ;
			iscan = new Scanner( br ) ;


			while( iscan.hasNextLine() ) {

				line = iscan.nextLine() ; boolean isUserName = true ;

				for( String val: line.split(" ") ) {

					if( isUserName ){ 

						user = val ; isUserName = false ;

					} else {
						content += val +" " ;
					}
				}
				USERs.put( user,content.trim() );
			}
			br.close() ;
			fr.close() ;
			iscan.close() ;
		}
		catch (Exception ex) { // Catch block to handle exceptions
			System.out.println(ex.getMessage());
		}
	}//'''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''
}//|||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||


















