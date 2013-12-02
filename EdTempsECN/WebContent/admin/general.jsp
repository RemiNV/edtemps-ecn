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
					for(TestBddResult res : results) {
						
						String lienReparation = res.getResultCode() == TestBddResultCode.OK ? "" : 
							"<a href='" + request.getContextPath() + "/admin/diagnostics/reparation.jsp?id=" + res.getTest().getId() + 
							"' title='" + res.getTest().getRepairMessage() + "'>Réparer</a>";
						
						out.write("<tr><td>" + res.getTest().getId() + "</td>\n" + 
								"<td>" + res.getTest().getNom() + "</td>\n" + 
								"<td class='" + res.getResultCode().getLabel() + "'>" + res.getMessage() + "</td>\n" + 
								"<td>" + lienReparation + "</td></tr>");
						// TODO : mettre un truc correct pour le lien de réparation
					}
					%>
				</table>
			</div>

		</div>

	</body>
</html>
