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
import org.json.*;

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

		try {
			db.close();
		}
		catch(Exception e) {

		}
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

		// Get all the alternative version / branches and import them as various designs in CogTool
		try {
			final Statement stmt = db.createStatement();
			final ResultSet branches = stmt.executeQuery("SELECT * FROM Branches;");
			final ResultSetMetaData rsmd = branches.getMetaData();

			// Add default devices
			outputXML.println("<device>keyboard</device>");
			outputXML.println("<device>mouse</device>");

			// For each branch / alternative version
			while (branches.next()) { 

				// Write to the file. Either as Official version or the name of the alternate
				String designName;
				final String branchID = branches.getString(1);
				final JSONObject ja = new JSONObject(branches.getString(2));
					
				if (ja.has("branchName")) {
					designName = ja.getString("branchName");
				}
				else {
					designName = "Official version";
				}

				// Include the JSON data of the branch
				outputXML.println("<design name=\"" + designName + "\">");
				
				// Discover the mockups of that version

			}
		}
		catch (final Exception e) {
			final String errMsg = e.getMessage();
			System.err.println(errMsg); 
			return;
		}
		

		outputXML.println("</cogtoolimport>");
		outputXML.close();

	}
}
