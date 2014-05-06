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
package de.tub.citydb.modules.citykml.gui.view;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import javax.xml.bind.JAXBContext;

import org.citygml4j.builder.jaxb.JAXBBuilder;

import de.tub.citydb.api.event.Event;
import de.tub.citydb.api.event.EventDispatcher;
import de.tub.citydb.api.event.EventHandler;
import de.tub.citydb.api.event.global.DatabaseConnectionStateEvent;
import de.tub.citydb.api.event.global.GlobalEvents;
import de.tub.citydb.api.log.LogLevel;
import de.tub.citydb.api.registry.ObjectRegistry;
import de.tub.citydb.config.Config;
import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.config.project.database.DBConnection;
// import de.tub.citydb.config.project.database.Database;
// import de.tub.citydb.config.project.database.Workspace;
import de.tub.citydb.config.project.exporter.ExportFilterConfig;
import de.tub.citydb.config.project.filter.FilterMode;
import de.tub.citydb.config.project.filter.TilingMode;
import de.tub.citydb.config.project.general.FeatureClassMode;
import de.tub.citydb.config.project.importer.ImportFilterConfig;
import de.tub.citydb.config.project.kmlExporter.DisplayForm;
import de.tub.citydb.config.project.kmlExporter.KmlExporter;
import de.tub.citydb.database.DatabaseConnectionPool;
import de.tub.citydb.gui.ImpExpGui;
import de.tub.citydb.gui.components.checkboxtree.DefaultCheckboxTreeCellRenderer;
import de.tub.citydb.gui.components.checkboxtree.DefaultTreeCheckingModel;
import de.tub.citydb.gui.components.ExportStatusDialog;
import de.tub.citydb.gui.components.ImportStatusDialog;
import de.tub.citydb.gui.components.bbox.BoundingBoxPanelImpl;
import de.tub.citydb.gui.components.checkboxtree.CheckboxTree;
import de.tub.citydb.gui.factory.PopupMenuDecorator;
import de.tub.citydb.log.Logger;
import de.tub.citydb.modules.citykml.concurrent.CityKmlImportWorker;
import de.tub.citydb.modules.citykml.concurrent.CityKmlImportWorkerFactory;
import de.tub.citydb.modules.citykml.controller.CityKmlImporter;
import de.tub.citydb.modules.citykml.util.KMLObject;
import de.tub.citydb.modules.common.event.InterruptEnum;
import de.tub.citydb.modules.common.event.InterruptEvent;
// import de.tub.citydb.util.Util;
import de.tub.citydb.util.database.DBUtil;
import de.tub.citydb.util.gui.GuiUtil;

@SuppressWarnings("serial")
public class CityKmlExportPanel extends JPanel implements EventHandler {

	protected static final int BORDER_THICKNESS = 5;
	protected static final int MAX_TEXTFIELD_HEIGHT = 20;
	protected static final int MAX_LABEL_WIDTH = 60;
	private static final int PREFERRED_WIDTH = 560;
	private static final int PREFERRED_HEIGHT = 780;
	
	private final Logger LOG = Logger.getInstance();
	
	
	private final JAXBBuilder jaxbBuilder;
	private final ReentrantLock mainLock = new ReentrantLock();
	private final JAXBContext jaxbKmlContext, jaxbColladaContext;
	private final Config config;
	private final ImpExpGui mainView;
	private final DatabaseConnectionPool dbPool;

	private JList fileList;
	private DefaultListModel fileListModel;
	private JButton removeButton;
	private JButton OpenFileButton;
	
	
	private JPanel browsePanel;
	private JTextField browseText = new JTextField("");
	private JButton browseButton = new JButton("");
	private JFormattedTextField srsField;
	private JLabel srsLabel;
	
	

	private JPanel versioningPanel;
//	private JLabel workspaceLabel = new JLabel();
//	private JTextField workspaceText = new JTextField("LIVE");
//	private JLabel timestampLabel = new JLabel();
//	private JTextField timestampText = new JTextField("");



	private JLabel rowsLabel = new JLabel();
	private JTextField rowsText = new JTextField("");
	private JLabel columnsLabel = new JLabel();
	private JTextField columnsText = new JTextField("");



	private JLabel featureClassesLabel = new JLabel();
	private CheckboxTree fcTree;
	private DefaultMutableTreeNode cityObject;
	private DefaultMutableTreeNode waterBody;
	private DefaultMutableTreeNode landUse;
	private DefaultMutableTreeNode building;
	private DefaultMutableTreeNode vegetation;
	private DefaultMutableTreeNode transportation;
	private DefaultMutableTreeNode relief;
	private DefaultMutableTreeNode cityFurniture;
	private DefaultMutableTreeNode genericCityObject;
	private DefaultMutableTreeNode cityObjectGroup;

	private JButton exportButton = new JButton("");
	
	

