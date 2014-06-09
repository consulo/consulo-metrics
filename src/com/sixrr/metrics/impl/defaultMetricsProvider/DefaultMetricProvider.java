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

package com.sixrr.metrics.impl.defaultMetricsProvider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import com.sixrr.metrics.Metric;
import com.sixrr.metrics.MetricProvider;
import com.sixrr.metrics.PrebuiltMetricProfile;
import com.sixrr.metrics.impl.defaultMetricsProvider.projectMetrics.LinesOfCodeProjectMetric;
import com.sixrr.metrics.profile.instanceHolder.MetricInstanceHolder;
import com.sixrr.metrics.utils.MetricsReloadedBundle;

/**
 * @author VISTALL
 * @since 09.06.14
 */
public class DefaultMetricProvider implements MetricProvider
{
	@NotNull
	@Override
	public List<Class<? extends Metric>> getMetricClasses()
	{
		return Collections.emptyList();
	}

	@NotNull
	@Override
	public List<PrebuiltMetricProfile> getPrebuiltProfiles()
	{
		List<PrebuiltMetricProfile> prebuiltMetricProfiles = new ArrayList<PrebuiltMetricProfile>();
		prebuiltMetricProfiles.add(createCodeSizeProfile());
		return prebuiltMetricProfiles;
	}

	private PrebuiltMetricProfile createCodeSizeProfile()
	{
		final PrebuiltMetricProfile profile = new PrebuiltMetricProfile(MetricsReloadedBundle.message("lines.of.code.metrics.profile.name"));

		for(Metric metric : MetricInstanceHolder.getInstance().getMetrics())
		{
			if(metric instanceof LinesOfCodeProjectMetric)
			{
				profile.addMetric(metric.getID());
			}
		}
		/*profile.addMetric("LinesOfCodeProject");
		profile.addMetric("LinesOfCodeModule");
		profile.addMetric("LinesOfCodePackage");
		profile.addMetric("LinesOfCodeRecursivePackage");
		profile.addMetric("LinesOfProductCodeProject");
		profile.addMetric("LinesOfProductCodeModule");
		profile.addMetric("LinesOfProductCodePackage");
		profile.addMetric("LinesOfProductCodeRecursivePackage");
		profile.addMetric("LinesOfTestCodeProject");
		profile.addMetric("LinesOfTestCodeModule");
		profile.addMetric("LinesOfTestCodePackage");
		profile.addMetric("LinesOfTestCodeRecursivePackage");
		profile.addMetric("LinesOfHTMLProject");
		profile.addMetric("LinesOfHTMLModule");
		profile.addMetric("LinesOfXMLProject");
		profile.addMetric("LinesOfXMLModule");  */
		return profile;
	}
}
