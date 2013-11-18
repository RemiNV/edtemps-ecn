<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html>
<html>
	<head>
		<meta charset="utf-8" />
		<title>Espace d'administration</title>
		
		<link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/admin/main.css" />
		<link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/admin/jquery-ui-1.10.3.custom.min.css" />
		
		<script type="text/javascript" src="<%=request.getContextPath()%>/scripts/lib/jquery-1.10.2.min.js"></script>
		<script type="text/javascript" src="<%=request.getContextPath()%>/scripts/lib/jquery-ui-1.10.3.notheme.min.js"></script>
	</head>
	<body>

		<jsp:include page="../includes/menu.jsp" />

		<div id="main_content">
			<h1>Espace d'administration &rarr; Gestion des salles</h1>			

			<div id="content">
				<p>Liste des salles :</p>
				
				
			</div>

		</div>

	</body>
</html>
