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
	MaterielGestion materielGestion = new MaterielGestion(bdd);
	Materiel materiel = materielGestion.getMateriel(id);
%>

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
			<h1>Espace d'administration &rarr; Modification du matériel '<%=materiel.getNom()%>'</h1>			

			<div id="content">
			
				<form action="<%=request.getContextPath() %>/administrateur/materiel/modifier" method="POST" id="modifier_materiel_form" onsubmit="return validationModifierMateriel()">
					<input type="hidden" value="<%=id %>" name="modifier_materiel_id" />
					<table>
						<tr><td><label for="modifier_materiel_nom">Nom :</label></td><td><input type="text" name="modifier_materiel_nom" id="modifier_salle_nom" size="50" value="<%=materiel.getNom()%>" /></td></tr>
						<tr><td colspan="2" class="materiel_form_boutons"><input type="button" value="Annuler" class="button" onclick="document.location.href='<%=request.getContextPath() %>/admin/materiel/index.jsp'" /><input type="submit" value="Enregistrer" class="button" /></td></tr>
					</table>
				</form>
				
				<%
					// Récupération des noms de materiel déjà utilisés pour éviter de modifier le matériel avec un nom déjà utilisé
					MaterielGestion gestionnaireMateriel = new MaterielGestion(bdd);
					List<Materiel> listeMateriels = gestionnaireMateriel.getListeMateriel();
					out.write("<div style='display: none'>");
					for (Materiel materielIdentifie : listeMateriels) {
						if (!materiel.getNom().equals(materielIdentifie.getNom())) {
							out.write("<span class='liste_materiel_nom'>"+materielIdentifie.getNom()+"</span>");
						}
					}
					out.write("</div>");
				%>
				
			</div>
		</div>

	</body>
</html>
