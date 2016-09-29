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

package consulo.metrics.java;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiJavaPackage;
import com.intellij.psi.PsiMethod;
import com.sixrr.metrics.Metric;
import com.sixrr.metrics.MetricCategory;
import com.sixrr.metrics.MetricsResultsHolder;
import consulo.metrics.metricModel.DelegateMetricsHolder;
import com.sixrr.metrics.metricModel.MetricsCategoryNameUtil;
import com.sixrr.metrics.metricModel.MetricsResult;
import com.sixrr.metrics.utils.MethodUtils;

/**
 * @author VISTALL
 * @since 06.06.14
 */
public class JavaMetricsRunImpl extends DelegateMetricsHolder implements JavaMetricsResultsHolder
{
	private static final Logger logger = Logger.getInstance(JavaMetricsRunImpl.class);

	public JavaMetricsRunImpl(MetricsResultsHolder resultsHolder)
	{
		super(resultsHolder);
	}

	@Override
	public void postPackageMetric(Metric metric, PsiJavaPackage aPackage, double value)
	{
		final MetricCategory category = metric.getCategory();
		if(category != MetricCategory.Package)
		{
			logger.error("Posting a package metric result from a " + MetricsCategoryNameUtil.getShortNameForCategory(category) + " metric");
		}
		final MetricsResult results = getResultsForCategory(MetricCategory.Package);
		final String packageName;
		if(aPackage != null)
		{
			packageName = aPackage.getQualifiedName();
		}
		else
		{
			packageName = "";
		}
		results.postValue(metric, packageName, value);
	}

	@Override
	public void postClassMetric(Metric metric, PsiClass aClass, double value)
	{
		final MetricCategory category = metric.getCategory();
		if(category != MetricCategory.Class)
		{
			logger.error("Posting a class metric result from a " + MetricsCategoryNameUtil.getShortNameForCategory(category) + " metric");
		}
		final MetricsResult results = getResultsForCategory(MetricCategory.Class);
		final String qualifiedName = aClass.getQualifiedName();
		results.postValue(metric, qualifiedName, value);
		results.setElementForMeasuredObject(qualifiedName, aClass);
	}

	@Override
	public void postInterfaceMetric(Metric metric, PsiClass anInterface, double value)
	{
		final MetricCategory category = metric.getCategory();
		if(category != MetricCategory.Interface)
		{
			logger.error("Posting an interface metric result from a " + MetricsCategoryNameUtil.getShortNameForCategory(category) + " metric");
		}
		final MetricsResult results = getResultsForCategory(MetricCategory.Interface);
		final String qualifiedName = anInterface.getQualifiedName();
		results.postValue(metric, qualifiedName, value);
		results.setElementForMeasuredObject(qualifiedName, anInterface);
	}

	@Override
	public void postMethodMetric(Metric metric, PsiMethod method, double value)
	{
		final MetricCategory category = metric.getCategory();
		if(category != MetricCategory.Method)
		{
			logger.error("Posting a method metric result from a " + MetricsCategoryNameUtil.getShortNameForCategory(category) + " metric");
		}
		final MetricsResult results = getResultsForCategory(MetricCategory.Method);
		final String signature = MethodUtils.calculateSignature(method);
		results.postValue(metric, signature, value);
		results.setElementForMeasuredObject(signature, method);
	}

	@Override
	public void postPackageMetric(Metric metric, PsiJavaPackage aPackage, double numerator, double denominator)
	{
		final MetricCategory category = metric.getCategory();
		if(category != MetricCategory.Package)
		{
			logger.error("Posting a package metric result from a " + MetricsCategoryNameUtil.getShortNameForCategory(category) + " metric");
		}
		final MetricsResult results = getResultsForCategory(MetricCategory.Package);
		results.postValue(metric, aPackage.getQualifiedName(), numerator, denominator);
	}

	@Override
	public void postClassMetric(Metric metric, PsiClass aClass, double numerator, double denominator)
	{
		final MetricCategory category = metric.getCategory();
		if(category != MetricCategory.Class)
		{
			logger.error("Posting a class metric result from a " + MetricsCategoryNameUtil.getShortNameForCategory(category) + " metric");
		}
		final MetricsResult results = getResultsForCategory(MetricCategory.Class);
		results.postValue(metric, aClass.getQualifiedName(), numerator, denominator);
	}

	@Override
	public void postInterfaceMetric(Metric metric, PsiClass anInterface, double numerator, double denominator)
	{
		final MetricCategory category = metric.getCategory();
		if(category != MetricCategory.Interface)
		{
			logger.error("Posting an interface metric result from a " + MetricsCategoryNameUtil.getShortNameForCategory(category) + " metric");
		}
		final MetricsResult results = getResultsForCategory(MetricCategory.Interface);
		results.postValue(metric, anInterface.getQualifiedName(), numerator, denominator);
	}

	@Override
	public void postMethodMetric(Metric metric, PsiMethod method, double numerator, double denominator)
	{
		final MetricCategory category = metric.getCategory();
		if(category != MetricCategory.Method)
		{
			logger.error("Posting a method metric result from a " + MetricsCategoryNameUtil.getShortNameForCategory(category) + " metric");
		}
		final MetricsResult results = getResultsForCategory(MetricCategory.Method);
		final String signature = MethodUtils.calculateSignature(method);
		results.postValue(metric, signature, numerator, denominator);
		results.setElementForMeasuredObject(signature, method);
	}

}
