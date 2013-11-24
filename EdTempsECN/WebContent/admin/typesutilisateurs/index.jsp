<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>

<%@page import="org.ecn.edtemps.managers.*"%>
<%@page import="org.ecn.edtemps.models.*"%>
<%@page import="org.ecn.edtemps.models.identifie.*"%>
<%@page import="java.util.*"%>

<!DOCTYPE html>
<html>
	<head>
		<meta charset="utf-8" />
		<title>Espace d'administration</title>
		<link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/admin/main.css" />
		<script type="text/javascript" src="<%=request.getContextPath()%>/scripts/lib/jquery-1.10.2.min.js"></script>
		<script type="text/javascript" src="<%=request.getContextPath()%>/scripts/lib/jquery.multi-select.js"></script>
		<script type="text/javascript" src="<%=request.getContextPath()%>/admin/scripts/typesutilisateurs.js"></script>
	</head>
	
	<body>

		<jsp:include page="../includes/menu.jsp" />

		<div id="main_content">
			<h1>Espace d'administration &rarr; Gestion des types d'utilisateurs</h1>			

			<div id="content">

				<table id="types_utilisateurs_liste" class="tableau_liste">
					<tr><th>Types d'utilisateurs</th></tr>
					<%
						BddGestion bdd = new BddGestion();
						UtilisateurGestion gestionUtilisateurs = new UtilisateurGestion(bdd);
						Map<Integer, String> typesUtilisateurs = gestionUtilisateurs.getListeTypesUtilisateur();
						for (Map.Entry<Integer, String> type : typesUtilisateurs.entrySet()) {
							out.write("<tr><td>"+type.getValue()+"</td></tr>");
						}
					%>
				</table>

				<form action="" method="POST" onsubmit="return validationModificationType()" id="types_utilisateurs_modifier">
				</form>

				http://loudev.com/
				
			</div>

		</div>

	</body>
</html>
