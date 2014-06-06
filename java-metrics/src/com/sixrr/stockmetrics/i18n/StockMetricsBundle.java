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
package com.sixrr.stockmetrics.i18n;

import org.jetbrains.annotations.PropertyKey;
import com.intellij.AbstractBundle;

public class StockMetricsBundle extends AbstractBundle
{

	public static final String BUNDLE = "com.sixrr.stockmetrics.i18n.StockMetricsBundle";
	private static final StockMetricsBundle INSTANCE = new StockMetricsBundle();

	private StockMetricsBundle()
	{
		super(BUNDLE);
	}

	public static String message(@PropertyKey(resourceBundle = BUNDLE) String key, Object... params)
	{
		return INSTANCE.getMessage(key, params);
	}
}
