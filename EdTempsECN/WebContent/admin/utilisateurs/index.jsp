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
						<th>Activé <img alt="Aide" src="<%=request.getContextPath()%>/img/help.png" title="Un utilisateur désactivé n'apparaît plus dans les listes déroulantes de choix d'utilisateurs" style="width: 20px;" /></th>
						<th>Actions</th>
					</tr>
					<tr>
						<td><input type="text" id="filtre_prenom" placeholder="Filtrer par prénom" /></td>
						<td><input type="text" id="filtre_nom" placeholder="Filtrer par nom" /></td>
						<td><input type="text" id="filtre_mail" placeholder="Filtrer par adresse mail" /></td>
						<td><input type="text" id="filtre_type" placeholder="Filtrer par type" /></td>
						<td class="colCenter"><input type="checkbox" id="filtre_statut" title="Coché, seuls les utilisateurs activés sont affichés" /></td>
						<td></td>
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
							if (utilisateur.getType().isEmpty()) {
								out.write("<td class='data-collumn-type'>-</td>");
							} else {
								StringBuilder nomsTypes = new StringBuilder();
								for (Integer idType : utilisateur.getType()) {
									nomsTypes.append(typesUtilisateurs.get(idType)+", ");
								}
								out.write("<td class='data-collumn-type'>"+nomsTypes.toString().substring(0, nomsTypes.length()-2)+"</td>");
							}
							out.write("<td class='colCenter data-collumn-statut'><form action='"+request.getContextPath()+"/administrateur/utilisateurs/" + (utilisateur.isActive() ? "desactiver" : "activer") +"' method='POST'><input type='submit' value='" + (utilisateur.isActive() ? "Désactiver" : "Activer") +"' class='button ptiButton' /><input type='hidden' name='id' value='"+utilisateur.getId()+"' /></form></td>");
							out.write("<td class='colCenter'><form action='"+request.getContextPath()+"/admin/utilisateurs/modifier.jsp' method='POST'><input src='"+request.getContextPath()+"/img/modifier.png' type='image' title='Modifier' /><input type='hidden' name='id' value='"+utilisateur.getId()+"' /><input type='hidden' name='nom' value='"+utilisateur.getNom()+"' /><input type='hidden' name='prenom' value='"+utilisateur.getPrenom()+"' /></form></td>");
							out.write("</tr>");
						}
					%>
				</table>
				
			</div>

		</div>

	</body>
</html>
