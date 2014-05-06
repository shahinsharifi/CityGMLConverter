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
package de.tub.citydb.modules.citykml.concurrent;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;




import org.citygml4j.builder.jaxb.JAXBBuilder;
import org.citygml4j.model.citygml.CityGML;
import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.model.citygml.appearance.Appearance;
import org.citygml4j.model.citygml.building.AbstractBuilding;
import org.citygml4j.model.citygml.building.Building;
import org.citygml4j.model.citygml.building.BuildingInstallation;
import org.citygml4j.model.citygml.building.BuildingInstallationProperty;
import org.citygml4j.model.citygml.cityfurniture.CityFurniture;
import org.citygml4j.model.citygml.cityobjectgroup.CityObjectGroup;
import org.citygml4j.model.citygml.core.AbstractCityObject;
import org.citygml4j.model.citygml.generics.GenericCityObject;
import org.citygml4j.model.citygml.landuse.LandUse;
import org.citygml4j.model.citygml.relief.ReliefFeature;
import org.citygml4j.model.citygml.transportation.TransportationComplex;
import org.citygml4j.model.citygml.vegetation.PlantCover;
import org.citygml4j.model.citygml.vegetation.SolitaryVegetationObject;
import org.citygml4j.model.citygml.waterbody.WaterBody;
import org.citygml4j.model.gml.basicTypes.Code;
import org.citygml4j.model.gml.basicTypes.Coordinates;
import org.citygml4j.model.gml.feature.AbstractFeature;
import org.citygml4j.model.gml.geometry.primitives.Envelope;

import de.tub.citydb.api.concurrent.Worker;
import de.tub.citydb.api.concurrent.WorkerPool;
import de.tub.citydb.api.concurrent.WorkerPool.WorkQueue;
import de.tub.citydb.api.event.EventDispatcher;
import de.tub.citydb.config.Config;
import de.tub.citydb.config.project.database.Database;
import de.tub.citydb.database.DatabaseConnectionPool;
import de.tub.citydb.log.Logger;
import de.tub.citydb.modules.citygml.common.database.gmlid.DBGmlIdLookupServerManager;
import de.tub.citydb.modules.citygml.common.database.xlink.DBXlink;
import de.tub.citydb.modules.citykml.content.DBAppearance;
import de.tub.citydb.modules.citykml.content.DBCityFurniture;
import de.tub.citydb.modules.citykml.content.DBCityObjectGroup;
import de.tub.citydb.modules.citykml.content.DBGenericCityObject;
import de.tub.citydb.modules.citykml.content.DBLandUse;
import de.tub.citydb.modules.citykml.content.DBPlantCover;
import de.tub.citydb.modules.citykml.content.DBReliefFeature;
import de.tub.citydb.modules.citykml.content.DBSolitaryVegetatObject;
import de.tub.citydb.modules.citykml.content.DBTransportationComplex;
import de.tub.citydb.modules.citykml.content.DBWaterBody;
import de.tub.citydb.modules.citykml.content.DbBuilding;
import de.tub.citydb.modules.citykml.content.ImporterEnum;
import de.tub.citydb.modules.citykml.content.ImporterManager;
import de.tub.citydb.modules.citykml.util.KMLObject;
import de.tub.citydb.modules.common.event.CounterEvent;
import de.tub.citydb.modules.common.event.CounterType;
import de.tub.citydb.modules.common.event.FeatureCounterEvent;
import de.tub.citydb.modules.common.event.GeometryCounterEvent;
import de.tub.citydb.modules.common.filter.ImportFilter;
import de.tub.citydb.modules.common.filter.feature.BoundingBoxFilter;
import de.tub.citydb.modules.common.filter.feature.GmlIdFilter;
import de.tub.citydb.modules.common.filter.feature.GmlNameFilter;
import de.tub.citydb.util.Util;


import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.PrecisionModel;



public class CityKmlImportWorker implements Worker<CityGML> {

	private final Logger LOG = Logger.getInstance();

	// instance members needed for WorkPool
	private volatile boolean shouldRun = true;
	private ReentrantLock runLock = new ReentrantLock();
	private WorkQueue<CityGML> workQueue = null;
	private CityGML firstWork;
	private Thread workerThread = null;

	// instance members needed to do work
	private final DatabaseConnectionPool dbConnectionPool;
	private final JAXBBuilder jaxbBuilder;
	private final WorkerPool<DBXlink> tmpXlinkPool;
	private final DBGmlIdLookupServerManager lookupServerManager;
	private final Config config;
	private final EventDispatcher eventDispatcher;
	private final ImportFilter importFilter;
	private Connection batchConn;
	private ImporterManager dbImporterManager;
	private int updateCounter = 0;
	private int commitAfter = 20;

