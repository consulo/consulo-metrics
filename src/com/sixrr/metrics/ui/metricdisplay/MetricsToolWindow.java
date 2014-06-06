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

package com.sixrr.metrics.ui.metricdisplay;

import org.jetbrains.annotations.NonNls;
import com.intellij.analysis.AnalysisScope;
import com.sixrr.metrics.MetricCategory;
import com.sixrr.metrics.metricModel.MetricsRun;
import com.sixrr.metrics.profile.MetricsProfile;

public interface MetricsToolWindow
{

	@NonNls
	String TOOL_WINDOW_ICON_PATH = "/images/metricsToolWindow.png";
	@NonNls
	String METRICS_TOOL_WINDOW_ID = "Metrics";

	void register();

	void show(MetricsRun results, MetricsProfile profile, AnalysisScope scope, boolean showOnlyWarnings);

	void update(MetricsRun results);

	void updateWithDiff(MetricsRun results);

	void reloadAsDiff(MetricsRun prevResults);

	void removeDiffOverlay();

	boolean hasDiffOverlay();

	void close();

	MetricsRun getCurrentRun();

	AnalysisScope getCurrentScope();

	void unregister();

	MetricsProfile getCurrentProfile();

	MetricCategory getSelectedCategory();
}
