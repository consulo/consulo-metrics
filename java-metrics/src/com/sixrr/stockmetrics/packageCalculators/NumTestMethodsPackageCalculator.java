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
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiJavaPackage;
import com.intellij.psi.PsiMethod;
import com.sixrr.metrics.utils.BucketedCount;
import com.sixrr.metrics.utils.ClassUtils;
import com.sixrr.metrics.utils.TestUtils;

public class NumTestMethodsPackageCalculator extends PackageCalculator
{

	private final BucketedCount<PsiJavaPackage> numTestMethodsPerPackages = new BucketedCount<PsiJavaPackage>();

	@Override
	public void endMetricsRun()
	{
		final Set<PsiJavaPackage> packages = numTestMethodsPerPackages.getBuckets();
		for(final PsiJavaPackage aPackage : packages)
		{
			final int numTestMethods = numTestMethodsPerPackages.getBucketValue(aPackage);

			postMetric(aPackage, numTestMethods);
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
		public void visitJavaFile(PsiJavaFile file)
		{
			super.visitJavaFile(file);
			final PsiJavaPackage aPackage = ClassUtils.findPackage(file);
			if(aPackage == null)
			{
				return;
			}
			numTestMethodsPerPackages.createBucket(aPackage);
		}

		@Override
		public void visitMethod(PsiMethod method)
		{
			super.visitMethod(method);
			final PsiClass aClass = method.getContainingClass();
			if(!TestUtils.isJUnitTestMethod(method))
			{
				return;
			}
			final PsiJavaPackage aPackage = ClassUtils.findPackage(aClass);
			if(aPackage == null)
			{
				return;
			}
			numTestMethodsPerPackages.incrementBucketValue(aPackage, 1);
		}
	}
}
