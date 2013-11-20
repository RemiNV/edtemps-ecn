<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
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

		<div id="main_content">
			<h1>Espace d'administration &rarr; Connexion</h1>

			<% if (session.getAttribute("connect")=="OK") {
					response.sendRedirect("general.jsp");
			} %>
			
			<form action="<%=request.getContextPath()%>/administrateur/connexion" method="POST" id="form_login">
				<table>
					<tr><td><label for="login">Identifiant</label></td><td><input type="text" name="login" id="login"></td></tr>
					<tr><td><label for="password">Mot de passe</label></td><td><input type="password" name="password" id="password"></td></tr>
					<% if (session.getAttribute("connect")=="KO") { %>
						<tr><td id="erreur_connexion" colspan="2">Identifiant et/ou mot de passe erron�</td></tr>
					<% } %>
					<tr><td colspan="2" align="right"><input type="submit" class="button" id="connexion" name="connexion" value="Connecter" /></td></tr>
				</table>
			</form>
			
		</div>

	</body>
</html>