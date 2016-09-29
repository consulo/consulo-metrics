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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.psi.PsiElementVisitor;
import com.sixrr.metrics.Metric;
import com.sixrr.metrics.MetricCalculator;
import com.sixrr.metrics.MetricCategory;
import com.sixrr.metrics.MetricType;
import com.sixrr.metrics.impl.SimpleMetricCalculator;
import com.sixrr.stockmetrics.metricModel.BaseMetric;

/**
 * @author VISTALL
 * @since 09.06.14
 */
public class DummyMetric implements Metric
{
	public static final DummyMetric INSTANCE = new DummyMetric();

	@Override
	public String getID()
	{
		return BaseMetric.getIdByClassName(getClass());
	}

	@Override
	public String getDisplayName()
	{
		return null;
	}

	@Override
	public String getAbbreviation()
	{
		return null;
	}

	@Override
	public MetricCategory getCategory()
	{
		return null;
	}

	@Override
	public MetricType getType()
	{
		return null;
	}

	@Nullable
	@Override
	public String getHelpURL()
	{
		return null;
	}

	@Nullable
	@Override
	public String getHelpDisplayString()
	{
		return null;
	}

	@Override
	public MetricCalculator createCalculator()
	{
		return new SimpleMetricCalculator()
		{
			@NotNull
			@Override
			protected PsiElementVisitor createVisitor()
			{
				return new PsiElementVisitor()
				{
				};
			}
		};
	}
}
