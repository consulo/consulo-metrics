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
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiJavaPackage;
import com.intellij.psi.util.PsiTreeUtil;
import com.sixrr.metrics.utils.BucketedCount;
import com.sixrr.metrics.utils.ClassUtils;
import com.sixrr.metrics.utils.TestUtils;
import com.sixrr.stockmetrics.utils.LineUtil;

public class SourceLinesOfCodeProductPackageCalculator extends PackageCalculator
{

	private final BucketedCount<PsiJavaPackage> numLinesPerPackage = new BucketedCount<PsiJavaPackage>();
	private final BucketedCount<PsiJavaPackage> numCommentLinesPerPackage = new BucketedCount<PsiJavaPackage>();

	@Override
	public void endMetricsRun()
	{
		final Set<PsiJavaPackage> packages = numLinesPerPackage.getBuckets();
		for(final PsiJavaPackage aPackage : packages)
		{
			final int numLines = numLinesPerPackage.getBucketValue(aPackage);
			final int numCommentLines = numCommentLinesPerPackage.getBucketValue(aPackage);
			postMetric(aPackage, numLines - numCommentLines);
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
			if(!TestUtils.isProduction(file))
			{
				return;
			}
			final PsiJavaPackage aPackage = ClassUtils.findPackage(file);
			if(aPackage == null)
			{
				return;
			}
			final int lineCount = LineUtil.countLines(file);
			numLinesPerPackage.incrementBucketValue(aPackage, lineCount);
		}

		@Override
		public void visitComment(PsiComment comment)
		{
			super.visitComment(comment);
			final PsiFile file = comment.getContainingFile();
			if(!TestUtils.isProduction(file))
			{
				return;
			}
			final PsiClass aClass = PsiTreeUtil.getParentOfType(comment, PsiClass.class);
			final PsiJavaPackage aPackage = ClassUtils.findPackage(aClass);
			if(aPackage == null)
			{
				return;
			}
			final int lineCount = LineUtil.countCommentOnlyLines(comment);
			numCommentLinesPerPackage.incrementBucketValue(aPackage, lineCount);
		}
	}
}
