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

package consulo.metrics.profile.instanceHolder;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jetbrains.annotations.NotNull;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeRegistry;
import com.intellij.openapi.fileTypes.PlainTextFileType;
import com.intellij.openapi.util.text.StringUtil;
import com.sixrr.metrics.Metric;
import com.sixrr.metrics.MetricProvider;
import consulo.metrics.impl.defaultMetricsProvider.LineOfCodeFileTypeProviderEP;
import com.sixrr.metrics.impl.defaultMetricsProvider.projectMetrics.LinesOfCodeProjectMetric;
import com.sixrr.metrics.metricModel.MetricInstance;
import com.sixrr.metrics.metricModel.MetricInstanceImpl;
import com.sixrr.stockmetrics.metricModel.BaseMetric;
import consulo.annotations.Immutable;
import consulo.lombok.annotations.ApplicationService;
import consulo.lombok.annotations.Logger;

/**
 * @author VISTALL
 * @since 09.06.14
 */
@ApplicationService
@Logger
public class MetricInstanceHolder
{
	private final Map<String, Metric> myMetricInstances = new HashMap<String, Metric>(200);

	public MetricInstanceHolder()
	{
		loadMetricsFromProviders();
	}

	public void createInstances(Set<MetricInstance> set)
	{
		for(Map.Entry<String, Metric> entry : myMetricInstances.entrySet())
		{
			set.add(new MetricInstanceImpl(entry.getKey(), entry.getValue()));
		}
	}

	@NotNull
	@Immutable
	public Collection<Metric> getMetrics()
	{
		return myMetricInstances.values();
	}

	@NotNull
	public Metric getMetricByClass(@NotNull String className)
	{
		Metric metric = myMetricInstances.get(className);
		return metric == null ? DummyMetric.INSTANCE : metric;
	}

	private void loadMetricsFromProviders()
	{
		for(MetricProvider provider : MetricProvider.EXTENSION_POINT_NAME.getExtensions())
		{
			final List<Class<? extends Metric>> classesForProvider = provider.getMetricClasses();

			for(Class<? extends Metric> aClass : classesForProvider)
			{
				try
				{
					myMetricInstances.put(aClass.getName(), aClass.newInstance());
				}
				catch(InstantiationException e)
				{
					MetricInstanceHolder.LOGGER.error(e);
				}
				catch(IllegalAccessException e)
				{
					MetricInstanceHolder.LOGGER.error(e);
				}
			}
		}

		for(Metric metric : Metric.EP_NAME.getExtensions())
		{
			myMetricInstances.put(metric.getClass().getName(), metric);
		}

		// add some magic
		// in Profiles it stored by ClassName. Line Of code is can be handle without new metrics, need only extends  LinesOfCodeProjectMetric with
		// new name
		for(LineOfCodeFileTypeProviderEP ep : LineOfCodeFileTypeProviderEP.EP_NAME.getExtensions())
		{
			FileType fileTypeByFileName = FileTypeRegistry.getInstance().findFileTypeByName(ep.fileType);

			if(fileTypeByFileName == null)
			{
				MetricInstanceHolder.LOGGER.error("File type is unknown: " + ep.fileType);
				fileTypeByFileName = PlainTextFileType.INSTANCE;
			}
			String className = LinesOfCodeProjectMetric.class.getName() + "$" + ep.fileType;

			LinesOfCodeProjectMetric projectMetric = new LinesOfCodeProjectMetric(fileTypeByFileName);
			projectMetric.setId(BaseMetric.getIdByClassName(StringUtil.getShortName(className)));

			myMetricInstances.put(className, projectMetric);
		}
	}
}
