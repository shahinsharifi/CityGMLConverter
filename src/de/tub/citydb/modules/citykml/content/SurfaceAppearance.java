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


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.citygml4j.geometry.Matrix;
import org.citygml4j.model.citygml.CityGML;
import org.citygml4j.model.citygml.appearance.AbstractSurfaceData;
import org.citygml4j.model.citygml.appearance.AbstractTextureParameterization;
import org.citygml4j.model.citygml.appearance.Appearance;
import org.citygml4j.model.citygml.appearance.AppearanceMember;
import org.citygml4j.model.citygml.appearance.AppearanceProperty;
import org.citygml4j.model.citygml.appearance.ParameterizedTexture;
import org.citygml4j.model.citygml.appearance.SurfaceDataProperty;
import org.citygml4j.model.citygml.appearance.TexCoordGen;
import org.citygml4j.model.citygml.appearance.TexCoordList;
import org.citygml4j.model.citygml.appearance.TextureAssociation;
import org.citygml4j.model.citygml.appearance.TextureCoordinates;
import org.citygml4j.model.citygml.appearance.X3DMaterial;
import org.citygml4j.model.citygml.building.AbstractBuilding;
import org.citygml4j.model.citygml.core.AbstractCityObject;
import org.citygml4j.model.citygml.core.CityModel;
import org.citygml4j.model.citygml.core.CityObjectMember;

import de.tub.citydb.log.Logger;
import de.tub.citydb.util.Util;

public class SurfaceAppearance {

	private AbstractBuilding _building;
	private boolean IsSetAppearance = false;
	List<Map<String, Object>> _SurfaceDataList = new ArrayList<Map<String,Object>>();

	public SurfaceAppearance(AbstractBuilding _Building)
	{	
		this._building = _Building;
	}

	public void SetAppearance(boolean _IsSet)
	{
		this.IsSetAppearance = _IsSet;
	}

	public boolean GetAppearance()
	{
		return this.IsSetAppearance;
	}


	public List<AppearanceMember> GetModelAppearanceMembers(CityGML _cityGML)
	{
		List<AppearanceMember> _list = new ArrayList<AppearanceMember>();
		String txtIDString = "";
		try{
			switch (_cityGML.getCityGMLClass()) {
			case BUILDING:
				AbstractCityObject _AbstractCityObject = (AbstractCityObject)_cityGML;
				CityObjectMember _CityObject = (CityObjectMember)_AbstractCityObject.getParent();
				txtIDString = _AbstractCityObject.getId();
				CityModel _CityModel = (CityModel)_CityObject.getParent();
				_list.addAll(_CityModel.getAppearanceMember());
				break;

			default:
				break;
			}
		}catch(Exception ex)
		{	
			Logger.getInstance().error(ex.toString() + "ID:" + txtIDString);
		}
		return _list;	
	}


	
	private boolean IsContainSurface(List<String> TargetList,String SurfaceID)
	{		
		for(String target : TargetList)
		{	
			if(SurfaceID.equals(target))
				return true;
		}		
		return false;		
	}


	public Map<String, Object> GetAppearanceBySurfaceID(String _SurfaceID , List<AppearanceProperty> _AppearanceList)
	{
		Map<String, Object> _SurfaceData = new HashMap<String, Object>();
		

		for(AppearanceProperty _Property: _AppearanceList)
		{
			Appearance _Appearance=_Property.getAppearance();
			for(SurfaceDataProperty _SurfaceDataMember: _Appearance.getSurfaceDataMember())
			{
				AbstractSurfaceData _AbstractSurfaceData = _SurfaceDataMember.getSurfaceData();
		
				if(_AbstractSurfaceData.getCityGMLClass().name().equals("X3D_MATERIAL")){
					X3DMaterial _X3D = (X3DMaterial)_AbstractSurfaceData;
					
					
					if(IsContainSurface(_X3D.getTarget(),_SurfaceID))
					//if(_SurfaceID.equals(_X3D.getTarget().get(0)))
					{
					
						_SurfaceData.put("id", _X3D.getId());
						_SurfaceData.put("imageuri", null);
						_SurfaceData.put("type", "X3D_MATERIAL");
						_SurfaceData.put("target", _X3D.getTarget());
						_SurfaceData.put("x3d_ambient_intensity", _X3D.getAmbientIntensity());
						_SurfaceData.put("x3d_shininess", _X3D.getShininess());
						_SurfaceData.put("x3d_transparency", _X3D.getTransparency());
						_SurfaceData.put("x3d_diffuse_color", _X3D.getDiffuseColor());
						_SurfaceData.put("x3d_specular_color", _X3D.getSpecularColor());
						_SurfaceData.put("x3d_emissive_color", _X3D.getEmissiveColor());
						_SurfaceData.put("x3d_is_smooth", _X3D.getIsSmooth());
						_SurfaceData.put("coord", null);
					}	

				}else if(_AbstractSurfaceData.getCityGMLClass().name().equals("PARAMETERIZED_TEXTURE")){

					ParameterizedTexture _Texture = (ParameterizedTexture)_AbstractSurfaceData;
					for (TextureAssociation target : _Texture.getTarget()) {
						String targetURI = target.getUri();
						if(targetURI.equals(_SurfaceID))
						{
							_SurfaceData.put("id", _Texture.getId());
							_SurfaceData.put("imageuri", _Texture.getImageURI());
							_SurfaceData.put("type", "PARAMETERIZED_TEXTURE");
							_SurfaceData.put("x3d_ambient_intensity", null);
							_SurfaceData.put("x3d_shininess", null);
							_SurfaceData.put("x3d_transparency", null);
							_SurfaceData.put("x3d_diffuse_color", null);
							_SurfaceData.put("x3d_specular_color", null);
							_SurfaceData.put("x3d_emissive_color", null);
							_SurfaceData.put("x3d_is_smooth", null);
							
							if (targetURI != null && targetURI.length() != 0) {
								if (target.isSetTextureParameterization()) {
									AbstractTextureParameterization texPara = target.getTextureParameterization();
									String texParamGmlId = texPara.getId();
									switch (texPara.getCityGMLClass()) {
									
										case TEX_COORD_GEN:
											TexCoordGen texCoordGen = (TexCoordGen)texPara;
											if (texCoordGen.isSetWorldToTexture()) {
												Matrix worldToTexture = texCoordGen.getWorldToTexture().getMatrix();	
												String worldToTextureString = Util.collection2string(worldToTexture.toRowPackedList(), " ");	
											}
											break;
											
										case TEX_COORD_LIST:
											TexCoordList texCoordList = (TexCoordList)texPara;	
											if (texCoordList.isSetTextureCoordinates()) {
												HashSet<String> rings = new HashSet<String>(texCoordList.getTextureCoordinates().size());	
												for (TextureCoordinates texCoord : texCoordList.getTextureCoordinates()) {
													String ring = texCoord.getRing();
													if (ring != null && ring.length() != 0 && texCoord.isSetValue()) {
														String coords = Util.collection2string(texCoord.getValue(), " ");
														_SurfaceData.put("target", targetURI);
														_SurfaceData.put("coord", coords);
													}
												}
											}
											break;
										}
								} 
								else
								{
									String href = target.getHref();
								}
							}
							break;
						}
					}
				}
			}
		}

		return _SurfaceData;
	}
}