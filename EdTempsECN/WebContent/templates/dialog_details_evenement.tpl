<div class="details_evenement">
	<table>
		<tr>
			<td>Date de début : </td>
			<td><%= strDateDebut %></td>
		</tr>
		<tr>
			<td>Date de fin : </td>
			<td><%= strDateFin %></td>
		</tr>
		<tr>
			<td>Salles : </td>
			<td><%= strSalles %></td>
		</tr>
		<tr>
			<td>Propriétaires : </td>
			<td>
			<% 
			var first = true;
			_.each(proprietaires, function(prop) {
				if(first) {
					first = false;
				}
				else {
					%>, <%
				}
				%>
				<span title="<%= prop.email ? prop.email : '' %>"><%= prop.prenom + " " + prop.nom %></span>
				<%
			});
			%>
			</td>
		</tr>
		<tr>
			<td>Intervenants : </td>
			<td><% 
			var first = true;
			_.each(intervenants, function(prop) {
				if(first) {
					first = false;
				}
				else {
					%>, <%
				}
				%>
				<span title="<%= prop.email ? prop.email : '' %>"><%= prop.prenom + " " + prop.nom %></span>
				<%
			});
			%></td>
		</tr>
	</table>
</div>
<div class="dialog_details_evenement_arrow_outer"></div>
<div class="dialog_details_evenement_arrow_inner"></div>