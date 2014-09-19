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

import java.util.List;

import org.citygml4j.model.citygml.CityGML;
import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.model.citygml.appearance.AppearanceProperty;

import de.tub.citydb.config.project.CitykmlExporter.DisplayForm;


public class Point {


	private double X;
	private double Y;
	private double Z;
	

	public Point(double X,double Y,double Z) {

		this.X = X;
		this.Y = Y;
		this.Z = Z;
		
	}

	
	public double getX() {
	
		return X;
	
	}
	
	public void setX(double X) {
		this.X = X;
	}

	
	
	public double getY() {
		
		return Y;
	
	}
	
	public void setY(double Y) {
		this.Y = Y;
	}

	
	public double getZ() {
		
		return Z;
	
	}
	
	public void setZ(double Z) {
		this.Z = Z;
	}

	
}
