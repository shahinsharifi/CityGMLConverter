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
package de.tub.citydb.cmd;

import java.io.File;
import java.io.FilenameFilter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.bind.JAXBContext;

import org.citygml4j.builder.jaxb.JAXBBuilder;

import de.tub.citydb.api.database.DatabaseConfigurationException;
import de.tub.citydb.api.database.DatabaseSrs;
import de.tub.citydb.api.event.EventDispatcher;
import de.tub.citydb.api.registry.ObjectRegistry;
import de.tub.citydb.config.Config;
import de.tub.citydb.config.project.database.DBConnection;
import de.tub.citydb.config.project.exporter.ExportFilterConfig;
import de.tub.citydb.log.Logger;


public class ImpExpCmd {
	private final Logger LOG = Logger.getInstance();
	private JAXBBuilder cityGMLBuilder;
	private JAXBContext jaxbKmlContext;
	private JAXBContext jaxbColladaContext;
	private Config config;

	public ImpExpCmd(JAXBBuilder cityGMLBuilder, Config config) {
		this.cityGMLBuilder = cityGMLBuilder;
		this.config = config;
	}

	public ImpExpCmd(JAXBContext jaxbKmlContext,
			JAXBContext jaxbColladaContext,
			Config config) {
		this.jaxbKmlContext = jaxbKmlContext;
		this.jaxbColladaContext = jaxbColladaContext;
		this.config = config;
	}

	
	private List<File> getFiles(String fileNames, String delim) {
		List<File> files = new ArrayList<File>();
		
		for (String part : fileNames.split(delim)) {
			if (part == null || part.trim().isEmpty())
				continue;

			File file = new File(part.trim());
			if (file.isDirectory()) {
				files.add(file);
				continue;
			}

			final String pathName = new File(file.getAbsolutePath()).getParent();
			final String fileName = file.getName().replace("?", ".?").replace("*", ".*?");

			file = new File(pathName);
			if (!file.exists()) {
				LOG.error("'" + file.toString() + "' does not exist");
				continue;
			}

			File[] wildcardList = file.listFiles(new FilenameFilter() {
				public boolean accept(File dir, String name) {
					return (name.matches(fileName));
				}
			});

			if (wildcardList != null && wildcardList.length != 0)
				files.addAll(Arrays.asList(wildcardList));
		}

		return files;
	}
}
