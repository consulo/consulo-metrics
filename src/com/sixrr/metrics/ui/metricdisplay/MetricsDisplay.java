/*
 * Copyright 2005-2011 Bas Leijdekkers, Sixth and Red River Software
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.sixrr.metrics.ui.metricdisplay;

import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import com.intellij.openapi.project.Project;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.table.JBTable;
import com.sixrr.metrics.Metric;
import com.sixrr.metrics.MetricCategory;
import com.sixrr.metrics.config.MetricsReloadedConfig;
import com.sixrr.metrics.metricModel.MetricInstance;
import com.sixrr.metrics.metricModel.MetricInstanceAbbreviationComparator;
import com.sixrr.metrics.metricModel.MetricsCategoryNameUtil;
import com.sixrr.metrics.metricModel.MetricsResult;
import com.sixrr.metrics.metricModel.MetricsRun;
import com.sixrr.metrics.plugin.MetricsPlugin;
import com.sixrr.metrics.profile.MetricDisplaySpecification;
import com.sixrr.metrics.profile.MetricTableSpecification;
import com.sixrr.metrics.utils.MetricsReloadedBundle;

public class MetricsDisplay
{

	private boolean hasOverlay = false;
	private final Map<MetricCategory, JTable> tables = new EnumMap<MetricCategory, JTable>(MetricCategory.class);
	private final JTabbedPane tabbedPane = new JTabbedPane();
	private final MetricsReloadedConfig configuration;
	private final MetricsPlugin metricsPlugin;

	public MetricsDisplay(
			Project project, MetricsReloadedConfig configuration, MetricsPlugin metricsPlugin)
	{
		this.configuration = configuration;
		this.metricsPlugin = metricsPlugin;
		final JTable projectMetricsTable = new JBTable();
		tables.put(MetricCategory.Project, projectMetricsTable);
		final JTable moduleMetricsTable = new JBTable();
		tables.put(MetricCategory.Module, moduleMetricsTable);
		final JTable packageMetricsTable = new JBTable();
		tables.put(MetricCategory.Package, packageMetricsTable);
		final JTable classMetricsTable = new JBTable();
		tables.put(MetricCategory.Class, classMetricsTable);
		final JTable interfaceMetricsTable = new JBTable();
		tables.put(MetricCategory.Interface, interfaceMetricsTable);
		final JTable methodMetricsTable = new JBTable();
		tables.put(MetricCategory.Method, methodMetricsTable);
		setupTable(projectMetricsTable, project);
		setupTable(moduleMetricsTable, project);
		setupTable(packageMetricsTable, project);
		setupTable(classMetricsTable, project);
		setupTable(interfaceMetricsTable, project);
		setupTable(methodMetricsTable, project);
		tabbedPane.add(MetricsReloadedBundle.message("project.metrics"), ScrollPaneFactory.createScrollPane(projectMetricsTable));
		tabbedPane.add(MetricsReloadedBundle.message("module.metrics"), ScrollPaneFactory.createScrollPane(moduleMetricsTable));
		tabbedPane.add(MetricsReloadedBundle.message("package.metrics"), ScrollPaneFactory.createScrollPane(packageMetricsTable));
		tabbedPane.add(MetricsReloadedBundle.message("class.metrics"), ScrollPaneFactory.createScrollPane(classMetricsTable));
		tabbedPane.add(MetricsReloadedBundle.message("interface.metrics"), ScrollPaneFactory.createScrollPane(interfaceMetricsTable));
		tabbedPane.add(MetricsReloadedBundle.message("method.metrics"), ScrollPaneFactory.createScrollPane(methodMetricsTable));
	}

	private void setupTable(JTable table, Project project)
	{
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		table.setRowSelectionAllowed(false);
		table.setColumnSelectionAllowed(true);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.addMouseListener(new MetricTableMouseListener(project, table, configuration));
		final JTableHeader tableHeader = table.getTableHeader();
		tableHeader.addMouseListener(new MetricTableHeaderMouseListener(project, table));
	}

	public void setMetricsResults(MetricDisplaySpecification displaySpecification, MetricsRun run)
	{
		final MetricCategory[] categories = MetricCategory.values();
		for(final MetricCategory category : categories)
		{
			final JTable table = tables.get(category);
			final String type = MetricsCategoryNameUtil.getShortNameForCategory(category);
			final MetricTableSpecification tableSpecification = displaySpecification.getSpecification(category);
			final MetricsResult results = run.getResultsForCategory(category);
			final MetricTableModel model = new MetricTableModel(results, type, tableSpecification, metricsPlugin.getProfileRepository());
			table.setModel(model);
			final Container tab = table.getParent().getParent();
			if(model.getRowCount() == 0)
			{
				tabbedPane.remove(tab);
				continue;
			}
			final String longName = MetricsCategoryNameUtil.getLongNameForCategory(category);
			tabbedPane.add(tab, longName);
			table.setCellSelectionEnabled(false);
			table.setRowSelectionAllowed(false);
			table.setColumnSelectionAllowed(true);
			table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			final MyColumnListener columnListener = new MyColumnListener(tableSpecification, table);
			final TableColumnModel columnModel = table.getColumnModel();
			columnModel.addColumnModelListener(columnListener);
			final int columnCount = columnModel.getColumnCount();
			for(int i = 0; i < columnCount; i++)
			{
				final TableColumn column = columnModel.getColumn(i);
				column.addPropertyChangeListener(columnListener);
			}
			setRenderers(table, type);
			setColumnWidths(table, tableSpecification);
		}
	}

	public void updateMetricsResults(
			MetricsRun run, MetricDisplaySpecification displaySpecification)
	{
		final MetricCategory[] categories = MetricCategory.values();
		for(final MetricCategory category : categories)
		{
			final JTable table = tables.get(category);
			final MetricTableModel model = (MetricTableModel) table.getModel();
			model.setResults(run.getResultsForCategory(category));
			table.setCellSelectionEnabled(false);
			table.setRowSelectionAllowed(false);
			table.setColumnSelectionAllowed(true);
			table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			final String shortName = MetricsCategoryNameUtil.getShortNameForCategory(category);
			setRenderers(table, shortName);
			final MetricTableSpecification specification = displaySpecification.getSpecification(category);
			setColumnWidths(table, specification);
		}
	}

	public void updateMetricsResultsWithDiff(
			MetricsRun results, MetricDisplaySpecification displaySpecification)
	{
		final MetricCategory[] categories = MetricCategory.values();
		for(final MetricCategory category : categories)
		{
			final JTable table = tables.get(category);
			final MetricTableModel model = (MetricTableModel) table.getModel();
			final MetricsResult prevResults = model.getResults();
			model.setPrevResults(prevResults);
			model.setResults(results.getResultsForCategory(category));
			final Container tab = table.getParent().getParent();
			if(model.getRowCount() == 0)
			{
				tabbedPane.remove(tab);
				continue;
			}
			final String longName = MetricsCategoryNameUtil.getLongNameForCategory(category);
			tabbedPane.add(tab, longName);
			final String shortName = MetricsCategoryNameUtil.getShortNameForCategory(category);
			setRenderers(table, shortName);
			final MetricTableSpecification specification = displaySpecification.getSpecification(category);
			setColumnWidths(table, specification);
		}
		hasOverlay = true;
	}

	public void overlayWithDiff(
			MetricsRun prevRun, MetricDisplaySpecification displaySpecification)
	{
		final MetricCategory[] categories = MetricCategory.values();
		for(final MetricCategory category : categories)
		{
			final JTable table = tables.get(category);
			final MetricTableModel model = (MetricTableModel) table.getModel();
			model.setPrevResults(prevRun.getResultsForCategory(category));
			final Container tab = table.getParent().getParent();
			if(model.getRowCount() == 0)
			{
				tabbedPane.remove(tab);
				continue;
			}
			final String longName = MetricsCategoryNameUtil.getLongNameForCategory(category);
			tabbedPane.add(tab, longName);
			final String shortName = MetricsCategoryNameUtil.getShortNameForCategory(category);
			setRenderers(table, shortName);
			final MetricTableSpecification specification = displaySpecification.getSpecification(category);
			setColumnWidths(table, specification);
		}
		hasOverlay = true;
	}

	public void removeDiffOverlay(MetricDisplaySpecification displaySpecification)
	{
		final MetricCategory[] categories = MetricCategory.values();
		for(final MetricCategory category : categories)
		{
			final JTable table = tables.get(category);
			final MetricTableModel model = (MetricTableModel) table.getModel();
			model.setPrevResults(null);
			final Container tab = table.getParent().getParent();
			if(model.getRowCount() == 0)
			{
				tabbedPane.remove(tab);
				continue;
			}
			final String longName = MetricsCategoryNameUtil.getLongNameForCategory(category);
			tabbedPane.add(tab, longName);
			final String shortName = MetricsCategoryNameUtil.getShortNameForCategory(category);
			setRenderers(table, shortName);
			final MetricTableSpecification specification = displaySpecification.getSpecification(category);
			setColumnWidths(table, specification);
		}
		hasOverlay = false;
	}

	private static void setColumnWidths(JTable table, MetricTableSpecification tableSpecification)
	{
		final TableModel model = table.getModel();
		final TableColumnModel columnModel = table.getColumnModel();

		final List<Integer> columnWidths = tableSpecification.getColumnWidths();
		final List<String> columnOrder = tableSpecification.getColumnOrder();
		if(columnWidths != null && !columnWidths.isEmpty())
		{

			final int columnCount = model.getColumnCount();
			for(int i = 0; i < columnCount; i++)
			{
				final String columnName = model.getColumnName(i);
				final int index = columnOrder.indexOf(columnName);
				if(index != -1)
				{
					final Integer width = columnWidths.get(index);
					final TableColumn column = columnModel.getColumn(i);
					column.setPreferredWidth(width);
				}
			}
		}
		else
		{
			final Graphics graphics = table.getGraphics();
			final Font font = table.getFont();
			final FontMetrics fontMetrics = table.getFontMetrics(font);

			final int rowCount = model.getRowCount();
			int maxFirstColumnWidth = 100;
			for(int i = 0; i < rowCount; i++)
			{
				final String name = (String) model.getValueAt(i, 0);
				if(name != null)
				{
					final Rectangle2D stringBounds = fontMetrics.getStringBounds(name, graphics);
					final double stringWidth = stringBounds.getWidth();
					if(stringWidth > maxFirstColumnWidth)
					{
						maxFirstColumnWidth = (int) stringWidth;
					}
				}
			}

			final int allocatedFirstColumnWidth = Math.min(300, maxFirstColumnWidth + 5);
			final TableColumn column = columnModel.getColumn(0);
			column.setPreferredWidth(allocatedFirstColumnWidth);
		}
	}

	private static void setRenderers(JTable table, String type)
	{
		final MetricTableModel model = (MetricTableModel) table.getModel();
		final MetricInstance[] metrics = model.getMetricsInstances();
		Arrays.sort(metrics, new MetricInstanceAbbreviationComparator());
		final TableColumnModel columnModel = table.getColumnModel();
		for(int i = 0; i < model.getColumnCount(); i++)
		{
			final String columnName = model.getColumnName(i);
			final TableColumn column = columnModel.getColumn(i);
			if(columnName.equals(type))
			{
				final TableCellRenderer renderer = new DefaultTableCellRenderer();
				column.setCellRenderer(renderer);
				final HeaderRenderer headerRenderer = new HeaderRenderer(null, model);
				column.setHeaderRenderer(headerRenderer);
			}
			else
			{
				final MetricInstance metricInstance = model.getMetricForColumn(i);
				final TableCellRenderer renderer = new MetricCellRenderer(metricInstance);
				column.setCellRenderer(renderer);
				final Metric metric = metricInstance.getMetric();
				final String displayName = metric.getDisplayName();
				final HeaderRenderer headerRenderer = new HeaderRenderer(displayName, model);
				column.setHeaderRenderer(headerRenderer);
			}
		}
	}

	public JTabbedPane getTabbedPane()
	{
		return tabbedPane;
	}

	public boolean hasDiffOverlay()
	{
		return hasOverlay;
	}

	public MetricCategory getSelectedCategory()
	{
		final Component component = tabbedPane.getSelectedComponent();
		for(MetricCategory category : MetricCategory.values())
		{
			final JTable table = tables.get(category);
			if(table.getParent().getParent().equals(component))
			{
				return category;
			}
		}
		return null;
	}

	private class MyColumnListener implements TableColumnModelListener, PropertyChangeListener
	{

		private final MetricTableSpecification tableSpecification;
		private final JTable table;

		private MyColumnListener(MetricTableSpecification tableSpecification, JTable table)
		{
			this.tableSpecification = tableSpecification;
			this.table = table;
		}

		@Override
		public void columnAdded(TableColumnModelEvent e)
		{
		}

		@Override
		public void columnRemoved(TableColumnModelEvent e)
		{
		}

		@Override
		public void columnMoved(TableColumnModelEvent e)
		{
			saveColumnSpecification();
		}

		@Override
		public void columnMarginChanged(ChangeEvent e)
		{
		}

		@Override
		public void columnSelectionChanged(ListSelectionEvent e)
		{
		}

		@Override
		@SuppressWarnings("HardCodedStringLiteral")
		public void propertyChange(PropertyChangeEvent evt)
		{
			final String propertyName = evt.getPropertyName();
			if("width".equalsIgnoreCase(propertyName))
			{
				saveColumnSpecification();
			}
		}

		private void saveColumnSpecification()
		{
			final TableColumnModel columnModel = table.getColumnModel();
			final int numColumns = table.getColumnCount();
			final List<String> columns = new ArrayList<String>(numColumns);
			final List<Integer> columnWidths = new ArrayList<Integer>(numColumns);
			for(int i = 0; i < numColumns; i++)
			{
				final String columnName = table.getColumnName(i);
				columns.add(columnName);
				final TableColumn column = columnModel.getColumn(i);
				final int columnWidth = column.getWidth();
				columnWidths.add(columnWidth);
			}
			tableSpecification.setColumnOrder(columns);
			tableSpecification.setColumnWidths(columnWidths);
			metricsPlugin.getProfileRepository().persistCurrentProfile();
		}
	}
}
