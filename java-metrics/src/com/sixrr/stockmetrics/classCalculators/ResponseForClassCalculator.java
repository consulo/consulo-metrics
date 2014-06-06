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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.intellij.openapi.progress.ProgressManager;
import com.intellij.psi.JavaRecursiveElementVisitor;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiMethodCallExpression;
import com.intellij.psi.search.searches.OverridingMethodsSearch;
import com.intellij.util.Query;
import com.sixrr.metrics.utils.ClassUtils;

public class ResponseForClassCalculator extends ClassCalculator
{
	private final Set<PsiMethod> methodsCalled = new HashSet<PsiMethod>();

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
			if(ClassUtils.isConcrete(aClass) && !ClassUtils.isAnonymous(aClass))
			{
				methodsCalled.clear();
				final PsiMethod[] methods = aClass.getMethods();
				for(PsiMethod method : methods)
				{
					methodsCalled.add(method);
				}
			}
			super.visitClass(aClass);
			if(ClassUtils.isConcrete(aClass) && !ClassUtils.isAnonymous(aClass))
			{
				final int numMethods = methodsCalled.size();
				postMetric(aClass, numMethods);
			}
		}

		@Override
		public void visitMethodCallExpression(PsiMethodCallExpression expression)
		{
			final PsiMethod referent = expression.resolveMethod();
			if(referent == null)
			{
				return;
			}
			final Runnable runnable = new Runnable()
			{
				@Override
				public void run()
				{
					methodsCalled.add(referent);
					final Query<PsiMethod> query = OverridingMethodsSearch.search(referent);
					final Collection<PsiMethod> overridingMethods = query.findAll();
					methodsCalled.addAll(overridingMethods);
				}
			};
			final ProgressManager progressManager = ProgressManager.getInstance();
			progressManager.runProcess(runnable, null);
		}
	}
}
