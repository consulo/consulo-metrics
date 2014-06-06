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

package org.mustbe.consulo.metrics.java;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiJavaPackage;
import com.intellij.psi.PsiMethod;
import com.sixrr.metrics.Metric;
import com.sixrr.metrics.MetricsResultsHolder;

/**
 * @author VISTALL
 * @since 06.06.14
 */
public interface JavaMetricsResultsHolder extends MetricsResultsHolder
{
	/**
	 * Post a ratio value for a package metric.
	 *
	 * @param metric      the metric to post the value for.   It should have a category of MetricCategory.Package.
	 * @param aPackage    the package for which the metric value is calculated.
	 * @param numerator   The numerator of the value to post.  Should be an integer.
	 * @param denominator The denominator of the value to post.  Should be a positive integer.
	 */
	void postPackageMetric(Metric metric, PsiJavaPackage aPackage, double numerator, double denominator);

	/**
	 * Post a ratio value for a class metric.
	 *
	 * @param metric      the metric to post the value for.   It should have a category of MetricCategory.Class.
	 * @param aClass      the class for which the metric value is calculated.
	 * @param numerator   The numerator of the value to post.  Should be an integer.
	 * @param denominator The denominator of the value to post.  Should be a positive integer.
	 */
	void postClassMetric(Metric metric, PsiClass aClass, double numerator, double denominator);

	/**
	 * Post a ratio value for a interface metric.
	 *
	 * @param metric      the metric to post the value for.   It should have a category of MetricCategory.Interface.
	 * @param anInterface the interface for which the metric value is calculated.
	 * @param numerator   The numerator of the value to post.  Should be an integer.
	 * @param denominator The denominator of the value to post.  Should be a positive integer.
	 */
	void postInterfaceMetric(Metric metric, PsiClass anInterface, double numerator, double denominator);

	/**
	 * Post a ratio value for a method metric.
	 *
	 * @param metric      the metric to post the value for.   It should have a category of MetricCategory.Method.
	 * @param method      the method for which the metric value is calculated.
	 * @param numerator   The numerator of the value to post.  Should be an integer.
	 * @param denominator The denominator of the value to post.  Should be a positive integer.
	 */
	void postMethodMetric(Metric metric, PsiMethod method, double numerator, double denominator);

	/**
	 * Post a simple value for a package metric.
	 *
	 * @param metric   the metric to post the value for.   It should have a category of MetricCategory.Package.
	 * @param aPackage the package for which the metric value is calculated.
	 * @param value    the value to post.
	 */
	void postPackageMetric(Metric metric, PsiJavaPackage aPackage, double value);

	/**
	 * Post a simple value for a class metric.
	 *
	 * @param metric the metric to post the value for.   It should have a category of MetricCategory.Class.
	 * @param aClass the class for which the metric value is calculated.
	 * @param value  the value to post.
	 */
	void postClassMetric(Metric metric, PsiClass aClass, double value);

	/**
	 * Post a simple value for an interface metric.
	 *
	 * @param metric      the metric to post the value for.   It should have a category of MetricCategory.Interface.
	 * @param anInterface the interface for which the metric value is calculated.
	 * @param value       the value to post.
	 */
	void postInterfaceMetric(Metric metric, PsiClass anInterface, double value);

	/**
	 * Post a simple value for a method metric.
	 *
	 * @param metric the metric to post the value for.   It should have a category of MetricCategory.Method.
	 * @param method the method for which the metric value is calculated.
	 * @param value  the value to post.
	 */
	void postMethodMetric(Metric metric, PsiMethod method, double value);
}
