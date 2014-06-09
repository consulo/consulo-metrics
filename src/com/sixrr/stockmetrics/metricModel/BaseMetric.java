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

package com.sixrr.stockmetrics.metricModel;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.sixrr.metrics.Metric;
import com.sixrr.metrics.MetricCalculator;

public abstract class BaseMetric implements Cloneable, Metric
{
	public static String getIdByClassName(@NotNull Class<? extends Metric> clazz)
	{
		final String className = clazz.getSimpleName();
		@NonNls final int endIndex = className.length() - "Metric".length();
		return className.substring(0, endIndex);
	}

	private final String myId;

	protected BaseMetric()
	{
		super();
		myId = getIdByClassName(getClass());
	}

	@Override
	public Metric clone() throws CloneNotSupportedException
	{
		return (Metric) super.clone();
	}

	@Override
	public boolean equals(Object obj)
	{
		if(this == obj)
		{
			return true;
		}
		if(!(obj instanceof BaseMetric))
		{
			return false;
		}

		final BaseMetric baseMetric = (BaseMetric) obj;

		return myId.equals(baseMetric.getID());
	}

	@Override
	public int hashCode()
	{
		return myId.hashCode();
	}

	@Override
	public String getID()
	{
		return myId;
	}

	@Override
	@Nullable
	public MetricCalculator createCalculator()
	{
		final String metricClassName = getClass().getName();
		//noinspection HardCodedStringLiteral
		final String calculatorClassName = metricClassName.replaceAll("Metric", "Calculator");
		final MetricCalculator calculator;
		try
		{
			final Class<?> calculatorClass = Class.forName(calculatorClassName);
			calculator = (MetricCalculator) calculatorClass.newInstance();
		}
		catch(ClassNotFoundException e)
		{
			return null;
		}
		catch(InstantiationException e)
		{
			return null;
		}
		catch(IllegalAccessException e)
		{
			return null;
		}
		return calculator;
	}

	@Override
	@Nullable
	public String getHelpURL()
	{
		return null;
	}

	@Override
	@Nullable
	public String getHelpDisplayString()
	{
		return null;
	}

	public boolean requiresDependents()
	{
		return false;
	}
}
