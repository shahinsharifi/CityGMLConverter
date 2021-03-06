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
package de.tub.citydb.modules.citykml.content;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
// import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import javax.imageio.ImageIO;
import javax.media.j3d.GeometryArray;
import javax.vecmath.Point3d;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import net.opengis.kml._2.AltitudeModeEnumType;
import net.opengis.kml._2.BoundaryType;
import net.opengis.kml._2.LinearRingType;
import net.opengis.kml._2.LinkType;
import net.opengis.kml._2.LocationType;
import net.opengis.kml._2.ModelType;
import net.opengis.kml._2.MultiGeometryType;
import net.opengis.kml._2.OrientationType;
import net.opengis.kml._2.PlacemarkType;
import net.opengis.kml._2.PolygonType;

import org.citygml.textureAtlasAPI.TextureAtlasGenerator;
import org.citygml.textureAtlasAPI.dataStructure.TexImage;
import org.citygml.textureAtlasAPI.dataStructure.TexImageInfo;
import org.citygml4j.factory.CityGMLFactory;
import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.model.citygml.appearance.Color;
import org.citygml4j.model.citygml.appearance.X3DMaterial;
import org.collada._2005._11.colladaschema.Accessor;
import org.collada._2005._11.colladaschema.Asset;
import org.collada._2005._11.colladaschema.BindMaterial;
import org.collada._2005._11.colladaschema.COLLADA;
import org.collada._2005._11.colladaschema.CommonColorOrTextureType;
import org.collada._2005._11.colladaschema.CommonFloatOrParamType;
import org.collada._2005._11.colladaschema.CommonNewparamType;
import org.collada._2005._11.colladaschema.Effect;
import org.collada._2005._11.colladaschema.Extra;
import org.collada._2005._11.colladaschema.FloatArray;
import org.collada._2005._11.colladaschema.FxSampler2DCommon;
import org.collada._2005._11.colladaschema.FxSurfaceCommon;
import org.collada._2005._11.colladaschema.FxSurfaceInitFromCommon;
//import org.collada._2005._11.colladaschema.Geometry;					// collides with org.postgis.Geometry
import org.collada._2005._11.colladaschema.Image;
import org.collada._2005._11.colladaschema.InputLocal;
import org.collada._2005._11.colladaschema.InputLocalOffset;
import org.collada._2005._11.colladaschema.InstanceEffect;
import org.collada._2005._11.colladaschema.InstanceGeometry;
import org.collada._2005._11.colladaschema.InstanceMaterial;
import org.collada._2005._11.colladaschema.InstanceWithExtra;
import org.collada._2005._11.colladaschema.LibraryEffects;
import org.collada._2005._11.colladaschema.LibraryGeometries;
import org.collada._2005._11.colladaschema.LibraryImages;
import org.collada._2005._11.colladaschema.LibraryMaterials;
import org.collada._2005._11.colladaschema.LibraryVisualScenes;
import org.collada._2005._11.colladaschema.Material;
import org.collada._2005._11.colladaschema.Mesh;
import org.collada._2005._11.colladaschema.ObjectFactory;
import org.collada._2005._11.colladaschema.Param;
import org.collada._2005._11.colladaschema.ProfileCOMMON;
import org.collada._2005._11.colladaschema.Source;
import org.collada._2005._11.colladaschema.Technique;
import org.collada._2005._11.colladaschema.Triangles;
import org.collada._2005._11.colladaschema.UpAxisType;
import org.collada._2005._11.colladaschema.Vertices;
import org.collada._2005._11.colladaschema.VisualScene;
import org.geotools.geometry.jts.GeometryBuilder;
import org.geotools.geometry.jts.JTS;
import org.postgis.Geometry;											// collides with Collada-Geometry
import org.postgis.MultiPolygon;
import org.postgis.PGgeometry;
import org.postgis.Polygon;

// import org.postgresql.largeobject.LargeObject;
// import org.postgresql.largeobject.LargeObjectManager;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.sun.j3d.utils.geometry.GeometryInfo;

import de.micromata.opengis.kml.v_2_2_0.Coordinate;
import de.tub.citydb.api.database.DatabaseSrs;
import de.tub.citydb.api.event.EventDispatcher;
import de.tub.citydb.api.log.LogLevel;
import de.tub.citydb.config.Config;
import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.config.project.CitykmlExporter.Balloon;
import de.tub.citydb.config.project.CitykmlExporter.ColladaOptions;
import de.tub.citydb.config.project.CitykmlExporter.DisplayForm;
import  de.tub.citydb.modules.citykml.content.TypeAttributeValueEnum;
import de.tub.citydb.io.DirectoryScanner;
import de.tub.citydb.io.DirectoryScanner.CityGMLFilenameFilter;
import de.tub.citydb.log.Logger;
import de.tub.citydb.modules.citykml.util.ElevationHelper;
import de.tub.citydb.modules.citykml.util.ProjConvertor;
import de.tub.citydb.modules.common.event.CounterEvent;
import de.tub.citydb.modules.common.event.CounterType;
import de.tub.citydb.modules.common.event.GeometryCounterEvent;
import de.tub.citydb.modules.citykml.content.BalloonTemplateHandlerImpl;
import de.tub.citydb.modules.citykml.content.ElevationServiceHandler;
import de.tub.citydb.modules.citykml.content.KmlExporterManager;
import de.tub.citydb.modules.citykml.content.KmlGenericObject;
import de.tub.citydb.modules.citykml.content.KmlSplittingResult;
import de.tub.citydb.modules.citykml.content.Queries;
import de.tub.citydb.modules.citykml.content.TexCoords;
import de.tub.citydb.modules.citykml.content.VertexInfo;
import de.tub.citydb.util.Util;

public abstract class KmlGenericObject {

	protected static final int GEOMETRY_AMOUNT_WARNING = 10000;

	/** Tolerance after triangulation must be bigger than before triangulation since some points
	 * may deviate 0.00999999 before and 0.01000001 after. Using a single bigger tolerance value
	 * does not help since the effect repeats itself (0.01999999 vs. 0.0200001).
	 * 
	 * Tolerance after triangulation must not be much bigger than tolerance before, otherwise
	 * there is a risk of going up the wrong node tree when searching for a vertex
	 */
	private final static double TOLERANCE_BEFORE_TRIANGULATION = 0.015d; // this is very tolerant!!!
	private final static double TOLERANCE_AFTER_TRIANGULATION = 0.0150005d; // this is very tolerant!!!

	private final static String NO_TEXIMAGE = "default";

	private HashMap<String, GeometryInfo> geometryInfos = new HashMap<String, GeometryInfo>();
	// coordinates include texCoordinates, which geometryInfo does not
	// texCoordinates in geometryInfo would be float --> precision loss
	private NodeZ coordinateTree;

	// key is surfaceId, surfaceId is originally a Long, here we use an Object for compatibility with the textureAtlasAPI
	private HashMap<Object, String> texImageUris = new HashMap<Object, String>();
	// key is imageUri
	private HashMap<String, BufferedImage> texImages = new HashMap<String, BufferedImage>();
	// for images in unusual formats or wrapping textures. Most times it will be null.
	// key is imageUri
	//	private HashMap<String, OrdImage> texOrdImages = null;
	// key is surfaceId, surfaceId is originally a Long
	private HashMap<String, X3DMaterial> x3dMaterials = null;

	private long id;
	private String gmlId;
	private BigInteger vertexIdCounter = new BigInteger("-1");
	private VertexInfo firstVertexInfo = null;
	private VertexInfo lastVertexInfo = null;

	// origin of the relative coordinates for the object
	private double originX;
	private double originY;
	private double originZ;

	// placemark location in WGS84
	private double locationX;
	private double locationY;
	private double locationZ;

	private double zOffset;

	private boolean ignoreSurfaceOrientation = true;

	protected Connection connection;
	protected KmlExporterManager kmlExporterManager;
	protected CityGMLFactory cityGMLFactory; 
	protected net.opengis.kml._2.ObjectFactory kmlFactory;
	protected ElevationServiceHandler elevationServiceHandler;
	protected BalloonTemplateHandlerImpl balloonTemplateHandler;
	protected EventDispatcher eventDispatcher;
	protected Config config;

	protected int currentLod;
	protected DatabaseSrs dbSrs;
	protected X3DMaterial defaultX3dMaterial;

	private DirectoryScanner directoryScanner;

	public KmlGenericObject(Connection connection,
			KmlExporterManager kmlExporterManager,
			CityGMLFactory cityGMLFactory,
			net.opengis.kml._2.ObjectFactory kmlFactory,
			ElevationServiceHandler elevationServiceHandler,
			BalloonTemplateHandlerImpl balloonTemplateHandler,
			EventDispatcher eventDispatcher,
			Config config) {

		this.connection = connection;
		this.kmlExporterManager = kmlExporterManager;
		this.cityGMLFactory = cityGMLFactory;
		this.kmlFactory = kmlFactory;
		this.elevationServiceHandler = elevationServiceHandler;
		this.balloonTemplateHandler = balloonTemplateHandler;
		this.eventDispatcher = eventDispatcher;
		this.config = config;

		//	dbSrs = DatabaseConnectionPool.getInstance().getActiveConnectionMetaData().getReferenceSystem();
		/*
		dateFormatter = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

		defaultX3dMaterial = cityGMLFactory.createX3DMaterial();
		defaultX3dMaterial.setAmbientIntensity(0.2d);
		defaultX3dMaterial.setShininess(0.2d);
		defaultX3dMaterial.setTransparency(0d);
		defaultX3dMaterial.setDiffuseColor(getX3dColorFromString("0.8 0.8 0.8"));
		defaultX3dMaterial.setSpecularColor(getX3dColorFromString("1.0 1.0 1.0"));
		defaultX3dMaterial.setEmissiveColor(getX3dColorFromString("0.0 0.0 0.0"));*/
	}

	public abstract void read(KmlSplittingResult work);
	public abstract String getStyleBasisName();
	public abstract ColladaOptions getColladaOptions();
	public abstract Balloon getBalloonSettings();
	protected abstract List<DisplayForm> getDisplayForms();
	protected abstract String getHighlightingQuery();

	public void setId(long id) {
		this.id = id;
	}

	public long getId() {
		return id;
	}

	public void setGmlId(String gmlId) {
		this.gmlId = gmlId.replace(':', '_');
	}

	public String getGmlId() {
		return gmlId;
	}

	public void setOriginX(double originX) {
		this.originX = originX;
	}

	public double getOriginX() {
		return originX;
	}

	public void setOriginY(double originY) {
		this.originY = originY;
	}

	public double getOriginY() {
		return originY;
	}

	public void setOriginZ(double originZ) {
		this.originZ = originZ;
	}

	public double getOriginZ() {
		return originZ;
	}

	public void setZOffset(double zOffset) {
		this.zOffset = zOffset;
	}

	public double getZOffset() {
		return zOffset;
	}

	public void setLocationX(double locationX) {
		this.locationX = locationX;
	}

	public double getLocationX() {
		return locationX;
	}

	public void setLocationY(double locationY) {
		this.locationY = locationY;
	}

	public double getLocationY() {
		return locationY;
	}

	protected void setLocationZ(double locationZ) {
		this.locationZ = locationZ;
	}

	protected double getLocationZ() {
		return locationZ;
	}

	public void setIgnoreSurfaceOrientation(boolean ignoreSurfaceOrientation) {
		this.ignoreSurfaceOrientation = ignoreSurfaceOrientation;
	}

	public boolean isIgnoreSurfaceOrientation() {
		return ignoreSurfaceOrientation;
	}


