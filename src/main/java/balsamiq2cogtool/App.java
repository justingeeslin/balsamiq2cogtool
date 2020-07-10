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

			// For each branch / alternative version
			while (branches.next()) { 

				// Write to the file. Either as Official version or the name of the alternate
				String designName;
				String branchID = branches.getString(1);
				JSONObject jsonObjectBranchName = new JSONObject(branches.getString(2));
					
				if (jsonObjectBranchName.has("branchName")) {
					designName = jsonObjectBranchName.getString("branchName");
				}
				else {
					designName = "Official version";
				}

				// Include the JSON data of the branch
				outputXML.println("<design name=\"" + designName + "\">");
				
				// Add default devices, added for each design
				outputXML.println("<device>keyboard</device>");
				outputXML.println("<device>mouse</device>");

				// Discover the mockups of that version
				Statement stmtMockups = db.createStatement();
				ResultSet resources = stmtMockups.executeQuery("SELECT * FROM Resources WHERE BRANCHID = '" + branchID + "';");
				ResultSetMetaData rsmdMockups = resources.getMetaData();
				Float frameOffset = new Float(16.0);
				Float frameOffsetIncrement = new Float(200.0);
				while (resources.next()){
					
					System.out.println("Found a resource on branch " + branchID);
					System.out.println("Resource attributes: " + resources.getString(3));
					JSONObject jsonAttributes = new JSONObject(resources.getString(3));
					System.out.println(jsonAttributes.getString("kind"));
					// If it is a mockup...
					// if (jsonAttributes.getString("kind") == "mockup") {
						System.out.println("Adding frame...");
						// .. create a frame in this design
						String frameName = jsonAttributes.getString("name");
						outputXML.println("<frame name=\"" + frameName + "\">");
						outputXML.println("<topLeftOrigin x=\"" + Float.toString(frameOffset) + "\" y=\"16.0\"/>");

						// Within each frame, get the components
						JSONObject jsonData = new JSONObject(resources.getString(4));

						JSONArray mockupControls = jsonData.getJSONObject("mockup").getJSONObject("controls").getJSONArray("control");

						for (int i = 0; i < mockupControls.length(); i++) {
							JSONObject controls = mockupControls.getJSONObject(i);
							System.out.println(controls.getString("typeID"));

							String widgetName = getCogToolWidgetType(controls.getString("typeID"));

							outputXML.println("<widget name=\"" + controls.getString("typeID") + " " + Integer.toString(i) + "\" type=\"" + widgetName + "\" shape=\"rectangle\" w-is-standard=\"true\">");

							// Add extents, width/height & positioning to the widget
							outputXML.println("<displayLabel><![CDATA[Submit Query]]></displayLabel>");
        					outputXML.println("<extent x=\"" + controls.getString("x") + "\" y=\"" + controls.getString("y") + "\" width=\"179.0\" height=\"58.0\"/>");

							outputXML.println("</widget>");
						}

						outputXML.println("</frame>");
					// }

					//Before moving on to the next frame, change its coordinate so they don't stack when imported.
					frameOffset += frameOffsetIncrement;
				}


				outputXML.println("</design>");
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

	private static String getCogToolWidgetType(String balsamiqType) {
		String cogToolType = "non-interactive";

		if (balsamiqType.equals("Button")) {
			cogToolType = "button";
		}

		return cogToolType;
	}
}
