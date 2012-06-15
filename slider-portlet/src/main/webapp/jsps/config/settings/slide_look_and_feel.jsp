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
<%@include file="/init.jsp" %>

<%

	String portletResource = ParamUtil.getString(renderRequest, "portletResource");
								 
	PortletPreferences preferences = SliderUtil
										.getPreference(renderRequest, portletResource);

	String themeValue = preferences.getValue(SliderParamUtil.SETTINGS_THEME, "default");
	String opacityValue = preferences.getValue(SliderParamUtil.SETTINGS_OPACTIY, "0.8");
	String addCssClassValue = preferences.getValue(SliderParamUtil.SETTINGS_ADDITIONAL_CSS_CLASS, "");
	String widthValue = preferences.getValue(SliderParamUtil.SETTINGS_SLIDE_WIDTH, "618");
	String heightValue = preferences.getValue(SliderParamUtil.SETTINGS_SLIDE_HEIGHT, "246");
	
	
%>

<liferay-portlet:actionURL portletConfiguration="true" var="actionURL" />

<aui:fieldset label="slide.animation">

	<aui:form action="<%=actionURL.toString()%>" method="post" name="fm">

			<aui:input name="<%=SliderConstants.CMD%>" type="hidden" 
					value="<%=SliderConstants.UPDATE_SETTINGS%>" />
			<aui:input name="tab" type="hidden" 
					value="<%=SliderConstants.TAB_SLIDES_LOOK_AND_FEEL%>" />		
			
			<%
				request.setAttribute("slide-name", SliderParamUtil.SETTINGS_THEME);
				request.setAttribute("slide-value", themeValue);
				request.setAttribute("slide-property", "slider-theme");
			%>	
			<jsp:include page="/jsps/config/util/settings_field.jsp"></jsp:include>
			
			<%
				request.setAttribute("slide-name", SliderParamUtil.SETTINGS_OPACTIY);
				request.setAttribute("slide-value", opacityValue);
				request.setAttribute("slide-property", "slider-opacity");
			%>	
			<jsp:include page="/jsps/config/util/settings_field.jsp"></jsp:include>
			
			<%
				request.setAttribute("slide-name", SliderParamUtil.SETTINGS_ADDITIONAL_CSS_CLASS);
				request.setAttribute("slide-value", addCssClassValue);
			%>	
			<jsp:include page="/jsps/config/util/settings_field.jsp"></jsp:include>
			
			<%
				request.setAttribute("slide-name", SliderParamUtil.SETTINGS_SLIDE_WIDTH);
				request.setAttribute("slide-value", widthValue);
			%>	
			<jsp:include page="/jsps/config/util/settings_field.jsp"></jsp:include>
			
			<%
				request.setAttribute("slide-name", SliderParamUtil.SETTINGS_SLIDE_HEIGHT);
				request.setAttribute("slide-value", heightValue);
			%>	
			<jsp:include page="/jsps/config/util/settings_field.jsp"></jsp:include>
								
			<aui:button-row>
				<aui:button name="saveButton" cssClass="save-btn" type="submit"
				value="save" />				
			</aui:button-row>
	</aui:form>
</aui:fieldset>

<style>
.slide-field-wrapper{
	width: 600px !important;
}
.aui-field-content{
	margin: 0px !important;
}
.ltr .aui-column, .rtl .aui-column-last{
	margin: 10px 0;
}
</style>