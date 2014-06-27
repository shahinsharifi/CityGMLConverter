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
package de.tub.citydb.modules.citykml.content2;

import java.io.File;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;

import javax.xml.namespace.QName;

import org.citygml4j.builder.jaxb.JAXBBuilder;
import org.citygml4j.builder.jaxb.xml.io.reader.CityGMLChunk;
import org.citygml4j.builder.jaxb.xml.io.reader.JAXBChunkReader;
import org.citygml4j.model.citygml.CityGML;
import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.model.citygml.appearance.Appearance;
import org.citygml4j.model.citygml.building.AbstractBuilding;
import org.citygml4j.model.citygml.core.AbstractCityObject;
import org.citygml4j.model.citygml.core.CityModel;
import org.citygml4j.model.gml.geometry.primitives.Envelope;
import org.citygml4j.xml.io.CityGMLInputFactory;
import org.citygml4j.xml.io.reader.CityGMLInputFilter;
import org.citygml4j.xml.io.reader.CityGMLReadException;
import org.citygml4j.xml.io.reader.FeatureReadMode;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.postgis.Geometry;
import org.postgis.PGgeometry;

import sun.util.logging.resources.logging;
import de.tub.citydb.api.concurrent.WorkerPool;
import de.tub.citydb.api.database.DatabaseSrs;
import de.tub.citydb.api.gui.BoundingBox;
import de.tub.citydb.config.Config;
import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.config.project.exporter.ExportFilterConfig;
import de.tub.citydb.config.project.filter.Tiling;
import de.tub.citydb.config.project.filter.TilingMode;
import de.tub.citydb.config.project.CitykmlExporter.DisplayForm;
import de.tub.citydb.database.DatabaseConnectionPool;
import de.tub.citydb.log.Logger;
import de.tub.citydb.modules.common.filter.ExportFilter;
import de.tub.citydb.modules.citykml.controller.CityKmlExporter;
import de.tub.citydb.modules.citykml.util.ProjConvertor;
import de.tub.citydb.modules.citykml.content2.KmlSplittingResult;
import de.tub.citydb.modules.citykml.content2.Queries;
import de.tub.citydb.modules.kml.util.CityObject4JSON;
import de.tub.citydb.util.Util;

public class KmlSplitter {

	private static HashSet<CityGMLClass> CURRENTLY_ALLOWED_CITY_OBJECT_TYPES = new HashSet<CityGMLClass>();

	private final WorkerPool<KmlSplittingResult> dbWorkerPool;
	private final DisplayForm displayForm;
	private final ExportFilter exportFilter;
	private final Config config;
	private ExportFilterConfig filterConfig;
	private final JAXBBuilder jaxbBuilder;
	private volatile boolean shouldRun = true;

	private final Logger LOG = Logger.getInstance();

	private Connection connection;
	private DatabaseSrs dbSrs;
	private String TargetSrs = "";

