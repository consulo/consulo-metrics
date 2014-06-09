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

public class CommentLinesOfCodePackageCalculator extends PackageCalculator
{

	private final BucketedCount<PsiJavaPackage> numCommentLinesPerPackage = new BucketedCount<PsiJavaPackage>();

	@Override
	public void endMetricsRun()
	{
		final Set<PsiJavaPackage> packages = numCommentLinesPerPackage.getBuckets();
		for(final PsiJavaPackage aPackage : packages)
		{
			final int numCommentLines = numCommentLinesPerPackage.getBucketValue(aPackage);

			postMetric(aPackage, numCommentLines);
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
			final PsiJavaPackage aPackage = ClassUtils.findPackage(file);
			if(aPackage == null)
			{
				return;
			}
			numCommentLinesPerPackage.createBucket(aPackage);
		}

		@Override
		public void visitComment(PsiComment comment)
		{
			final PsiClass aClass = PsiTreeUtil.getParentOfType(comment, PsiClass.class);
			final PsiJavaPackage aPackage = ClassUtils.findPackage(aClass);
			if(aPackage == null)
			{
				return;
			}
			final int lineCount = com.sixrr.stockmetrics.utils.LineUtil.countLines(comment);
			numCommentLinesPerPackage.incrementBucketValue(aPackage, lineCount);
		}
	}
}
