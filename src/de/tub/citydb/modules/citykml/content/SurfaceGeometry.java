package de.tub.citydb.modules.citykml.content;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.model.citygml.building.AbstractBoundarySurface;
import org.citygml4j.model.citygml.building.AbstractBuilding;
import org.citygml4j.model.citygml.building.BoundarySurfaceProperty;
import org.citygml4j.model.citygml.texturedsurface._TexturedSurface;
import org.citygml4j.model.gml.GMLClass;
import org.citygml4j.model.gml.geometry.AbstractGeometry;
import org.citygml4j.model.gml.geometry.aggregates.MultiCurve;
import org.citygml4j.model.gml.geometry.aggregates.MultiCurveProperty;
import org.citygml4j.model.gml.geometry.aggregates.MultiPoint;
import org.citygml4j.model.gml.geometry.aggregates.MultiPointProperty;
import org.citygml4j.model.gml.geometry.aggregates.MultiPolygon;
import org.citygml4j.model.gml.geometry.aggregates.MultiSolid;
import org.citygml4j.model.gml.geometry.aggregates.MultiSurface;
import org.citygml4j.model.gml.geometry.aggregates.MultiSurfaceProperty;
import org.citygml4j.model.gml.geometry.complexes.CompositeCurve;
import org.citygml4j.model.gml.geometry.complexes.CompositeSolid;
import org.citygml4j.model.gml.geometry.complexes.CompositeSurface;
import org.citygml4j.model.gml.geometry.complexes.GeometricComplex;
import org.citygml4j.model.gml.geometry.complexes.GeometricComplexProperty;
import org.citygml4j.model.gml.geometry.primitives.AbstractCurve;
import org.citygml4j.model.gml.geometry.primitives.AbstractCurveSegment;
import org.citygml4j.model.gml.geometry.primitives.AbstractGeometricPrimitive;
import org.citygml4j.model.gml.geometry.primitives.AbstractRing;
import org.citygml4j.model.gml.geometry.primitives.AbstractRingProperty;
import org.citygml4j.model.gml.geometry.primitives.AbstractSolid;
import org.citygml4j.model.gml.geometry.primitives.AbstractSurface;
import org.citygml4j.model.gml.geometry.primitives.AbstractSurfacePatch;
import org.citygml4j.model.gml.geometry.primitives.ControlPoint;
import org.citygml4j.model.gml.geometry.primitives.Curve;
import org.citygml4j.model.gml.geometry.primitives.CurveArrayProperty;
import org.citygml4j.model.gml.geometry.primitives.CurveProperty;
import org.citygml4j.model.gml.geometry.primitives.CurveSegmentArrayProperty;
import org.citygml4j.model.gml.geometry.primitives.GeometricPositionGroup;
import org.citygml4j.model.gml.geometry.primitives.GeometricPrimitiveProperty;
import org.citygml4j.model.gml.geometry.primitives.LineString;
import org.citygml4j.model.gml.geometry.primitives.LineStringSegment;
import org.citygml4j.model.gml.geometry.primitives.LineStringSegmentArrayProperty;
import org.citygml4j.model.gml.geometry.primitives.LinearRing;
import org.citygml4j.model.gml.geometry.primitives.OrientableCurve;
import org.citygml4j.model.gml.geometry.primitives.OrientableSurface;
import org.citygml4j.model.gml.geometry.primitives.Point;
import org.citygml4j.model.gml.geometry.primitives.PointArrayProperty;
import org.citygml4j.model.gml.geometry.primitives.PointProperty;
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

import de.tub.citydb.config.Config;
import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.log.Logger;
import de.tub.citydb.modules.citykml.common.xlink.content.DBXlinkLinearRing;
import de.tub.citydb.modules.citykml.common.xlink.content.DBXlinkSurfaceGeometry;
import de.tub.citydb.modules.citykml.common.xlink.importer.DBXlinkImporterManager;
import de.tub.citydb.modules.citykml.util.Sqlite.SqliteImporterManager;
import de.tub.citydb.util.Util;

