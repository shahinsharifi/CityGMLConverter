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


import java.sql.SQLException;
import java.util.HashMap;

import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.model.gml.GMLClass;


import de.tub.citydb.api.concurrent.WorkerPool;
import de.tub.citydb.modules.citykml.common.xlink.content.DBXlink;


public class SqliteImporterManager {

	private final WorkerPool<DBXlink> tmpXlinkPool;


	private final HashMap<CityGMLClass, Long> featureCounterMap;
	private final HashMap<GMLClass, Long> geometryCounterMap;
	

	public SqliteImporterManager(WorkerPool<DBXlink> tmpXlinkPool) throws SQLException {
		

		this.tmpXlinkPool = tmpXlinkPool;

		featureCounterMap = new HashMap<CityGMLClass, Long>();
		geometryCounterMap = new HashMap<GMLClass, Long>();
	}

	

	public void propagateXlink(DBXlink xlink) {
		tmpXlinkPool.addWork(xlink);
	}

	
	public void updateFeatureCounter(CityGMLClass featureType) {
		Long counter = featureCounterMap.get(featureType);
		if (counter == null)
			featureCounterMap.put(featureType, new Long(1));
		else
			featureCounterMap.put(featureType, counter + 1);
	}


	public WorkerPool<DBXlink> getTmpXlinkPool() {
		return tmpXlinkPool;
	}



	public HashMap<CityGMLClass, Long> getFeatureCounter() {
		return featureCounterMap;
	}

	public void updateGeometryCounter(GMLClass geometryType) {
		Long counter = geometryCounterMap.get(geometryType);
		if (counter == null)
			geometryCounterMap.put(geometryType, new Long(1));
		else
			geometryCounterMap.put(geometryType, counter + 1);
	}

	public HashMap<GMLClass, Long> getGeometryCounter() {
		return geometryCounterMap;
	}

	
}
