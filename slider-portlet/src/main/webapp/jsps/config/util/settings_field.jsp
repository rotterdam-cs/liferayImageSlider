<%@include file="/init.jsp" %>

<%
	String fieldName = (String)request.getAttribute("slide-name");
	String fieldValue = (String)request.getAttribute("slide-value");
	String property = (String)request.getAttribute("slide-property");
	String desc = "desc-"  + fieldName;
%>

<aui:layout cssClass="slide-field-wrapper">
		<aui:column columnWidth="20" first="true">
			<liferay-ui:message key="<%=fieldName%>"></liferay-ui:message>
		</aui:column>
		
		<aui:column columnWidth="35">
			<% if(property != null && !property.trim().equals("")){ %>
				<aui:select name="<%=fieldName%>" label="">
					<%					
						String[] options = PortletProps.getArray(property);
						for (String option: options) {
						%>
							<aui:option value="<%= option.trim() %>" label="<%= option.trim() %>" 
									selected="<%= (option.trim().equals(fieldValue)) %>" />
						<%
						}
					%>
				</aui:select>
			<% } else { %>
				<aui:input name="<%=fieldName%>" label="" value="<%=fieldValue%>"></aui:input>
			<% } %>			
		</aui:column>		
		<aui:column columnWidth="45" last="true">
			<liferay-ui:message key="<%=desc%>"></liferay-ui:message>
		</aui:column>
</aui:layout>

<%
	request.removeAttribute("slide-name");
	request.removeAttribute("slide-value");
	request.removeAttribute("slide-property");
%>
	
