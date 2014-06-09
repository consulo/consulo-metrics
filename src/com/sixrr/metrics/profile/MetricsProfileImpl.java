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

package com.sixrr.metrics.profile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import org.jdom.Document;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.openapi.util.JDOMUtil;
import com.intellij.util.containers.ContainerUtil;
import com.sixrr.metrics.Metric;
import com.sixrr.metrics.MetricCategory;
import com.sixrr.metrics.metricModel.MetricInstance;
import com.sixrr.metrics.metricModel.MetricInstanceImpl;
import com.sixrr.metrics.profile.instanceHolder.DummyMetric;
import com.sixrr.metrics.profile.instanceHolder.MetricInstanceHolder;

public class MetricsProfileImpl implements MetricsProfile
{
	private String myName;
	private Set<MetricInstance> myMetricInstances = new LinkedHashSet<MetricInstance>();
	private MetricDisplaySpecification displaySpecification = new MetricDisplaySpecification();
	private boolean builtIn = false;

	public MetricsProfileImpl(String name, List<MetricInstance> metrics)
	{
		myName = name;

		MetricInstanceHolder.getInstance().createInstances(myMetricInstances);

		myMetricInstances.removeAll(metrics);  // remove old
		myMetricInstances.addAll(metrics);
	}

	@Override
	public MetricDisplaySpecification getDisplaySpecification()
	{
		return displaySpecification;
	}

	@Override
	public void copyFrom(@NotNull List<MetricInstance> metrics)
	{
		for(MetricInstance newMetric : metrics)
		{
			MetricInstance methodByClassName = getMethodByClassName(newMetric.getMetricClass());

			assert methodByClassName != null;

			methodByClassName.copyFrom(newMetric);
		}
	}

	@Override
	public boolean isBuiltIn()
	{
		return builtIn;
	}

	@Override
	public void setBuiltIn(boolean builtIn)
	{
		this.builtIn = builtIn;
	}

	@Override
	public void setName(String newProfileName)
	{
		myName = newProfileName;
	}

	@Override
	public String getName()
	{
		return myName;
	}

	@NotNull
	@Override
	public Set<MetricInstance> getMetrics()
	{
		return Collections.unmodifiableSet(myMetricInstances);
	}

	@NotNull
	@Override
	public List<MetricInstance> getSortedMetrics(boolean onlyEnabled)
	{
		List<MetricInstance> metricInstances = new ArrayList<MetricInstance>();
		for(MetricInstance metricInstance : myMetricInstances)
		{
			if(metricInstance.getMetric() == DummyMetric.INSTANCE)
			{
				continue;
			}

			if(onlyEnabled && !metricInstance.isEnabled())
			{
				continue;
			}
			metricInstances.add(metricInstance);
		}
		ContainerUtil.sort(metricInstances);
		return metricInstances;
	}

	@Override
	public Object clone() throws CloneNotSupportedException
	{
		final MetricsProfileImpl out = (MetricsProfileImpl) super.clone();
		out.myMetricInstances = new HashSet<MetricInstance>(myMetricInstances.size());
		for(MetricInstance metric : myMetricInstances)
		{
			out.myMetricInstances.add(metric.clone());
		}
		out.displaySpecification = new MetricDisplaySpecification();
		return out;
	}

	@Override
	@Nullable
	public MetricInstance getMetricForClass(Class<? extends Metric> aClass)
	{
		for(final MetricInstance metric : myMetricInstances)
		{
			final Class<? extends Metric> metricClass = metric.getMetric().getClass();
			if(metricClass.equals(aClass))
			{
				return metric;
			}
		}
		return null;
	}

	@Override
	@Nullable
	public MetricInstance getMetricByID(String name)
	{
		for(final MetricInstance metric : myMetricInstances)
		{
			final String metricName = metric.getMetric().getID();
			if(metricName.equals(name))
			{
				return metric;
			}
		}
		return null;
	}

