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

package com.sixrr.stockmetrics.classMetrics;

import org.jetbrains.annotations.Nullable;
import com.sixrr.metrics.MetricType;
import com.sixrr.stockmetrics.i18n.HelpURLs;
import com.sixrr.stockmetrics.i18n.StockMetricsBundle;

public class LevelOrderClassMetric extends ClassMetric
{
	@Override
	public String getDisplayName()
	{
		return StockMetricsBundle.message("level.order.display.name");
	}

	@Override
	public String getAbbreviation()
	{
		return StockMetricsBundle.message("level.order.abbreviation");
	}

	@Override
	public MetricType getType()
	{
		return MetricType.Score;
	}

	@Override
	@Nullable
	public String getHelpURL()
	{
		return HelpURLs.LAYERING_URL;
	}

	@Override
	@Nullable
	public String getHelpDisplayString()
	{
		return HelpURLs.LAYERING_DISPLAY_STRING;
	}

	@Override
	public boolean requiresDependents()
	{
		return true;
	}
}
