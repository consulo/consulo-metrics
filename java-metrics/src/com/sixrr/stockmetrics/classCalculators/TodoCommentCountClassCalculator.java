/*
 * Copyright 2005, Sixth and Red River Software
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

package com.sixrr.stockmetrics.classCalculators;

import com.intellij.psi.JavaRecursiveElementVisitor;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElementVisitor;
import com.sixrr.metrics.utils.ClassUtils;
import com.sixrr.stockmetrics.utils.TodoUtil;

public class TodoCommentCountClassCalculator extends ClassCalculator
{
	private int elementCount = 0;

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
			final int prevElementCount = elementCount;
			if(!ClassUtils.isAnonymous(aClass))
			{
				elementCount = 0;
			}
			super.visitClass(aClass);
			if(!ClassUtils.isAnonymous(aClass))
			{
				if(!aClass.isInterface())
				{
					postMetric(aClass, elementCount);
				}
				elementCount = prevElementCount;
			}
		}

		@Override
		public void visitComment(PsiComment comment)
		{
			super.visitComment(comment);
			if(TodoUtil.isTodoComment(comment))
			{

				elementCount += com.sixrr.stockmetrics.utils.LineUtil.countLines(comment);
			}
		}
	}
}