	public COLLADA generateColladaTree() throws DatatypeConfigurationException{

		ObjectFactory colladaFactory = new ObjectFactory();

		// java and XML...
		DatatypeFactory df = DatatypeFactory.newInstance();
		XMLGregorianCalendar xmlGregorianCalendar = df.newXMLGregorianCalendar(new GregorianCalendar());
		xmlGregorianCalendar.setTimezone(DatatypeConstants.FIELD_UNDEFINED);

		COLLADA	collada = colladaFactory.createCOLLADA();
		collada.setVersion("1.4.1");
		// --------------------------- asset ---------------------------

		Asset asset = colladaFactory.createAsset();
		asset.setCreated(xmlGregorianCalendar);
		asset.setModified(xmlGregorianCalendar);
		Asset.Unit unit = colladaFactory.createAssetUnit();
		unit.setName("meters");
		unit.setMeter(1.0);
		asset.setUnit(unit);
		asset.setUpAxis(UpAxisType.Z_UP);
		Asset.Contributor contributor = colladaFactory.createAssetContributor();
		// System.getProperty("line.separator") produces weird effects here
		contributor.setAuthoringTool(this.getClass().getPackage().getImplementationTitle() + ", version " +
				this.getClass().getPackage().getImplementationVersion() + "; " +
				this.getClass().getPackage().getImplementationVendor());
		asset.getContributor().add(contributor);
		collada.setAsset(asset);

		LibraryImages libraryImages = colladaFactory.createLibraryImages();
		LibraryMaterials libraryMaterials = colladaFactory.createLibraryMaterials();
		LibraryEffects libraryEffects = colladaFactory.createLibraryEffects();
		LibraryGeometries libraryGeometries = colladaFactory.createLibraryGeometries();
		LibraryVisualScenes libraryVisualScenes = colladaFactory.createLibraryVisualScenes();

		// --------------------------- geometry (constant part) ---------------------------
		org.collada._2005._11.colladaschema.Geometry geometry = colladaFactory.createGeometry();
		geometry.setId("geometry0");

		Source positionSource = colladaFactory.createSource();
		positionSource.setId("geometry0-position");

		FloatArray positionArray = colladaFactory.createFloatArray();
		positionArray.setId("geometry0-position-array");
		List<Double> positionValues = positionArray.getValue();
		positionSource.setFloatArray(positionArray);

		Accessor positionAccessor = colladaFactory.createAccessor();
		positionAccessor.setSource("#" + positionArray.getId());
		positionAccessor.setStride(new BigInteger("3"));
		Param paramX = colladaFactory.createParam();
		paramX.setType("float");
		paramX.setName("X");
		Param paramY = colladaFactory.createParam();
		paramY.setType("float");
		paramY.setName("Y");
		Param paramZ = colladaFactory.createParam();
		paramZ.setType("float");
		paramZ.setName("Z");
		positionAccessor.getParam().add(paramX);
		positionAccessor.getParam().add(paramY);
		positionAccessor.getParam().add(paramZ);
		Source.TechniqueCommon positionTechnique = colladaFactory.createSourceTechniqueCommon();
		positionTechnique.setAccessor(positionAccessor);
		positionSource.setTechniqueCommon(positionTechnique);

		Source texCoordsSource = colladaFactory.createSource();
		texCoordsSource.setId("geometry0-texCoords");

		FloatArray texCoordsArray = colladaFactory.createFloatArray();
		texCoordsArray.setId("geometry0-texCoords-array");
		List<Double> texCoordsValues = texCoordsArray.getValue();
		texCoordsSource.setFloatArray(texCoordsArray);

		Accessor texCoordsAccessor = colladaFactory.createAccessor();
		texCoordsAccessor.setSource("#" + texCoordsArray.getId());
		texCoordsAccessor.setStride(new BigInteger("2"));
		Param paramS = colladaFactory.createParam();
		paramS.setType("float");
		paramS.setName("S");
		Param paramT = colladaFactory.createParam();
		paramT.setType("float");
		paramT.setName("T");
		texCoordsAccessor.getParam().add(paramS);
		texCoordsAccessor.getParam().add(paramT);
		Source.TechniqueCommon texCoordsTechnique = colladaFactory.createSourceTechniqueCommon();
		texCoordsTechnique.setAccessor(texCoordsAccessor);
		texCoordsSource.setTechniqueCommon(texCoordsTechnique);

		Vertices vertices = colladaFactory.createVertices();
		vertices.setId("geometry0-vertex");
		InputLocal input = colladaFactory.createInputLocal();
		input.setSemantic("POSITION");
		input.setSource("#" + positionSource.getId());
		vertices.getInput().add(input);

		Mesh mesh = colladaFactory.createMesh();
		mesh.getSource().add(positionSource);
		mesh.getSource().add(texCoordsSource);
		mesh.setVertices(vertices);
		geometry.setMesh(mesh);
		libraryGeometries.getGeometry().add(geometry);
		BigInteger texCoordsCounter = BigInteger.ZERO;

		// --------------------------- visual scenes ---------------------------
		VisualScene visualScene = colladaFactory.createVisualScene();
		visualScene.setId("Building_" + gmlId);
		BindMaterial.TechniqueCommon techniqueCommon = colladaFactory.createBindMaterialTechniqueCommon();
		BindMaterial bindMaterial = colladaFactory.createBindMaterial();
		bindMaterial.setTechniqueCommon(techniqueCommon);
		InstanceGeometry instanceGeometry = colladaFactory.createInstanceGeometry();
		instanceGeometry.setUrl("#" + geometry.getId());
		instanceGeometry.setBindMaterial(bindMaterial);
		org.collada._2005._11.colladaschema.Node node = colladaFactory.createNode();
		node.getInstanceGeometry().add(instanceGeometry);
		visualScene.getNode().add(node);
		libraryVisualScenes.getVisualScene().add(visualScene);

		// --------------------------- now the variable part ---------------------------
		Triangles triangles = null;
		HashMap<String, Triangles> trianglesByTexImageName = new HashMap<String, Triangles>();

		// geometryInfos contains all surfaces, textured or not
		Set<String> keySet = geometryInfos.keySet();
		Iterator<String> iterator = keySet.iterator();
		while (iterator.hasNext()) {
			String surfaceId = iterator.next();
			
			String texImageName = texImageUris.get(surfaceId);
			X3DMaterial x3dMaterial = getX3dMaterial(surfaceId);
			boolean surfaceTextured = true;
			if (texImageName == null) {
				surfaceTextured = false;
				texImageName = (x3dMaterial != null) ?
						buildNameFromX3dMaterial(x3dMaterial):
							NO_TEXIMAGE; // <- should never happen
			}

			triangles = trianglesByTexImageName.get(texImageName);
			if (triangles == null) { // never worked on this image or material before

				// --------------------------- materials ---------------------------
				Material material = colladaFactory.createMaterial();
				material.setId(replaceExtensionWithSuffix(texImageName, "_mat"));
				InstanceEffect instanceEffect = colladaFactory.createInstanceEffect();
				instanceEffect.setUrl("#" + replaceExtensionWithSuffix(texImageName, "_eff"));
				material.setInstanceEffect(instanceEffect);
				libraryMaterials.getMaterial().add(material);

				// --------------------- effects common part 1 ---------------------
				Effect effect = colladaFactory.createEffect();
				effect.setId(replaceExtensionWithSuffix(texImageName, "_eff"));
				ProfileCOMMON profileCommon = colladaFactory.createProfileCOMMON();

				if (surfaceTextured) {
					// --------------------------- images ---------------------------
					Image image = colladaFactory.createImage();
					image.setId(replaceExtensionWithSuffix(texImageName, "_img"));
					image.setInitFrom(texImageName);
					libraryImages.getImage().add(image);

					// --------------------------- effects ---------------------------
					FxSurfaceInitFromCommon initFrom = colladaFactory.createFxSurfaceInitFromCommon();
					initFrom.setValue(image); // evtl. image.getId();
					FxSurfaceCommon surface = colladaFactory.createFxSurfaceCommon();
					surface.setType("2D"); // ColladaConstants.SURFACE_TYPE_2D
					surface.getInitFrom().add(initFrom);

					CommonNewparamType newParam1 = colladaFactory.createCommonNewparamType();
					newParam1.setSurface(surface);
					newParam1.setSid(replaceExtensionWithSuffix(texImageName, "_surface"));
					profileCommon.getImageOrNewparam().add(newParam1);

					FxSampler2DCommon sampler2D = colladaFactory.createFxSampler2DCommon();
					sampler2D.setSource(newParam1.getSid());
					CommonNewparamType newParam2 = colladaFactory.createCommonNewparamType();
					newParam2.setSampler2D(sampler2D);
					newParam2.setSid(replaceExtensionWithSuffix(texImageName, "_sampler"));
					profileCommon.getImageOrNewparam().add(newParam2);

					ProfileCOMMON.Technique profileCommonTechnique = colladaFactory.createProfileCOMMONTechnique();
					profileCommonTechnique.setSid("COMMON");
					ProfileCOMMON.Technique.Lambert lambert = colladaFactory.createProfileCOMMONTechniqueLambert();
					CommonColorOrTextureType.Texture texture = colladaFactory.createCommonColorOrTextureTypeTexture();
					texture.setTexture(newParam2.getSid());
					texture.setTexcoord("TEXCOORD"); // ColladaConstants.INPUT_SEMANTIC_TEXCOORD
					CommonColorOrTextureType ccott = colladaFactory.createCommonColorOrTextureType();
					ccott.setTexture(texture);
					lambert.setDiffuse(ccott);
					profileCommonTechnique.setLambert(lambert);
					profileCommon.setTechnique(profileCommonTechnique);
				}
				else {
					// --------------------------- effects ---------------------------
					ProfileCOMMON.Technique profileCommonTechnique = colladaFactory.createProfileCOMMONTechnique();
					profileCommonTechnique.setSid("COMMON");
					ProfileCOMMON.Technique.Lambert lambert = colladaFactory.createProfileCOMMONTechniqueLambert();

					CommonFloatOrParamType cfopt = colladaFactory.createCommonFloatOrParamType();
					CommonFloatOrParamType.Float cfoptf = colladaFactory.createCommonFloatOrParamTypeFloat();
					if (x3dMaterial.isSetShininess()) {
						cfoptf.setValue(x3dMaterial.getShininess());
						cfopt.setFloat(cfoptf);
						lambert.setReflectivity(cfopt);
					}

					if (x3dMaterial.isSetTransparency()) {
						cfopt = colladaFactory.createCommonFloatOrParamType();
						cfoptf = colladaFactory.createCommonFloatOrParamTypeFloat();
						cfoptf.setValue(x3dMaterial.getTransparency());
						cfopt.setFloat(cfoptf);
						lambert.setTransparency(cfopt);
					}

					if (x3dMaterial.isSetDiffuseColor()) {
						CommonColorOrTextureType.Color color = colladaFactory.createCommonColorOrTextureTypeColor();
						color.getValue().add(x3dMaterial.getDiffuseColor().getRed());
						color.getValue().add(x3dMaterial.getDiffuseColor().getGreen());
						color.getValue().add(x3dMaterial.getDiffuseColor().getBlue());
						color.getValue().add(1d); // alpha
						CommonColorOrTextureType ccott = colladaFactory.createCommonColorOrTextureType();
						ccott.setColor(color);
						lambert.setDiffuse(ccott);
					}

					if (x3dMaterial.isSetSpecularColor()) {
						CommonColorOrTextureType.Color color = colladaFactory.createCommonColorOrTextureTypeColor();
						color.getValue().add(x3dMaterial.getSpecularColor().getRed());
						color.getValue().add(x3dMaterial.getSpecularColor().getGreen());
						color.getValue().add(x3dMaterial.getSpecularColor().getBlue());
						color.getValue().add(1d); // alpha
						CommonColorOrTextureType ccott = colladaFactory.createCommonColorOrTextureType();
						ccott.setColor(color);
						lambert.setReflective(ccott);
					}

					if (x3dMaterial.isSetEmissiveColor()) {
						CommonColorOrTextureType.Color color = colladaFactory.createCommonColorOrTextureTypeColor();
						color.getValue().add(x3dMaterial.getEmissiveColor().getRed());
						color.getValue().add(x3dMaterial.getEmissiveColor().getGreen());
						color.getValue().add(x3dMaterial.getEmissiveColor().getBlue());
						color.getValue().add(1d); // alpha
						CommonColorOrTextureType ccott = colladaFactory.createCommonColorOrTextureType();
						ccott.setColor(color);
						lambert.setEmission(ccott);
					}

					profileCommonTechnique.setLambert(lambert);
					profileCommon.setTechnique(profileCommonTechnique);
				}

				// --------------------- effects common part 2 ---------------------
				Technique geTechnique = colladaFactory.createTechnique();
				geTechnique.setProfile("GOOGLEEARTH");

				try {
					DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
					DocumentBuilder docBuilder = factory.newDocumentBuilder();
					Document document = docBuilder.newDocument();
					factory.setNamespaceAware(true);
					Element doubleSided = document.createElementNS("http://www.collada.org/2005/11/COLLADASchema", "double_sided");
					doubleSided.setTextContent(ignoreSurfaceOrientation ? "1": "0");
					geTechnique.getAny().add(doubleSided);
				} catch (ParserConfigurationException e) {
					e.printStackTrace();
				}

				Extra extra = colladaFactory.createExtra();
				extra.getTechnique().add(geTechnique);
				profileCommon.getExtra().add(extra);

				effect.getFxProfileAbstract().add(colladaFactory.createProfileCOMMON(profileCommon));

				libraryEffects.getEffect().add(effect);

				// --------------------------- triangles ---------------------------
				triangles = colladaFactory.createTriangles();
				triangles.setMaterial(replaceExtensionWithSuffix(texImageName, "_tri"));
				InputLocalOffset inputV = colladaFactory.createInputLocalOffset();
				inputV.setSemantic("VERTEX"); // ColladaConstants.INPUT_SEMANTIC_VERTEX
				inputV.setSource("#" + vertices.getId());
				inputV.setOffset(BigInteger.ZERO);
				triangles.getInput().add(inputV);
				if (surfaceTextured) {
					InputLocalOffset inputT = colladaFactory.createInputLocalOffset();
					inputT.setSemantic("TEXCOORD"); // ColladaConstants.INPUT_SEMANTIC_TEXCOORD
					inputT.setSource("#" + texCoordsSource.getId());
					inputT.setOffset(BigInteger.ONE);
					triangles.getInput().add(inputT);
				}

				trianglesByTexImageName.put(texImageName, triangles);
			}

			// --------------------------- geometry (variable part) ---------------------------
			GeometryInfo ginfo = geometryInfos.get(surfaceId);
			ginfo.convertToIndexedTriangles();
			/*
		// the following seems to be buggy, so don't do it for now
		// generate normals, currently not used, but this is the recommended order
		NormalGenerator ng = new NormalGenerator();
		ng.generateNormals(ginfo);
		// stripify: merge triangles together into bigger triangles when possible
		Stripifier st = new Stripifier();
		st.stripify(ginfo);
			 */
			GeometryArray gArray = ginfo.getGeometryArray();
			Point3d coordPoint = new Point3d();
			for(int i = 0; i < gArray.getVertexCount(); i++){
				gArray.getCoordinate(i, coordPoint);
			
				VertexInfo vertexInfo = getVertexInfoForXYZ(coordPoint.x, coordPoint.y, coordPoint.z);
				if (vertexInfo == null || (surfaceTextured && vertexInfo.getTexCoords(surfaceId) == null)) {
					// no node or wrong node found
					// use best fit only in extreme cases (it is slow)
					if (surfaceTextured) {
						vertexInfo = getVertexInfoBestFitForXYZ(coordPoint.x, coordPoint.y, coordPoint.z, surfaceId);
					}
					else {
						vertexInfo = getVertexInfoBestFitForXYZ(coordPoint.x, coordPoint.y, coordPoint.z);
					}
				}
				triangles.getP().add(vertexInfo.getVertexId());

				if (surfaceTextured) {
					TexCoords texCoords = vertexInfo.getTexCoords(surfaceId);
					if (texCoords != null) {
						// trying to save some texture points
						int indexOfT = texCoordsValues.indexOf(texCoords.getT());
						if (indexOfT > 0 && indexOfT%2 == 1 && // avoid coincidences
								texCoordsValues.get(indexOfT - 1).equals(texCoords.getS())) {
							triangles.getP().add(new BigInteger(String.valueOf((indexOfT - 1)/2)));
						}
						else {
							texCoordsValues.add(new Double(texCoords.getS()));
							texCoordsValues.add(new Double(texCoords.getT()));
							triangles.getP().add(texCoordsCounter);
							texCoordsCounter = texCoordsCounter.add(BigInteger.ONE);
							// no triangleCounter++ since it is BigInteger
						}
					}
					else { // should never happen
						triangles.getP().add(texCoordsCounter); // wrong data is better than triangles out of sync
						Logger.getInstance().log(LogLevel.DEBUG,
								"texCoords not found for (" + coordPoint.x + ", " + coordPoint.y + ", "
										+ coordPoint.z + "). TOLERANCE = " + TOLERANCE_AFTER_TRIANGULATION);
					}
				}
			}
		}

		VertexInfo vertexInfoIterator = firstVertexInfo;
		while (vertexInfoIterator != null) {
			// undo trick for very close coordinates
			positionValues.add(new Double(reducePrecisionForXorY((vertexInfoIterator.getX() - originX)/100)));
			positionValues.add(new Double(reducePrecisionForXorY((vertexInfoIterator.getY() - originY)/100)));
			positionValues.add(new Double(reducePrecisionForZ((vertexInfoIterator.getZ() - originZ)/100)));
			vertexInfoIterator = vertexInfoIterator.getNextVertexInfo();
		}
		positionArray.setCount(new BigInteger(String.valueOf(positionValues.size()))); // gotta love BigInteger!
		texCoordsArray.setCount(new BigInteger(String.valueOf(texCoordsValues.size())));
		positionAccessor.setCount(positionArray.getCount().divide(positionAccessor.getStride()));
		texCoordsAccessor.setCount(texCoordsArray.getCount().divide(texCoordsAccessor.getStride()));

		Set<String> trianglesKeySet = trianglesByTexImageName.keySet();
		Iterator<String> trianglesIterator = trianglesKeySet.iterator();
		while (trianglesIterator.hasNext()) {
			String texImageName = trianglesIterator.next();
			triangles = trianglesByTexImageName.get(texImageName);
			triangles.setCount(new BigInteger(String.valueOf(triangles.getP().size()/(3*triangles.getInput().size()))));
			if (texImageName.startsWith(NO_TEXIMAGE)) { // materials first, textures last
				mesh.getLinesOrLinestripsOrPolygons().add(0, triangles);
			}
			else {
				mesh.getLinesOrLinestripsOrPolygons().add(triangles);
			}
			InstanceMaterial instanceMaterial = colladaFactory.createInstanceMaterial();
			instanceMaterial.setSymbol(triangles.getMaterial());
			instanceMaterial.setTarget("#" + replaceExtensionWithSuffix(texImageName, "_mat"));
			techniqueCommon.getInstanceMaterial().add(instanceMaterial);
		}

		// this method's name is really like this...
		List<Object> libraries = collada.getLibraryAnimationsOrLibraryAnimationClipsOrLibraryCameras();

		if (!libraryImages.getImage().isEmpty()) { // there may be buildings with no textures at all
			libraries.add(libraryImages);
		}
		libraries.add(libraryMaterials);
		libraries.add(libraryEffects);
		libraries.add(libraryGeometries);
		libraries.add(libraryVisualScenes);

		InstanceWithExtra instanceWithExtra = colladaFactory.createInstanceWithExtra();
		instanceWithExtra.setUrl("#" + visualScene.getId());
		COLLADA.Scene scene = colladaFactory.createCOLLADAScene();
		scene.setInstanceVisualScene(instanceWithExtra);
		collada.setScene(scene);

		return collada;
	}

