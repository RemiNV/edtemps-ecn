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
		<script type="text/javascript" src="<%=request.getContextPath()%>/scripts/lib/jquery-ui-1.10.3.notheme.min.js"></script>
		<script type="text/javascript" src="<%=request.getContextPath()%>/scripts/lib/jquery.maskedinput.min.js"></script>
		<script type="text/javascript" src="<%=request.getContextPath()%>/admin/scripts/materiels.js"></script>
	</head>
	<body>

		<jsp:include page="../includes/menu.jsp" />

		<div id="main_content">
			<h1>Espace d'administration &rarr; Gestion du matériel</h1>			

			<div id="content">
			
				<div id="ajouter_materiel">
					<div id="ajouter_materiel_bouton" class="button" onclick="afficheCacheFormulaireAjouterMateriel()">Ajouter un matériel</div>
					<form action="<%=request.getContextPath() %>/administrateur/materiel/ajouter" method="POST" id="ajouter_materiel_form" onsubmit="return validationAjouterMateriel()">
						<table>
							<tr><td><label for="ajouter_materiel_nom">Nom :</label></td><td><input type="text" name="ajouter_materiel_nom" id="ajouter_materiel_nom" size="50" /></td></tr>
							<tr><td colspan="2" class="materiel_form_boutons"><input type="reset" id="ajouter_materiel_form_annuler" value="Annuler" class="button" onclick="afficheCacheFormulaireAjouterMateriel()" /><input type="submit" id="ajouter_materiel_form_ajouter" value="Ajouter" class="button" /></td></tr>
						</table>
					</form>
				</div>
			
				<div id="liste_materiel">
					<%
						BddGestion bdd = new BddGestion();
						MaterielGestion materielGestion = new MaterielGestion(bdd);
						List<Materiel> listeMateriels = materielGestion.getListeMateriel();
						
						if (listeMateriels.isEmpty()) {
							out.write("<tr><td colspan='7'>Aucun matériel dans la base de données</td></tr>");
						} else {
							out.write("<table>");
							out.write("<tr>");
							out.write("<th>Nom</th>");
							out.write("<th colspan='2'>Actions</th>");
							out.write("</tr>");
							for (Materiel materiel : listeMateriels) {
								out.write("<tr>");
								out.write("<td class='liste_materiel_nom'>"+materiel.getNom()+"</td>");
								out.write("<td class='liste_materiel_modifier'><a href='"+request.getContextPath()+"/admin/materiel/modifier.jsp?id="+materiel.getId()+"'><img alt='Modifier' title='Modifier' src='"+request.getContextPath()+"/img/modifier.png' /></a></td>");
								out.write("<td class='liste_materiel_supprimer'><form onsubmit='return confirmationSupprimerMateriel()' action='"+request.getContextPath()+"/administrateur/materiel/supprimer' method='POST'><input src='"+request.getContextPath()+"/img/supprimer.png' type='image' title='Supprimer' /><input type='hidden' name='id' value='"+materiel.getId()+"' /></form></td>");
								out.write("</tr>");
							}
							out.write("</table>");
						}
					%>
				</div>
				
			</div>

		</div>

	</body>
</html>
