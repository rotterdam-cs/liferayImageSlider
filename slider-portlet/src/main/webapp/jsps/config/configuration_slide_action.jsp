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

<liferay-portlet:renderURL portletConfiguration="true" var="updateURL" >
	<liferay-portlet:param name="slideParamId" value="<%=slideId%>" />
</liferay-portlet:renderURL>

<liferay-ui:icon
		image="edit" label=""
		url="<%= updateURL %>"
/>
		
<%

StringBuilder confirmJsSb = new StringBuilder(5);
confirmJsSb.append("javascript:if (confirm('");
confirmJsSb.append(
	UnicodeLanguageUtil.get(
		pageContext, "are-you-sure-you-want-to-delete-slide"));
confirmJsSb.append("')) { ");
confirmJsSb.append("deleteSlide('" + slideId + "')");
confirmJsSb.append(" } else { self.focus(); }");

%>
<span> 
	<a href="<%=confirmJsSb.toString() %>" class="taglib-icon">
	 <img title="Delete"
	  alt="Delete" src="/html/themes/classic/images/common/delete.png" 
	  class="icon">
	  </a> 
</span>

<!-- liferay-ui:icon
		image="delete" label=""
		url="deleteURL"
/ -->