	public KmlSplitter( 
			WorkerPool<KmlSplittingResult> dbWorkerPool, 
			ExportFilter exportFilter,
			String _TargetSrs,
			JAXBBuilder jaxbBuilder,
			DisplayForm displayForm,
			Config config) throws SQLException {

		this.dbWorkerPool = dbWorkerPool;
		this.exportFilter = exportFilter;
		this.TargetSrs = _TargetSrs;
		this.jaxbBuilder = jaxbBuilder;
		this.displayForm = displayForm;
		this.config = config;

		this.filterConfig = config.getProject().getCityKmlExporter().getFilter();
		CURRENTLY_ALLOWED_CITY_OBJECT_TYPES.clear();
		if (filterConfig.getComplexFilter().getFeatureClass().isSetBuilding() 
				&& config.getProject().getKmlExporter().getLodToExportFrom() > 0) {
			CURRENTLY_ALLOWED_CITY_OBJECT_TYPES.add(CityGMLClass.BUILDING);
		}
		if (filterConfig.getComplexFilter().getFeatureClass().isSetWaterBody()) {
			CURRENTLY_ALLOWED_CITY_OBJECT_TYPES.add(CityGMLClass.WATER_BODY);
			/*
			CURRENTLY_ALLOWED_CITY_OBJECT_TYPES.add(CityGMLClass.WATER_SURFACE);
			CURRENTLY_ALLOWED_CITY_OBJECT_TYPES.add(CityGMLClass.WATER_CLOSURE_SURFACE);
			CURRENTLY_ALLOWED_CITY_OBJECT_TYPES.add(CityGMLClass.WATER_GROUND_SURFACE);
			 */
		}
		if (filterConfig.getComplexFilter().getFeatureClass().isSetLandUse()) {
			CURRENTLY_ALLOWED_CITY_OBJECT_TYPES.add(CityGMLClass.LAND_USE);
		}
		if (filterConfig.getComplexFilter().getFeatureClass().isSetVegetation()
				&& config.getProject().getKmlExporter().getLodToExportFrom() > 0) {
			CURRENTLY_ALLOWED_CITY_OBJECT_TYPES.add(CityGMLClass.SOLITARY_VEGETATION_OBJECT);
			CURRENTLY_ALLOWED_CITY_OBJECT_TYPES.add(CityGMLClass.PLANT_COVER);
		}
		if (filterConfig.getComplexFilter().getFeatureClass().isSetTransportation()) {
			/*
			CURRENTLY_ALLOWED_CITY_OBJECT_TYPES.add(CityGMLClass.TRAFFIC_AREA);
			CURRENTLY_ALLOWED_CITY_OBJECT_TYPES.add(CityGMLClass.AUXILIARY_TRAFFIC_AREA);
			 */
			CURRENTLY_ALLOWED_CITY_OBJECT_TYPES.add(CityGMLClass.TRANSPORTATION_COMPLEX);
			CURRENTLY_ALLOWED_CITY_OBJECT_TYPES.add(CityGMLClass.TRACK);
			CURRENTLY_ALLOWED_CITY_OBJECT_TYPES.add(CityGMLClass.RAILWAY);
			CURRENTLY_ALLOWED_CITY_OBJECT_TYPES.add(CityGMLClass.ROAD);
			CURRENTLY_ALLOWED_CITY_OBJECT_TYPES.add(CityGMLClass.SQUARE);
		}
		if (filterConfig.getComplexFilter().getFeatureClass().isSetReliefFeature()
				&& config.getProject().getKmlExporter().getLodToExportFrom() > 0) {
			CURRENTLY_ALLOWED_CITY_OBJECT_TYPES.add(CityGMLClass.RELIEF_FEATURE);
			/*

			 */
		}
		if (filterConfig.getComplexFilter().getFeatureClass().isSetGenericCityObject()) {
			CURRENTLY_ALLOWED_CITY_OBJECT_TYPES.add(CityGMLClass.GENERIC_CITY_OBJECT);
		}
		if (filterConfig.getComplexFilter().getFeatureClass().isSetCityFurniture()
				&& config.getProject().getKmlExporter().getLodToExportFrom() > 0) {
			CURRENTLY_ALLOWED_CITY_OBJECT_TYPES.add(CityGMLClass.CITY_FURNITURE);
		}
		if (filterConfig.getComplexFilter().getFeatureClass().isSetCityObjectGroup()
				&& config.getProject().getKmlExporter().getLodToExportFrom() > 0) {
			CURRENTLY_ALLOWED_CITY_OBJECT_TYPES.add(CityGMLClass.CITY_OBJECT_GROUP);
		}



		// try and change workspace for connection if needed
		/*Database database = config.getProject().getDatabase();
		dbConnectionPool.gotoWorkspace(connection, 
										 database.getWorkspaces().getKmlExportWorkspace());*/

	}

