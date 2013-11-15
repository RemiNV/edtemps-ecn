/**
 * @module DialogDetailGroupeParticipants
 */
define([ "RestManager" ], function(RestManager) {

	/**
	 * @constructor
	 * @alias DialogDetailGroupeParticipants
	 */
	var DialogDetailGroupeParticipants = function(restManager) {
		this.restManager = restManager;
		this.initAppele = false;
		
		this.jqDetailGroupeParticipants = $("#dialog_detail_groupe");
	};

	/**
	 * Affiche la boîte de dialogue de détail d'un groupe de participants
	 * @param groupe Groupe à afficher
	 */
	DialogDetailGroupeParticipants.prototype.show = function(groupe) {
		if(!this.initAppele) {
			this.init();
			this.initAppele = true;
		}
		
		this.chargerContenu(groupe);
		
		this.jqDetailGroupeParticipants.dialog("open");
		
	};
	
	
	/**
	 * Initialise la boîte de dialogue de détail d'un groupe de participants
	 */
	DialogDetailGroupeParticipants.prototype.init = function() {

		// Affiche la boîte dialogue de recherche d'une salle libre
		this.jqDetailGroupeParticipants.dialog({
			autoOpen: false,
			width: 500,
			modal: true,
			show: {
				effect: "fade",
				duration: 200
			},
			hide: {
				effect: "explode",
				duration: 200
			}
		});

		this.initAppele = true;
	};
	
	/**
	 * Charge le contenu dans la boîte de dialogue
	 * @param groupe Groupe à afficher
	 */
	DialogDetailGroupeParticipants.prototype.chargerContenu = function(groupe) {
		var me = this;
		
		// Prépare le contenu de la fenêtre
		var contenuHtml =
			"<table>" +
				"<tr><td class='dialog_detail_groupe_label'>Nom du groupe</td><td class='dialog_detail_groupe_value'>"+groupe.nom+"</td></tr>";

		if (groupe.parentId>0) {
			contenuHtml +=
				"<tr><td class='dialog_detail_groupe_label'>Groupe parent</td><td class='dialog_detail_groupe_value'>"+groupe.parentId+"</td></tr>";
		} else {
			if (groupe.parentIdTmp>0) {
				contenuHtml +=
					"<tr><td class='dialog_detail_groupe_label'>Groupe parent</td><td class='dialog_detail_groupe_value'>"+groupe.parentIdTmp+" (rattachement non validé !)</td></tr>";
			} else {
				contenuHtml +=
					"<tr><td class='dialog_detail_groupe_label'>Groupe parent</td><td class='dialog_detail_groupe_value'>Aucun</td></tr>";
			}
		}
		
		contenuHtml +=
				"<tr><td class='dialog_detail_groupe_label'>Rattachement autorisé</td><td class='dialog_detail_groupe_value'>"+(groupe.rattachementAutorise ? "Oui" : "Non")+"</td></tr>" +
				"<tr><td class='dialog_detail_groupe_label'>Groupe de cours</td><td class='dialog_detail_groupe_value'>"+(groupe.estCours ? "Oui" : "Non")+"</td></tr>" +
			"</table>" +
			"<div id='dialog_detail_groupe_boutons'><input class='button' type='button' value='Fermer' id='dialog_detail_groupe_fermer' /></div>";

		// Ecrit le contenu dans la fenêtre
		this.jqDetailGroupeParticipants.html(contenuHtml);
		
		// Affectation d'une méthode au clic sur le bouton "Fermer"
		this.jqDetailGroupeParticipants.find("#dialog_detail_groupe_fermer").click(function() {
			me.jqDetailGroupeParticipants.dialog("close");
		});

	};


	return DialogDetailGroupeParticipants;

});
