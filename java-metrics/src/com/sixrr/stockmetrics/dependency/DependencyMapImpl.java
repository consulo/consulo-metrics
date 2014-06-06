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

package com.sixrr.stockmetrics.dependency;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.sixrr.metrics.utils.Bag;
import com.sixrr.metrics.utils.ClassUtils;

@SuppressWarnings({
		"MethodWithMultipleLoops",
		"ClassWithTooManyFields"
})
public class DependencyMapImpl implements DependencyMap
{
	private final Map<PsiClass, Bag<PsiClass>> dependencies = new HashMap<PsiClass, Bag<PsiClass>>(1024);
	private final Map<PsiClass, Set<PsiClass>> transitiveDependencies = new HashMap<PsiClass, Set<PsiClass>>(1024);
	private final Map<PsiJavaPackage, Set<PsiJavaPackage>> transitivePackageDependencies = new HashMap<PsiJavaPackage, Set<PsiJavaPackage>>(1024);
	private final Map<PsiClass, Set<PsiClass>> stronglyConnectedComponents = new HashMap<PsiClass, Set<PsiClass>>(1024);
	private final Map<PsiClass, Integer> levelOrders = new HashMap<PsiClass, Integer>(1024);
	private final Map<PsiClass, Integer> adjustedLevelOrders = new HashMap<PsiClass, Integer>(1024);
	private final Map<PsiClass, Bag<PsiJavaPackage>> packageDependencies = new HashMap<PsiClass, Bag<PsiJavaPackage>>(1024);
	private final Map<PsiJavaPackage, Bag<PsiJavaPackage>> packageToPackageDependencies = new HashMap<PsiJavaPackage, Bag<PsiJavaPackage>>(1024);
	private final Map<PsiJavaPackage, Set<PsiJavaPackage>> stronglyConnectedPackageComponents = new HashMap<PsiJavaPackage, Set<PsiJavaPackage>>(1024);
	private final Map<PsiJavaPackage, Integer> packageLevelOrders = new HashMap<PsiJavaPackage, Integer>(1024);
	private final Map<PsiJavaPackage, Integer> packageAdjustedLevelOrders = new HashMap<PsiJavaPackage, Integer>(1024);

	@Override
	public Set<PsiClass> calculateDependencies(PsiClass aClass)
	{
		if(dependencies.containsKey(aClass))
		{
			final Bag<PsiClass> dependenciesForClass = dependencies.get(aClass);
			return dependenciesForClass.getContents();
		}
		else
		{
			return Collections.emptySet();
		}
	}

	@Override
	public Set<PsiClass> calculateTransitiveDependencies(PsiClass aClass)
	{
		final Set<PsiClass> out = transitiveDependencies.get(aClass);
		if(out != null)
		{
			return out;
		}

		final List<PsiClass> pendingClasses = new ArrayList<PsiClass>();
		pendingClasses.add(aClass);
		final Set<PsiClass> allDependencies = new HashSet<PsiClass>();
		while(!pendingClasses.isEmpty())
		{
			final PsiClass pendingClass = pendingClasses.get(0);
			pendingClasses.remove(0);
			if(!allDependencies.contains(pendingClass))
			{
				if(transitiveDependencies.containsKey(pendingClass))
				{
					allDependencies.addAll(transitiveDependencies.get(pendingClass));
				}
				else
				{
					allDependencies.add(pendingClass);
					if(pendingClass != null)
					{
						final Set<PsiClass> indirectDependencies = calculateDependencies(pendingClass);
						pendingClasses.addAll(indirectDependencies);
					}
				}
			}
		}
		transitiveDependencies.put(aClass, allDependencies);
		return allDependencies;
	}

	@Override
	public Set<PsiClass> calculateStronglyConnectedComponents(PsiClass aClass)
	{
		final Set<PsiClass> out = stronglyConnectedComponents.get(aClass);
		if(out != null)
		{
			return out;
		}
		final Set<PsiClass> transitiveDeps = calculateTransitiveDependencies(aClass);
		final Set<PsiClass> component = new HashSet<PsiClass>();
		component.add(aClass);
		for(final PsiClass dependencyClass : transitiveDeps)
		{
			if(dependencyClass != null)
			{
				final Set<PsiClass> dependencyDependencies = calculateTransitiveDependencies(dependencyClass);
				if(transitiveDeps.size() == dependencyDependencies.size())
				{
					component.add(dependencyClass);
				}
			}
		}
		for(final PsiClass componentElement : component)
		{
			stronglyConnectedComponents.put(componentElement, component);
		}
		return component;
	}

