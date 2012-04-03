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

package com.rcs.portlet.slider.action;

import java.util.Date;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletConfig;
import javax.portlet.PortletPreferences;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.portlet.ConfigurationAction;
import com.liferay.portal.kernel.servlet.SessionErrors;
import com.liferay.portal.kernel.servlet.SessionMessages;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portlet.PortletPreferencesFactoryUtil;

/**
 * @author Rajesh
 */
public class ConfigurationActionImpl implements ConfigurationAction {

		public String render(PortletConfig portletConfig,
										RenderRequest renderRequest,
										RenderResponse renderResponse)
										throws Exception {

				return "/jsps/config/configuration.jsp";
		}

		public void processAction(PortletConfig portletConfig,
										ActionRequest actionRequest,
										ActionResponse actionResponse)
										throws Exception {

				String cmd = actionRequest.getParameter(SliderConstants.CMD);

				if (Validator.isNull(cmd)) {
						throw new Exception("exception-occurred");
				}

				try {
						if (cmd.equals(SliderConstants.UPDATE)) {
								savePreferences(actionRequest, actionResponse);
						}
						else if (cmd.equals(SliderConstants.DELETE)) {
								deleteSlide(actionRequest, actionResponse);
						}

						SessionMessages.add(actionRequest,
								"request-successfully",
								portletConfig.getPortletName() + ".doConfigure");

						return;
				}
				catch (Exception e) {

						_log.error(e.getMessage());
						SessionErrors.add(actionRequest, e.getMessage());

						return;
				}
		}

		private void deleteSlide(ActionRequest request, ActionResponse response)
										throws Exception {

				String slideId = ParamUtil.getString(request, "slideId", null);

				if (_log.isDebugEnabled())
						_log.debug("deleteSlide - slideId=" + slideId);

				if (Validator.isNotNull(slideId)) {
						String portletResource = ParamUtil.getString(request,
								"portletResource");

						PortletPreferences preferences = PortletPreferencesFactoryUtil
														.getPortletSetup(
																request,
																portletResource);
						preferences.reset(slideId);
						preferences.store();
				}
				else {
						throw new Exception("invalid-slide");
				}

		}

		private void savePreferences(ActionRequest request,
										ActionResponse response)
										throws Exception {

				String portletResource = ParamUtil.getString(request,
						"portletResource");

				PortletPreferences portletPreferences = PortletPreferencesFactoryUtil
												.getPortletSetup(request,
														portletResource);

				String slideId = ParamUtil.getString(request, "slideId", null);
				String title = ParamUtil.getString(request, "title", "");
				String link = ParamUtil.getString(request, "link", "");
				String desc = ParamUtil.getString(request, "desc", "");
				String image = ParamUtil.getString(request, "image", "");

				if (_log.isDebugEnabled()) {
						_log.debug("savePreferences - slideId=" + slideId
								   + ", title=" + title + ", link=" + link
								   + ", desc=" + desc + ", image=" + image);
				}

				verifyParameter(title, link, image);

				String[] values = new String[] { title, link, desc, image, String
												.valueOf((new Date().getTime())) };

				if (slideId == null || "".equals(slideId.trim())) {
						slideId = "slides_"
								  + String.valueOf((new Date()).getTime());
				}

				if (_log.isDebugEnabled())
						_log.debug("slideId=" + slideId);

				portletPreferences.setValues(slideId, values);
				portletPreferences.store();

				response.setRenderParameter("slideParamId", slideId);
				response.setRenderParameter("slideImage", image);
		}

		private void verifyParameter(String title, String link, String image) {

				if (Validator.isNull(title)) {
						throw new IllegalArgumentException("title-invalid");
				}
				if (Validator.isNull(image)) {
						throw new IllegalArgumentException("image-invalid");
				}
		}

		private Log _log = LogFactoryUtil.getLog(ConfigurationActionImpl.class);
}