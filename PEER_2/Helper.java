import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

//||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||
// This class contains useful common functions that can be used by the other classes
public class Helper {
	
	
	//..............................................................................................................
	public Helper( ) {

	}//'''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''
	
	//..............................................................................................................
	// This function loads the index list of ALL available PEERs and ALL their files. 
	// Below is the read-in reference
	// E.g. 
	// Martin_01 			fileID:1  	value:Lorem_Ipsum.txt
	//						fileID:2  	value:exampl_pic.jpg
	//
	// Juan_01	fileID:1  	value:example_file.txt
	//						fileID:2  	value:cloud-computing.jpg
	//						fileID:3  	value:hacker_1.jpg
	//						fileID:4  	value:hacker_2.jpg
	// So text file looks like e.g.:
	// Martin_01 1 Lorem_Ipsum.txt 2 example_pic.jpg
	// Juan_01 1 example_file.txt 2 cloud.jpg 3 hacker_1.jpg 4 hacker_2.jpg  
	//
	// INDEX is HashMap< String, HashMap< Integer,String >>
	public void load_Index( File idx_file, HashMap< String, HashMap< Integer,String >> INDEX ) {

		BufferedReader br = null ;
		FileReader fr = null ;
		Scanner iscan = null ;
		
		try {
			fr = new FileReader( idx_file ) ; 
			br = new BufferedReader( fr  ) ;
			iscan = new Scanner( br ) ;

			String line = "" ;
			while( iscan.hasNextLine() ) {

				line = iscan.nextLine() ; 

				if( !line.equals(""))
					update_Index_line( line,INDEX ) ;
			} 
			br.close() ;
			fr.close() ;
			iscan.close() ;
		}
		catch (Exception ex) { // Catch block to handle exceptions
			System.out.println(ex.getMessage());
		}
	}//'''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''
	//..............................................................................................................
	// This is a helper function to the function above. BUT it can also double as a single line reader, i.e. when 
	// the CLIENT sends its index_client.txt file as a stream of bytes into the local String variable 
	// index_client.txt is just a one liner:
	//											 linearg ← Martin_01 example.txt hacker1.jpg hacker2.jpg
	public void update_Index_line( String linearg, HashMap< String, HashMap< Integer,String >> INDEX ) {

		HashMap< Integer,String > temp = new HashMap< Integer,String >() ;
		
		String[] line_ARR = linearg.split(" ") ;
		
		String username = line_ARR[ 0 ] ;
		
		int idx = 0 ;
		for( String file_name: line_ARR ) {
			
			if( idx==0 ){ idx++ ; continue ; }
			
			temp.put( idx,file_name ) ;
			idx++ ;
		}
		INDEX.put( username,temp ) ;
	}//'''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''
	//..............................................................................................................
	public boolean isNumeric( String str ) { 
		try {  
			Double.parseDouble(str);  
			return true;	

		} catch(NumberFormatException e) {  
			return false;  
		}  
	}//'''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''
	//..............................................................................................................
	// This function reads the files located in the main directory into a String. 
	// E.g. "index_client.txt" file:
	//           example.txt hacker1.jpg hacker2.jpg → output
	public String read_file( String file_name ) { 
		
		String output = "" ;

		//Path path = Paths.get("index_client.txt") ;
		Path path = Paths.get( file_name ) ;
		File file = new File( path.toString() ) ;	

		if( file.exists() ) {

			BufferedReader br = null ;
			FileReader fr = null ;
			Scanner iscan = null ;

			try {
				fr = new FileReader( file ) ; 
				br = new BufferedReader( fr  ) ;
				iscan = new Scanner( br ) ;

				while( iscan.hasNextLine() ) {

					output += iscan.nextLine() + "\n" ;
				}

				br.close() ;
				fr.close() ;
				iscan.close() ;
			}
			catch (Exception ex) { // Catch block to handle exceptions
				System.out.println(ex.getMessage());
			}
		}
		return output ;
	}//'''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''
	//..............................................................................................................
	// Example of peers.txt
	// E.g.:
	//     Martin_01 192.168.1.186 6004
	//     Juan_01 192.168.1.186 6007
	//     Tristan_01 192.168.1.186 6010
	public void load_Peers( File file , HashMap< String,String > PEERs ) {

		BufferedReader br = null ;
		FileReader fr = null ;
		Scanner iscan = null ;
		
		try {
			fr = new FileReader( file ) ; 
			br = new BufferedReader( fr  ) ;
			iscan = new Scanner( br ) ;

			String line = "" ;
			while( iscan.hasNextLine() ) {

				line = iscan.nextLine() ; 

				if( !line.equals(""))
					update_PEERs_line( line,PEERs ) ;
			} 
			br.close() ;
			fr.close() ;
			iscan.close() ;
		}
		catch (Exception ex) { // Catch block to handle exceptions
			System.out.println(ex.getMessage());
		}
	}//'''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''
	//..............................................................................................................
	// Example of peers.txt line: Martin_01 192.168.1.186 6004
	public void update_PEERs_line( String linearg , HashMap< String,String > PEERs ) {
		
		String[] line_ARR = linearg.split(" ") ;
		
		String username = line_ARR[ 0 ] ; String value = "" ;
		
		int idx = 0 ;
		for( String token: line_ARR ) {
			
			if( idx==0 ){ idx++ ; continue ; }
			
			value += token+" " ; 
			idx++ ;
		}
		PEERs.put( username,value.trim() ) ;
	}//'''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''
	//..............................................................................................................
	// This function persists the PEERs into peers.txt file--in case the Directory Server crashes 
	// The output should look like the following example:
	//          Martin_01 192.168.1.186 6004
	//          Juan_01 192.168.1.186 6007
	//          Tristan_01 192.168.1.186 6010
	public void persist_PEERs( String path , HashMap< String,String > PEERs  ){

		String output="", key="", val="" ;

		for ( Map.Entry< String,String > entry : PEERs.entrySet() ) {	// Iterating via Hash-Table using for loop

			key = entry.getKey() ;		// e.g. key ←  Martin_01
			val = entry.getValue() ;	// e.g. val ← 192.168.1.186 6004
			
			output += key+" "+val+"\n" ;
		}

		outputToFile( path, "", output ) ; // path= "/peers.txt"
	}//'''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''
	//..............................................................................................................
	public void outputToFile( String path, String fileName , String content ) {

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
	
}//|||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||




















































