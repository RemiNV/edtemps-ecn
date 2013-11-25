<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>

<%@page import="org.ecn.edtemps.managers.*"%>
<%@page import="org.ecn.edtemps.models.*"%>
<%@page import="org.ecn.edtemps.models.identifie.*"%>
<%@page import="java.util.*"%>

<%
	Integer id = null; 
	try {
		id = Integer.valueOf(request.getParameter("modifier_types_utilisateurs_id"));
	} catch (NumberFormatException e) {
		response.sendRedirect("index.jsp");
	}

	BddGestion bdd = new BddGestion();
	AdministrateurGestion gestionAdministrateurs = new AdministrateurGestion(bdd);
	UtilisateurGestion gestionUtilisateurs = new UtilisateurGestion(bdd);
	Map<Integer, String> typesUtilisateurs = gestionUtilisateurs.getListeTypesUtilisateur();
	Map<Integer, String> toutesLesActionsPossibles = gestionAdministrateurs.listerActionsEdtemps();
	List<Integer> actionsAutoriseesPourCeType = gestionAdministrateurs.getListeActionsTypeUtilisateurs(id); 
%>

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
			<h1>Espace d'administration &rarr; Modification du type d'utilisateurs '<%=typesUtilisateurs.get(id) %>'</h1>

			<div id="content">

				<form action="<%=request.getContextPath() %>/administrateur/typesutilisateurs/modifierDroits" method="POST" id="modifier_type_utilisateurs_form" onsubmit="return validationModifierType()">
					<input type="hidden" name="modifier_type_utilisateurs_form_id" value="<%=id %>" />
					<table>
						<tr><td><label for="modifier_type_utilisateurs_form_nom">Nom du type d'utilisateurs :</label></td><td><input type="text" id="modifier_type_utilisateurs_form_nom" name="modifier_type_utilisateurs_form_nom" value="<%=typesUtilisateurs.get(id) %>" /></td></tr>
						<tr>
							<td><label for="modifier_type_utilisateurs_form_droits">Sélectionnez les droits du type :</label></td>
							<td>
								<select multiple="multiple" id="modifier_type_utilisateurs_form_droits" name="modifier_type_utilisateurs_form_droits">
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
							</td>
						</tr>
						<tr><td colspan="2"><input type="button" class="button" onclick="document.location.href='<%=request.getContextPath() %>/admin/typesutilisateurs/index.jsp'" value="Annuler" /><input type="submit" class="button" value="Enregistrer" /></td></tr>
					</table>
					
					<table style="display: none">
					<%
						for (Map.Entry<Integer, String> type : typesUtilisateurs.entrySet()) {
							if (type.getKey()!=id) {
								out.write("<tr><td class='liste_types_nom'>"+type.getValue()+"</td></tr>");
							}
						}
					%>
					</table>
					
				</form>
			</div>
		</div>

	</body>
</html>
