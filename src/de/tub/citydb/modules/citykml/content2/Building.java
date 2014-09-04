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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.vecmath.Point3d;
import javax.xml.bind.JAXBException;

import net.opengis.kml._2.AltitudeModeEnumType;
import net.opengis.kml._2.BoundaryType;
import net.opengis.kml._2.LinearRingType;
import net.opengis.kml._2.MultiGeometryType;
import net.opengis.kml._2.PlacemarkType;
import net.opengis.kml._2.PolygonType;

import org.citygml4j.factory.CityGMLFactory;
import org.citygml4j.geometry.Matrix;
import org.citygml4j.model.citygml.CityGML;
import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.model.citygml.appearance.AbstractSurfaceData;
import org.citygml4j.model.citygml.appearance.AbstractTextureParameterization;
import org.citygml4j.model.citygml.appearance.Appearance;
import org.citygml4j.model.citygml.appearance.AppearanceProperty;
import org.citygml4j.model.citygml.appearance.ParameterizedTexture;
import org.citygml4j.model.citygml.appearance.SurfaceDataProperty;
import org.citygml4j.model.citygml.appearance.TexCoordGen;
import org.citygml4j.model.citygml.appearance.TexCoordList;
import org.citygml4j.model.citygml.appearance.TextureAssociation;
import org.citygml4j.model.citygml.appearance.TextureCoordinates;
import org.citygml4j.model.citygml.appearance.X3DMaterial;
import org.citygml4j.model.citygml.building.AbstractBoundarySurface;
import org.citygml4j.model.citygml.building.AbstractBuilding;
import org.citygml4j.model.citygml.building.AbstractOpening;
import org.citygml4j.model.citygml.building.BoundarySurfaceProperty;
import org.citygml4j.model.citygml.building.BuildingFurniture;
import org.citygml4j.model.citygml.building.BuildingInstallation;
import org.citygml4j.model.citygml.building.BuildingInstallationProperty;
import org.citygml4j.model.citygml.building.BuildingPart;
import org.citygml4j.model.citygml.building.BuildingPartProperty;
import org.citygml4j.model.citygml.building.IntBuildingInstallation;
import org.citygml4j.model.citygml.building.IntBuildingInstallationProperty;
import org.citygml4j.model.citygml.building.InteriorFurnitureProperty;
import org.citygml4j.model.citygml.building.InteriorRoomProperty;
import org.citygml4j.model.citygml.building.OpeningProperty;
import org.citygml4j.model.citygml.building.Room;
import org.citygml4j.model.citygml.texturedsurface._TexturedSurface;
import org.citygml4j.model.gml.GMLClass;
import org.citygml4j.model.gml.base.AssociationAttributeGroup;
import org.citygml4j.model.gml.geometry.AbstractGeometry;
import org.citygml4j.model.gml.geometry.GeometryProperty;
import org.citygml4j.model.gml.geometry.aggregates.MultiCurveProperty;
import org.citygml4j.model.gml.geometry.aggregates.MultiPolygon;
import org.citygml4j.model.gml.geometry.aggregates.MultiSolid;
import org.citygml4j.model.gml.geometry.aggregates.MultiSurface;
import org.citygml4j.model.gml.geometry.aggregates.MultiSurfaceProperty;
import org.citygml4j.model.gml.geometry.complexes.CompositeSolid;
import org.citygml4j.model.gml.geometry.complexes.CompositeSurface;
import org.citygml4j.model.gml.geometry.complexes.GeometricComplex;
import org.citygml4j.model.gml.geometry.primitives.AbstractRing;
import org.citygml4j.model.gml.geometry.primitives.AbstractRingProperty;
import org.citygml4j.model.gml.geometry.primitives.AbstractSolid;
import org.citygml4j.model.gml.geometry.primitives.AbstractSurface;
import org.citygml4j.model.gml.geometry.primitives.AbstractSurfacePatch;
import org.citygml4j.model.gml.geometry.primitives.GeometricPrimitiveProperty;
import org.citygml4j.model.gml.geometry.primitives.LinearRing;
import org.citygml4j.model.gml.geometry.primitives.OrientableSurface;
import org.citygml4j.model.gml.geometry.primitives.PolygonProperty;
import org.citygml4j.model.gml.geometry.primitives.Rectangle;
import org.citygml4j.model.gml.geometry.primitives.Solid;
import org.citygml4j.model.gml.geometry.primitives.SolidArrayProperty;
import org.citygml4j.model.gml.geometry.primitives.SolidProperty;
import org.citygml4j.model.gml.geometry.primitives.Surface;
import org.citygml4j.model.gml.geometry.primitives.SurfaceArrayProperty;
import org.citygml4j.model.gml.geometry.primitives.SurfacePatchArrayProperty;
import org.citygml4j.model.gml.geometry.primitives.SurfaceProperty;
import org.citygml4j.model.gml.geometry.primitives.Triangle;
import org.citygml4j.model.gml.geometry.primitives.TrianglePatchArrayProperty;
import org.citygml4j.model.gml.geometry.primitives.TriangulatedSurface;
import org.citygml4j.util.gmlid.DefaultGMLIdManager;
import org.postgis.PGgeometry;
import org.postgis.Polygon;

