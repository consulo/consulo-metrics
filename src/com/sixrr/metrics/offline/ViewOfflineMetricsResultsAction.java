/*
 * Copyright 2005-2013 Sixth and Red River Software, Bas Leijdekkers
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

package com.sixrr.metrics.offline;

import java.awt.Window;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataConstants;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.WindowManager;
import com.sixrr.metrics.Metric;
import com.sixrr.metrics.metricModel.BaseMetricsRunImpl;
import com.sixrr.metrics.metricModel.MetricInstance;
import com.sixrr.metrics.metricModel.MetricInstanceImpl;
import com.sixrr.metrics.metricModel.MetricsRun;
import com.sixrr.metrics.plugin.MetricsPlugin;
import com.sixrr.metrics.profile.MetricsProfile;
import com.sixrr.metrics.profile.MetricsProfileImpl;
import com.sixrr.metrics.profile.MetricsProfileRepository;
import com.sixrr.metrics.ui.metricdisplay.MetricsToolWindow;
import com.sixrr.metrics.ui.metricdisplay.SnapshotFileFilter;

public class ViewOfflineMetricsResultsAction extends AnAction
{

	@Override
	public void actionPerformed(AnActionEvent event)
	{
		final DataContext dataContext = event.getDataContext();
		final Project project = CommonDataKeys.PROJECT.getData(dataContext);

		final JFileChooser chooser = new JFileChooser();
		final FileFilter filter = new SnapshotFileFilter();
		chooser.setFileFilter(filter);
		final WindowManager myWindowManager = WindowManager.getInstance();
		final Window parent = myWindowManager.suggestParentWindow(project);
		final int returnVal = chooser.showOpenDialog(parent);

		if(returnVal != JFileChooser.APPROVE_OPTION)
		{
			return;
		}
		final File selectedFile = chooser.getSelectedFile();
		final MetricsRun results = BaseMetricsRunImpl.readFromFile(selectedFile);
		assert project != null;
		final MetricsPlugin plugin = project.getComponent(MetricsPlugin.class);
		final MetricsToolWindow toolWindow = plugin.getMetricsToolWindow();
		final MetricsProfileRepository repository = plugin.getProfileRepository();
		final String profileName = results.getProfileName();
		MetricsProfile profile = repository.getProfileForName(profileName);
		if(profile == null)
		{
			final List<Metric> metrics = results.getMetrics();
			final List<MetricInstance> instances = new ArrayList<MetricInstance>();
			for(Metric metric : metrics)
			{
				final MetricInstanceImpl metricInstance = new MetricInstanceImpl(metric.getClass().getName(), metric);
				metricInstance.setEnabled(true);
				instances.add(metricInstance);
			}
			profile = new MetricsProfileImpl(profileName, instances);
			repository.addProfile(profile);
		}
		toolWindow.show(results, profile, null, false); //TODO
	}

	@Override
	public void update(AnActionEvent event)
	{
		super.update(event);
		final Presentation presentation = event.getPresentation();
		final DataContext dataContext = event.getDataContext();
		final Project project = (Project) dataContext.getData(DataConstants.PROJECT);
		presentation.setEnabled(project != null);
	}
}
