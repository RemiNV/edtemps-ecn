<div class="details_jourbloque">
	<table>
	<%
		_.each(elements, function(e) {
			%>
			<tr title="<%= e.strGroupesAssocies %>">
				<td width="300"><%= e.libelle %></td>
				<td width="120"><%= e.strHeureDebut %> - <%= e.strHeureFin %></td>
				<td width="40"><img src='./img/modifier.png' title='Modifier' /></td>
				<td width="40"><img src='./img/supprimer.png' title='Supprimer' /></td>
			</tr>
			<%
		});
	%>
	</table>
</div>

<div class="dialog_details_evenement_arrow_outer"></div>
<div class="dialog_details_evenement_arrow_inner"></div>