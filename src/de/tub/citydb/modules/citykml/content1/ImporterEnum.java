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

import java.util.LinkedList;
import java.util.List;

import de.tub.citydb.modules.citykml.content1.ImporterEnum;

public enum ImporterEnum {
	SURFACE_GEOMETRY(),
	IMPLICIT_GEOMETRY(SURFACE_GEOMETRY),
	CITYOBJECT(),
	CITYOBJECT_GENERICATTRIB(CITYOBJECT, SURFACE_GEOMETRY),
	EXTERNAL_REFERENCE(CITYOBJECT),
	BUILDING(CITYOBJECT, SURFACE_GEOMETRY),
	ROOM(CITYOBJECT, BUILDING, SURFACE_GEOMETRY),
	BUILDING_FURNITURE(CITYOBJECT, ROOM, SURFACE_GEOMETRY, IMPLICIT_GEOMETRY),
	BUILDING_INSTALLATION(CITYOBJECT, BUILDING, ROOM, SURFACE_GEOMETRY),
	THEMATIC_SURFACE(CITYOBJECT, BUILDING, ROOM, SURFACE_GEOMETRY),
	ADDRESS(),
	ADDRESS_TO_BUILDING(ADDRESS, BUILDING),
	OPENING(CITYOBJECT, ADDRESS, SURFACE_GEOMETRY),
	OPENING_TO_THEM_SURFACE(OPENING, THEMATIC_SURFACE),
	TRANSPORTATION_COMPLEX(CITYOBJECT, SURFACE_GEOMETRY),
	TRAFFIC_AREA(CITYOBJECT, TRANSPORTATION_COMPLEX, SURFACE_GEOMETRY),
	CITY_FURNITURE(CITYOBJECT, SURFACE_GEOMETRY, IMPLICIT_GEOMETRY),
	LAND_USE(CITYOBJECT, SURFACE_GEOMETRY),
	WATERBODY(CITYOBJECT, SURFACE_GEOMETRY),
	WATERBOUNDARY_SURFACE(CITYOBJECT, SURFACE_GEOMETRY),
	WATERBOD_TO_WATERBND_SRF(WATERBODY, WATERBOUNDARY_SURFACE),
	PLANT_COVER(CITYOBJECT, SURFACE_GEOMETRY),
	SOLITARY_VEGETAT_OBJECT(CITYOBJECT, SURFACE_GEOMETRY, IMPLICIT_GEOMETRY),
	RELIEF_FEATURE(CITYOBJECT),
	RELIEF_COMPONENT(CITYOBJECT, SURFACE_GEOMETRY),
	RELIEF_FEAT_TO_REL_COMP(RELIEF_FEATURE, RELIEF_COMPONENT),
	GENERIC_CITYOBJECT(CITYOBJECT, SURFACE_GEOMETRY, IMPLICIT_GEOMETRY),
	CITYOBJECTGROUP(CITYOBJECT, SURFACE_GEOMETRY),
	DEPRECATED_MATERIAL_MODEL(),
	APPEARANCE(CITYOBJECT, DEPRECATED_MATERIAL_MODEL),
	SURFACE_DATA(),
	APPEAR_TO_SURFACE_DATA(APPEARANCE, SURFACE_DATA),	
	ST_GEOMETRY();

	private ImporterEnum[] dependencies;
	public static List<ImporterEnum> EXECUTION_PLAN = getExecutionPlan();
	
	private ImporterEnum(ImporterEnum... dependencies) {
		this.dependencies = dependencies;
	}
	
	public static List<ImporterEnum> getExecutionPlan() {
		Integer[] weights = new Integer[values().length];

		for (ImporterEnum type : values()) {			
			if (weights[type.ordinal()] == null) {
				weightDependencies(type, weights);
				weights[type.ordinal()] = 0;
			}
		}

		return getExecutionPlan(weights);
	}
	
	public static List<ImporterEnum> getExecutionPlan(ImporterEnum type) {
		Integer[] weights = new Integer[values().length];
		weights[type.ordinal()] = 0;
		weightDependencies(type, weights);
		
		return getExecutionPlan(weights);
	}
	
	private static List<ImporterEnum> getExecutionPlan(Integer[] weights) {
		LinkedList<ImporterEnum> executionPlan = new LinkedList<ImporterEnum>();
		
		int i, j;
		for (i = 0; i < values().length; i++) {
			if (weights[i] == null)
				continue;
			
			j = 0;
			for (ImporterEnum item : executionPlan) {
				if (weights[i] >= weights[item.ordinal()])
					break;
				
				j++;
			}
			
			executionPlan.add(j, ImporterEnum.values()[i]);
		}
		
		return executionPlan;
	}

	private static void weightDependencies(ImporterEnum type, Integer[] weights) {
		for (ImporterEnum dependence : type.dependencies) {
			if (dependence != null) {				
				if (weights[type.ordinal()] == null)
					weights[type.ordinal()] = 0;
				
				if (weights[dependence.ordinal()] == null)
					weights[dependence.ordinal()] = 0;
				
				weights[dependence.ordinal()] += weights[type.ordinal()] + 1;
				weightDependencies(dependence, weights);
			}
		}
	}
}