public class SurfaceGeometry {
	
	private List<List<Double>> _pointList = new ArrayList<List<Double>>();
	private final Logger LOG = Logger.getInstance();
	private List<Map<String, Object>> _SurfaceList = new ArrayList<Map<String,Object>>();
	private List<String> _SurfaceGmlId = new ArrayList<String>();
	private SqliteImporterManager sqlliteImporterManager;
	private boolean isCopy;
	private boolean isXlink;
	private boolean importAppearance;
	
	public SurfaceGeometry(Config config , SqliteImporterManager sqlliteImporterManager) {
			
		this.sqlliteImporterManager = sqlliteImporterManager;
		importAppearance = config.getProject().getImporter().getAppearances().isSetImportAppearance();
	}
	
	public void SetSurfaceID(String _SurfaceId)
	{
		_SurfaceGmlId.add(_SurfaceId);
	}
	
	public List<String> GetSurfaceID()
	{
		return _SurfaceGmlId;
	}

	
	public List<List<Double>> GetSurfaceGeometry(String buildingGmlId , AbstractGeometry surfaceGeometry , boolean reverse) throws Exception {

		
	
		GMLClass surfaceGeometryType = surfaceGeometry.getGMLClass();
		sqlliteImporterManager.updateGeometryCounter(surfaceGeometryType);

		if (!isCopy)
			isCopy = surfaceGeometry.hasLocalProperty(Internal.GEOMETRY_ORIGINAL);

		if (!isXlink)
			isXlink = surfaceGeometry.hasLocalProperty(Internal.GEOMETRY_XLINK);

		// gml:id handling
		String origGmlId, gmlId;
		origGmlId = gmlId = surfaceGeometry.getId();


		// ok, now we can have a look at different gml geometry objects
		// firstly, handle simple surface geometries
		// a single linearRing
		if (surfaceGeometryType == GMLClass.LINEAR_RING) {

		
			LinearRing linearRing = (LinearRing)surfaceGeometry;
			List<Double> points = linearRing.toList3d(reverse);
			List<List<Double>> pointList = new ArrayList<List<Double>>();
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

				SetSurfaceID(linearRing.getId());
				pointList.add(points);
				
				if (nrOfCoordinates < 4) {
					// invalid ring...
					StringBuilder msg = new StringBuilder(Util.getGeometrySignature(
							linearRing.getGMLClass(),
							origGmlId));
					msg.append(": Ring contains less than 4 coordinates. Skipping invalid ring.");
					LOG.error(msg.toString());
					return _pointList;
				}
				
				if (importAppearance && !isCopy) {
					if (origGmlId == null)
						origGmlId = gmlId;							

					if (linearRing.isSetId())
						sqlliteImporterManager.propagateXlink(new DBXlinkLinearRing(
								origGmlId,
								origGmlId,
								0));
				}

			}
			_pointList.addAll(pointList);

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
							return _pointList;
						}

						SetSurfaceID(polygon.getId());
						pointList.add(points);
						int ringNo = 0;

