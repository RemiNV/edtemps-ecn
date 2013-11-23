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
		<script type="text/javascript" src="<%=request.getContextPath()%>/admin/scripts/administrateurs.js"></script>
	</head>
	
	<body>

		<jsp:include page="../includes/menu.jsp" />

		<div id="main_content">
			<h1>Espace d'administration &rarr; Gestion des administrateurs</h1>			

			<div id="content" style="min-height: 270px">

				<div id="ajouter_administrateur">
					<p class="formulaire_zone_titre">Ajouter un administrateur :</p>
					<form action="<%=request.getContextPath() %>/administrateur/ajouter" method="POST" id="ajouter_administrateur_form" onsubmit="return validationAjouterAdministrateur()">
						<table>
							<tr><td><label for="ajouter_administrateur_login">Identifiant </label></td><td><input type="text" name="ajouter_administrateur_login" id="ajouter_administrateur_login" size="30" placeholder="Identifiant de l'administrateur" autocomplete="off" /></td></tr>
							<tr><td><label for="ajouter_administrateur_password">Mot de passe </label></td><td><input type="password" name="ajouter_administrateur_password" id="ajouter_administrateur_password" size="30" placeholder="Mot de passe de l'administrateur" autocomplete="off" /></td></tr>
							<tr><td><label for="ajouter_administrateur_password_again">Vérification du mot de passe </label></td><td><input type="password" name="ajouter_administrateur_password_again" id="ajouter_administrateur_password_again" size="30" placeholder="Ressaissez le mot de passe" autocomplete="off" /></td></tr>
						</table>
						<input type="submit" value="Ajouter" class="button" style="margin: 10px 10px 10px 355px;" />
					</form>
				</div>

				<div id="supprimer_administrateur">
					<p class="formulaire_zone_titre">Supprimer un administrateur :</p>
					<form action="<%=request.getContextPath() %>/administrateur/supprimer" method="POST" id="supprimer_administrateur_form" onsubmit="return confirmationSupprimerAdministrateur()">
						<select name="supprimer_administrateur_id" id="supprimer_administrateur_id">
							<%
								BddGestion bdd = new BddGestion();
								AdministrateurGestion administrateurGestion = new AdministrateurGestion(bdd);
								Map<Integer, String> listeAdministrateurs = administrateurGestion.listerAdministrateurs();
								boolean ilYADesValeurs = false; 
								for (Map.Entry<Integer, String> admin : listeAdministrateurs.entrySet()) {
									if (!admin.getValue().equals(session.getAttribute("login"))) {
										out.write("<option value='"+admin.getKey()+"'>"+admin.getValue()+"</option>");
										ilYADesValeurs = true;
									}
								}
								if (!ilYADesValeurs) {
									out.write("<option value='-1'>Aucun utilisateur à supprimer</option>");
								}
							%>
						</select>
						<input type="submit" value="Supprimer" class="button" style="height: 22px; padding-top: 2px;" <% if (!ilYADesValeurs) out.write("disabled"); %> />
					</form>
				</div>

				<table id="liste_administrateurs">
					<tr>
						<th>Liste des administrateurs</th>
					</tr>
					<%
						for (Map.Entry<Integer, String> admin : listeAdministrateurs.entrySet()) {
							out.write("<tr>");
							out.write("<td class='liste_administrateur_nom'>"+admin.getValue()+"</td>");
							out.write("</tr>");
						}
					%>
				</table>
				
			</div>

		</div>

	</body>
</html>
