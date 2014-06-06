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

package com.sixrr.stockmetrics.dependency;

import java.util.Set;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiJavaPackage;

public interface DependencyMap
{
	Set<PsiClass> calculateDependencies(PsiClass aClass);

	Set<PsiClass> calculateTransitiveDependencies(PsiClass aClass);

	Set<PsiClass> calculateStronglyConnectedComponents(PsiClass aClass);

	int calculateLevelOrder(PsiClass aClass);

	int calculateAdjustedLevelOrder(PsiClass aClass);

	Set<PsiJavaPackage> calculatePackageDependencies(PsiClass aClass);

	Set<PsiJavaPackage> calculateTransitivePackageDependencies(PsiJavaPackage packageName);

	Set<PsiJavaPackage> calculateStronglyConnectedPackageComponents(PsiJavaPackage name);

	int calculatePackageLevelOrder(PsiJavaPackage packageName);

	int calculatePackageAdjustedLevelOrder(PsiJavaPackage aPackage);

	int getStrengthForDependency(PsiClass aClass, PsiClass dependencyClass);

	int getStrengthForPackageDependency(PsiClass aClass, PsiJavaPackage dependencyPackage);

	Set<PsiJavaPackage> calculatePackageToPackageDependencies(PsiJavaPackage packageName);
}
