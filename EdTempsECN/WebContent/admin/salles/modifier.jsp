<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>

<%@page import="org.ecn.edtemps.managers.*"%>
<%@page import="org.ecn.edtemps.models.*"%>
<%@page import="org.ecn.edtemps.models.identifie.*"%>
<%@page import="java.util.*"%>

<%
	Integer id = null; 
	try {
		id = Integer.valueOf(request.getParameter("id"));
	} catch (NumberFormatException e) {
		response.sendRedirect("index.jsp");
	}

	BddGestion bdd = new BddGestion();
	SalleGestion salleGestion = new SalleGestion(bdd);
	SalleIdentifie salle = salleGestion.getSalle(id, true);
%>

<!DOCTYPE html>
<html>
	<head>
		<meta charset="utf-8" />
		<title>Espace d'administration</title>
		
		<link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/admin/main.css" />

		<script type="text/javascript" src="<%=request.getContextPath()%>/admin/scripts/salles.js"></script>
	</head>
	<body>

		<jsp:include page="../includes/menu.jsp" />

		<div id="main_content">
			<h1>Espace d'administration &rarr; Modification de la salle '<%=salle.getNom()%>'</h1>			

			<div id="content">
			
				<form action="<%=request.getContextPath() %>/administrateur/salles/modifier" method="POST" id="modifier_salle_form" onsubmit="return validationModifierSalle()">
					<input type="hidden" value="<%=id %>" name="modifier_salle_id" />
					<table>
						<tr><td><label for="modifier_salle_batiment">Bâtiment :</label></td><td><input type="text" name="modifier_salle_batiment" id="modifier_salle_batiment" size="50" value="<%=salle.getBatiment()%>" /></td></tr>
						<tr><td><label for="modifier_salle_niveau">Niveau :</label></td><td><input type="text" name="modifier_salle_niveau" id="modifier_salle_niveau" size="50" value="<%=salle.getNiveau()%>" /></td></tr>
						<tr><td><label for="modifier_salle_numero">Numéro :</label></td><td><input type="text" name="modifier_salle_numero" id="modifier_salle_numero" size="50" value="<%=salle.getNumero()%>" /></td></tr>
						<tr><td><label for="modifier_salle_nom">Nom :</label></td><td><input type="text" name="modifier_salle_nom" id="modifier_salle_nom" size="50" value="<%=salle.getNom()%>" /></td></tr>
						<tr><td><label for="modifier_salle_capacite">Capacité :</label></td><td><input type="text" name="modifier_salle_capacite" id="modifier_salle_capacite" size="50" value="<%=salle.getCapacite()%>" /></td></tr>
						<%
							MaterielGestion materielGestion = new MaterielGestion(bdd);
							List<Materiel> listeMateriels = materielGestion.getListeMateriel();
							if (!listeMateriels.isEmpty()) {
								out.write("<tr>");
								out.write("<td>Liste des matériels :</td>");
								out.write("<td>");
								out.write("<table id='salle_materiels'>");
								List<Integer> listeIdMateriel = new ArrayList<Integer>();
								String stringListeIdMateriel = "";
								for (Materiel materiel : salle.getMateriels()) {
									out.write("<tr><td>"+materiel.getNom()+"</td><td class='salle_quantite_materiel'><input type='number' name='modifier_salle_materiel_"+materiel.getId()+"' value='"+materiel.getQuantite()+"' /></td></tr>");
									listeIdMateriel.add(materiel.getId());
									stringListeIdMateriel += materiel.getId() + ",";
								}
								for (Materiel materiel : listeMateriels) {
									if (!listeIdMateriel.contains(materiel.getId())) {
										out.write("<tr><td>"+materiel.getNom()+"</td><td class='salle_quantite_materiel'><input type='number' name='modifier_salle_materiel_"+materiel.getId()+"' value='0' /></td></tr>");
										listeIdMateriel.add(materiel.getId());
										stringListeIdMateriel += materiel.getId() + ",";
									}
								}
								out.write("<tr style='display: none'><td colspan='2'><input type='hidden' name='listeIdMateriel' value='"+((stringListeIdMateriel=="") ? "" : stringListeIdMateriel.substring(0, stringListeIdMateriel.length()-1))+"' /></td></tr>");
								out.write("</table>");
								out.write("</td>");
								out.write("</tr>");
							}
						%>
						<tr><td colspan="2" class="salle_form_boutons"><input type="button" value="Annuler" class="button" onclick="document.location.href='<%=request.getContextPath() %>/admin/salles/index.jsp'" /><input type="submit" value="Enregistrer" class="button" /></td></tr>
					</table>
				</form>
				
				<%
					// Récupération des noms des salles déjà utilisés pour éviter de modifier la salle avec un nom déjà utilisé
					SalleGestion gestionnaireSalles = new SalleGestion(bdd);
					List<SalleIdentifie> listeSalles = gestionnaireSalles.listerToutesSalles();
					out.write("<div style='display: none'>");
					for (SalleIdentifie salleIdentifie : listeSalles) {
						if (!salle.getNom().equals(salleIdentifie.getNom())) {
							out.write("<span class='liste_salles_nom'>"+salleIdentifie.getNom()+"</span>");
						}
					}
					out.write("</div>");
				%>
				
			</div>
		</div>

	</body>
</html>
