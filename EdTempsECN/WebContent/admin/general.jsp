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

		<jsp:include page="/admin/includes/menu.jsp" />

		<div id="main_content">
			<h1>Espace d'administration &rarr; Général</h1>
			
			<div id="content">
				<p>Page générale</p>
			</div>

		</div>

	</body>
</html>
