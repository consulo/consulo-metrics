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
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiJavaPackage;
import com.intellij.psi.util.PsiTreeUtil;
import com.sixrr.metrics.utils.BucketedCount;
import com.sixrr.metrics.utils.ClassUtils;
import com.sixrr.stockmetrics.utils.TodoUtil;

public class TodoCommentCountRecursivePackageCalculator extends PackageCalculator
{

	private final BucketedCount<PsiJavaPackage> numTodoCommentsPerPackage = new BucketedCount<PsiJavaPackage>();

	@Override
	public void endMetricsRun()
	{
		final Set<PsiJavaPackage> packages = numTodoCommentsPerPackage.getBuckets();
		for(final PsiJavaPackage aPackage : packages)
		{
			final int numCommentLines = numTodoCommentsPerPackage.getBucketValue(aPackage);

			postMetric(aPackage, (double) numCommentLines);
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
			final PsiJavaPackage[] packages = ClassUtils.calculatePackagesRecursive(file);
			for(PsiJavaPackage aPackage : packages)
			{
				numTodoCommentsPerPackage.createBucket(aPackage);
			}
		}

		@Override
		public void visitComment(PsiComment comment)
		{
			super.visitComment(comment);
			final PsiClass aClass = PsiTreeUtil.getParentOfType(comment, PsiClass.class);
			if(TodoUtil.isTodoComment(comment))
			{
				final PsiJavaPackage[] packages = ClassUtils.calculatePackagesRecursive(aClass);
				for(PsiJavaPackage aPackage : packages)
				{
					numTodoCommentsPerPackage.incrementBucketValue(aPackage, 1);
				}
			}
		}
	}
}
