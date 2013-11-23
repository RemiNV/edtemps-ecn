<% if (session.getAttribute("connect")!="OK") { %>
	<jsp:forward page="../login.jsp" />
<% } %>

<table id="header">
	<tr>
		<td id="logo">
			<img src="<%=request.getContextPath()%>/img/icone_appli.png" alt="Emploi du temps ECN" />
		</td>
		<td id="menu">
			<ul class="button_nav">
				<li><a href="<%=request.getContextPath()%>/admin/general.jsp">G�n�ral</a></li>
				<li><a href="<%=request.getContextPath()%>/admin/administrateurs/index.jsp">Administrateurs</a></li>
				<li><a href="<%=request.getContextPath()%>/admin/utilisateurs/index.jsp">Utilisateurs</a></li>
				<li><a href="<%=request.getContextPath()%>/admin/salles/index.jsp">Salles</a></li>
				<li><a href="<%=request.getContextPath()%>/admin/materiel/index.jsp">Mat�riel</a></li>
				<li><a href="<%=request.getContextPath()%>/admin/deconnexion.jsp">D�connexion</a></li>
			</ul>
		</td>
	</tr>
</table>