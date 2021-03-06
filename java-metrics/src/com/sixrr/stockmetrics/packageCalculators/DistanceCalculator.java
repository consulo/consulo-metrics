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
import com.sixrr.stockmetrics.dependency.DependencyMap;
import com.sixrr.stockmetrics.dependency.DependentsMap;

public class DistanceCalculator extends PackageCalculator
{

	private final BucketedCount<PsiJavaPackage> numClassesPerPackage = new BucketedCount<PsiJavaPackage>();
	private final BucketedCount<PsiJavaPackage> numAbstractClassesPerPackage = new BucketedCount<PsiJavaPackage>();
	private final BucketedCount<PsiJavaPackage> numExternalDependentsPerPackage = new BucketedCount<PsiJavaPackage>();
	private final BucketedCount<PsiJavaPackage> numExternalDependenciesPerPackage = new BucketedCount<PsiJavaPackage>();

	@Override
	public void endMetricsRun()
	{
		final Set<PsiJavaPackage> packages = numExternalDependentsPerPackage.getBuckets();
		for(final PsiJavaPackage aPackage : packages)
		{
			final double numClasses = (double) numClassesPerPackage.getBucketValue(aPackage);
			final double numAbstractClasses = (double) numAbstractClassesPerPackage.getBucketValue(aPackage);
			final double numExternalDependents = (double) numExternalDependentsPerPackage.getBucketValue(aPackage);
			final double numExternalDependencies = (double) numExternalDependenciesPerPackage.getBucketValue(aPackage);
			final double instability = numExternalDependencies / (numExternalDependencies + numExternalDependents);
			final double abstractness = numAbstractClasses / numClasses;
			final double distance = Math.abs(1.0 - instability - abstractness);
			postMetric(aPackage, distance);
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
			final PsiJavaPackage currentPackage = ClassUtils.findPackage(aClass);
			if(currentPackage == null)
			{
				return;
			}
			if(aClass.isInterface() || aClass.hasModifierProperty(PsiModifier.ABSTRACT))
			{
				numAbstractClassesPerPackage.incrementBucketValue(currentPackage);
			}
			numClassesPerPackage.incrementBucketValue(currentPackage);
			numExternalDependentsPerPackage.createBucket(currentPackage);
			final DependentsMap dependentsMap = getDependentsMap();
			final Set<PsiJavaPackage> packageDependents = dependentsMap.calculatePackageDependents(aClass);
			for(final PsiJavaPackage referencingPackage : packageDependents)
			{
				final int strength = dependentsMap.getStrengthForPackageDependent(aClass, referencingPackage);
				numExternalDependentsPerPackage.incrementBucketValue(currentPackage, strength);
			}
			numExternalDependenciesPerPackage.createBucket(currentPackage);
			final DependencyMap dependencyMap = getDependencyMap();
			final Set<PsiJavaPackage> packageDependencies = dependencyMap.calculatePackageDependencies(aClass);
			for(final PsiJavaPackage referencedPackage : packageDependencies)
			{
				final int strength = dependencyMap.getStrengthForPackageDependency(aClass, referencedPackage);
				numExternalDependenciesPerPackage.incrementBucketValue(currentPackage, strength);
			}
		}
	}
}
