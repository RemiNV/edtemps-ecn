<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>

<%@page import="org.ecn.edtemps.managers.SalleGestion"%>
<%@page import="org.ecn.edtemps.managers.BddGestion"%>
<%@page import="org.ecn.edtemps.models.identifie.SalleIdentifie"%>
<%@page import="org.ecn.edtemps.models.Materiel"%>
<%@page import="org.apache.commons.lang3.StringUtils"%>
<%@page import="java.util.List"%>

<!DOCTYPE html>
<html>
	<head>
		<meta charset="utf-8" />
		<title>Espace d'administration</title>
		
		<link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/admin/main.css" />
		
		<script type="text/javascript" src="<%=request.getContextPath()%>/scripts/lib/jquery-1.10.2.min.js"></script>
		<script type="text/javascript" src="<%=request.getContextPath()%>/scripts/lib/jquery-ui-1.10.3.notheme.min.js"></script>
	</head>
	<body>

		<jsp:include page="../includes/menu.jsp" />

		<div id="main_content">
			<h1>Espace d'administration &rarr; Gestion des salles</h1>			

			<div id="content">
			
				<div id="ajouter_salle">
				</div>
			
				<div id="liste_salles">
					<p>Liste des salles :</p>
					<table>
						<tr>
							<th>Nom</th>
							<th>Bâtiment</th>
							<th>Niveau</th>
							<th>Numéro</th>
							<th>Capacité</th>
							<th>Liste des matériels</th>
							<th>Actions</th>
						</tr>
						<%
							BddGestion bdd = new BddGestion();
							SalleGestion gestionnaireSalles = new SalleGestion(bdd);
							List<SalleIdentifie> listeSalles = gestionnaireSalles.listerToutesSalles();
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
								out.write("<td>"+StringUtils.substringBeforeLast(preparationCase.toString(), ", ")+"</td>");
								out.write("<td></td>");
								out.write("</tr>");
							}
						%>
					</table>
				</div>
			</div>

		</div>

	</body>
</html>
