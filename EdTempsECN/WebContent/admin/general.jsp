<%@page import="org.ecn.edtemps.managers.CreneauGestion"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>

<%@page import="java.util.ArrayList" %>
<%@page import="org.ecn.edtemps.managers.BddGestion" %>    
<%@page import="org.ecn.edtemps.diagnosticbdd.*" %>
<%@page import="org.ecn.edtemps.diagnosticbdd.TestBdd.TestBddResult" %>
<%@page import="org.ecn.edtemps.diagnosticbdd.TestBdd.TestBddResultCode" %>
<%@page import="org.ecn.edtemps.models.identifie.CreneauIdentifie" %>
<%@page import="java.text.SimpleDateFormat" %>
<!DOCTYPE html>
<html>
	<head>
		<meta charset="utf-8" />
		<title>Espace d'administration</title>
		<link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/admin/main.css" />
		<script type="text/javascript" src="<%=request.getContextPath()%>/scripts/lib/jquery-1.10.2.min.js"></script>
		<script type="text/javascript" src="<%=request.getContextPath()%>/scripts/lib/jquery.maskedinput.min.js"></script>
		<script type="text/javascript" src="<%=request.getContextPath()%>/admin/scripts/creneaux.js"></script>
	</head>
	
	<body>

		<jsp:include page="/admin/includes/menu.jsp" />

		<div id="main_content">
			<h1>Espace d'administration &rarr; Général</h1>
			
			<div id="content">
				<h2>Diagnostic de la base de données</h2>
				<table class="tableau_liste">
					<tr>
						<th>ID</th>
						<th>Libellé</th>
						<th>Résultat</th>
						<th>Réparation</th>
					</tr>
					<%
					BddGestion bdd = new BddGestion();
					
					DiagnosticsBdd diagnostics = new DiagnosticsBdd(bdd);
					
					ArrayList<TestBddResult> results = diagnostics.runAllTests();
					
					for(TestBddResult res : results) {
						
						%>
						<tr>
							<td><%= res.getTest().getId() %></td>
							<td><%= res.getTest().getNom() %></td>
							<td class="<%= res.getResultCode().getLabel() %>"><%= res.getMessage() %></td>
							
						<%
						if(res.getResultCode() == TestBddResultCode.OK || res.getResultCode() == TestBddResultCode.TEST_FAILED) {
						%>
							<td></td>
						<%
						}
						else {
						%>
							<td>
								<form method="post" action="<%= request.getContextPath() %>/administrateur/reparer" class="form_reparer">
									<input type="hidden" name="id" value="<%= res.getTest().getId() %>" />
									<input type="submit" value="Réparer" title="<%= res.getTest().getRepairMessage() %>" class="button" />
								</form>
							</td>
						<%
						}
					}
					%>
				</table>
				
				<br/><br/>
				<h2>Créneaux horaires</h2>
				<table class="tableau_liste">
					<tr>
						<th width="100">Libellé</th>
						<th width="100">Début</th>
						<th width="100">Fin</th>
						<th width="100" colspan="2">Actions</th>
					</tr>
					<%
						CreneauGestion creneau = new CreneauGestion(bdd);
						ArrayList<CreneauIdentifie> creneaux = creneau.getCreneaux();
						bdd.close();
						
						for(CreneauIdentifie c : creneaux) {
							SimpleDateFormat formater = new SimpleDateFormat("HH:mm");
						%>
							<tr data-id="<%= c.getId() %>">
								<td><%= c.getLibelle() %></td>
								<td><%= formater.format(c.getDebut()) %></td>
								<td><%= formater.format(c.getFin()) %></td>
								<td class="colCenter">
									<a href="<%= request.getContextPath() %>/admin/creneaux/modifier.jsp?id=<%= c.getId() %>"><img alt="Modifier" title="Modifier" src="<%= request.getContextPath() %>/img/modifier.png" class="action" /></a>
									<form class="action" onsubmit='return confirmationSupprimerCreneau()' action='<%= request.getContextPath() %>/administrateur/creneaux/supprimer' method='POST'><input src='<%= request.getContextPath() %>/img/supprimer.png' type='image' title='Supprimer' /><input type='hidden' name='idCreneau' value='<%= c.getId() %>' /></form>
								</td>
							</tr>
						<%	
						}
					%>
				</table>
				<br/>
				<a href="<%= request.getContextPath() %>/admin/creneaux/modifier.jsp" class="button">Ajouter un créneau</a>
			</div>

		</div>

	</body>
</html>