	// filter
	private BoundingBoxFilter featureBoundingBoxFilter;
	private GmlIdFilter featureGmlIdFilter;
	private GmlNameFilter featureGmlNameFilter;
	private static List<List<Double>> pointList = new ArrayList<List<Double>>();
	private static List<de.tub.citydb.modules.citykml.content1.Building> _building
		= new ArrayList<de.tub.citydb.modules.citykml.content1.Building>();
	
	
	public CityKmlImportWorker(DatabaseConnectionPool dbConnectionPool,
			JAXBBuilder jaxbBuilder,
			WorkerPool<DBXlink> tmpXlinkPool,
			DBGmlIdLookupServerManager lookupServerManager,
			ImportFilter importFilter,
			Config config,
			EventDispatcher eventDispatcher) throws SQLException {
		this.dbConnectionPool = dbConnectionPool;
		this.jaxbBuilder = jaxbBuilder;
		this.tmpXlinkPool = tmpXlinkPool;
		this.lookupServerManager = lookupServerManager;
		this.importFilter = importFilter;
		this.config = config;
		this.eventDispatcher = eventDispatcher;


		init();
	}

	private void init() throws SQLException {
	//	batchConn = dbConnectionPool.getConnection();
	//	batchConn.setAutoCommit(false);
		// try and change workspace for both connections if needed
	//	Database database = config.getProject().getDatabase();
		//		Workspace workspace = database.getWorkspaces().getImportWorkspace();
		//		dbConnectionPool.gotoWorkspace(batchConn, workspace);
		//		dbConnectionPool.gotoWorkspace(commitConn, workspace);

		// init filter 
		featureBoundingBoxFilter = importFilter.getBoundingBoxFilter();
		featureGmlIdFilter = importFilter.getGmlIdFilter();
		featureGmlNameFilter = importFilter.getGmlNameFilter();		

		dbImporterManager = new ImporterManager(
				batchConn,
				jaxbBuilder,
				config,
				tmpXlinkPool,
				lookupServerManager,
				eventDispatcher);

	//	Integer commitAfterProp = database.getUpdateBatching().getFeatureBatchValue();
//		if (commitAfterProp != null && commitAfterProp > 0)
	//		commitAfter = commitAfterProp;
	}

	@Override
	public Thread getThread() {
		return workerThread;
	}

	@Override
	public void interrupt() {
		shouldRun = false;
		workerThread.interrupt();
	}

	@Override
	public void interruptIfIdle() {
		final ReentrantLock runLock = this.runLock;
		shouldRun = false;

		if (runLock.tryLock()) {
			try {
				workerThread.interrupt();
			} finally {
				runLock.unlock();
			}
		}
	}

	@Override
	public void setFirstWork(CityGML firstWork) {
		this.firstWork = firstWork;
	}

	@Override
	public void setThread(Thread workerThread) {
		this.workerThread = workerThread;
	}

	@Override
	public void setWorkQueue(WorkQueue<CityGML> workQueue) {
		this.workQueue = workQueue;
	}
	

	@Override
	public void run() {
		try {
			if (firstWork != null && shouldRun) {
				doWork(firstWork);
				firstWork = null;
			}

			while (shouldRun) {
				try {
					CityGML work = workQueue.take();
					doWork(work);
				} catch (InterruptedException ie) {
					// re-check state
				}
			}
			

			try {
			
			//dbImporterManager.executeBatch();
			//	batchConn.commit();

				eventDispatcher.triggerEvent(new CounterEvent(CounterType.TOPLEVEL_FEATURE, updateCounter, this));
			} catch (Exception sqlEx) {
				LOG.error("SQL error: " + sqlEx.getMessage());
			}

			try {
		//		dbImporterManager.close();
			} catch (Exception sqlEx) {
				LOG.error("SQL error: " + sqlEx.getMessage());
			}

			eventDispatcher.triggerEvent(new FeatureCounterEvent(dbImporterManager.getFeatureCounter(), this));
			eventDispatcher.triggerEvent(new GeometryCounterEvent(dbImporterManager.getGeometryCounter(), this));
		} finally {
			if (batchConn != null) {
				try {
			//		batchConn.close();
				} catch (Exception sqlEx) {
					//
				}

				batchConn = null;
			}
		}

	}

	
	
	public List<List<Double>> GetCityGmlObject(){
		
		return pointList;
				
	}
	
	
	public List<de.tub.citydb.modules.citykml.content1.Building> GetBuilding()
	{
		return _building;			
	}
	