	private String replaceExtensionWithSuffix (String imageName, String suffix) {
		int indexOfExtension = imageName.lastIndexOf('.');
		if (indexOfExtension != -1) {
			imageName = imageName.substring(0, indexOfExtension);
		}
		return imageName + suffix;
	}

	protected HashMap<Object, String> getTexImageUris(){
		return texImageUris;
	}

	public void addGeometryInfo(String surfaceId, GeometryInfo geometryInfo){
		geometryInfos.put(surfaceId, geometryInfo);
	}

	protected int getGeometryAmount(){
		return geometryInfos.size();
	}

	public GeometryInfo getGeometryInfo(String surfaceId){
		return geometryInfos.get(surfaceId);
	}

	protected void addX3dMaterial(String surfaceId, X3DMaterial x3dMaterial){
		if (x3dMaterial == null) return;
		if (x3dMaterial.isSetAmbientIntensity()
				|| x3dMaterial.isSetShininess()
				|| x3dMaterial.isSetTransparency()
				|| x3dMaterial.isSetDiffuseColor()
				|| x3dMaterial.isSetSpecularColor()
				|| x3dMaterial.isSetEmissiveColor()) {

			if (x3dMaterials == null) {
				x3dMaterials = new HashMap<String, X3DMaterial>();
			}
			x3dMaterials.put(surfaceId, x3dMaterial);
		}
	}

	protected X3DMaterial getX3dMaterial(String surfaceId) {
		X3DMaterial x3dMaterial = null;
		if (x3dMaterials != null) {
			x3dMaterial = x3dMaterials.get(surfaceId);
		}
		return x3dMaterial;
	}

	protected void addTexImageUri(String surfaceId, String texImageUri){
		if (texImageUri != null) {
			texImageUris.put(surfaceId, texImageUri);
		}
	}

	protected void addTexImage(String texImageUri, BufferedImage texImage){
		if (texImage != null) {
			texImages.put(texImageUri, texImage);

		}
	}

	protected void removeTexImage(String texImageUri){
		texImages.remove(texImageUri);
	}

	public HashMap<String, BufferedImage> getTexImages(){
		return texImages;
	}

	protected BufferedImage getTexImage(String texImageUri){
		BufferedImage texImage = null;
		if (texImages != null) {
			texImage = texImages.get(texImageUri);
		}
		return texImage;
	}

	/*
	private void removeTexImage(String texImageUri){
		texImages.remove(texImageUri);
	}

	public void addTexOrdImage(String texImageUri, OrdImage texOrdImage){
		if (texOrdImage == null) {
			return;
		}
		if (texOrdImages == null) {
			texOrdImages = new HashMap<String, OrdImage>();
		}
		texOrdImages.put(texImageUri, texOrdImage);
	}

	public HashMap<String, OrdImage> getTexOrdImages(){
		return texOrdImages;
	}

	public OrdImage getTexOrdImage(String texImageUri){
		OrdImage texOrdImage = null;
		if (texOrdImages != null) {
			texOrdImage = texOrdImages.get(texImageUri);
		}
		return texOrdImage;
	}*/

	public void setVertexInfoForXYZ(String surfaceId, double x, double y, double z, TexCoords texCoordsForThisSurface){
		vertexIdCounter = vertexIdCounter.add(BigInteger.ONE);
		VertexInfo vertexInfo = new VertexInfo(vertexIdCounter, x, y, z);
		vertexInfo.addTexCoords(surfaceId, texCoordsForThisSurface);
		NodeZ nodeToInsert = new NodeZ(z, new NodeY(y, new NodeX(x, vertexInfo)));
		if (coordinateTree == null) {
			coordinateTree =  nodeToInsert;
			firstVertexInfo = vertexInfo;
			lastVertexInfo = vertexInfo;
		}
		else {
			insertNode(coordinateTree, nodeToInsert);
		}
	}

	protected VertexInfo getVertexInfoForXYZ(double x, double y, double z){
		NodeY rootY = (NodeY) getValue(z, coordinateTree);
		NodeX rootX = (NodeX) getValue(y, rootY);
		VertexInfo vertexInfo = (VertexInfo) getValue(x, rootX);
		return vertexInfo;
	}

	private void insertNode(Node currentBasis, Node nodeToInsert) {
		int compareKeysResult = compareKeys(nodeToInsert.key, currentBasis.key, TOLERANCE_BEFORE_TRIANGULATION);
		if (compareKeysResult > 0) {
			if (currentBasis.rightArc == null){
				currentBasis.setRightArc(nodeToInsert);
				linkCurrentVertexInfoToLastVertexInfo(nodeToInsert);
			}
			else {
				insertNode(currentBasis.rightArc, nodeToInsert);
			}
		}
		else if (compareKeysResult < 0) {
			if (currentBasis.leftArc == null){
				currentBasis.setLeftArc(nodeToInsert);
				linkCurrentVertexInfoToLastVertexInfo(nodeToInsert);
			}
			else {
				insertNode(currentBasis.leftArc, nodeToInsert);
			}
		}
		else {
			replaceOrAddValue(currentBasis, nodeToInsert);
		}
	}

	private Object getValue(double key, Node currentBasis) {
		if (currentBasis == null) {
			return null;
		}
		int compareKeysResult = compareKeys(key, currentBasis.key, TOLERANCE_AFTER_TRIANGULATION);
		if (compareKeysResult > 0) {
			return getValue(key, currentBasis.rightArc);
		}
		else if (compareKeysResult < 0) {
			return getValue(key, currentBasis.leftArc);
		}
		return currentBasis.value;
	}


	public VertexInfo getVertexInfoBestFitForXYZ(double x, double y, double z, String surfaceId) {
		VertexInfo result = null;
		VertexInfo vertexInfoIterator = firstVertexInfo;
		double distancePow2 = Double.MAX_VALUE;
		double currentDistancePow2;
		while (vertexInfoIterator != null) {
			if (vertexInfoIterator.getTexCoords(surfaceId) != null) {
				currentDistancePow2 = Math.pow(x - (float)vertexInfoIterator.getX(), 2) + 
						Math.pow(y - (float)vertexInfoIterator.getY(), 2) +
						Math.pow(z - (float)vertexInfoIterator.getZ(), 2);
				if (currentDistancePow2 < distancePow2) {
					distancePow2 = currentDistancePow2;
					result = vertexInfoIterator;
				}
			}
			vertexInfoIterator = vertexInfoIterator.getNextVertexInfo();
		}
		if (result == null) {
			result = getVertexInfoBestFitForXYZ(x, y, z);
		}
		return result;
	}

	public VertexInfo getVertexInfoBestFitForXYZ(double x, double y, double z) {
		VertexInfo result = null;
		VertexInfo vertexInfoIterator = firstVertexInfo;
		double distancePow2 = Double.MAX_VALUE;
		double currentDistancePow2;
		while (vertexInfoIterator != null) {
			currentDistancePow2 = Math.pow(x - (float)vertexInfoIterator.getX(), 2) + 
					Math.pow(y - (float)vertexInfoIterator.getY(), 2) +
					Math.pow(z - (float)vertexInfoIterator.getZ(), 2);
			if (currentDistancePow2 < distancePow2) {
				distancePow2 = currentDistancePow2;
				result = vertexInfoIterator;
			}
			vertexInfoIterator = vertexInfoIterator.getNextVertexInfo();
		}
		return result;
	}

	private void replaceOrAddValue(Node currentBasis, Node nodeToInsert) {
		if (nodeToInsert.value instanceof VertexInfo) {
			VertexInfo vertexInfoToInsert = (VertexInfo)nodeToInsert.value;
			if (currentBasis.value == null) { // no vertexInfo yet for this point
				currentBasis.value = nodeToInsert.value;
				linkCurrentVertexInfoToLastVertexInfo(vertexInfoToInsert);
			}
			else {
				vertexIdCounter = vertexIdCounter.subtract(BigInteger.ONE);
				((VertexInfo)currentBasis.value).addTexCoordsFrom(vertexInfoToInsert);
			}
		}
		else { // Node
			insertNode((Node)currentBasis.value, (Node)nodeToInsert.value);
		}
	}

	private void linkCurrentVertexInfoToLastVertexInfo (Node node) {
		while (!(node.value instanceof VertexInfo)) {
			node = (Node)node.value;
		}
		linkCurrentVertexInfoToLastVertexInfo((VertexInfo)node.value);
	}

	private void linkCurrentVertexInfoToLastVertexInfo (VertexInfo currentVertexInfo) {
		lastVertexInfo.setNextVertexInfo(currentVertexInfo);
		lastVertexInfo = currentVertexInfo;
	}

	private int compareKeys (double key1, double key2, double tolerance){
		int result = 0;
		if (Math.abs(key1 - key2) > tolerance) {
			result = key1 > key2 ? 1 : -1;
		}
		return result;
	}

