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
package de.tub.citydb.modules.citykml.content1;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


import net.opengis.kml._2.BoundaryType;
import net.opengis.kml._2.LinearRingType;

import org.citygml4j.geometry.BoundingBox;
import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.model.citygml.building.AbstractBoundarySurface;
import org.citygml4j.model.citygml.building.AbstractBuilding;
import org.citygml4j.model.citygml.building.BoundarySurfaceProperty;
import org.citygml4j.model.citygml.building.BuildingInstallation;
import org.citygml4j.model.citygml.building.BuildingInstallationProperty;
import org.citygml4j.model.citygml.building.BuildingPart;
import org.citygml4j.model.citygml.building.BuildingPartProperty;
import org.citygml4j.model.citygml.building.IntBuildingInstallation;
import org.citygml4j.model.citygml.building.IntBuildingInstallationProperty;
import org.citygml4j.model.citygml.building.InteriorRoomProperty;
import org.citygml4j.model.citygml.building.Room;
import org.citygml4j.model.citygml.building.WallSurface;
import org.citygml4j.model.citygml.core.Address;
import org.citygml4j.model.citygml.core.AddressProperty;
import org.citygml4j.model.gml.GMLClass;
import org.citygml4j.model.gml.basicTypes.DoubleOrNull;
import org.citygml4j.model.gml.basicTypes.MeasureOrNullList;
import org.citygml4j.model.gml.geometry.AbstractGeometry;
import org.citygml4j.model.gml.geometry.GeometryProperty;
import org.citygml4j.model.gml.geometry.aggregates.MultiCurveProperty;
import org.citygml4j.model.gml.geometry.aggregates.MultiSurface;
import org.citygml4j.model.gml.geometry.aggregates.MultiSurfaceProperty;
import org.citygml4j.model.gml.geometry.complexes.CompositeSurface;
import org.citygml4j.model.gml.geometry.primitives.AbstractRing;
import org.citygml4j.model.gml.geometry.primitives.AbstractRingProperty;
import org.citygml4j.model.gml.geometry.primitives.AbstractSurface;
import org.citygml4j.model.gml.geometry.primitives.Solid;
import org.citygml4j.model.gml.geometry.primitives.SolidProperty;
import org.citygml4j.model.gml.geometry.primitives.SurfaceProperty;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.opengis.geometry.Boundary;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.postgis.PGgeometry;

import sun.util.logging.resources.logging;
import de.micromata.opengis.kml.v_2_2_0.AltitudeMode;
import de.micromata.opengis.kml.v_2_2_0.Coordinate;
import de.micromata.opengis.kml.v_2_2_0.Document;
import de.micromata.opengis.kml.v_2_2_0.Kml;
import de.micromata.opengis.kml.v_2_2_0.LinearRing;
import de.micromata.opengis.kml.v_2_2_0.Placemark;
import de.micromata.opengis.kml.v_2_2_0.Point;
import de.micromata.opengis.kml.v_2_2_0.Polygon;
import de.micromata.opengis.kml.v_2_2_0.Style;
import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.database.TableEnum;
import de.tub.citydb.database.TypeAttributeValueEnum;
import de.tub.citydb.log.Logger;
import de.tub.citydb.modules.citygml.common.database.xlink.DBXlinkBasic;
import de.tub.citydb.modules.citygml.common.database.xlink.DBXlinkLinearRing;
import de.tub.citydb.modules.citygml.common.database.xlink.DBXlinkSurfaceGeometry;
import de.tub.citydb.util.Util;


public  class Building implements Importer {
	
	private final Logger LOG = Logger.getInstance();
	
	//General Information
	private String Gml_Id = "";
	private String Gml_Name = "";
	private String GML_Class = "";
	private String GML_Function = "";
	private String GML_Description = "";
	private String GML_Usage = "";
	private java.util.Date GML_YearOfConstruction;
	private java.util.Date GML_YearOfDemolition;
	private String GML_RoofType = "";
	private Double GML_MeasuredHeight;
	private Integer GML_StoreysAboveGround;
	private Integer GML_StoreysBelowGround;
	private String GML_StoreyHeightsAboveGround;
	private String GML_StoreyHeightsBelowGround;
	private Address GML_Address = null;
	
	//Geometry
	private List<SolidProperty> _SolidProperty = null;
	private List<MultiSurfaceProperty> _MultiSurfaceProperty = null;
	private List<MultiCurveProperty> _MultiCurveProperty = null;
	private List<AbstractBoundarySurface> _BoundarySurfaceProperty = null;

	
	
	
	public Building() throws SQLException {
		
		init();
	}

	private void init() throws SQLException {
		
	}
	
