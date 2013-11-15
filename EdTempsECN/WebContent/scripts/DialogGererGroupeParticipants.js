/**
 * @module DialogGererGroupeParticipants
 */
define([ "RestManager", "GroupeGestion" ], function(RestManager, GroupeGestion) {

	/**
	 * @constructor
	 * @alias DialogGererGroupeParticipants
	 */
	var DialogGererGroupeParticipants = function(restManager) {
		this.restManager = restManager;
		this.groupeGestion = new GroupeGestion(this.restManager);
		
		this.initAppele = false; /* Permet de ne lancer l'initialisation de la dialogue une seule fois */
		this.jqGererGroupeParticipants = $("#dialog_gerer_groupe"); /* Pointeur jQuery vers la dialogue */
	};

	/**
	 * Affiche la boîte de dialogue de gestion d'un groupe de participants
	 * @param idGroupe Identifiant du groupe à gérer
	 * @param listeGroupesEnAttenteDeValidation Liste des groupes en attente de validation du groupe à gérer
	 */
	DialogGererGroupeParticipants.prototype.show = function(idGroupe, listeGroupesEnAttenteDeValidation) {
		if(!this.initAppele) {
			this.init();
			this.initAppele = true;
		}
		
		var me=this;
		
		// Récupération des données sur le groupe
		this.groupeGestion.queryGetGroupeComplet(idGroupe, function(resultCode, data) {

			if (resultCode == RestManager.resultCode_Success) {
				// Ecrit le contenu de la boîte de dialogue
				me.chargerContenu(data.groupe, listeGroupesEnAttenteDeValidation);
				
				// Ouvre la boîte de dialogue
				me.jqGererGroupeParticipants.dialog("open");
			} else {
				window.showToast("La récupération des informations sur le groupe a échoué ; vérifiez votre connexion.");
			}
			
		});
		
	};
	
	
	/**
	 * Initialise la boîte de dialogue de gestion d'un groupe de participants
	 */
	DialogGererGroupeParticipants.prototype.init = function() {

		// Affiche la boîte dialogue de gestion d'un groupe de participants
		this.jqGererGroupeParticipants.dialog({
			autoOpen: false,
			width: 510,
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
	 * Ecrit le contenu de la boite de dialogue
	 * @param listeGroupesEnAttenteDeValidation Liste des groupes en attente de validation du groupe à gérer
	 */
	DialogGererGroupeParticipants.prototype.chargerContenu = function(groupe, listeGroupesEnAttenteDeValidation) {
		
		var listRattachementTemplate = 
			"<% _.each(groupes, function(groupe) { %> <tr>" +
				"<td><%= groupe.nom %></td>" +
				"<td width='160'><input type='button' class='button' value='Accepter' title='Accepter' /><input type='button' class='button' value='Refuser' title='Refuser' /></td>" +
			"</tr> <% }); %>";

		this.jqGererGroupeParticipants.find("table").html(_.template(listRattachementTemplate, {groupes: listeGroupesEnAttenteDeValidation}));

	};

	return DialogGererGroupeParticipants;

});
