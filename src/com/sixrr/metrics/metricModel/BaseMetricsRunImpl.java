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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jetbrains.annotations.NonNls;
import com.intellij.analysis.AnalysisScope;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.psi.PsiElement;
import com.sixrr.metrics.Metric;
import com.sixrr.metrics.MetricCategory;
import com.sixrr.metrics.MetricsResultsHolder;
import com.sixrr.metrics.profile.MetricsProfile;

public class BaseMetricsRunImpl implements MetricsRun, MetricsResultsHolder
{
	private static final Logger logger = Logger.getInstance("MetricsReloaded");
	private final Map<MetricCategory, MetricsResult> metricResults = new EnumMap<MetricCategory, MetricsResult>(MetricCategory.class);
	private String profileName = null;
	private AnalysisScope context = null;
	private TimeStamp timestamp = null;

	public BaseMetricsRunImpl()
	{
		final MetricCategory[] categories = MetricCategory.values();
		for(MetricCategory category : categories)
		{
			metricResults.put(category, new MetricsResultImpl());
		}
	}

	@Override
	public List<Metric> getMetrics()
	{
		final Set<Metric> allMetrics = new HashSet<Metric>();
		final Collection<MetricsResult> results = metricResults.values();
		for(MetricsResult result : results)
		{
			final Metric[] metrics = result.getMetrics();
			allMetrics.addAll(Arrays.asList(metrics));
		}
		return new ArrayList<Metric>(allMetrics);
	}

	@Override
	public void postProjectMetric(Metric metric, double value)
	{
		final MetricCategory category = metric.getCategory();
		if(category != MetricCategory.Project)
		{
			logger.error("Posting a project metric result from a " + MetricsCategoryNameUtil.getShortNameForCategory(category) + " metric");
		}
		final MetricsResult results = getResultsForCategory(MetricCategory.Project);
		results.postValue(metric, "project", value);
	}

	@Override
	public void postModuleMetric(Metric metric, Module module, double value)
	{
		final MetricCategory category = metric.getCategory();
		if(category != MetricCategory.Module)
		{
			logger.error("Posting a module metric result from a " + MetricsCategoryNameUtil.getShortNameForCategory(category) + " metric");
		}
		final MetricsResult results = getResultsForCategory(MetricCategory.Module);
		results.postValue(metric, module.getName(), value);
	}

	@Override
	public void postProjectMetric(Metric metric, double numerator, double denominator)
	{

		final MetricCategory category = metric.getCategory();
		if(category != MetricCategory.Project)
		{
			logger.error("Posting a project metric result from a " + MetricsCategoryNameUtil.getShortNameForCategory(category) + " metric");
		}
		final MetricsResult results = getResultsForCategory(MetricCategory.Project);
		results.postValue(metric, "project", numerator, denominator);
	}

	@Override
	public void postModuleMetric(Metric metric, Module module, double numerator, double denominator)
	{
		final MetricCategory category = metric.getCategory();
		if(category != MetricCategory.Module)
		{
			logger.error("Posting a module metric result from a " + MetricsCategoryNameUtil.getShortNameForCategory(category) + " metric");
		}
		final MetricsResult results = getResultsForCategory(MetricCategory.Module);
		results.postValue(metric, module.getName(), numerator, denominator);
	}

	public void postRawMetric(Metric metric, String measured, double value)
	{
		final MetricCategory category = metric.getCategory();
		final MetricsResult result = metricResults.get(category);
		result.postValue(metric, measured, value);
	}

	public void postMetric(Metric metric, String key, double value)
	{
		final MetricCategory category = metric.getCategory();
		final MetricsResult results = getResultsForCategory(category);
		results.postValue(metric, key, value);
	}

	public void postMetric(Metric metric, String key, double numerator, double denominator)
	{
		final MetricCategory category = metric.getCategory();
		final MetricsResult results = getResultsForCategory(category);
		results.postValue(metric, key, numerator, denominator);
	}

	public void postMetric(Metric metric, String key, PsiElement element, double value)
	{
		final MetricCategory category = metric.getCategory();
		final MetricsResult results = getResultsForCategory(category);
		results.postValue(metric, key, value);
		if(element != null)
		{
			results.setElementForMeasuredObject(key, element);
		}
	}

	public void postMetric(Metric metric, String key, PsiElement element, double numerator, double denominator)
	{
		final MetricCategory category = metric.getCategory();
		final MetricsResult results = getResultsForCategory(category);
		results.postValue(metric, key, numerator, denominator);
		if(element != null)
		{
			results.setElementForMeasuredObject(key, element);
		}
	}

	@Override
	public MetricsResult getResultsForCategory(MetricCategory category)
	{
		return metricResults.get(category);
	}

	public void setResultsForCategory(MetricCategory category, MetricsResult results)
	{
		metricResults.put(category, results);
	}