	public CityKmlExportPanel(JAXBBuilder jaxbBuilder, JAXBContext jaxbKmlContext, JAXBContext jaxbColladaContext, Config config, ImpExpGui mainView) {
		
		this.jaxbBuilder = jaxbBuilder;
		this.jaxbKmlContext = jaxbKmlContext;
		this.jaxbColladaContext = jaxbColladaContext;
		this.mainView = mainView;
		this.config = config;
		dbPool = DatabaseConnectionPool.getInstance();
		ObjectRegistry.getInstance().getEventDispatcher().addEventHandler(GlobalEvents.DATABASE_CONNECTION_STATE, this);

		initGui();
		addListeners();
		clearGui();
	}

	private void initGui() {
		
		
		
		fileList = new JList();		
		OpenFileButton = new JButton();
		removeButton = new JButton();

		browsePanel = new JPanel();
		browsePanel.setLayout(new GridBagLayout());
		browsePanel.add(browseText, GuiUtil.setConstraints(0,0,1.0,1.0,GridBagConstraints.BOTH,BORDER_THICKNESS,BORDER_THICKNESS,BORDER_THICKNESS,BORDER_THICKNESS));
		browsePanel.add(browseButton, GuiUtil.setConstraints(1,0,0.0,0.0,GridBagConstraints.NONE,BORDER_THICKNESS,BORDER_THICKNESS,BORDER_THICKNESS,BORDER_THICKNESS));

		versioningPanel = new JPanel();
		versioningPanel.setLayout(new GridBagLayout());
		versioningPanel.setBorder(BorderFactory.createTitledBorder(""));


		

		DropCutCopyPasteHandler handler = new DropCutCopyPasteHandler();

		fileListModel = new DefaultListModel();
		fileList.setModel(fileListModel);
		fileList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		fileList.setTransferHandler(handler);

		DropTarget dropTarget = new DropTarget(fileList, handler);
		fileList.setDropTarget(dropTarget);
		setDropTarget(dropTarget);

		ActionMap actionMap = fileList.getActionMap();
		actionMap.put(TransferHandler.getCutAction().getValue(Action.NAME), TransferHandler.getCutAction());
		actionMap.put(TransferHandler.getCopyAction().getValue(Action.NAME), TransferHandler.getCopyAction());
		actionMap.put(TransferHandler.getPasteAction().getValue(Action.NAME), TransferHandler.getPasteAction());

		InputMap inputMap = fileList.getInputMap();
		inputMap.put(KeyStroke.getKeyStroke('X', InputEvent.CTRL_MASK), TransferHandler.getCutAction().getValue(Action.NAME));
		inputMap.put(KeyStroke.getKeyStroke('C', InputEvent.CTRL_MASK), TransferHandler.getCopyAction().getValue(Action.NAME));
		inputMap.put(KeyStroke.getKeyStroke('V', InputEvent.CTRL_MASK), TransferHandler.getPasteAction().getValue(Action.NAME));
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), TransferHandler.getCutAction().getValue(Action.NAME));
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0), TransferHandler.getCutAction().getValue(Action.NAME));