	@Override
	public int calculateLevelOrder(PsiClass aClass)
	{
		final Integer out = levelOrders.get(aClass);
		if(out != null)
		{
			return out;
		}
		final Set<PsiClass> dependentClasses = calculateDependencies(aClass);
		final Set<PsiClass> mutuallyDependentClasses = calculateStronglyConnectedComponents(aClass);
		int levelOrder = 0;
		for(final PsiClass dependentClass : dependentClasses)
		{
			if(!mutuallyDependentClasses.contains(dependentClass))
			{
				if(dependentClass != null)
				{
					final int dependentLevelOrder = calculateLevelOrder(dependentClass);
					levelOrder = Math.max(levelOrder, dependentLevelOrder);
				}
			}
		}
		levelOrder += 1;
		for(final PsiClass stronglyConnectedClass : mutuallyDependentClasses)
		{
			levelOrders.put(stronglyConnectedClass, levelOrder);
		}
		return levelOrder;
	}

	@Override
	public int calculateAdjustedLevelOrder(PsiClass aClass)
	{
		final Integer out = adjustedLevelOrders.get(aClass);
		if(out != null)
		{
			return out;
		}
		final Set<PsiClass> dependentClasses = calculateDependencies(aClass);
		final Set<PsiClass> mutuallyDependentClasses = calculateStronglyConnectedComponents(aClass);
		int levelOrder = 0;
		for(final PsiClass dependentClass : dependentClasses)
		{
			if(!mutuallyDependentClasses.contains(dependentClass))
			{
				if(dependentClass != null)
				{
					final int dependentLevelOrder = calculateAdjustedLevelOrder(dependentClass);
					levelOrder = Math.max(levelOrder, dependentLevelOrder);
				}
			}
		}
		levelOrder += mutuallyDependentClasses.size();
		for(final PsiClass stronglyConnectedClass : mutuallyDependentClasses)
		{
			adjustedLevelOrders.put(stronglyConnectedClass, levelOrder);
		}
		return levelOrder;
	}

	@Override
	public Set<PsiJavaPackage> calculatePackageDependencies(PsiClass aClass)
	{
		final Bag<PsiJavaPackage> existing = packageDependencies.get(aClass);
		if(existing != null)
		{
			return existing.getContents();
		}
		return Collections.emptySet();
	}

	@Override
	public Set<PsiJavaPackage> calculateTransitivePackageDependencies(PsiJavaPackage packageName)
	{
		final Set<PsiJavaPackage> out = transitivePackageDependencies.get(packageName);

		if(out != null)
		{
			return out;
		}
		final List<PsiJavaPackage> pendingPackages = new ArrayList<PsiJavaPackage>();
		pendingPackages.add(packageName);
		final Set<PsiJavaPackage> allDependencies = new HashSet<PsiJavaPackage>();
		while(!pendingPackages.isEmpty())
		{
			final PsiJavaPackage dependencyPackageName = pendingPackages.get(0);
			pendingPackages.remove(0);
			if(!allDependencies.contains(dependencyPackageName))
			{
				if(transitivePackageDependencies.containsKey(dependencyPackageName))
				{
					allDependencies.addAll(transitivePackageDependencies.get(dependencyPackageName));
				}
				else
				{
					allDependencies.add(dependencyPackageName);

					final Set<PsiJavaPackage> indirectDependencies = calculatePackageToPackageDependencies(dependencyPackageName);
					pendingPackages.addAll(indirectDependencies);
				}
			}
		}
		transitivePackageDependencies.put(packageName, allDependencies);
		return allDependencies;
	}

	@Override
	public Set<PsiJavaPackage> calculateStronglyConnectedPackageComponents(PsiJavaPackage name)
	{
		final Set<PsiJavaPackage> out = stronglyConnectedPackageComponents.get(name);
		if(out != null)
		{
			return out;
		}
		final Set<PsiJavaPackage> transitiveDeps = calculateTransitivePackageDependencies(name);
		final Set<PsiJavaPackage> component = new HashSet<PsiJavaPackage>();
		component.add(name);
		for(final PsiJavaPackage dependencyPackage : transitiveDeps)
		{
			final Set<PsiJavaPackage> dependencyDependencies = calculateTransitivePackageDependencies(dependencyPackage);
			if(transitiveDeps.size() == dependencyDependencies.size())
			{
				component.add(dependencyPackage);
			}
		}
		for(final PsiJavaPackage componentElement : component)
		{
			stronglyConnectedPackageComponents.put(componentElement, component);
		}
		return component;
	}

