<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>

<%@page import="org.ecn.edtemps.diagnosticbdd.TestBdd" %>
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
			<h1>Espace d'administration &rarr; R�paration de la base</h1>
			
			<div id="content">
				<h2>
				<% 
				TestBdd testBdd = (TestBdd) request.getAttribute("test");
				if(testBdd != null) {
					out.write("R�paration de : " + testBdd.getNom());
				}
				else {
					out.write("ID de r�paration non fourni ou invalide");
				}
				%>
				</h2>
				<p>
				<%
				String resultatReparation = (String) request.getAttribute("resultatReparation");
				if(resultatReparation != null) {
					out.write("R�sultat de la r�paration : " + resultatReparation);
				}
				%>
				</p>
			</div>

		</div>

	</body>
</html>
