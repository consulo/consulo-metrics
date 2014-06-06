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

package com.sixrr.stockmetrics.classCalculators;

import com.intellij.psi.JavaRecursiveElementVisitor;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiTypeParameter;
import com.intellij.psi.PsiTypeParameterList;
import com.sixrr.metrics.utils.ClassUtils;

public class NumTypeParametersClassCalculator extends ClassCalculator
{
	@Override
	protected PsiElementVisitor createVisitor()
	{
		return new Visitor();
	}

	private class Visitor extends JavaRecursiveElementVisitor
	{

		@Override
		public void visitClass(PsiClass aClass)
		{
			super.visitClass(aClass);
			if(ClassUtils.isAnonymous(aClass) || aClass.isInterface())
			{
				return;
			}
			final PsiTypeParameterList typeParams = aClass.getTypeParameterList();
			if(typeParams == null)
			{
				postMetric(aClass, 0);
			}
			else
			{
				final PsiTypeParameter[] parameters = typeParams.getTypeParameters();
				if(parameters == null)
				{
					postMetric(aClass, 0);
				}
				else
				{
					postMetric(aClass, parameters.length);
				}
			}
		}
	}
}
