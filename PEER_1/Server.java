
import java.io.* ;
import java.net.* ;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Scanner;


//||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||
public class Server { // A multi-threaded Server that will deal with multiple Clients concurrently.

	
	public static HashMap< String,String > USERs =  new HashMap< String,String >() ;
	// the above HashMap store the listening posts according to their corresponding usernames
	// KEY=username VALUE= client-side-port# server-side-port#
	
	//..............................................................................................................
	public static void main(String[] args) {
		
		String user_name = "" ;
		String server_side_port = "" ;
		
		try {
		
			// We are running on a subnet so only Local IP should suffice
			// String address_public = whatsMyPublicIPAddress() ;
			// System.out.println( "Server Public IP Address is: " + address_public ) ;   // Server's Public IP Address

			System.out.println( "\nP2P Server-Side is ON" ) ;

			// The BufferedReader will read the data from the console.
			InputStreamReader in = new InputStreamReader( System.in ) ;     
			BufferedReader reader  = new BufferedReader( in ) ;

//STEP 0.0:	get user-name from Command-Line console
			System.out.print( "please enter your username: " ) ;
			user_name = reader.readLine() ;

//STEP 0.1: get server IP ADDRESS
			String address_local = whatsMyLocalIPAddress() ;
			System.out.println( "IP Address: " + address_local ) ;   // Server's Local IP Address

//STEP 0.2: read users.txt and extract port#			
			server_side_port = read_users_file( user_name ) ; // populate the USERs HashMap from the given users.txt
			System.out.println( "Port Numb : " + server_side_port ) ;
			int port = Integer.parseInt( server_side_port ) ;
			

			try {

//STEP 1: 	Setup the listening socket and Wait for files requests
				ServerSocket serverSocket = new ServerSocket( port ) ;	//Initialize the Server Socket object with port#



				boolean isStopped = false ;	// the Server stays looping waiting for the clients
				while( !isStopped ) {
					// while-loop is needed so that the Server can accept multiple Clients where each Client gets its
					// separate thread--i.e. the Server can respond to (service) multiple clients concurrently.
					
					System.out.println( "P2P-Server is ON and waiting for clients...  (to exit type: CTRL+C)" ) ;

					//Initialize Client Socket object via the serverSocket
					Socket clientSocket = serverSocket.accept() ;
					// After the ServerSocket object is created we call the accept() method. When ServerSocket accept()
					// method is called the Server waits for any Client requests (coming from remote machines). Once 
					// accept() returns a socket the connection is established and the Server uses that socket to
					// communicate with the client.
					// A clientSocket represents the the connection with a Client. Each time a new Client connects to
					// the Server the accept() method creates a new Socket object for that Client. 

					System.out.println( "Client "+clientSocket.getInetAddress().getHostAddress()+" is connected." ) ;

//STEP 2: 	Process the request for file
					// Initialize a new Thread that will deal with the newly incoming Client.
					ClientThread clientThread = new ClientThread( clientSocket ) ;
					clientThread.start() ;

				}	

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
		
	} catch( Exception e ) {
		System.out.println( e.toString() ) ;
	}
		

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
	// This function populates USERs HashMap from the given users.txt file. By this it reads the returns the 
	// listening server-side port number according to the given user-name.
	// 
	// Ports: 
	//		Directory Server Port#: 6000
	//
	//		Username	: client-side port#		: server-side port#
	//		Martin_01 	: 6003   				: 6004
	//		Juan_01 	: 6006   				: 6007
	//		Tristan_01 	: 6009 					: 6010
	//
	// So the users.txt looks like:
	//		Martin_01 6003 6004
	//		Juan_01 6006 6007
	//		Tristan_01 6009 6010
	private static String read_users_file( String username ) {
		
		String server_port = "" ;
		
		Path locpath = Paths.get("users.txt") ;
		File file_users = new File( locpath.toString() ) ;
		if( file_users.exists() ) {

			String content = "", user="", line="" ;

			BufferedReader br = null ;
			FileReader fr = null ;
			Scanner iscan = null ;
			try {
				fr = new FileReader( file_users ) ; 
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
					USERs.put( user,content.trim() );   				// USERs ← "Martin_01"  "6003 6004"
				}
				br.close() ;
				fr.close() ;
				iscan.close() ;
			}
			catch (Exception ex) { // Catch block to handle exceptions
				System.out.println(ex.getMessage());
			}
			
			String temp = USERs.get( username ) ;							// temp ← "6003 6004"

			server_port = temp.substring( temp.lastIndexOf(" ")+1 ) ; 	// server_port ← "6004"	 
		}
		return server_port ;
	}//'''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''
//	//..............................................................................................................
//	// This function pigs one of the AWS's servers which provides "whatIsMyIP" service. I.e. upon receiving the ping
//	//  from the given client, the server returns the PUBLIC IP ADDRESS from which the ping originated.
//	private static String whatsMyPublicIPAddress() {
//		
//		URL whatismyip ;
//		String ip = "" ;
//		
//		try {
//			whatismyip = new URL( "http://checkip.amazonaws.com" ) ;
//			
//			BufferedReader in = new BufferedReader(new InputStreamReader( whatismyip.openStream() ) ) ;
//
//			ip = in.readLine(); //you get the Public IP Address as a String
//			//System.out.println( ip ) ;
//				
//		} catch (MalformedURLException e) {			
//			e.printStackTrace();
//			
//		} catch( IOException e2 ) {
//			System.out.println( "IO Exception "+ e2.toString() ) ;	
//		}
//		return ip ;
//	}//'''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''

}//|||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||



















