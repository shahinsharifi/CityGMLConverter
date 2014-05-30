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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;

import com.vividsolutions.jts.index.bintree.Root;

import de.tub.citydb.log.Logger;





public class FileHandler {


	
	private final static Logger LOG = Logger.getInstance();
	
	public FileHandler() throws Exception {

	}

	

	public static void CreateXML(File _file,  List<Map<String, Object>> _InputData) throws Exception
	{


		String _FileName = _file.getName();
		String _FilePath = _file.getParent();
	
		try {

			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

			// root elements
			Document doc = docBuilder.newDocument();
			Element rootElement = doc.createElement("Elevation");
			doc.appendChild(rootElement);

			int index = 0;
			for(Map<String, Object> _Row: _InputData)
			{	
				Element _Child1 = doc.createElement("Building" + index);
				
				for(String _Key:_Row.keySet())
				{
					Element _Child2 = doc.createElement(_Key);
					_Child2.setTextContent(String.valueOf(_Row.get(_Key)));
					_Child1.appendChild(_Child2);
					
				}
				rootElement.appendChild(_Child1);
				index++;
			}
						

			// write the content into xml file
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(new File(_FilePath +"\\ElevationConfig.xml"));


			transformer.transform(source, result);
			Thread.sleep(100);// It is a delay to prevent the conflict between workers for writing into the file. 
			LOG.info("Elevation offset has been saved for the:" + _FileName);

			
		}catch (Exception e) {
			
			LOG.error("CreateXML:" + e.toString());
			
		}		

	}
	
	
	
	public static Map<String, Object> ReadXML(File _file, String TargetElementName)
	{

		String _FileName = _file.getName();
		String _FilePath = _file.getParent();

		Map<String, Object> Result = new HashMap<String,Object>();
		try {
			
			File fXmlFile = new File(_FilePath +"\\ElevationConfig.xml");
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile);

			doc.getDocumentElement().normalize();
			
			
			Node _root = doc.getElementsByTagName("Elevation").item(0);
			
			NodeList nList = _root.getChildNodes();
			
			for (int temp = 0; temp < nList.getLength(); temp++) {
				
				Node nNode = nList.item(temp);
		 
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
		 
					
					Element eElement = (Element) nNode;
					final double zOffset = Double.parseDouble(eElement.getElementsByTagName(TargetElementName).item(0).getTextContent());
					Result.put("zOffset", zOffset);
		 
				}
			}

			
		} catch (Exception e) {
			
			LOG.error("ReadXML:" + e.toString());
		}


		return Result;
	}	
	
	
	public static boolean IsFileExists(File _file)
	{
		String _FileName = _file.getName();
		String _FilePath = _file.getParent();
		
		try {
			
		  File f = new File(_FilePath +"\\ElevationConfig.xml");
			 
		  if(f.exists()){
			  
			  return true;
		  
		  }else{
			
			  return false;
		  }
			
		} catch (Exception e) {
			
			LOG.error("FileCheck:" + e.toString());
			return false;
		}
		  
		
	}



}