	public void appendObject (KmlGenericObject objectToAppend) {

		VertexInfo vertexInfoIterator = objectToAppend.firstVertexInfo;
		while (vertexInfoIterator != null) {
			if (vertexInfoIterator.getAllTexCoords() == null) {
				this.setVertexInfoForXYZ("-1", // dummy
						vertexInfoIterator.getX(),
						vertexInfoIterator.getY(),
						vertexInfoIterator.getZ(),
						null);
			}
			else {
				Set<String> keySet = vertexInfoIterator.getAllTexCoords().keySet();
				Iterator<String> iterator = keySet.iterator();
				while (iterator.hasNext()) {
					String surfaceId = iterator.next();
					this.setVertexInfoForXYZ(surfaceId,
							vertexInfoIterator.getX(),
							vertexInfoIterator.getY(),
							vertexInfoIterator.getZ(),
							vertexInfoIterator.getTexCoords(surfaceId));
				}
			}
			vertexInfoIterator = vertexInfoIterator.getNextVertexInfo();
		} 

		Set<String> keySet = objectToAppend.geometryInfos.keySet();
		Iterator<String> iterator = keySet.iterator();
		while (iterator.hasNext()) {
			String surfaceId = iterator.next();
			this.addX3dMaterial(surfaceId, objectToAppend.getX3dMaterial(surfaceId));
			String imageUri = objectToAppend.texImageUris.get(surfaceId);
			this.addTexImageUri(surfaceId, imageUri);
			this.addTexImage(imageUri, objectToAppend.getTexImage(imageUri));
			//			this.addTexOrdImage(imageUri, objectToAppend.getTexOrdImage(imageUri));
			this.addGeometryInfo(surfaceId, objectToAppend.geometryInfos.get(surfaceId));
		}

		// adapt id accordingly
		int indexOf_to_ = this.gmlId.indexOf("_to_");
		String ownLowerLimit = "";
		String ownUpperLimit = "";
		if (indexOf_to_ != -1) { // already more than one building in here
			ownLowerLimit = this.gmlId.substring(0, indexOf_to_);
			ownUpperLimit = this.gmlId.substring(indexOf_to_ + 4);
		}
		else {
			ownLowerLimit = this.gmlId;
			ownUpperLimit = ownLowerLimit;
		}

		int btaIndexOf_to_ = objectToAppend.gmlId.indexOf("_to_");
		String btaLowerLimit = "";
		String btaUpperLimit = "";
		if (btaIndexOf_to_ != -1) { // already more than one building in there
			btaLowerLimit = objectToAppend.gmlId.substring(0, btaIndexOf_to_);
			btaUpperLimit = objectToAppend.gmlId.substring(btaIndexOf_to_ + 4);
		}
		else {
			btaLowerLimit = objectToAppend.gmlId;
			btaUpperLimit = btaLowerLimit;
		}

		ownLowerLimit = ownLowerLimit.compareTo(btaLowerLimit)<0 ? ownLowerLimit: btaLowerLimit;
		ownUpperLimit = ownUpperLimit.compareTo(btaUpperLimit)>0 ? ownUpperLimit: btaUpperLimit;

		this.setGmlId(String.valueOf(ownLowerLimit) + "_to_" + ownUpperLimit);
	}


	public void createTextureAtlas(int packingAlgorithm, double imageScaleFactor, boolean pots) throws SQLException, IOException {

		if (texImages.size() < 2 /*&& texOrdImages == null*/) {
			// building has not enough textures or they are in an unknown image format 
			return;
		}

		switch (packingAlgorithm) {
		case -1:
			useInternalTAGenerator(imageScaleFactor, pots);
			break;
		default:
			useExternalTAGenerator(packingAlgorithm, imageScaleFactor, pots);
		}
	}

	private void useExternalTAGenerator(int packingAlgorithm, double scaleFactor, boolean pots) throws SQLException, IOException {

		TextureAtlasGenerator taGenerator = new TextureAtlasGenerator();
		TexImageInfo tiInfo = new TexImageInfo();
		tiInfo.setTexImageURIs(texImageUris);

		HashMap<String, TexImage> tiInfoImages = new HashMap<String, TexImage>();

		Set<String> texImagesSet = texImages.keySet();
		Iterator<String> texImagesIterator = texImagesSet.iterator();
		while (texImagesIterator.hasNext()) {
			String imageName = texImagesIterator.next();
			TexImage image = new TexImage(texImages.get(imageName));
			tiInfoImages.put(imageName, image);
		}

		//		if (texOrdImages != null) {
		//			texImagesSet = texOrdImages.keySet();
		//			texImagesIterator = texImagesSet.iterator();
		//			while (texImagesIterator.hasNext()) {
		//				String imageName = texImagesIterator.next();
		//				TexImage image = new TexImage(texOrdImages.get(imageName));
		//				tiInfoImages.put(imageName, image);
		//			}
		//		}

		tiInfo.setTexImages(tiInfoImages);

		// texture coordinates
		HashMap<Object, String> tiInfoCoords = new HashMap<Object, String>();

		Set<Object> sgIdSet = texImageUris.keySet();
		Iterator<Object> sgIdIterator = sgIdSet.iterator();
		while (sgIdIterator.hasNext()) {
			String sgId = (String) sgIdIterator.next();
			VertexInfo vertexInfoIterator = firstVertexInfo;
			while (vertexInfoIterator != null) {
				if (vertexInfoIterator.getAllTexCoords() != null &&
						vertexInfoIterator.getAllTexCoords().containsKey(sgId)) {
					double s = vertexInfoIterator.getTexCoords(sgId).getS();
					double t = vertexInfoIterator.getTexCoords(sgId).getT();
					String tiInfoCoordsForSgId = tiInfoCoords.get(sgId);
					tiInfoCoordsForSgId = (tiInfoCoordsForSgId == null) ?
							"" :
								tiInfoCoordsForSgId + " ";	
					tiInfoCoords.put(sgId, tiInfoCoordsForSgId + String.valueOf(s) + " " + String.valueOf(t));
				}
				vertexInfoIterator = vertexInfoIterator.getNextVertexInfo();
			}
		} 

		tiInfo.setTexCoordinates(tiInfoCoords);

		taGenerator.setUsePOTS(pots);
		taGenerator.setScaleFactor(scaleFactor);
		tiInfo = taGenerator.convert(tiInfo, packingAlgorithm);

		texImageUris = tiInfo.getTexImageURIs();
		tiInfoImages = tiInfo.getTexImages(); 
		tiInfoCoords = tiInfo.getTexCoordinates();

		texImages.clear();
		//		if (texOrdImages != null) {
		//			texOrdImages.clear();
		//		}

		texImagesSet = tiInfoImages.keySet();
		texImagesIterator = texImagesSet.iterator();
		while (texImagesIterator.hasNext()) {
			String texImageName = texImagesIterator.next();
			TexImage texImage = tiInfoImages.get(texImageName);
			if (texImage.getBufferedImage() != null) {
				texImages.put(texImageName, texImage.getBufferedImage());
			}
			//			else if (texImage.getOrdImage() != null) {
			//				if (texOrdImages == null) {
			//					texOrdImages = new HashMap<String, OrdImage>();
			//				}
			//				texOrdImages.put(texImageName, texImage.getOrdImage());
			//			}
		}

		sgIdIterator = sgIdSet.iterator();
		while (sgIdIterator.hasNext()) {
			String sgId = (String) sgIdIterator.next();
			StringTokenizer texCoordsTokenized = new StringTokenizer(tiInfoCoords.get(sgId), " ");
			VertexInfo vertexInfoIterator = firstVertexInfo;
			while (texCoordsTokenized.hasMoreElements() &&
					vertexInfoIterator != null) {
				if (vertexInfoIterator.getAllTexCoords() != null && 
						vertexInfoIterator.getAllTexCoords().containsKey(sgId)) {
					vertexInfoIterator.getTexCoords(sgId).setS(Double.parseDouble(texCoordsTokenized.nextToken()));
					vertexInfoIterator.getTexCoords(sgId).setT(Double.parseDouble(texCoordsTokenized.nextToken()));
				}
				vertexInfoIterator = vertexInfoIterator.getNextVertexInfo();
			}
		} 
	}

	private void useInternalTAGenerator(double scaleFactor, boolean pots) throws SQLException, IOException {

		if (texImages.size() < 2) {
			// building has not enough textures or they are in an unknown image format 
			return;
		}
		// imageNamesOrderedByImageHeight
		ArrayList<String> inobih = new ArrayList<String>();

		int totalWidth = 0;
		// order images by height		
		Set<String> texImagesSet = texImages.keySet();
		Iterator<String> texImagesIterator = texImagesSet.iterator();
		while (texImagesIterator.hasNext()) {
			String imageName = texImagesIterator.next();
			BufferedImage imageToAdd = texImages.get(imageName);
			int index = 0;
			while (index < inobih.size() 
					&& texImages.get(inobih.get(index)).getHeight() > imageToAdd.getHeight()) {
				index++;
			}
			inobih.add(index, imageName);
			totalWidth = totalWidth + imageToAdd.getWidth();
		}

		// calculate size of texture atlas
		final int TEX_ATLAS_MAX_WIDTH = (int)(totalWidth*scaleFactor/Math.sqrt(inobih.size()));
		int accumulatedWidth = 0;
		int maxWidth = 0;
		int maxHeightForRow = 0;
		int accumulatedHeight = 0;

		for (String imageName: inobih) {
			BufferedImage imageToAdd = texImages.get(imageName);
			if (accumulatedWidth + imageToAdd.getWidth()*scaleFactor > TEX_ATLAS_MAX_WIDTH) { // new row
				maxWidth = Math.max(maxWidth, accumulatedWidth);
				accumulatedHeight = accumulatedHeight + maxHeightForRow;
				accumulatedWidth = 0;
				maxHeightForRow = 0;
			}
			maxHeightForRow = Math.max(maxHeightForRow, (int)(imageToAdd.getHeight()*scaleFactor));
			accumulatedWidth = accumulatedWidth + (int)(imageToAdd.getWidth()*scaleFactor);
		}
		maxWidth = Math.max(maxWidth, accumulatedWidth);
		accumulatedHeight = accumulatedHeight + maxHeightForRow; // add last row

		if (pots) {
			maxWidth = roundUpPots(maxWidth);
			accumulatedHeight = roundUpPots(accumulatedHeight);
		}

		// check the first image as example, is it jpeg or png?
		int type = (texImages.get(inobih.get(0)).getTransparency() == Transparency.OPAQUE) ?
				BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;
		// draw texture atlas
		BufferedImage textureAtlas = new BufferedImage(maxWidth, accumulatedHeight, type);
		Graphics2D g2d = textureAtlas.createGraphics();

		accumulatedWidth = 0;
		maxWidth = 0;
		maxHeightForRow = 0;
		accumulatedHeight = 0;
		HashMap<String, Point> imageOffset = new HashMap<String, Point>();

		for (String imageName: inobih) {
			BufferedImage imageToAdd = texImages.get(imageName);
			if (accumulatedWidth + imageToAdd.getWidth()*scaleFactor > TEX_ATLAS_MAX_WIDTH) { // new row
				maxWidth = Math.max(maxWidth, accumulatedWidth);
				accumulatedHeight = accumulatedHeight + maxHeightForRow;
				accumulatedWidth = 0;
				maxHeightForRow = 0;
			}
			maxHeightForRow = Math.max(maxHeightForRow, (int)(imageToAdd.getHeight()*scaleFactor));
			Point offsetPoint = new Point (accumulatedWidth,
					accumulatedHeight + maxHeightForRow - (int)(imageToAdd.getHeight()*scaleFactor));
			g2d.drawImage(imageToAdd, offsetPoint.x, offsetPoint.y, (int)(imageToAdd.getWidth()*scaleFactor), (int)(imageToAdd.getHeight()*scaleFactor), null);
			imageOffset.put(imageName, offsetPoint);
			accumulatedWidth = accumulatedWidth + (int)(imageToAdd.getWidth()*scaleFactor);
		}


		HashSet<String> wrappedSurfacesSet = new HashSet<String>();
		// transform texture coordinates
		VertexInfo vertexInfoIterator = firstVertexInfo;
		while (vertexInfoIterator != null) {
			if (vertexInfoIterator.getAllTexCoords() != null) {
				Set<String> surfaceIdSet = vertexInfoIterator.getAllTexCoords().keySet();
				Iterator<String> surfaceIdIterator = surfaceIdSet.iterator();
				while (surfaceIdIterator.hasNext()) {
					String surfaceId = surfaceIdIterator.next();
					String imageName = texImageUris.get(surfaceId);
					BufferedImage texImage = texImages.get(imageName);

					if (texImage == null) { // wrapped textures or unknown format images are in texOrdImages
						wrappedSurfacesSet.add(surfaceId);
						continue;
					}

					double s = vertexInfoIterator.getTexCoords(surfaceId).getS();
					double t = vertexInfoIterator.getTexCoords(surfaceId).getT();
					s = (imageOffset.get(imageName).x + (s * texImage.getWidth()*scaleFactor)) / textureAtlas.getWidth();
					// graphics2D coordinates start at the top left point
					// texture coordinates start at the bottom left point
					t = ((textureAtlas.getHeight() - imageOffset.get(imageName).y - texImage.getHeight()*scaleFactor) + 
							t * texImage.getHeight()*scaleFactor) / textureAtlas.getHeight();
					vertexInfoIterator.getTexCoords(surfaceId).setS(s);
					vertexInfoIterator.getTexCoords(surfaceId).setT(t);
				}
			}
			vertexInfoIterator = vertexInfoIterator.getNextVertexInfo();
		} 

		// redirect all non-wrapping, known-formatted texture images to texture atlas
		String textureAtlasName = "textureAtlas_BASIC_" + getGmlId().hashCode() + "_" +
				inobih.get(0).substring(inobih.get(0).lastIndexOf('.'));

		Set<Object> surfaceIdSet = texImageUris.keySet();
		Iterator<Object> surfaceIdIterator = surfaceIdSet.iterator();
		while (surfaceIdIterator.hasNext()) {
			Object surfaceId = surfaceIdIterator.next();
			if (!wrappedSurfacesSet.contains(surfaceId)) {
				texImageUris.put(surfaceId, textureAtlasName);
			}
		}

		// remove all texture images included in texture atlas
		texImages.clear();
		texImages.put(textureAtlasName, textureAtlas);
		g2d.dispose();
	}


