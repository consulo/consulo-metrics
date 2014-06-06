package com.sixrr.stockmetrics;

import java.util.Set;

import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiReference;

public interface MethodCallMap
{
	Set<PsiReference> calculateMethodCallPoints(PsiMethod method);

	Set<PsiReference> calculateTestMethodCallPoints(PsiMethod method);

	Set<PsiReference> calculateProductMethodCallPoints(PsiMethod method);
}
