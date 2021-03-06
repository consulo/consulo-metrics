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
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiJavaPackage;
import com.intellij.psi.javadoc.PsiDocComment;
import com.sixrr.metrics.utils.BucketedCount;
import com.sixrr.metrics.utils.ClassUtils;

public class PercentFieldsJavadocedPackageCalculator extends PackageCalculator
{

	private final BucketedCount<PsiJavaPackage> numJavadocedFieldsPerPackage = new BucketedCount<PsiJavaPackage>();
	private final BucketedCount<PsiJavaPackage> numFieldsPerPackage = new BucketedCount<PsiJavaPackage>();

	@Override
	public void endMetricsRun()
	{
		final Set<PsiJavaPackage> packages = numFieldsPerPackage.getBuckets();
		for(final PsiJavaPackage packageName : packages)
		{
			final int numFields = numFieldsPerPackage.getBucketValue(packageName);
			final int numJavadocedFields = numJavadocedFieldsPerPackage.getBucketValue(packageName);

			postMetric(packageName, numJavadocedFields, numFields);
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
		public void visitField(PsiField field)
		{
			super.visitField(field);
			final PsiClass containingClass = field.getContainingClass();
			if(containingClass == null || ClassUtils.isAnonymous(containingClass))
			{
				return;
			}
			final PsiJavaPackage aPackage = ClassUtils.findPackage(containingClass);
			if(aPackage == null)
			{
				return;
			}
			numFieldsPerPackage.createBucket(aPackage);
			if(field.getFirstChild() instanceof PsiDocComment)
			{
				numJavadocedFieldsPerPackage.incrementBucketValue(aPackage);
			}
			numFieldsPerPackage.incrementBucketValue(aPackage);
		}
	}
}
