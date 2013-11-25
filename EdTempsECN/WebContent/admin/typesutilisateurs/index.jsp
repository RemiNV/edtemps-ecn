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
		<link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/multi-select.css" />
		<script type="text/javascript" src="<%=request.getContextPath()%>/scripts/lib/jquery-1.10.2.min.js"></script>
		<script type="text/javascript" src="<%=request.getContextPath()%>/scripts/lib/jquery.multi-select.js"></script>
		<script type="text/javascript" src="<%=request.getContextPath()%>/admin/scripts/typesutilisateurs.js"></script>
	</head>
	
	<body>

		<jsp:include page="../includes/menu.jsp" />

		<div id="main_content">
			<h1>Espace d'administration &rarr; Gestion des types d'utilisateurs</h1>			

			<div id="content" style="min-height: 320px;">

				<table id="types_utilisateurs_liste" class="tableau_liste">
					<tr>
						<th>Types d'utilisateurs</th>
						<th colspan="2">Actions</th>
					</tr>
					<%
						BddGestion bdd = new BddGestion();
						UtilisateurGestion gestionUtilisateurs = new UtilisateurGestion(bdd);
						Map<Integer, String> typesUtilisateurs = gestionUtilisateurs.getListeTypesUtilisateur();
						for (Map.Entry<Integer, String> type : typesUtilisateurs.entrySet()) {
							out.write("<tr>");
							out.write("<td>"+type.getValue()+"</td>");
							out.write("<td class='liste_types_utilisateurs_modifier'><a href='"+request.getContextPath()+"/admin/typesutilisateurs/modifier.jsp?id="+type.getKey()+"'><img alt='Modifier' title='Modifier' src='"+request.getContextPath()+"/img/modifier.png' /></a></td>");
							out.write("<td class='liste_types_utilisateurs_supprimer'><form onsubmit='return confirmationSupprimerType()' action='"+request.getContextPath()+"/administrateur/typesutilisateurs/supprimer' method='POST'><input src='"+request.getContextPath()+"/img/supprimer.png' type='image' title='Supprimer' /><input type='hidden' name='id' value='"+type.getKey()+"' /></form></td>");
							out.write("</tr>");
						}
					%>
				</table>
				
			</div>

		</div>

	</body>
</html>
