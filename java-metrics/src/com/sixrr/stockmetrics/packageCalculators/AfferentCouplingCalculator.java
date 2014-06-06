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
import com.sixrr.metrics.utils.BucketedCount;
import com.sixrr.metrics.utils.ClassUtils;
import com.sixrr.stockmetrics.dependency.DependentsMap;

public class AfferentCouplingCalculator extends PackageCalculator
{

	private final BucketedCount<PsiJavaPackage> numExternalDependentsPerPackage = new BucketedCount<PsiJavaPackage>();

	@Override
	public void endMetricsRun()
	{
		final Set<PsiJavaPackage> packages = numExternalDependentsPerPackage.getBuckets();
		for(final PsiJavaPackage aPackage : packages)
		{
			final int numExternalDependents = numExternalDependentsPerPackage.getBucketValue(aPackage);
			postMetric(aPackage, numExternalDependents);
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
			final PsiJavaPackage referencedPackage = ClassUtils.findPackage(aClass);
			if(referencedPackage == null)
			{
				return;
			}
			numExternalDependentsPerPackage.createBucket(referencedPackage);
			final DependentsMap dependentsMap = getDependentsMap();
			final Set<PsiJavaPackage> packageDependents = dependentsMap.calculatePackageDependents(aClass);
			for(final PsiJavaPackage referencingPackage : packageDependents)
			{
				final int strength = dependentsMap.getStrengthForPackageDependent(aClass, referencingPackage);
				numExternalDependentsPerPackage.incrementBucketValue(referencedPackage, strength);
			}
		}
	}
}
