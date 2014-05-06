package de.tub.citydb.modules.citykml.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.model.citygml.building.AbstractBoundarySurface;
import org.citygml4j.model.gml.GMLClass;
import org.citygml4j.model.gml.geometry.AbstractGeometry;
import org.citygml4j.model.gml.geometry.aggregates.MultiSurface;
import org.citygml4j.model.gml.geometry.aggregates.MultiSurfaceProperty;
import org.citygml4j.model.gml.geometry.primitives.AbstractRing;
import org.citygml4j.model.gml.geometry.primitives.AbstractSurface;
import org.citygml4j.model.gml.geometry.primitives.SurfaceArrayProperty;
import org.citygml4j.model.gml.geometry.primitives.SurfaceProperty;

import de.micromata.opengis.kml.v_2_2_0.AltitudeMode;
import de.micromata.opengis.kml.v_2_2_0.Coordinate;
import de.micromata.opengis.kml.v_2_2_0.Document;
import de.micromata.opengis.kml.v_2_2_0.Kml;
import de.micromata.opengis.kml.v_2_2_0.LinearRing;
import de.micromata.opengis.kml.v_2_2_0.Placemark;
import de.micromata.opengis.kml.v_2_2_0.Polygon;
import de.micromata.opengis.kml.v_2_2_0.Style;
import de.tub.citydb.database.TableEnum;
import de.tub.citydb.database.TypeAttributeValueEnum;
import de.tub.citydb.modules.citygml.common.database.xlink.DBXlinkBasic;
import de.tub.citydb.modules.citygml.common.database.xlink.DBXlinkSurfaceGeometry;
import de.tub.citydb.modules.citykml.content.DBSequencerEnum;
import de.tub.citydb.modules.citykml.content1.Building;
import de.tub.citydb.modules.citykml.util.*;

public class KMLObject {
	
	private static KMLObject _kmlObject;
	private List<List<Double>> pointList = new ArrayList<List<Double>>();

	public KMLObject () {

		
	}
	
	
	public static void setInstance() {
	    if (_kmlObject == null) {
	    	_kmlObject = new KMLObject();
	    }
	 }
	
	public static synchronized KMLObject getInstance() {
	    if (_kmlObject == null) {
	    	_kmlObject = new KMLObject();
	    }
	    return _kmlObject;
	 }
	
	public synchronized List<List<Double>> getKMLInstance() {
	    if (pointList == null) {
	    	
	    }
	    return pointList;
	 }
	
