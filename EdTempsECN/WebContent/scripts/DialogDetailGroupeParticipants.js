/**
 * Module de contrôle de la boîte de dialogue de détail d'un groupe de participants
 * @module DialogDetailGroupeParticipants
 */
define([ "RestManager", "GroupeGestion" ], function(RestManager, GroupeGestion) {

	/**
	 * @constructor
	 * @alias module:DialogDetailGroupeParticipants
	 */
	var DialogDetailGroupeParticipants = function(restManager, jqDialog) {
		this.restManager = restManager;
		this.jqDialog = jqDialog;

		this.groupeGestion = new GroupeGestion(this.restManager);

		// Permet de ne lancer l'initialisation de la dialogue qu'une seule fois 
		this.initAppele = false;
	};

	/**
	 * Affiche la boîte de dialogue de détail d'un groupe de participants
	 * @param {number} idGroupe Identifiant du groupe à afficher
	 */
	DialogDetailGroupeParticipants.prototype.show = function(idGroupe) {
		if(!this.initAppele) {
			this.init();
		}
		
		var me=this;
		
		// Récupération des données sur le groupe à afficher
		this.groupeGestion.queryGetGroupeComplet(idGroupe, function(resultCode, data) {
			if (resultCode == RestManager.resultCode_Success) {
				me.chargerContenu(data.groupe);
			} else {
				window.showToast("La récupération des informations sur le groupe a échoué ; vérifiez votre connexion.");
			}
		});
	};
	
	/**
	 * Initialise la boîte de dialogue de détail d'un groupe de participants
	 */
	DialogDetailGroupeParticipants.prototype.init = function() {

		// Affiche la boîte dialogue de détail d'un groupe de participants
		this.jqDialog.dialog({
			autoOpen: false,
			appendTo: "#dialog_hook",
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
	 * @param {groupe} groupe Groupe à afficher
	 */
	DialogDetailGroupeParticipants.prototype.chargerContenu = function(groupe) {
		var me = this;
		
		// Prépare le contenu de la fenêtre
		// Nom du groupe
		var contenuHtml = "<p><span class='dialog_detail_groupe_label'>Nom du groupe :</span> "+groupe.nom+"</p>";

		// Groupe parent
		if (groupe.parentId>0) {
			contenuHtml += "<p><span class='dialog_detail_groupe_label'>Groupe parent :</span> "+groupe.parent.nom+"</p>";
		} else if (groupe.parentIdTmp>0) {
			contenuHtml += "<p><span class='dialog_detail_groupe_label'>Groupe parent :</span> "+groupe.parent.nom+" <i style='color: red;'>(En attente de validation)</i></p>";
		}

		// Propriétaires
		contenuHtml += "<p><span class='dialog_detail_groupe_label'>Propriétaire(s) :</span> ";
		for (var i=0, maxI=groupe.proprietaires.length; i<maxI; i++) {
			contenuHtml += groupe.proprietaires[i].prenom + " " + groupe.proprietaires[i].nom;
			if (i<maxI-1) contenuHtml += ", ";
		}
		contenuHtml += "</p>";
		
		// Calendriers
		var maxI = groupe.calendriers.length;
		if (maxI>0) {
			contenuHtml += "<p><span class='dialog_detail_groupe_label'>Calendrier(s) lié(s) :</span> ";
			for (var i=0; i<maxI; i++) {
				contenuHtml += groupe.calendriers[i].nom;
				if (i<maxI-1) contenuHtml += ", ";
			}
			contenuHtml += "</p>";
		}
		
		//Finalisation
		contenuHtml +=
			"<p><span class='dialog_detail_groupe_label'>Rattachement autorisé :</span> <input type='checkbox' disabled='disabled' "+(groupe.rattachementAutorise ? "checked" : "")+" title='"+(groupe.rattachementAutorise ? "Oui" : "Non")+"' /></p>" +
			"<p><span class='dialog_detail_groupe_label'>Groupe de cours :</span> <input type='checkbox' disabled='disabled' "+(groupe.estCours ? "checked" : "")+" title='"+(groupe.estCours ? "Oui" : "Non")+"' /></p>" +
			"<div id='dialog_detail_groupe_boutons'><input class='button' type='button' value='Fermer' id='dialog_detail_groupe_fermer' /></div>";

		// Ecrit le contenu dans la fenêtre
		this.jqDialog.html(contenuHtml);

		// Affectation d'une méthode au clic sur le bouton "Fermer"
		this.jqDialog.find("#dialog_detail_groupe_fermer").click(function() {
			me.jqDialog.dialog("close");
		});
		
		// Affiche la boîte de dialogue
		this.jqDialog.dialog("open");

	};

	return DialogDetailGroupeParticipants;

});
