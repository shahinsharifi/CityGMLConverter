package de.tub.citydb.modules.citykml.content2;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.citygml4j.model.citygml.building.AbstractBoundarySurface;
import org.citygml4j.model.citygml.building.AbstractBuilding;
import org.citygml4j.model.citygml.building.BoundarySurfaceProperty;
import org.citygml4j.model.citygml.texturedsurface._TexturedSurface;
import org.citygml4j.model.gml.GMLClass;
import org.citygml4j.model.gml.geometry.AbstractGeometry;
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
import org.citygml4j.model.gml.geometry.primitives.Polygon;
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






import de.tub.citydb.database.TypeAttributeValueEnum;
import de.tub.citydb.log.Logger;
import de.tub.citydb.modules.citykml.util.KMLObject;
import de.tub.citydb.util.Util;

public class SurfaceGeometry {
	
	private List<List<Double>> _pointList = new ArrayList<List<Double>>();
	private final Logger LOG = Logger.getInstance();
	List<HashMap<String, Object>> _SurfaceList = new ArrayList<HashMap<String,Object>>();
	
	
	public SurfaceGeometry() {
			
	
	}
	
	
	public List<HashMap<String, Object>> GetAbstractGeometry(AbstractBuilding _building) throws SQLException
	{
	
		HashMap<String, Object> _BuildingSurfaces = new HashMap<String, Object>();
		
		String _SurfaceType = "undefined";
		
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
					
					_pointList.clear();
    				_SurfaceList.clear();
    				_BuildingSurfaces.clear();
					
    				GetSurfaceGeometry(solidProperty.getSolid(), false);
    				
    				for(List<Double> _Geometry : _pointList){
    					
    					_BuildingSurfaces.put("type", _SurfaceType);
    					_BuildingSurfaces.put("Geometry", _Geometry);
    					_SurfaceList.add(_BuildingSurfaces);
    					
    				//	_kml.WriteGmlToKml(_Geometry, _SurfaceType);
    				
    				}
				
				} else {
					
				}
			}

			
		}
		
		// BoundarySurfacesOfBuilding
		if (_building.isSetBoundedBySurface()) {
			
			
			for (BoundarySurfaceProperty boundarySurfaceProperty : _building.getBoundedBySurface()) {
				AbstractBoundarySurface boundarySurface = boundarySurfaceProperty.getBoundarySurface();
				
				if (boundarySurface != null) {
					
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
			    			
			    			if (multiSurfaceProperty.isSetMultiSurface()) {
			    				
			    				_pointList.clear();
			    				
			    				
			    				_SurfaceType = TypeAttributeValueEnum.fromCityGMLClass(boundarySurface.getCityGMLClass()).toString();
			    				
			    				GetSurfaceGeometry(multiSurfaceProperty.getMultiSurface(), false);
			    				
			    				for(List<Double> _Geometry : _pointList){
			    					
			    					System.out.println( _building.getId() + "(" + _SurfaceType + "):" + _Geometry);
			    					
			    					
			    				//	_BuildingSurfaces.clear();
			    					
			    					/*_BuildingSurfaces.put("type", _SurfaceType);
			    					_BuildingSurfaces.put("Geometry", _Geometry);
			    					_SurfaceList.add(_BuildingSurfaces);
			    					*///_kml.WriteGmlToKml(_Geometry, _SurfaceType);
			    				
			    				}
			    			} 
			    		}

			        }
				} else {
					// xlink
					String href = boundarySurfaceProperty.getHref();

					if (href != null && href.length() != 0) {
						LOG.error("XLink reference '" + href + "' to BoundarySurface feature is not supported.");
					}
				}
			}
			
		}
		return _SurfaceList;
		
	}
	
	
	public void GetSurfaceGeometry(AbstractGeometry surfaceGeometry,boolean reverse) throws SQLException {

		
		GMLClass surfaceGeometryType = surfaceGeometry.getGMLClass();
		

		// gml:id handling
		String origGmlId, gmlId;
		origGmlId = gmlId = surfaceGeometry.getId();


		// ok, now we can have a look at different gml geometry objects
		// firstly, handle simple surface geometries
		// a single linearRing
		if (surfaceGeometryType == GMLClass.LINEAR_RING) {


			LinearRing linearRing = (LinearRing)surfaceGeometry;
			List<Double> points = linearRing.toList3d(reverse);

			if (points != null && !points.isEmpty()) {
				Double x = points.get(0);
				Double y = points.get(1);
				Double z = points.get(2);
				int nrOfPoints = points.size();
				int nrOfCoordinates = points.size() / 3;

				if (!x.equals(points.get(nrOfPoints - 3)) ||
						!y.equals(points.get(nrOfPoints - 2)) ||
						!z.equals(points.get(nrOfPoints - 1))) {
					// repair unclosed ring because geometryAPI fails to do its job...
					StringBuilder msg = new StringBuilder(Util.getGeometrySignature(
							linearRing.getGMLClass(),
							origGmlId));
					msg.append(": Ring is not closed. Appending first coordinate to fix it.");
					LOG.warn(msg.toString());

					points.add(x);
					points.add(y);
					points.add(z);
					++nrOfCoordinates;
				}

				if (nrOfCoordinates < 4) {
					// invalid ring...
					StringBuilder msg = new StringBuilder(Util.getGeometrySignature(
							linearRing.getGMLClass(),
							origGmlId));
					msg.append(": Ring contains less than 4 coordinates. Skipping invalid ring.");
					LOG.error(msg.toString());
					return;
				}

			}	

		}
		// A simple polygon
		else if (surfaceGeometryType == GMLClass.POLYGON) {
			
			Polygon polygon = (Polygon)surfaceGeometry;
			if (polygon.isSetExterior()) {
				List<List<Double>> pointList = new ArrayList<List<Double>>();
				AbstractRing exteriorAbstractRing = polygon.getExterior().getRing();
				if (exteriorAbstractRing instanceof LinearRing) {
					LinearRing exteriorLinearRing = (LinearRing)exteriorAbstractRing;
					List<Double> points = exteriorLinearRing.toList3d(reverse);

					if (points != null && !points.isEmpty()) {
						Double x = points.get(0);
						Double y = points.get(1);
						Double z = points.get(2);
						int nrOfPoints = points.size();
						int nrOfCoordinates = points.size() / 3;

						if (!x.equals(points.get(nrOfPoints - 3)) ||
								!y.equals(points.get(nrOfPoints - 2)) ||
								!z.equals(points.get(nrOfPoints - 1))) {
							// repair unclosed ring because geometryAPI fails to do its job...
							StringBuilder msg = new StringBuilder(Util.getGeometrySignature(
									exteriorLinearRing.getGMLClass(),
									origGmlId));
							msg.append(": Exterior ring is not closed. Appending first coordinate to fix it.");
							LOG.warn(msg.toString());

							points.add(x);
							points.add(y);
							points.add(z);
							++nrOfCoordinates;
						}	

						if (nrOfCoordinates < 4) {
							// invalid ring...
							StringBuilder msg = new StringBuilder(Util.getGeometrySignature(
									exteriorLinearRing.getGMLClass(),
									origGmlId));
							msg.append(": Exterior ring contains less than 4 coordinates. Skipping invalid ring.");
							LOG.error(msg.toString());
							return;
						}

						
						pointList.add(points);
						int ringNo = 0;

						// well, taking care about geometry is not enough... this ring could
						// be referenced by a <textureCoordinates> element. since we cannot store
						// the gml:id of linear rings in the database, we have to remember its id


						if (polygon.isSetInterior()) {
							for (AbstractRingProperty abstractRingProperty : polygon.getInterior()) {
								AbstractRing interiorAbstractRing = abstractRingProperty.getRing();
								if (interiorAbstractRing instanceof LinearRing) {	
									LinearRing interiorLinearRing = (LinearRing)interiorAbstractRing;
									List<Double> interiorPoints = interiorLinearRing.toList3d(reverse);

									if (interiorPoints != null && !interiorPoints.isEmpty()) {	
										x = interiorPoints.get(0);
										y = interiorPoints.get(1);
										z = interiorPoints.get(2);
										nrOfPoints = interiorPoints.size();
										nrOfCoordinates = interiorPoints.size() / 3;

										if (!x.equals(interiorPoints.get(nrOfPoints - 3)) ||
												!y.equals(interiorPoints.get(nrOfPoints - 2)) ||
												!z.equals(interiorPoints.get(nrOfPoints - 1))) {
											// repair unclosed ring because sdoapi fails to do its job...
											StringBuilder msg = new StringBuilder(Util.getGeometrySignature(
													interiorLinearRing.getGMLClass(),
													origGmlId));
											msg.append(": Interior ring is not closed. Appending first coordinate to fix it.");
											LOG.warn(msg.toString());

											interiorPoints.add(x);
											interiorPoints.add(y);
											interiorPoints.add(z);
											++nrOfCoordinates;
										}	

										if (nrOfCoordinates < 4) {
											// invalid ring...
											StringBuilder msg = new StringBuilder(Util.getGeometrySignature(
													interiorLinearRing.getGMLClass(),
													origGmlId));
											msg.append(": Interior ring contains less than 4 coordinates. Skipping invalid ring.");
											LOG.error(msg.toString());
											return;
										}

										pointList.add(interiorPoints);
										ringNo++;


										// also remember the gml:id of interior rings in case it is
										// referenced by a <textureCoordinates> element

									}
								} else {
									// invalid ring...
									StringBuilder msg = new StringBuilder(Util.getGeometrySignature(
											interiorAbstractRing.getGMLClass(),
											origGmlId));
									msg.append(": Only gml:LinearRing elements are supported as interior rings.");
									LOG.error(msg.toString());
									return;
								}
							}


						}
						
						_pointList.addAll(pointList);


					}
				} else {
					// invalid ring...
					StringBuilder msg = new StringBuilder(Util.getGeometrySignature(
							exteriorAbstractRing.getGMLClass(),
							origGmlId));
					msg.append(": Only gml:LinearRing elements are supported as exterior rings.");
					LOG.error(msg.toString());
					return;
				}
			}
		}

		// ok, handle complexes, composites and aggregates
		// orientableSurface
		else if (surfaceGeometryType == GMLClass.ORIENTABLE_SURFACE) {
			OrientableSurface orientableSurface = (OrientableSurface)surfaceGeometry;

			boolean negativeOrientation = false;
			if (orientableSurface.isSetOrientation() && orientableSurface.getOrientation().equals("-")) {
				reverse = !reverse;
				negativeOrientation = true;
			}

			if (orientableSurface.isSetBaseSurface()) {
				SurfaceProperty surfaceProperty = orientableSurface.getBaseSurface();
				String mapping = null;

				if (surfaceProperty.isSetSurface()) {
					AbstractSurface abstractSurface = surfaceProperty.getSurface();
					if (!abstractSurface.isSetId())
						abstractSurface.setId(DefaultGMLIdManager.getInstance().generateUUID());

					// mapping target
					mapping = abstractSurface.getId();

					switch (abstractSurface.getGMLClass()) {
					case POLYGON:
					case _TEXTURED_SURFACE:
					case ORIENTABLE_SURFACE:
						GetSurfaceGeometry(abstractSurface,reverse);
						break;
					case COMPOSITE_SURFACE:
					case SURFACE:
					case TRIANGULATED_SURFACE:
					case TIN:
						GetSurfaceGeometry(abstractSurface,reverse);
						break;
					}

				} else {
					// xlink
					String href = surfaceProperty.getHref();

					if (href != null && href.length() != 0) {

					}

					mapping = href.replaceAll("^#", "");
				}

				// do mapping
				//if (origGmlId != null && !isCopy)
				//dbImporterManager.putGmlId(origGmlId, -1, -1, negativeOrientation, mapping, CityGMLClass.ABSTRACT_GML_GEOMETRY);
			}
		}

		// texturedSurface
		// this is a CityGML class, not a GML class.
		else if (surfaceGeometryType == GMLClass._TEXTURED_SURFACE) {
			_TexturedSurface texturedSurface = (_TexturedSurface)surfaceGeometry;
			AbstractSurface abstractSurface = null;

			boolean negativeOrientation = false;
			if (texturedSurface.isSetOrientation() && texturedSurface.getOrientation().equals("-")) {
				reverse = !reverse;
				negativeOrientation = true;
			}

			String targetURI = null;

			if (texturedSurface.isSetBaseSurface()) {
				SurfaceProperty surfaceProperty = texturedSurface.getBaseSurface();
				if (surfaceProperty.isSetSurface()) {
					abstractSurface = surfaceProperty.getSurface();

					if (!abstractSurface.isSetId())
						abstractSurface.setId(DefaultGMLIdManager.getInstance().generateUUID());

					// appearance and mapping target
					targetURI = abstractSurface.getId();

					// do mapping
					//if (origGmlId != null && !isCopy)
					//dbImporterManager.putGmlId(origGmlId, -1, -1, negativeOrientation, targetURI, CityGMLClass.ABSTRACT_GML_GEOMETRY);

					switch (abstractSurface.getGMLClass()) {
					case POLYGON:
						Polygon polygon = (Polygon)abstractSurface;

						// make sure all exterior and interior rings do have a gml:id
						// in order to assign texture coordinates
						if (polygon.isSetExterior()) {
							LinearRing exteriorRing = (LinearRing)polygon.getExterior().getRing();
							if (exteriorRing != null && !exteriorRing.isSetId())
								exteriorRing.setId(targetURI);
						}

						if (polygon.isSetInterior()) {
							for (AbstractRingProperty abstractRingProperty : polygon.getInterior()) {
								LinearRing interiorRing = (LinearRing)abstractRingProperty.getRing();

								if (!interiorRing.isSetId())
									interiorRing.setId(DefaultGMLIdManager.getInstance().generateUUID());
							}
						}
					case _TEXTURED_SURFACE:
					case ORIENTABLE_SURFACE:
						GetSurfaceGeometry(abstractSurface,reverse);
						break;
					case COMPOSITE_SURFACE:
					case SURFACE:
					case TRIANGULATED_SURFACE:
					case TIN:
						GetSurfaceGeometry(abstractSurface,reverse);
						break;
					}

				} else {
					// xlink
					String href = surfaceProperty.getHref();

					if (href != null && href.length() != 0) {


						targetURI = href.replaceAll("^#", "");

						// do mapping
						//if (origGmlId != null && !isCopy)
						//dbImporterManager.putGmlId(origGmlId, -1, -1, negativeOrientation, targetURI, CityGMLClass.ABSTRACT_GML_GEOMETRY);

						// well, regarding appearances we cannot work on remote geometries so far...
						StringBuilder msg = new StringBuilder(Util.getGeometrySignature(
								texturedSurface.getGMLClass(),
								origGmlId));
						msg.append(": Texture information for referenced geometry objects are not supported.");

						LOG.error(msg.toString());
					}

					return;
				}
			} else {
				// we cannot continue without having a base surface...
				StringBuilder msg = new StringBuilder(Util.getGeometrySignature(
						texturedSurface.getGMLClass(),
						origGmlId));
				msg.append(": Could not find <baseSurface> element.");

				LOG.error(msg.toString());	
				return;
			}

			/*if (importAppearance && !isCopy && texturedSurface.isSetAppearance()) {
		for (_AppearanceProperty appearanceProperty : texturedSurface.getAppearance()) {
		if (appearanceProperty.isSetAppearance()) {
		_AbstractAppearance appearance = appearanceProperty.getAppearance();

		// how to map texture coordinates to a composite surface of
		// arbitrary depth?
		if (appearance.getCityGMLClass() == CityGMLClass._SIMPLE_TEXTURE &&
		abstractSurface.getGMLClass() != GMLClass.POLYGON) {

		StringBuilder msg = new StringBuilder(Util.getGeometrySignature(
		texturedSurface.getGMLClass(),
		origGmlId));
		msg.append(": Texture coordinates are only supported for base surfaces of type gml:Polygon.");

		LOG.error(msg.toString());
		continue;
		}

		boolean isFront = !(appearanceProperty.isSetOrientation() &&
		appearanceProperty.getOrientation().equals("-"));

		materialModelImporter.insert(appearance, abstractSurface, cityObjectId, isFront, targetURI);
		} else {
		// xlink
		String href = appearanceProperty.getHref();

		if (href != null && href.length() != 0) {
		boolean success = materialModelImporter.insertXlink(href, surfaceGeometryId, cityObjectId);
		if (!success) {
		LOG.error("XLink reference '" + href + "' could not be resolved.");
		}
		}
		}
		}
		}*/
		}

		// compositeSurface
		else if (surfaceGeometryType == GMLClass.COMPOSITE_SURFACE) {
			CompositeSurface compositeSurface = (CompositeSurface)surfaceGeometry;

			//if (origGmlId != null && !isCopy)
			//dbImporterManager.putGmlId(origGmlId, surfaceGeometryId, rootId, reverse, gmlId, CityGMLClass.ABSTRACT_GML_GEOMETRY);

			// set root entry


			// get surfaceMember
			if (compositeSurface.isSetSurfaceMember()) {
				for (SurfaceProperty surfaceProperty : compositeSurface.getSurfaceMember()) {
					if (surfaceProperty.isSetSurface()) {
						AbstractSurface abstractSurface = surfaceProperty.getSurface();

						switch (abstractSurface.getGMLClass()) {
						case POLYGON:
						case _TEXTURED_SURFACE:
						case ORIENTABLE_SURFACE:
							GetSurfaceGeometry(abstractSurface,reverse);
							break;
						case COMPOSITE_SURFACE:
						case SURFACE:
						case TRIANGULATED_SURFACE:
						case TIN:
							GetSurfaceGeometry(abstractSurface,reverse);
							break;
						}

					} else {
						// xlink
						String href = surfaceProperty.getHref();

						if (href != null && href.length() != 0) {

						}
					}
				}
			}
		}

		// Surface
		// since a surface is a geometric primitive we represent it as composite surface
		// within the database
		else if (surfaceGeometryType == GMLClass.SURFACE) {
			Surface surface = (Surface)surfaceGeometry;

			//if (origGmlId != null && !isCopy)
			//dbImporterManager.putGmlId(origGmlId, surfaceGeometryId, rootId, reverse, gmlId, CityGMLClass.ABSTRACT_GML_GEOMETRY);

			// set root entry


			// get surface patches
			if (surface.isSetPatches()) {
				SurfacePatchArrayProperty arrayProperty = surface.getPatches();
				if (arrayProperty.isSetSurfacePatch()) {
					for (AbstractSurfacePatch surfacePatch : arrayProperty.getSurfacePatch()) {

						if (surfacePatch.getGMLClass() == GMLClass.RECTANGLE) {
							Rectangle rectangle = (Rectangle)surfacePatch;
							if (rectangle.isSetExterior()) {
								LinearRing exteriorLinearRing = (LinearRing)rectangle.getExterior().getRing();
								if (exteriorLinearRing != null)
									GetSurfaceGeometry(exteriorLinearRing, reverse);
							}
						}

						else if (surfacePatch.getGMLClass() == GMLClass.TRIANGLE) {
							Triangle triangle = (Triangle)surfacePatch;
							if (triangle.isSetExterior()) {
								LinearRing exteriorLinearRing = (LinearRing)triangle.getExterior().getRing();
								if (exteriorLinearRing != null)
									GetSurfaceGeometry(exteriorLinearRing, reverse);
							}
						}
					}
				}
			}
		}

		// TriangulatedSurface, TIN
		else if (surfaceGeometryType == GMLClass.TRIANGULATED_SURFACE ||
				surfaceGeometryType == GMLClass.TIN) {
			TriangulatedSurface triangulatedSurface = (TriangulatedSurface)surfaceGeometry;

			//if (origGmlId != null && !isCopy)
			//dbImporterManager.putGmlId(origGmlId, surfaceGeometryId, rootId, reverse, gmlId, CityGMLClass.ABSTRACT_GML_GEOMETRY);



			// get triangles
			if (triangulatedSurface.isSetTrianglePatches()) {
				TrianglePatchArrayProperty arrayProperty = triangulatedSurface.getTrianglePatches();
				if (arrayProperty.isSetTriangle()) {
					for (Triangle trianglePatch : arrayProperty.getTriangle()) {
						if (trianglePatch.isSetExterior()) {
							LinearRing exteriorLinearRing = (LinearRing)trianglePatch.getExterior().getRing();
							if (exteriorLinearRing != null)
								GetSurfaceGeometry(exteriorLinearRing, reverse);
						}	
					}
				}
			}
		}

		// Solid
		else if (surfaceGeometryType == GMLClass.SOLID) {
			Solid solid = (Solid)surfaceGeometry;

			//if (origGmlId != null && !isCopy)
			//dbImporterManager.putGmlId(origGmlId, surfaceGeometryId, rootId, reverse, gmlId, CityGMLClass.ABSTRACT_GML_GEOMETRY);

			// set root entry


			// get Exterior
			if (solid.isSetExterior()) {
				SurfaceProperty exteriorSurface = solid.getExterior();

				if (exteriorSurface.isSetSurface()) {
					AbstractSurface abstractSurface = exteriorSurface.getSurface();

					// we just allow CompositeSurfaces here!
					if (abstractSurface.getGMLClass() == GMLClass.COMPOSITE_SURFACE) {
						GetSurfaceGeometry(abstractSurface,reverse);
					}
				} else {
					// xlink
					String href = exteriorSurface.getHref();

					if (href != null && href.length() != 0) {

					}
				}
			}

			// interior is not supported!
			if (solid.isSetInterior()) {
				StringBuilder msg = new StringBuilder(Util.getGeometrySignature(
						solid.getGMLClass(),
						origGmlId));
				msg.append(": gml:interior is not supported.");

				LOG.error(msg.toString());
			}
		}

		// CompositeSolid
		else if (surfaceGeometryType == GMLClass.COMPOSITE_SOLID) {
			CompositeSolid compositeSolid = (CompositeSolid)surfaceGeometry;

			//if (origGmlId != null && !isCopy)
			//dbImporterManager.putGmlId(origGmlId, surfaceGeometryId, rootId, reverse, gmlId, CityGMLClass.ABSTRACT_GML_GEOMETRY);



			// get solidMember
			if (compositeSolid.isSetSolidMember()) {
				for (SolidProperty solidProperty : compositeSolid.getSolidMember()) {
					if (solidProperty.isSetSolid()) {
						GetSurfaceGeometry(solidProperty.getSolid(),reverse);
					} else {
						// xlink
						String href = solidProperty.getHref();

						if (href != null && href.length() != 0) {

						}
					}
				}
			}
		}

		// MultiPolygon
		else if (surfaceGeometryType == GMLClass.MULTI_POLYGON) {
			MultiPolygon multiPolygon = (MultiPolygon)surfaceGeometry;

			//if (origGmlId != null && !isCopy)
			//dbImporterManager.putGmlId(origGmlId, surfaceGeometryId, rootId, reverse, gmlId, CityGMLClass.ABSTRACT_GML_GEOMETRY);



			// get polygonMember
			if (multiPolygon.isSetPolygonMember()) {
				for (PolygonProperty polygonProperty : multiPolygon.getPolygonMember()) {
					if (polygonProperty.isSetPolygon())
						GetSurfaceGeometry(polygonProperty.getPolygon(), reverse);
					else {
						// xlink
						String href = polygonProperty.getHref();

						if (href != null && href.length() != 0) {

						}
					}
				}
			}
		}

		// MultiSurface
		else if (surfaceGeometryType == GMLClass.MULTI_SURFACE) {
			MultiSurface multiSurface = (MultiSurface)surfaceGeometry;
			
			//if (origGmlId != null && !isCopy)
			//dbImporterManager.putGmlId(origGmlId, surfaceGeometryId, rootId, reverse, gmlId, CityGMLClass.ABSTRACT_GML_GEOMETRY);



			// get surfaceMember
			if (multiSurface.isSetSurfaceMember()) {
				for (SurfaceProperty surfaceProperty : multiSurface.getSurfaceMember()) {
					if (surfaceProperty.isSetSurface()) {
						AbstractSurface abstractSurface = surfaceProperty.getSurface();

						switch (abstractSurface.getGMLClass()) {
						case POLYGON:
						case _TEXTURED_SURFACE:
						case ORIENTABLE_SURFACE:
							GetSurfaceGeometry(abstractSurface, reverse);
							break;
						case COMPOSITE_SURFACE:
						case SURFACE:
						case TRIANGULATED_SURFACE:
						case TIN:
							GetSurfaceGeometry(abstractSurface, reverse);
							break;
						}

					} else {
						// xlink
						String href = surfaceProperty.getHref();

						if (href != null && href.length() != 0) {

						}
					}
				}
			}

			// get surfaceMembers
			if (multiSurface.isSetSurfaceMembers()) {
				SurfaceArrayProperty surfaceArrayProperty = multiSurface.getSurfaceMembers();

				if (surfaceArrayProperty.isSetSurface()) {
					for (AbstractSurface abstractSurface : surfaceArrayProperty.getSurface()) {

						switch (abstractSurface.getGMLClass()) {
						case POLYGON:
						case _TEXTURED_SURFACE:
						case ORIENTABLE_SURFACE:
							GetSurfaceGeometry(abstractSurface,reverse);
							break;
						case COMPOSITE_SURFACE:
						case SURFACE:
						case TRIANGULATED_SURFACE:
						case TIN:
							GetSurfaceGeometry(abstractSurface,reverse);
							break;
						}
					}
				}
			}
		}

		// MultiSolid
		else if (surfaceGeometryType == GMLClass.MULTI_SOLID) {
			MultiSolid multiSolid = (MultiSolid)surfaceGeometry;

			//if (origGmlId != null && !isCopy)
			//dbImporterManager.putGmlId(origGmlId, surfaceGeometryId, rootId, reverse, gmlId, CityGMLClass.ABSTRACT_GML_GEOMETRY);



			// get solidMember
			if (multiSolid.isSetSolidMember()) {
				for (SolidProperty solidProperty : multiSolid.getSolidMember()) {
					if (solidProperty.isSetSolid()) {
						//surfaceGeometryId = dbImporterManager.getDBId(DBSequencerEnum.SURFACE_GEOMETRY_ID_SEQ);
						GetSurfaceGeometry(solidProperty.getSolid(), reverse);
					} else {
						// xlink
						String href = solidProperty.getHref();

						if (href != null && href.length() != 0) {

						}
					}
				}
			}

			// get SolidMembers
			if (multiSolid.isSetSolidMembers()) {
				SolidArrayProperty solidArrayProperty = multiSolid.getSolidMembers();

				if (solidArrayProperty.isSetSolid()) {
					for (AbstractSolid abstractSolid : solidArrayProperty.getSolid()) {

						GetSurfaceGeometry(abstractSolid, reverse);
					}
				}
			}
		}

		// GeometricComplex
		else if (surfaceGeometryType == GMLClass.GEOMETRIC_COMPLEX) {
			GeometricComplex geometricComplex = (GeometricComplex)surfaceGeometry;

			if (geometricComplex.isSetElement()) {
				for (GeometricPrimitiveProperty geometricPrimitiveProperty : geometricComplex.getElement()) {
					if (geometricPrimitiveProperty.isSetGeometricPrimitive())
						GetSurfaceGeometry(geometricPrimitiveProperty.getGeometricPrimitive(), reverse);
					else {
						// xlink
						String href = geometricPrimitiveProperty.getHref();

						if (href != null && href.length() != 0) {

						}
					}
				}
			}
		}
		

	}
	
	
	

}
