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
		<script type="text/javascript" src="<%=request.getContextPath()%>/admin/scripts/utilisateurs.js"></script>
	</head>
	
	<body>

		<jsp:include page="../includes/menu.jsp" />

		<div id="main_content">
			<h1>Espace d'administration &rarr; Gestion des utilisateurs</h1>			

			<div id="content">
			
				<table id="liste_utilisateurs">
					<tr>
						<th width="200">Prénom</th>
						<th width="200">Nom</th>
						<th width="300">Adresse mail</th>
						<th width="200">Type</th>
					</tr>
					<tr>
						<td><input type="text" id="filtre_prenom" style="width:100%" placeholder="Filtrer par prénom" /></td>
						<td><input type="text" id="filtre_nom" style="width:100%" placeholder="Filtrer par nom" /></td>
						<td><input type="text" id="filtre_mail" style="width:100%" placeholder="Filtrer par adresse mail" /></td>
						<td><input type="text" id="filtre_type" style="width:100%" placeholder="Filtrer par type" /></td>
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
							out.write("<td class='data-collumn-prenom'>"+utilisateur.getPrenom()+"</td>");
							out.write("<td class='data-collumn-nom'>"+utilisateur.getNom()+"</td>");
							out.write("<td class='data-collumn-mail'>"+(utilisateur.getEmail()==null ? "-" : utilisateur.getEmail() )+"</td>");
							out.write("<td class='data-collumn-type'>"+(utilisateur.getType()==0 ? "-" : typesUtilisateurs.get(utilisateur.getType()))+"</td>");
							out.write("<td>");
							out.write("<form action='"+request.getContextPath()+"/administrateur/utilisateurs/modifiertype' method='POST'>");
							out.write("<select class='select_type' name='user_type' data-type-id='"+utilisateur.getType()+"'>"+listeDeroulanteTypes+"</select>");
							out.write("<input type='hidden' name='user_id' value='"+utilisateur.getId()+"' /></form>");
							out.write("</td>");
							out.write("<td class='form_supprimer_utilisateur'><form onsubmit='return confirmationSupprimerUtilisateur()' action='"+request.getContextPath()+"/administrateur/utilisateurs/supprimer' method='POST'><input src='"+request.getContextPath()+"/img/supprimer.png' type='image' title='Supprimer' /><input type='hidden' name='id' value='"+utilisateur.getId()+"' /></form></td>");
							out.write("</tr>");
						}
					%>
				</table>
				
			</div>

		</div>

	</body>
</html>
