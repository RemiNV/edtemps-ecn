<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"

	import="org.ecn.edtemps.managers.CreneauGestion"
	import="org.ecn.edtemps.managers.BddGestion"
	import="org.ecn.edtemps.models.identifie.CreneauIdentifie"
	import="org.apache.commons.lang3.StringUtils"
	import="java.text.SimpleDateFormat"
	import="org.ecn.edtemps.exceptions.DatabaseException"
%>

<%
	Integer id = null;
	try {
		String strId = request.getParameter("id");
		if (StringUtils.isNotBlank(strId)) {
			id = Integer.valueOf(strId);
		}
	} catch (NumberFormatException e) {
		response.sendRedirect("../general.jsp");
	}

	BddGestion bdd = new BddGestion();
	CreneauGestion creneauGestion = new CreneauGestion(bdd);
	CreneauIdentifie creneau = null;
	if (id!=null) {
		try {
			creneau = creneauGestion.getCreneau(id);
		} catch (DatabaseException e) {
			response.sendRedirect("../general.jsp");
		}
	}
	SimpleDateFormat formater = new SimpleDateFormat("HH:mm");
%>

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

		<jsp:include page="../includes/menu.jsp" />

		<div id="main_content">
			<h1>Espace d'administration &rarr; Modification du créneau '<%=(creneau==null) ? "" : creneau.getLibelle() %>'</h1>			

			<div id="content">
			
				<form action="<%=request.getContextPath() %>/administrateur/creneaux/ajoutermodifier" method="POST" onsubmit="return validationAjouterModifierCreneaux()">
					<input type="hidden"<%=(creneau==null) ? "" : " value='"+id+"'" %> name="idCreneau" />
					<table>
						<tr><td><label for="creneau_libelle">Libellé :</label></td><td><input type="text" name="libelleCreneau" id="creneau_libelle" size="50" value="<%=creneau==null ? "" : creneau.getLibelle() %>" autofocus /></td></tr>
						<tr>
							<td>
								<label for="creneau_debut">Début :</label></td><td><input type="text" id="creneau_debut" size="5" value="<%=creneau==null ? "" : formater.format(creneau.getDebut()) %>" />
								<input type="hidden" name="debutCreneau" id="debutCreneau" /> <i>(Format attendu : "hh:mm")</i>
							</td>
						</tr>
						<tr>
							<td>
								<label for="creneau_fin">Fin :</label></td><td><input type="text" id="creneau_fin" size="5" value="<%=creneau==null ? "" : formater.format(creneau.getFin()) %>" />
								<input type="hidden" name="finCreneau" id="finCreneau" /> <i>(Format attendu : "hh:mm")</i>
							</td>
						</tr>
					</table>
					<input type="submit" class="button" value="<%=(creneau==null) ? "Ajouter" : "Modifier" %>" />
				</form>
				
			</div>
		</div>

	</body>
</html>
