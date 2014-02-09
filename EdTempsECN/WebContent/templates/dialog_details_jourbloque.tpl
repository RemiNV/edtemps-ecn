<div class="details_jourbloque">
	<table>
	<%
		_.each(elements, function(e) {
			%>
			<tr title="<%= e.strGroupesAssocies %>" data-id="<%= e.id %>">
				<td width="300"><%= e.libelle %></td>
				<td width="120"><%= e.strHeureDebut %> - <%= e.strHeureFin %></td>
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