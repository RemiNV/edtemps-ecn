<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>

<%@page import="org.ecn.edtemps.exceptions.DatabaseException" %>
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
			<h1>Espace d'administration &rarr; Réparation de la base</h1>
			
			<div id="content">
				<h2>
				<% 
				BddGestion bdd = new BddGestion();
				DiagnosticsBdd diagnostics = new DiagnosticsBdd(bdd);
				
				String idDiagnostic = request.getParameter("id");
				Integer id = null;
				if(idDiagnostic != null) {
					try {
						id = Integer.parseInt(idDiagnostic);
					}
					catch(NumberFormatException e) { } // id == null
				}
				
				TestBdd testBdd = null;
				if(id != null) {
					testBdd = diagnostics.createTest(id);
				}
				
				if(testBdd != null) {
					out.write("Réparation de : " + testBdd.getNom());
				}
				else {
					out.write("ID de réparation non fourni ou invalide");
				}
				%>
				</h2>
				<p>
				<%
				try {
					testBdd.repair(bdd);
					// TODO : compléter et faire un truc moins moche (avec un servlet)
				}
				catch(DatabaseException e) {
					out.write("Erreur lors de l'exécution du test : " + e.getMessage());
					
				}
				%>
				</p>
			</div>

		</div>

	</body>
</html>
