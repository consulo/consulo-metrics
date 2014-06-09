/*
 * Copyright 2005-2011 Sixth and Red River Software, Bas Leijdekkers
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

package com.sixrr.metrics.metricModel;

import org.jetbrains.annotations.NotNull;
import com.sixrr.metrics.Metric;
import com.sixrr.metrics.MetricCategory;

public class MetricInstanceImpl implements MetricInstance
{
	private final Metric myMetric;
	private final String myMetricClassName;
	private boolean enabled = false;
	private boolean upperThresholdEnabled = false;
	private double upperThreshold = 0.0;
	private boolean lowerThresholdEnabled = false;
	private double lowerThreshold = 0.0;

	public MetricInstanceImpl(@NotNull String metricClassName, Metric metric)
	{
		myMetric = metric;
		myMetricClassName = metricClassName;
	}

	@Override
	public void copyFrom(MetricInstance o)
	{
		upperThresholdEnabled = o.isUpperThresholdEnabled();
		upperThreshold = o.getUpperThreshold();
		lowerThresholdEnabled = o.isLowerThresholdEnabled();
		lowerThreshold = o.getLowerThreshold();
	}

	@Override
	public int compareTo(@NotNull MetricInstance o)
	{
		final MetricCategory category1 = myMetric.getCategory();
		final MetricCategory category2 = o.getMetric().getCategory();
		final int categoryComparison = category1.compareTo(category2);
		if(categoryComparison != 0)
		{
			return -categoryComparison;
		}
		final String displayName1 = myMetric.getDisplayName();
		final String displayName2 = o.getMetric().getDisplayName();
		return displayName1.compareTo(displayName2);
	}

	@Override
	public boolean equals(Object o)
	{
		return o instanceof MetricInstanceImpl && getMetricClass().equals(((MetricInstanceImpl) o).getMetricClass());
	}

	@Override
	public int hashCode()
	{
		return 31 * myMetric.getCategory().hashCode() + myMetric.getDisplayName().hashCode();
	}

	@NotNull
	@Override
	public String getMetricClass()
	{
		return myMetricClassName;
	}

	@NotNull
	@Override
	public Metric getMetric()
	{
		return myMetric;
	}

	@Override
	public boolean isEnabled()
	{
		return enabled;
	}

	@Override
	public void setEnabled(boolean enabled)
	{
		this.enabled = enabled;
	}

	@Override
	public boolean isUpperThresholdEnabled()
	{
		return upperThresholdEnabled;
	}

	@Override
	public void setUpperThresholdEnabled(boolean upperThresholdEnabled)
	{
		this.upperThresholdEnabled = upperThresholdEnabled;
	}

	@Override
	public double getUpperThreshold()
	{
		return upperThreshold;
	}

	@Override
	public void setUpperThreshold(double upperThreshold)
	{
		this.upperThreshold = upperThreshold;
	}

	@Override
	public boolean isLowerThresholdEnabled()
	{
		return lowerThresholdEnabled;
	}

	@Override
	public void setLowerThresholdEnabled(boolean lowerThresholdEnabled)
	{
		this.lowerThresholdEnabled = lowerThresholdEnabled;
	}

	@Override
	public double getLowerThreshold()
	{
		return lowerThreshold;
	}

	@Override
	public void setLowerThreshold(double lowerThreshold)
	{
		this.lowerThreshold = lowerThreshold;
	}

	@Override
	public MetricInstanceImpl clone() throws CloneNotSupportedException
	{
		return (MetricInstanceImpl) super.clone();
	}

	@Override
	public String toString()
	{
		return myMetric.getCategory() + "/" + myMetric.getDisplayName();
	}
}
