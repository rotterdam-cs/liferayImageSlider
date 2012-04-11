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
ResultRow row = (ResultRow)request.getAttribute(WebKeys.SEARCH_CONTAINER_RESULT_ROW);

Slide slide = (Slide)row.getObject();

String slideId = "slides_" + slide.getId();
%>

<liferay-portlet:renderURL portletConfiguration="true" var="slideUpURL" >
	<liferay-portlet:param name="slideId" value="<%=slideId%>" />
	<liferay-portlet:param name="<%=SliderConstants.CMD %>" value="<%=SliderConstants.SLIDE_MOVE_UP%>" />
	<liferay-portlet:param name="tab" value="<%=SliderConstants.TAB_SLIDES%>" />
</liferay-portlet:renderURL>
		
<liferay-ui:icon
		image="top" message="slide-up"
		url="<%= slideUpURL.toString() %>"
/>

<liferay-portlet:renderURL portletConfiguration="true" var="slideDownURL" >
	<liferay-portlet:param name="slideId" value="<%=slideId%>" />
	<liferay-portlet:param name="<%=SliderConstants.CMD %>" value="<%=SliderConstants.SLIDE_MOVE_DOWN%>" />
	<liferay-portlet:param name="tab" value="<%=SliderConstants.TAB_SLIDES%>" />
</liferay-portlet:renderURL>

<liferay-ui:icon
		image="bottom" message="slide-down"
		url="<%= slideDownURL.toString() %>"
/>

<liferay-portlet:renderURL portletConfiguration="true" var="updateURL" >
	<liferay-portlet:param name="slideParamId" value="<%=slideId%>" />
</liferay-portlet:renderURL>

<liferay-ui:icon
		image="edit"
		url="<%= updateURL %>"
/>

<liferay-portlet:renderURL portletConfiguration="true" var="deleteURL" >
	<liferay-portlet:param name="slideId" value="<%=slideId%>" />
	<liferay-portlet:param name="<%=SliderConstants.CMD %>" value="<%=SliderConstants.DELETE%>" />
	<liferay-portlet:param name="tab" value="<%=SliderConstants.TAB_SLIDES%>" />
</liferay-portlet:renderURL >

<span> 
	<a href="<%=deleteURL.toString() %>" onclick="return confirmDeleteSlide()" class="taglib-icon">
	 <img title="Delete"
	  alt="Delete" src="/html/themes/classic/images/common/delete.png" 
	  class="icon">
	  </a> 
</span>

<!-- liferay-ui:icon
		image="delete" label=""
		url="deleteURL"
/ -->


