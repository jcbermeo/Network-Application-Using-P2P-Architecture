

import java.io.* ;
import java.net.* ;
import java.nio.file.Path;
import java.nio.file.Paths;


//||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||
public class ClientThread extends Thread {
	
	private Socket clientSocket = null ;
	
    //private BufferedReader input = null ;
    private DataInputStream input = null ;
    
    
    private BufferedOutputStream output = null ;
    private BufferedInputStream fileReader = null ;
	
	
	//..............................................................................................................
	public ClientThread( Socket socket ) {
		
		this.clientSocket = socket ;
		
	}//'''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''
	//..............................................................................................................
	public void run() {
		
		try {
			
			//Use the DataInputStream to get the Name of the requested File from the Client 
            input = new DataInputStream( clientSocket.getInputStream() ) ;
            String fileName = input.readUTF() ;	// this line contains e.g. "examplePic.jpg"
            fileName = fileName.trim();
            
			// Use the BufferedOutputStream to send the requested File back to the Client. Using BufferedOutputStream
			// is very efficient when transferring files over a network (to other remote hosts). 
			output = new BufferedOutputStream( clientSocket.getOutputStream() ) ;
            
            System.out.println( "\nFile name: " + fileName + " has been requested by " + clientSocket.getInetAddress().getHostAddress() ) ;
            
            
            // To keep things simple, the file is stored in the Server root-directory (in our case the root directory is
            // the same as the project directory). If you want to use another directory, you can store the directory name in
            // a string variable, and then concatenate the root directory name with the filename to create the file relative path.
        	Path path_data = Paths.get("Data/"+fileName) ;
        	String path = path_data.toString() ;
        	File file_data = new File( path ) ;
            
            
            if( !file_data.exists() ) {  	//Verify that the file does in fact exists in the Server root directory
            	
            	System.out.println( "Sorry File NOT found!" ) ;
               
                byte code = (byte)0 ;	// Code 0, will inform the Client that the requested File is NOT available
                output.write( code ) ; 	// So if the file does NOT exist, send Code 0 and Close the Connection
                closeConnection();
                
                // NOTE: The above write() method except a byte as an argument and we cast an integer to a byte. You have
                // to be careful when you cast an integer to a byte. A bite can only represent the values -128 to 127, 
                // so you cannot use a number greater than 127 for your code number.
               
//STEP 3:	Upload the file
            } else {					//If the file does exist, send Code 1 and send the file to the Client
            	
            	System.out.println( "The file is found! Sending the file now." ) ;
            	
            	output.write( (byte)1 ) ;
            	
            	// To read the actual content of the requested File we need another BufferedReader.
                fileReader = new BufferedInputStream( new FileInputStream( file_data ) ) ;
                
                // Next we create a byte array with size equal to 1KB. This means that we'll read the file in chunks,
                // where the size of each chunk is 1KB (another good buffer size is 8KB).
                byte[] buffer = new byte[ 1024 ] ;	// Set the buffer size to 1KB i.e. 1,000 Bytes
                

                // Next we use the while loop to keep on reading the file (in max of 1KB chunks) until the file is finished.
                // E.g. If the file length is greater than 1KB, say 1524 bytes, the file will be read in two cycles. In the
                // first cycle the read() method reads the first 1024 bytes, and write() method writes those bytes to the
                // socket connection. In the second cycle, the read() method returns a value of 500, because it reads the
                // remaining bytes from the specified file.
                int bytesRead = 0;
                while( (bytesRead = fileReader.read(buffer)) != -1 ) { // when the number of to be sent bytes is -1, we are done
                	
                	// The call to write() sends <= 1KB of Data over the TCP connection (from the byte array) starting
                	// at offset 0 ; bytesRead keeps track of how many bytes has been sent over (in each cycle)
                	output.write( buffer,0,bytesRead ) ;
                	
                    System.out.println( "Server Bytes Sent: "+bytesRead ) ;
                    
                    // After each cycle we call the flush() method to send the content of the byte array to the socket.
                    output.flush() ;
                }
                // When the file transfer process is done, we close the client connection.
            	closeConnection() ;
            }
            
		} catch ( Exception e ) {
			System.out.println( e.toString() ) ;
		}
	}//'''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''
	//..............................................................................................................
	public void closeConnection() {

		try {
			if( output!=null ) output.close() ;
			if( input!=null) input.close() ;
			if( fileReader!=null ) fileReader.close() ;
			if( output!=null ) clientSocket.close() ;
			
		} catch(Exception e) {
			System.out.println( e.toString() ) ;
		}
	}//'''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''

}//|||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||

















