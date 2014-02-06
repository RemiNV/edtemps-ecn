<div>
	<h3>Paramètres :</h3>
	<table>
		<tr>
			<td><label for="input_frequence">Fréquence : </label></td>
			<td>Tous les <input type="number" id="input_frequence" class="input_small" /> jours</td>
		</tr>
		<tr>
			<td><label for="input_nb_evenements">Nombre d'événements : </label></td>
			<td><input type="number" id="input_nb_evenements" /></td>
		</tr>
	</table>
	
	<h3>Synthèse de l'action : </h3>
	<div id="div_synthese" data-template="
		<table class='tbl_standard'>
			<tr>
				<th>Num.</th><th>Date</th><th>Statut</th><th>Action</th>
			</tr>
			<% _.each(synthese, function(element, index) { %>
				<tr>
					<td><%= element.num %></td>
					<td><%= element.date %></td>
					<td><%= element.statut %></td>
					<td>
						<%
						switch(element.statusCode) {
							
							case 1: // Salle occupée par cours
								%>
								<span class='button btn_rechercher_salle'>Rech. salle</span>
								%>
								break;
							case 2: // Salle occupée par non cours
								%>
								<span class='button btn_rechercher_salle'>Rech. salle</span>
								<%
							case 3: // Public occupé
							case 4: // Jour bloqué
								%>
								<span class='button btn_forcer_ajout'>Forcer ajout</span>
								<%
								break;
						}	
						%>
					</td>
				</tr>
			<% }) %>
		</table>
		">
	</div>
	<div class="boutons_valider">
		<span id="btn_annuler" class="button">Annuler</span>
		<span id="btn_executer" class="button">Exécuter</span> 
	</div>
</div>
