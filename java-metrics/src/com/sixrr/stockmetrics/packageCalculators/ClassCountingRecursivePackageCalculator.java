/*
 * Copyright 2005-2013, Bas Leijdekkers, Sixth and Red River Software
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
import com.intellij.psi.PsiEnumConstantInitializer;
import com.intellij.psi.PsiTypeParameter;
import com.sixrr.metrics.utils.BucketedCount;
import com.sixrr.metrics.utils.ClassUtils;

abstract class ClassCountingRecursivePackageCalculator extends PackageCalculator
{

	private final BucketedCount<PsiPackage> numClassesPerPackage = new BucketedCount<PsiPackage>();

	@Override
	public void endMetricsRun()
	{
		final Set<PsiPackage> packages = numClassesPerPackage.getBuckets();
		for(final PsiPackage aPackage : packages)
		{
			final int numClasses = numClassesPerPackage.getBucketValue(aPackage);
			postMetric(aPackage, (double) numClasses);
		}
	}

	protected abstract boolean satisfies(PsiClass aClass);

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
			if(aClass instanceof PsiTypeParameter || aClass instanceof PsiEnumConstantInitializer)
			{
				return;
			}
			final boolean satisfied = satisfies(aClass);
			final PsiPackage[] packages = ClassUtils.calculatePackagesRecursive(aClass);
			for(final PsiPackage aPackage : packages)
			{
				numClassesPerPackage.createBucket(aPackage);
				if(satisfied)
				{
					numClassesPerPackage.incrementBucketValue(aPackage);
				}
			}
		}
	}
}