	public synchronized void setKMLInstance(List<List<Double>> _pointList) {
		if (pointList == null) {
	    	pointList=_pointList;
	    }
	 }
	
	
	public void ReadBuilding(List<de.tub.citydb.modules.citykml.content1.Building> _Building,String _TargeFile,String _SourceSrs)		
	{	
		List<List<Double>> _pointList = new ArrayList<List<Double>>();
		List<String> _SurfeceType = new ArrayList<String>();
		for(Building _building : _Building)
		{
		
			for(AbstractBoundarySurface boundarySurface : _building.GetGmlBoundrySurface())
			{
				for (int lod = 2; lod < 5; lod++) {
		        	MultiSurfaceProperty multiSurfaceProperty = null;
		        	long multiSurfaceId = 0;

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
		    			if (multiSurfaceProperty.isSetMultiSurface()){
		    				AbstractGeometry _Geom = multiSurfaceProperty.getMultiSurface();
		    				GMLClass surfaceGeometryType = _Geom.getGMLClass();
		    				
		    				if (surfaceGeometryType == GMLClass.MULTI_SURFACE) {
		    					MultiSurface multiSurface = (MultiSurface)_Geom;


		    					// get surfaceMember
		    					if (multiSurface.isSetSurfaceMember()) {
		    						for (SurfaceProperty surfaceProperty : multiSurface.getSurfaceMember()) {
		    							if (surfaceProperty.isSetSurface()) {

		    								AbstractSurface _abstractSurface = surfaceProperty.getSurface();

		    								org.citygml4j.model.gml.geometry.primitives.Polygon polygon =
		    										(org.citygml4j.model.gml.geometry.primitives.Polygon)_abstractSurface;


		    								
		    								if (polygon.isSetExterior()) {

		    									AbstractRing exteriorAbstractRing = polygon.getExterior().getRing();
		    									if (exteriorAbstractRing instanceof org.citygml4j.model.gml.geometry.primitives.LinearRing) {
		    										org.citygml4j.model.gml.geometry.primitives.LinearRing exteriorLinearRing =
		    												(org.citygml4j.model.gml.geometry.primitives.LinearRing)exteriorAbstractRing;
		    										List<Double> points = exteriorLinearRing.toList3d(false);

		    										if (points != null && !points.isEmpty()) {
		    											Double x = points.get(0);
		    											Double y = points.get(1);
		    											Double z = points.get(2);
		    											int nrOfPoints = points.size();

		    											int nrOfCoordinates = points.size() / 3;

		    											if (!x.equals(points.get(nrOfPoints - 3)) ||
		    													!y.equals(points.get(nrOfPoints - 2)) ||
		    													!z.equals(points.get(nrOfPoints - 1))) {


		    												points.add(x);
		    												points.add(y);
		    												points.add(z);
		    												++nrOfCoordinates;
		    											}					

		    											if (nrOfCoordinates < 4) {
		    												
		    											}
		    											else {
		    												
		    												
		    												pointList.add(points);
		    												String _type = TypeAttributeValueEnum.fromCityGMLClass(boundarySurface.getCityGMLClass()).toString();
		    												_SurfeceType.add(_type);	
				    									
		    											}


		    											if (polygon.isSetInterior()) {

		    											}


		    										}

		    									} else {

		    									}
		    								}

		    							}
		    						}
		    					}

		    					// get surfaceMembers
		    					if (multiSurface.isSetSurfaceMembers()) {
		    						SurfaceArrayProperty surfaceArrayProperty = multiSurface.getSurfaceMembers();

		    						if (surfaceArrayProperty.isSetSurface()) {
		    							for (AbstractSurface _abstractSurface2 : surfaceArrayProperty.getSurface()) {
		    								org.citygml4j.model.gml.geometry.primitives.Polygon polygon =
		    										(org.citygml4j.model.gml.geometry.primitives.Polygon)_abstractSurface2;


		    								
		    								if (polygon.isSetExterior()) {

		    									AbstractRing exteriorAbstractRing = polygon.getExterior().getRing();
		    									if (exteriorAbstractRing instanceof org.citygml4j.model.gml.geometry.primitives.LinearRing) {
		    										org.citygml4j.model.gml.geometry.primitives.LinearRing exteriorLinearRing =
		    												(org.citygml4j.model.gml.geometry.primitives.LinearRing)exteriorAbstractRing;
		    										List<Double> points = exteriorLinearRing.toList3d(false);

		    										if (points != null && !points.isEmpty()) {
		    											Double x = points.get(0);
		    											Double y = points.get(1);
		    											Double z = points.get(2);
		    											int nrOfPoints = points.size();

		    											int nrOfCoordinates = points.size() / 3;

		    											if (!x.equals(points.get(nrOfPoints - 3)) ||
		    													!y.equals(points.get(nrOfPoints - 2)) ||
		    													!z.equals(points.get(nrOfPoints - 1))) {


		    												points.add(x);
		    												points.add(y);
		    												points.add(z);
		    												++nrOfCoordinates;
		    											}					

		    											if (nrOfCoordinates < 4) {
		    												
		    											}
		    											else {
		    												
		    												
		    												
		    												pointList.add(points);
		    												String _type=TypeAttributeValueEnum.fromCityGMLClass(boundarySurface.getCityGMLClass()).toString();
		    												_SurfeceType.add(_type);	
				    										
			    										}


		    											if (polygon.isSetInterior()) {

		    											}


		    										}

		    									} else {

		    									}
		    								}
		    								
		    							}
		    						}
		    					}
		    				}
		    				
		    				
		    			}
		    		}
				}
			}
		}
		
		
		WriteGmlToKml(pointList,_TargeFile,_SourceSrs);
	
	}
	
	public static void WriteGmlToKml(List<List<Double>> pointList,String _TargeFile,String _SourceSrs)
	{
		//LOG.info("Shahin Sharifi");

		File f = new File(_TargeFile);

		if(!f.exists()){

			final Kml kml = new Kml();

			Document _doc = kml.createAndSetDocument();

			int _index=0;
			System.out.println("KML generating, Please wait...");
			for (List<Double> coordinate : pointList) {

				List<Double> Target_Coordinates;
				try {

					Placemark _Placemark = _doc.createAndAddPlacemark();
					
					
					String _SurfaceType = DetectType(coordinate);
					_Placemark.setStyleUrl(_SurfaceType);

					/*					Polygon _polygon = _Placemark
							.withName(Util.gmlName2dbString(building)[0]).withOpen(Boolean.FALSE)
							.createAndSetMultiGeometry().createAndAddPolygon();*/
					Polygon _polygon = _Placemark
							.withName("SampleBuilding").withOpen(Boolean.FALSE)
							.createAndSetMultiGeometry().createAndAddPolygon();


					_polygon.setAltitudeMode(AltitudeMode.ABSOLUTE);

					LinearRing _Ring = _polygon.createAndSetOuterBoundaryIs().createAndSetLinearRing();

					List<Coordinate> _coordinates = _Ring.createAndSetCoordinates();

					for (int i = 1; i < coordinate.size(); i = i+3) {				

						Target_Coordinates = ProjConvertor.TransformProjection(coordinate.get(i-1),coordinate.get(i),coordinate.get(i+1),_SourceSrs,"4326");

						_coordinates.add(new Coordinate(Target_Coordinates.get(1),Target_Coordinates.get(0),Target_Coordinates.get(2)));

					}

				} catch (Exception e) {

					System.out.println(e.toString());
				}

				_index++;
			}




			Style _Wallstyle = _doc.createAndAddStyle();
			_Wallstyle.setId("WallSurface");
			_Wallstyle.createAndSetLineStyle().setColor("C8666666");
			_Wallstyle.createAndSetPolyStyle().setColor("C8CCCCCC");
			_Wallstyle.createAndSetBalloonStyle().setText("$[description]");
			

			Style _Roofstyle = _doc.createAndAddStyle();
			_Roofstyle.setId("RoofSurface");
			_Roofstyle.createAndSetLineStyle().setColor("C8000099");
			_Roofstyle.createAndSetPolyStyle().setColor("C83333FF");
			_Roofstyle.createAndSetBalloonStyle().setText("$[description]");


			try {

				kml.marshal(new File(_TargeFile));

			} catch (FileNotFoundException e) {

				System.out.println("Error");
			}

		}else{



		}

	}
	
	public static String DetectType(List<Double> _pointList){
		
		
		List<Double> _TestList = new ArrayList<Double>();
		for (int i=0; i<_pointList.size()-1;i=i+3) {
			
			_TestList.add(_pointList.get(i+2));
		}
		if(_TestList.get(1).intValue() == _TestList.get(2).intValue() && _TestList.get(1).intValue()== _TestList.get(3).intValue())
			return "RoofSurface";//roof
		else {
			return "WallSurface";//wall
		}
	}	

}
