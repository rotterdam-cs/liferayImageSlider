/**
 * Copyright (C) Rotterdam Community Solutions B.V.
 * http://www.rotterdam-cs.com
 *
 ***********************************************************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package com.rcs.portlet.slider.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.theme.ThemeDisplay;
import com.liferay.portlet.PortletPreferencesFactoryUtil;
import com.rcs.portlet.slider.model.Slide;

/**
 * @author Rajesh
 * 
 */
public class SliderUtil {

		public static List<Slide> getSlides(PortletRequest request,
										PortletResponse response)
										throws PortalException, SystemException {

				String portletResource = ParamUtil.getString(request,
						"portletResource");
				PortletPreferences portletPreferences = getPreference(request,
						portletResource);

				List<Slide> slides = new ArrayList<Slide>();
				Enumeration<String> prefMap = portletPreferences.getNames();

				while (prefMap.hasMoreElements()) {

						String slideId = prefMap.nextElement();
						if (slideId.startsWith("slides_")) {
								String[] values = portletPreferences.getValues(
										slideId, null);
								if (Validator.isNotNull(values)) {
										Slide slide = getSlide(slideId, values);
										slides.add(slide);
								}
						}
				}

				Collections.sort(slides, new DateComparator());

				return slides;
		}

		private static PortletPreferences getPreference(PortletRequest request,
										String portletResource)
										throws PortalException, SystemException {

				if (portletResource == null
					|| portletResource.trim().equals("")) {
						ThemeDisplay themeDisplay = (ThemeDisplay) request
														.getAttribute("THEME_DISPLAY");
						portletResource = themeDisplay.getPortletDisplay()
														.getId();
				}

				return PortletPreferencesFactoryUtil.getPortletSetup(request,
						portletResource);
		}

		public static Slide getSlide(PortletRequest request, String slideId) {

				PortletPreferences preferences = request.getPreferences();
				String[] values = preferences.getValues(slideId, null);

				if (Validator.isNotNull(values)) {
						return getSlide(slideId, values);
				}

				return new Slide();
		}

		public static Slide getSlide(String slideId, String[] values) {

				String title = values[0];
				String link = values[1];
				String desc = values[2];
				String imageUrl = values[3];
				String timeMillis = values[4];

				Slide slide = new Slide(slideId, title, link, imageUrl, desc,
										timeMillis, 0);
				return slide;
		}
}
