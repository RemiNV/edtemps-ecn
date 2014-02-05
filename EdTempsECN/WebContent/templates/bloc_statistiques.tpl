<table class='tbl_statistiques'>
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
				<td>
				<% if(statistiques[typeCours][idGroupe]) { %>
					<%= statistiques[typeCours][idGroupe].actuel %>/<%= statistiques[typeCours][idGroupe].prevu %>
				<% } %>
				</td>
			<% } %>
		</tr>
	<% } %>
</table>