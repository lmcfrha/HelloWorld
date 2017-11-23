package oracledbquery;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.Properties;



public class OracleDBQuery {

	
		   // JDBC driver name and database URL
		   static final String JDBC_DRIVER = "oracle.jdbc.driver.OracleDriver";  
		   static final String DB_URL = "jdbc:oracle:thin:spp/spp@10.188.75.101:1536/NSIMPDB ";

		   //  Database credentials
		   static final String USER = "spp";
		   static final String PASS = "spp";
		   
		   public static void main(String[] args) {
		
			   Properties dbProps = new Properties();
			   FileInputStream in=null;
			   

			try {
				in =  new FileInputStream("./datasource.properties");
			} catch (FileNotFoundException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
			try {
				dbProps.load(in);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			   try {
				in.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
   
			   
		   Connection conn = null;
		   PreparedStatement preparedStatement=null;
		   try{
		      //STEP 2: Register JDBC driver
		      Class.forName(dbProps.getProperty("JDBC_DRIVER"));

		      //STEP 3: Open a connection
//		      System.out.println("Connecting to database...");
		      conn = DriverManager.getConnection(dbProps.getProperty("DB_URL"),dbProps.getProperty("USER"),dbProps.getProperty("PASS"));

		      //STEP 4: Execute a query
//		      System.out.println("Creating statement...");
		      if (args[0].equals("msisdn")) {
		        String selectSQL = "SELECT msisdn,device_id from device_push_token where push_token= ? and service_name= ?";
		        preparedStatement = conn.prepareStatement(selectSQL);
		        preparedStatement.setString(1, args[1]);
	            preparedStatement.setString(2, args[2]);
		        ResultSet rs = preparedStatement.executeQuery();
		      // Extract data from result set
		        while(rs.next()){
		         //Retrieve by column name
//		           int id  = rs.getInt("id");
//		           int age = rs.getInt("age");
		           String msisdn = rs.getString("msisdn");
		           String device_id = rs.getString("device_id");
		         //Display values
//		         System.out.print("ID: " + id);
//		         System.out.print(", Age: " + age);
//		         System.out.print(", First: " + first);
  		           System.out.println(msisdn+" "+device_id);
		        }
		        rs.close();
		      }
		      else if (args[0].equals("token")) {
			        String selectSQL = "SELECT push_token,last_modify_time from device_push_token where msisdn= ? and service_name= ? and device_id= ?";
			        preparedStatement = conn.prepareStatement(selectSQL);
			        preparedStatement.setString(1, args[1]);
		            preparedStatement.setString(2, args[2]);
		            preparedStatement.setString(3, args[3]);
			        ResultSet rs = preparedStatement.executeQuery();
			      // Extract data from result set
			        while(rs.next()){
			         //Retrieve by column name
//			           int id  = rs.getInt("id");
//			           int age = rs.getInt("age");
			           String token = rs.getString("push_token");
			           String time = rs.getString("last_modify_time");
			         //Display values
//			         System.out.print("ID: " + id);
//			         System.out.print(", Age: " + age);
//			         System.out.print(", First: " + first);
	  		           System.out.println(token+" "+time);
			        }
			        rs.close();
		      }
		      //STEP 6: Clean-up environment
		      
		      preparedStatement.close();
		      conn.close();
		   }catch(SQLException se){
		      //Handle errors for JDBC
		      se.printStackTrace();
		   }catch(Exception e){
		      //Handle errors for Class.forName
		      e.printStackTrace();
		   }finally{
		      //finally block used to close resources
		      try{
		         if(preparedStatement!=null)
		            preparedStatement.close();
		      }catch(SQLException se2){
		      }// nothing we can do
		      try{
		         if(conn!=null)
		            conn.close();
		      }catch(SQLException se){
		         se.printStackTrace();
		      }//end finally try
		   }//end try
//		   System.out.println("Goodbye!");
		}//end main
	
	}


