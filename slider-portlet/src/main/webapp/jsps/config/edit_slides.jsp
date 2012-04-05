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

<liferay-ui:error key="image-invalid" message="image-invalid" />
<liferay-ui:error key="title-invalid" message="title-invalid" />
<liferay-ui:error key="invalid-slide" message="invalid-slide" />

<br />
 
<aui:layout cssClass="slide-layout">
	<aui:column columnWidth="50" first="true">
		<jsp:include page="/jsps/config/slides_details.jsp"></jsp:include>		
	</aui:column>
	<aui:column columnWidth="50" last="true">
		<jsp:include page="/jsps/config/add_slides.jsp"></jsp:include>
	</aui:column>
</aui:layout>

<br />
<br />