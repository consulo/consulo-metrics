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

package com.sixrr.metrics.profile.instanceHolder;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.consulo.annotations.Immutable;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import com.intellij.ide.plugins.cl.PluginClassLoader;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeRegistry;
import com.intellij.openapi.fileTypes.PlainTextFileType;
import com.intellij.util.lang.UrlClassLoader;
import com.sixrr.metrics.Metric;
import com.sixrr.metrics.MetricProvider;
import com.sixrr.metrics.impl.defaultMetricsProvider.LineOfCodeFileTypeProviderEP;
import com.sixrr.metrics.impl.defaultMetricsProvider.projectMetrics.LinesOfCodeProjectMetric;
import com.sixrr.metrics.metricModel.MetricInstance;
import com.sixrr.metrics.metricModel.MetricInstanceImpl;
import consulo.lombok.annotations.ApplicationService;
import consulo.lombok.annotations.Logger;

/**
 * @author VISTALL
 * @since 09.06.14
 */
@ApplicationService
@Logger
public class MetricInstanceHolder
{
	private final Map<String, Metric> myMetricInstances = new HashMap<String, Metric>(200);

	public MetricInstanceHolder()
	{
		loadMetricsFromProviders();
	}

	public void createInstances(Set<MetricInstance> set)
	{
		for(Map.Entry<String, Metric> entry : myMetricInstances.entrySet())
		{
			set.add(new MetricInstanceImpl(entry.getKey(), entry.getValue()));
		}
	}

	@NotNull
	@Immutable
	public Collection<Metric> getMetrics()
	{
		return myMetricInstances.values();
	}

	@NotNull
	public Metric getMetricByClass(@NotNull String className)
	{
		Metric metric = myMetricInstances.get(className);
		return metric == null ? DummyMetric.INSTANCE : metric;
	}

	private void loadMetricsFromProviders()
	{
		for(MetricProvider provider : MetricProvider.EXTENSION_POINT_NAME.getExtensions())
		{
			final List<Class<? extends Metric>> classesForProvider = provider.getMetricClasses();

			for(Class<? extends Metric> aClass : classesForProvider)
			{
				try
				{
					myMetricInstances.put(aClass.getName(), aClass.newInstance());
				}
				catch(InstantiationException e)
				{
					MetricInstanceHolder.LOGGER.error(e);
				}
				catch(IllegalAccessException e)
				{
					MetricInstanceHolder.LOGGER.error(e);
				}
			}
		}

		for(Metric metric : Metric.EP_NAME.getExtensions())
		{
			myMetricInstances.put(metric.getClass().getName(), metric);
		}

		// add some magic
		// in Profiles it stored by ClassName. Line Of code is can be handle without new metrics, need only extends  LinesOfCodeProjectMetric with
		// new name
		for(LineOfCodeFileTypeProviderEP ep : LineOfCodeFileTypeProviderEP.EP_NAME.getExtensions())
		{
			FileType fileTypeByFileName = FileTypeRegistry.getInstance().findFileTypeByName(ep.fileType);

			if(fileTypeByFileName == null)
			{
				LOGGER.error("File type is unknown: " + ep.fileType);
				fileTypeByFileName = PlainTextFileType.INSTANCE;
			}
			String lineOfCodeClass = Type.getInternalName(LinesOfCodeProjectMetric.class);
			String className = lineOfCodeClass + "$" + ep.fileType;

			ClassWriter writer = new ClassWriter(Opcodes.F_FULL);
			writer.visit(Opcodes.V1_6, Opcodes.ACC_PUBLIC, className, null, lineOfCodeClass, null);

			String desc = Type.getConstructorDescriptor(LinesOfCodeProjectMetric.class.getConstructors()[0]);
			MethodVisitor methodVisitor = writer.visitMethod(Opcodes.ACC_PUBLIC, "<init>", desc, null, null);

			methodVisitor.visitMaxs(2, 2);
			methodVisitor.visitVarInsn(Opcodes.ALOAD, 0);
			methodVisitor.visitVarInsn(Opcodes.ALOAD, 1);
			methodVisitor.visitMethodInsn(Opcodes.INVOKESPECIAL, lineOfCodeClass, "<init>", desc);
			methodVisitor.visitInsn(Opcodes.RETURN);
			methodVisitor.visitEnd();
			writer.visitEnd();

			Class<?> aClass = defineClass(className.replace("/", "."), writer.toByteArray());

			try
			{
				myMetricInstances.put(aClass.getName(), (Metric) aClass.getConstructors()[0].newInstance(fileTypeByFileName));
			}
			catch(InstantiationException e)
			{
				LOGGER.error(e);
			}
			catch(IllegalAccessException e)
			{
				LOGGER.error(e);
			}
			catch(InvocationTargetException e)
			{
				LOGGER.error(e);
			}
		}
	}

	private Class<?> defineClass(String name, byte[] array)
	{
		PluginClassLoader classLoader = ((PluginClassLoader) getClass().getClassLoader());
		try
		{
			Method defineClass = UrlClassLoader.class.getDeclaredMethod("_defineClass", String.class, byte[].class);
			defineClass.setAccessible(true);
			return (Class<?>) defineClass.invoke(classLoader, name, array);
		}
		catch(NoSuchMethodException e)
		{
			LOGGER.error(e);
		}
		catch(InvocationTargetException e)
		{
			LOGGER.error(e);
		}
		catch(IllegalAccessException e)
		{
			LOGGER.error(e);
		}
		return null;
	}
}
