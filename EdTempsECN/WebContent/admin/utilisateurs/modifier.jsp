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
		<script type="text/javascript" src="<%=request.getContextPath()%>/admin/scripts/utilisateurs.js"></script>
	</head>
	
	<body>

		<jsp:include page="../includes/menu.jsp" />

		<div id="main_content">
			<h1>Espace d'administration &rarr; Modification du type de l'utilisateur '<%=request.getParameter("prenom") %> <%=request.getParameter("nom") %>'</h1>

			<div id="content">

				<form action="<%=request.getContextPath() %>/administrateur/utilisateurs/modifiertype" method="POST" id="modifier_type_utilisateurs_form" onsubmit="return validationModifierType()">
					<input type="hidden" name="modifier_utilisateur_id" value="<%=id %>" />
					<select multiple="multiple" id="modifier_utilisateur_types" name="modifier_utilisateur_types">
						<%
						BddGestion bdd = new BddGestion();
						UtilisateurGestion gestionUtilisateurs = new UtilisateurGestion(bdd);
						Map<Integer, String> typesUtilisateurs = gestionUtilisateurs.getListeTypesUtilisateur();
						List<Integer> listeTypesDeLutilisateur = gestionUtilisateurs.getListeTypes(id);

						for (Map.Entry<Integer, String> type : typesUtilisateurs.entrySet()) {
							if (listeTypesDeLutilisateur.contains(type.getKey())) {
								out.write("<option value='"+type.getKey()+"' selected>"+type.getValue()+"</option>");
							} else {
								out.write("<option value='"+type.getKey()+"'>"+type.getValue()+"</option>");
							}
						}
						%>
					</select>
					<input type="button" class="button" onclick="document.location.href='<%=request.getContextPath() %>/admin/utilisateurs/index.jsp'" value="Annuler" /><input type="submit" class="button" value="Enregistrer" />
				</form>
			</div>
		</div>

	</body>
</html>
