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
import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.sixrr.metrics.Metric;
import com.sixrr.metrics.metricModel.MetricInstance;

public interface MetricsProfile extends Cloneable
{
	void copyFrom(@NotNull List<MetricInstance> metrics);

	void setName(String newProfileName);

	String getName();

	@NotNull
	Set<MetricInstance> getMetrics();

	/**
	 * Return metric instances with order, and without DummyMetric
	 * @return sorted list
	 */
	@NotNull
	List<MetricInstance> getSortedMetrics(boolean onlyEnabled);

	@Nullable
	MetricInstance getMetricForClass(Class<? extends Metric> aClass);

	@Nullable
	MetricInstance getMetricByID(String metricName);

	void writeToFile(File profileFile) throws IOException;

	MetricDisplaySpecification getDisplaySpecification();

	void setBuiltIn(boolean builtIn);

	boolean isBuiltIn();
}
