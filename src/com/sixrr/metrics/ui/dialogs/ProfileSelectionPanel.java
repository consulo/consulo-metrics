/*
 * Copyright 2005-2011, Bas Leijdekkers, Sixth and Red River Software
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

package com.sixrr.metrics.ui.dialogs;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;

import com.intellij.openapi.project.Project;
import com.intellij.ui.ComboboxWithBrowseButton;
import com.intellij.ui.TitledSeparator;
import com.sixrr.metrics.config.MetricsReloadedConfig;
import com.sixrr.metrics.plugin.MetricsPlugin;
import com.sixrr.metrics.profile.MetricsProfile;
import com.sixrr.metrics.profile.MetricsProfileRepository;
import com.sixrr.metrics.ui.metricdisplay.MetricsConfigurationDialog;
import com.sixrr.metrics.utils.MetricsReloadedBundle;

public class ProfileSelectionPanel extends JPanel
{

	public ProfileSelectionPanel(Project project)
	{
		super(new GridBagLayout());
		final MetricsPlugin plugin = project.getComponent(MetricsPlugin.class);
		final MetricsProfileRepository repository = plugin.getProfileRepository();
		final MetricsReloadedConfig configuration = plugin.getConfiguration();

		final ComboboxWithBrowseButton comboboxWithBrowseButton = buildComboBoxWithBrowseButton(project, repository);

		final JComponent separator = new TitledSeparator(MetricsReloadedBundle.message("metrics.profile"));
		final JCheckBox checkBox = buildCheckBox(configuration);

		final GridBagConstraints constraints = new GridBagConstraints();
		constraints.insets.left = 0;
		constraints.insets.bottom = 8;
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.weightx = 1.0;
		constraints.weighty = 0.0;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.anchor = GridBagConstraints.NORTHWEST;
		add(separator, constraints);

		constraints.insets.left = 12;
		constraints.gridy = 1;
		add(comboboxWithBrowseButton, constraints);

		constraints.gridy = 2;
		constraints.weighty = 1.0;
		add(checkBox, constraints);
	}

	private static JCheckBox buildCheckBox(final MetricsReloadedConfig configuration)
	{
		final JCheckBox checkBox = new JCheckBox(MetricsReloadedBundle.message("show.only.results.which.exceed.metrics.thresholds"));
		checkBox.setSelected(configuration.isShowOnlyWarnings());
		checkBox.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent event)
			{
				configuration.setShowOnlyWarnings(checkBox.isSelected());
			}
		});
		return checkBox;
	}

	private static ComboboxWithBrowseButton buildComboBoxWithBrowseButton(
			final Project project, final MetricsProfileRepository repository)
	{
		final String[] profiles = repository.getProfileNames();
		final JComboBox comboBox = new JComboBox(new DefaultComboBoxModel(profiles));
		final ComboboxWithBrowseButton comboboxWithBrowseButton = new ComboboxWithBrowseButton(comboBox);
		final MetricsProfile currentProfile = repository.getCurrentProfile();
		final String currentProfileName = currentProfile.getName();
		comboBox.setSelectedItem(currentProfileName);
		comboBox.addItemListener(new ItemListener()
		{
			@Override
			public void itemStateChanged(ItemEvent event)
			{
				if(event.getStateChange() == ItemEvent.DESELECTED)
				{
					return;
				}
				final String selectedProfile = (String) comboBox.getSelectedItem();
				repository.setSelectedProfile(selectedProfile);
			}
		});
		comboboxWithBrowseButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				final MetricsConfigurationDialog configurationDialog = new MetricsConfigurationDialog(project, repository);
				configurationDialog.show();
				final String[] profiles = repository.getProfileNames();
				comboBox.setModel(new DefaultComboBoxModel(profiles));
				final MetricsProfile currentProfile = repository.getCurrentProfile();
				final String currentProfileName = currentProfile.getName();
				comboBox.setSelectedItem(currentProfileName);
			}
		});
		return comboboxWithBrowseButton;
	}
}
