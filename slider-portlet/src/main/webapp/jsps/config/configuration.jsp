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

<%@include file="/jsps/config/js/jquery-1.7.2.min.js.jsp" %>

<%
	String tabValue = ParamUtil.getString(request, "tab", "slides");
	String tabValues = SliderConstants.TAB_SLIDES 
					   + "," + SliderConstants.TAB_SLIDES_ANIMATION
					   + "," + SliderConstants.TAB_SLIDES_LOOK_AND_FEEL 
					   + "," + SliderConstants.TAB_SLIDES_NAVIGATION;
								 
	//String tabNames = LanguageUtil.get(renderRequest.getLocale(), "slider-tabs");					 
%>

<liferay-portlet:renderURL portletConfiguration="true" var="slidesURL">
</liferay-portlet:renderURL>

<liferay-ui:tabs
    names="slides,slide.animation,slide.look.and.feel,slide.navigation"
    value="<%=tabValue%>"
    param="tab"
    url="<%= slidesURL %>"
    tabsValues="<%= tabValues %>"
/>

<liferay-ui:success key="request-successfully" message="request-successfully" />
<liferay-ui:error key="exception-occurred" message="exception-occurred" />

<% if(tabValue.equalsIgnoreCase(SliderConstants.TAB_SLIDES_ANIMATION)){ %>

	<jsp:include page="/jsps/config/settings/slide_animation.jsp"></jsp:include>
	
<% }else if(tabValue.equalsIgnoreCase(SliderConstants.TAB_SLIDES_LOOK_AND_FEEL)){ %>

	<jsp:include page="/jsps/config/settings/slide_look_and_feel.jsp"></jsp:include>
	
<% }else if(tabValue.equalsIgnoreCase(SliderConstants.TAB_SLIDES_NAVIGATION)){  %>

	<jsp:include page="/jsps/config/settings/slide_nav.jsp"></jsp:include>
	
<% } else { %>

	<jsp:include page="/jsps/config/edit_slides.jsp"></jsp:include>
	
<% }  %>






