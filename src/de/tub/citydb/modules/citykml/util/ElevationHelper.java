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


import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;

import de.tub.citydb.log.Logger;


public class ElevationHelper {

	
	private final static Logger LOG = Logger.getInstance();
	private Connection connection;
	
	
	public ElevationHelper(Connection _connection) throws Exception {		
		connection = _connection;
	}

	
	
	public int CreateElevationTable(int TimeOut)
	{	
		String CreateCommand = "CREATE TABLE Elevations (Id integer PRIMARY KEY, gmlid text, zoffset double)";
		int nTable = 0;
		try {			
			Statement stmt = connection.createStatement();
			if(TimeOut>0)
				stmt.setQueryTimeout(TimeOut);			
			nTable = stmt.executeUpdate( CreateCommand );		
		} catch (SQLException e) {
			LOG.error(e.toString());
		}	
		return nTable;	
	}
	
	
	
	public int InsertElevationOffSet(String gmlId , double zOffSet , int TimeOut)
	{
		int nRows = 0;
		String insertCommand = "INSERT INTO Elevations(gmlid,zoffset) VALUES('" + gmlId + "'," + zOffSet + ")";
		try {		
			Statement stmt = connection.createStatement();
			if(TimeOut>0)
				stmt.setQueryTimeout(TimeOut);			
			nRows = stmt.executeUpdate( insertCommand );	
		} catch (SQLException e) {
			LOG.error(e.toString());
		}	
		return nRows;	
	}
	
	
	
	public ResultSet SelectElevationOffSet(String gmlId , int TimeOut)
	{
		ResultSet Result = null;
		String SelectCommand = "SELECT zoffset FROM Elevations WHERE gmlid LIKE '" + gmlId + "'";
		try {			
			Statement stmt = connection.createStatement();
			if(TimeOut>0)
				stmt.setQueryTimeout(TimeOut);			
			Result = stmt.executeQuery(SelectCommand);				
		} catch (SQLException e) {
			LOG.error(e.toString());
		}	
		return Result;		
	}
	
	
	// this checks whether the table has already been created or not
	public boolean IsTableCreated() throws SQLException {

		Statement stmt = connection.createStatement();
		ResultSet rs = stmt.executeQuery("SELECT count(name) AS flag FROM sqlite_master WHERE type='table' AND name='Elevations'");
		boolean IsCreated = (rs.getInt("flag") > 0) ? true : false;
		return IsCreated;	
	}

}