import de.tub.citydb.api.event.EventDispatcher;
import de.tub.citydb.config.Config;
import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.config.project.CitykmlExporter.Balloon;
import de.tub.citydb.config.project.CitykmlExporter.ColladaOptions;
import de.tub.citydb.config.project.CitykmlExporter.DisplayForm;
import de.tub.citydb.database.TableEnum;
import de.tub.citydb.database.TypeAttributeValueEnum;
import de.tub.citydb.log.Logger;
import de.tub.citydb.modules.citygml.common.database.xlink.DBXlinkBasic;
import de.tub.citydb.modules.citygml.common.database.xlink.DBXlinkTextureParam;
import de.tub.citydb.modules.citygml.common.database.xlink.DBXlinkTextureParamEnum;
import de.tub.citydb.modules.citygml.importer.database.content.DBImporterEnum;
import de.tub.citydb.modules.citygml.importer.database.content.DBSequencerEnum;
import de.tub.citydb.modules.citykml.util.ProjConvertor;
import de.tub.citydb.modules.common.event.CounterEvent;
import de.tub.citydb.modules.common.event.CounterType;
import de.tub.citydb.modules.kml.database.Queries;
import de.tub.citydb.util.Util;

public class Building extends KmlGenericObject{

	public static final String STYLE_BASIS_NAME = ""; // "Building"
	//private List<List<Double>> _pointList = new ArrayList<List<Double>>();
	private List<Map<String, Object>> _ParentSurfaceList = new ArrayList<Map<String,Object>>();

	public Building(Connection connection,
			KmlExporterManager kmlExporterManager,
			CityGMLFactory cityGMLFactory,
			net.opengis.kml._2.ObjectFactory kmlFactory,
			ElevationServiceHandler elevationServiceHandler,
			BalloonTemplateHandlerImpl balloonTemplateHandler,
			EventDispatcher eventDispatcher,
			Config config) {

		super(connection,
				kmlExporterManager,
				cityGMLFactory,
				kmlFactory,
				elevationServiceHandler,
				balloonTemplateHandler,
				eventDispatcher,
				config);
	}

	protected List<DisplayForm> getDisplayForms() {
		return config.getProject().getCityKmlExporter().getBuildingDisplayForms();
	}

	public ColladaOptions getColladaOptions() {
		return config.getProject().getCityKmlExporter().getBuildingColladaOptions();
	}

	public Balloon getBalloonSettings() {
		return config.getProject().getCityKmlExporter().getBuildingBalloon();
	}

	public String getStyleBasisName() {
		return STYLE_BASIS_NAME;
	}

	protected String getHighlightingQuery() {
		return Queries.getBuildingPartHighlightingQuery(currentLod);
	}

	public void read(KmlSplittingResult work) {

		List<PlacemarkType> placemarks = new ArrayList<PlacemarkType>();

		try {

			List<PlacemarkType> placemarkBPart = readBuildingPart(work);
			if (placemarkBPart != null) 
				placemarks.addAll(placemarkBPart);			
		}
		catch (Exception Ex) {
			Logger.getInstance().error("SQL error while getting building parts for building " + work.getGmlId() + ": " + Ex.getMessage());
		}
		finally {

			if (placemarks.size() == 0) {
				int lodToExportFrom = config.getProject().getCityKmlExporter().getLodToExportFrom();
				String fromMessage = " from LoD" + lodToExportFrom;
				if (lodToExportFrom == 5) {
					if (work.getDisplayForm().getForm() == DisplayForm.COLLADA)
						fromMessage = ". LoD1 or higher required";
					else
						fromMessage = " from any LoD";
				}
				Logger.getInstance().info("Could not display object " + work.getGmlId() 
						+ " as " + work.getDisplayForm().getName() + fromMessage + ".");
			}
			else {
				try {
					// compact list before exporting
					for (int i = 0; i < placemarks.size(); i++) {
						PlacemarkType placemark1 = placemarks.get(i);
						if (placemark1 == null) continue;
						MultiGeometryType multiGeometry1 = (MultiGeometryType) placemark1.getAbstractGeometryGroup().getValue();
						for (int j = i+1; j < placemarks.size(); j++) {
							PlacemarkType placemark2 = placemarks.get(j);
							if (placemark2 == null || !placemark1.getId().equals(placemark2.getId())) continue;
							// compact since ids are identical
							MultiGeometryType multiGeometry2 = (MultiGeometryType) placemark2.getAbstractGeometryGroup().getValue();
							multiGeometry1.getAbstractGeometryGroup().addAll(multiGeometry2.getAbstractGeometryGroup());
							placemarks.set(j, null); // polygons transfered, placemark exhausted
						}
					}

					kmlExporterManager.print(placemarks,
							work,
							getBalloonSettings().isBalloonContentInSeparateFile());

					eventDispatcher.triggerEvent(new CounterEvent(CounterType.TOPLEVEL_FEATURE, 1, this));
				}
				catch (JAXBException jaxbEx) {}
			}
		}

	}


