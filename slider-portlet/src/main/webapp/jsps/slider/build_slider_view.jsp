<%--
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
 --%>
 <%@ include file="/init.jsp"%>
<%		
		String slidesBuilder = SliderUtil.buildSlides(renderRequest, renderResponse);
		String buildSettings = SliderUtil.buildSettings(renderRequest, renderResponse);
		boolean displaySlide = (slidesBuilder != null && !slidesBuilder.trim().equals(""));

		//Slides Themes
		PortletPreferences preferences = SliderUtil
											.getPreference(renderRequest, null);
		
		String themeValue = preferences.getValue(SliderParamUtil.SETTINGS_THEME, "default");
		String addCssClassValue = preferences.getValue(SliderParamUtil.SETTINGS_ADDITIONAL_CSS_CLASS, "");
		String widthValue = preferences.getValue(SliderParamUtil.SETTINGS_SLIDE_WIDTH, "618");
		String heightValue = preferences.getValue(SliderParamUtil.SETTINGS_SLIDE_HEIGHT, "246");
		
		if(Validator.isNull(widthValue) ||  !Validator.isNumber(widthValue))
				widthValue = "618";
		if(Validator.isNull(heightValue) ||  !Validator.isNumber(heightValue))
				heightValue = "246";
		themeValue = themeValue.toLowerCase();
%>
 
<style>
	#slider {
	    margin:0 auto;
	    width:<%=widthValue%>px; /* Make sure your images are the same size */
	    height:<%=heightValue%>px; /* Make sure your images are the same size */
	}
</style>
 
<%if(displaySlide){ %>
	<div class="slider-wrapper theme-<%=themeValue%> <%=addCssClassValue%>">
	      <div class="ribbon"></div>
	      <div id="slider" class="nivoSlider">
	      	<%=slidesBuilder%>
		  </div>
	</div>
<%}else{%>
	<center><b><liferay-ui:message key="no-slides-configured-message"></liferay-ui:message></b></center>
<% } %>

<link rel="stylesheet" href="<%=renderRequest.getContextPath()%>/css/<%=themeValue%>/<%=themeValue%>.css" type="text/css" media="screen" />
<link rel="stylesheet" href="<%=renderRequest.getContextPath()%>/css/nivo-slider.css" type="text/css" media="screen" />

<%@include file="/jsps/config/js/jquery-1.7.2.min.js.jsp" %>

<script type="text/javascript" src="<%=renderRequest.getContextPath()%>/js/jquery.nivo.slider.pack.js"></script>

<script type="text/javascript">
	$(window).load(function() {
	    $('#slider').nivoSlider({<%=buildSettings%>});
	});
</script>
