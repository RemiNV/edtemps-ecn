<div>
	<h3>Paramètres :</h3>
	<table>
		<tr>
			<td><label for="input_frequence">Fréquence : </label></td>
			<td>Tous les <input type="number" id="input_frequence" class="input_small" value="7" /> jours</td>
		</tr>
		<tr>
			<td><label for="input_nb_evenements">Nombre d'événements : </label></td>
			<td><input type="number" id="input_nb_evenements" /></td>
		</tr>
	</table>
	
	<h3>Synthèse de l'action : </h3>
	<div id="div_synthese" data-template="
		<table class='tbl_standard centrer'>
			<tr>
				<th>Num.</th><th>Date</th><th>Statut</th><th>Action</th>
			</tr>
			<% _.each(synthese, function(element, index) { %>
				<tr class='<%= element.problemes.length > 0 ? (element.resteProblemes ? 'ignorer' : 'warning') : '' %>'>
					<td><%= (element.resteProblemes ? '(' + element.num + ')' : element.num) %></td>
					<td><%= element.strDate %></td>
					<td><%= element.strProblemes %></td>
					<td>
						<%
						if(element.resteProblemes) {
							%>
							<span>Ignoré</span><br/>
							<%
						}
						if(element.afficherBoutonRechercheSalle) {
							if(element.nouvellesSalles) {
								%>
								<span>(<%= element.strNouvellesSalles %>)</span>
								<%
							}
							else {
								%>
								<span class='button btn_rechercher_salle' data-id='<%= element.id %>'>Rech. salle</span>
								<%
							}
						}
						
						if(element.afficherBoutonForcer) {
							if(element.forcerAjout) {
								%>
								<span>(Ajout forcé)</span>
								<%
							}
							else {
								%>
								<span class='button btn_forcer_ajout' data-id='<%= element.id %>'>Forcer ajout</span>
								<%
							}
						}
						%>
					</td>
				</tr>
			<% }) %>
		</table>
		">
	</div>
	<div class="boutons_valider">
		<input type="button" id="btn_annuler" class="button" value="Annuler"/>
		<input type="button" id="btn_previsualiser" class="button" value="Prévisualiser"/>
		<input type="button" id="btn_executer" class="button" value="Exécuter"/> 
	</div>
	<div class="message_alerte" id="msg_repetition_plusieurs_calendriers">
		
	</div>
	<div id="dialog_repeter_chargement" style="display: none">
		<img src="img/spinner_chargement_outer_small.gif" class="spinner_chargement" alt="chargement" />
		<span id="dialog_repeter_message_chargement"></span>
	</div>
</div>
</div>
