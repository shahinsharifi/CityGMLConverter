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
 * License aString with this program. If not, see 
 * <http://www.gnu.org/licenses/>.
 * 
 * The development of the 3D City Database Importer/Exporter has 
 * been financially supported by the following cooperation partners:
 * 
 * Business Location Center, Berlin <http://www.businesslocationcenter.de/>
 * virtualcitySYSTEMS GmbH, Berlin <http://www.virtualcitysystems.de/>
 * Berlin Senate of Business, Technology and Women <http://www.berlin.de/sen/wtf/>
 */
package de.tub.citydb.modules.citykml.common.xlink.content;

public class DBXlinkTextureAssociation implements DBXlink {
	private String surfaceDataId;
	private String surfaceGeometryId;
	private String gmlId;

	public DBXlinkTextureAssociation(String surfaceDataId, String surfaceGeometryId, String gmlId) {
		this.surfaceDataId = surfaceDataId;
		this.surfaceGeometryId = surfaceGeometryId;
		this.gmlId = gmlId;
	}

	public String getSurfaceDataId() {
		return surfaceDataId;
	}

	public void setSurfaceDataId(String surfaceDataId) {
		this.surfaceDataId = surfaceDataId;
	}

	public String getSurfaceGeometryId() {
		return surfaceGeometryId;
	}

	public void setSurfaceGeometryId(String surfaceGeometryId) {
		this.surfaceGeometryId = surfaceGeometryId;
	}

	public String getGmlId() {
		return gmlId;
	}

	public void setGmlId(String gmlId) {
		this.gmlId = gmlId;
	}

	@Override
	public DBXlinkEnum getXlinkType() {
		return DBXlinkEnum.TEXTUREASSOCIATION;
	}

}
