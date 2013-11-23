<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html>
<html>
	<head>
		<meta charset="utf-8" />
		<title>Espace d'administration</title>
		
		<link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/admin/main.css" />
	</head>
	
	<body>

		<div id="form_login">
			<h1>Espace d'administration</h1>

			<% if (session.getAttribute("connect")=="OK") {
					response.sendRedirect("general.jsp");
			} %>
			
			<form action="<%=request.getContextPath()%>/administrateur/connexion" method="POST">
				<table>
					<tr><td align="right"><label for="login">Identifiant </label></td><td><input type="text" name="login" id="login" /></td></tr>
					<tr><td align="right"><label for="password">Mot de passe </label></td><td><input type="password" name="password" id="password" /></td></tr>
					<% if (session.getAttribute("connect")=="KO") { %>
						<tr><td id="erreur_connexion" colspan="2">Identifiant et/ou mot de passe erroné</td></tr>
					<% } %>
					<tr><td colspan="2" align="right"><input type="submit" class="button" name="connexion" value="Connecter" /></td></tr>
				</table>
			</form>
			
		</div>

	</body>
</html>