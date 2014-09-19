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
package de.tub.citydb.modules.citykml.util;


import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.DriverManager;
import java.sql.ResultSet;

import javax.xml.bind.JAXBContext;

import net.opengis.kml._2.ObjectFactory;

import org.citygml4j.factory.CityGMLFactory;
import org.citygml4j.util.xml.SAXEventBuffer;
import org.h2.table.Table;

import de.tub.citydb.api.concurrent.Worker;
import de.tub.citydb.api.concurrent.WorkerPool;
import de.tub.citydb.api.event.EventDispatcher;
import de.tub.citydb.config.Config;
import de.tub.citydb.database.DatabaseConnectionPool;
import de.tub.citydb.log.Logger;
import de.tub.citydb.modules.kml.concurrent.KmlExportWorker;
import de.tub.citydb.modules.kml.database.KmlSplittingResult;


public class SQLiteFactory {

	
	private final static Logger LOG = Logger.getInstance();
	private String dbName;
	private String dbPath; 
	private String DriverName;
	private static Connection conn;
	
	public SQLiteFactory(String dbName, String dbPath, String DriverName) throws Exception {
		
		this.dbName = dbName;
		this.dbPath = dbPath;
		this.DriverName = DriverName;
	}
	
	
	
	public Connection createConnection() {
		
		Connection conn = null;
		try {
	        
			// register the driver 
	        Class.forName(DriverName);
	 
	        // now we set up a set of fairly basic string variables 
	        String sJdbc = "jdbc:sqlite";
	        String sDbUrl = sJdbc + ":" + dbPath +"\\"+ dbName;
	        // which will produce a legitimate Url for SqlLite JDBC
	        	 
	        conn = DriverManager.getConnection(sDbUrl);
			        
		} 
	    catch (Exception e) {
	    	    
			LOG.error(e.toString());
		}
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