	private void doWork(CityGML work) {
		
		final ReentrantLock runLock = this.runLock;
		runLock.lock();
		
		

		try {
			try {
				
				long id = 0;

				if (work.getCityGMLClass() == CityGMLClass.APPEARANCE) {
					// global appearances
					DBAppearance dbAppearance = (DBAppearance)dbImporterManager.getDBImporter(ImporterEnum.APPEARANCE);
					if (dbAppearance != null)
						id = dbAppearance.insert((Appearance)work, CityGMLClass.CITY_MODEL, 0);

				} else if (CityGMLClass.ABSTRACT_CITY_OBJECT.isInstance(work.getCityGMLClass())) {
					AbstractCityObject cityObject = (AbstractCityObject)work;

					// gml:id filter
					if (featureGmlIdFilter.isActive()) {
						if (cityObject.isSetId()) {
							if (featureGmlIdFilter.filter(cityObject.getId()))
								return;
						} else
							return;
					}

					// gml:name filter
					if (featureGmlNameFilter.isActive()) {
						if (cityObject.isSetName()) {
							boolean success = false;

							for (Code code : cityObject.getName()) {
								if (code.isSetValue() && !featureGmlNameFilter.filter(code.getValue())) {
									success = true;
									break;
								}
							}

							if (!success)
								return;

						} else
							return;
					}

					// bounding box filter
					// first of all compute bounding box for cityobject since we need it anyways
					if (!cityObject.isSetBoundedBy() || !cityObject.getBoundedBy().isSetEnvelope())
						cityObject.calcBoundedBy(true);
					else if (!cityObject.getBoundedBy().getEnvelope().isSetLowerCorner() ||
							!cityObject.getBoundedBy().getEnvelope().isSetUpperCorner()){
						Envelope envelope = cityObject.getBoundedBy().getEnvelope().convert3d();
						if (envelope != null)
							cityObject.getBoundedBy().setEnvelope(envelope);
						else
							cityObject.calcBoundedBy(true);
					}

					// filter
					if (cityObject.isSetBoundedBy() && 
							featureBoundingBoxFilter.filter(cityObject.getBoundedBy().getEnvelope()))
						return;

				

					//*********************Shahin Sharifi**********************

					
					
					if(work.getCityGMLClass() == CityGMLClass.BUILDING)
					{


							// if the cityobject did pass all filters, let us further work on it
							switch (work.getCityGMLClass()) {

								case BUILDING:
									DbBuilding dbBuilding = (DbBuilding)dbImporterManager.getDBImporter(ImporterEnum.BUILDING);
									if (dbBuilding != null)
										pointList.addAll(dbBuilding.insertIntoKML((Building)work));
										//dbBuilding.insert((Building)work);
										//de.tub.citydb.modules.citykml.content1.Building _MyBuilding = dbBuilding.getBuilding();
										//_building.add(dbBuilding.getBuilding());
										break;
	
								case CITY_FURNITURE:
									DBCityFurniture dbCityFurniture = (DBCityFurniture)dbImporterManager.getDBImporter(ImporterEnum.CITY_FURNITURE);
									if (dbCityFurniture != null)
										id = dbCityFurniture.insert((CityFurniture)work);
									break;
	
								case LAND_USE:
									DBLandUse dbLandUse = (DBLandUse)dbImporterManager.getDBImporter(ImporterEnum.LAND_USE);
									if (dbLandUse != null)
										id = dbLandUse.insert((LandUse)work);
	
									break;
	
								case WATER_BODY:
									DBWaterBody dbWaterBody = (DBWaterBody)dbImporterManager.getDBImporter(ImporterEnum.WATERBODY);
									if (dbWaterBody != null)
										id = dbWaterBody.insert((WaterBody)work);
	
									break;
	
								case PLANT_COVER:
									DBPlantCover dbPlantCover = (DBPlantCover)dbImporterManager.getDBImporter(ImporterEnum.PLANT_COVER);
									if (dbPlantCover != null)
										id = dbPlantCover.insert((PlantCover)work);
	
									break;
	
								case SOLITARY_VEGETATION_OBJECT:
									DBSolitaryVegetatObject dbSolVegObject = (DBSolitaryVegetatObject)dbImporterManager.getDBImporter(ImporterEnum.SOLITARY_VEGETAT_OBJECT);
									if (dbSolVegObject != null)
										id = dbSolVegObject.insert((SolitaryVegetationObject)work);
	
									break;
	
								case TRANSPORTATION_COMPLEX:
								case ROAD:
								case RAILWAY:
								case TRACK:
								case SQUARE:
									DBTransportationComplex dbTransComplex = (DBTransportationComplex)dbImporterManager.getDBImporter(ImporterEnum.TRANSPORTATION_COMPLEX);
									if (dbTransComplex != null)
										id = dbTransComplex.insert((TransportationComplex)work);
	
									break;
								case RELIEF_FEATURE:
									DBReliefFeature dbReliefFeature = (DBReliefFeature)dbImporterManager.getDBImporter(ImporterEnum.RELIEF_FEATURE);
									if (dbReliefFeature != null)
										id = dbReliefFeature.insert((ReliefFeature)work);
	
									break;
								case GENERIC_CITY_OBJECT:
									DBGenericCityObject dbGenericCityObject = (DBGenericCityObject)dbImporterManager.getDBImporter(ImporterEnum.GENERIC_CITYOBJECT);
									if (dbGenericCityObject != null)
										id = dbGenericCityObject.insert((GenericCityObject)work);
	
									break;
								case CITY_OBJECT_GROUP:
									DBCityObjectGroup dbCityObjectGroup = (DBCityObjectGroup)dbImporterManager.getDBImporter(ImporterEnum.CITYOBJECTGROUP);
									if (dbCityObjectGroup != null)
										id = dbCityObjectGroup.insert((CityObjectGroup)work);
	
									break;
							}


							if (id != 0)
								updateCounter++;
						}
					}


			} catch (SQLException sqlEx) {
				

				return;
			}



		} finally {
			runLock.unlock();
			
		}
	}
}
