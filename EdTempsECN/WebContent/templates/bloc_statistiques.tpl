<table class='tbl_statistiques tbl_standard'>
	<tr>
		<th></th>
		<% for(var typeCours in statistiques) { %>
			<th><%= typeCours %></th>
		<% } %>
	</tr>
	<% for(var idGroupe in groupes) { %>
		<tr>
			<td><%= groupes[idGroupe] %></td>
			<% for(var typeCours in statistiques) { %>
				<td <%
				if(statistiques[typeCours][idGroupe].prevu) {
					if(statistiques[typeCours][idGroupe].actuel == statistiques[typeCours][idGroupe].prevu) {
						%>class='quota_juste'<%
					}
					else if(statistiques[typeCours][idGroupe].actuel < statistiques[typeCours][idGroupe].prevu) {
						%>class='quota_insuffisant'<%
					}
					else {
						%>class='quota_depasse'<%
					}
				}
				%>>
				<% if(statistiques[typeCours][idGroupe]) { %>
					<%= Math.round(statistiques[typeCours][idGroupe].actuel/360)/10 %>/<%= Math.round(statistiques[typeCours][idGroupe].prevu/360)/10 %>
				<% } %>
				</td>
			<% } %>
		</tr>
	<% } %>
</table>