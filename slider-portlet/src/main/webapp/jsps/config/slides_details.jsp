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
  
<%@page import="com.liferay.portal.kernel.portlet.LiferayWindowState"%>
<%@include file="/init.jsp" %>

<aui:fieldset label="slides" cssClass="slides">
<%
	 Locale  locale = renderRequest.getLocale();
								 
	List<String> headerNames = new ArrayList<String>();
	headerNames.add(LanguageUtil.get(locale, "title"));
	headerNames.add(LanguageUtil.get(locale, "order"));
	headerNames.add(LanguageUtil.get(locale, "action"));
	
	PortletURL portletURL = renderResponse.createRenderURL();
	
	// create search container, used to display table
	SearchContainer searchContainer = new SearchContainer(renderRequest, null, null,
			SearchContainer.DEFAULT_CUR_PARAM, SearchContainer.DEFAULT_DELTA, portletURL, headerNames,
			"There No Records");
	
	portletURL.setParameter(searchContainer.getCurParam(), String.valueOf(searchContainer.getCurValue()));
		
	List<Slide> slides = SliderUtil.getSlides(renderRequest, resourceResponse);
	int count  = slides.size();
	
	searchContainer.setTotal(count);
	
	// fill table
	List<ResultRow> resultRows = searchContainer.getResultRows();
		
	for (int i = 0; i < slides.size(); i++) {
			
			Slide slide = slides.get(i);
			
			ResultRow row = new ResultRow(slide, slide.getId(), 1);
			
			row.addText(slide.getTitle());
			row.addText(String.valueOf(slide.getOrder()));
					
			row.addJSP("center", SearchEntry.DEFAULT_VALIGN, "/jsps/config/slide_action.jsp",
					config.getServletContext(), request, response);

			resultRows.add(row);
	}	
%>
<liferay-ui:search-iterator searchContainer="<%= searchContainer %>" />

<liferay-portlet:renderURL portletConfiguration="true" var="addSlideURL"></liferay-portlet:renderURL>

<aui:button-row>
	<aui:button href="<%=addSlideURL%>" value="add-slide"></aui:button>
</aui:button-row>

</aui:fieldset>

<style>
.slides .aui-legend{
	border-bottom: 0px none !important; 
}
.slides .results-header th.last{
	text-align: center;
}
</style>

<aui:script>
	function confirmDeleteSlide(){
		if (confirm('<liferay-ui:message key="are-you-sure-you-want-to-delete-slide"/>')){
		 	return true;
		}else{
			self.focus(); 
			return false;
		}
		
		return false;
	}
</aui:script>		