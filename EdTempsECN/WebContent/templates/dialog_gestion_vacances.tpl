<div class="gestion_vacances">
	<table>
	<%
		_.each(elements, function(periode) {
			%>
			<tr data-id="<%= periode.id %>" title="<%= periode.strGroupesAssocies %>">
				<td width="200"><%= periode.libelle %></td>
				<td width="95"><%= periode.strDateDebut %></td>
				<td width="95"><%= periode.strDateFin %></td>
				<td width="40"><img src='./img/modifier.png' title='Modifier' class='modifier' /></td>
				<td width="40"><img src='./img/supprimer.png' title='Supprimer' class='supprimer' /></td>
			</tr>
			<%
		});
	%>
	</table>
</div>

<div class="dialog_details_evenement_arrow_outer"></div>
<div class="dialog_details_evenement_arrow_inner"></div>