	//GML:ID
	public void SetGmlID(String GmlId)
	{
		Gml_Id=GmlId;		
	}
	
	public String GetGmlID()
	{	
	
		return Gml_Id;
	}

	
	//GML:Name
	public void SetGmlName(String GmlName)	
	{
		Gml_Name = GmlName;		
	}
	
	public String GetGmlName()
	{	
	
		return Gml_Name;
	}
	
	
	//GML:Description
	public void SetGmlDescription(String GmlDescription)	
	{
		GML_Description = GmlDescription;		
	}
	
	public String GetGmlDescription()
	{	
	
		return GML_Description;
	}
	
	
	
	//GML:Function
	public void SetGmlFunction(String GmlFunction)	
	{
		GML_Function = GmlFunction;		
	}
	
	public String GetGmlFunction()
	{	
	
		return GML_Function;
	}
	
	
	//GML:Class
	public void SetGmlClass(String GmlClass)	
	{
		GML_Class = GmlClass;		
	}
	
	public String GetGmlClass()
	{	
	
		return GML_Class;
	}
	
	
	//GML:Usage
	public void SetGmlUsage(String GmlUsage)	
	{
		GML_Usage = GmlUsage;		
	}
	
	public String GetGmlUsage()
	{	
	
		return GML_Usage;
	}
	
	
	
	//GML:YearOfConstruction
	public void SetGmlYearOfConstruction(java.util.Date GmlYearOfConstruction)	
	{
		GML_YearOfConstruction = GmlYearOfConstruction;		
	}
	
	public java.util.Date GetGmlYearOfConstruction()
	{	
	
		return GML_YearOfConstruction;
	}
	
	
	//GML:YearOfDemolition
	public void SetGmlYearOfDemolition(java.util.Date GmlYearOfDemolition)	
	{
		GML_YearOfDemolition = GmlYearOfDemolition;		
	}
	
	public java.util.Date GetGmlYearOfDemolition()
	{	
	
		return GML_YearOfDemolition;
	}
	

	
	//GML:RoofType
	public void SetGmlRoofType(String GmlRoofType)	
	{
		GML_RoofType = GmlRoofType;		
	}
	
	public String GetGmlRoofType()
	{	
	
		return GML_RoofType;
	}
		
		
				
		
	//GML:GML_MeasuredHeight
	public void SetGmlMeasuredHeight(Double GmlMeasuredHeight)	
	{
		GML_MeasuredHeight = GmlMeasuredHeight;		
	}
	
	public Double GetGmlMeasuredHeight()
	{	
	
		return GML_MeasuredHeight;
	}
		
		
		
	//GML:StoreysAboveGround
	public void SetGmlStoreysAboveGround(Integer GmlStoreysAboveGround)	
	{
		GML_StoreysAboveGround = GmlStoreysAboveGround;		
	}
	
	public Integer GetGmlStoreysAboveGround()
	{	
	
		return GML_StoreysAboveGround;
	}
		
		
		
	//GML:StoreysBelowGround
	public void SetGmlStoreysBelowGround(Integer GmlStoreysBelowGround)	
	{
		GML_StoreysBelowGround = GmlStoreysBelowGround;		
	}
	
	public Integer GetGmlStoreysBelowGround()
	{	
	
		return GML_StoreysBelowGround;
	}
	
	
	//GML:StoreyHeightsAboveGround
	public void SetGmlStoreyHeightsAboveGround(String GmlStoreyHeightsAboveGround)	
	{
		GML_StoreyHeightsAboveGround = GmlStoreyHeightsAboveGround;		
	}
	
	public String GetGmlStoreyHeightsAboveGround()
	{	
	
		return GML_StoreyHeightsAboveGround;
	}
		
			
		
	//GML:StoreysBelowGround
	public void SetGmlStoreyHeightsBelowGround(String GmlStoreyHeightsBelowGround)	
	{
		GML_StoreyHeightsBelowGround = GmlStoreyHeightsBelowGround;		
	}
	
	public String GetStoreyHeightsBelowGround()
	{		
		return GML_StoreyHeightsBelowGround;
	}
	
	
	//GML:BoundarySurfaceProperty
	public void SetGmlBoundrySurface(List<AbstractBoundarySurface> GmlBoundrySurface)	
	{
		_BoundarySurfaceProperty = GmlBoundrySurface;		
	}
	
	public List<AbstractBoundarySurface> GetGmlBoundrySurface()
	{		
		return _BoundarySurfaceProperty;
	}
	
	

	@Override
	public ImporterEnum getDBImporterType() {
		return ImporterEnum.BUILDING;
	}

}
