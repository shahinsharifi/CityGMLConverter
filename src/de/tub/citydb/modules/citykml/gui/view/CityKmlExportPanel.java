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
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;

import org.citygml4j.builder.jaxb.JAXBBuilder;
import org.citygml4j.builder.jaxb.xml.io.reader.CityGMLChunk;
import org.citygml4j.builder.jaxb.xml.io.reader.JAXBChunkReader;
import org.citygml4j.model.citygml.CityGML;
import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.model.citygml.core.CityModel;
import org.citygml4j.xml.io.CityGMLInputFactory;
import org.citygml4j.xml.io.reader.CityGMLInputFilter;
import org.citygml4j.xml.io.reader.CityGMLReadException;
import org.citygml4j.xml.io.reader.FeatureReadMode;
import org.citygml4j.xml.io.reader.MissingADESchemaException;
import org.xml.sax.SAXException;

import de.tub.citydb.api.concurrent.WorkerPool;
import de.tub.citydb.api.database.DatabaseSrs;
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
import de.tub.citydb.config.project.general.AffineTransformation;
import de.tub.citydb.config.project.general.FeatureClassMode;
import de.tub.citydb.config.project.importer.ImportFilterConfig;
import de.tub.citydb.config.project.importer.XMLValidation;
import de.tub.citydb.config.project.kmlExporter.KmlExporter;
import de.tub.citydb.config.project.CitykmlExporter.CityKmlExporter;
import de.tub.citydb.config.project.CitykmlExporter.DisplayForm;
import de.tub.citydb.database.DatabaseConnectionPool;
import de.tub.citydb.gui.ImpExpGui;
import de.tub.citydb.gui.components.checkboxtree.DefaultCheckboxTreeCellRenderer;
import de.tub.citydb.gui.components.checkboxtree.DefaultTreeCheckingModel;
import de.tub.citydb.gui.components.ExportStatusDialog;
import de.tub.citydb.gui.components.ImportStatusDialog;
import de.tub.citydb.gui.components.bbox.BoundingBoxPanelImpl;
import de.tub.citydb.gui.components.checkboxtree.CheckboxTree;
import de.tub.citydb.gui.factory.PopupMenuDecorator;
import de.tub.citydb.io.DirectoryScanner;
import de.tub.citydb.io.DirectoryScanner.CityGMLFilenameFilter;
import de.tub.citydb.log.Logger;
import de.tub.citydb.modules.citygml.importer.util.AffineTransformer;
import de.tub.citydb.modules.citykml.concurrent.CityKmlFeatureReaderWorkerFactory;
import de.tub.citydb.modules.citykml.concurrent.CityKmlImportWorker;
import de.tub.citydb.modules.citykml.concurrent.CityKmlImportWorkerFactory;
import de.tub.citydb.modules.citykml.controller.CityKmlImporter;

import de.tub.citydb.modules.common.event.CounterEvent;
import de.tub.citydb.modules.common.event.CounterType;
import de.tub.citydb.modules.common.event.InterruptEnum;
import de.tub.citydb.modules.common.event.InterruptEvent;

