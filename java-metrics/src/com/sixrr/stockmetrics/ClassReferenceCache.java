package com.sixrr.stockmetrics;

import java.util.Collection;
import java.util.Map;
import java.util.WeakHashMap;

import com.intellij.openapi.progress.ProgressManager;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiReference;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import com.intellij.psi.search.SearchScope;
import com.intellij.psi.search.searches.ReferencesSearch;

public class ClassReferenceCache
{

	private final Map<SmartPsiElementPointer<PsiClass>, Collection<PsiReference>> cachedReferences = new WeakHashMap<SmartPsiElementPointer<PsiClass>,
			Collection<PsiReference>>(256);

	public Collection<PsiReference> findClassReferences(final PsiClass aClass)
	{
		final SmartPointerManager manager = SmartPointerManager.getInstance(aClass.getProject());
		final SmartPsiElementPointer<PsiClass> pointer = manager.createSmartPsiElementPointer(aClass);

		final Collection<PsiReference> references = cachedReferences.get(pointer);
		if(references == null)
		{
			final Runnable runnable = new Runnable()
			{
				public void run()
				{
					final SearchScope scope = aClass.getUseScope();
					final Collection<PsiReference> references = ReferencesSearch.search(aClass, scope, true).findAll();
					cachedReferences.put(pointer, references);
				}
			};
			final ProgressManager progressManager = ProgressManager.getInstance();
			progressManager.runProcess(runnable, null);
		}
		return cachedReferences.get(pointer);
	}
}