	@Override
	public int calculatePackageLevelOrder(PsiJavaPackage packageName)
	{
		final Integer out = packageLevelOrders.get(packageName);
		if(out != null)
		{
			return out;
		}
		final Set<PsiJavaPackage> dependentPackages = calculatePackageToPackageDependencies(packageName);
		final Set<PsiJavaPackage> mutuallyDependentPackages = calculateStronglyConnectedPackageComponents(packageName);
		int levelOrder = 0;
		for(final PsiJavaPackage dependentPackage : dependentPackages)
		{
			if(!mutuallyDependentPackages.contains(dependentPackage))
			{
				final int dependentLevelOrder = calculatePackageLevelOrder(dependentPackage);
				levelOrder = Math.max(levelOrder, dependentLevelOrder);
			}
		}
		levelOrder += 1;
		for(final PsiJavaPackage mutuallyDependentPackage : mutuallyDependentPackages)
		{
			packageLevelOrders.put(mutuallyDependentPackage, levelOrder);
		}
		return levelOrder;
	}

	@Override
	public int calculatePackageAdjustedLevelOrder(PsiJavaPackage aPackage)
	{
		final Integer out = packageAdjustedLevelOrders.get(aPackage);
		if(out != null)
		{
			return out;
		}
		final Set<PsiJavaPackage> dependentPackages = calculatePackageToPackageDependencies(aPackage);
		final Set<PsiJavaPackage> mutuallyDependentPackages = calculateStronglyConnectedPackageComponents(aPackage);
		int levelOrder = 0;
		for(final PsiJavaPackage dependentPackage : dependentPackages)
		{
			if(!mutuallyDependentPackages.contains(dependentPackage))
			{
				final int dependentLevelOrder = calculatePackageAdjustedLevelOrder(dependentPackage);
				levelOrder = Math.max(levelOrder, dependentLevelOrder);
			}
		}
		levelOrder += mutuallyDependentPackages.size();
		for(final PsiJavaPackage stronglyConnectedPackage : mutuallyDependentPackages)
		{
			packageAdjustedLevelOrders.put(stronglyConnectedPackage, levelOrder);
		}
		return levelOrder;
	}

	@Override
	public int getStrengthForDependency(PsiClass aClass, PsiClass dependencyClass)
	{
		final Bag<PsiClass> dependenciesForClass = dependencies.get(aClass);
		return dependenciesForClass.getCountForObject(dependencyClass);
	}

	@Override
	public int getStrengthForPackageDependency(PsiClass aClass, PsiJavaPackage dependencyPackage)
	{
		final Bag<PsiJavaPackage> dependenciesForClass = packageDependencies.get(aClass);
		return dependenciesForClass.getCountForObject(dependencyPackage);
	}

	@Override
	public Set<PsiJavaPackage> calculatePackageToPackageDependencies(PsiJavaPackage packageName)
	{
		if(packageToPackageDependencies.containsKey(packageName))
		{
			final Bag<PsiJavaPackage> dependenciesForPackage = packageToPackageDependencies.get(packageName);
			return dependenciesForPackage.getContents();
		}
		else
		{
			return Collections.emptySet();
		}
	}

	public void build(PsiClass aClass)
	{
		final DependenciesVisitor visitor = new DependenciesVisitor();
		aClass.accept(visitor);
	}

	private class DependenciesVisitor extends JavaRecursiveElementVisitor
	{

		private final Stack<PsiClass> classStack = new Stack<PsiClass>();
		private PsiClass currentClass = null;

		@Override
		public void visitClass(PsiClass aClass)
		{
			if(!ClassUtils.isAnonymous(aClass))
			{
				classStack.push(currentClass);
				currentClass = aClass;
				final PsiType[] superTypes = aClass.getSuperTypes();
				for(final PsiType superType : superTypes)
				{
					addDependencyForType(superType);
				}
				final PsiTypeParameter[] parameters = aClass.getTypeParameters();
				for(PsiTypeParameter parameter : parameters)
				{
					addDependencyForClass(parameter);
				}
			}
			super.visitClass(aClass);
			if(!ClassUtils.isAnonymous(aClass))
			{
				currentClass = classStack.pop();
			}
		}

		@Override
		public void visitMethodCallExpression(PsiMethodCallExpression expression)
		{
			super.visitMethodCallExpression(expression);
			final PsiMethod method = expression.resolveMethod();
			if(method == null)
			{
				return;
			}
			final PsiClass containingClass = method.getContainingClass();
			if(containingClass == null)
			{
				return;
			}
			final Project project = expression.getProject();
			final JavaPsiFacade psiFacade = JavaPsiFacade.getInstance(project);
			final PsiElementFactory elementFactory = psiFacade.getElementFactory();
			final PsiClassType type = elementFactory.createType(containingClass);
			addDependencyForType(type);
			final PsiType[] arguments = expression.getTypeArguments();
			for(PsiType argument : arguments)
			{
				addDependencyForType(argument);
			}
		}

		@Override
		public void visitReferenceExpression(PsiReferenceExpression expression)
		{
			super.visitReferenceExpression(expression);

			final PsiElement element = expression.resolve();
			if(element == null)
			{
				return;
			}
			if(element instanceof PsiField)
			{
				final PsiClass containingClass = ((PsiMember) element).getContainingClass();
				addDependencyForClass(containingClass);
			}
			else if(element instanceof PsiClass)
			{
				addDependencyForClass((PsiClass) element);
			}
		}

