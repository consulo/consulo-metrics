/*
 * Copyright 2005, Sixth and Red River Software
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

package com.sixrr.metrics.export;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import org.jetbrains.annotations.NonNls;
import com.intellij.analysis.AnalysisScope;
import com.sixrr.metrics.Metric;
import com.sixrr.metrics.MetricCategory;
import com.sixrr.metrics.metricModel.MetricsResult;
import com.sixrr.metrics.metricModel.MetricsRun;

public class XMLExporter implements Exporter
{
	private final MetricsRun run;

	public XMLExporter(MetricsRun run)
	{
		super();
		this.run = run;
	}

	public void export(String fileName) throws IOException
	{
		FileOutputStream outputStream = null;
		@NonNls PrintWriter writer = null;
		try
		{
			outputStream = new FileOutputStream(fileName);
			writer = new PrintWriter(outputStream);
			writer.println("<METRICS " + " profile = \"" + run.getProfileName() + '\"' + " timestamp = \"" +
					run.getTimestamp() + '\"' + '>');
			writeContext(run.getContext());
			final MetricCategory[] categories = MetricCategory.values();
			for(MetricCategory category : categories)
			{
				writeResultsForCategory(category, writer);
			}
			writer.println("</METRICS>");
		}
		finally
		{
			if(writer != null)
			{
				writer.close();
			}
			if(outputStream != null)
			{
				outputStream.close();
			}
		}
	}

	private void writeContext(AnalysisScope context)
	{
	}

	private void writeResultsForCategory(MetricCategory category, PrintWriter writer)
	{
		final MetricsResult results = run.getResultsForCategory(category);
		final Metric[] metrics = results.getMetrics();
		for(final Metric metric : metrics)
		{
			writeResultsForMetric(category, metric, results, writer);
		}
	}

	private static void writeResultsForMetric(
			MetricCategory category, Metric metric, MetricsResult results, @NonNls PrintWriter writer)
	{
		final String[] measuredObjects = results.getMeasuredObjects();
		writer.println("\t\t<METRIC" + " category = \"" + category.name() + '\"' + " name= \"" +
				metric.getDisplayName() + '\"' + " abbreviation= \"" + metric.getAbbreviation() + '\"' + '>');
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
			writer.println("\t\t\t<VALUE measured = \"" + escape(measuredObject) + "\" value = \"" + value + "\"/>");
		}
	}

	private static String escape(String measuredObject)
	{
		@NonNls final StringBuilder sb = new StringBuilder(measuredObject.length());

		for(int idx = 0; idx < measuredObject.length(); idx++)
		{
			final char c = measuredObject.charAt(idx);

			switch(c)
			{
				case '<':
					sb.append("&lt;");
					break;
				case '>':
					sb.append("&gt;");
					break;
				default:
					sb.append(c);
			}
		}

		return sb.toString();
	}
}
