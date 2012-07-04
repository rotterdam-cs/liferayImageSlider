/*=== new version ===*/
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

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.portlet.ConfigurationAction;
import com.liferay.portal.kernel.servlet.SessionErrors;
import com.liferay.portal.kernel.servlet.SessionMessages;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.theme.ThemeDisplay;
import com.liferay.portlet.PortletPreferencesFactoryUtil;
import com.rcs.portlet.slider.model.Slide;
import com.rcs.portlet.slider.util.SliderUtil;
import com.rcs.portlet.slider.util.webcontent.SliderArticle;
import com.rcs.portlet.slider.util.webcontent.SliderArticleUtil;

import javax.portlet.*;
import java.util.*;

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

        private void deleteSlide(PortletRequest request, PortletResponse response) throws Exception {

            String slideId = ParamUtil.getString(request, "slideId", null);

            _log.info("deleteSlide - slideId=" + slideId);

            if (Validator.isNotNull(slideId)) {
                String portletResource = ParamUtil.getString(request, "portletResource");

                PortletPreferences preferences = PortletPreferencesFactoryUtil.getPortletSetup(request, portletResource);

                Enumeration<String> names = preferences.getNames();
                while (names.hasMoreElements()) {

                    String name = names.nextElement();
                    if (name.startsWith(slideId)) {
                        preferences.reset(name);
                        preferences.store();
                    }

                }

            } else {
                throw new Exception("invalid-slide");
            }

        }

		private void savePreferences(ActionRequest request,
										ActionResponse response)
										throws Exception {

				String portletResource = ParamUtil.getString(request, "portletResource");

				PortletPreferences portletPreferences = PortletPreferencesFactoryUtil.getPortletSetup(request, portletResource);

                ThemeDisplay themeDisplay = (ThemeDisplay) request.getAttribute(WebKeys.THEME_DISPLAY);

                long groupId = themeDisplay.getScopeGroupId();

                String articleId = ParamUtil.getString(request, "web-content");

                SliderArticle sliderArticle = null;
                if (articleId != null && !"".equals(articleId)) {
                    sliderArticle = SliderArticleUtil.getSliderArticle(articleId, groupId);
                }

                if (sliderArticle != null) {

                    /*=== update available languages ===*/
                    String[] availableLanguages = sliderArticle.getLocales();
                    String[] preferenceLanguages = portletPreferences.getValues(SliderConstants.LANGUAGES, new String[]{SliderArticle.defaultLanguageId});
                    List<String> preferencesLanguageList = new ArrayList<String>();
                    for (String preferenceLanguage: preferenceLanguages) {
                        preferencesLanguageList.add(preferenceLanguage);
                    }
                    for (String availableLanguage: availableLanguages) {
                        if (!preferencesLanguageList.contains(availableLanguage)) {
                            preferencesLanguageList.add(availableLanguage);
                        }
                    }
                    int listSize = preferencesLanguageList.size();
                    String[] updatedLanguages = new String[listSize];
                    int i = 0;
                    for (String preferencesLanguage: preferencesLanguageList) {
                        updatedLanguages[i] = preferencesLanguage;
                        i++;
                    }
                    portletPreferences.setValues(SliderConstants.LANGUAGES, updatedLanguages);
                    portletPreferences.store();
                    /*=== update available languages end ===*/

                    int currentOrder = SliderUtil.getLastSlide(request, response);

                    for (String currentLanguageId: availableLanguages) {

                        String currentTitle = sliderArticle.getTitle(currentLanguageId);
                        String currentText = sliderArticle.getText(currentLanguageId);
                        String currentLink = sliderArticle.getLink(currentLanguageId);
                        String currentImage = sliderArticle.getImage(currentLanguageId);

                        String[] currentValues = new String[] { currentTitle, currentLink, currentText, currentImage,
                                articleId, String.valueOf(currentOrder)};

                        String currentSlideId = "slides_" + articleId + "_" + currentLanguageId;

                        portletPreferences.setValues(currentSlideId, currentValues);
                        portletPreferences.store();
                    }
                }
		}

    public void updateAvailableLocales(String[] availableLanguages, PortletPreferences preferences){


    }

    private void saveSlideOrder(PortletRequest request,
                                PortletResponse response, Slide slide,
                                int order) throws Exception {

        String portletResource = ParamUtil.getString(request, "portletResource");

        PortletPreferences preferences = PortletPreferencesFactoryUtil.getPortletSetup(request, portletResource);

        String[] languages = preferences.getValues(SliderConstants.LANGUAGES, new String[]{SliderArticle.defaultLanguageId});

        for (String languageId : languages) {

            String slideId = "slides_" + slide.getId() + "_" + languageId;

            Slide currentSlide = SliderUtil.getSlide(request, slideId);

            String title = currentSlide.getTitle();
            String link = currentSlide.getLink();
            String desc = currentSlide.getDesc();
            String image = currentSlide.getImageUrl();

            if (isValid(title, image)) {

                _log.info("savePreferences - slideId=" + slideId + ", title="
                        + title + ", link=" + link + ", desc=" + desc
                        + ", image=" + image + ", order=" + order);

                String[] values = new String[]{title, link, desc, image, String
                        .valueOf(slide.getTimeMillis()), String
                        .valueOf(order)};

                preferences.setValues(slideId, values);
                preferences.store();

            }
        }
    }

    private boolean isValid(String title, String image){
        return Validator.isNotNull(title) && Validator.isNotNull(image);
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