	@SuppressWarnings("unchecked")
	private List<PlacemarkType> readBuildingPart(KmlSplittingResult work) throws Exception {

		List<Map<String, Object>> _SurfaceDataList = new ArrayList<Map<String,Object>>();
		boolean reversePointOrder = false;

		try {

			AbstractBuilding _building = (AbstractBuilding)work.getCityGmlClass();
			SurfaceAppearance _SurfaceAppear = new SurfaceAppearance(_building);
			List<Map<String, Object>> _surfaceList = GetBuildingGeometries(_building);

			if (_surfaceList.size()!=0) { // result not empty

				switch (work.getDisplayForm().getForm()) {

				case DisplayForm.FOOTPRINT:
					return createPlacemarksForFootprint(_surfaceList, work);

				case DisplayForm.EXTRUDED:

					double measuredHeight = _building.getMeasuredHeight().getValue();
					return createPlacemarksForExtruded(_surfaceList, work, measuredHeight, reversePointOrder);

				case DisplayForm.GEOMETRY:

					if (work.getDisplayForm().isHighlightingEnabled()) {
						if (config.getProject().getCityKmlExporter().getFilter().isSetComplexFilter()) { // region

							List<PlacemarkType> hlPlacemarks = createPlacemarksForHighlighting(_surfaceList, work);
							hlPlacemarks.addAll(createPlacemarksForGeometry(_surfaceList, work));
							return hlPlacemarks;
						}
						else { // reverse order for single buildings

							List<PlacemarkType> placemarks = createPlacemarksForGeometry(_surfaceList, work);
							placemarks.addAll(createPlacemarksForHighlighting(_surfaceList, work));
							return placemarks;
						}
					}

					return createPlacemarksForGeometry(_surfaceList, work);

				case DisplayForm.COLLADA:

					fillGenericObjectForCollada(work,_surfaceList,_SurfaceAppear,_ParentSurfaceList);
					setGmlId(work.getGmlId());
					setId(work.getId());

					if (getGeometryAmount() > GEOMETRY_AMOUNT_WARNING) {
						Logger.getInstance().info("Object " + work.getGmlId() + " has more than " + GEOMETRY_AMOUNT_WARNING + " geometries. This may take a while to process...");
					}


					//	List<Point3d> lowestPointCandidates = getLowestPointsCoordinates(_surfaceList , work);

					//	double zOffset = getZOffsetFromGEService(lowestPointCandidates,work.getTargetSrs());

					List<Point3d> anchorCandidates = setOrigins(); // setOrigins() called mainly for the side-effect
					double zOffset = getZOffsetFromGEService(anchorCandidates,work.getTargetSrs());

					setZOffset(zOffset);
					//System.out.println(zOffset);
					ColladaOptions colladaOptions = getColladaOptions();
					setIgnoreSurfaceOrientation(colladaOptions.isIgnoreSurfaceOrientation());
					try {

						// just COLLADA, no KML
						List<PlacemarkType> dummy = new ArrayList<PlacemarkType>();
						dummy.add(null);
						return dummy;
					}
					catch (Exception ioe) {
						ioe.printStackTrace();
					}
				}
			}
		}
		catch (SQLException sqlEx) {
			Logger.getInstance().error("SQL error while querying city object " + work.getGmlId() + ": " + sqlEx.getMessage());
			return null;
		}
		finally {

		}

		return null; 
		// nothing found 
	}



	public PlacemarkType createPlacemarkForColladaModel(KmlSplittingResult work) throws Exception {
		// undo trick for very close coordinates
		List<Double> originInWGS84 = ProjConvertor.transformPoint(getOriginX()/100, getOriginY()/100, getOriginZ()/100, work.getTargetSrs(), "4326");

		setLocationX(reducePrecisionForXorY(originInWGS84.get(1)));
		setLocationY(reducePrecisionForXorY(originInWGS84.get(0)));
		setLocationZ(reducePrecisionForZ(originInWGS84.get(2)));

		return super.createPlacemarkForColladaModel(work);
	}

