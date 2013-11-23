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
	</head>
	
	<body>

		<jsp:include page="../includes/menu.jsp" />

		<div id="main_content">
			<h1>Espace d'administration &rarr; Gestion des utilisateurs</h1>			

			<div id="content">

				<table id="liste_utilisateurs">
					<tr>
						<th>Prénom</th>
						<th class='bordure_gauche_blanche'>Nom</th>
						<th class='bordure_gauche_blanche'>Mail</th>
						<th class='bordure_gauche_blanche'>Type d'utilisateur</th>
					</tr>
					<%
						BddGestion bdd = new BddGestion();
						UtilisateurGestion gestionUtilisateurs = new UtilisateurGestion(bdd);
						List<UtilisateurIdentifie> listeUtilisateurs = gestionUtilisateurs.getListeUtilisateurs();
						for (UtilisateurIdentifie utilisateur : listeUtilisateurs) {
							out.write("<tr>");
							out.write("<td>"+utilisateur.getPrenom()+"</td>");
							out.write("<td class='bordure_gauche_grise'>"+utilisateur.getNom()+"</td>");
							out.write("<td class='bordure_gauche_grise'>"+(utilisateur.getEmail()==null ? "-" : utilisateur.getEmail() )+"</td>");
							out.write("<td class='bordure_gauche_grise'>"+(utilisateur.getType()==0 ? "-" : utilisateur.getType())+"</td>");
							out.write("</tr>");
						}
					%>
				</table>
				
			</div>

		</div>

	</body>
</html>