		@Override
		public void visitVariable(PsiVariable variable)
		{
			super.visitVariable(variable);
			addDependencyForType(variable.getType());
		}

		@Override
		public void visitMethod(PsiMethod method)
		{
			super.visitMethod(method);
			final PsiType returnType = method.getReturnType();
			addDependencyForType(returnType);
			addDependencyForThrowsClause(method);
		}

		private void addDependencyForThrowsClause(PsiMethod method)
		{
			final PsiReferenceList throwsList = method.getThrowsList();
			final PsiClassType[] throwsTypes = throwsList.getReferencedTypes();
			for(final PsiClassType throwsType : throwsTypes)
			{
				addDependencyForType(throwsType);
			}
		}

		@Override
		public void visitNewExpression(PsiNewExpression expression)
		{
			super.visitNewExpression(expression);
			final PsiType classType = expression.getType();
			addDependencyForType(classType);
			final PsiType[] arguments = expression.getTypeArguments();
			for(PsiType argument : arguments)
			{
				addDependencyForType(argument);
			}
		}

		@Override
		public void visitClassObjectAccessExpression(PsiClassObjectAccessExpression exp)
		{
			super.visitClassObjectAccessExpression(exp);
			final PsiTypeElement operand = exp.getOperand();
			final PsiType classType = operand.getType();
			addDependencyForType(classType);
		}

		@Override
		public void visitInstanceOfExpression(PsiInstanceOfExpression exp)
		{
			super.visitInstanceOfExpression(exp);
			final PsiTypeElement checkType = exp.getCheckType();
			if(checkType == null)
			{
				return;
			}
			final PsiType classType = checkType.getType();
			addDependencyForType(classType);
		}

		@Override
		public void visitTypeCastExpression(PsiTypeCastExpression exp)
		{
			super.visitTypeCastExpression(exp);
			final PsiTypeElement castType = exp.getCastType();
			if(castType == null)
			{
				return;
			}
			final PsiType classType = castType.getType();
			addDependencyForType(classType);
		}

		private void addDependencyForType(@Nullable PsiType type)
		{
			if(type == null)
			{
				return;
			}
			final PsiType baseType = type.getDeepComponentType();
			if(!(baseType instanceof PsiClassType))
			{
				return;
			}
			final PsiClassType classType = (PsiClassType) baseType;
			final PsiType[] parameters = classType.getParameters();
			for(PsiType parameter : parameters)
			{
				addDependencyForType(parameter);
			}
			final PsiClass referencedClass = classType.resolve();
			addDependencyForClass(referencedClass);
		}

		private void addDependencyForClass(PsiClass referencedClass)
		{
			if(currentClass == null)
			{
				return;
			}
			if(referencedClass == null)
			{
				return;
			}
			if(referencedClass.equals(currentClass))
			{
				return;
			}
			@NonNls final String referencedClassName = referencedClass.getQualifiedName();
			if("_Dummy_.__Array__".equals(referencedClassName))
			{
				return;
			}
			final PsiJavaFile classFile = PsiTreeUtil.getParentOfType(referencedClass, PsiJavaFile.class);
			if(classFile == null)
			{
				return;
			}
			@NonNls final String classFileName = classFile.getName();
			if(!classFileName.endsWith(".java"))
			{
				return;
			}
			final PsiJavaPackage referencedPackage = ClassUtils.findPackage(referencedClass);
			addDependency(currentClass, referencedClass, referencedPackage);
		}

		private void addDependency(PsiClass aClass, PsiClass dependencyClass, PsiJavaPackage dependencyPackage)
		{
			Bag<PsiClass> dependenciesForClass = dependencies.get(aClass);
			if(dependenciesForClass == null)
			{
				dependenciesForClass = new Bag<PsiClass>();
				dependencies.put(aClass, dependenciesForClass);
			}
			dependenciesForClass.add(dependencyClass);
			Bag<PsiJavaPackage> packageDependentsForClass = packageDependencies.get(aClass);
			if(packageDependentsForClass == null)
			{
				packageDependentsForClass = new Bag<PsiJavaPackage>();
				packageDependencies.put(aClass, packageDependentsForClass);
			}
			packageDependentsForClass.add(dependencyPackage);
			Bag<PsiJavaPackage> packageDependentsForPackage = packageToPackageDependencies.get(dependencyPackage);
			if(packageDependentsForPackage == null)
			{
				packageDependentsForPackage = new Bag<PsiJavaPackage>();
				packageToPackageDependencies.put(dependencyPackage, packageDependentsForPackage);
			}
			packageDependentsForPackage.add(dependencyPackage);
		}
	}
}
