package balsamiq2cogtool;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import org.sqlite.JDBC;

public class App
{

	private static Connection db;

    public static void main( String[] args )
    {
        System.out.println("Running..");

		try {
					
			/* Connect to sqlite database */
			Class.forName("org.sqlite.JDBC");
			db = DriverManager.getConnection("jdbc:sqlite:TypeTesting.bmpr"); 
			System.out.println("Opened a sql file..");
					
			db.close();
		
		} catch (ClassNotFoundException e) {
	
			String errMsg = e.getMessage();
			System.err.println("ClassNotFound " + errMsg); 

		} catch (SQLException e) {
		
			String errMsg = e.getMessage();
			System.err.println(errMsg); 
			main(null);

		} catch (Exception e) {
			
			String errMsg = e.getMessage();
			System.err.println(errMsg); 
			main(null);
		}

		parseBalsamiqFile();
    }

	private static void parseBalsamiqFile(){
		PrintStream outputXML;
		try {
			outputXML = new PrintStream("cogtool.XML");
		} catch (FileNotFoundException e) {
			
			String errMsg = e.getMessage();
			System.err.println("File not found. " + errMsg); 
			return;

		} catch (Exception e) {
			
			String errMsg = e.getMessage();
			System.err.println(errMsg); 
			return;
		}

		System.out.println("About to write a CogTool XML...");
		/*Create and open new movie.xml file*/
		
		outputXML.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		outputXML.println("<cogtoolimport version=\"1\">");

		outputXML.println("</cogtoolimport>");
		outputXML.close();

	}
}
