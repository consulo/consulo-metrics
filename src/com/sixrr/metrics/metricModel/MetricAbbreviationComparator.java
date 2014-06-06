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

package com.sixrr.metrics.metricModel;

import java.util.Comparator;

import com.sixrr.metrics.Metric;

public class MetricAbbreviationComparator implements Comparator<Metric>
{
	@Override
	public int compare(Metric o1, Metric o2)
	{
		final String abbrev1 = o1.getAbbreviation();
		final String upperAbbrev1 = abbrev1.toUpperCase();
		final String abbrev2 = o2.getAbbreviation();
		final String upperAbbrev2 = abbrev2.toUpperCase();
		final int caseInsensitiveCompare = upperAbbrev1.compareTo(upperAbbrev2);
		if(caseInsensitiveCompare != 0)
		{
			return caseInsensitiveCompare;
		}
		return abbrev1.compareTo(abbrev2);
	}
}
