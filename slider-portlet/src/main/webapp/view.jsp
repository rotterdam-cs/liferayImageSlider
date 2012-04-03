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

<portlet:defineObjects />

<style>
.theme-default #slider {
    margin:0 auto;
    width:618px; /* Make sure your images are the same size */
    height:246px; /* Make sure your images are the same size */
}
</style>
 
<%
		List<Slide> slides = SliderUtil.getSlides(renderRequest, renderResponse);
		boolean displaySlide = (slides != null && slides.size() > 0);
		StringBuilder slidesBuilder = new StringBuilder();

		for(Slide slide: slides){
		
				if(Validator.isNotNull(slide.getLink())){
						slidesBuilder.append("<a href=\"");
						slidesBuilder.append(slide.getLink());
						slidesBuilder.append("\">");
				}
				
				slidesBuilder.append("<img src=\"");
				slidesBuilder.append(slide.getImageUrl());
				slidesBuilder.append("\" ");
				
				if(Validator.isNotNull(slide.getDesc())){
						slidesBuilder.append(" title=\"");
						slidesBuilder.append(slide.getDesc());
						slidesBuilder.append("\" ");
				}
				
				slidesBuilder.append("/>");
				
				if(Validator.isNotNull(slide.getLink())){
						slidesBuilder.append("</a>");
				}
		}//end slides for
%>

<%if(displaySlide){%>
	<%@ include file="/jsps/slider/nova_init.jsp"%>
	<div class="slider-wrapper theme-default">
	      <div class="ribbon"></div>
	      <div id="slider" class="nivoSlider">
	      	<%=slidesBuilder.toString()%>
		  </div>
	</div>
<%}else{%>
	<center><b> No slides configured, configure slides using config options visible via admin </b></center>
<% } %>


 <%--
 	<img src="/slider-portlet/images/toystory.jpg" alt="" />
   <a href="http://dev7studios.com"><img src="/slider-portlet/images/up.jpg"
   	 alt="" title="This is an example of a caption" /></a>
  <img src="/slider-portlet/images/walle.jpg" alt="" data-transition="slideInLeft" />
  <img src="/slider-portlet/images/nemo.jpg" alt="" title="#htmlcaption" />
  --%> 