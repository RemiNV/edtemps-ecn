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
			
				<table id="liste_utilisateurs" class="tableau_liste">
					<tr>
						<th width="200">Prénom</th>
						<th width="200">Nom</th>
						<th width="300">Adresse mail</th>
						<th width="200">Type</th>
						<th width="200" colspan="2">Actions</th>
					</tr>
					<tr>
						<td><input type="text" id="filtre_prenom" placeholder="Filtrer par prénom" /></td>
						<td><input type="text" id="filtre_nom" placeholder="Filtrer par nom" /></td>
						<td><input type="text" id="filtre_mail" placeholder="Filtrer par adresse mail" /></td>
						<td><input type="text" id="filtre_type" placeholder="Filtrer par type" /></td>
						<td colspan="2"></td>
					</tr>
					<%
						BddGestion bdd = new BddGestion();
						UtilisateurGestion gestionUtilisateurs = new UtilisateurGestion(bdd);
						Map<Integer, String> typesUtilisateurs = gestionUtilisateurs.getListeTypesUtilisateur();
						
						List<UtilisateurIdentifie> listeUtilisateurs = gestionUtilisateurs.getListeUtilisateurs();
						for (UtilisateurIdentifie utilisateur : listeUtilisateurs) {
							out.write("<tr>");
							out.write("<td class='data-collumn-prenom'>"+utilisateur.getPrenom()+"</td>");
							out.write("<td class='data-collumn-nom'>"+utilisateur.getNom()+"</td>");
							out.write("<td class='data-collumn-mail'>"+(utilisateur.getEmail()==null ? "-" : utilisateur.getEmail() )+"</td>");
							out.write("<td class='data-collumn-type'>"+(utilisateur.getType()==0 ? "-" : typesUtilisateurs.get(utilisateur.getType()))+"</td>");
							out.write("<td class='form_modifier_utilisateur'><form action='"+request.getContextPath()+"/admin/utilisateurs/modifier.jsp' method='POST'><input src='"+request.getContextPath()+"/img/modifier.png' type='image' title='Modifier' /><input type='hidden' name='id' value='"+utilisateur.getId()+"' /><input type='hidden' name='nom' value='"+utilisateur.getNom()+"' /><input type='hidden' name='prenom' value='"+utilisateur.getPrenom()+"' /></form></td>");
							out.write("<td class='form_supprimer_utilisateur'><form onsubmit='return confirmationSupprimerUtilisateur()' action='"+request.getContextPath()+"/administrateur/utilisateurs/supprimer' method='POST'><input src='"+request.getContextPath()+"/img/supprimer.png' type='image' title='Supprimer' /><input type='hidden' name='id' value='"+utilisateur.getId()+"' /></form></td>");
							out.write("</tr>");
						}
					%>
				</table>
				
			</div>

		</div>

	</body>
</html>