	private void queryObjects(File file) throws SQLException {


		if (filterConfig.isSetSimpleFilter()) {
			for (String gmlId: filterConfig.getSimpleFilter().getGmlIdFilter().getGmlIds()) {
				if (!shouldRun) break;


			}
		}
		else if (filterConfig.isSetComplexFilter() &&
				filterConfig.getComplexFilter().getTiledBoundingBox().isSet()) {

			try {

				int boundingBoxSrs = filterConfig.getComplexFilter().getTiledBoundingBox().getSrs().getSrid();
				de.tub.citydb.modules.citykml.util.BoundingBox _bounds = null;
				Tiling tiling = config.getProject().getCityKmlExporter().getFilter().getComplexFilter().getTiledBoundingBox().getTiling();

				if(tiling.getMode() != TilingMode.NO_TILING)
				{
					BoundingBox tile = exportFilter.getBoundingBoxFilter().getFilterState();
					
					if(boundingBoxSrs != 4326)
					{
						_bounds = new de.tub.citydb.modules.citykml.util.BoundingBox(
								tile.getLowerLeftCorner().getX() ,
								tile.getLowerLeftCorner().getY() ,
								tile.getUpperRightCorner().getX() ,
								tile.getUpperRightCorner().getY() ,
								this.TargetSrs);						

					}else {

						_bounds = new de.tub.citydb.modules.citykml.util.BoundingBox(
								tile.getLowerLeftCorner().getX() ,
								tile.getLowerLeftCorner().getY() ,
								tile.getUpperRightCorner().getX() ,
								tile.getUpperRightCorner().getY(),
								"4326");
					}
					
				}
				else{

					if(boundingBoxSrs != 4326)
					{

						BoundingBox BBox = filterConfig.getComplexFilter().getTiledBoundingBox();

						_bounds = new de.tub.citydb.modules.citykml.util.BoundingBox(
								BBox.getLowerLeftCorner().getX() ,
								BBox.getLowerLeftCorner().getY() ,
								BBox.getUpperRightCorner().getX() ,
								BBox.getUpperRightCorner().getY() ,
								this.TargetSrs);

					}else {


					//	BoundingBox BBox = ProjConvertor.transformBBox(filterConfig.getComplexFilter().getTiledBoundingBox() , "4326", this.TargetSrs); 
						BoundingBox BBox = filterConfig.getComplexFilter().getTiledBoundingBox();
						
						_bounds = new de.tub.citydb.modules.citykml.util.BoundingBox(
								BBox.getLowerLeftCorner().getX() ,
								BBox.getLowerLeftCorner().getY() ,
								BBox.getUpperRightCorner().getX() ,
								BBox.getUpperRightCorner().getY() ,
								"4326");

					}

				}

				
				//****************************************
				
				// prepare CityGML input factory

				CityGMLInputFactory in = null;
				try {
					in = jaxbBuilder.createCityGMLInputFactory();
					in.setProperty(CityGMLInputFactory.FEATURE_READ_MODE, FeatureReadMode.SPLIT_PER_COLLECTION_MEMBER);
					in.setProperty(CityGMLInputFactory.FAIL_ON_MISSING_ADE_SCHEMA, false);
					in.setProperty(CityGMLInputFactory.PARSE_SCHEMA, false);
					in.setProperty(CityGMLInputFactory.SPLIT_AT_FEATURE_PROPERTY, new QName("generalizesTo"));
					in.setProperty(CityGMLInputFactory.EXCLUDE_FROM_SPLITTING, CityModel.class);
				} catch (CityGMLReadException e) {
					LOG.error("Failed to initialize CityGML parser. Aborting.");
					
				}


				// prepare feature filter

				CityGMLInputFilter inputFilter = new CityGMLInputFilter() {
					public boolean accept(CityGMLClass type) {
						return true;
					}
				};



				JAXBChunkReader _ChunkReader = (JAXBChunkReader)in.createFilteredCityGMLReader(in.createCityGMLReader(file), inputFilter);	


				//	LOG.info("Writing placemarks into target file, Please wait ...");

				while (_ChunkReader.hasNextChunk()) {


					CityGMLChunk chunk = _ChunkReader.nextChunk();

					CityGML _CityGML = chunk.unmarshal();

					CityGMLClass cityObjectType = _CityGML.getCityGMLClass();

					AbstractCityObject cityObject = (AbstractCityObject)_CityGML;

					// bounding box filter
					// first of all compute bounding box for cityobject since we need it anyways

					Envelope envelope = cityObject.getBoundedBy().getEnvelope().convert3d();
					ReferencedEnvelope _refEnvelope = new ReferencedEnvelope(
							envelope.getLowerCorner().toList3d().get(0),
							envelope.getUpperCorner().toList3d().get(0),	
							envelope.getLowerCorner().toList3d().get(1),							
							envelope.getUpperCorner().toList3d().get(1),
							CRS.decode("EPSG:" + this.TargetSrs, true));


					if(envelope != null && _bounds.ContainCentroid(_refEnvelope,TargetSrs))						
					{						
						if(cityObjectType != CityGMLClass.APPEARANCE)
						{
							KmlSplittingResult splitter = new KmlSplittingResult(cityObject.getId() ,_CityGML , cityObjectType, displayForm, TargetSrs);										
							dbWorkerPool.addWork(splitter);				
						}
						else {
						}
					}
				}


			} catch (Exception e) {

				System.out.println(e.toString());

			}


		}
	}

	public void startQuery(File reader) throws SQLException {
		try {

			queryObjects(reader);

			if (shouldRun) {

				try {

					dbWorkerPool.join();
				}
				catch (InterruptedException e) {}
			}

		}
		finally {
			if (connection != null) {
				try {
					connection.close();
				}
				catch (SQLException sqlEx) {}

				connection = null;
			}
		}
	}

	public void shutdown() {
		shouldRun = false;
	}

	

	private double[] getEnvelopeInWGS84(long id) {
		double[] ordinatesArray = null;
		PreparedStatement psQuery = null;
		ResultSet rs = null;

		try {
			psQuery = dbSrs.is3D() ? 
					connection.prepareStatement(Queries.GET_ENVELOPE_IN_WGS84_3D_FROM_ID):
						connection.prepareStatement(Queries.GET_ENVELOPE_IN_WGS84_FROM_ID);

					psQuery.setLong(1, id);

					rs = psQuery.executeQuery();
					if (rs.next()) {
						PGgeometry pgGeom = (PGgeometry)rs.getObject(1); 
						if (!rs.wasNull() && pgGeom != null) {
							Geometry geom = pgGeom.getGeometry();

							ordinatesArray = new double[geom.numPoints() * 3];

							for (int i=0, j=0; i<geom.numPoints(); i+=3, j++){
								ordinatesArray[i] = geom.getPoint(j).x;
								ordinatesArray[i+1] = geom.getPoint(j).y;
								ordinatesArray[i+2] = geom.getPoint(j).z;
							}
						}
					}
		} 
		catch (SQLException sqlEx) {}
		finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException sqlEx) {}

				rs = null;
			}

			if (psQuery != null) {
				try {
					psQuery.close();
				} catch (SQLException sqlEx) {}

				psQuery = null;
			}
		}
		return ordinatesArray;
	}

}
