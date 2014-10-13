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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.citygml4j.CityGMLContext;
import org.citygml4j.builder.CityGMLBuilder;
import org.citygml4j.model.citygml.appearance.AppearanceProperty;
import org.citygml4j.model.citygml.core.AbstractCityObject;
import org.citygml4j.model.citygml.core.CityModel;
import org.citygml4j.model.citygml.core.CityObjectMember;
import org.citygml4j.xml.io.CityGMLInputFactory;
import org.citygml4j.xml.io.reader.CityGMLReader;


public class DSUtil {

	public static List<String> getAppearanceThemeList(File file) throws Exception {

		ArrayList<String> appearanceThemes = new ArrayList<String>();
		List<AppearanceProperty> tmpAppearanceList = new ArrayList<AppearanceProperty>();
		final String THEME_UNKNOWN = "<unknown>";
		CityGMLContext ctx = new CityGMLContext();
		CityGMLBuilder builder = ctx.createCityGMLBuilder();
		CityGMLInputFactory in = builder.createCityGMLInputFactory();
		CityGMLReader reader = null;

		try {

			reader = in.createCityGMLReader(file);
			CityModel cityModel = (CityModel)reader.nextFeature();			
			
			if(cityModel.isSetCityObjectMember()){
				
				for (CityObjectMember member : cityModel.getCityObjectMember()) {
					
					if (member.isSetCityObject()) {
						
						AbstractCityObject cityObject = member.getCityObject();
												
						if(cityObject.isSetAppearance())
							tmpAppearanceList.addAll(cityObject.getAppearance());
						else 
							tmpAppearanceList.addAll(cityModel.getAppearanceMember());						
						
					}
				}
			}

			if(tmpAppearanceList.size() > 0){
				
				for(AppearanceProperty app:tmpAppearanceList){
					
					String THEME_VALID = app.getAppearance().getTheme();
					if(THEME_VALID != null){
						if(!appearanceThemes.contains(THEME_VALID))
							appearanceThemes.add(THEME_VALID);
					}else {
						if(!appearanceThemes.contains(THEME_UNKNOWN))
							appearanceThemes.add(THEME_UNKNOWN);
					}
				}
			}

		} catch (Exception Ex) {
			throw Ex;
		} finally {
			reader.close();
		}

		return appearanceThemes;
	}
	

}
