<%@ page import="com.rcs.portlet.slider.util.webcontent.SliderArticleUtil" %>
<%@ page import="com.rcs.portlet.slider.util.webcontent.SliderArticle" %>
<%--/*=== new version ===*/ --%>
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
	long plid = themeDisplay.getPlid();
	String portletId = themeDisplay.getPortletDisplay().getId();
	String doAsUserId = String.valueOf(themeDisplay.getUserId());
	String doAsGroupId = String.valueOf(themeDisplay.getScopeGroupId());
    String languageId = themeDisplay.getLanguageId();
		
	String connectorURL = themeDisplay.getURLPortal()
			 + "/html/js/editor/ckeditor/editor/filemanager/browser/liferay/browser.html?Connector=";
	
	String resourceSelectorParam = "/c/portal/fckeditor?p_l_id=" + plid 
			 + "&p_p_id=" + HttpUtil.encodeURL(portletId)
			 + "&userId=" + HttpUtil.encodeURL(doAsUserId)
			 + "&doAsGroupId=" + HttpUtil.encodeURL(doAsGroupId);
	
	connectorURL += HttpUtil.encodeURL(resourceSelectorParam);
		
	String slideId = request.getParameter("slideParamId") != null ? request.getParameter("slideParamId") : "";
						
	String slideImage = "";				
	Slide slide = null;
	if(Validator.isNotNull(slideId)){
			slide = SliderUtil.getSlide(renderRequest, slideId);
			slideImage = slide.getImageUrl();
	}else{
			slide = new Slide();
	}


%>

<aui:model-context bean="<%= slide %>" model="<%= Slide.class %>" />
			
<liferay-portlet:actionURL portletConfiguration="true" var="actionURL" />
<% List<SliderArticle> sliderArticles = SliderArticleUtil.getAllSliderArticles(renderRequest); %>
<aui:form action="<%= actionURL.toString() %>" method="post">
    <aui:input name="slideId" type="hidden" value="<%=slideId%>"/>
    <aui:input name="<%= SliderConstants.CMD %>" type="hidden" value="<%=SliderConstants.UPDATE%>"/>
    <aui:fieldset label="slide.detail">
        <aui:select name="web-content">
            <%
                for (SliderArticle sliderArticle : sliderArticles) {
                 %>
                    <aui:option value="<%= sliderArticle.getArticleId() %>">
                        <%= sliderArticle.getTitle(languageId) %>
                    </aui:option>
                <%
                }
            %>
        </aui:select>
        <aui:button-row>
            <aui:button name="saveButton" cssClass="save-btn" type="submit" value="add-slide"/>
        </aui:button-row>
    </aui:fieldset>
</aui:form>
			
<script type="text/javascript">
	var CKEDITOR={
			 tools : {
				callFunction: function(funcNum, fileUrl){
					$('#imagePreview').attr('src' ,fileUrl);
					$('#<portlet:namespace/>slideImage').val(fileUrl);
				}
			}
	};
	function selectImage(){
			window.open('<%=connectorURL%>',"mywindow","scroll=1,status=1,menubar=1,width=700,height=550");
	}				
</script>

<style type="text/css">
	.input-text,textarea {
		margin: 0px 0px 10px;
	}
</style>

