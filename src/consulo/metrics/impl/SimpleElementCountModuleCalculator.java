/*
 * Copyright 2013-2014 must-be.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package consulo.metrics.impl;

import java.util.Set;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.psi.PsiElement;
import com.sixrr.metrics.utils.BucketedCount;

/**
 * @author VISTALL
 * @since 09.06.14
 */
public abstract class SimpleElementCountModuleCalculator extends SimpleModuleCalculator
{
	protected final BucketedCount<Module> elementsCountPerModule = new BucketedCount<Module>();

	@Override
	public void endMetricsRun()
	{
		final Set<Module> modules = elementsCountPerModule.getBuckets();
		for(final Module module : modules)
		{
			final int numCommentLines = elementsCountPerModule.getBucketValue(module);

			postMetric(module, numCommentLines);
		}
	}

	protected void incrementElementCount(PsiElement element, int count)
	{
		Module module = ModuleUtilCore.findModuleForPsiElement(element);
		if(module == null)
		{
			return;
		}
		elementsCountPerModule.incrementBucketValue(module, count);
	}
}
