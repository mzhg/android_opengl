/*
 * Copyright 2017 mzhg
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jet.learning.opengl.android.base;

public class MainActivity extends BaseActivity {

	public static final String ACTION_SAMPLE = "jet.learning.opengl.action.sample";
	public static final String CATEGOTY_SUB = "jet.learning.opengl.category.sub";
	public static final String CATEGOTY_ES1 = "jet.learning.opengl.category.es1";
	public static final String CATEGOTY_ES2 = "jet.learning.opengl.category.es2";
	public static final String CATEGOTY_ES3 = "jet.learning.opengl.category.es3";
	public static final String CATEGOTY_ES31 = "jet.learning.opengl.category.es31";
	public static final String CATEGOTY_ES32 = "jet.learning.opengl.category.es32";
	public static final String RENDER_SCRIPT = "jet.learning.opengl.category.rs";

	@Override
	protected String getAction() {
		return null;
	}

	@Override
	protected String getCategory() {
		return ACTION_SAMPLE;
	}

}
