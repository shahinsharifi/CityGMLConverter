/*
 * This file is part of the 3D City Database Importer/Exporter.
 * Copyright (c) 2007 - 2013
 * Institute for Geodesy and Geoinformation Science
 * Technische Universitaet Berlin, Germany
 * http://www.gis.tu-berlin.de/
 * 
 * The 3D City Database Importer/Exporter program is free software:
 * you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program. If not, see 
 * <http://www.gnu.org/licenses/>.
 * 
 * The development of the 3D City Database Importer/Exporter has 
 * been financially supported by the following cooperation partners:
 * 
 * Business Location Center, Berlin <http://www.businesslocationcenter.de/>
 * virtualcitySYSTEMS GmbH, Berlin <http://www.virtualcitysystems.de/>
 * Berlin Senate of Business, Technology and Women <http://www.berlin.de/sen/wtf/>
 */
package de.tub.citydb.modules.citykml.util.Sqlite;


import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.DriverManager;

import de.tub.citydb.log.Logger;



public class SQLiteFactory {

	
	private final static Logger LOG = Logger.getInstance();
	private String dbName;
	private String dbPath; 
	private String DriverName;
	private final Connection conn;
	
	public SQLiteFactory(String dbName, String dbPath, String DriverName) throws Exception {
		
		this.dbName = dbName;
		this.dbPath = dbPath;
		this.DriverName = DriverName;
		this.conn = createConnection();
	}
	
	
	
	public Connection createConnection() {
		
		Connection connection = null;
		try {
	        
			// register the driver 
	        Class.forName(DriverName);
	 
	        // now we set up a set of fairly basic string variables 
	        String sJdbc = "jdbc:sqlite";
	        String sDbUrl = sJdbc + ":" + dbPath +"\\"+ dbName;
	        // which will produce a legitimate Url for SqlLite JDBC
	        	 
	        connection = DriverManager.getConnection(sDbUrl);
			        
		} 
	    catch (Exception e) {
	    	    
			LOG.error(e.toString());
		}
	    return connection;

	}
	
	public Connection getConnection()
	{	
		return conn;
	}
	
	
	// this checks whether the db has already been created or not
	public boolean IsDbCreated() {

		try {
			
		  File f = new File(dbPath +"\\"+ dbName);			 
		  if(f.exists()){			  
			  return true;		  
		  }else{			
			  return false;
		  }
			
		} catch (Exception e) {			
			LOG.error("FileCheck:" + e.toString());
			return false;
		}
	}
	
	
	
	public boolean KillConnection() throws SQLException{
		
		conn.close();
		return conn.isClosed();
		
	}
	

}