	public void resizeAllImagesByFactor (double factor) throws SQLException, IOException {
		if (texImages.size() == 0) { // building has no textures at all
			return;
		}

		Set<String> keySet = texImages.keySet();
		Iterator<String> iterator = keySet.iterator();
		while (iterator.hasNext()) {
			String imageName = iterator.next();
			BufferedImage imageToResize = texImages.get(imageName);
			if (imageToResize.getWidth()*factor < 1 || imageToResize.getHeight()*factor < 1) {
				continue;
			}
			BufferedImage resizedImage = getScaledInstance(imageToResize,
					(int)(imageToResize.getWidth()*factor),
					(int)(imageToResize.getHeight()*factor),
					RenderingHints.VALUE_INTERPOLATION_BILINEAR,
					true);
			texImages.put(imageName, resizedImage);
		}

	}


	/**
	 * Convenience method that returns a scaled instance of the
	 * provided {@code BufferedImage}.
	 *
	 * @param img the original image to be scaled
	 * @param targetWidth the desired width of the scaled instance,
	 *    in pixels
	 * @param targetHeight the desired height of the scaled instance,
	 *    in pixels
	 * @param hint one of the rendering hints that corresponds to
	 *    {@code RenderingHints.KEY_INTERPOLATION} (e.g.
	 *    {@code RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR},
	 *    {@code RenderingHints.VALUE_INTERPOLATION_BILINEAR},
	 *    {@code RenderingHints.VALUE_INTERPOLATION_BICUBIC})
	 * @param higherQuality if true, this method will use a multi-step
	 *    scaling technique that provides higher quality than the usual
	 *    one-step technique (only useful in downscaling cases, where
	 *    {@code targetWidth} or {@code targetHeight} is
	 *    smaller than the original dimensions, and generally only when
	 *    the {@code BILINEAR} hint is specified)
	 * @return a scaled version of the original {@code BufferedImage}
	 */
	private BufferedImage getScaledInstance(BufferedImage img,
			int targetWidth,
			int targetHeight,
			Object hint,
			boolean higherQuality) {

		int type = (img.getTransparency() == Transparency.OPAQUE) ?
				BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;
		BufferedImage ret = (BufferedImage)img;
		int w, h;
		if (higherQuality) {
			// Use multi-step technique: start with original size, then
			// scale down in multiple passes with drawImage()
			// until the target size is reached
			w = img.getWidth();
			h = img.getHeight();
		} 
		else {
			// Use one-step technique: scale directly from original
			// size to target size with a single drawImage() call
			w = targetWidth;
			h = targetHeight;
		}

		do {
			if (higherQuality && w > targetWidth) {
				w /= 2;
				if (w < targetWidth) {
					w = targetWidth;
				}
			}

			if (higherQuality && h > targetHeight) {
				h /= 2;
				if (h < targetHeight) {
					h = targetHeight;
				}
			}

			BufferedImage tmp = new BufferedImage(w, h, type);
			Graphics2D g2 = tmp.createGraphics();
			g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, hint);
			g2.drawImage(ret, 0, 0, w, h, null);
			g2.dispose();

			ret = tmp;
		}
		while (w != targetWidth || h != targetHeight);