	@Override
	public void writeToFile(String fileName)
	{
		FileOutputStream fileStr = null;
		@NonNls PrintWriter writer = null;
		try
		{
			fileStr = new FileOutputStream(fileName);
			writer = new PrintWriter(fileStr);
			writer.println("<SNAPSHOT" + " profile = \"" + profileName + '\"' + " timestamp = \"" + timestamp + '\"' + '>');

			final MetricCategory[] categories = MetricCategory.values();
			for(MetricCategory category : categories)
			{
				writeResultsForCategory(category, writer);
			}
			writer.println("</SNAPSHOT>");
		}
		catch(IOException e)
		{
			return;
		}
		finally
		{
			try
			{
				if(writer != null)
				{
					writer.close();
				}
				if(fileStr != null)
				{
					fileStr.close();
				}
			}
			catch(IOException e)
			{
			}
		}
	}

	@Override
	public String getProfileName()
	{
		return profileName;
	}

	public void setProfileName(String profileName)
	{
		this.profileName = profileName;
	}

	public void setContext(AnalysisScope context)
	{
		this.context = context;
	}

	public void setTimestamp(TimeStamp timestamp)
	{
		this.timestamp = timestamp;
	}

	@Override
	public TimeStamp getTimestamp()
	{
		return timestamp;
	}

	@Override
	public AnalysisScope getContext()
	{
		return context;
	}

	@Override
	public MetricsRun filterRowsWithoutWarnings(MetricsProfile profile)
	{
		final BaseMetricsRunImpl out = new BaseMetricsRunImpl();
		out.context = context;
		out.profileName = profileName;
		out.timestamp = timestamp;

		final Set<MetricCategory> categories = metricResults.keySet();
		for(MetricCategory category : categories)
		{
			final MetricsResult results = getResultsForCategory(category);
			final MetricsResult filteredResults = results.filterRowsWithoutWarnings(profile);
			out.setResultsForCategory(category, filteredResults);
		}

		return out;
	}

	private void writeResultsForCategory(MetricCategory category, PrintWriter writer)
	{
		final MetricsResult results = getResultsForCategory(category);
		final Metric[] metrics = results.getMetrics();
		for(final Metric metric : metrics)
		{
			writeResultsForMetric(metric, results, writer);
		}
	}

	private static void writeResultsForMetric(Metric metric, MetricsResult results, @NonNls PrintWriter writer)
	{
		final Class<?> metricClass = metric.getClass();
		final String[] measuredObjects = results.getMeasuredObjects();
		writer.println("\t\t<METRIC class_name= \"" + metricClass.getName() + "\">");
		for(final String measuredObject : measuredObjects)
		{
			writeValue(results, metric, measuredObject, writer);
		}
		writer.println("\t\t</METRIC>");
	}

	private static void writeValue(
			MetricsResult results, Metric metric, String measuredObject, @NonNls PrintWriter writer)
	{
		final Double value = results.getValueForMetric(metric, measuredObject);
		if(value != null)
		{
			writer.println("\t\t\t<VALUE measured = \"" + measuredObject + "\" value = \"" + value + "\"/>");
		}
	}

	public static MetricsRun readFromFile(File file)
	{
		final SAXBuilder builder = new SAXBuilder();
		final Document doc;
		try
		{
			doc = builder.build(file);
		}
		catch(Exception e)
		{
			return null;
		}
		@NonNls final Element snapshotElement = doc.getRootElement();
		final BaseMetricsRunImpl run = new BaseMetricsRunImpl();
		run.setTimestamp(new TimeStamp(snapshotElement.getAttributeValue("timestamp")));
		run.setProfileName(snapshotElement.getAttributeValue("profile"));
		final List metrics = snapshotElement.getChildren("METRIC");
		for(final Object metric : metrics)
		{
			final Element metricElement = (Element) metric;
			readMetricElement(metricElement, run);
		}
		return run;
	}

	private static void readMetricElement(@NonNls Element metricElement, BaseMetricsRunImpl run)
	{
		try
		{
			final String className = metricElement.getAttributeValue("class_name");
			final Class<?> metricClass = Class.forName(className);
			final Metric metric = (Metric) metricClass.newInstance();
			final List values = metricElement.getChildren("VALUE");
			for(final Object value1 : values)
			{
				@NonNls final Element valueElement = (Element) value1;
				final String measured = valueElement.getAttributeValue("measured");
				final String valueString = valueElement.getAttributeValue("value");
				final double value = Double.parseDouble(valueString);
				run.postRawMetric(metric, measured, value);
			}
		}
		catch(Exception e)
		{
			//don't do anything;
		}
	}

	public boolean hasWarnings(MetricsProfile profile)
	{
		for(MetricCategory category : MetricCategory.values())
		{
			final MetricsResult result = metricResults.get(category);
			if(result.hasWarnings(profile))
			{
				return true;
			}
		}
		return false;
	}
}
