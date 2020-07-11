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
import java.util.Hashtable;

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
		
		// Counter for widget groups.
		int groupCounter = 1;

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
							System.out.println("Controls " + controls);
							

							String widgetName = getCogToolWidgetType(controls.getString("typeID"));

							// Some widgets require extra properties. Likely, because these are often grouped.
							String extraProperties = "";
							
							if (widgetName.equals("check box") || widgetName.equals("radio button")) {
								extraProperties = "x=\"0.0\" y=\"0.0\" group=\"Group [i" + Integer.toString(groupCounter) + "]\"";
								groupCounter++;
							}

							outputXML.println("<widget name=\"" + controls.getString("typeID") + " " + Integer.toString(i) + "\" type=\"" + widgetName + "\" " + extraProperties + " shape=\"rectangle\" w-is-standard=\"true\">");

							// Get properties like text and links, etc.
							String widgetText = "Widget";
							if (controls.has("properties")) {
								JSONObject controlProperties = controls.getJSONObject("properties");
								System.out.println("Properties " + controlProperties);

								if (controlProperties.has("text")) {
									widgetText = controlProperties.getString("text");
								}

								if (controlProperties.has("href")) {
									String linkedToMockup = controlProperties.getJSONObject("href").getString("ID");
								}
								
							}
							
							
							// Get the text
							// String widgetText = controlProperties.getString("text");
							
							
							outputXML.println("<displayLabel><![CDATA[" + widgetText + "]]></displayLabel>");
							// Add extents, width/height & positioning to the widget

							// Get the Width and Height either in w or in measuredW
							String widgetWidth = "180";
							if (controls.has("w")) {
								widgetWidth = controls.getString("w");
							}
							if (controls.has("measuredW")) {
								widgetWidth = controls.getString("measuredW");
							}

							String widgetHeight = "60";
							if (controls.has("h")) {
								widgetHeight = controls.getString("h");
							}
							if (controls.has("measuredH")) {
								widgetHeight = controls.getString("measuredH");
							}

        					outputXML.println("<extent x=\"" + controls.getString("x") + "\" y=\"" + controls.getString("y") + "\" width=\"" + widgetWidth + "\" height=\"" + widgetHeight + "\"/>");

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
		Hashtable<String, String> balsamiq2CogToolWidgetType = new Hashtable<String, String>();

		// Balsamiq type, CogTool widget type
		balsamiq2CogToolWidgetType.put("Button", "button");
		balsamiq2CogToolWidgetType.put("Link", "link");
		balsamiq2CogToolWidgetType.put("CheckBox", "check box");
		balsamiq2CogToolWidgetType.put("RadioButton", "radio button");
		balsamiq2CogToolWidgetType.put("TextInput", "text box");

		if (balsamiq2CogToolWidgetType.containsKey(balsamiqType)){
			return balsamiq2CogToolWidgetType.get(balsamiqType);
		}
		else {
			// A resonable default for the balsamiq types we don't anticpate.
			return "non-interactive";
		}

	}
}