	// overloaded for just one line, but this is safest
	protected List<PlacemarkType> createPlacemarksForHighlighting(List<Map<String, Object>> result, KmlSplittingResult work) throws SQLException {


		int buildingPartId = 1;

		List<PlacemarkType> placemarkList= new ArrayList<PlacemarkType>();

		PlacemarkType placemark = kmlFactory.createPlacemarkType();
		placemark.setStyleUrl("#" + getStyleBasisName() + work.getDisplayForm().getName() + "Style");
		placemark.setName(work.getGmlId());
		placemark.setId(DisplayForm.GEOMETRY_HIGHLIGHTED_PLACEMARK_ID + placemark.getName());
		placemarkList.add(placemark);

		if (getBalloonSettings().isIncludeDescription()) {
			addBalloonContents(placemark, work.getId());
		}

		MultiGeometryType multiGeometry =  kmlFactory.createMultiGeometryType();
		placemark.setAbstractGeometryGroup(kmlFactory.createMultiGeometry(multiGeometry));

		PreparedStatement getGeometriesStmt = null;
		ResultSet rs = null;

		double hlDistance = work.getDisplayForm().getHighlightingDistance();

		try {
			getGeometriesStmt = connection.prepareStatement(getHighlightingQuery(),
					ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_READ_ONLY);

			for (int i = 1; i <= getGeometriesStmt.getParameterMetaData().getParameterCount(); i++) {
				// this is THE LINE
				getGeometriesStmt.setLong(i, buildingPartId);
			}
			rs = getGeometriesStmt.executeQuery();

			double zOffset = getZOffsetFromConfigOrDB(work.getId());
			if (zOffset == Double.MAX_VALUE) {
				List<Point3d> lowestPointCandidates = getLowestPointsCoordinates(result, work);
				rs.beforeFirst(); // return cursor to beginning
				zOffset = getZOffsetFromGEService(lowestPointCandidates , work.getTargetSrs());
			}

			while (rs.next()) {	
				PGgeometry unconverted = (PGgeometry)rs.getObject(1);
				Polygon unconvertedSurface = (Polygon)unconverted.getGeometry();
				double[] ordinatesArray = new double[unconvertedSurface.numPoints()*3];

				for (int i = 0, j = 0; i < unconvertedSurface.numPoints(); i++, j+=3){
					ordinatesArray[j] = unconvertedSurface.getPoint(i).x;
					ordinatesArray[j+1] = unconvertedSurface.getPoint(i).y;
					ordinatesArray[j+2] = unconvertedSurface.getPoint(i).z;
				}		

				int contourCount = unconvertedSurface.numRings();
				// remove normal-irrelevant points
				int startContour1 = 0;
				int endContour1 = (contourCount == 1) ? 
						ordinatesArray.length: // last
							(unconvertedSurface.getRing(startContour1).numPoints()*3); // holes are irrelevant for normal calculation
				// last point of polygons in gml is identical to first and useless for GeometryInfo
				endContour1 = endContour1 - 3;

				double nx = 0;
				double ny = 0;
				double nz = 0;
				int cellCount = 0;

				for (int current = startContour1; current < endContour1; current = current+3) {
					int next = current+3;
					if (next >= endContour1) next = 0;
					nx = nx + ((ordinatesArray[current+1] - ordinatesArray[next+1]) * (ordinatesArray[current+2] + ordinatesArray[next+2])); 
					ny = ny + ((ordinatesArray[current+2] - ordinatesArray[next+2]) * (ordinatesArray[current] + ordinatesArray[next])); 
					nz = nz + ((ordinatesArray[current] - ordinatesArray[next]) * (ordinatesArray[current+1] + ordinatesArray[next+1])); 
				}

				double value = Math.sqrt(nx * nx + ny * ny + nz * nz);
				if (value == 0) { // not a surface, but a line
					continue;
				}
				nx = nx / value;
				ny = ny / value;
				nz = nz / value;

				for (int i = 0, j = 0; i < unconvertedSurface.numPoints(); i++, j+=3){
					unconvertedSurface.getPoint(i).x = ordinatesArray[j] + hlDistance * nx;
					unconvertedSurface.getPoint(i).y = ordinatesArray[j+1] + hlDistance * ny;
					unconvertedSurface.getPoint(i).z = ordinatesArray[j+2] + zOffset + hlDistance * nz;
				}

				// now convert to WGS84
				Polygon surface = (Polygon)convertToWGS84(unconvertedSurface);

				for (int i = 0, j = 0; i < surface.numPoints(); i++, j+=3){
					ordinatesArray[j] = surface.getPoint(i).x;
					ordinatesArray[j+1] = surface.getPoint(i).y;
					ordinatesArray[j+2] = surface.getPoint(i).z;
				}				

				PolygonType polygon = kmlFactory.createPolygonType();
				switch (config.getProject().getCityKmlExporter().getAltitudeMode()) {
				case ABSOLUTE:
					polygon.setAltitudeModeGroup(kmlFactory.createAltitudeMode(AltitudeModeEnumType.ABSOLUTE));
					break;
				case RELATIVE:
					polygon.setAltitudeModeGroup(kmlFactory.createAltitudeMode(AltitudeModeEnumType.RELATIVE_TO_GROUND));
					break;
				}
				multiGeometry.getAbstractGeometryGroup().add(kmlFactory.createPolygon(polygon));

				for (int i = 0; i < surface.numRings(); i++){
					LinearRingType linearRing = kmlFactory.createLinearRingType();
					BoundaryType boundary = kmlFactory.createBoundaryType();
					boundary.setLinearRing(linearRing);
					if (i == 0) { // EXTERIOR_POLYGON_RING
						polygon.setOuterBoundaryIs(boundary);
					}
					else { // INTERIOR_POLYGON_RING
						polygon.getInnerBoundaryIs().add(boundary);
					}

					int startNextRing = ((i+1) < surface.numRings()) ? 
							(surface.getRing(i).numPoints()*3): // still holes to come
								ordinatesArray.length; // default

							// order points clockwise
							for (int j = cellCount; j < startNextRing; j+=3) {
								linearRing.getCoordinates().add(String.valueOf(reducePrecisionForXorY(ordinatesArray[j]) + "," 
										+ reducePrecisionForXorY(ordinatesArray[j+1]) + ","
										+ reducePrecisionForZ(ordinatesArray[j+2])));
							}
							cellCount += (surface.getRing(i).numPoints()*3);
				}
			}
		}
		catch (Exception e) {
			Logger.getInstance().warn("Exception when generating highlighting geometry of object " + work.getGmlId());
			e.printStackTrace();
		}
		finally {
			if (rs != null) rs.close();
			if (getGeometriesStmt != null) getGeometriesStmt.close();
		}

		return placemarkList;
	}



