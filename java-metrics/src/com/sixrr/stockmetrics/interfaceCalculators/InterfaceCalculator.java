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

package com.sixrr.stockmetrics.interfaceCalculators;

import com.intellij.psi.PsiClass;
import com.sixrr.stockmetrics.execution.BaseMetricsCalculator;

abstract class InterfaceCalculator extends BaseMetricsCalculator
{
	void postMetric(PsiClass aClass, int value)
	{
		resultsHolder.postInterfaceMetric(metric, aClass, (double) value);
	}

	void postMetric(PsiClass aClass, double value)
	{
		resultsHolder.postInterfaceMetric(metric, aClass, value);
	}

	void postMetric(PsiClass aClass, int numerator, int denominator)
	{
		resultsHolder.postInterfaceMetric(metric, aClass, (double) numerator, (double) denominator);
	}

	protected static boolean isInterface(PsiClass aClass)
	{
		return aClass.isInterface() && !aClass.isAnnotationType();
	}
}