//		PopupMenuDecorator.getInstance().decorate(fileList, workspaceText);
		PopupMenuDecorator.getInstance().decorate(fileList);
		
		OpenFileButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				loadFile(Internal.I18N.getString("main.tabbedPane.import"));
			}
		});

		removeButton.setActionCommand((String)TransferHandler.getCutAction().getValue(Action.NAME));
		removeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String action = (String)e.getActionCommand();
				Action a = fileList.getActionMap().get(action);
				if (a != null)
					a.actionPerformed(new ActionEvent(fileList, ActionEvent.ACTION_PERFORMED, null));
			}
		});
		removeButton.setEnabled(false);



		fileList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting())
					removeButton.setEnabled(true);
			}
		});

		setLayout(new GridBagLayout());

		JPanel filePanel = new JPanel();
		JPanel fileButton = new JPanel();
		add(filePanel,GuiUtil.setConstraints(0,0,1.0,0.0,GridBagConstraints.HORIZONTAL,10,5,5,5));
		filePanel.setLayout(new GridBagLayout());
		JScrollPane fileScroll = new JScrollPane(fileList);
		fileScroll.setPreferredSize(fileScroll.getPreferredSize());

		filePanel.add(fileScroll, GuiUtil.setConstraints(0,0,1.0,1.0,GridBagConstraints.BOTH,5,5,5,5));
		filePanel.add(fileButton, GuiUtil.setConstraints(1,0,0.0,0.0,GridBagConstraints.BOTH,5,5,5,5));
		fileButton.setLayout(new GridBagLayout());
		fileButton.add(OpenFileButton, GuiUtil.setConstraints(0,0,0.0,0.0,GridBagConstraints.HORIZONTAL,0,0,0,0));
		fileButton.add(removeButton, GuiUtil.setConstraints(0,1,0.0,1.0,GridBagConstraints.NORTH,GridBagConstraints.HORIZONTAL,5,0,15,0));

		
		
		
		cityObject = new DefaultMutableTreeNode(FeatureClassMode.CITYOBJECT);
		building = new DefaultMutableTreeNode(FeatureClassMode.BUILDING);
		waterBody = new DefaultMutableTreeNode(FeatureClassMode.WATERBODY);
		landUse = new DefaultMutableTreeNode(FeatureClassMode.LANDUSE);
		vegetation = new DefaultMutableTreeNode(FeatureClassMode.VEGETATION);
		transportation = new DefaultMutableTreeNode(FeatureClassMode.TRANSPORTATION);
		relief = new DefaultMutableTreeNode(FeatureClassMode.RELIEFFEATURE);
		cityFurniture = new DefaultMutableTreeNode(FeatureClassMode.CITYFURNITURE);
		genericCityObject = new DefaultMutableTreeNode(FeatureClassMode.GENERICCITYOBJECT);
		cityObjectGroup = new DefaultMutableTreeNode(FeatureClassMode.CITYOBJECTGROUP);

		cityObject.add(building);
		cityObject.add(waterBody);
		cityObject.add(landUse);
		cityObject.add(vegetation);
		cityObject.add(transportation);
		cityObject.add(relief);
		cityObject.add(cityFurniture);
		cityObject.add(genericCityObject);
		cityObject.add(cityObjectGroup);

		fcTree = new CheckboxTree(cityObject);
		fcTree.setRowHeight((int)(new JCheckBox().getPreferredSize().getHeight()) - 4);		
		fcTree.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(), 
				BorderFactory.createEmptyBorder(0,0,BORDER_THICKNESS,0)));
		
		// get rid of standard icons
		DefaultCheckboxTreeCellRenderer renderer = (DefaultCheckboxTreeCellRenderer)fcTree.getCellRenderer();
		renderer.setLeafIcon(null);
		renderer.setOpenIcon(null);
		renderer.setClosedIcon(null);
		
		
		
		DecimalFormat bboxFormat = new DecimalFormat("#######", DecimalFormatSymbols.getInstance(Locale.ENGLISH));	
		srsField = new JFormattedTextField(bboxFormat);	
		srsField.setFocusLostBehavior(JFormattedTextField.COMMIT);
		srsLabel = new JLabel("Reference System");				
		JPanel inputFieldsPanel = new JPanel();
		inputFieldsPanel.setLayout(new GridBagLayout());
		inputFieldsPanel.add(srsLabel, GuiUtil.setConstraints(0,0,0.0,0.0,GridBagConstraints.NONE,0,0,0,5));
		inputFieldsPanel.add(srsField, GuiUtil.setConstraints(1,0,1.0,0.0,GridBagConstraints.HORIZONTAL,0,5,0,5));

		
		
		JPanel exportButtonPanel = new JPanel();
		exportButtonPanel.add(exportButton);
		
	
		
		JPanel scrollView = new JPanel();
		scrollView.setLayout(new GridBagLayout());
		scrollView.add(browsePanel, GuiUtil.setConstraints(0,0,1.0,0.0,GridBagConstraints.HORIZONTAL,0,5,0,5));		
		scrollView.add(inputFieldsPanel, GuiUtil.setConstraints(0,1,1.0,0.0,GridBagConstraints.HORIZONTAL,0,5,0,5));
		scrollView.add(featureClassesLabel, GuiUtil.setConstraints(0,2,1.0,0.0,GridBagConstraints.HORIZONTAL,5,8,0,0));
		scrollView.add(fcTree, GuiUtil.setConstraints(0,3,1.0,1.0,GridBagConstraints.NORTH,GridBagConstraints.HORIZONTAL,5,7,0,7));
		JScrollPane scrollPane = new JScrollPane(scrollView);
		scrollPane.setBorder(BorderFactory.createEmptyBorder());
		scrollPane.setViewportBorder(BorderFactory.createEmptyBorder());
		
		

	//	this.setLayout(new GridBagLayout());	
	//	this.add(browsePanel, GuiUtil.setConstraints(0,0,1.0,0.0,GridBagConstraints.BOTH,10,5,5,5));
		//this.add(openFilePanel, GuiUtil.setConstraints(0,0,1.0,0.0,GridBagConstraints.BOTH,10,5,5,5));
		this.add(scrollPane, GuiUtil.setConstraints(0,1,1.0,1.0,GridBagConstraints.BOTH,0,0,0,0));		
		this.add(exportButtonPanel, GuiUtil.setConstraints(0,3,1.0,0.0,GridBagConstraints.BOTH,5,5,5,5));

	
	}

	// localized Labels und Strings
	public void doTranslation() {

//		Internal.I18N.getString("common.button.browse")
		OpenFileButton.setText(Internal.I18N.getString("common.button.browse"));
		removeButton.setText(Internal.I18N.getString("import.button.remove"));
		
		browseButton.setText(Internal.I18N.getString("common.button.browse"));

		((TitledBorder)versioningPanel.getBorder()).setTitle(Internal.I18N.getString("common.border.versioning"));


		featureClassesLabel.setText(Internal.I18N.getString("filter.border.featureClass"));
		
		exportButton.setText(Internal.I18N.getString("export.button.export"));
	}

	private void clearGui() {
		
		browseText.setText("");

	}

	public void loadSettings() {
		
		clearGui();


		KmlExporter kmlExporter = config.getProject().getKmlExporter();
		if (kmlExporter == null) return;

		

		// this block should be under the former else block
		if (kmlExporter.getFilter().getComplexFilter().getFeatureClass().isSetBuilding()) {
			fcTree.getCheckingModel().addCheckingPath(new TreePath(building.getPath()));
		}
		else {
			fcTree.getCheckingModel().removeCheckingPath(new TreePath(building.getPath()));
		}
		if (kmlExporter.getFilter().getComplexFilter().getFeatureClass().isSetWaterBody()) {
			fcTree.getCheckingModel().addCheckingPath(new TreePath(waterBody.getPath()));
		}
		else {
			fcTree.getCheckingModel().removeCheckingPath(new TreePath(waterBody.getPath()));
		}
		if (kmlExporter.getFilter().getComplexFilter().getFeatureClass().isSetLandUse()) {
			fcTree.getCheckingModel().addCheckingPath(new TreePath(landUse.getPath()));
		}
		else {
			fcTree.getCheckingModel().removeCheckingPath(new TreePath(landUse.getPath()));
		}
		if (kmlExporter.getFilter().getComplexFilter().getFeatureClass().isSetVegetation()) {
			fcTree.getCheckingModel().addCheckingPath(new TreePath(vegetation.getPath()));
		}
		else {
			fcTree.getCheckingModel().removeCheckingPath(new TreePath(vegetation.getPath()));
		}
		if (kmlExporter.getFilter().getComplexFilter().getFeatureClass().isSetTransportation()) {
			fcTree.getCheckingModel().addCheckingPath(new TreePath(transportation.getPath()));
		}
		else {
			fcTree.getCheckingModel().removeCheckingPath(new TreePath(transportation.getPath()));
		}
		if (kmlExporter.getFilter().getComplexFilter().getFeatureClass().isSetReliefFeature()) {
			fcTree.getCheckingModel().addCheckingPath(new TreePath(relief.getPath()));
		}
		else {
			fcTree.getCheckingModel().removeCheckingPath(new TreePath(relief.getPath()));
		}
		if (kmlExporter.getFilter().getComplexFilter().getFeatureClass().isSetCityFurniture()) {
			fcTree.getCheckingModel().addCheckingPath(new TreePath(cityFurniture.getPath()));
		}
		else {
			fcTree.getCheckingModel().removeCheckingPath(new TreePath(cityFurniture.getPath()));
		}
		if (kmlExporter.getFilter().getComplexFilter().getFeatureClass().isSetGenericCityObject()) {
			fcTree.getCheckingModel().addCheckingPath(new TreePath(genericCityObject.getPath()));
		}
		else {
			fcTree.getCheckingModel().removeCheckingPath(new TreePath(genericCityObject.getPath()));
		}
		if (kmlExporter.getFilter().getComplexFilter().getFeatureClass().isSetCityObjectGroup()) {
			fcTree.getCheckingModel().addCheckingPath(new TreePath(cityObjectGroup.getPath()));
		}
		else {
			fcTree.getCheckingModel().removeCheckingPath(new TreePath(cityObjectGroup.getPath()));
		}
		// end of block


	}

	public void setSettings() {


		File[] importFiles = new File[fileListModel.size()]; 
		for (int i = 0; i < fileListModel.size(); ++i)
			importFiles[i] = new File(fileListModel.get(i).toString());

		config.getInternal().setImportFiles(importFiles);		


		
		config.getInternal().setExportFileName(browseText.getText().trim());

	}
	

		
	

	private void addListeners() {
		
		enableEvents(AWTEvent.WINDOW_EVENT_MASK);

		exportButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Thread thread = new Thread() {
					public void run() {
						//doExport();
						
						try {
							doImport();
						} catch (SQLException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				};
				thread.setDaemon(true);
				thread.start();
			}
		});

		browseButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveFile();
			}
		});

		ActionListener filterListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			//	setFilterEnabledValues();
			}
		};


	}
	
	
	private void doImport() throws SQLException {
		final ReentrantLock lock = this.mainLock;
		lock.lock();

		try {
			mainView.clearConsole();
			setSettings();

			ImportFilterConfig filter = config.getProject().getImporter().getFilter();

			// check all input values...
			if (config.getInternal().getImportFiles() == null || config.getInternal().getImportFiles().length == 0) {
				mainView.errorMessage(Internal.I18N.getString("import.dialog.error.incompleteData"), 
						Internal.I18N.getString("import.dialog.error.incompleteData.dataset"));
				return;
			}

			// gmlId
			if (filter.isSetSimpleFilter() &&
					filter.getSimpleFilter().getGmlIdFilter().getGmlIds().isEmpty()) {
				mainView.errorMessage(Internal.I18N.getString("import.dialog.error.incorrectData"), 
						Internal.I18N.getString("common.dialog.error.incorrectData.gmlId"));
				return;
			}

			// cityObject
			if (filter.isSetComplexFilter() &&
					filter.getComplexFilter().getFeatureCount().isSet()) {
				Long coStart = filter.getComplexFilter().getFeatureCount().getFrom();
				Long coEnd = filter.getComplexFilter().getFeatureCount().getTo();
				String coEndValue = String.valueOf(filter.getComplexFilter().getFeatureCount().getTo());

				if (coStart == null || (!coEndValue.trim().equals("") && coEnd == null)) {
					mainView.errorMessage(Internal.I18N.getString("import.dialog.error.incorrectData"), 
							Internal.I18N.getString("import.dialog.error.incorrectData.range"));
					return;
				}

				if ((coStart != null && coStart <= 0) || (coEnd != null && coEnd <= 0)) {
					mainView.errorMessage(Internal.I18N.getString("import.dialog.error.incorrectData"),
							Internal.I18N.getString("import.dialog.error.incorrectData.range"));
					return;
				}

				if (coEnd != null && coEnd < coStart) {
					mainView.errorMessage(Internal.I18N.getString("import.dialog.error.incorrectData"),
							Internal.I18N.getString("import.dialog.error.incorrectData.range"));
					return;
				}
			}

			// gmlName
			if (filter.isSetComplexFilter() &&
					filter.getComplexFilter().getGmlName().isSet() &&
					filter.getComplexFilter().getGmlName().getValue().trim().equals("")) {
				mainView.errorMessage(Internal.I18N.getString("import.dialog.error.incorrectData"),
						Internal.I18N.getString("common.dialog.error.incorrectData.gmlName"));
				return;
			}

			// BoundingBox
			if (filter.isSetComplexFilter() &&
					filter.getComplexFilter().getBoundingBox().isSet()) {
				Double xMin = filter.getComplexFilter().getBoundingBox().getLowerLeftCorner().getX();
				Double yMin = filter.getComplexFilter().getBoundingBox().getLowerLeftCorner().getY();
				Double xMax = filter.getComplexFilter().getBoundingBox().getUpperRightCorner().getX();
				Double yMax = filter.getComplexFilter().getBoundingBox().getUpperRightCorner().getY();

				if (xMin == null || yMin == null || xMax == null || yMax == null) {
					mainView.errorMessage(Internal.I18N.getString("import.dialog.error.incorrectData"),
							Internal.I18N.getString("common.dialog.error.incorrectData.bbox"));
					return;
				}
			}

			// affine transformation
			if (config.getProject().getImporter().getAffineTransformation().isSetUseAffineTransformation()) {
				if (JOptionPane.showConfirmDialog(
						mainView, 
						Internal.I18N.getString("import.dialog.warning.affineTransformation"),
						Internal.I18N.getString("common.dialog.warning.title"), 
						JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION)
					return;				
			}



			mainView.setStatusText(Internal.I18N.getString("main.status.import.label"));
			LOG.info("Initializing CityGML import...");

			// initialize event dispatcher
			final EventDispatcher eventDispatcher = ObjectRegistry.getInstance().getEventDispatcher();
			//final ImportStatusDialog importDialog = new ImportStatusDialog(mainView, 
			//		Internal.I18N.getString("import.dialog.window"), 
			//		Internal.I18N.getString("import.dialog.msg"));

			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
				//	importDialog.setLocationRelativeTo(mainView);
				//	importDialog.setVisible(true);
				}
			});

			CityKmlImporter importer = new CityKmlImporter(jaxbBuilder, dbPool, config, eventDispatcher);

			/*importDialog.getCancelButton().addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							eventDispatcher.triggerEvent(new InterruptEvent(
									InterruptEnum.USER_ABORT, 
									"User abort of database import.", 
									LogLevel.INFO, 
									this));
						}
					});
				}
			});*/

			
			
			
			
			
			boolean success = importer.doProcess();
			
			
			
			try {
				eventDispatcher.flushEvents();
			} catch (InterruptedException e1) {
				//
			}

			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
				//	importDialog.dispose();
				}
			});

			// cleanup
			importer.cleanup();

			if (success) {
				
				LOG.info("CityGML import successfully finished.");
				
				//*******************Shahin Sharifi****************************
				doExport(importer);
				
				
			} else {
				LOG.warn("CityGML import aborted.");
			}

			mainView.setStatusText(Internal.I18N.getString("main.status.ready.label"));
		} finally {
			lock.unlock();
		}
	}
	
	

	private void doExport(CityKmlImporter _importer) throws SQLException {
		
		final ReentrantLock lock = this.mainLock;
		lock.lock();

		try {
			mainView.clearConsole();
			setSettings();

			ExportFilterConfig filter = config.getProject().getKmlExporter().getFilter();
//			Database db = config.getProject().getDatabase();

			// check all input values...
			if (config.getInternal().getExportFileName().trim().equals("")) {
				mainView.errorMessage(Internal.I18N.getString("kmlExport.dialog.error.incompleteData"), 
						Internal.I18N.getString("kmlExport.dialog.error.incompleteData.dataset"));
				return;
			}

			// workspace timestamp
//			if (!Util.checkWorkspaceTimestamp(db.getWorkspaces().getExportWorkspace())) {
//				mainView.errorMessage(Internal.I18N.getString("export.dialog.error.incorrectData"), 
//						Internal.I18N.getString("export.dialog.error.incorrectData.date"));
//				return;
//			}

			// gmlId
			if (filter.isSetSimpleFilter() &&
					filter.getSimpleFilter().getGmlIdFilter().getGmlIds().isEmpty()) {
				mainView.errorMessage(Internal.I18N.getString("export.dialog.error.incorrectData"), 
						Internal.I18N.getString("common.dialog.error.incorrectData.gmlId"));
				return;
			}

			// DisplayForms
			int activeDisplayFormsAmount =
				KmlExporter.getActiveDisplayFormsAmount(config.getProject().getKmlExporter().getBuildingDisplayForms()); 
			if (activeDisplayFormsAmount == 0) {
				mainView.errorMessage(Internal.I18N.getString("export.dialog.error.incorrectData"), 
						Internal.I18N.getString("kmlExport.dialog.error.incorrectData.displayForms"));
				return;
			}

			// initialize event dispatcher
			final EventDispatcher eventDispatcher = ObjectRegistry.getInstance().getEventDispatcher();
			de.tub.citydb.modules.citykml.controller.CityKmlExporter CityKmlExporter = new de.tub.citydb.modules.citykml.controller.CityKmlExporter(jaxbKmlContext, jaxbColladaContext, dbPool, config, eventDispatcher);

			// BoundingBox check
			if (filter.isSetComplexFilter() &&
				filter.getComplexFilter().getTiledBoundingBox().isSet()) {
				Double xMin = filter.getComplexFilter().getTiledBoundingBox().getLowerLeftCorner().getX();
				Double yMin = filter.getComplexFilter().getTiledBoundingBox().getLowerLeftCorner().getY();
				Double xMax = filter.getComplexFilter().getTiledBoundingBox().getUpperRightCorner().getX();
				Double yMax = filter.getComplexFilter().getTiledBoundingBox().getUpperRightCorner().getY();

				if (xMin == null || yMin == null || xMax == null || yMax == null) {
					mainView.errorMessage(Internal.I18N.getString("export.dialog.error.incorrectData"),
							Internal.I18N.getString("common.dialog.error.incorrectData.bbox"));
					return;
				}
			}

			// Feature classes check
			if (filter.isSetComplexFilter() &&
			   !filter.getComplexFilter().getFeatureClass().isSetBuilding() &&
			   !filter.getComplexFilter().getFeatureClass().isSetCityFurniture() &&
			   !filter.getComplexFilter().getFeatureClass().isSetCityObjectGroup() &&
			   !filter.getComplexFilter().getFeatureClass().isSetGenericCityObject() &&
			   !filter.getComplexFilter().getFeatureClass().isSetLandUse() &&
			   !filter.getComplexFilter().getFeatureClass().isSetReliefFeature() &&
			   !filter.getComplexFilter().getFeatureClass().isSetTransportation() &&
			   !filter.getComplexFilter().getFeatureClass().isSetVegetation() &&
			   !filter.getComplexFilter().getFeatureClass().isSetWaterBody()) {
				mainView.errorMessage(Internal.I18N.getString("export.dialog.error.incorrectData"),
						Internal.I18N.getString("kmlExport.dialog.error.incorrectData.featureClass"));
				return;
			}
			
			if (!dbPool.isConnected()) {
				mainView.connectToDatabase();

				if (!dbPool.isConnected())
					return;
			}

			// tile amount calculation
			int tileAmount = 1;
			/*if (filter.isSetComplexFilter() &&
				filter.getComplexFilter().getTiledBoundingBox().isSet()) {
				try {
					tileAmount = CityKmlExporter.calculateRowsColumnsAndDelta();
				}
				catch (SQLException sqle) {
					String srsDescription = filter.getComplexFilter().getBoundingBox().getSrs().getDescription();
					Logger.getInstance().error(srsDescription + " " + sqle.getMessage());
					return;
				}
			}
			tileAmount = tileAmount * activeDisplayFormsAmount;
*/
			
		/*	mainView.setStatusText(Internal.I18N.getString("main.status.kmlExport.label"));
			Logger.getInstance().info("Initializing database export...");

			final ExportStatusDialog exportDialog = new ExportStatusDialog(mainView, 
					Internal.I18N.getString("kmlExport.dialog.window"),
					Internal.I18N.getString("export.dialog.msg"),
					tileAmount);

			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					exportDialog.setLocationRelativeTo(mainView);
					exportDialog.setVisible(true);
				}
			});

			exportDialog.getCancelButton().addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							eventDispatcher.triggerEvent(new InterruptEvent(
									InterruptEnum.USER_ABORT, 
									"User abort of database export.", 
									LogLevel.INFO, 
									this));
						}
					});
				}
			});

			
			*/

			String TargetFile = "";			
			
			if(browseText.getText().equals("")){				
			
				TargetFile = saveFile();
				
			}else {
				
				TargetFile = browseText.getText();			
			}
			

			CityKmlImportWorkerFactory _CityKmlImportWorker = _importer.GetKmlImportWorker();			
			
			CityKmlExporter.SetTargetSrs((!srsField.getText().equals("")) ? srsField.getText() : "4326");
			CityKmlExporter.SetTargetFile(TargetFile);
			CityKmlExporter.SetPointList(((CityKmlImportWorker)_CityKmlImportWorker.createWorker()).GetCityGmlObject());
		//	CityKmlExporter.SetBuilding(((CityKmlImportWorker)_CityKmlImportWorker.createWorker()).GetBuilding());

					
			boolean success=false;
			try {
				success = CityKmlExporter.doProcess();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			
			
			try {
				eventDispatcher.flushEvents();
			} catch (InterruptedException e1) {
				//
			}

			SwingUtilities.invokeLater(new Runnable() {
				
				public void run() {
					
					//exportDialog.dispose();
				
				}
			});
			
			// cleanup
			CityKmlExporter.cleanup();

			if (success) {
				Logger.getInstance().info("KML export successfully finished.");

			} else {
				Logger.getInstance().warn("KML export aborted.");
			}

			mainView.setStatusText(Internal.I18N.getString("main.status.ready.label"));
		} finally {
			lock.unlock();
		}
	}
	
	

	private void setFilterEnabledValues() {
		
	}

	private void setVisibilityEnabledValues() {

	}

	public static void centerOnScreen(Component component) {
		
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		int x = (screen.width - component.getSize().width)/2;
		int y = (screen.height - component.getSize().height)/2;
		component.setBounds(x, y, component.getSize().width, component.getSize().height);
	}

	public Dimension getPreferredSize() {
		return new Dimension(PREFERRED_WIDTH, PREFERRED_HEIGHT);
	}

	private String saveFile() {
		
		JFileChooser fileChooser = new JFileChooser();

		FileNameExtensionFilter filter = new FileNameExtensionFilter("KML Files (*.kml)", "kml");
		fileChooser.addChoosableFileFilter(filter);
		fileChooser.addChoosableFileFilter(fileChooser.getAcceptAllFileFilter());
		fileChooser.setFileFilter(filter);

		if (config.getProject().getKmlExporter().getPath().isSetLastUsedMode()) {
		
			fileChooser.setCurrentDirectory(new File(config.getProject().getKmlExporter().getPath().getLastUsedPath()));
		
		} else {
			
			fileChooser.setCurrentDirectory(new File(config.getProject().getExporter().getPath().getStandardPath()));
		}
		
		int result = fileChooser.showSaveDialog(getTopLevelAncestor());
		
		if (result == JFileChooser.CANCEL_OPTION) 
			return "sample";
		
		try {
			
			String exportString = fileChooser.getSelectedFile().toString();
			if (exportString.lastIndexOf('.') != -1	&&
				exportString.lastIndexOf('.') > exportString.lastIndexOf(File.separator)) {
				exportString = exportString.substring(0, exportString.lastIndexOf('.'));
			}
			
			exportString = config.getProject().getKmlExporter().isExportAsKmz() ?
					exportString + ".kml":
						exportString + ".kml";

			browseText.setText(exportString);
			config.getProject().getKmlExporter().getPath().setLastUsedPath(fileChooser.getCurrentDirectory().getAbsolutePath());
			
		}
		catch (Exception e) {
			//
		}
		
		return browseText.getText();
	}

	
	@Override
	public void handleEvent(Event event) throws Exception {
		
	}

	private class ThemeUpdater extends Thread {
		
		public void run() {
			
		}
	}
	
	
	
	private void loadFile(String title) {
		JFileChooser chooser = new JFileChooser();
		chooser.setDialogTitle(title);
		chooser.setMultiSelectionEnabled(true);
		chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

		FileNameExtensionFilter filter = new FileNameExtensionFilter("CityGML Files (*.gml, *.xml)", "xml", "gml");
		chooser.addChoosableFileFilter(filter);
		chooser.addChoosableFileFilter(chooser.getAcceptAllFileFilter());
		chooser.setFileFilter(filter);

		if (fileListModel.isEmpty()) {
			if (config.getProject().getImporter().getPath().isSetLastUsedMode()) {
				chooser.setCurrentDirectory(new File(config.getProject().getImporter().getPath().getLastUsedPath()));
			} else {
				chooser.setCurrentDirectory(new File(config.getProject().getImporter().getPath().getStandardPath()));
			}
		} else
			chooser.setCurrentDirectory(new File(fileListModel.get(0).toString()));

		int result = chooser.showOpenDialog(getTopLevelAncestor());
		if (result == JFileChooser.CANCEL_OPTION) 
			return;

		fileListModel.clear();
		for (File file : chooser.getSelectedFiles())
			fileListModel.addElement(file.toString());

		config.getProject().getImporter().getPath().setLastUsedPath(chooser.getCurrentDirectory().getAbsolutePath());
	}
	
	
	
	private final class DropCutCopyPasteHandler extends TransferHandler implements DropTargetListener {

		@Override
		public boolean importData(TransferHandler.TransferSupport info) {	    	
			if (!info.isDataFlavorSupported(DataFlavor.stringFlavor))
				return false;

			if (info.isDrop())
				return false;

			List<String> fileNames = new ArrayList<String>();
			try {
				String value = (String)info.getTransferable().getTransferData(DataFlavor.stringFlavor);
				StringTokenizer t = new StringTokenizer(value, System.getProperty("line.separator"));

				while (t.hasMoreTokens()) {
					File file = new File(t.nextToken());
					if (file.exists())
						fileNames.add(file.getCanonicalPath());
					else
						LOG.warn("Failed to paste from clipboard: '" + file.getAbsolutePath() + "' is not a file.");
				}

				if (!fileNames.isEmpty()) {
					addFileNames(fileNames);
					return true;
				}
			} catch (UnsupportedFlavorException ufe) {
				//
			} catch (IOException ioe) {
				//
			}

			return false;
		}

		@Override
		protected Transferable createTransferable(JComponent c) {
			int[] indices = fileList.getSelectedIndices();
			String newLine = System.getProperty("line.separator");

			StringBuilder builder = new StringBuilder();
			for (int i = 0; i < indices.length; i++) {
				builder.append((String)fileList.getModel().getElementAt(indices[i]));
				if (i < indices.length - 1)
					builder.append(newLine);
			}

			return new StringSelection(builder.toString());
		}

		@Override
		public int getSourceActions(JComponent c) {
			return COPY_OR_MOVE;
		}

		@Override
		protected void exportDone(JComponent c, Transferable data, int action) {
			if (action != MOVE)
				return;

			if (!fileList.isSelectionEmpty()) {
				int[] indices = fileList.getSelectedIndices();
				int first = indices[0];		

				for (int i = indices.length - 1; i >= 0; i--)
					fileListModel.remove(indices[i]);

				if (first > fileListModel.size() - 1)
					first = fileListModel.size() - 1;

				if (fileListModel.isEmpty())
					removeButton.setEnabled(false);
				else
					fileList.setSelectedIndex(first);
			}
		}

		@Override
		public void dragEnter(DropTargetDragEvent dtde) {
			dtde.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE);
		}

		@SuppressWarnings("unchecked")
		@Override
		public void drop(DropTargetDropEvent dtde) {
			for (DataFlavor dataFlover : dtde.getCurrentDataFlavors()) {
				if (dataFlover.isFlavorJavaFileListType()) {
					try {
						dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);

						List<String> fileNames = new ArrayList<String>();
						for (File file : (List<File>)dtde.getTransferable().getTransferData(DataFlavor.javaFileListFlavor))
							if (file.exists())
								fileNames.add(file.getCanonicalPath());
							else
								LOG.warn("Failed to drop from clipboard: '" + file.getAbsolutePath() + "' is not a file.");

						if (!fileNames.isEmpty()) {
							if (dtde.getDropAction() != DnDConstants.ACTION_COPY)
								fileListModel.clear();

							addFileNames(fileNames);
						}

						dtde.getDropTargetContext().dropComplete(true);	
					} catch (UnsupportedFlavorException e1) {
						//
					} catch (IOException e2) {
						//
					}
				}
			}
		}

		private void addFileNames(List<String> fileNames) {
			int index = fileList.getSelectedIndex() + 1;
			for (String fileName : fileNames)
				fileListModel.add(index++, fileName);

			config.getProject().getImporter().getPath().setLastUsedPath(
					new File(fileListModel.getElementAt(0).toString()).getAbsolutePath());
		}

		@Override
		public void dropActionChanged(DropTargetDragEvent dtde) {
			// nothing to do here
		}

		@Override
		public void dragExit(DropTargetEvent dte) {
			// nothing to do here
		}

		@Override
		public void dragOver(DropTargetDragEvent dtde) {
			// nothing to do here
		}
	}
	
	
}
