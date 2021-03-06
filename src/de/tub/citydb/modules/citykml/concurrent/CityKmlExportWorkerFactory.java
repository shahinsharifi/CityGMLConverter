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
package de.tub.citydb.modules.citykml.concurrent;

import java.sql.SQLException;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.xml.bind.JAXBContext;

import net.opengis.kml._2.ObjectFactory;

import org.citygml4j.factory.CityGMLFactory;
import org.citygml4j.util.xml.SAXEventBuffer;

import de.tub.citydb.api.concurrent.Worker;
import de.tub.citydb.api.concurrent.WorkerFactory;
import de.tub.citydb.api.concurrent.WorkerPool;
import de.tub.citydb.api.event.EventDispatcher;
import de.tub.citydb.config.Config;
import de.tub.citydb.modules.citykml.common.xlink.content.DBXlink;
import de.tub.citydb.modules.citykml.content.ColladaBundle;
import de.tub.citydb.modules.citykml.content.KmlSplittingResult;

public class CityKmlExportWorkerFactory implements WorkerFactory<KmlSplittingResult> {
	private final JAXBContext jaxbKmlContext;
	private final JAXBContext jaxbColladaContext;
	private final WorkerPool<SAXEventBuffer> ioWriterPool;
	private WorkerPool<DBXlink> tmpXlinkPool;
	private final ObjectFactory kmlFactory;
	private final CityGMLFactory cityGMLFactory;
	private final Config config;
	private final EventDispatcher eventDispatcher;

	public CityKmlExportWorkerFactory(
			JAXBContext jaxbKmlContext,
			JAXBContext jaxbColladaContext,			
			WorkerPool<SAXEventBuffer> ioWriterPool,
			WorkerPool<DBXlink> tmpXlinkPool,
			ObjectFactory kmlFactory,
			CityGMLFactory cityGMLFactory,
			Config config,
			EventDispatcher eventDispatcher) {
		this.jaxbKmlContext = jaxbKmlContext;
		this.jaxbColladaContext = jaxbColladaContext;
		this.tmpXlinkPool = tmpXlinkPool;
		this.ioWriterPool = ioWriterPool;
		this.kmlFactory = kmlFactory;
		this.cityGMLFactory = cityGMLFactory;
		this.config = config;
		this.eventDispatcher = eventDispatcher;
	}

	@Override
	public Worker<KmlSplittingResult> createWorker() {
		CityKmlExportWorker kmlWorker = null;

		try {
			kmlWorker = new CityKmlExportWorker(
					jaxbKmlContext,
					jaxbColladaContext,
					ioWriterPool,
					tmpXlinkPool,
					kmlFactory,
					cityGMLFactory,
					config,
					eventDispatcher);
		} catch (SQLException sqlEx) {
			// could not instantiate DBWorker
		}

		return kmlWorker;
	}

}
