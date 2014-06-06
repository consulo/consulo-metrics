/*
 * Copyright 2005-2011 Sixth and Red River Software, Bas Leijdekkers
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

package com.sixrr.metrics.metricModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import com.intellij.analysis.AnalysisScope;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import com.sixrr.metrics.Metric;
import com.sixrr.metrics.MetricCalculator;
import com.sixrr.metrics.MetricsExecutionContext;
import com.sixrr.metrics.MetricsResultsHolder;
import com.sixrr.metrics.profile.MetricsProfile;
import com.sixrr.metrics.utils.MetricsReloadedBundle;

public abstract class MetricsExecutionContextImpl implements MetricsExecutionContext
{

	private final Project project;
	private final AnalysisScope scope;

	protected MetricsExecutionContextImpl(Project project, AnalysisScope scope)
	{
		this.project = project;
		this.scope = scope;
	}

	public final void execute(
			final MetricsProfile profile, final MetricsResultsHolder metricsRun)
	{
		final Task.Backgroundable task = new Task.Backgroundable(project, MetricsReloadedBundle.message("calculating.metrics"), true)
		{

			public void run(@NotNull final ProgressIndicator indicator)
			{
				final List<MetricInstance> metrics = profile.getMetrics();
				indicator.setText(MetricsReloadedBundle.message("initializing.progress.string"));
				final int numFiles = scope.getFileCount();
				final int numMetrics = metrics.size();
				final List<MetricCalculator> calculators = new ArrayList<MetricCalculator>(numMetrics);
				ApplicationManager.getApplication().runReadAction(new Runnable()
				{
					public void run()
					{
						for(final MetricInstance metricInstance : metrics)
						{
							if(indicator.isCanceled())
							{
								return;
							}
							final Metric metric = metricInstance.getMetric();
							if(metricInstance.isEnabled())
							{
								final MetricCalculator calculator = metric.createCalculator();

								if(calculator != null)
								{
									calculators.add(calculator);
									calculator.beginMetricsRun(metricInstance.getMetric(), metricsRun, MetricsExecutionContextImpl.this);
								}
							}
						}

						scope.accept(new PsiElementVisitor()
						{
							private int mainTraversalProgress = 0;

							@Override
							public void visitFile(PsiFile psiFile)
							{
								super.visitFile(psiFile);
								final String fileName = psiFile.getName();
								indicator.setText(MetricsReloadedBundle.message("analyzing.progress.string", fileName));
								mainTraversalProgress++;

								for(MetricCalculator calculator : calculators)
								{
									calculator.processFile(psiFile);
								}
								indicator.setFraction((double) mainTraversalProgress / (double) numFiles);
							}
						});
					}
				});


				indicator.setText(MetricsReloadedBundle.message("tabulating.results.progress.string"));
				for(MetricCalculator calculator : calculators)
				{
					if(indicator.isCanceled())
					{
						return;
					}
					calculator.endMetricsRun();
				}
			}

			@Override
			public void onSuccess()
			{
				onFinish();
			}

			@Override
			public void onCancel()
			{
				MetricsExecutionContextImpl.this.onCancel();
			}
		};
		task.queue();
	}

	public abstract void onFinish();

	public void onCancel()
	{
	}

	public final Project getProject()
	{
		return project;
	}

	public final AnalysisScope getScope()
	{
		return scope;
	}

	private Map userData = new HashMap();

	public final <T> T getUserData(Key<T> key)
	{
		return (T) userData.get(key);
	}

	public final <T> void putUserData(Key<T> key, T t)
	{
		userData.put(key, t);
	}
}
