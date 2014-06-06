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

package com.sixrr.metrics.metricModel;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jetbrains.annotations.Nullable;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import com.sixrr.metrics.Metric;
import com.sixrr.metrics.MetricType;
import com.sixrr.metrics.profile.MetricsProfile;
import com.sixrr.metrics.utils.StringToFractionMap;

public class MetricsResultImpl implements MetricsResult
{
	private final Map<Metric, StringToFractionMap> values = new HashMap<Metric, StringToFractionMap>(32);
	private final Set<String> measuredObjects = new HashSet<String>(32);
	private final Set<Metric> metrics = new HashSet<Metric>(32);
	private final Map<String, SmartPsiElementPointer<PsiElement>> elements = new HashMap<String, SmartPsiElementPointer<PsiElement>>(1024);

	@Override
	public void postValue(Metric metric, String measured, double value)
	{
		postValue(metric, measured, value, 1.0);
	}

	@Override
	public void postValue(Metric metric, String measured, double numerator, double denominator)
	{
		if(measured == null)
		{
			return;
		}
		StringToFractionMap metricValues = values.get(metric);
		if(metricValues == null)
		{
			metricValues = new StringToFractionMap();
			values.put(metric, metricValues);
		}
		metricValues.put(measured, numerator, denominator);
		measuredObjects.add(measured);
		metrics.add(metric);
	}

	@Override
	@Nullable
	public Double getValueForMetric(Metric metric, String measured)
	{
		final StringToFractionMap metricValues = values.get(metric);
		if(metricValues == null)
		{
			return null;
		}
		if(metricValues.containsKey(measured))
		{
			return metricValues.get(measured);
		}
		else
		{
			return null;
		}
	}

	@Override
	public String[] getMeasuredObjects()
	{
		return measuredObjects.toArray(new String[measuredObjects.size()]);
	}

	@Override
	public Metric[] getMetrics()
	{
		return metrics.toArray(new Metric[metrics.size()]);
	}

	@Override
	@Nullable
	public Double getMinimumForMetric(Metric metric)
	{
		final StringToFractionMap metricValues = values.get(metric);
		if(metricValues == null)
		{
			return 0.0;
		}
		return metricValues.getMinimum();
	}

	@Override
	@Nullable
	public Double getMaximumForMetric(Metric metric)
	{
		final StringToFractionMap metricValues = values.get(metric);
		if(metricValues == null)
		{
			return 0.0;
		}
		return metricValues.getMaximum();
	}

	@Override
	@Nullable
	public Double getTotalForMetric(Metric metric)
	{
		final MetricType metricType = metric.getType();
		if(metricType != MetricType.Count)
		{
			return null;
		}
		final StringToFractionMap metricValues = values.get(metric);
		return metricValues.getTotal();
	}

	@Override
	@Nullable
	public Double getAverageForMetric(Metric metric)
	{
		final MetricType metricType = metric.getType();
		if(metricType == MetricType.RecursiveCount || metricType == MetricType.RecursiveRatio)
		{
			return null;
		}
		final StringToFractionMap metricValues = values.get(metric);
		return metricValues.getAverage();
	}

	@Override
	public void setElementForMeasuredObject(String measuredObject, PsiElement element)
	{
		final PsiManager psiManager = element.getManager();
		final Project project = psiManager.getProject();
		final SmartPointerManager pointerManager = SmartPointerManager.getInstance(project);
		final SmartPsiElementPointer<PsiElement> pointer = pointerManager.createSmartPsiElementPointer(element);
		elements.put(measuredObject, pointer);
	}

	@Override
	@Nullable
	public PsiElement getElementForMeasuredObject(String measuredObject)
	{
		final SmartPsiElementPointer<PsiElement> pointer = elements.get(measuredObject);
		if(pointer == null)
		{
			return null;
		}
		return pointer.getElement();
	}

	@Override
	public boolean hasWarnings(MetricsProfile profile)
	{
		for(Metric metric : metrics)
		{
			final MetricInstance metricInstance = profile.getMetricForClass(metric.getClass());
			if(metricInstance == null)
			{
				continue;
			}
			final StringToFractionMap valuesForMetric = values.get(metric);
			for(String measuredObject : measuredObjects)
			{
				final double value = valuesForMetric.get(measuredObject);
				if(metricInstance.isUpperThresholdEnabled() && value > metricInstance.getUpperThreshold())
				{
					return true;
				}
				if(metricInstance.isLowerThresholdEnabled() && value < metricInstance.getLowerThreshold())
				{
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public MetricsResult filterRowsWithoutWarnings(MetricsProfile profile)
	{
		final MetricsResultImpl out = new MetricsResultImpl();
		for(String measuredObject : measuredObjects)
		{
			boolean found = false;
			for(Metric metric : metrics)
			{
				final MetricInstance metricInstance = profile.getMetricForClass(metric.getClass());
				if(metricInstance == null || !metricInstance.isEnabled())
				{
					continue;
				}
				final StringToFractionMap valuesForMetric = values.get(metric);
				if(!valuesForMetric.containsKey(measuredObject))
				{
					continue;
				}
				final double value = valuesForMetric.get(measuredObject);
				if(metricInstance.isUpperThresholdEnabled() && value > metricInstance.getUpperThreshold())
				{
					found = true;
					break;
				}
				if(metricInstance.isLowerThresholdEnabled() && value < metricInstance.getLowerThreshold())
				{
					found = true;
					break;
				}
			}
			if(found)
			{
				for(Metric metric : metrics)
				{
					final StringToFractionMap valuesForMetric = values.get(metric);
					final double value = valuesForMetric.get(measuredObject);
					out.postValue(metric, measuredObject, value, 1.0); //not quite right
				}
				out.setElementForMeasuredObject(measuredObject, getElementForMeasuredObject(measuredObject));
			}
		}
		return out;

	}
}
