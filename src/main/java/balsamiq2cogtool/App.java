package balsamiq2cogtool;

import java.io.PrintStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import org.sqlite.JDBC'

public class App 
{
    public static void main( String[] args )
    {
        System.out.println("Running..");

		try {
					
			/* Connect to sqlite database */
			Class.forName("org.sqlite.JDBC");
			Connection db = DriverManager.getConnection("jdbc:sqlite:TypeTesting.bmpr"); 
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
    }
}
