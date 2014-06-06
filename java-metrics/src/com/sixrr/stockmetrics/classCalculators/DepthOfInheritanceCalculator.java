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
import com.intellij.psi.PsiElementVisitor;

public class DepthOfInheritanceCalculator extends ClassCalculator
{

	protected PsiElementVisitor createVisitor()
	{
		return new Visitor();
	}

	private class Visitor extends JavaRecursiveElementVisitor
	{

		public void visitClass(PsiClass aClass)
		{
			super.visitClass(aClass);
			if(isConcreteClass(aClass))
			{
				final int depth = getInheritanceDepth(aClass);
				postMetric(aClass, depth);
			}
		}
	}

	private static int getInheritanceDepth(PsiClass aClass)
	{
		final PsiClass superClass = aClass.getSuperClass();
		if(superClass == null)
		{
			return 0;
		}
		else
		{
			return getInheritanceDepth(superClass) + 1;
		}
	}
}
