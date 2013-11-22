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
		<script type="text/javascript" src="<%=request.getContextPath()%>/admin/scripts/salles.js"></script>
	</head>
	<body>

		<jsp:include page="../includes/menu.jsp" />

		<div id="main_content">
			<h1>Espace d'administration &rarr; Gestion des salles</h1>			

			<div id="content">
			
				<div id="ajouter_salle">
					<div id="ajouter_salle_bouton" class="button" onclick="afficheCacheFormulaireAjouterSalle()">Ajouter une salle</div>
					<form action="<%=request.getContextPath() %>/administrateur/salles/ajouter" method="POST" id="ajouter_salle_form" onsubmit='return validationModifierSalle()'>
						<table>
							<tr><td><label for="ajouter_salle_batiment">Bâtiment :</label></td><td><input type="text" name="ajouter_salle_batiment" id="ajouter_salle_batiment" size="50" onchange="afficheNomSalle()" /></td></tr>
							<tr><td><label for="ajouter_salle_niveau">Niveau :</label></td><td><input type="text" name="ajouter_salle_niveau" id="ajouter_salle_niveau" size="50" onchange="afficheNomSalle()" /></td></tr>
							<tr><td><label for="ajouter_salle_numero">Numéro :</label></td><td><input type="text" name="ajouter_salle_numero" id="ajouter_salle_numero" size="50" onchange="afficheNomSalle()" /></td></tr>
							<tr><td><label for="ajouter_salle_nom">Nom :</label></td><td><input type="text" name="ajouter_salle_nom" id="ajouter_salle_nom" size="46" disabled /><img alt="Modifier" onclick="activeChampNom()" title="Cliquez si vous souhaitez modifier manuellement le nom de la salle" src="<%=request.getContextPath()%>/img/modifier.png" /></td></tr>
							<tr><td><label for="ajouter_salle_capacite">Capacité :</label></td><td><input type="text" name="ajouter_salle_capacite" id="ajouter_salle_capacite" size="50" /></td></tr>
							<%
								BddGestion bdd = new BddGestion();
								MaterielGestion materielGestion = new MaterielGestion(bdd);
								List<Materiel> listeMateriels = materielGestion.getListeMateriel();
								if (!listeMateriels.isEmpty()) {
									out.write("<tr>");
									out.write("<td>Liste des matériels :</td>");
									out.write("<td>");
									out.write("<table id='ajouter_salle_materiels'>");
									String stringListeIdMateriel = "";
									for (Materiel materiel : listeMateriels) {
										out.write("<tr><td>"+materiel.getNom()+"</td><td class='ajouter_salle_quantite_materiel'><input type='number' name='ajouter_salle_materiel_"+materiel.getId()+"' value='0' /></td></tr>");
										stringListeIdMateriel += materiel.getId() + ",";
									}
									out.write("<tr style='display: none'><td colspan='2'><input type='hidden' name='listeIdMateriel' value='"+((stringListeIdMateriel=="") ? "" : stringListeIdMateriel.substring(0, stringListeIdMateriel.length()-1))+"' /></td></tr>");
									out.write("</table>");
									out.write("</td>");
									out.write("</tr>");
								}
							%>
							<tr><td colspan="2" class="ajouter_salle_form_boutons"><input type="reset" id="ajouter_salle_form_annuler" value="Annuler" class="button" onclick="afficheCacheFormulaireAjouterSalle()" /><input type="submit"  id="ajouter_salle_form_ajouter" value="Ajouter" class="button" /></td></tr>
						</table>
						<div id="ajouter_salle_form_chargement" style="display: none">
							<img src="<%=request.getContextPath()%>/img/spinner_chargement.gif" alt="Chargement" width="30" />
							<span>Traitement en cours</span>
						</div>
						
						<div class="information">
							<p>Le nom de la salle est généré automatiquement à partir du bâtiment, du niveau et du numéro. Vous pouvez cependant l'éditer manuellement en cliquant sur le bouton modifier à côté du champ.
						</div>
					</form>
				</div>
			
				<div id="liste_salles">
					<%
						SalleGestion gestionnaireSalles = new SalleGestion(bdd);
						List<SalleIdentifie> listeSalles = gestionnaireSalles.listerToutesSalles();
						
						if (listeSalles.isEmpty()) {
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
								out.write("<td class='liste_salles_nom'>"+salle.getNom()+"</td>");
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
