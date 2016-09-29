/*
 * Copyright 2013-2014 must-be.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package consulo.metrics.metricModel;

import java.util.List;

import org.jetbrains.annotations.NonNls;
import com.intellij.analysis.AnalysisScope;
import com.intellij.openapi.module.Module;
import com.sixrr.metrics.Metric;
import com.sixrr.metrics.MetricCategory;
import com.sixrr.metrics.MetricsResultsHolder;
import com.sixrr.metrics.metricModel.MetricsResult;
import com.sixrr.metrics.metricModel.MetricsRun;
import com.sixrr.metrics.metricModel.TimeStamp;
import com.sixrr.metrics.profile.MetricsProfile;

/**
 * @author VISTALL
 * @since 06.06.14
 */
public class DelegateMetricsHolder implements MetricsResultsHolder
{
	protected final MetricsResultsHolder myMetricsRun;

	public DelegateMetricsHolder(MetricsResultsHolder metricsRun)
	{
		myMetricsRun = metricsRun;
	}

	@Override
	public List<Metric> getMetrics()
	{
		return myMetricsRun.getMetrics();
	}

	@Override
	public MetricsResult getResultsForCategory(MetricCategory category)
	{
		return myMetricsRun.getResultsForCategory(category);
	}

	@Override
	public void writeToFile(@NonNls String fileName)
	{
		myMetricsRun.writeToFile(fileName);
	}

	@Override
	public String getProfileName()
	{
		return myMetricsRun.getProfileName();
	}

	@Override
	public TimeStamp getTimestamp()
	{
		return myMetricsRun.getTimestamp();
	}

	@Override
	public AnalysisScope getContext()
	{
		return myMetricsRun.getContext();
	}

	@Override
	public MetricsRun filterRowsWithoutWarnings(MetricsProfile profile)
	{
		return myMetricsRun.filterRowsWithoutWarnings(profile);
	}

	@Override
	public void postProjectMetric(Metric metric, double value)
	{
		myMetricsRun.postProjectMetric(metric, value);
	}

	@Override
	public void postModuleMetric(Metric metric, Module module, double value)
	{
		myMetricsRun.postModuleMetric(metric, module, value);
	}

	@Override
	public void postProjectMetric(Metric metric, double numerator, double denominator)
	{
		myMetricsRun.postProjectMetric(metric, numerator, denominator);
	}

	@Override
	public void postModuleMetric(Metric metric, Module module, double numerator, double denominator)
	{
		myMetricsRun.postModuleMetric(metric, module, numerator, denominator);
	}
}
