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
		<script type="text/javascript" src="<%=request.getContextPath()%>/admin/scripts/materiels.js"></script>
	</head>
	
	<body>

		<jsp:include page="../includes/menu.jsp" />

		<div id="main_content">
			<h1>Espace d'administration &rarr; Gestion des types de matériel</h1>			

			<div id="content" style="min-height: 250px">

				<div id="ajouter_materiel">
					<p class="formulaire_zone_titre">Ajouter un type de matériel :</p>
					<form action="<%=request.getContextPath() %>/administrateur/materiel/ajouter" method="POST" id="ajouter_materiel_form" onsubmit="return validationAjouterMateriel()">
						<input type="text" name="ajouter_materiel_nom" id="ajouter_materiel_nom" size="50" placeholder="Nom du type de matériel que vous souhaitez ajouter" />
						<input type="submit" value="Ajouter" class="button ptiButton" />
					</form>
				</div>

				<div id="supprimer_materiel">
					<p class="formulaire_zone_titre">Supprimer un type de matériel :</p>
					<form action="<%=request.getContextPath() %>/administrateur/materiel/supprimer" method="POST" id="supprimer_materiel_form" onsubmit="return confirmationSupprimerMateriel()">
						<select name="supprimer_materiel_id" id="supprimer_materiel_id">
							<%
								BddGestion bdd = new BddGestion();
								MaterielGestion materielGestion = new MaterielGestion(bdd);
								List<Materiel> listeMateriels = materielGestion.getListeMateriel();

								for (Materiel materiel : listeMateriels) {
									out.write("<option value='"+materiel.getId()+"'>"+materiel.getNom()+"</option>");
								}
							%>
						</select>
						<input type="submit" value="Supprimer" class="button ptiButton" />
					</form>
					<p class="information">Attention! Supprimer un type de matériel rompt, de manière irrévocable, les liens avec les salles qui contiennent ce type de matériel.</p>
				</div>

				<table id="liste_materiel" class="tableau_liste">
					<%
						if (listeMateriels.isEmpty()) {
							out.write("<tr><td colspan='7'>Aucun matériel dans la base de données</td></tr>");
						} else {
							out.write("<tr>");
							out.write("<th>Types de matériel</th>");
							out.write("</tr>");
							for (Materiel materiel : listeMateriels) {
								out.write("<tr>");
								out.write("<td class='liste_materiel_nom'>"+materiel.getNom()+"</td>");
								out.write("</tr>");
							}
						}
					%>
				</table>
				
			</div>

		</div>

	</body>
</html>
