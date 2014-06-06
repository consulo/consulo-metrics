/*
 * Copyright 2005-2013 Sixth and Red River Software, Bas Leijdekkers
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

package com.sixrr.stockmetrics.packageCalculators;

import java.util.Set;

import com.intellij.psi.JavaRecursiveElementVisitor;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiJavaPackage;
import com.intellij.psi.PsiModifier;
import com.sixrr.metrics.utils.BucketedCount;
import com.sixrr.metrics.utils.ClassUtils;

public class AbstractnessCalculator extends PackageCalculator
{

	private final BucketedCount<PsiJavaPackage> numAbstractClassesPerPackage = new BucketedCount<PsiJavaPackage>();
	private final BucketedCount<PsiJavaPackage> numClassesPerPackage = new BucketedCount<PsiJavaPackage>();

	@Override
	public void endMetricsRun()
	{
		final Set<PsiJavaPackage> packages = numClassesPerPackage.getBuckets();
		for(final PsiJavaPackage aPackage : packages)
		{
			final int numClasses = numClassesPerPackage.getBucketValue(aPackage);
			final int numAbstractClasses = numAbstractClassesPerPackage.getBucketValue(aPackage);

			postMetric(aPackage, numAbstractClasses, numClasses);
		}
	}

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
			if(ClassUtils.isAnonymous(aClass))
			{
				return;
			}
			final PsiJavaPackage aPackage = ClassUtils.findPackage(aClass);
			if(aPackage == null)
			{
				return;
			}
			if(aClass.isInterface() || aClass.hasModifierProperty(PsiModifier.ABSTRACT))
			{
				numAbstractClassesPerPackage.incrementBucketValue(aPackage);
			}
			numClassesPerPackage.incrementBucketValue(aPackage);
		}
	}
}
