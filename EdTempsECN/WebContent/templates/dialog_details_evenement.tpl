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
			<td><%= strProprietaires %></td>
		</tr>
		<tr>
			<td>Intervenants : </td>
			<td><%= strIntervenants %></td>
		</tr>
		<tr>
			<td>Calendriers : </td>
			<td><%= strCalendriers %></td>
		</tr>
	</table>

	<div class="boutons_valider">
		<input type="button" class="button" value="Modifier" id="btnModifierEvenement" />
		<input type="button" class="button" value="Supprimer" id="btnSupprimerEvenement" />
	</div>
</div>
<div class="dialog_details_evenement_arrow_outer"></div>
<div class="dialog_details_evenement_arrow_inner"></div>