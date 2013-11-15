/**
 * @module DialogDetailGroupeParticipants
 */
define([ "RestManager", "GroupeGestion" ], function(RestManager, GroupeGestion) {

	/**
	 * @constructor
	 * @alias DialogDetailGroupeParticipants
	 */
	var DialogDetailGroupeParticipants = function(restManager) {
		this.restManager = restManager;
		this.groupeGestion = new GroupeGestion(this.restManager);
		
		this.initAppele = false; /* Permet de ne lancer l'initialisation de la dialogue une seule fois */
		this.jqDetailGroupeParticipants = $("#dialog_detail_groupe"); /* Pointeur jQuery vers la dialogue */
	};

	/**
	 * Affiche la boîte de dialogue de détail d'un groupe de participants
	 * @param idGroupe Identifiant du groupe à afficher
	 */
	DialogDetailGroupeParticipants.prototype.show = function(idGroupe) {
		if(!this.initAppele) {
			this.init();
			this.initAppele = true;
		}
		
		var me=this;
		
		// Récupération des données sur le groupe à afficher
		this.groupeGestion.querySupprimerGroupes(idGroupe, function(resultCode, data) {

			if (resultCode == RestManager.resultCode_Success) {
				// Ecrit le contenu de la boîte de dialogue
				me.chargerContenu(data.groupe);
				
				// Ouvre la boîte de dialogue
				me.jqDetailGroupeParticipants.dialog("open");
			} else {
				window.showToast("La récupération des informations sur le groupe a échoué ; vérifiez votre connexion.");
			}
			
		});
		
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
		// Nom du groupe
		var contenuHtml = "<p><span class='dialog_detail_groupe_label'>Nom du groupe :</span> "+groupe.nom+"</p>";

		// Groupe parent
		if (groupe.parentId>0) {
			contenuHtml += "<p><span class='dialog_detail_groupe_label'>Groupe parent :</span> "+groupe.parentId+"</p>";
		} else {
			if (groupe.parentIdTmp>0) {
				contenuHtml += "<p><span class='dialog_detail_groupe_label'>Groupe parent :</span> "+groupe.parentIdTmp+" <span style='color: red; font-size: 0.9em;'>(en attente de validation)</span></p>";
			} else {
				contenuHtml += "<p><span class='dialog_detail_groupe_label'>Groupe parent :</span> Aucun</p>";
			}
		}

		// Propriétaires
		
		
		// Calendriers
		
		
		//Finalisation
		contenuHtml +=
			"<p><span class='dialog_detail_groupe_label'>Rattachement autorisé :</span> "+(groupe.rattachementAutorise ? "Oui" : "Non")+"</p>" +
			"<p><span class='dialog_detail_groupe_label'>Groupe de cours :</span> "+(groupe.estCours ? "Oui" : "Non")+"</p>" +
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
