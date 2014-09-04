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
import de.tub.citydb.database.TypeAttributeValueEnum;

public class BuildingSurface implements Surface{


	private Object PId;
	private String Id;
	private String Type;
	private List<Double> Geometry;
	

	public BuildingSurface() {

	}

	
	//Parent Surface ID
	public Object getPId() {
		return PId;
	}

	
	public void setPId(Object PId) {
		this.PId = PId;
	}

	
	
	//Surface ID
	@Override
	public String getId() {
		return Id;
	}
	
	@Override
	public void setId(String Id) {
		this.Id = Id;
	}
	
	
	//Surface Type
	@Override
	public String getType() {
		return Type;
	}

	@Override
	public void setType(String Type) {
		this.Type = Type;
	}
	

	//Surface Geometry
	@Override
	public List<Double> getGeometry() {
		return Geometry;
	}

	@Override
	public void setGeometry(List<Double> Geometry) {
		this.Geometry = Geometry;
	}

	
	
}
