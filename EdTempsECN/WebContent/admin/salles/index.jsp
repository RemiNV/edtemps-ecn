<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>

<%@page import="org.ecn.edtemps.managers.SalleGestion"%>
<%@page import="org.ecn.edtemps.managers.BddGestion"%>
<%@page import="org.ecn.edtemps.models.identifie.SalleIdentifie"%>
<%@page import="org.ecn.edtemps.models.Materiel"%>
<%@page import="java.util.List"%>

<!DOCTYPE html>
<html>
	<head>
		<meta charset="utf-8" />
		<title>Espace d'administration</title>
		
		<link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/admin/main.css" />
		
		<script type="text/javascript" src="<%=request.getContextPath()%>/scripts/lib/jquery-1.10.2.min.js"></script>
		<script type="text/javascript" src="<%=request.getContextPath()%>/scripts/lib/jquery-ui-1.10.3.notheme.min.js"></script>
		<script type="text/javascript" src="<%=request.getContextPath()%>/admin/scripts/main.js"></script>
	</head>
	<body>

		<jsp:include page="../includes/menu.jsp" />

		<div id="main_content">
			<h1>Espace d'administration &rarr; Gestion des salles</h1>			

			<div id="content">
			
				<div id="ajouter_salle">
					<div id="ajouter_salle_bouton" class="button" onclick="afficheCacheFormulaireModifierSalle()">Ajouter une salle</div>
					<form action="<%=request.getContextPath() %>/administrateur/salles/ajouter" method="POST" id="ajouter_salle_form" onsubmit='return validationModifierSalle()'>
						<table>
							<tr><td><label for="ajouter_salle_nom">Nom</label></td><td><input type="text" name="ajouter_salle_nom" /></td></tr>
							<tr><td><label for="ajouter_salle_batiment">Bâtiment</label></td><td><input type="text" name="ajouter_salle_batiment" /></td></tr>
							<tr><td><label for="ajouter_salle_niveau">Niveau</label></td><td><input type="text" name="ajouter_salle_niveau" /></td></tr>
							<tr><td><label for="ajouter_salle_numero">Numéro</label></td><td><input type="text" name="ajouter_salle_numero" /></td></tr>
							<tr><td><label for="ajouter_salle_capacite">Capacité</label></td><td><input type="text" name="ajouter_salle_capacite" /></td></tr>
							<tr><td><label for="ajouter_salle_materiel">Liste des matériels</label></td><td><input type="text" name="ajouter_salle_materiel" /></td></tr>
							<tr><td colspan="2"><input type="submit" value="Ajouter" class="button" /></td></tr>
						</table>
					</form>
				</div>
			
				<div id="liste_salles">
					<p>Liste des salles :</p>
					<%
						BddGestion bdd = new BddGestion();
						SalleGestion gestionnaireSalles = new SalleGestion(bdd);
						List<SalleIdentifie> listeSalles = gestionnaireSalles.listerToutesSalles();
						
						if (listeSalles.size()==0) {
							out.write("<tr><td colspan='7'>Aucunes salles dans la base de données</td></tr>");
						} else {
							out.write("<table>");
							out.write("<tr>");
							out.write("<th>Nom</th>");
							out.write("<th>Bâtiment</th>");
							out.write("<th>Niveau</th>");
							out.write("<th>Numéro</th>");
							out.write("<th>Capacité</th>");
							out.write("<th>Liste des matériels</th>");
							out.write("<th colspan='2'>Actions</th>");
							out.write("</tr>");
							for (SalleIdentifie salle : listeSalles) {
								out.write("<tr>");
								out.write("<td>"+salle.getNom()+"</td>");
								out.write("<td>"+salle.getBatiment()+"</td>");
								out.write("<td>"+salle.getNiveau()+"</td>");
								out.write("<td>"+salle.getNumero()+"</td>");
								out.write("<td>"+salle.getCapacite()+"</td>");
								StringBuilder preparationCase = new StringBuilder();
								for (Materiel materiel : salle.getMateriels()) {
									if (materiel.getQuantite()==1) {
										preparationCase.append(materiel.getQuantite() + " " + materiel.getNom() + ", ");
									} else if (materiel.getQuantite()>1) {
										preparationCase.append(materiel.getQuantite() + " " + materiel.getNom() + "s, ");
									}
								}
								out.write("<td>" + (preparationCase.length()>0 ? preparationCase.substring(0, preparationCase.length()-2) : "") + "</td>");
								out.write("<td class='liste_salles_modifier'><a href=''><img alt='Modifier' title='Modifier' src='"+request.getContextPath()+"/img/modifier.png' /></a></td>");
								out.write("<td class='liste_salles_supprimer'><form onsubmit='return confirmationSupprimerSalle()' action='"+request.getContextPath()+"/administrateur/salles/supprimer' method='POST' class='liste_salles_form_supprimer'><input src='"+request.getContextPath()+"/img/supprimer.png' type='image' title='Supprimer' /><input type='hidden' name='id' value='"+salle.getId()+"' /></form></td>");
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
