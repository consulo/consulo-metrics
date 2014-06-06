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

import com.intellij.psi.*;
import com.intellij.psi.javadoc.PsiDocComment;
import com.intellij.psi.util.PsiTreeUtil;
import com.sixrr.metrics.utils.BucketedCount;
import com.sixrr.metrics.utils.ClassUtils;
import com.sixrr.stockmetrics.utils.LineUtil;

import java.util.Set;

public class JavadocLinesOfCodePackageCalculator extends PackageCalculator {

    private final BucketedCount<PsiPackage> numCommentLinesPerPackage = new BucketedCount<PsiPackage>();

    @Override
    public void endMetricsRun() {
        final Set<PsiPackage> packages = numCommentLinesPerPackage.getBuckets();
        for (final PsiPackage aPackage : packages) {
            final int numCommentLines = numCommentLinesPerPackage.getBucketValue(aPackage);

            postMetric(aPackage, numCommentLines);
        }
    }

    @Override
    protected PsiElementVisitor createVisitor() {
        return new Visitor();
    }

    private class Visitor extends JavaRecursiveElementVisitor {

        @Override
        public void visitJavaFile(PsiJavaFile file) {
            super.visitJavaFile(file);
            final PsiPackage aPackage = ClassUtils.findPackage(file);
            if (aPackage == null) {
                return;
            }
            numCommentLinesPerPackage.createBucket(aPackage);
        }

        @Override
        public void visitDocComment(PsiDocComment comment) {
            super.visitDocComment(comment);
            final PsiClass aClass = PsiTreeUtil.getParentOfType(comment, PsiClass.class);
            final PsiPackage aPackage = ClassUtils.findPackage(aClass);
            if (aPackage == null) {
                return;
            }
            final int lineCount = LineUtil.countLines(comment);
            numCommentLinesPerPackage.incrementBucketValue(aPackage, lineCount);
        }
    }
}