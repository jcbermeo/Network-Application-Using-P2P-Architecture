

import java.io.* ;
import java.net.* ;


//||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||
public class ClientThreadDir extends Thread {
	
	private Socket socket_client = null ;
    
    private PrintWriter out = null ;
    
    private BufferedOutputStream output = null ;
    private BufferedInputStream fileReader = null ;
    
    private String filename = null ;
    private String client_username = null ;
    private String file_owner = null ;
    private String owner_ipAddress = null ;	// desired file owner's IP Address
    private String owner_port = null ;		// desired file owner's port number
	
	
	//..............................................................................................................
	public ClientThreadDir( Socket socket, String filename , String client_username, String file_owner, String owner_addr_port ) {
		
		this.socket_client = socket ;
		this.filename = filename ;
		this.client_username = client_username ;
		this.file_owner = file_owner ;
		
		boolean isAddr = true ;
		for( String word : owner_addr_port.split("_") ) {
			
			if( isAddr ) { this.owner_ipAddress = word ; isAddr = false ; }
			this.owner_port = word ;	
		}
	}//'''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''
	//..............................................................................................................
	public void run() {
		
// STEP 9:	Reply to the client with the file owner’s information
		
		System.out.println( "\nGreat news "+client_username+"! I found the filename:\""+filename+"\"") ;
		System.out.println( "The file owner is:"+file_owner+" IPAddress:"+owner_ipAddress+" Port:"+owner_port) ;
		String toSend = owner_ipAddress+" "+owner_port ;
		

	    OutputStreamWriter osw;
	    try {
	    
	        osw =new OutputStreamWriter( socket_client.getOutputStream(), "UTF-8" ) ;
	        
	        osw.write( toSend, 0, toSend.length() ) ;
	        
	    } catch (IOException e) {
	    	
	        System.err.print(e);
	        
	    } finally {
	    	
	    	try {
				socket_client.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
	    }		
	}//'''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''
	//..............................................................................................................
	public void closeConnection() {

		try {
			if( output!=null ) output.close() ;
			//if( input!=null) input.close() ;
			if( fileReader!=null ) fileReader.close() ;
			
			if( out!=null ) out.close() ;
			if( output!=null ) socket_client.close() ;
			
		} catch(Exception e) {
			System.out.println( e.toString() ) ;
		}
	}//'''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''
}//|||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||


















