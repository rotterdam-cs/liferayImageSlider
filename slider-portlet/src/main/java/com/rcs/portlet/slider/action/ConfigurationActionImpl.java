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
import java.util.Enumeration;
import java.util.List;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletConfig;
import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
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
import com.rcs.portlet.slider.model.Slide;
import com.rcs.portlet.slider.util.SliderUtil;

/**
 * @author Rajesh
 */
public class ConfigurationActionImpl implements ConfigurationAction {

		public String render(PortletConfig portletConfig,
										RenderRequest renderRequest,
										RenderResponse renderResponse)
										throws Exception {

				String cmd = renderRequest.getParameter(SliderConstants.CMD);

				if (Validator.isNotNull(cmd)) {
						processRequest(renderRequest, renderResponse, cmd);
				}

				return "/jsps/config/configuration.jsp";
		}

		public void processAction(PortletConfig portletConfig,
										ActionRequest request,
										ActionResponse response)
										throws Exception {

				String cmd = request.getParameter(SliderConstants.CMD);

				if (Validator.isNull(cmd)) {
						throw new Exception("exception-occurred");
				}
				try {
						if (cmd.equals(SliderConstants.UPDATE)) {
								savePreferences(request, response);
								response.setRenderParameter("tab", "slides");
						}
						else if (cmd.equals(SliderConstants.UPDATE_SETTINGS)) {
								String tab = ParamUtil.getString(request, "tab");
								if (tab != null)
										response.setRenderParameter("tab", tab);
								updateSettings(request, response);
						}
						else if (cmd.equals(SliderConstants.DELETE)) {
								deleteSlide(request, response);
								response.setRenderParameter("tab", "slides");
						}

						SessionMessages.add(request, "request-successfully");
				}
				catch (Exception e) {
						_log.error(e.getMessage());
						SessionErrors.add(request, e.getMessage());
				}
		}

		private void processRequest(PortletRequest request,
										PortletResponse response, String cmd) {

				try {
						if (cmd.equals(SliderConstants.DELETE)) {
								deleteSlide(request, response);
						}
						else if (cmd.equals(SliderConstants.SLIDE_MOVE_DOWN)) {
								updateSlideOrder(request, response, true);
						}
						else if (cmd.equals(SliderConstants.SLIDE_MOVE_UP)) {
								updateSlideOrder(request, response, false);
						}

						SessionMessages.add(request, "request-successfully");
				}
				catch (Exception e) {
						_log.error(e.getMessage());
						SessionErrors.add(request, e.getMessage());
				}
		}

		private void updateSlideOrder(PortletRequest request,
										PortletResponse response,
										boolean slideDown) throws Exception {

				String slideIdParam = ParamUtil.getString(request, "slideId",
						null);

				_log.info("updateSlideOrder - slideId=" + slideIdParam);

				if (Validator.isNull(slideIdParam)) {
						throw new IllegalArgumentException("exception-occurred");
				}

				Long slideId = SliderUtil.getSlideId(slideIdParam);

				List<Slide> slides = SliderUtil.getSlides(request, response);

				for (Slide slide : slides) {

						if (slide.getId() == slideId) {

								Slide nextSlide = null;
								int indexOf = slides.indexOf(slide);
								int nextSlideIndex = slideDown ? indexOf + 1
										: indexOf - 1;

								if (nextSlideIndex >= 0
									&& nextSlideIndex < slides.size()) {
										nextSlide = slides.get(nextSlideIndex);
										if (nextSlide != null)
												switchSlide(request, response,
														slide, nextSlide);
								}

								break;
						}
				}

		}

		private void switchSlide(PortletRequest request,
										PortletResponse response, Slide slide,
										Slide nextSlide) throws Exception {

				int slideOrder = slide.getOrder();
				int nextSlideOrder = nextSlide.getOrder();

				// exchange slide order
				saveSlideOrder(request, response, slide, nextSlideOrder);

				saveSlideOrder(request, response, nextSlide, slideOrder);
		}

		private void updateSettings(ActionRequest actionRequest,
										ActionResponse actionResponse)
										throws Exception {

				_log.info("updateAnimation - start");

				String portletResource = ParamUtil.getString(actionRequest,
						"portletResource");
				PortletPreferences preferences = PortletPreferencesFactoryUtil
												.getPortletSetup(actionRequest,
														portletResource);

				Enumeration<String> parameterNames = actionRequest
												.getParameterNames();

				while (parameterNames.hasMoreElements()) {

						String param = parameterNames.nextElement();
						if (param.startsWith("settings")) {

								String value = ParamUtil.get(actionRequest,
										param, "");
								_log.info("save param=" + param + ", value="
										  + value);

								preferences.setValue(param, value);
						}
				}

				preferences.store();

				_log.info("updateAnimation - end");

		}

		private void deleteSlide(PortletRequest request,
										PortletResponse response)
										throws Exception {

				String slideId = ParamUtil.getString(request, "slideId", null);

				_log.info("deleteSlide - slideId=" + slideId);

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

				int order = SliderUtil.getLastSlide(request, response);

				if (_log.isDebugEnabled()) {
						_log.debug("savePreferences - order=" + order);
				}

				String[] values = new String[] { title, link, desc, image, String
												.valueOf((new Date().getTime())), String
												.valueOf(order) };

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

		private void saveSlideOrder(PortletRequest request,
										PortletResponse response, Slide slide,
										int order) throws Exception {

				String portletResource = ParamUtil.getString(request,
						"portletResource");

				PortletPreferences portletPreferences = PortletPreferencesFactoryUtil
												.getPortletSetup(request,
														portletResource);

				String slideId = "slides_" + slide.getId();
				String title = slide.getTitle();
				String link = slide.getLink();
				String desc = slide.getDesc();
				String image = slide.getImageUrl();

				_log.info("savePreferences - slideId=" + slideId + ", title="
						  + title + ", link=" + link + ", desc=" + desc
						  + ", image=" + image + ", order=" + order);

				String[] values = new String[] { title, link, desc, image, String
												.valueOf(slide.getTimeMillis()), String
												.valueOf(order) };

				portletPreferences.setValues(slideId, values);
				portletPreferences.store();
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