	@Nullable
	public MetricInstance getMethodByClassName(String name)
	{
		for(final MetricInstance metric : myMetricInstances)
		{
			final String metricName = metric.getMetric().getClass().getName();
			if(metricName.equals(name))
			{
				return metric;
			}
		}
		return null;
	}

	@SuppressWarnings("HardCodedStringLiteral")
	@Nullable
	public static MetricsProfile loadFromFile(File file)
	{
		final Document doc;
		try
		{
			doc = JDOMUtil.loadDocument(file);
		}
		catch(Exception e)
		{
			return null;
		}
		final Element profileRoot = doc.getRootElement();
		final String profileName = profileRoot.getAttributeValue("name");
		final List<Element> children = profileRoot.getChildren("METRIC");
		final List<MetricInstance> profileMetrics = new ArrayList<MetricInstance>(200);
		for(final Element metricElement : children)
		{
			final MetricInstance metric = parseMetric(metricElement);
			if(metric != null)
			{
				profileMetrics.add(metric);
			}
		}
		final MetricsProfileImpl profile = new MetricsProfileImpl(profileName, profileMetrics);
		final Element displaySpecElement = profileRoot.getChild("DISPLAY_SPEC");
		if(displaySpecElement != null)
		{
			parseDisplaySpec(displaySpecElement, profile.getDisplaySpecification());
		}
		return profile;
	}

	@SuppressWarnings({"HardCodedStringLiteral"})
	private static void parseDisplaySpec(
			Element displaySpecElement, MetricDisplaySpecification spec)
	{
		final MetricCategory[] categories = MetricCategory.values();
		for(MetricCategory category : categories)
		{
			final String tag = category.name();
			final Element projectElement = displaySpecElement.getChild(tag.toUpperCase());
			final MetricTableSpecification projectDisplaySpecification = spec.getSpecification(category);
			projectDisplaySpecification.setAscending("true".equals(projectElement.getAttributeValue("ascending")));
			projectDisplaySpecification.setSortColumn(Integer.parseInt(projectElement.getAttributeValue("sort_column")));
			projectDisplaySpecification.setColumnOrder(parseStringList(projectElement.getAttributeValue("column_order")));
			projectDisplaySpecification.setColumnWidths(parseIntList(projectElement.getAttributeValue("column_widths")));
		}
	}

	private static List<Integer> parseIntList(String value)
	{
		if(value != null && value.length() != 0)
		{
			final StringTokenizer tokenizer = new StringTokenizer(value, "|");
			final List<Integer> out = new ArrayList<Integer>(32);
			while(tokenizer.hasMoreTokens())
			{
				final String token = tokenizer.nextToken();
				final Integer intValue = new Integer(token);
				out.add(intValue);
			}
			return out;
		}
		else
		{
			return new ArrayList<Integer>(0);
		}
	}

	private static List<String> parseStringList(String value)
	{
		if(value != null && value.length() != 0)
		{
			final StringTokenizer tokenizer = new StringTokenizer(value, "|");
			final List<String> out = new ArrayList<String>(32);
			while(tokenizer.hasMoreTokens())
			{
				final String token = tokenizer.nextToken();
				out.add(token);
			}
			return out;
		}
		else
		{
			return new ArrayList<String>(0);
		}
	}