						// well, taking care about geometry is not enough... this ring could
						// be referenced by a <textureCoordinates> element. since we cannot store
						// the gml:id of linear rings in the database, we have to remember its id
						if (importAppearance && !isCopy) {
							if (origGmlId == null)
								origGmlId = gmlId;							

							if (exteriorLinearRing.isSetId())
								sqlliteImporterManager.propagateXlink(new DBXlinkLinearRing(
										exteriorLinearRing.getId(),
										origGmlId,
										ringNo));
						}

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
											return _pointList;
										}
										
										SetSurfaceID(polygon.getId());
										pointList.add(interiorPoints);
										ringNo++;


										// also remember the gml:id of interior rings in case it is
										// referenced by a <textureCoordinates> element
										if (importAppearance && !isCopy && interiorLinearRing.isSetId())
											sqlliteImporterManager.propagateXlink(new DBXlinkLinearRing(
													interiorLinearRing.getId(),
													origGmlId,
													ringNo));

									}
								} else {
									// invalid ring...
									StringBuilder msg = new StringBuilder(Util.getGeometrySignature(
											interiorAbstractRing.getGMLClass(),
											origGmlId));
									msg.append(": Only gml:LinearRing elements are supported as interior rings.");
									LOG.error(msg.toString());
									return _pointList;
								}
							}
							// we need this dummy entry to know the maximum number of found rings later on...
							if (importAppearance && !isCopy && ringNo > 0)
								sqlliteImporterManager.propagateXlink(new DBXlinkLinearRing(
										null,
										origGmlId,
										ringNo));

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
					return _pointList;
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
						GetSurfaceGeometry(buildingGmlId ,abstractSurface,reverse);
						break;
					case COMPOSITE_SURFACE:
					case SURFACE:
					case TRIANGULATED_SURFACE:
					case TIN:
						GetSurfaceGeometry(buildingGmlId ,abstractSurface,reverse);
						break;
					}

				} else {
					// xlink
					String href = surfaceProperty.getHref();

					if (href != null && href.length() != 0) {
						
						DBXlinkSurfaceGeometry xlink = new DBXlinkSurfaceGeometry(
								buildingGmlId,
								"0",
								"0",
								reverse,
								href
								);
						
						sqlliteImporterManager.propagateXlink(xlink);
							
					};
					mapping = href.replaceAll("^#", "");
				}

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
						GetSurfaceGeometry(buildingGmlId ,abstractSurface,reverse);
						break;
					case COMPOSITE_SURFACE:
					case SURFACE:
					case TRIANGULATED_SURFACE:
					case TIN:
						GetSurfaceGeometry(buildingGmlId ,abstractSurface,reverse);
						break;
					}

				} else {
					// xlink
					String href = surfaceProperty.getHref();

					if (href != null && href.length() != 0) {
						DBXlinkSurfaceGeometry xlink = new DBXlinkSurfaceGeometry(
								buildingGmlId,
								"0",
								"0",
								reverse,
								href
								);

						sqlliteImporterManager.propagateXlink(xlink);

						targetURI = href.replaceAll("^#", "");

						
					}
					return _pointList;
				}
			} else {
				// we cannot continue without having a base surface...
				StringBuilder msg = new StringBuilder(Util.getGeometrySignature(
						texturedSurface.getGMLClass(),
						origGmlId));
				msg.append(": Could not find <baseSurface> element.");

				LOG.error(msg.toString());	
				return _pointList;
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
							GetSurfaceGeometry(buildingGmlId ,abstractSurface,reverse);
							break;
						case COMPOSITE_SURFACE:
						case SURFACE:
						case TRIANGULATED_SURFACE:
						case TIN:
							GetSurfaceGeometry(buildingGmlId ,abstractSurface,reverse);
							break;
						}

					} else {
						// xlink
						String href = surfaceProperty.getHref();

						if (href != null && href.length() != 0) {
							DBXlinkSurfaceGeometry xlink = new DBXlinkSurfaceGeometry(
									buildingGmlId,
									"0",
									"0",
									reverse,
									href
									);
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
									GetSurfaceGeometry(buildingGmlId ,exteriorLinearRing, reverse);
							}
						}

						else if (surfacePatch.getGMLClass() == GMLClass.TRIANGLE) {
							Triangle triangle = (Triangle)surfacePatch;
							if (triangle.isSetExterior()) {
								LinearRing exteriorLinearRing = (LinearRing)triangle.getExterior().getRing();
								if (exteriorLinearRing != null)
									GetSurfaceGeometry(buildingGmlId ,exteriorLinearRing, reverse);
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
								GetSurfaceGeometry(buildingGmlId ,exteriorLinearRing, reverse);
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
						GetSurfaceGeometry(buildingGmlId ,abstractSurface,reverse);
					}
				} else {
					// xlink
					String href = exteriorSurface.getHref();

					if (href != null && href.length() != 0) {
						DBXlinkSurfaceGeometry xlink = new DBXlinkSurfaceGeometry(
								buildingGmlId,
								"0",
								"0",
								reverse,
								href
								);
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
						GetSurfaceGeometry(buildingGmlId ,solidProperty.getSolid(),reverse);
					} else {
						// xlink
						String href = solidProperty.getHref();

						if (href != null && href.length() != 0) {
							DBXlinkSurfaceGeometry xlink = new DBXlinkSurfaceGeometry(
									buildingGmlId,
									"0",
									"0",
									reverse,
									href
									);
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
						GetSurfaceGeometry(buildingGmlId ,polygonProperty.getPolygon(), reverse);
					else {
						// xlink
						String href = polygonProperty.getHref();

						if (href != null && href.length() != 0) {
							DBXlinkSurfaceGeometry xlink = new DBXlinkSurfaceGeometry(
									buildingGmlId,
									"0",
									"0",
									reverse,
									href
									);
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
							GetSurfaceGeometry(buildingGmlId ,abstractSurface, reverse);
							break;
						case COMPOSITE_SURFACE:
						case SURFACE:
						case TRIANGULATED_SURFACE:
						case TIN:
							GetSurfaceGeometry(buildingGmlId ,abstractSurface, reverse);
							break;
						}

					} else {
						// xlink
						String href = surfaceProperty.getHref();

						if (href != null && href.length() != 0) {
							DBXlinkSurfaceGeometry xlink = new DBXlinkSurfaceGeometry(
									buildingGmlId,
									"0",
									"0",
									reverse,
									href
									);
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
							GetSurfaceGeometry(buildingGmlId ,abstractSurface,reverse);
							break;
						case COMPOSITE_SURFACE:
						case SURFACE:
						case TRIANGULATED_SURFACE:
						case TIN:
							GetSurfaceGeometry(buildingGmlId ,abstractSurface,reverse);
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
						GetSurfaceGeometry(buildingGmlId ,solidProperty.getSolid(), reverse);
					} else {
						// xlink
						String href = solidProperty.getHref();

						if (href != null && href.length() != 0) {
							DBXlinkSurfaceGeometry xlink = new DBXlinkSurfaceGeometry(
									buildingGmlId,
									"0",
									"0",
									reverse,
									href
									);
						}
					}
				}
			}

			// get SolidMembers
			if (multiSolid.isSetSolidMembers()) {
				SolidArrayProperty solidArrayProperty = multiSolid.getSolidMembers();

				if (solidArrayProperty.isSetSolid()) {
					for (AbstractSolid abstractSolid : solidArrayProperty.getSolid()) {

						GetSurfaceGeometry(buildingGmlId ,abstractSolid, reverse);
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
						GetSurfaceGeometry(buildingGmlId ,geometricPrimitiveProperty.getGeometricPrimitive(), reverse);
					else {
						// xlink
						String href = geometricPrimitiveProperty.getHref();

						if (href != null && href.length() != 0) {
							DBXlinkSurfaceGeometry xlink = new DBXlinkSurfaceGeometry(
									buildingGmlId,
									"0",
									"0",
									reverse,
									href
									);
						}
					}
				}
			}
		}
	
		return _pointList;

	}
	


	
	public List<Double>  getCurve(CurveProperty curveProperty) throws Exception {
		
		return curveProperty != null ? getCurve(curveProperty.getCurve()): null;
	
	}

	
	
	public List<List<Double>> getMultiCurve(MultiCurveProperty multiCurveProperty)throws Exception {
	
		return multiCurveProperty != null ? getMultiCurve(multiCurveProperty.getMultiCurve()) : null;
	}

	
	
	public List<Double> getCurve(AbstractCurve curve) throws Exception {
		
		List<Double> pointList = new ArrayList<Double>();

		if (curve != null) {
			generatePointList(curve, pointList, false);
			
		}
		return pointList;
	
	}
	
	
	

	public List<List<Double>> getMultiCurve(MultiCurve multiCurve) throws Exception
	{
				
		List<List<Double>> pointList = new ArrayList<List<Double>>();
		
		if (multiCurve != null) {

			if (multiCurve.isSetCurveMember()) {

				for (CurveProperty curveProperty : multiCurve.getCurveMember()) {
					if (curveProperty.isSetCurve()) {
						AbstractCurve curve = curveProperty.getCurve();
						List<Double> points = new ArrayList<Double>();
						generatePointList(curve, points, false);

						if (!points.isEmpty())
							pointList.add(points);
					}
				}
			} else if (multiCurve.isSetCurveMembers()) {
				CurveArrayProperty curveArrayProperty = multiCurve.getCurveMembers();
				for (AbstractCurve curve : curveArrayProperty.getCurve()) {
					List<Double> points = new ArrayList<Double>();
					generatePointList(curve, points, false);

					if (!points.isEmpty())
						pointList.add(points);
				}
			}

		}

		return pointList;
	}
	
	
	
	private void generatePointList(AbstractCurve abstractCurve, List<Double> pointList, boolean reverse) {

		if (abstractCurve.getGMLClass() == GMLClass.LINE_STRING) {
			LineString lineString = (LineString) abstractCurve;
			List<Double> points = lineString.toList3d(reverse);

			if (points != null && !points.isEmpty())
				pointList.addAll(points);
		}

		else if (abstractCurve.getGMLClass() == GMLClass.CURVE) {
			Curve curve = (Curve) abstractCurve;
			if (curve.isSetSegments()) {
				CurveSegmentArrayProperty arrayProperty = curve.getSegments();

				if (arrayProperty.isSetCurveSegment()) {
					List<Double> points = new ArrayList<Double>();

					for (AbstractCurveSegment abstractCurveSegment : arrayProperty
							.getCurveSegment())
						if (abstractCurveSegment.getGMLClass() == GMLClass.LINE_STRING_SEGMENT)
							points.addAll(((LineStringSegment) abstractCurveSegment)
									.toList3d());

					if (!points.isEmpty()) {
						if (!reverse)
							pointList.addAll(points);
						else {
							for (int i = points.size() - 3; i >= 0; i -= 3)
								pointList.addAll(points.subList(i, i + 3));
						}
					}
				}
			}
		}

		else if (abstractCurve.getGMLClass() == GMLClass.COMPOSITE_CURVE) {
			CompositeCurve compositeCurve = (CompositeCurve) abstractCurve;
			if (compositeCurve.isSetCurveMember()) {
				for (CurveProperty curveProperty : compositeCurve
						.getCurveMember()) {
					if (curveProperty.isSetCurve())
						generatePointList(curveProperty.getCurve(), pointList,
								reverse);
					else {
						// xlinks are not allowed here...
					}
				}
			}
		}

		else if (abstractCurve.getGMLClass() == GMLClass.ORIENTABLE_CURVE) {
			OrientableCurve orientableCurve = (OrientableCurve) abstractCurve;
			if (orientableCurve.isSetOrientation()
					&& orientableCurve.getOrientation().equals("-"))
				reverse = !reverse;

			if (orientableCurve.isSetBaseCurve()) {
				CurveProperty curveProperty = orientableCurve.getBaseCurve();
				if (curveProperty.isSetCurve())
					generatePointList(curveProperty.getCurve(), pointList,
							reverse);
				else {
					// xlinks are not allowed here
				}
			}
		}
	}
	
	
	public  String DetectSurfaceType(List<Double> _pointList){
		
		
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
	
	
	public boolean ClearPointList()
	{	
	
		if(_pointList.size()>0)
		{
			_pointList.clear();
			return true;
		}
		else {
			return false;
		}
		
	}
	
	
	public boolean ClearIdList()
	{	
	
		if(_SurfaceGmlId.size()>0)
		{
			_SurfaceGmlId.clear();
			return true;
		}
		else {
			return false;
		}
		
	}

}