		return ret;
	}

	private String buildNameFromX3dMaterial(X3DMaterial x3dMaterial) {
		String name = NO_TEXIMAGE;
		if (x3dMaterial.isSetAmbientIntensity()) { name = name + "_ai_" + x3dMaterial.getAmbientIntensity();}
		if (x3dMaterial.isSetShininess()) { name = name + "_sh_" + x3dMaterial.getShininess();}
		if (x3dMaterial.isSetTransparency()) { name = name + "_tr_" + x3dMaterial.getTransparency();}
		if (x3dMaterial.isSetDiffuseColor()) { name = name + "_dc_r_" + x3dMaterial.getDiffuseColor().getRed()
				+ "_g_" + x3dMaterial.getDiffuseColor().getGreen()
				+ "_b_" + x3dMaterial.getDiffuseColor().getBlue();}
		if (x3dMaterial.isSetSpecularColor()) { name = name + "_sc_r_" + x3dMaterial.getSpecularColor().getRed()
				+ "_g_" + x3dMaterial.getSpecularColor().getGreen()
				+ "_b_" + x3dMaterial.getSpecularColor().getBlue();}
		if (x3dMaterial.isSetEmissiveColor()) { name = name + "_ec_r_" + x3dMaterial.getEmissiveColor().getRed()
				+ "_g_" + x3dMaterial.getEmissiveColor().getGreen()
				+ "_b_" + x3dMaterial.getEmissiveColor().getBlue();}
		return name;
	}

	protected List<Point3d> setOrigins() {
		originZ = Double.MAX_VALUE;
		List<Point3d> coords = new ArrayList<Point3d>();
		VertexInfo vertexInfoIterator = firstVertexInfo;
		while (vertexInfoIterator != null) {
			if (vertexInfoIterator.getZ() < originZ) { // origin must be a point with the lowest z-coordinate
				originX = vertexInfoIterator.getX();
				originY = vertexInfoIterator.getY();
				originZ = vertexInfoIterator.getZ();
				coords.clear();
				Point3d point3d = new Point3d(originX, originY, originZ);
				coords.add(point3d);
			}
			if (vertexInfoIterator.getZ() == originZ) {
				Point3d point3d = new Point3d(vertexInfoIterator.getX(), vertexInfoIterator.getY(), vertexInfoIterator.getZ());
				coords.add(point3d);
			}
			vertexInfoIterator = vertexInfoIterator.getNextVertexInfo();
		}
		return coords;
	}

	protected static double reducePrecisionForXorY (double originalValue) {
		double newValue = originalValue; // + 0.00000005d;
		//		if (decimalDigits != 0) {
		//			double factor = Math.pow(10, decimalDigits);
		double factor = Math.pow(10, 7);
		newValue = Math.rint(newValue*factor);
		newValue = newValue/factor;
		//		}
		return newValue;
	}

	protected static double reducePrecisionForZ (double originalValue) {
		double newValue = originalValue; // + 0.0005d;
		//		if (decimalDigits != 0) {
		//			double factor = Math.pow(10, decimalDigits);
		double factor = Math.pow(10, 4);
		newValue = Math.rint(newValue*factor);
		newValue = newValue/factor;
		//		}
		return newValue;
	}

	protected List<PlacemarkType> createPlacemarksForFootprint(List<BuildingSurface> result, KmlSplittingResult work) throws Exception {

		ResultSet rs = null;
		List<PlacemarkType> placemarkList = new ArrayList<PlacemarkType>();
		PlacemarkType placemark = kmlFactory.createPlacemarkType();
		placemark.setName(work.getGmlId());
		placemark.setId(DisplayForm.FOOTPRINT_PLACEMARK_ID + placemark.getName());

		if (work.getDisplayForm().isHighlightingEnabled()) {
			placemark.setStyleUrl("#" + getStyleBasisName() + DisplayForm.FOOTPRINT_STR + "Style");
		}
		else {
			placemark.setStyleUrl("#" + getStyleBasisName() + DisplayForm.FOOTPRINT_STR + "Normal");
		}

		if (getBalloonSettings().isIncludeDescription()) {
			addBalloonContents(placemark, work.getId());
		}
		MultiGeometryType multiGeometry = kmlFactory.createMultiGeometryType();
		placemark.setAbstractGeometryGroup(kmlFactory.createMultiGeometry(multiGeometry));

		PolygonType polygon = null;
		PolygonType[] multiPolygon = null;
		for (BuildingSurface Row: result) {


			if (Row != null) {
				eventDispatcher.triggerEvent(new GeometryCounterEvent(null, this));

				polygon = kmlFactory.createPolygonType();
				polygon.setTessellate(true);
				polygon.setExtrude(false);
				polygon.setAltitudeModeGroup(kmlFactory.createAltitudeMode(AltitudeModeEnumType.CLAMP_TO_GROUND));


				//***********************************************************************

				@SuppressWarnings("unchecked")
				List<Double> _Geometry = (List<Double>)Row.getGeometry();

				org.postgis.Point[] tmpPoint = new org.postgis.Point[_Geometry.size()/3];			

				for (int i = 1,j = 0; i < _Geometry.size(); j++, i = i+3) {				

					List<Double> Target_Coordinates = ProjConvertor.transformPoint(_Geometry.get(i-1),_Geometry.get(i),_Geometry.get(i+1), work.getTargetSrs(), "4326");							
					tmpPoint[j] = new org.postgis.Point(
							Target_Coordinates.get(1),
							Target_Coordinates.get(0),
							Target_Coordinates.get(2)
							);

				}

				Polygon surface = new Polygon(
						new org.postgis.LinearRing[] {
								new org.postgis.LinearRing(
										tmpPoint
										)
						}
						);


				//***********************************************************************

				Geometry groundSurface = surface;

				switch (groundSurface.getType()) {
				case Geometry.POLYGON:
					Polygon polyGeom = (Polygon) groundSurface;

					for (int ring = 0; ring < polyGeom.numRings(); ring++){					
						LinearRingType linearRing = kmlFactory.createLinearRingType();
						BoundaryType boundary = kmlFactory.createBoundaryType();
						boundary.setLinearRing(linearRing);

						double [] ordinatesArray = new double[polyGeom.getRing(ring).numPoints() * 2];

						for (int j=polyGeom.getRing(ring).numPoints()-1, k=0; j >= 0; j--, k+=2){
							ordinatesArray[k] = polyGeom.getRing(ring).getPoint(j).x;
							ordinatesArray[k+1] = polyGeom.getRing(ring).getPoint(j).y;
						}

						// the first ring usually is the outer ring in a PostGIS-Polygon
						// e.g. POLYGON((outerBoundary),(innerBoundary),(innerBoundary))
						if (ring == 0){
							polygon.setOuterBoundaryIs(boundary);
							for (int j = 0; j < ordinatesArray.length; j+=2) {
								linearRing.getCoordinates().add(String.valueOf(ordinatesArray[j] + "," + ordinatesArray[j+1] + ",0"));
							}
						}
						else {
							polygon.getInnerBoundaryIs().add(boundary);
							for (int j = ordinatesArray.length - 2; j >= 0; j-=2) {
								linearRing.getCoordinates().add(String.valueOf(ordinatesArray[j] + "," + ordinatesArray[j+1] + ",0"));
							}	
						}
					}				
					break;

				case Geometry.MULTIPOLYGON:
					MultiPolygon multiPolyGeom = (MultiPolygon) groundSurface;
					multiPolygon = new PolygonType[multiPolyGeom.numPolygons()]; 

					for (int p = 0; p < multiPolyGeom.numPolygons(); p++){
						Polygon subPolyGeom = multiPolyGeom.getPolygon(p);

						multiPolygon[p] = kmlFactory.createPolygonType();
						multiPolygon[p].setTessellate(true);
						multiPolygon[p].setExtrude(true);
						multiPolygon[p].setAltitudeModeGroup(kmlFactory.createAltitudeMode(AltitudeModeEnumType.RELATIVE_TO_GROUND));

						for (int ring = 0; ring < subPolyGeom.numRings(); ring++){
							LinearRingType linearRing = kmlFactory.createLinearRingType();
							BoundaryType boundary = kmlFactory.createBoundaryType();
							boundary.setLinearRing(linearRing);

							double [] ordinatesArray = new double[subPolyGeom.getRing(ring).numPoints() * 2];

							for (int j=subPolyGeom.getRing(ring).numPoints()-1, k=0; j >= 0; j--, k+=2){
								ordinatesArray[k] = subPolyGeom.getRing(ring).getPoint(j).x;
								ordinatesArray[k+1] = subPolyGeom.getRing(ring).getPoint(j).y;
							}

							// the first ring usually is the outer ring in a PostGIS-Polygon
							// e.g. POLYGON((outerBoundary),(innerBoundary),(innerBoundary))
							if (ring == 0){
								multiPolygon[p].setOuterBoundaryIs(boundary);
								for (int j = 0; j < ordinatesArray.length; j+=2) {
									linearRing.getCoordinates().add(String.valueOf(ordinatesArray[j] + "," + ordinatesArray[j+1] + ",0"));
								}
							}
							else {
								multiPolygon[p].getInnerBoundaryIs().add(boundary);
								for (int j = ordinatesArray.length - 2; j >= 0; j-=2) {
									linearRing.getCoordinates().add(String.valueOf(ordinatesArray[j] + "," + ordinatesArray[j+1] + ",0"));
								}	
							}
						}
					}
				case Geometry.POINT:
				case Geometry.LINESTRING:
				case Geometry.MULTIPOINT:
				case Geometry.MULTILINESTRING:
				case Geometry.GEOMETRYCOLLECTION:
					continue;
				default:
					Logger.getInstance().warn("Unknown geometry for " + work.getGmlId());
					continue;
				}

				if (polygon != null){
					multiGeometry.getAbstractGeometryGroup().add(kmlFactory.createPolygon(polygon));
				}

				if (multiPolygon != null){
					for (int p = 0; p < multiPolygon.length; p++){
						multiGeometry.getAbstractGeometryGroup().add(kmlFactory.createPolygon(multiPolygon[p]));
					}
				}
			}
		}
		// if there is at least some content
		if (polygon != null) { 
			placemarkList.add(placemark);
		}
		return placemarkList;
	}

	protected List<PlacemarkType> createPlacemarksForExtruded(List<BuildingSurface> result,
			KmlSplittingResult work,
			double measuredHeight,
			boolean reversePointOrder) throws Exception {


		List<PlacemarkType> placemarkList = new ArrayList<PlacemarkType>();
		PlacemarkType placemark = kmlFactory.createPlacemarkType();
		placemark.setName(work.getGmlId());
		placemark.setId(DisplayForm.EXTRUDED_PLACEMARK_ID + placemark.getName());
		if (work.getDisplayForm().isHighlightingEnabled()) {
			placemark.setStyleUrl("#" + getStyleBasisName() + DisplayForm.EXTRUDED_STR + "Style");
		}
		else {
			placemark.setStyleUrl("#" + getStyleBasisName() + DisplayForm.EXTRUDED_STR + "Normal");
		}
		if (getBalloonSettings().isIncludeDescription()) {
			addBalloonContents(placemark, work.getId());
		}
		MultiGeometryType multiGeometry = kmlFactory.createMultiGeometryType();
		placemark.setAbstractGeometryGroup(kmlFactory.createMultiGeometry(multiGeometry));

		PolygonType polygon = null;
		PolygonType[] multiPolygon = null;

		for (BuildingSurface Row: result) {


			if (Row != null) {
				eventDispatcher.triggerEvent(new GeometryCounterEvent(null, this));

				polygon = kmlFactory.createPolygonType();
				polygon.setTessellate(true);
				polygon.setExtrude(true);
				polygon.setAltitudeModeGroup(kmlFactory.createAltitudeMode(AltitudeModeEnumType.RELATIVE_TO_GROUND));


				//***********************************************************************

				@SuppressWarnings("unchecked")
				List<Double> _Geometry = (List<Double>)Row.getGeometry();

				org.postgis.Point[] tmpPoint = new org.postgis.Point[_Geometry.size()/3];			

				for (int i = 1,j = 0; i < _Geometry.size(); j++, i = i+3) {				

					List<Double> Target_Coordinates = ProjConvertor.transformPoint(_Geometry.get(i-1),_Geometry.get(i),_Geometry.get(i+1), work.getTargetSrs(), "4326");							
					tmpPoint[j] = new org.postgis.Point(
							Target_Coordinates.get(1),
							Target_Coordinates.get(0),
							Target_Coordinates.get(2)
							);

				}

				Polygon surface = new Polygon(
						new org.postgis.LinearRing[] {
								new org.postgis.LinearRing(
										tmpPoint
										)
						}
						);


				//***********************************************************************

				Geometry groundSurface = surface;

				switch (groundSurface.getType()) {
				case Geometry.POLYGON:
					Polygon polyGeom = (Polygon) groundSurface;
					for (int ring = 0; ring < polyGeom.numRings(); ring++){

						LinearRingType linearRing = kmlFactory.createLinearRingType();
						BoundaryType boundary = kmlFactory.createBoundaryType();
						boundary.setLinearRing(linearRing);

						double [] ordinatesArray = new double[polyGeom.getRing(ring).numPoints() * 2];

						if (reversePointOrder) {
							for (int j=polyGeom.getRing(ring).numPoints()-1, k=0; j >= 0; j--, k+=2){
								ordinatesArray[k] = polyGeom.getRing(ring).getPoint(j).x;
								ordinatesArray[k+1] = polyGeom.getRing(ring).getPoint(j).y;
							}
						}
						else {
							for (int j=0, k=0; j < polyGeom.getRing(ring).numPoints(); j++, k+=2){
								ordinatesArray[k] = polyGeom.getRing(ring).getPoint(j).x;
								ordinatesArray[k+1] = polyGeom.getRing(ring).getPoint(j).y;
							}							
						}

						// the first ring usually is the outer ring in a PostGIS-Polygon
						// e.g. POLYGON((outerBoundary),(innerBoundary),(innerBoundary))
						if (ring == 0){
							polygon.setOuterBoundaryIs(boundary);
							for (int j = 0; j < ordinatesArray.length; j+=2) {
								linearRing.getCoordinates().add(String.valueOf(ordinatesArray[j] + "," + ordinatesArray[j+1] + "," + measuredHeight));
							}
						}
						else {
							polygon.getInnerBoundaryIs().add(boundary);
							for (int j = ordinatesArray.length - 2; j >= 0; j-=2) {
								linearRing.getCoordinates().add(String.valueOf(ordinatesArray[j] + "," + ordinatesArray[j+1] + "," + measuredHeight));
							}	
						}
					}				
					break;
				case Geometry.MULTIPOLYGON:
					MultiPolygon multiPolyGeom = (MultiPolygon) groundSurface;
					multiPolygon = new PolygonType[multiPolyGeom.numPolygons()]; 

					for (int p = 0; p < multiPolyGeom.numPolygons(); p++){
						Polygon subPolyGeom = multiPolyGeom.getPolygon(p);

						multiPolygon[p] = kmlFactory.createPolygonType();
						multiPolygon[p].setTessellate(true);
						multiPolygon[p].setExtrude(true);
						multiPolygon[p].setAltitudeModeGroup(kmlFactory.createAltitudeMode(AltitudeModeEnumType.RELATIVE_TO_GROUND));

						for (int ring = 0; ring < subPolyGeom.numRings(); ring++){
							LinearRingType linearRing = kmlFactory.createLinearRingType();
							BoundaryType boundary = kmlFactory.createBoundaryType();
							boundary.setLinearRing(linearRing);

							double [] ordinatesArray = new double[subPolyGeom.getRing(ring).numPoints() * 2];

							for (int j=subPolyGeom.getRing(ring).numPoints()-1, k=0; j >= 0; j--, k+=2){
								ordinatesArray[k] = subPolyGeom.getRing(ring).getPoint(j).x;
								ordinatesArray[k+1] = subPolyGeom.getRing(ring).getPoint(j).y;
							}

							// the first ring usually is the outer ring in a PostGIS-Polygon
							// e.g. POLYGON((outerBoundary),(innerBoundary),(innerBoundary))
							if (ring == 0){
								multiPolygon[p].setOuterBoundaryIs(boundary);
								for (int j = 0; j < ordinatesArray.length; j+=2) {
									linearRing.getCoordinates().add(String.valueOf(ordinatesArray[j] + "," + ordinatesArray[j+1] + ",0"));
								}
							}
							else {
								multiPolygon[p].getInnerBoundaryIs().add(boundary);
								for (int j = ordinatesArray.length - 2; j >= 0; j-=2) {
									linearRing.getCoordinates().add(String.valueOf(ordinatesArray[j] + "," + ordinatesArray[j+1] + ",0"));
								}	
							}
						}
					}
				case Geometry.POINT:
				case Geometry.LINESTRING:
				case Geometry.MULTIPOINT:
				case Geometry.MULTILINESTRING:
				case Geometry.GEOMETRYCOLLECTION:
					continue;
				default:
					Logger.getInstance().warn("Unknown geometry for " + work.getGmlId());
					continue;
				}

				if (polygon != null){
					multiGeometry.getAbstractGeometryGroup().add(kmlFactory.createPolygon(polygon));
				}

				if (multiPolygon != null){
					for (int p = 0; p < multiPolygon.length; p++){
						multiGeometry.getAbstractGeometryGroup().add(kmlFactory.createPolygon(multiPolygon[p]));
					}
				}
			}
		}
		if (polygon != null) { // if there is at least some content
			placemarkList.add(placemark);
		}
		return placemarkList;
	}

	protected List<PlacemarkType> createPlacemarksForGeometry(List<BuildingSurface> rs,
			KmlSplittingResult work) throws Exception{

		return createPlacemarksForGeometry(rs, work, false, false);

	}

	protected List<PlacemarkType> createPlacemarksForGeometry(List<BuildingSurface> result,
			KmlSplittingResult work,
			boolean includeGroundSurface,
			boolean includeClosureSurface) throws Exception {


		HashMap<String, MultiGeometryType> multiGeometries = new HashMap<String, MultiGeometryType>();
		MultiGeometryType multiGeometry = null;
		PolygonType polygon = null;

		double zOffset = getZOffsetFromDB(work.getGmlId(),work.GetElevation());
		if (zOffset == Double.MAX_VALUE) {
			List<Point3d> lowestPointCandidates = getLowestPointsCoordinates(result,  work);
			zOffset = getZOffsetFromGEService(work.getGmlId(),lowestPointCandidates,work.getTargetSrs(),work.GetElevation());
		}

		for (BuildingSurface Row: result) {
			
			
			String _SurfaceData = Row.getId();

			String surfaceType = (String)Row.getType();
			if (surfaceType != null && !surfaceType.endsWith("Surface")) {
				surfaceType = surfaceType + "Surface";
			}

			if ((!includeGroundSurface && TypeAttributeValueEnum.fromCityGMLClass(CityGMLClass.GROUND_SURFACE).toString().equalsIgnoreCase(surfaceType)) ||
					(!includeClosureSurface && TypeAttributeValueEnum.fromCityGMLClass(CityGMLClass.CLOSURE_SURFACE).toString().equalsIgnoreCase(surfaceType)))	{
				continue;
			}


			@SuppressWarnings("unchecked")
			List<Double> _Geometry = (List<Double>)Row.getGeometry();

			org.postgis.Point[] tmpPoint = new org.postgis.Point[_Geometry.size()/3];			

			for (int i = 1,j = 0; i < _Geometry.size(); j++, i = i+3) {				

				List<Double> Target_Coordinates = ProjConvertor.transformPoint(_Geometry.get(i-1),_Geometry.get(i),_Geometry.get(i+1), work.getTargetSrs(), "4326");							
				tmpPoint[j] = new org.postgis.Point(
						Target_Coordinates.get(1),
						Target_Coordinates.get(0),
						Target_Coordinates.get(2)
						);
			}

			Polygon surface = new Polygon(
					new org.postgis.LinearRing[] {
							new org.postgis.LinearRing(
									tmpPoint
									)
					}
					);




			double[] ordinatesArray = new double[surface.numPoints()*3];

			for (int i = 0, j = 0; i < surface.numPoints(); i++, j+=3){
				ordinatesArray[j] = surface.getPoint(i).x;
				ordinatesArray[j+1] = surface.getPoint(i).y;
				ordinatesArray[j+2] = surface.getPoint(i).z;
			}



			eventDispatcher.triggerEvent(new GeometryCounterEvent(null, this));

			polygon = kmlFactory.createPolygonType();

			switch (config.getProject().getKmlExporter().getAltitudeMode()) {

			case ABSOLUTE:
				polygon.setAltitudeModeGroup(kmlFactory.createAltitudeMode(AltitudeModeEnumType.ABSOLUTE));
				break;
			case RELATIVE:
				polygon.setAltitudeModeGroup(kmlFactory.createAltitudeMode(AltitudeModeEnumType.RELATIVE_TO_GROUND));
				break;
			}




			// just in case surfaceType == null
			boolean probablyRoof = true;
			double nx = 0;
			double ny = 0;
			double nz = 0;
			int cellCount = 0;

			for (int i = 0; i < surface.numRings(); i++){
				LinearRingType linearRing = kmlFactory.createLinearRingType();
				BoundaryType boundary = kmlFactory.createBoundaryType();
				boundary.setLinearRing(linearRing);
				if (i == 0) {
					polygon.setOuterBoundaryIs(boundary);
				}
				else {
					polygon.getInnerBoundaryIs().add(boundary);
				}

				int startNextRing = ((i+1) < surface.numRings()) ? 
						(surface.getRing(i).numPoints()*3): // still holes to come
							ordinatesArray.length; // default

						// order points clockwise
						for (int j = cellCount; j < startNextRing; j+=3) {
							linearRing.getCoordinates().add(String.valueOf(reducePrecisionForXorY(ordinatesArray[j]) + "," 
									+ reducePrecisionForXorY(ordinatesArray[j+1]) + ","
									+ reducePrecisionForZ(ordinatesArray[j+2] + zOffset)));


							//		probablyRoof = probablyRoof && (reducePrecisionForZ(ordinatesArray[j+2] - lowestZCoordinate) > 0);
							// not touching the ground

							if (currentLod == 1) { // calculate normal
								int current = j;
								int next = j+3;
								if (next >= ordinatesArray.length) next = 0;
								nx = nx + ((ordinatesArray[current+1] - ordinatesArray[next+1]) * (ordinatesArray[current+2] + ordinatesArray[next+2])); 
								ny = ny + ((ordinatesArray[current+2] - ordinatesArray[next+2]) * (ordinatesArray[current] + ordinatesArray[next])); 
								nz = nz + ((ordinatesArray[current] - ordinatesArray[next]) * (ordinatesArray[current+1] + ordinatesArray[next+1]));
							}
						}	
						cellCount += (surface.getRing(i).numPoints()*3);
			}

			if (currentLod == 1) { // calculate normal
				double value = Math.sqrt(nx * nx + ny * ny + nz * nz);
				if (value == 0) { // not a surface, but a line
					continue;
				}
				nx = nx / value;
				ny = ny / value;
				nz = nz / value;
			}

			if (surfaceType == null) {
				surfaceType = TypeAttributeValueEnum.fromCityGMLClass(CityGMLClass.WALL_SURFACE).toString();
				switch (currentLod) {
				case 1:
					if (probablyRoof && (nz > 0.999)) {
						surfaceType = TypeAttributeValueEnum.fromCityGMLClass(CityGMLClass.ROOF_SURFACE).toString();
					}
					break;
				case 2:
					if (probablyRoof) {
						surfaceType = TypeAttributeValueEnum.fromCityGMLClass(CityGMLClass.ROOF_SURFACE).toString();
					}
					break;
				}
			}

			multiGeometry = multiGeometries.get(surfaceType);
			if (multiGeometry == null) {
				multiGeometry = kmlFactory.createMultiGeometryType();
				multiGeometries.put(surfaceType, multiGeometry);
			}
			multiGeometry.getAbstractGeometryGroup().add(kmlFactory.createPolygon(polygon));

		}

		List<PlacemarkType> placemarkList = new ArrayList<PlacemarkType>();
		Set<String> keySet = multiGeometries.keySet();
		Iterator<String> iterator = keySet.iterator();
		while (iterator.hasNext()) {
			String surfaceType = iterator.next();
			PlacemarkType placemark = kmlFactory.createPlacemarkType();
			placemark.setName(work.getGmlId() + "_" + surfaceType);
			placemark.setId(DisplayForm.GEOMETRY_PLACEMARK_ID + placemark.getName());
			if (work.isBuilding())
				placemark.setStyleUrl("#" + surfaceType + "Normal");
			else
				placemark.setStyleUrl("#" + getStyleBasisName() + DisplayForm.GEOMETRY_STR + "Normal");
			if (getBalloonSettings().isIncludeDescription() &&
					!work.getDisplayForm().isHighlightingEnabled()) { // avoid double description
				addBalloonContents(placemark, work.getId());
			}
			multiGeometry = multiGeometries.get(surfaceType);
			placemark.setAbstractGeometryGroup(kmlFactory.createMultiGeometry(multiGeometry));
			placemarkList.add(placemark);
		}
		return placemarkList;
	}

	protected void fillGenericObjectForCollada(KmlSplittingResult work , List<BuildingSurface> _SurfaceList ,
			SurfaceAppearance _SurfaceAppearance, List<BuildingSurface> _ParentSurfaceList) throws Exception {	
		
		String selectedTheme = config.getProject().getCityKmlExporter().getAppearanceTheme();
		String filePath=GetImagePath();
		int texImageCounter = 0;

		try {

				for(BuildingSurface Row:_ParentSurfaceList){

					String parentid= String.valueOf(Row.getPId());
					String id = Row.getId();
				
					Map<String, Object> tmpResult = _SurfaceAppearance.GetAppearanceBySurfaceID("#" + id , work.getAppearanceList() , selectedTheme);
					String AppreanceType = (String)tmpResult.get("type");
					
					
					if(AppreanceType != null){
				
						if(AppreanceType.equals("X3D_MATERIAL"))
						{
							X3DMaterial x3dMaterial = cityGMLFactory.createX3DMaterial();
							fillX3dMaterialValues(x3dMaterial, tmpResult);
							addX3dMaterial(parentid, x3dMaterial);

						}
					}
				}

				for (BuildingSurface Row: _SurfaceList)  {

					Map<String, Object> _AppResult = _SurfaceAppearance.GetAppearanceBySurfaceID("#" + Row.getId() , work.getAppearanceList() , selectedTheme);
									
					String surfaceId = Row.getId();
					String parentId = String.valueOf(Row.getPId());
					
					// from here on it is a surfaceMember
					eventDispatcher.triggerEvent(new GeometryCounterEvent(null, this));

					String texImageUri = null;
					//						OrdImage texImage = null;
					InputStream texImage = null;
					//						byte buf[] = null;
					StringTokenizer texCoordsTokenized = null;
									
					if (_AppResult.get("type") == null) {
										
						if(getX3dMaterial(parentId) != null)  {
							
							addX3dMaterial(surfaceId, getX3dMaterial(parentId));								
						}
						else {	
							
							if (getX3dMaterial(surfaceId) == null) {
								
								defaultX3dMaterial = cityGMLFactory.createX3DMaterial();
								defaultX3dMaterial.setAmbientIntensity(0.2d);
								defaultX3dMaterial.setShininess(0.2d);
								defaultX3dMaterial.setTransparency(0d);
								defaultX3dMaterial.setDiffuseColor(getX3dColorFromString("0.8 0.8 0.8"));
								defaultX3dMaterial.setSpecularColor(getX3dColorFromString("1.0 1.0 1.0"));
								defaultX3dMaterial.setEmissiveColor(getX3dColorFromString("0.0 0.0 0.0"));
								addX3dMaterial(surfaceId, defaultX3dMaterial);							
								
							}
						}				
					}
					else{


						texImageUri = (_AppResult.get("imageuri") != null) ? _AppResult.get("imageuri").toString() : null;	
						String texCoords = (_AppResult.get("coord") != null) ? _AppResult.get("coord").toString() : null;
						
						if (texImageUri != null && texImageUri.trim().length() != 0 &&  texCoords != null && texCoords.trim().length() != 0) {
							
							String finalImagePath = filePath + "\\" + texImageUri;
							int fileSeparatorIndex = Math.max(texImageUri.lastIndexOf("\\"), texImageUri.lastIndexOf("/")); 
							texImageUri = "_" + texImageUri.substring(fileSeparatorIndex + 1);

							addTexImageUri(surfaceId, texImageUri);
							
							if (getTexImage(texImageUri) == null) {

								texImage = new BufferedInputStream(new FileInputStream(finalImagePath));
								
								BufferedImage bufferedImage = null;

								try {
									
									bufferedImage = ImageIO.read(texImage);
								}
								catch (IOException ioe) {}

								if (bufferedImage != null) { // image in JPEG, PNG or another usual format
									addTexImage(texImageUri, bufferedImage);
								}
								
								texImageCounter++;
								if (texImageCounter > 20) {
									eventDispatcher.triggerEvent(new CounterEvent(CounterType.TEXTURE_IMAGE, texImageCounter, this));
									texImageCounter = 0;
								}
							}

							texCoords = texCoords.replaceAll(";", " "); // substitute of ; for internal ring
							texCoordsTokenized = new StringTokenizer(texCoords.trim(), " ");
						}
						else {
							X3DMaterial x3dMaterial = cityGMLFactory.createX3DMaterial();
							fillX3dMaterialValues(x3dMaterial, _AppResult);
							
							// x3dMaterial will only added if not all x3dMaterial members are null
							addX3dMaterial(surfaceId, x3dMaterial);
							if (getX3dMaterial(surfaceId) == null) {
								// untextured surface and no x3dMaterial -> default x3dMaterial (gray)
								addX3dMaterial(surfaceId, defaultX3dMaterial);

							}
						}
					}

					@SuppressWarnings("unchecked")
					List<Double> _Geometry = (List<Double>)Row.getGeometry();

					org.postgis.Point[] tmpPoint = new org.postgis.Point[_Geometry.size()/3];			

					for (int i = 1,j = 0; i < _Geometry.size(); j++, i = i+3) {				

						List<Double> Target_Coordinates = ProjConvertor.transformPoint(_Geometry.get(i-1),_Geometry.get(i),_Geometry.get(i+1), work.getTargetSrs(), work.getTargetSrs());							
						tmpPoint[j] = new org.postgis.Point(
								Target_Coordinates.get(1),
								Target_Coordinates.get(0),
								Target_Coordinates.get(2)
								);

					}

					Polygon surface = new Polygon(
							new org.postgis.LinearRing[] {									
									new org.postgis.LinearRing(tmpPoint)
							});

					double[] ordinatesArray = new double[surface.numPoints()*3];
					for (int i = 0, j = 0; i < surface.numPoints(); i++, j+=3){
						ordinatesArray[j] = surface.getPoint(i).x;
						ordinatesArray[j+1] = surface.getPoint(i).y;
						ordinatesArray[j+2] = surface.getPoint(i).z;
					}



					GeometryInfo gi = new GeometryInfo(GeometryInfo.POLYGON_ARRAY);
					int contourCount = surface.numRings();
					int cellCount = 0;
					// last point of polygons in gml is identical to first and useless for GeometryInfo
					double[] giOrdinatesArray = new double[ordinatesArray.length - (contourCount*3)];

					int[] stripCountArray = new int[contourCount];
					int[] countourCountArray = {contourCount};

					for (int currentContour = 1; currentContour <= contourCount; currentContour++) {
						int startOfCurrentRing = cellCount;
						cellCount += (surface.getRing(currentContour-1).numPoints()*3);
						int startOfNextRing = (currentContour == contourCount) ? 
								ordinatesArray.length: // last
									cellCount; // still holes to come

						for (int j = startOfCurrentRing; j < startOfNextRing - 3; j+=3) {

							giOrdinatesArray[(j-(currentContour-1)*3)] = ordinatesArray[j] * 100; // trick for very close coordinates
							giOrdinatesArray[(j-(currentContour-1)*3)+1] = ordinatesArray[j+1] * 100;
							giOrdinatesArray[(j-(currentContour-1)*3)+2] = ordinatesArray[j+2] * 100;

							TexCoords texCoordsForThisSurface = null;
							if (texCoordsTokenized != null && texCoordsTokenized.hasMoreTokens()) {
								double s = Double.parseDouble(texCoordsTokenized.nextToken());
								double t = Double.parseDouble(texCoordsTokenized.nextToken());

								/*									if (s > 1.1 || s < -0.1 || t < -0.1 || t > 1.1) { // texture wrapping -- it conflicts with texture atlas
										removeTexImage(texImageUri);
										BufferedImage bufferedImage = null;
										try {
											bufferedImage = ImageIO.read(texImage);
										} catch (IOException e) {}
										addTexImage(texImageUri, bufferedImage);
//										addTexOrdImage(texImageUri, texImage);
									}
								 */
								texCoordsForThisSurface = new TexCoords(s, t);
							}
							setVertexInfoForXYZ(surfaceId,
									giOrdinatesArray[(j-(currentContour-1)*3)],
									giOrdinatesArray[(j-(currentContour-1)*3)+1],
									giOrdinatesArray[(j-(currentContour-1)*3)+2],
									texCoordsForThisSurface);
						}
						stripCountArray[currentContour-1] = (startOfNextRing -3 - startOfCurrentRing)/3;
						if (texCoordsTokenized != null  && texCoordsTokenized.hasMoreTokens()) {
							texCoordsTokenized.nextToken(); // geometryInfo ignores last point in a polygon
							texCoordsTokenized.nextToken(); // keep texture coordinates in sync
						}
					}


		
					gi.setCoordinates(giOrdinatesArray);
					gi.setContourCounts(countourCountArray);
					gi.setStripCounts(stripCountArray);
					addGeometryInfo(surfaceId, gi);
					
				}
			//}
		}
		catch (Exception Ex) {
			Logger.getInstance().error("The error while querying city object: " + Ex.toString());
		}
		finally {

		}

		//	}

		// count rest images
		eventDispatcher.triggerEvent(new CounterEvent(CounterType.TEXTURE_IMAGE, texImageCounter, this));
	}

	public PlacemarkType createPlacemarkForColladaModel(KmlSplittingResult work) throws Exception {

		PlacemarkType placemark = kmlFactory.createPlacemarkType();
		placemark.setName(getGmlId());
		placemark.setId(DisplayForm.COLLADA_PLACEMARK_ID + placemark.getName());

		DisplayForm colladaDisplayForm = null;
		for (DisplayForm displayForm: getDisplayForms()) {
			if (displayForm.getForm() == DisplayForm.COLLADA) {
				colladaDisplayForm = displayForm;
				break;
			}
		}

		if (getBalloonSettings().isIncludeDescription() 
				&& !colladaDisplayForm.isHighlightingEnabled()) { // avoid double description

			ColladaOptions colladaOptions = getColladaOptions();
			if (!colladaOptions.isGroupObjects() || colladaOptions.getGroupSize() == 1) {
				addBalloonContents(placemark, getId());
			}
		}

		ModelType model = kmlFactory.createModelType();
		LocationType location = kmlFactory.createLocationType();

		switch (config.getProject().getKmlExporter().getAltitudeMode()) {
		case ABSOLUTE:
			model.setAltitudeModeGroup(kmlFactory.createAltitudeMode(AltitudeModeEnumType.ABSOLUTE));
			break;
		case RELATIVE:
			model.setAltitudeModeGroup(kmlFactory.createAltitudeMode(AltitudeModeEnumType.RELATIVE_TO_GROUND));
			break;
		}

		location.setLatitude(getLocationY());
		location.setLongitude(getLocationX());
		location.setAltitude(getLocationZ() + reducePrecisionForZ(getZOffset()));
		model.setLocation(location);

		// correct heading value
		double lat1 = Math.toRadians(getLocationY());
		// undo trick for very close coordinates
		List<Double> dummy = ProjConvertor.transformPoint(getOriginX()/100, getOriginY()/100-20 , getOriginZ()/100, work.getTargetSrs(), "4326");
		double lat2 = Math.toRadians(dummy.get(0));
		double dLon = Math.toRadians(dummy.get(1) - getLocationX());
		double y = Math.sin(dLon) * Math.cos(lat2);
		double x = Math.cos(lat1)*Math.sin(lat2) - Math.sin(lat1)*Math.cos(lat2)*Math.cos(dLon);
		double bearing = Math.toDegrees(Math.atan2(y, x));
		bearing = (bearing + 180) % 360;
		
		

		OrientationType orientation = kmlFactory.createOrientationType();
		orientation.setHeading(reducePrecisionForZ(bearing));
		model.setOrientation(orientation);

		LinkType link = kmlFactory.createLinkType();
		if (config.getProject().getKmlExporter().isOneFilePerObject() &&
				!config.getProject().getKmlExporter().isExportAsKmz() &&
				config.getProject().getKmlExporter().getFilter().getComplexFilter().getTiledBoundingBox().getActive().booleanValue())
		{
			link.setHref(getGmlId() + ".dae");
		}
		else {
			// File.separator would be wrong here, it MUST be "/"
			link.setHref(getGmlId() + "/" + getGmlId() + ".dae");
		}
		model.setLink(link);

		placemark.setAbstractGeometryGroup(kmlFactory.createModel(model));
		return placemark;
	}


	protected List<PlacemarkType> createPlacemarksForHighlighting(List<BuildingSurface> result, 
			KmlSplittingResult work) throws SQLException {

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
				getGeometriesStmt.setLong(i, work.getId());
			}
			rs = getGeometriesStmt.executeQuery();

			double zOffset = getZOffsetFromDB(work.getGmlId(),work.GetElevation());
			if (zOffset == Double.MAX_VALUE) {
				List<Point3d> lowestPointCandidates = getLowestPointsCoordinates(result,  work);
				rs.beforeFirst(); // return cursor to beginning
				zOffset = getZOffsetFromGEService(work.getGmlId(),lowestPointCandidates,work.getTargetSrs(),work.GetElevation());
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
				switch (config.getProject().getKmlExporter().getAltitudeMode()) {
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
					if (i == 0) {
						polygon.setOuterBoundaryIs(boundary);
					}
					else {
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

	private String getBalloonContentFromGenericAttribute(long id) {

		String balloonContent = null;
		String genericAttribName = "Balloon_Content"; 
		PreparedStatement selectQuery = null;
		ResultSet rs = null;

		try {
			// look for the value in the DB
			selectQuery = connection.prepareStatement(Queries.GET_STRVAL_GENERICATTRIB_FROM_ID);
			selectQuery.setLong(1, id);
			selectQuery.setString(2, genericAttribName);
			rs = selectQuery.executeQuery();
			if (rs.next()) {
				balloonContent = rs.getString(1);
			}
		}
		catch (Exception e) {}
		finally {
			try {
				if (rs != null) rs.close();
				if (selectQuery != null) selectQuery.close();
			}
			catch (Exception e2) {}
		}
		return balloonContent;
	}

	protected void addBalloonContents(PlacemarkType placemark, long id) {
		try {
			switch (getBalloonSettings().getBalloonContentMode()) {
			case GEN_ATTRIB:
				String balloonTemplate = getBalloonContentFromGenericAttribute(id);
				if (balloonTemplate != null) {
					if (balloonTemplateHandler == null) { // just in case
						balloonTemplateHandler = new BalloonTemplateHandlerImpl((File) null, connection);
					}
					placemark.setDescription(balloonTemplateHandler.getBalloonContent(balloonTemplate, id, currentLod));
				}
				break;
			case GEN_ATTRIB_AND_FILE:
				balloonTemplate = getBalloonContentFromGenericAttribute(id);
				if (balloonTemplate != null) {
					placemark.setDescription(balloonTemplateHandler.getBalloonContent(balloonTemplate, id, currentLod));
					break;
				}
			case FILE :
				if (balloonTemplateHandler != null) {
					placemark.setDescription(balloonTemplateHandler.getBalloonContent(id, currentLod));
				}
				break;
			}
		}
		catch (Exception e) { } // invalid balloons are silently discarded
	}

	protected void fillX3dMaterialValues (X3DMaterial x3dMaterial, Map<String, Object> rs) throws SQLException {
		

		Double ambientIntensity = (Double)rs.get("x3d_ambient_intensity");
	
		if (ambientIntensity!=null) {
			x3dMaterial.setAmbientIntensity(ambientIntensity);
		}
		
		
		Double shininess = (Double)rs.get("x3d_shininess");
		if (shininess!=null) {
			x3dMaterial.setShininess(shininess);
		}
		
		
		Double transparency = (Double)rs.get("x3d_transparency");
		if (transparency != null) {
			x3dMaterial.setTransparency(transparency);
		}
		
		
		Color color = (Color)rs.get("x3d_diffuse_color");
		if (color != null) {
			x3dMaterial.setDiffuseColor(color);
	}
		
		color = (Color)rs.get("x3d_specular_color");
		if (color != null) {
			x3dMaterial.setSpecularColor(color);
			
		}
	
		
		color = (Color)rs.get("x3d_emissive_color");
		if (color != null) {
			x3dMaterial.setEmissiveColor(color);
		}

		
		x3dMaterial.setIsSmooth((boolean)rs.get("x3d_is_smooth") == true);
	
		
	}

	private Color getX3dColorFromString(String colorString) {
		Color color = null;
		if (colorString != null) {
			List<Double> colorList = Util.string2double(colorString, "\\s+");

			if (colorList != null && colorList.size() >= 3) {
				color = cityGMLFactory.createColor(colorList.get(0), colorList.get(1), colorList.get(2));
			}
		}
		return color;
	}

	protected double getZOffsetFromDB (String id , ElevationHelper Elevation) throws SQLException {

		double zOffset = Double.MAX_VALUE;

		if(Elevation.IsTableCreated())
		{
			ResultSet rs = Elevation.SelectElevationOffSet(id, 0);
			while ( rs.next() ) { 
				zOffset = rs.getDouble("zoffset");
			}
		}
		
		return zOffset;
	}


	protected double getZOffsetFromGEService (String gmlId, List<Point3d> candidates, String _TargetSrs , ElevationHelper Elevation) {

		double zOffset = Double.MAX_VALUE;
		
		try{
				double[] coords = new double[candidates.size()*3];
				int index = 0;
				for (Point3d point3d: candidates) {
					// undo trick for very close coordinates
					List<Double> tmpPointList = ProjConvertor.transformPoint(point3d.x / 100 , point3d.y / 100 , point3d.z / 100 , _TargetSrs , "4326");
					coords[index++] = tmpPointList.get(1); 
					coords[index++] = tmpPointList.get(0);
					coords[index++] = tmpPointList.get(2);
				}

				Logger.getInstance().info("Getting zOffset from Google's elevation API with " + candidates.size() + " points.");

				zOffset = elevationServiceHandler.getZOffset(coords);
				if(!Elevation.IsTableCreated())
					Elevation.CreateElevationTable(0);
				Elevation.InsertElevationOffSet(gmlId , zOffset , 0);
		}

		catch (Exception e) {

			Logger.getInstance().error(e.toString());

		}

		return zOffset;
	}

	
	
	public String GetImagePath()
	{
		Internal intConfig = config.getInternal();		
		directoryScanner = new DirectoryScanner(true);
		directoryScanner.addFilenameFilter(new CityGMLFilenameFilter());		
		List<File> importFiles = directoryScanner.getFiles(intConfig.getImportFiles());
		return importFiles.get(0).getParent();
	}

	@SuppressWarnings("unchecked")
	protected List<Point3d> getLowestPointsCoordinates(List<BuildingSurface> result, KmlSplittingResult work) throws Exception {

		double currentlyLowestZCoordinate = Double.MAX_VALUE;

		List<Point3d> coords = new ArrayList<Point3d>();
		List<Double> ordinates = new ArrayList<Double>();


		for(BuildingSurface _row :result)
		{
			List<Double> PointList = (List<Double>)_row.getGeometry();
			//ordinates.addAll(ProjConvertor.TransformProjection(PointList.get(0), PointList.get(1), PointList.get(2), work.getTargetSrs() , "4326"));
			ordinates.addAll(PointList);
		}

		// we are only interested in the z coordinate
		for (int j = 2; j < ordinates.size(); j+=3) {
			if (ordinates.get(j) < currentlyLowestZCoordinate) {
				coords.clear();
				Point3d point3d = new Point3d(ordinates.get(j-2), ordinates.get(j-1), ordinates.get(j));
				coords.add(point3d);
				currentlyLowestZCoordinate = point3d.z;
			}
			if (ordinates.get(j) == currentlyLowestZCoordinate) {
				Point3d point3d = new Point3d(ordinates.get(j-2), ordinates.get(j-1), ordinates.get(j));
				if (!coords.contains(point3d)) {
					coords.add(point3d);
				}
			}
		}

		for (Point3d point3d: coords) {
			point3d.x = point3d.x * 100; // trick for very close coordinates
			point3d.y = point3d.y * 100;
			point3d.z = point3d.z * 100;
		}


		return coords;
	}


	protected double[] convertPointCoordinatesToWGS84(double[] coords) throws SQLException {

		double[] pointCoords = null; 

		StringBuilder geomEWKT = new StringBuilder("");
		String coordComma = "";

		geomEWKT.append("SRID=").append(dbSrs.getSrid()).append(";");

		if (coords.length == 3){
			geomEWKT.append("POINT(")
			.append(coords[0]).append(" ")
			.append(coords[1]).append(" ")
			.append(coords[2]);
		}
		else {
			geomEWKT.append("LINESTRING(");

			for (int i = 0; i < coords.length; i += 3){
				geomEWKT.append(coordComma)
				.append(coords[0]).append(" ")
				.append(coords[1]).append(" ")
				.append(coords[2]);

				coordComma = ",";
			}
		}

		geomEWKT.append(")");

		Geometry geom = PGgeometry.geomFromString(geomEWKT.toString());
		Geometry convertedPointGeom = convertToWGS84(geom);

		if (convertedPointGeom != null) {
			pointCoords = new double[3];
			pointCoords[0] = convertedPointGeom.getFirstPoint().x;
			pointCoords[1] = convertedPointGeom.getFirstPoint().y;
			pointCoords[2] = convertedPointGeom.getFirstPoint().z;
		}

		return pointCoords;
	}


	protected Geometry convertToWGS84(Geometry geometry) throws SQLException {

		double[] originalCoords = new double[(geometry.numPoints()*3)];

		for (int i = 0, j = 0; i < geometry.numPoints(); i++, j+=3){
			originalCoords[j] = geometry.getPoint(i).x;
			originalCoords[j+1] = geometry.getPoint(i).y;
			originalCoords[j+2] = geometry.getPoint(i).z;
		}

		Geometry convertedPointGeom = null;
		PreparedStatement convertStmt = null;
		ResultSet rs2 = null;
		try {
			convertStmt = (dbSrs.is3D() && geometry.getDimension() == 3) ?
					connection.prepareStatement(Queries.TRANSFORM_GEOMETRY_TO_WGS84_3D):
						connection.prepareStatement(Queries.TRANSFORM_GEOMETRY_TO_WGS84);
					// now convert to WGS84
					PGgeometry unconverted = new PGgeometry(geometry);
					convertStmt.setObject(1, unconverted);
					rs2 = convertStmt.executeQuery();
					while (rs2.next()) {
						// ColumnName is ST_Transform(Geometry, 4326)
						PGgeometry converted = (PGgeometry)rs2.getObject(1); 
						convertedPointGeom = converted.getGeometry();
					}
		}
		catch (Exception e) {
			Logger.getInstance().warn("Exception when converting geometry to WGS84");
			e.printStackTrace();
		}
		finally {
			try {
				if (rs2 != null) rs2.close();
				if (convertStmt != null) convertStmt.close();
			}
			catch (Exception e2) {}
		}

		if (config.getProject().getKmlExporter().isUseOriginalZCoords()) {
			for (int i = 0, j = 2; i < convertedPointGeom.numPoints(); i++, j+=3) {
				convertedPointGeom.getPoint(i).setZ(originalCoords[j]);
			}
		}

		return convertedPointGeom;
	}


	private int roundUpPots(int t)
	{
		t--;
		t |= t >> 1;
			t |= t >> 2;
			t |= t >> 4;
		t |= t >> 8;
				t |= t >> 16;
				t |= t >> 32;
			t++;
			return t;
	}

	protected class Node{
		double key;
		Object value;
		Node rightArc;
		Node leftArc;

		protected Node(double key, Object value){
			this.key = key;
			this.value = value;
		}

		protected void setLeftArc(Node leftArc) {
			this.leftArc = leftArc;
		}

		protected Node getLeftArc() {
			return leftArc;
		}

		protected void setRightArc (Node rightArc) {
			this.rightArc = rightArc;
		}

		protected Node getRightArc() {
			return rightArc;
		}

	}

	protected class NodeX extends Node{
		protected NodeX(double key, Object value){
			super(key, value);
		}
	}
	protected class NodeY extends Node{
		protected NodeY(double key, Object value){
			super(key, value);
		}
	}
	protected class NodeZ extends Node{
		protected NodeZ(double key, Object value){
			super(key, value);
		}
	}


}
