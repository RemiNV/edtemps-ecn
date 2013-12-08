<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>

<%@page import="java.util.ArrayList" %>
<%@page import="org.ecn.edtemps.managers.BddGestion" %>    
<%@page import="org.ecn.edtemps.diagnosticbdd.*" %>
<%@page import="org.ecn.edtemps.diagnosticbdd.TestBdd.TestBddResult" %>
<%@page import="org.ecn.edtemps.diagnosticbdd.TestBdd.TestBddResultCode" %>
<!DOCTYPE html>
<html>
	<head>
		<meta charset="utf-8" />
		<title>Espace d'administration</title>
		<link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/admin/main.css" />
	</head>
	
	<body>

		<jsp:include page="/admin/includes/menu.jsp" />

		<div id="main_content">
			<h1>Espace d'administration &rarr; Général</h1>
			
			<div id="content">
				<h2>Diagnostic de la base de données</h2>
				<table class="tableau_liste">
					<tr>
						<th>ID</th>
						<th>Libellé</th>
						<th>Résultat</th>
						<th>Réparation</th>
					</tr>
					<%
					BddGestion bdd = new BddGestion();
					
					DiagnosticsBdd diagnostics = new DiagnosticsBdd(bdd);
					
					ArrayList<TestBddResult> results = diagnostics.runAllTests();
					bdd.close();
					
					for(TestBddResult res : results) {
						
						%>
						<tr>
							<td><%= res.getTest().getId() %></td>
							<td><%= res.getTest().getNom() %></td>
							<td class="<%= res.getResultCode().getLabel() %>"><%= res.getMessage() %></td>
							
						<%
						if(res.getResultCode() == TestBddResultCode.OK || res.getResultCode() == TestBddResultCode.TEST_FAILED) {
						%>
							<td></td>
						<%
						}
						else {
						%>
							<td>
								<form method="post" action="<%= request.getContextPath() %>/administrateur/reparer" class="form_reparer">
									<input type="hidden" name="id" value="<%= res.getTest().getId() %>" />
									<input type="submit" value="Réparer" title="<%= res.getTest().getRepairMessage() %>" class="button" />
								</form>
							</td>
						<%
						}
					}
					%>
				</table>
			</div>

		</div>

	</body>
</html>
