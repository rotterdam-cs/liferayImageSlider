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

<script type="text/javascript" src="/slider-portlet/js/jquery-1.7.1.min.js"></script>

<%
	String tabValue = ParamUtil.getString(request, "tab", "slides");
%>

<liferay-portlet:renderURL portletConfiguration="true" var="slidesURL">
</liferay-portlet:renderURL>

<liferay-ui:tabs
    names="Slides,Settings" 
    value="<%=tabValue%>"
    param="tab"
    url="<%= slidesURL %>"
    tabsValues="slides,settings"
/>

<liferay-ui:success key="request-successfully" message="request-successfully" />
<liferay-ui:error key="exception-occurred" message="exception-occurred" />

<% if(tabValue.equalsIgnoreCase("Settings")){ %>
	 <%@include file="/jsps/config/slide_settings.jsp"%>
<% }else{ %>
	<%@include file="/jsps/config/edit_slides.jsp"%>
<% } %>




