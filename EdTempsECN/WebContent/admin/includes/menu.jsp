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
				<li><a href="<%=request.getContextPath()%>/admin/general.jsp">Général</a></li>
				<li><a href="<%=request.getContextPath()%>/admin/salles/index.jsp">Gestion des salles</a></li>
				<li><a href="<%=request.getContextPath()%>/admin/deconnexion.jsp">Déconnexion</a></li>
			</ul>
		</td>
		<td class="message_bonjour">Bonjour <%=session.getAttribute("login")%>,</td>
	</tr>
</table>