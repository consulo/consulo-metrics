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

package consulo.metrics.impl.defaultMetricsProvider;

import com.intellij.openapi.extensions.AbstractExtensionPointBean;
import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.util.xmlb.annotations.Attribute;

/**
 * @author VISTALL
 * @since 09.06.14
 */
public class LineOfCodeFileTypeProviderEP extends AbstractExtensionPointBean
{
	public static ExtensionPointName<LineOfCodeFileTypeProviderEP> EP_NAME = ExtensionPointName.create("consulo.metrics.lineOfCodeFileTypeProvider");

	@Attribute("fileType")
	public String fileType;
}
