package de.tub.citydb.modules.citykml;

import java.util.Locale;

import javax.xml.bind.JAXBContext;

import org.citygml4j.builder.jaxb.JAXBBuilder;

import de.tub.citydb.api.plugin.extension.preferences.Preferences;
import de.tub.citydb.api.plugin.extension.preferences.PreferencesExtension;
import de.tub.citydb.api.plugin.extension.view.View;
import de.tub.citydb.api.plugin.extension.view.ViewExtension;
import de.tub.citydb.config.Config;
import de.tub.citydb.gui.ImpExpGui;
import de.tub.citydb.modules.citykml.gui.view.CityKMLExportView;
import de.tub.citydb.modules.citykml.gui.preferences.CityKMLExportPreferences;
import de.tub.citydb.modules.citykml.gui.view.CityKMLExportView;
import de.tub.citydb.plugin.InternalPlugin;


public class CityKMLExportPlugin implements InternalPlugin, ViewExtension, PreferencesExtension {
	private CityKMLExportView view;
	private CityKMLExportPreferences preferences;
	
	public CityKMLExportPlugin(JAXBBuilder jaxbBuilder, JAXBContext kmlContext, JAXBContext colladaContext, Config config, ImpExpGui mainView) {
		view = new CityKMLExportView(jaxbBuilder,kmlContext, colladaContext, config, mainView);
		preferences = new CityKMLExportPreferences(mainView, config);
	}
		
	@Override
	public void init(Locale locale) {
		loadSettings();
	}

	@Override
	public void shutdown() {
		setSettings();
	}

	@Override
	public void switchLocale(Locale newLocale) {
		view.doTranslation();
		preferences.doTranslation();
	}

	@Override
	public Preferences getPreferences() {
		return preferences;
	}

	@Override
	public View getView() {
		return view;
	}
	
	@Override
	public void loadSettings() {
		view.loadSettings();
		preferences.loadSettings();
	}

	@Override
	public void setSettings() {
		view.setSettings();
		preferences.setSettings();
	}
	
}