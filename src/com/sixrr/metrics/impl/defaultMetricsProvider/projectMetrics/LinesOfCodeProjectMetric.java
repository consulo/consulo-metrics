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

package com.sixrr.metrics.impl.defaultMetricsProvider.projectMetrics;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import com.sixrr.metrics.MetricCalculator;
import com.sixrr.metrics.MetricType;
import com.sixrr.metrics.impl.SimpleElementCountProjectCalculator;
import com.sixrr.metrics.impl.defaultMetricsProvider.projectCalculators.LineUtil;
import com.sixrr.metrics.utils.MetricsReloadedBundle;
import com.sixrr.stockmetrics.projectMetrics.ProjectMetric;

public class LinesOfCodeProjectMetric extends ProjectMetric
{
	private FileType myFileType;

	public LinesOfCodeProjectMetric(FileType fileType)
	{
		myFileType = fileType;
	}

	@Nullable
	@Override
	public MetricCalculator createCalculator()
	{
		return new SimpleElementCountProjectCalculator()
		{
			private PsiElementVisitor myVisitor = new PsiElementVisitor()
			{
				@Override
				public void visitFile(PsiFile file)
				{
					if(file.getFileType() != myFileType)
					{
						return;
					}
					myElements += LineUtil.countLines(file);
				}
			};

			@NotNull
			@Override
			protected PsiElementVisitor createVisitor()
			{
				return myVisitor;
			}
		};
	}

	@Override
	public String getDisplayName()
	{
		return MetricsReloadedBundle.message("lines.of.code.display.name", myFileType.getDescription());
	}

	@Override
	public String getAbbreviation()
	{
		return MetricsReloadedBundle.message("lines.of.code.abbreviation", myFileType.getName());
	}

	@Override
	public MetricType getType()
	{
		return MetricType.Count;
	}
}