	public List<Map<String, Object>> GetBuildingGeometries(AbstractBuilding _building) throws Exception
	{
		List<Map<String, Object>> _SurfaceList = new ArrayList<Map<String,Object>>();
		SurfaceGeometry _Surface = new SurfaceGeometry();		
		String _SurfaceType = "undefined";



		// lodXSolid
		for (int lod = 1; lod < 5; lod++) {

			SolidProperty solidProperty = null;

			switch (lod) {
			case 1:
				solidProperty = _building.getLod1Solid();
				break;
			case 2:
				solidProperty = _building.getLod2Solid();
				break;
			case 3:
				solidProperty = _building.getLod3Solid();
				break;
			case 4:
				solidProperty = _building.getLod4Solid();
				break;
			}

			if (solidProperty != null) {
				if (solidProperty.isSetSolid()) {

					_Surface.ClearPointList();				
					List<List<Double>> _pointList = _Surface.GetSurfaceGeometry(solidProperty.getSolid(), false);

					for(List<Double> _Geometry : _pointList){

						Map<String, Object> _BuildingSurfaces = new HashMap<String, Object>();   					
						_SurfaceType = _Surface.DetectSurfaceType(_Geometry); 					
						_BuildingSurfaces.put("id", _Surface.GetSurfaceID());
						_BuildingSurfaces.put("type", _SurfaceType);    				
						_BuildingSurfaces.put("Geometry", _Geometry);    					   					
						_SurfaceList.add(_BuildingSurfaces);

					}

				} 
			}

		}



		// lodXMultiSurface
		for (int lod = 1; lod < 5; lod++) {

			//if (lodGeometry[lod - 1])
			//continue;

			MultiSurfaceProperty multiSurfaceProperty = null;


			switch (lod) {
			case 1:
				multiSurfaceProperty = _building.getLod1MultiSurface();
				break;
			case 2:
				multiSurfaceProperty = _building.getLod2MultiSurface();
				break;
			case 3:
				multiSurfaceProperty = _building.getLod3MultiSurface();
				break;
			case 4:
				multiSurfaceProperty = _building.getLod4MultiSurface();
				break;
			}

			if (multiSurfaceProperty != null) {

				if (multiSurfaceProperty.isSetMultiSurface()) {

					_Surface.ClearPointList();				
					List<List<Double>> _pointList = _Surface.GetSurfaceGeometry(multiSurfaceProperty.getMultiSurface(), false);    				

					for(List<Double> _Geometry : _pointList){    					

						Map<String, Object> _BuildingSurfaces = new HashMap<String, Object>();    					
						_SurfaceType = _Surface.DetectSurfaceType(_Geometry);  
						_BuildingSurfaces.put("id", _Surface.GetSurfaceID());
						_BuildingSurfaces.put("type", _SurfaceType);    				
						_BuildingSurfaces.put("Geometry", _Geometry);    					    					
						_SurfaceList.add(_BuildingSurfaces);

					}

				}
			}

		}



		// lodXTerrainIntersectionCurve
		for (int lod = 1; lod < 5; lod++) {

			MultiCurveProperty multiCurveProperty = null;

			switch (lod) {
			case 1:
				multiCurveProperty = _building.getLod1TerrainIntersection();
				break;
			case 2:
				multiCurveProperty = _building.getLod2TerrainIntersection();
				break;
			case 3:
				multiCurveProperty = _building.getLod3TerrainIntersection();
				break;
			case 4:
				multiCurveProperty = _building.getLod4TerrainIntersection();
				break;
			}

			if (multiCurveProperty != null)
			{

				_Surface.ClearPointList();

				List<List<Double>> _pointList  = _Surface.getMultiCurve(multiCurveProperty);


				for(List<Double> _Geometry : _pointList){

					Map<String, Object> _BuildingSurfaces = new HashMap<String, Object>();

					_BuildingSurfaces.put("id", _Surface.GetSurfaceID());

					_BuildingSurfaces.put("type", _SurfaceType);

					_BuildingSurfaces.put("Geometry", _Geometry);    					

					_SurfaceList.add(_BuildingSurfaces);

				}

			}			

		}



		// lodXMultiCurve
		for (int lod = 2; lod < 5; lod++) {

			MultiCurveProperty multiCurveProperty = null;


			switch (lod) {
			case 2:
				multiCurveProperty = _building.getLod2MultiCurve();
				break;
			case 3:
				multiCurveProperty = _building.getLod3MultiCurve();
				break;
			case 4:
				multiCurveProperty = _building.getLod4MultiCurve();
				break;
			}

			if (multiCurveProperty != null)
			{
				_Surface.ClearPointList();

				List<List<Double>> _pointList  = _Surface.getMultiCurve(multiCurveProperty);

				for(List<Double> _Geometry : _pointList){

					Map<String, Object> _BuildingSurfaces = new HashMap<String, Object>();

					_BuildingSurfaces.put("id", _Surface.GetSurfaceID());

					_BuildingSurfaces.put("type", _SurfaceType);

					_BuildingSurfaces.put("Geometry", _Geometry);    					

					_SurfaceList.add(_BuildingSurfaces);

				}

			}

		}


		// BoundarySurfaces
		if (_building.isSetBoundedBySurface()) {

			long ParentCounter = 1;
			for (BoundarySurfaceProperty boundarySurfaceProperty : _building.getBoundedBySurface()) {
				AbstractBoundarySurface boundarySurface = boundarySurfaceProperty.getBoundarySurface();
				Map<String, Object> _BuildingParentSurfaces = new HashMap<String, Object>();
				if (boundarySurface != null) {

					for (int lod = 2; lod < 5; lod++) {

						MultiSurfaceProperty multiSurfaceProperty = null;

						switch (lod) {
						case 2:
							multiSurfaceProperty = boundarySurface.getLod2MultiSurface();
							break;
						case 3:
							multiSurfaceProperty = boundarySurface.getLod3MultiSurface();
							break;
						case 4:
							multiSurfaceProperty = boundarySurface.getLod4MultiSurface();
							break;
						}

						if (multiSurfaceProperty != null) {

							if (multiSurfaceProperty.isSetMultiSurface()) {


								if(multiSurfaceProperty.getMultiSurface().isSetId()){
									_BuildingParentSurfaces.put("pid", ParentCounter);
									String _Pid = multiSurfaceProperty.getMultiSurface().getId();
									_BuildingParentSurfaces.put("id", _Pid);
									_BuildingParentSurfaces.put("Geometry", null);
									_ParentSurfaceList.add(_BuildingParentSurfaces);
								}

								_Surface.ClearPointList();
								_Surface.ClearIdList();
								_SurfaceType = TypeAttributeValueEnum.fromCityGMLClass(boundarySurface.getCityGMLClass()).toString();			    												
								List<List<Double>> _pointList  = _Surface.GetSurfaceGeometry(multiSurfaceProperty.getMultiSurface(), false);

								int counter = 0;
								for(List<Double> _Geometry : _pointList){

									Map<String, Object> _BuildingSurfaces = new HashMap<String, Object>();

									_BuildingSurfaces.put("pid", ParentCounter);
									_BuildingSurfaces.put("id", _Surface.GetSurfaceID().get(counter));
									_BuildingSurfaces.put("type", _SurfaceType);		    				
									_BuildingSurfaces.put("Geometry", _Geometry);    							    					
									_SurfaceList.add(_BuildingSurfaces);
									counter++;

								}
							} 
						}

					}
					
					
					// Room - BoundrySurface - Openings
					if (boundarySurface.isSetOpening()) {
						
						for (OpeningProperty openingProperty : boundarySurface.getOpening()) {
							if (openingProperty.isSetOpening()) {
								AbstractOpening opening = openingProperty.getOpening();						        			

								// Opening - Geometry
								for (int lod = 3; lod < 5; lod++) {

									MultiSurfaceProperty multiSurfaceProperty = null;


									switch (lod) {
									case 3:
										multiSurfaceProperty = opening.getLod3MultiSurface();
										break;
									case 4:
										multiSurfaceProperty = opening.getLod4MultiSurface();
										break;
									}

									if (multiSurfaceProperty != null) {
										if (multiSurfaceProperty.isSetMultiSurface()) {
											
											_Surface.ClearPointList();				
											List<List<Double>> _pointList = _Surface.GetSurfaceGeometry(multiSurfaceProperty.getMultiSurface(), false);
											_SurfaceType = TypeAttributeValueEnum.fromCityGMLClass(CityGMLClass.WALL_SURFACE).toString();

											int counter = 0;
											for(List<Double> _Geometry : _pointList){


												Map<String, Object> _BuildingSurfaces = new HashMap<String, Object>();   					
												_BuildingSurfaces.put("id", _Surface.GetSurfaceID().get(counter));			
												_BuildingSurfaces.put("type", _SurfaceType);    				
												_BuildingSurfaces.put("Geometry", _Geometry);    					   					
												_SurfaceList.add(_BuildingSurfaces);
												counter++;
											}


										}
									}

									// free memory of nested feature
									openingProperty.unsetOpening();
								}
							}
						}

						// free memory of nested feature
						boundarySurfaceProperty.unsetBoundarySurface();
					}
					
				}
				ParentCounter++; 
			}

		}


		// BuildingInstallation
		if (_building.isSetOuterBuildingInstallation()) {
			for (BuildingInstallationProperty buildingInstProperty : _building.getOuterBuildingInstallation()) {
				BuildingInstallation buildingInst = buildingInstProperty.getBuildingInstallation();

				if (buildingInst != null) {


					// Geometry
					for (int lod = 2; lod < 5; lod++) {
						GeometryProperty<? extends AbstractGeometry> geometryProperty = null;


						switch (lod) {
						case 2:
							geometryProperty = buildingInst.getLod2Geometry();
							break;
						case 3:
							geometryProperty = buildingInst.getLod3Geometry();
							break;
						case 4:
							geometryProperty = buildingInst.getLod4Geometry();
							break;
						}

						if (geometryProperty != null) {
							if (geometryProperty.isSetGeometry()) {

								_Surface.ClearPointList();				
								List<List<Double>> _pointList = _Surface.GetSurfaceGeometry(geometryProperty.getGeometry(), false);
								_SurfaceType = TypeAttributeValueEnum.fromCityGMLClass(CityGMLClass.WALL_SURFACE).toString();
								for(List<Double> _Geometry : _pointList){

									Map<String, Object> _BuildingSurfaces = new HashMap<String, Object>();   					
									_BuildingSurfaces.put("id", _Surface.GetSurfaceID());
									_BuildingSurfaces.put("type", _SurfaceType);    				
									_BuildingSurfaces.put("Geometry", _Geometry);    					   					
									_SurfaceList.add(_BuildingSurfaces);

								}
							}
						}

					}

					// free memory of nested feature
					buildingInstProperty.unsetBuildingInstallation();
				} 
			}
		}

		
		// IntBuildingInstallation
		if (_building.isSetInteriorBuildingInstallation()) {
			for (IntBuildingInstallationProperty intBuildingInstProperty : _building.getInteriorBuildingInstallation()) {
				IntBuildingInstallation intBuildingInst = intBuildingInstProperty.getIntBuildingInstallation();

				if (intBuildingInst != null) {


					if (intBuildingInst.isSetLod4Geometry()) {
						GeometryProperty<? extends AbstractGeometry> geometryProperty = intBuildingInst.getLod4Geometry();

						if (geometryProperty.isSetGeometry()) {

							_Surface.ClearPointList();				
							List<List<Double>> _pointList = _Surface.GetSurfaceGeometry(geometryProperty.getGeometry(), false);
							_SurfaceType = TypeAttributeValueEnum.fromCityGMLClass(CityGMLClass.WALL_SURFACE).toString();
							for(List<Double> _Geometry : _pointList){

								Map<String, Object> _BuildingSurfaces = new HashMap<String, Object>();   					
								_BuildingSurfaces.put("id", _Surface.GetSurfaceID());	
								_BuildingSurfaces.put("type", _SurfaceType);    				
								_BuildingSurfaces.put("Geometry", _Geometry);    					   					
								_SurfaceList.add(_BuildingSurfaces);

							}
						}
					}


					// free memory of nested feature
					intBuildingInstProperty.unsetIntBuildingInstallation();
				}
			}
		}

		
		int tmpcounter = 0;
		// Room
		if (_building.isSetInteriorRoom()) {
			for (InteriorRoomProperty roomProperty : _building.getInteriorRoom()) {
				Room room = roomProperty.getRoom();
				
				if (room != null) {

					if (room.isSetLod4MultiSurface() && room.isSetLod4Solid()) {

						StringBuilder msg = new StringBuilder();					
						msg.append("Found both elements lod4Solid and lod4MultiSurface. Only lod4Solid will be imported.");
						Logger.getInstance().warn(msg.toString());

						room.unsetLod4MultiSurface();
					}



					if (room.isSetLod4Solid()) {

						SolidProperty solidProperty = room.getLod4Solid();

						if (solidProperty.isSetSolid()) {

							_Surface.ClearPointList();				
							List<List<Double>> _pointList = _Surface.GetSurfaceGeometry(solidProperty.getSolid(), false);
							_SurfaceType = TypeAttributeValueEnum.fromCityGMLClass(CityGMLClass.WALL_SURFACE).toString();
							for(List<Double> _Geometry : _pointList){

								Map<String, Object> _BuildingSurfaces = new HashMap<String, Object>();   					
								_BuildingSurfaces.put("id", _Surface.GetSurfaceID());
								_BuildingSurfaces.put("type", _SurfaceType);    				
								_BuildingSurfaces.put("Geometry", _Geometry);    					   					
								_SurfaceList.add(_BuildingSurfaces);

							}
						} 

					} else if (room.isSetLod4MultiSurface()) {

						MultiSurfaceProperty multiSurfacePropery = room.getLod4MultiSurface();

						if (multiSurfacePropery.isSetMultiSurface()) {

							_Surface.ClearPointList();				
							List<List<Double>> _pointList = _Surface.GetSurfaceGeometry(multiSurfacePropery.getMultiSurface(), false);
							_SurfaceType = TypeAttributeValueEnum.fromCityGMLClass(CityGMLClass.WALL_SURFACE).toString();
							for(List<Double> _Geometry : _pointList){

								Map<String, Object> _BuildingSurfaces = new HashMap<String, Object>();   					
								_BuildingSurfaces.put("id", _Surface.GetSurfaceID());		
								_BuildingSurfaces.put("type", _SurfaceType);    				
								_BuildingSurfaces.put("Geometry", _Geometry);    					   					
								_SurfaceList.add(_BuildingSurfaces);

							}
						}
					} 


					// Room - BoundarySurfaces
					if (room.isSetBoundedBySurface()) {
						for (BoundarySurfaceProperty boundarySurfaceProperty : room.getBoundedBySurface()) {
							AbstractBoundarySurface boundarySurface = boundarySurfaceProperty.getBoundarySurface();

							if (boundarySurface != null) {

								for (int lod = 2; lod < 5; lod++) {
									MultiSurfaceProperty multiSurfaceProperty = null;


									switch (lod) {
									case 2:
										multiSurfaceProperty = boundarySurface.getLod2MultiSurface();
										break;
									case 3:
										multiSurfaceProperty = boundarySurface.getLod3MultiSurface();
										break;
									case 4:
										multiSurfaceProperty = boundarySurface.getLod4MultiSurface();
										break;
									}

									if (multiSurfaceProperty != null) {
										if (multiSurfaceProperty.isSetMultiSurface()) {

											_Surface.ClearPointList();
											_SurfaceType = TypeAttributeValueEnum.fromCityGMLClass(boundarySurface.getCityGMLClass()).toString();			    				
											List<List<Double>> _pointList  = _Surface.GetSurfaceGeometry(multiSurfaceProperty.getMultiSurface(), false);

											for(List<Double> _Geometry : _pointList){

												Map<String, Object> _BuildingSurfaces = new HashMap<String, Object>();		    					
												_BuildingSurfaces.put("id", _Surface.GetSurfaceID());
												_BuildingSurfaces.put("type", _SurfaceType);		    				
												_BuildingSurfaces.put("Geometry", _Geometry);    							    					
												_SurfaceList.add(_BuildingSurfaces);	    					

											}

										} 
									}

								}

								// Room - BoundrySurface - Openings
								if (boundarySurface.isSetOpening()) {
									
									for (OpeningProperty openingProperty : boundarySurface.getOpening()) {
										if (openingProperty.isSetOpening()) {
											AbstractOpening opening = openingProperty.getOpening();						        			

											// Opening - Geometry
											for (int lod = 3; lod < 5; lod++) {

												MultiSurfaceProperty multiSurfaceProperty = null;


												switch (lod) {
												case 3:
													multiSurfaceProperty = opening.getLod3MultiSurface();
													break;
												case 4:
													multiSurfaceProperty = opening.getLod4MultiSurface();
													break;
												}

												if (multiSurfaceProperty != null) {
													if (multiSurfaceProperty.isSetMultiSurface()) {
														
														_Surface.ClearPointList();				
														List<List<Double>> _pointList = _Surface.GetSurfaceGeometry(multiSurfaceProperty.getMultiSurface(), false);
														_SurfaceType = TypeAttributeValueEnum.fromCityGMLClass(CityGMLClass.WALL_SURFACE).toString();

														int counter = 0;
														for(List<Double> _Geometry : _pointList){



															Map<String, Object> _BuildingSurfaces = new HashMap<String, Object>();   					
															_BuildingSurfaces.put("id", _Surface.GetSurfaceID().get(counter));			
															_BuildingSurfaces.put("type", _SurfaceType);    				
															_BuildingSurfaces.put("Geometry", _Geometry);    					   					
															_SurfaceList.add(_BuildingSurfaces);
															counter++;
														}


													}
												}

												// free memory of nested feature
												openingProperty.unsetOpening();
											}
										}
									}

									// free memory of nested feature
									boundarySurfaceProperty.unsetBoundarySurface();
								}
							}
						}


						// Room - IntBuildingInstallation
						if (room.isSetRoomInstallation()) {
							for (IntBuildingInstallationProperty intBuildingInstProperty : room.getRoomInstallation()) {
								IntBuildingInstallation intBuildingInst = intBuildingInstProperty.getObject();

								if (intBuildingInst != null) {

									if (intBuildingInst.isSetLod4Geometry()) {
										GeometryProperty<? extends AbstractGeometry> geometryProperty = intBuildingInst.getLod4Geometry();

										if (geometryProperty.isSetGeometry()) {

											_Surface.ClearPointList();				
											List<List<Double>> _pointList = _Surface.GetSurfaceGeometry(geometryProperty.getGeometry(), false);
											_SurfaceType = TypeAttributeValueEnum.fromCityGMLClass(CityGMLClass.WALL_SURFACE).toString();

											for(List<Double> _Geometry : _pointList){

												Map<String, Object> _BuildingSurfaces = new HashMap<String, Object>();   					
												_BuildingSurfaces.put("id", _Surface.GetSurfaceID());			
												_BuildingSurfaces.put("type", _SurfaceType);    				
												_BuildingSurfaces.put("Geometry", _Geometry);    					   					
												_SurfaceList.add(_BuildingSurfaces);

											}
										}
									}


									// free memory of nested feature
									intBuildingInstProperty.unsetIntBuildingInstallation();
								}
							}
						}



						// Room - BuildingFurniture
						if (room.isSetInteriorFurniture()) {
							for (InteriorFurnitureProperty intFurnitureProperty : room.getInteriorFurniture()) {
								BuildingFurniture furniture = intFurnitureProperty.getObject();

								if (furniture != null) {


									if (furniture.isSetLod4Geometry()) {
										GeometryProperty<? extends AbstractGeometry> geometryProperty = furniture.getLod4Geometry();

										if (geometryProperty.isSetGeometry()) {

											_Surface.ClearPointList();				
											List<List<Double>> _pointList = _Surface.GetSurfaceGeometry(geometryProperty.getGeometry(), false);
											_SurfaceType = TypeAttributeValueEnum.fromCityGMLClass(CityGMLClass.WALL_SURFACE).toString();

											for(List<Double> _Geometry : _pointList){

												Map<String, Object> _BuildingSurfaces = new HashMap<String, Object>();   					
												_BuildingSurfaces.put("id", _Surface.GetSurfaceID());
												_BuildingSurfaces.put("type", _SurfaceType);    				
												_BuildingSurfaces.put("Geometry", _Geometry);    					   					
												_SurfaceList.add(_BuildingSurfaces);

											}
										}
									}


									// free memory of nested feature
									intFurnitureProperty.unsetBuildingFurniture();
								} 
							}
						}


						// free memory of nested feature
						roomProperty.unsetRoom();
					}
				}
			}

		}



		// BuildingPart
		if (_building.isSetConsistsOfBuildingPart()) {
			for (BuildingPartProperty buildingPartProperty : _building.getConsistsOfBuildingPart()) {
				BuildingPart buildingPart = buildingPartProperty.getBuildingPart();

				if (buildingPart != null) {

					GetBuildingGeometries(buildingPart);


					// free memory of nested feature
					buildingPartProperty.unsetBuildingPart();
				}
			}
		}


		return _SurfaceList;

	}



}
