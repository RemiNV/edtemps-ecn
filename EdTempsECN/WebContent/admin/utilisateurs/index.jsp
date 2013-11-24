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
		<script type="text/javascript" src="<%=request.getContextPath()%>/admin/scripts/utilisateurs.js"></script>
	</head>
	
	<body>

		<jsp:include page="../includes/menu.jsp" />

		<div id="main_content">
			<h1>Espace d'administration &rarr; Gestion des utilisateurs</h1>			

			<div id="content">

				<table id="liste_utilisateurs">
					<tr>
						<th>Pr�nom</th>
						<th class='bordure_gauche_blanche'>Nom</th>
						<th class='bordure_gauche_blanche'>Mail</th>
						<th class='bordure_gauche_blanche'>Type d'utilisateur</th>
					</tr>
					<%
						BddGestion bdd = new BddGestion();
						UtilisateurGestion gestionUtilisateurs = new UtilisateurGestion(bdd);
						
						Map<Integer, String> typesUtilisateurs = gestionUtilisateurs.getListeTypesUtilisateur();
						StringBuilder listeDeroulanteTypes = new StringBuilder("<option value='-1'>---</option>");
						for (Map.Entry<Integer, String> type : typesUtilisateurs.entrySet()) {
							listeDeroulanteTypes.append("<option value='"+type.getKey()+"'>"+type.getValue()+"</option>");
						}
						
						List<UtilisateurIdentifie> listeUtilisateurs = gestionUtilisateurs.getListeUtilisateurs();
						for (UtilisateurIdentifie utilisateur : listeUtilisateurs) {
							out.write("<tr>");
							out.write("<td>"+utilisateur.getPrenom()+"</td>");
							out.write("<td class='bordure_gauche_grise'>"+utilisateur.getNom()+"</td>");
							out.write("<td class='bordure_gauche_grise'>"+(utilisateur.getEmail()==null ? "-" : utilisateur.getEmail() )+"</td>");
							out.write("<td class='bordure_gauche_grise'>");
							out.write("<form action='"+request.getContextPath()+"/administrateur/utilisateurs/modifiertype' method='POST'>");
							out.write("<select class='select_type' name='user_type' data-type-id='"+utilisateur.getType()+"'>"+listeDeroulanteTypes+"</select>");
							out.write("<input type='hidden' name='user_id' value='"+utilisateur.getId()+"' /></form>");
							out.write("</td>");
							out.write("</tr>");
						}
					%>
				</table>
				
			</div>

		</div>

	</body>
</html>
