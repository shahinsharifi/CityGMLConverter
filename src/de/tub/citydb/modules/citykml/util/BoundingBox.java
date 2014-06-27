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


import java.util.List;

import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.opengis.geometry.Envelope;
import org.opengis.metadata.extent.Extent;
import org.opengis.referencing.ReferenceIdentifier;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.cs.CoordinateSystem;

import com.vividsolutions.jts.awt.PointShapeFactory.Point;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;

import de.tub.citydb.log.Logger;



public class BoundingBox {

	private org.opengis.geometry.BoundingBox nativeBounds;
	private String SourceSRS;
	
	
	public BoundingBox(double MinX, double MinY, double MaxX, double MaxY, String EPSG) throws Exception {
		
		SourceSRS = EPSG;
		CoordinateReferenceSystem nativeCrs = CRS.decode("EPSG:" + EPSG, true);
	    nativeBounds = new ReferencedEnvelope(MinX, MaxX, MinY, MaxY, nativeCrs);
	    
	}
	
	
	public boolean Contains(Envelope bounds)
	{
		
		if(this.nativeBounds.intersects((org.opengis.geometry.BoundingBox)bounds))
		{
			return true;
		}
		else {
			return false;
		}
		
	}
	
	
	
	
	public double OverlapArea(Envelope bounds)
	{	
		Geometry _buildingPolygon = JTS.toGeometry((org.opengis.geometry.BoundingBox)bounds);
		
		Geometry _nativePolygon = JTS.toGeometry(this.nativeBounds);		
		Polygon _overlapArea = (Polygon)_nativePolygon.intersection(_buildingPolygon);
		double TargetArea = _overlapArea.getArea();
		double BuildingArea = _buildingPolygon.getArea();

		return (TargetArea/BuildingArea)*100;		
	}
	

	
	
	public boolean ContainCentroid(Envelope bounds, String targetSRS) throws Exception
	{					
		Geometry _buildingPolygon = JTS.toGeometry((org.opengis.geometry.BoundingBox)bounds);		
		return ContainPoint(_buildingPolygon.getCentroid(),targetSRS);		
	}
	
	
	
	
	private boolean ContainPoint(com.vividsolutions.jts.geom.Point _point, String targetSRS) throws Exception
	{	
		double pointX = 0.0, pointY = 0.0;
		
		if(SourceSRS.equals("4326"))
		{
			List<Double> tmpPoint = ProjConvertor.transformPoint(_point.getX(), _point.getY(), 0, targetSRS, SourceSRS);//We should convert the CRS of the building's center point to the CRS of BoundingBox. 
			pointX = tmpPoint.get(1);
			pointY = tmpPoint.get(0);					
		}
		else {			
			pointX = _point.getX();
			pointY = _point.getY();				
		}

		if(Double.compare(pointX,this.nativeBounds.getMaxX()) < 0 && Double.compare(pointX,this.nativeBounds.getMinX()) > 0 && 			
		   Double.compare(pointY,this.nativeBounds.getMaxY()) < 0 && Double.compare(pointY,this.nativeBounds.getMinY()) > 0)
			return true;		
		else 
			return false;						
	}
	
	
	
}
