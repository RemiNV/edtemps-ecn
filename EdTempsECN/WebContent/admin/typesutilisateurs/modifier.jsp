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

				<table id="types_utilisateurs_liste" class="tableau_liste">
					<tr><th>Types d'utilisateurs</th></tr>
					<%
						BddGestion bdd = new BddGestion();
						UtilisateurGestion gestionUtilisateurs = new UtilisateurGestion(bdd);
						Map<Integer, String> typesUtilisateurs = gestionUtilisateurs.getListeTypesUtilisateur();
						for (Map.Entry<Integer, String> type : typesUtilisateurs.entrySet()) {
							out.write("<tr><td onclick='afficheMultiSelect("+type.getKey()+", &apos;"+type.getValue()+"&apos;)'>"+type.getValue()+"</td></tr>");
						}
					%>
				</table>
				
				<div id="types_utilisateurs_modifier">
					<p id="types_utilisateurs_modifier_titre"> Cliquez sur un type dans la liste de gauche pour en afficher les droits.</p>
					<%
						// Récupération de la liste des actions disponibles
						AdministrateurGestion gestionAdministrateurs = new AdministrateurGestion(bdd);
						Map<Integer, String> toutesLesActionsPossibles = gestionAdministrateurs.listerActionsEdtemps();

						for (Map.Entry<Integer, String> type : typesUtilisateurs.entrySet()) {
						
							// Récupération pour ce type des actions autorisées
							List<Integer> actionsAutoriseesPourCeType = gestionAdministrateurs.getListeActionsTypeUtilisateurs(type.getKey()); 
						
						%>
							<form action="<%=request.getContextPath() %>/administrateur/typesutilisateurs/modifierDroits" method="POST" onsubmit="return validationModificationType()" id="types_utilisateurs_modifier_form_<%=type.getKey() %>">
								<select multiple="multiple" id="types_utilisateurs_modifier_form_select_<%=type.getKey() %>" name="types_utilisateurs_modifier_form_select" class="types_utilisateurs_modifier_select">
									<%
									for (Map.Entry<Integer, String> action : toutesLesActionsPossibles.entrySet()) {
										if (actionsAutoriseesPourCeType.contains(action.getKey())) {
											out.write("<option value='"+action.getKey()+"' selected>"+action.getValue()+"</option>");
										} else {
											out.write("<option value='"+action.getKey()+"'>"+action.getValue()+"</option>");
										}
									}
									%>
								</select>
								<input type="submit" class="button types_utilisateurs_modifier_form_submit" value="Enregistrer" id="types_utilisateurs_modifier_form_submit_<%=type.getKey() %>" />
							</form>
					<%	}	%>
					<p id="types_utilisateurs_modifier_form_information">Pensez à cliquer sur le bouton Enregistrer pour sauver vos modifications.</p>
				</div>

				
				
				
				
				
			</div>

		</div>

	</body>
</html>
