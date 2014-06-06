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
import com.intellij.psi.PsiMethod;
import com.intellij.psi.javadoc.PsiDocComment;
import com.sixrr.metrics.utils.BucketedCount;
import com.sixrr.metrics.utils.ClassUtils;

public class PercentMethodsJavadocedRecursivePackageCalculator extends PackageCalculator
{

	private final BucketedCount<PsiJavaPackage> numJavadocedMethodsPerPackage = new BucketedCount<PsiJavaPackage>();
	private final BucketedCount<PsiJavaPackage> numMethodsPerPackage = new BucketedCount<PsiJavaPackage>();

	@Override
	public void endMetricsRun()
	{
		final Set<PsiJavaPackage> packages = numMethodsPerPackage.getBuckets();
		for(final PsiJavaPackage packageName : packages)
		{
			final int numMethods = numMethodsPerPackage.getBucketValue(packageName);
			final int numJavadocedMethods = numJavadocedMethodsPerPackage.getBucketValue(packageName);

			postMetric(packageName, numJavadocedMethods, numMethods);
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
		public void visitMethod(PsiMethod method)
		{
			super.visitMethod(method);
			final PsiClass containingClass = method.getContainingClass();
			if(containingClass == null || ClassUtils.isAnonymous(containingClass))
			{
				return;
			}
			final PsiJavaPackage[] packages = ClassUtils.calculatePackagesRecursive(containingClass);

			for(final PsiJavaPackage aPackage : packages)
			{
				numMethodsPerPackage.createBucket(aPackage);

				if(method.getFirstChild() instanceof PsiDocComment)
				{
					numJavadocedMethodsPerPackage.incrementBucketValue(aPackage);
				}
				numMethodsPerPackage.incrementBucketValue(aPackage);
			}
		}
	}
}
