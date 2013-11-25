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

				<div id="ajouter_type_utilisateurs">
					<p class="formulaire_zone_titre">Ajouter un type d'utilisateurs :</p>
					<form action="<%=request.getContextPath() %>/administrateur/typesutilisateurs/ajouter" method="POST" onsubmit="return validationAjouterType()">
						<input type="text" name="ajouter_type_utilisateurs_nom" id="ajouter_type_utilisateurs_nom" size="50" placeholder="Nom du type d'utilisateurs que vous souhaitez ajouter" />
						<input type="submit" value="Ajouter" class="button" style="height: 22px; padding-top: 2px;" />
					</form>
				</div>

				<div id="modifier_type_utilisateurs">
					<p class="formulaire_zone_titre">Modifier un type d'utilisateurs :</p>
					<form action="<%=request.getContextPath() %>/admin/typesutilisateurs/modifier.jsp" method="POST">
						<select name="modifier_types_utilisateurs_id" id="modifier_types_utilisateurs_id">
							<%
								BddGestion bdd = new BddGestion();
								UtilisateurGestion gestionUtilisateurs = new UtilisateurGestion(bdd);
								Map<Integer, String> typesUtilisateurs = gestionUtilisateurs.getListeTypesUtilisateur();

								for (Map.Entry<Integer, String> type : typesUtilisateurs.entrySet()) {
									out.write("<option value='"+type.getKey()+"'>"+type.getValue()+"</option>");
								}
							%>
						</select>
						<input type="submit" value="Modifier" class="button" style="height: 22px; padding-top: 2px;" />
					</form>
				</div>
				
				<div id="supprimer_type_utilisateurs">
					<p class="formulaire_zone_titre">Supprimer un type d'utilisateurs :</p>
					<form action="<%=request.getContextPath() %>/administrateur/typesutilisateurs/supprimer" method="POST" onsubmit="return confirmationSupprimerType()">
						<select name="supprimer_types_utilisateurs_id" id="supprimer_types_utilisateurs_id">
							<%
								for (Map.Entry<Integer, String> type : typesUtilisateurs.entrySet()) {
									out.write("<option value='"+type.getKey()+"'>"+type.getValue()+"</option>");
								}
							%>
						</select>
						<input type="submit" value="Supprimer" class="button" style="height: 22px; padding-top: 2px;" />
					</form>
					<p class="information">Attention! Supprimer un type d'utilisateur entraîne la suppression de ce type pour tous les utilisateurs liés.</p>
				</div>
			
				<table id="types_utilisateurs_liste" class="tableau_liste">
					<tr>
						<th>Types d'utilisateurs</th>
					</tr>
					<%
						for (Map.Entry<Integer, String> type : typesUtilisateurs.entrySet()) {
							out.write("<tr>");
							out.write("<td class='liste_types_nom'>"+type.getValue()+"</td>");
							out.write("</tr>");
						}
					%>
				</table>

			</div>

		</div>

	</body>
</html>