import de.tub.citydb.modules.common.event.StatusDialogMessage;
import de.tub.citydb.modules.common.event.StatusDialogProgressBar;
import de.tub.citydb.modules.common.event.StatusDialogTitle;
import de.tub.citydb.modules.common.filter.ImportFilter;
import de.tub.citydb.modules.common.filter.statistic.FeatureCounterFilter;
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


	private JLabel rowsLabel = new JLabel();
	private JTextField rowsText = new JTextField("");
	private JLabel columnsLabel = new JLabel();
	private JTextField columnsText = new JTextField("");

	//*********************New Attributes************************************


	private ButtonGroup filterButtonGroup = new ButtonGroup();

	private JPanel filterPanel;
	private JRadioButton singleBuildingRadioButton = new JRadioButton("");
	private JLabel gmlIdLabel = new JLabel("gml:id");
	private JTextField gmlIdText = new JTextField("");

	private JRadioButton boundingBoxRadioButton = new JRadioButton("");
	private BoundingBoxPanelImpl bboxComponent;

	private JPanel tilingPanel;
	private ButtonGroup tilingButtonGroup = new ButtonGroup();
	private JRadioButton noTilingRadioButton = new JRadioButton("");
	private JRadioButton automaticTilingRadioButton = new JRadioButton("");
	private JRadioButton manualTilingRadioButton = new JRadioButton("");

	private JPanel exportFromLODPanel;
	private JComboBox lodComboBox = new JComboBox();

	private JPanel displayAsPanel;
	private JCheckBox footprintCheckbox = new JCheckBox();
	private JCheckBox extrudedCheckbox = new JCheckBox();
	private JCheckBox geometryCheckbox = new JCheckBox();
	private JCheckBox colladaCheckbox = new JCheckBox();

	private JLabel visibleFromFootprintLabel = new JLabel();
	private JTextField footprintVisibleFromText = new JTextField("", 3);
	private JLabel pixelsFootprintLabel = new JLabel();
	private JLabel visibleFromExtrudedLabel = new JLabel();
	private JTextField extrudedVisibleFromText = new JTextField("", 3);
	private JLabel pixelsExtrudedLabel = new JLabel();
	private JLabel visibleFromGeometryLabel = new JLabel();
	private JTextField geometryVisibleFromText = new JTextField("", 3);
	private JLabel pixelsGeometryLabel = new JLabel();
	private JLabel visibleFromColladaLabel = new JLabel();
	private JTextField colladaVisibleFromText = new JTextField("", 3);
	private JLabel pixelsColladaLabel = new JLabel();

	private JLabel themeLabel = new JLabel();
	private JComboBox themeComboBox = new JComboBox();
	private JButton fetchThemesButton = new JButton(" ");


	//********************************************************************



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

		//	versioningPanel = new JPanel();
		//	versioningPanel.setLayout(new GridBagLayout());
		//	versioningPanel.setBorder(BorderFactory.createTitledBorder(""));




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

		//****************************New Components***************************************


		Box filterContentPanel = Box.createVerticalBox();
		filterButtonGroup.add(singleBuildingRadioButton);
		singleBuildingRadioButton.setIconTextGap(10);
		filterButtonGroup.add(boundingBoxRadioButton);
		boundingBoxRadioButton.setIconTextGap(10);
		boundingBoxRadioButton.setSelected(true);
		int lmargin = (int)(singleBuildingRadioButton.getPreferredSize().getWidth()) + 6;

		JPanel singleBuildingRadioPanel = new JPanel();
		singleBuildingRadioPanel.setLayout(new BorderLayout());
		singleBuildingRadioPanel.add(singleBuildingRadioButton, BorderLayout.WEST);

		Box singleBuildingPanel = Box.createHorizontalBox();
		singleBuildingPanel.add(Box.createRigidArea(new Dimension(lmargin, 0)));
		singleBuildingPanel.add(gmlIdLabel);
		singleBuildingPanel.add(Box.createRigidArea(new Dimension(BORDER_THICKNESS * 2, 0)));
		singleBuildingPanel.add(gmlIdText);
		singleBuildingPanel.add(Box.createRigidArea(new Dimension(BORDER_THICKNESS, 0)));

		JPanel boundingBoxRadioPanel = new JPanel();
		boundingBoxRadioPanel.setLayout(new GridBagLayout());
		boundingBoxRadioPanel.add(boundingBoxRadioButton, GuiUtil.setConstraints(0,0,1.0,1.0,GridBagConstraints.BOTH,0,0,0,BORDER_THICKNESS));

		JPanel boundingBoxPanel = new JPanel();
		boundingBoxPanel.setLayout(new GridBagLayout());
		bboxComponent = new BoundingBoxPanelImpl(config);

		boundingBoxPanel.add(bboxComponent, GuiUtil.setConstraints(0,0,1.0,0.0,GridBagConstraints.HORIZONTAL,2,lmargin,0,BORDER_THICKNESS));

		tilingButtonGroup.add(noTilingRadioButton);
		noTilingRadioButton.setIconTextGap(10);
		tilingButtonGroup.add(automaticTilingRadioButton);
		automaticTilingRadioButton.setIconTextGap(10);
		tilingButtonGroup.add(manualTilingRadioButton);
		manualTilingRadioButton.setIconTextGap(10);
		automaticTilingRadioButton.setSelected(true);

		tilingPanel = new JPanel();
		tilingPanel.setLayout(new GridBagLayout());
		tilingPanel.setBorder(BorderFactory.createTitledBorder(""));

		tilingPanel.add(noTilingRadioButton, GuiUtil.setConstraints(0,0,0.0,1.0,GridBagConstraints.BOTH,0,BORDER_THICKNESS * 2,BORDER_THICKNESS,0));
		tilingPanel.add(automaticTilingRadioButton, GuiUtil.setConstraints(1,0,0.0,1.0,GridBagConstraints.BOTH,0,BORDER_THICKNESS * 2,BORDER_THICKNESS,0));
		tilingPanel.add(manualTilingRadioButton, GuiUtil.setConstraints(2,0,0.0,1.0,GridBagConstraints.BOTH,0,BORDER_THICKNESS * 2,BORDER_THICKNESS,0));
		tilingPanel.add(rowsLabel, GuiUtil.setConstraints(3,0,0.0,1.0,GridBagConstraints.BOTH,0,BORDER_THICKNESS * 6,BORDER_THICKNESS,0));
		tilingPanel.add(rowsText, GuiUtil.setConstraints(4,0,0.5,1.0,GridBagConstraints.BOTH,0,BORDER_THICKNESS,BORDER_THICKNESS,0));
		tilingPanel.add(columnsLabel, GuiUtil.setConstraints(5,0,0.0,1.0,GridBagConstraints.BOTH,0,BORDER_THICKNESS * 3,BORDER_THICKNESS,0));
		tilingPanel.add(columnsText, GuiUtil.setConstraints(6,0,0.5,1.0,GridBagConstraints.BOTH,0,BORDER_THICKNESS,BORDER_THICKNESS,BORDER_THICKNESS * 2));

		Box tilingParentPanel = Box.createHorizontalBox();
		tilingParentPanel.add(Box.createRigidArea(new Dimension(BORDER_THICKNESS, 0)));
		tilingParentPanel.add(tilingPanel);
		tilingParentPanel.add(Box.createRigidArea(new Dimension(BORDER_THICKNESS, 0)));

		filterContentPanel.add(singleBuildingRadioPanel);
		filterContentPanel.add(singleBuildingPanel);
		filterContentPanel.add(Box.createRigidArea(new Dimension(0, BORDER_THICKNESS)));
		filterContentPanel.add(boundingBoxRadioPanel);
		filterContentPanel.add(boundingBoxPanel);
		filterContentPanel.add(Box.createRigidArea(new Dimension(0, BORDER_THICKNESS)));
		filterContentPanel.add(tilingParentPanel);
		filterContentPanel.add(Box.createRigidArea(new Dimension(0, BORDER_THICKNESS)));

		filterPanel = new JPanel();
		filterPanel.setLayout(new BorderLayout());
		filterPanel.setBorder(BorderFactory.createTitledBorder(""));
		filterPanel.add(filterContentPanel, BorderLayout.CENTER);

		exportFromLODPanel = new JPanel();
		exportFromLODPanel.setLayout(new GridBagLayout());
		exportFromLODPanel.setBorder(BorderFactory.createTitledBorder(""));

		for (int index = 0; index < 5; index++) {
			lodComboBox.insertItemAt("LoD" + index, index);
		}
		lodComboBox.insertItemAt(Internal.I18N.getString("kmlExport.label.highestLODAvailable"), lodComboBox.getItemCount());
		lodComboBox.setSelectedIndex(2);
		GridBagConstraints lcb = GuiUtil.setConstraints(0,0,0.0,1.0,GridBagConstraints.HORIZONTAL,BORDER_THICKNESS + footprintCheckbox.getPreferredSize().height,BORDER_THICKNESS,BORDER_THICKNESS,BORDER_THICKNESS);
		lcb.anchor = GridBagConstraints.NORTH;
		exportFromLODPanel.add(lodComboBox, lcb);
		lodComboBox.setMinimumSize(lodComboBox.getPreferredSize());
		exportFromLODPanel.setMinimumSize(exportFromLODPanel.getPreferredSize());

		displayAsPanel = new JPanel();
		displayAsPanel.setLayout(new GridBagLayout());
		displayAsPanel.setBorder(BorderFactory.createTitledBorder(""));

		footprintCheckbox.setIconTextGap(10);
		displayAsPanel.add(footprintCheckbox, GuiUtil.setConstraints(0,0,0.0,1.0,GridBagConstraints.BOTH,0,BORDER_THICKNESS,0,0));
		GridBagConstraints vffl = GuiUtil.setConstraints(2,0,0.0,1.0,GridBagConstraints.NONE,0,BORDER_THICKNESS,0,0);
		vffl.anchor = GridBagConstraints.EAST;
		displayAsPanel.add(visibleFromFootprintLabel, vffl);
		displayAsPanel.add(footprintVisibleFromText, GuiUtil.setConstraints(3,0,0.25,1.0,GridBagConstraints.BOTH,0,BORDER_THICKNESS,0,0));
		displayAsPanel.add(pixelsFootprintLabel, GuiUtil.setConstraints(4,0,0.0,1.0,GridBagConstraints.BOTH,0,BORDER_THICKNESS,0,BORDER_THICKNESS));

		extrudedCheckbox.setIconTextGap(10);
		displayAsPanel.add(extrudedCheckbox, GuiUtil.setConstraints(0,1,0.0,1.0,GridBagConstraints.BOTH,2,BORDER_THICKNESS,0,0));
		GridBagConstraints vfel = GuiUtil.setConstraints(2,1,0.0,1.0,GridBagConstraints.NONE,0,BORDER_THICKNESS,0,0);
		vfel.anchor = GridBagConstraints.EAST;
		displayAsPanel.add(visibleFromExtrudedLabel, vfel);
		displayAsPanel.add(extrudedVisibleFromText, GuiUtil.setConstraints(3,1,0.25,1.0,GridBagConstraints.BOTH,2,BORDER_THICKNESS,0,0));
		displayAsPanel.add(pixelsExtrudedLabel, GuiUtil.setConstraints(4,1,0.0,1.0,GridBagConstraints.BOTH,2,BORDER_THICKNESS,0,BORDER_THICKNESS));

		geometryCheckbox.setIconTextGap(10);
		displayAsPanel.add(geometryCheckbox, GuiUtil.setConstraints(0,2,0.0,1.0,GridBagConstraints.BOTH,2,BORDER_THICKNESS,0,0));
		GridBagConstraints vfgl = GuiUtil.setConstraints(2,2,0.0,1.0,GridBagConstraints.NONE,0,BORDER_THICKNESS,0,0);
		vfgl.anchor = GridBagConstraints.EAST;
		displayAsPanel.add(visibleFromGeometryLabel, vfgl);
		displayAsPanel.add(geometryVisibleFromText, GuiUtil.setConstraints(3,2,0.25,1.0,GridBagConstraints.BOTH,2,BORDER_THICKNESS,0,0));
		displayAsPanel.add(pixelsGeometryLabel, GuiUtil.setConstraints(4,2,0.0,1.0,GridBagConstraints.BOTH,2,BORDER_THICKNESS,0,BORDER_THICKNESS));

		colladaCheckbox.setIconTextGap(10);
		displayAsPanel.add(colladaCheckbox, GuiUtil.setConstraints(0,3,0.0,1.0,GridBagConstraints.BOTH,2,BORDER_THICKNESS,0,0));
		GridBagConstraints vfcl = GuiUtil.setConstraints(2,3,0.0,1.0,GridBagConstraints.NONE,0,BORDER_THICKNESS,0,0);
		vfcl.anchor = GridBagConstraints.EAST;
		displayAsPanel.add(visibleFromColladaLabel, vfcl);
		displayAsPanel.add(colladaVisibleFromText, GuiUtil.setConstraints(3,3,0.25,1.0,GridBagConstraints.BOTH,2,BORDER_THICKNESS,0,0));
		displayAsPanel.add(pixelsColladaLabel, GuiUtil.setConstraints(4,3,0.0,1.0,GridBagConstraints.BOTH,2,BORDER_THICKNESS,0,BORDER_THICKNESS));

		displayAsPanel.add(themeLabel, GuiUtil.setConstraints(0,4,0.0,1.0,GridBagConstraints.BOTH,BORDER_THICKNESS,32,BORDER_THICKNESS,0));
		//		themeComboBox.setMinimumSize(new Dimension(80, (int)themeComboBox.getPreferredSize().getHeight()));
		//		themeComboBox.setPreferredSize(new Dimension(80, (int)fetchThemesButton.getPreferredSize().getHeight()));
		GridBagConstraints tcb = GuiUtil.setConstraints(1,4,1.0,1.0,GridBagConstraints.BOTH,BORDER_THICKNESS,BORDER_THICKNESS,BORDER_THICKNESS,0);
		tcb.gridwidth = 1;
		displayAsPanel.add(themeComboBox, tcb);
		GridBagConstraints fb = GuiUtil.setConstraints(2,4,0.0,1.0,GridBagConstraints.BOTH,BORDER_THICKNESS,BORDER_THICKNESS,BORDER_THICKNESS,BORDER_THICKNESS);
		fb.gridwidth = 3;
		displayAsPanel.add(fetchThemesButton, fb);


		JPanel exportAndDisplayPanel = new JPanel();
		exportAndDisplayPanel.setLayout(new GridBagLayout());
		exportAndDisplayPanel.add(exportFromLODPanel, GuiUtil.setConstraints(0,0,0.3,0,GridBagConstraints.BOTH,0,0,0,0));
		exportAndDisplayPanel.add(Box.createRigidArea(new Dimension(BORDER_THICKNESS, 0)), GuiUtil.setConstraints(1,0,0,0,GridBagConstraints.NONE,0,0,0,0));
		exportAndDisplayPanel.add(displayAsPanel, GuiUtil.setConstraints(2,0,0.7,0,GridBagConstraints.BOTH,0,0,0,0));


		//********************************************************************************
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
		srsLabel = new JLabel("EPSG:");	

		JPanel EpsgPanel = new JPanel();
		EpsgPanel.setLayout(new GridBagLayout());
		EpsgPanel.setBorder(BorderFactory.createTitledBorder("Reference systems"));

		EpsgPanel.add(srsLabel, GuiUtil.setConstraints(3,0,0.0,1.0,GridBagConstraints.BOTH,0,BORDER_THICKNESS * 6,BORDER_THICKNESS,0));
		EpsgPanel.add(srsField, GuiUtil.setConstraints(4,0,0.5,1.0,GridBagConstraints.BOTH,0,BORDER_THICKNESS,BORDER_THICKNESS,0));

		Box EpsgParentPanel = Box.createHorizontalBox();
		EpsgParentPanel.add(Box.createRigidArea(new Dimension(BORDER_THICKNESS, 0)));
		EpsgParentPanel.add(EpsgPanel);
		EpsgParentPanel.add(Box.createRigidArea(new Dimension(BORDER_THICKNESS, 0)));


		JPanel exportButtonPanel = new JPanel();
		exportButtonPanel.add(exportButton);



		JPanel scrollView = new JPanel();
		scrollView.setLayout(new GridBagLayout());
		scrollView.add(browsePanel, GuiUtil.setConstraints(0,0,1.0,0.0,GridBagConstraints.HORIZONTAL,0,5,0,5));		
		scrollView.add(EpsgPanel, GuiUtil.setConstraints(0,1,1.0,0.0,GridBagConstraints.HORIZONTAL,0,5,0,5));
		scrollView.add(filterPanel, GuiUtil.setConstraints(0,3,1.0,0.0,GridBagConstraints.HORIZONTAL,0,5,0,5));
		scrollView.add(exportAndDisplayPanel, GuiUtil.setConstraints(0,4,1.0,0.0,GridBagConstraints.HORIZONTAL,0,5,0,5));
		scrollView.add(featureClassesLabel, GuiUtil.setConstraints(0,5,1.0,0.0,GridBagConstraints.HORIZONTAL,5,8,0,0));
		scrollView.add(fcTree, GuiUtil.setConstraints(0,6,1.0,1.0,GridBagConstraints.NORTH,GridBagConstraints.HORIZONTAL,5,7,0,7));
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

		((TitledBorder)filterPanel.getBorder()).setTitle(Internal.I18N.getString("kmlExport.label.exportContents"));
		singleBuildingRadioButton.setText(Internal.I18N.getString("kmlExport.label.singleBuilding"));
		boundingBoxRadioButton.setText(Internal.I18N.getString("filter.border.boundingBox"));

		((TitledBorder) tilingPanel.getBorder()).setTitle(Internal.I18N.getString("pref.export.boundingBox.border.tiling"));
		noTilingRadioButton.setText(Internal.I18N.getString("kmlExport.label.noTiling"));
		manualTilingRadioButton.setText(Internal.I18N.getString("kmlExport.label.manual"));
		rowsLabel.setText(Internal.I18N.getString("pref.export.boundingBox.label.rows"));
		columnsLabel.setText(Internal.I18N.getString("pref.export.boundingBox.label.columns"));
		automaticTilingRadioButton.setText(Internal.I18N.getString("kmlExport.label.automatic"));

		((TitledBorder)exportFromLODPanel.getBorder()).setTitle(Internal.I18N.getString("kmlExport.label.fromLOD"));
		/**/
		int selectedIndex = lodComboBox.getSelectedIndex();
		if (!lodComboBox.getItemAt(lodComboBox.getItemCount() - 1).toString().endsWith("4")) {
			lodComboBox.removeItemAt(lodComboBox.getItemCount() - 1);
		}
		lodComboBox.insertItemAt(Internal.I18N.getString("kmlExport.label.highestLODAvailable"), lodComboBox.getItemCount());
		lodComboBox.setSelectedIndex(selectedIndex);
		lodComboBox.setMinimumSize(lodComboBox.getPreferredSize());
		exportFromLODPanel.setMinimumSize(exportFromLODPanel.getPreferredSize());
		/**/
		((TitledBorder)displayAsPanel.getBorder()).setTitle(Internal.I18N.getString("kmlExport.label.displayAs"));
		footprintCheckbox.setText(Internal.I18N.getString("kmlExport.label.footprint"));
		extrudedCheckbox.setText(Internal.I18N.getString("kmlExport.label.extruded"));
		geometryCheckbox.setText(Internal.I18N.getString("kmlExport.label.geometry"));
		colladaCheckbox.setText(Internal.I18N.getString("kmlExport.label.collada"));

		visibleFromFootprintLabel.setText(Internal.I18N.getString("kmlExport.label.visibleFrom"));
		pixelsFootprintLabel.setText(Internal.I18N.getString("kmlExport.label.pixels"));
		visibleFromExtrudedLabel.setText(Internal.I18N.getString("kmlExport.label.visibleFrom"));
		pixelsExtrudedLabel.setText(Internal.I18N.getString("kmlExport.label.pixels"));
		visibleFromGeometryLabel.setText(Internal.I18N.getString("kmlExport.label.visibleFrom"));
		pixelsGeometryLabel.setText(Internal.I18N.getString("kmlExport.label.pixels"));
		visibleFromColladaLabel.setText(Internal.I18N.getString("kmlExport.label.visibleFrom"));
		pixelsColladaLabel.setText(Internal.I18N.getString("kmlExport.label.pixels"));

		themeLabel.setText(Internal.I18N.getString("pref.kmlexport.label.theme"));
		fetchThemesButton.setText(Internal.I18N.getString("pref.kmlexport.label.fetchTheme"));

		featureClassesLabel.setText(Internal.I18N.getString("filter.border.featureClass"));

		exportButton.setText(Internal.I18N.getString("export.button.export"));
	}

	private void clearGui() {

		browseText.setText("");

	}

	public void loadSettings() {

		clearGui();


		de.tub.citydb.config.project.CitykmlExporter.CityKmlExporter citykmlExporter = config.getProject().getCityKmlExporter();
		if (citykmlExporter == null) return;

		if (citykmlExporter.getFilter().isSetSimpleFilter()) {
			singleBuildingRadioButton.setSelected(true);
		}
		else {
			boundingBoxRadioButton.setSelected(true);
		}

		// this block should be under the former else block
		if (citykmlExporter.getFilter().getComplexFilter().getFeatureClass().isSetBuilding()) {
			fcTree.getCheckingModel().addCheckingPath(new TreePath(building.getPath()));
		}
		else {
			fcTree.getCheckingModel().removeCheckingPath(new TreePath(building.getPath()));
		}
		if (citykmlExporter.getFilter().getComplexFilter().getFeatureClass().isSetWaterBody()) {
			fcTree.getCheckingModel().addCheckingPath(new TreePath(waterBody.getPath()));
		}
		else {
			fcTree.getCheckingModel().removeCheckingPath(new TreePath(waterBody.getPath()));
		}
		if (citykmlExporter.getFilter().getComplexFilter().getFeatureClass().isSetLandUse()) {
			fcTree.getCheckingModel().addCheckingPath(new TreePath(landUse.getPath()));
		}
		else {
			fcTree.getCheckingModel().removeCheckingPath(new TreePath(landUse.getPath()));
		}
		if (citykmlExporter.getFilter().getComplexFilter().getFeatureClass().isSetVegetation()) {
			fcTree.getCheckingModel().addCheckingPath(new TreePath(vegetation.getPath()));
		}
		else {
			fcTree.getCheckingModel().removeCheckingPath(new TreePath(vegetation.getPath()));
		}
		if (citykmlExporter.getFilter().getComplexFilter().getFeatureClass().isSetTransportation()) {
			fcTree.getCheckingModel().addCheckingPath(new TreePath(transportation.getPath()));
		}
		else {
			fcTree.getCheckingModel().removeCheckingPath(new TreePath(transportation.getPath()));
		}
		if (citykmlExporter.getFilter().getComplexFilter().getFeatureClass().isSetReliefFeature()) {
			fcTree.getCheckingModel().addCheckingPath(new TreePath(relief.getPath()));
		}
		else {
			fcTree.getCheckingModel().removeCheckingPath(new TreePath(relief.getPath()));
		}
		if (citykmlExporter.getFilter().getComplexFilter().getFeatureClass().isSetCityFurniture()) {
			fcTree.getCheckingModel().addCheckingPath(new TreePath(cityFurniture.getPath()));
		}
		else {
			fcTree.getCheckingModel().removeCheckingPath(new TreePath(cityFurniture.getPath()));
		}
		if (citykmlExporter.getFilter().getComplexFilter().getFeatureClass().isSetGenericCityObject()) {
			fcTree.getCheckingModel().addCheckingPath(new TreePath(genericCityObject.getPath()));
		}
		else {
			fcTree.getCheckingModel().removeCheckingPath(new TreePath(genericCityObject.getPath()));
		}
		if (citykmlExporter.getFilter().getComplexFilter().getFeatureClass().isSetCityObjectGroup()) {
			fcTree.getCheckingModel().addCheckingPath(new TreePath(cityObjectGroup.getPath()));
		}
		else {
			fcTree.getCheckingModel().removeCheckingPath(new TreePath(cityObjectGroup.getPath()));
		}
		// end of block

		boolean isFirst = true;
		String gmlIds = "";
		for (String gmlId: citykmlExporter.getFilter().getSimpleFilter().getGmlIdFilter().getGmlIds()) {
			if (!isFirst) {
				gmlIds = gmlIds + ", ";
			}
			else {
				isFirst = false;
			}
			gmlIds = gmlIds + gmlId;
		}
		gmlIdText.setText(gmlIds);

		bboxComponent.setBoundingBox(citykmlExporter.getFilter().getComplexFilter().getTiledBoundingBox());

		String tilingMode = citykmlExporter.getFilter().getComplexFilter().getTiledBoundingBox().getTiling().getMode().value();

		if (tilingMode.equals(TilingMode.NO_TILING.value())) {
			noTilingRadioButton.setSelected(true);
		}
		else if (tilingMode.equals(TilingMode.AUTOMATIC.value())) {
			automaticTilingRadioButton.setSelected(true);
		}
		else {
			manualTilingRadioButton.setSelected(true);
		}

		rowsText.setText(String.valueOf(citykmlExporter.getFilter().getComplexFilter().
				getTiledBoundingBox().getTiling().getRows()));
		columnsText.setText(String.valueOf(citykmlExporter.getFilter().getComplexFilter().
				getTiledBoundingBox().getTiling().getColumns()));

		int lod = citykmlExporter.getLodToExportFrom();
		lod = lod >= lodComboBox.getItemCount() ? lodComboBox.getItemCount() - 1: lod; 
		lodComboBox.setSelectedIndex(lod);

		for (DisplayForm displayForm : citykmlExporter.getBuildingDisplayForms()) {
			switch (displayForm.getForm()) {
			case DisplayForm.FOOTPRINT:
				if (displayForm.isActive()) {
					footprintCheckbox.setSelected(true);
					footprintVisibleFromText.setText(String.valueOf(displayForm.getVisibleFrom()));
				}
				break;
			case DisplayForm.EXTRUDED:
				if (displayForm.isActive()) {
					extrudedCheckbox.setSelected(true);
					extrudedVisibleFromText.setText(String.valueOf(displayForm.getVisibleFrom()));
				}
				break;
			case DisplayForm.GEOMETRY:
				if (displayForm.isActive()) {
					geometryCheckbox.setSelected(true);
					geometryVisibleFromText.setText(String.valueOf(displayForm.getVisibleFrom()));
				}
				break;
			case DisplayForm.COLLADA:
				if (displayForm.isActive()) {
					colladaCheckbox.setSelected(true);
					colladaVisibleFromText.setText(String.valueOf(displayForm.getVisibleFrom()));
				}
				break;
			}
		}

		themeComboBox.removeAllItems();
		themeComboBox.addItem(citykmlExporter.THEME_NONE);
		themeComboBox.setSelectedItem(citykmlExporter.THEME_NONE);
		if (dbPool.isConnected()) {
			try {
				//				Workspace workspace = new Workspace();
				//				workspace.setName(workspaceText.getText().trim());
				//				workspace.setTimestamp(timestampText.getText().trim());
				for (String theme: DBUtil.getAppearanceThemeList()) {
					if (theme == null) continue; 
					themeComboBox.addItem(theme);
					if (theme.equals(citykmlExporter.getAppearanceTheme())) {
						themeComboBox.setSelectedItem(theme);
					}
				}
				themeComboBox.setEnabled(true);
			}
			catch (SQLException sqlEx) { }
		}
		else {
			themeComboBox.setEnabled(false);
		}

		setFilterEnabledValues();


	}


	public void setSettings() {


		File[] importFiles = new File[fileListModel.size()];
		for (int i = 0; i < fileListModel.size(); ++i)
			importFiles[i] = new File(fileListModel.get(i).toString());

		config.getInternal().setImportFiles(importFiles);	

		config.getInternal().setExportFileName(browseText.getText().trim());



		CityKmlExporter kmlExporter = config.getProject().getCityKmlExporter();
		ExportFilterConfig kmlExportFilter = kmlExporter.getFilter();

		kmlExportFilter.getComplexFilter().getTiledBoundingBox().setActive(!singleBuildingRadioButton.isSelected());
		if (singleBuildingRadioButton.isSelected()) {
			kmlExportFilter.setMode(FilterMode.SIMPLE);
		}
		else {
			kmlExportFilter.setMode(FilterMode.COMPLEX);

			if (noTilingRadioButton.isSelected()) {
				kmlExportFilter.getComplexFilter().getTiledBoundingBox().getTiling().setMode(TilingMode.NO_TILING);
			}
			else if (automaticTilingRadioButton.isSelected()) {
				kmlExportFilter.getComplexFilter().getTiledBoundingBox().getTiling().setMode(TilingMode.AUTOMATIC);
			}
			else {
				kmlExportFilter.getComplexFilter().getTiledBoundingBox().getTiling().setMode(TilingMode.MANUAL);
			}
		}

		kmlExportFilter.getSimpleFilter().getGmlIdFilter().getGmlIds().clear();
		StringTokenizer st = new StringTokenizer(gmlIdText.getText().trim(), ",");
		while (st.hasMoreTokens()) {
			kmlExportFilter.getSimpleFilter().getGmlIdFilter().addGmlId(st.nextToken().trim());
		}

		kmlExportFilter.getComplexFilter().getTiledBoundingBox().copyFrom(bboxComponent.getBoundingBox());

		try {
			kmlExportFilter.getComplexFilter().getTiledBoundingBox().
			getTiling().setRows(Integer.parseInt(rowsText.getText().trim()));
		}
		catch (NumberFormatException nfe) {
			kmlExportFilter.getComplexFilter().getTiledBoundingBox().getTiling().setRows(1);
		}
		try {
			kmlExportFilter.getComplexFilter().getTiledBoundingBox().
			getTiling().setColumns(Integer.parseInt(columnsText.getText().trim()));
		}
		catch (NumberFormatException nfe) {
			kmlExportFilter.getComplexFilter().getTiledBoundingBox().getTiling().setColumns(1);
		}

		kmlExporter.setLodToExportFrom(lodComboBox.getSelectedIndex());

		setDisplayFormSettings(kmlExporter.getBuildingDisplayForms());
		setDisplayFormSettings(kmlExporter.getWaterBodyDisplayForms());
		setDisplayFormSettings(kmlExporter.getLandUseDisplayForms());
		setDisplayFormSettings(kmlExporter.getVegetationDisplayForms());
		setDisplayFormSettings(kmlExporter.getTransportationDisplayForms());
		setDisplayFormSettings(kmlExporter.getReliefDisplayForms());
		setDisplayFormSettings(kmlExporter.getCityFurnitureDisplayForms());
		setDisplayFormSettings(kmlExporter.getGenericCityObjectDisplayForms());
		setDisplayFormSettings(kmlExporter.getCityObjectGroupDisplayForms());

		//		if (themeComboBox.getItemCount() > 0) {
		kmlExporter.setAppearanceTheme(themeComboBox.getSelectedItem().toString());
		//		}

		kmlExportFilter.getComplexFilter().getFeatureClass().setBuilding(fcTree.getCheckingModel().isPathChecked(new TreePath(building.getPath()))); 
		kmlExportFilter.getComplexFilter().getFeatureClass().setWaterBody(fcTree.getCheckingModel().isPathChecked(new TreePath(waterBody.getPath()))); 
		kmlExportFilter.getComplexFilter().getFeatureClass().setLandUse(fcTree.getCheckingModel().isPathChecked(new TreePath(landUse.getPath()))); 
		kmlExportFilter.getComplexFilter().getFeatureClass().setVegetation(fcTree.getCheckingModel().isPathChecked(new TreePath(vegetation.getPath())));
		kmlExportFilter.getComplexFilter().getFeatureClass().setTransportation(fcTree.getCheckingModel().isPathChecked(new TreePath(transportation.getPath())));
		kmlExportFilter.getComplexFilter().getFeatureClass().setReliefFeature(fcTree.getCheckingModel().isPathChecked(new TreePath(relief.getPath())));
		kmlExportFilter.getComplexFilter().getFeatureClass().setCityFurniture(fcTree.getCheckingModel().isPathChecked(new TreePath(cityFurniture.getPath())));
		kmlExportFilter.getComplexFilter().getFeatureClass().setGenericCityObject(fcTree.getCheckingModel().isPathChecked(new TreePath(genericCityObject.getPath())));
		kmlExportFilter.getComplexFilter().getFeatureClass().setCityObjectGroup(fcTree.getCheckingModel().isPathChecked(new TreePath(cityObjectGroup.getPath())));

		config.getProject().setCityKmlExporter(kmlExporter);

	}


	private void setDisplayFormSettings(List<DisplayForm> displayForms) {
		DisplayForm df = new DisplayForm(DisplayForm.COLLADA, -1, -1);
		int indexOfDf = displayForms.indexOf(df); 
		if (indexOfDf != -1) {
			df = displayForms.get(indexOfDf);
		}
		else { // should never happen
			displayForms.add(df);
		}
		if (colladaCheckbox.isSelected() && config.getProject().getKmlExporter().getLodToExportFrom()>0) {
			int levelVisibility = 0;
			try {
				levelVisibility = Integer.parseInt(colladaVisibleFromText.getText().trim());
			}
			catch (NumberFormatException nfe) {}
			df.setActive(true);
			df.setVisibleFrom(levelVisibility);
		}
		else {
			df.setActive(false);
		}

		df = new DisplayForm(DisplayForm.GEOMETRY, -1, -1);
		indexOfDf = displayForms.indexOf(df); 
		if (indexOfDf != -1) {
			df = displayForms.get(indexOfDf);
		}
		else { // should never happen
			displayForms.add(df);
		}
		if (geometryCheckbox.isSelected() && config.getProject().getKmlExporter().getLodToExportFrom()>0) {
			int levelVisibility = 0;
			try {
				levelVisibility = Integer.parseInt(geometryVisibleFromText.getText().trim());
			}
			catch (NumberFormatException nfe) {}
			df.setActive(true);
			df.setVisibleFrom(levelVisibility);
		}
		else {
			df.setActive(false);
		}

		df = new DisplayForm(DisplayForm.EXTRUDED, -1, -1);
		indexOfDf = displayForms.indexOf(df); 
		if (indexOfDf != -1) {
			df = displayForms.get(indexOfDf);
		}
		else { // should never happen
			displayForms.add(df);
		}
		if (extrudedCheckbox.isSelected() && config.getProject().getKmlExporter().getLodToExportFrom()>0) {
			int levelVisibility = 0;
			try {
				levelVisibility = Integer.parseInt(extrudedVisibleFromText.getText().trim());
			}
			catch (NumberFormatException nfe) {}
			df.setActive(true);
			df.setVisibleFrom(levelVisibility);
		}
		else {
			df.setActive(false);
		}

		df = new DisplayForm(DisplayForm.FOOTPRINT, -1, -1);
		indexOfDf = displayForms.indexOf(df); 
		if (indexOfDf != -1) {
			df = displayForms.get(indexOfDf);
		}
		else { // should never happen
			displayForms.add(df);
		}
		if (footprintCheckbox.isSelected()) {
			int levelVisibility = 0;
			try {
				levelVisibility = Integer.parseInt(footprintVisibleFromText.getText().trim());
			}
			catch (NumberFormatException nfe) {}
			df.setActive(true);
			df.setVisibleFrom(levelVisibility);
		}
		else {
			df.setActive(false);
		}

		int upperLevelVisibility = -1; 
		for (int i = DisplayForm.COLLADA; i >= DisplayForm.FOOTPRINT; i--) {
			df = new DisplayForm(i, -1, -1);
			indexOfDf = displayForms.indexOf(df); 
			df = displayForms.get(indexOfDf);

			if (df.isActive()) {
				df.setVisibleUpTo(upperLevelVisibility);
				upperLevelVisibility = df.getVisibleFrom();
			}
		}

	}


	private void addListeners() {

		enableEvents(AWTEvent.WINDOW_EVENT_MASK);

		exportButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Thread thread = new Thread() {
					public void run() {
						//doExport();

						try {
							doExport();
						} catch (Exception e) {
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
				setFilterEnabledValues();
			}
		};

		singleBuildingRadioButton.addActionListener(filterListener);
		boundingBoxRadioButton.addActionListener(filterListener);

		noTilingRadioButton.addActionListener(filterListener);
		manualTilingRadioButton.addActionListener(filterListener);
		automaticTilingRadioButton.addActionListener(filterListener);

		lodComboBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setVisibilityEnabledValues();
			}
		});

		footprintCheckbox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setVisibilityEnabledValues();
			}
		});

		extrudedCheckbox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setVisibilityEnabledValues();
			}
		});

		geometryCheckbox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setVisibilityEnabledValues();
			}
		});

		colladaCheckbox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setVisibilityEnabledValues();
			}
		});

		fetchThemesButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ThemeUpdater themeUpdater = new ThemeUpdater();
				themeUpdater.setDaemon(true);
				themeUpdater.start();
			}
		});


	}



	private JAXBChunkReader doImport() throws Exception {

		JAXBChunkReader _ChunkReader = null;
		final de.tub.citydb.config.project.CitykmlExporter.CityKmlExporter importer = config.getProject().getCityKmlExporter();

		Internal intConfig = config.getInternal();		

		
		// build list of import files
		LOG.info("Creating list of CityGML files to be imported...");	
		DirectoryScanner directoryScanner = new DirectoryScanner(true);
		directoryScanner.addFilenameFilter(new CityGMLFilenameFilter());		
		List<File> importFiles = directoryScanner.getFiles(intConfig.getImportFiles());



		if (importFiles.size() == 0) {
			LOG.warn("Failed to find CityGML files at the specified locations.");
			return null;
		}

		int fileCounter = 0;
		int remainingFiles = importFiles.size();
		LOG.info("List of import files successfully created.");
		LOG.info(remainingFiles + " file(s) will be imported.");

		// prepare CityGML input factory
		CityGMLInputFactory in = null;
		try {
			in = jaxbBuilder.createCityGMLInputFactory();
			in.setProperty(CityGMLInputFactory.FEATURE_READ_MODE, FeatureReadMode.SPLIT_PER_COLLECTION_MEMBER);
			in.setProperty(CityGMLInputFactory.FAIL_ON_MISSING_ADE_SCHEMA, false);
			in.setProperty(CityGMLInputFactory.PARSE_SCHEMA, false);
			in.setProperty(CityGMLInputFactory.SPLIT_AT_FEATURE_PROPERTY, new QName("generalizesTo"));
			in.setProperty(CityGMLInputFactory.EXCLUDE_FROM_SPLITTING, CityModel.class);
		} catch (CityGMLReadException e) {
			LOG.error("Failed to initialize CityGML parser. Aborting.");
			return null;
		}


		// prepare feature filter

		CityGMLInputFilter inputFilter = new CityGMLInputFilter() {
			public boolean accept(CityGMLClass type) {
				return true;
			}
		};



		while (fileCounter < importFiles.size()) {
			// check whether we reached the counter limit
			
			try {

				File file = importFiles.get(fileCounter++);
				intConfig.setImportPath(file.getParent());
				intConfig.setCurrentImportFile(file);

				// ok, preparation done. inform user and start parsing the input file
				LOG.info("Reading the imported file...");

				try {

					_ChunkReader = (JAXBChunkReader)in.createFilteredCityGMLReader(in.createCityGMLReader(file), inputFilter);	


				} catch (CityGMLReadException e) {
					LOG.error("Fatal CityGML parser error: " + e.getCause().getMessage());
					continue;
				}
			}
			catch(Exception ex)
			{
				
				LOG.error(ex.toString());
				
			}
		}
		
		return _ChunkReader;

	}

	private void doExport() throws Exception {

		final ReentrantLock lock = this.mainLock;
		lock.lock();

		try {
			//mainView.clearConsole();
			setSettings();

			ExportFilterConfig filter = config.getProject().getCityKmlExporter().getFilter();
			//			Database db = config.getProject().getDatabase();

			// check all input values...
			if (config.getInternal().getExportFileName().trim().equals("")) {
				mainView.errorMessage(Internal.I18N.getString("kmlExport.dialog.error.incompleteData"), 
						Internal.I18N.getString("kmlExport.dialog.error.incompleteData.dataset"));
				return;
			}



			// gmlId
			if (filter.isSetSimpleFilter() &&
					filter.getSimpleFilter().getGmlIdFilter().getGmlIds().isEmpty()) {
				mainView.errorMessage(Internal.I18N.getString("export.dialog.error.incorrectData"), 
						Internal.I18N.getString("common.dialog.error.incorrectData.gmlId"));
				return;
			}

			// DisplayForms
			int activeDisplayFormsAmount =
					CityKmlExporter.getActiveDisplayFormsAmount(config.getProject().getCityKmlExporter().getBuildingDisplayForms()); 
			if (activeDisplayFormsAmount == 0) {
				mainView.errorMessage(Internal.I18N.getString("export.dialog.error.incorrectData"), 
						Internal.I18N.getString("kmlExport.dialog.error.incorrectData.displayForms"));
				return;
			}

			// initialize event dispatcher
			final EventDispatcher eventDispatcher = ObjectRegistry.getInstance().getEventDispatcher();
			de.tub.citydb.modules.citykml.controller.CityKmlExporter CityKmlExporter = 
					new de.tub.citydb.modules.citykml.controller.CityKmlExporter(
							jaxbKmlContext,
							jaxbColladaContext,
							dbPool,
							config,
							(!srsField.getText().equals("")) ? srsField.getText() : "4326",
									eventDispatcher);

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

			// tile amount calculation
			//	int tileAmount = 1;
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
			Logger.getInstance().info("Initializing database export...");*/

			final ExportStatusDialog exportDialog = new ExportStatusDialog(mainView, 
					Internal.I18N.getString("kmlExport.dialog.window"),
					Internal.I18N.getString("export.dialog.msg"),
					1);

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


			//Start reading the input file
			JAXBChunkReader _Reader = doImport();

			if (_Reader != null) {				


				mainView.setStatusText(Internal.I18N.getString("main.status.ready.label"));

				boolean success = false;
				
				try {

					success = CityKmlExporter.doProcess(_Reader);

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
						exportDialog.dispose();
					}
				});

				// cleanup
				CityKmlExporter.cleanup();

				if (success) {
					Logger.getInstance().info("Database export successfully finished.");
				} else {
					Logger.getInstance().warn("Database export aborted.");
				}

				mainView.setStatusText(Internal.I18N.getString("main.status.ready.label"));

			} else {
				LOG.warn("CityGML import aborted.");
			}


		} finally {
			lock.unlock();
		}
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

	private void setFilterEnabledValues() {
		gmlIdLabel.setEnabled(singleBuildingRadioButton.isSelected());
		gmlIdText.setEnabled(singleBuildingRadioButton.isSelected());

		bboxComponent.setEnabled(boundingBoxRadioButton.isSelected());

		DefaultTreeCheckingModel model = (DefaultTreeCheckingModel)fcTree.getCheckingModel();
		model.setPathEnabled(new TreePath(cityObject), boundingBoxRadioButton.isSelected());
		model.setPathEnabled(new TreePath(new Object[]{cityObject, building}), boundingBoxRadioButton.isSelected());
		model.setPathEnabled(new TreePath(new Object[]{cityObject, waterBody}), boundingBoxRadioButton.isSelected());
		model.setPathEnabled(new TreePath(new Object[]{cityObject, landUse}), boundingBoxRadioButton.isSelected());
		model.setPathEnabled(new TreePath(new Object[]{cityObject, vegetation}), boundingBoxRadioButton.isSelected());
		model.setPathEnabled(new TreePath(new Object[]{cityObject, transportation}), boundingBoxRadioButton.isSelected());
		model.setPathEnabled(new TreePath(new Object[]{cityObject, relief}), boundingBoxRadioButton.isSelected());
		model.setPathEnabled(new TreePath(new Object[]{cityObject, cityFurniture}), boundingBoxRadioButton.isSelected());
		model.setPathEnabled(new TreePath(new Object[]{cityObject, genericCityObject}), boundingBoxRadioButton.isSelected());
		model.setPathEnabled(new TreePath(new Object[]{cityObject, cityObjectGroup}), boundingBoxRadioButton.isSelected());
		fcTree.repaint();

		noTilingRadioButton.setEnabled(boundingBoxRadioButton.isSelected());
		automaticTilingRadioButton.setEnabled(boundingBoxRadioButton.isSelected());
		manualTilingRadioButton.setEnabled(boundingBoxRadioButton.isSelected());
		((TitledBorder) tilingPanel.getBorder()).setTitleColor(boundingBoxRadioButton.isSelected() ? 
				UIManager.getColor("Label.foreground"):
					UIManager.getColor("Label.disabledForeground"));
		tilingPanel.repaint();

		rowsLabel.setEnabled(manualTilingRadioButton.isEnabled()&& manualTilingRadioButton.isSelected());
		rowsText.setEnabled(manualTilingRadioButton.isEnabled()&& manualTilingRadioButton.isSelected());
		columnsLabel.setEnabled(manualTilingRadioButton.isEnabled()&& manualTilingRadioButton.isSelected());
		columnsText.setEnabled(manualTilingRadioButton.isEnabled()&& manualTilingRadioButton.isSelected());

		setVisibilityEnabledValues();

	}

	private void setVisibilityEnabledValues() {

		extrudedCheckbox.setEnabled(DisplayForm.isAchievableFromLoD(DisplayForm.EXTRUDED, lodComboBox.getSelectedIndex()));
		geometryCheckbox.setEnabled(DisplayForm.isAchievableFromLoD(DisplayForm.GEOMETRY, lodComboBox.getSelectedIndex()));
		colladaCheckbox.setEnabled(DisplayForm.isAchievableFromLoD(DisplayForm.COLLADA, lodComboBox.getSelectedIndex()));

		visibleFromFootprintLabel.setEnabled(boundingBoxRadioButton.isSelected() && footprintCheckbox.isEnabled() && footprintCheckbox.isSelected());
		footprintVisibleFromText.setEnabled(boundingBoxRadioButton.isSelected() && footprintCheckbox.isEnabled() && footprintCheckbox.isSelected());
		pixelsFootprintLabel.setEnabled(boundingBoxRadioButton.isSelected() && footprintCheckbox.isEnabled() && footprintCheckbox.isSelected());

		visibleFromExtrudedLabel.setEnabled(boundingBoxRadioButton.isSelected() && extrudedCheckbox.isEnabled() && extrudedCheckbox.isSelected());
		extrudedVisibleFromText.setEnabled(boundingBoxRadioButton.isSelected() && extrudedCheckbox.isEnabled() && extrudedCheckbox.isSelected());
		pixelsExtrudedLabel.setEnabled(boundingBoxRadioButton.isSelected() && extrudedCheckbox.isEnabled() && extrudedCheckbox.isSelected());

		visibleFromGeometryLabel.setEnabled(boundingBoxRadioButton.isSelected() && geometryCheckbox.isEnabled() && geometryCheckbox.isSelected());
		geometryVisibleFromText.setEnabled(boundingBoxRadioButton.isSelected() && geometryCheckbox.isEnabled() && geometryCheckbox.isSelected());
		pixelsGeometryLabel.setEnabled(boundingBoxRadioButton.isSelected() && geometryCheckbox.isEnabled() && geometryCheckbox.isSelected());

		visibleFromColladaLabel.setEnabled(boundingBoxRadioButton.isSelected() && colladaCheckbox.isEnabled() && colladaCheckbox.isSelected());
		colladaVisibleFromText.setEnabled(boundingBoxRadioButton.isSelected() && colladaCheckbox.isEnabled() && colladaCheckbox.isSelected());
		pixelsColladaLabel.setEnabled(boundingBoxRadioButton.isSelected() && colladaCheckbox.isEnabled() && colladaCheckbox.isSelected());

		themeLabel.setEnabled(colladaCheckbox.isEnabled() && colladaCheckbox.isSelected());
		themeComboBox.setEnabled(dbPool.isConnected() && colladaCheckbox.isEnabled() && colladaCheckbox.isSelected());
		fetchThemesButton.setEnabled(colladaCheckbox.isEnabled() && colladaCheckbox.isSelected());

		fcTree.getCheckingModel().setPathEnabled(new TreePath(building.getPath()), boundingBoxRadioButton.isSelected() && (lodComboBox.getSelectedIndex() > 0));
		fcTree.getCheckingModel().setPathEnabled(new TreePath(vegetation.getPath()), boundingBoxRadioButton.isSelected() && (lodComboBox.getSelectedIndex() > 0));
		fcTree.getCheckingModel().setPathEnabled(new TreePath(relief.getPath()), boundingBoxRadioButton.isSelected() && (lodComboBox.getSelectedIndex() > 0));
		fcTree.getCheckingModel().setPathEnabled(new TreePath(cityFurniture.getPath()), boundingBoxRadioButton.isSelected() && (lodComboBox.getSelectedIndex() > 0));
		fcTree.getCheckingModel().setPathEnabled(new TreePath(cityObjectGroup.getPath()), boundingBoxRadioButton.isSelected() && (lodComboBox.getSelectedIndex() > 0));
		fcTree.repaint();

	}



	private void saveFile() {
		JFileChooser fileChooser = new JFileChooser();

		FileNameExtensionFilter filter = new FileNameExtensionFilter("KML Files (*.kml, *.kmz)", "kml", "kmz");
		fileChooser.addChoosableFileFilter(filter);
		fileChooser.addChoosableFileFilter(fileChooser.getAcceptAllFileFilter());
		fileChooser.setFileFilter(filter);

		if (config.getProject().getKmlExporter().getPath().isSetLastUsedMode()) {
			fileChooser.setCurrentDirectory(new File(config.getProject().getKmlExporter().getPath().getLastUsedPath()));
		} else {
			fileChooser.setCurrentDirectory(new File(config.getProject().getExporter().getPath().getStandardPath()));
		}
		int result = fileChooser.showSaveDialog(getTopLevelAncestor());
		if (result == JFileChooser.CANCEL_OPTION) return;
		try {
			String exportString = fileChooser.getSelectedFile().toString();
			if (exportString.lastIndexOf('.') != -1	&&
					exportString.lastIndexOf('.') > exportString.lastIndexOf(File.separator)) {
				exportString = exportString.substring(0, exportString.lastIndexOf('.'));
			}
			exportString = config.getProject().getKmlExporter().isExportAsKmz() ?
					exportString + ".kmz":
						exportString + ".kml";

			browseText.setText(exportString);
			config.getProject().getKmlExporter().getPath().setLastUsedPath(fileChooser.getCurrentDirectory().getAbsolutePath());
		}
		catch (Exception e) {
			//
		}
	}

	@Override
	public void handleEvent(Event event) throws Exception {
		boolean isConnected = ((DatabaseConnectionStateEvent)event).isConnected();

		themeComboBox.removeAllItems();
		themeComboBox.addItem(CityKmlExporter.THEME_NONE);
		themeComboBox.setSelectedItem(CityKmlExporter.THEME_NONE);
		if (!isConnected) {
			themeComboBox.setEnabled(false);
		}
	}

	private class ThemeUpdater extends Thread {
		public void run() {
			Thread.currentThread().setName(this.getClass().getSimpleName());
			fetchThemesButton.setEnabled(false);
			try {
				String text = Internal.I18N.getString("pref.kmlexport.connectDialog.line2");
				DBConnection conn = config.getProject().getDatabase().getActiveConnection();
				Object[] args = new Object[]{conn.getDescription(), conn.toConnectString()};
				String formattedMsg = MessageFormat.format(text, args);
				String[] connectConfirm = {Internal.I18N.getString("pref.kmlexport.connectDialog.line1"),
						formattedMsg,
						Internal.I18N.getString("pref.kmlexport.connectDialog.line3")};

				if (!dbPool.isConnected() &&
						JOptionPane.showConfirmDialog(getTopLevelAncestor(),
								connectConfirm,
								Internal.I18N.getString("pref.kmlexport.connectDialog.title"),
								JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
					mainView.connectToDatabase();
				}

				if (dbPool.isConnected()) {
					themeComboBox.removeAllItems();
					themeComboBox.addItem(CityKmlExporter.THEME_NONE);
					themeComboBox.setSelectedItem(CityKmlExporter.THEME_NONE);
					//					Workspace workspace = new Workspace();
					//					workspace.setName(workspaceText.getText().trim());
					//					workspace.setTimestamp(timestampText.getText().trim());
					for (String theme: DBUtil.getAppearanceThemeList()) {
						if (theme == null) continue; 
						themeComboBox.addItem(theme);
						if (theme.equals(config.getProject().getKmlExporter().getAppearanceTheme())) {
							themeComboBox.setSelectedItem(theme);
						}
					}
					themeComboBox.setEnabled(true);
				}
			}
			catch (Exception e) { }
			finally {
				fetchThemesButton.setEnabled(true);
			}
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