	@SuppressWarnings({"HardCodedStringLiteral"})
	@Nullable
	private static MetricInstance parseMetric(Element metricElement)
	{
		final String className = metricElement.getAttributeValue("className");

		final String lowerThresholdEnabledString = metricElement.getAttributeValue("lowerThresholdEnabled");
		boolean lowerThresholdEnabled = false;
		if(lowerThresholdEnabledString != null)
		{
			lowerThresholdEnabled = "true".equals(lowerThresholdEnabledString);
		}
		final String lowerThresholdString = metricElement.getAttributeValue("lowerThreshold");
		double lowerThreshold = 0.0;
		if(lowerThresholdString != null)
		{
			lowerThreshold = Double.parseDouble(lowerThresholdString);
		}
		final String upperThresholdEnabledString = metricElement.getAttributeValue("upperThresholdEnabled");

		boolean upperThresholdEnabled = false;
		if(upperThresholdEnabledString != null)
		{
			upperThresholdEnabled = "true".equals(upperThresholdEnabledString);
		}
		final String upperThresholdString = metricElement.getAttributeValue("upperThreshold");
		double upperThreshold = 0.0;
		if(upperThresholdString != null)
		{
			upperThreshold = Double.parseDouble(upperThresholdString);
		}
		boolean enabled = false;
		final String enabledString = metricElement.getAttributeValue("enabled");
		if(enabledString != null)
		{
			enabled = "true".equals(enabledString);
		}
		Metric metricByClass = MetricInstanceHolder.getInstance().getMetricByClass(className);
		final MetricInstance metricInstance = new MetricInstanceImpl(className, metricByClass);
		metricInstance.setEnabled(enabled);
		metricInstance.setLowerThreshold(lowerThreshold);
		metricInstance.setLowerThresholdEnabled(lowerThresholdEnabled);
		metricInstance.setUpperThreshold(upperThreshold);
		metricInstance.setUpperThresholdEnabled(upperThresholdEnabled);
		return metricInstance;
	}

	@Override
	@SuppressWarnings({"HardCodedStringLiteral"})
	public void writeToFile(File profileFile) throws FileNotFoundException
	{
		final PrintWriter writer = new PrintWriter(new FileOutputStream(profileFile));
		try
		{
			writer.println("<METRICS_PROFILE name = \"" + myName + "\">");
			writeDisplaySpec(writer, displaySpecification);
			for(final MetricInstance metric : myMetricInstances)
			{
				writeMetric(metric, writer);
			}
			writer.println("</METRICS_PROFILE>");
		}
		finally
		{
			writer.close();
		}
	}

	@SuppressWarnings({"HardCodedStringLiteral"})
	private static void writeDisplaySpec(
			PrintWriter writer, MetricDisplaySpecification displaySpecification)
	{
		writer.println("\t<DISPLAY_SPEC>");

		final MetricCategory[] categories = MetricCategory.values();
		for(MetricCategory category : categories)
		{
			printTableSpec(displaySpecification, category, writer);
		}
		writer.println("\t</DISPLAY_SPEC>");
	}

	@SuppressWarnings({"HardCodedStringLiteral"})
	private static void printTableSpec(
			MetricDisplaySpecification displaySpecification, MetricCategory category, PrintWriter writer)
	{
		final MetricTableSpecification projectSpec = displaySpecification.getSpecification(category);
		final List<String> columnOrder = projectSpec.getColumnOrder();
		final List<Integer> columnWidths = projectSpec.getColumnWidths();
		final String tag = category.name().toUpperCase();
		writer.println("\t\t<" + tag + ' ' + "ascending = \"" + projectSpec.isAscending() + "\" " + "sort_column = \"" +
				projectSpec.getSortColumn() + "\" " + "column_order = \"" + writeListAsString(columnOrder) + "\" " +
				"column_widths = \"" + writeListAsString(columnWidths) + "\" " + "/>");
	}

	private static String writeListAsString(List<?> list)
	{
		final StringBuilder out = new StringBuilder();

		for(Iterator<?> iterator = list.iterator(); iterator.hasNext(); )
		{
			final Object element = iterator.next();
			out.append(element);
			if(iterator.hasNext())
			{
				out.append('|');
			}
		}
		return out.toString();
	}

	@SuppressWarnings({"HardCodedStringLiteral"})
	private static void writeMetric(MetricInstance metric, PrintWriter writer)
	{
		writer.println("\t<METRIC className = \"" + metric.getMetricClass() + "\" " + "enabled = \"" +
				metric.isEnabled() + "\" " + "lowerThreshold = \"" + metric.getLowerThreshold() + "\" " +
				"lowerThresholdEnabled = \"" + metric.isLowerThresholdEnabled() + "\" " + "upperThreshold = \"" +
				metric.getUpperThreshold() + "\" " + "upperThresholdEnabled = \"" + metric.isUpperThresholdEnabled() +
				"\" " + "/>");
	}
}
