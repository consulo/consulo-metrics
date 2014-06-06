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

package com.sixrr.stockmetrics.moduleCalculators;

import com.intellij.openapi.module.Module;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.sixrr.metrics.utils.BucketedCount;
import com.sixrr.metrics.utils.ClassUtils;

import java.util.Set;

abstract class ElementRatioModuleCalculator extends ModuleCalculator {
    protected final BucketedCount<Module> numeratorPerModule = new BucketedCount<Module>();
    protected final BucketedCount<Module> denominatorPerModule = new BucketedCount<Module>();

    @Override
    public void endMetricsRun() {
        final Set<Module> modules = numeratorPerModule.getBuckets();
        for (final Module module : modules) {
            final int numerator = numeratorPerModule.getBucketValue(module);
            final int denominator = denominatorPerModule.getBucketValue(module);

            if (denominator == 0) {
                postMetric(module, 0);
            } else {
                postMetric(module, numerator, denominator);
            }
        }
    }

    protected void incrementNumerator(PsiElement element, int count) {
        final PsiFile file = PsiTreeUtil.getParentOfType(element, PsiFile.class, false);
        if (file == null) {
            return;
        }
        final Module module = ClassUtils.calculateModule(file);
        if (module == null) {
            return;
        }
        numeratorPerModule.incrementBucketValue(module, count);
    }

    protected void incrementDenominator(PsiElement element, int count) {
        final PsiFile file = PsiTreeUtil.getParentOfType(element, PsiFile.class, false);
        if (file == null) {
            return;
        }
        final Module module = ClassUtils.calculateModule(file);
        if (module == null) {
            return;
        }
        denominatorPerModule.incrementBucketValue(module, count);
    }
}
