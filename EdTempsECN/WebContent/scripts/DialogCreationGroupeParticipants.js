define([ "RestManager", "jquerymaskedinput" ], function(RestManager) {

	/**
	 * Constructeur
	 */
	function DialogCreationGroupeParticipants(restManager) {
		this.restManager = restManager;
		
		this.jqCreationGroupeForm = $("#form_creer_groupe");
		
		this.initAppele = false;
	};

	/**
	 * Affiche la boîte de dialogue de recherche d'une salle libre
	 */
	DialogCreationGroupeParticipants.prototype.show = function() {
		if(!this.initAppele) {
			this.init();
			this.initAppele = true;
		}
		
		this.jqCreationGroupeForm.dialog("open");
	};
	
	
	/**
	 * Initialise et affiche la boîte de dialogue de création d'un groupe de participants
	 */
	DialogCreationGroupeParticipants.prototype.init = function() {
		var me = this;

		// Ajout des masques aux différents champs


		// Affectation d'une méthode au clic sur le bouton "Rechercher"
		this.jqCreationGroupeForm.find("#form_creation_groupe_valid").click(function() {
			
		});

		// Affectation d'une méthode au clic sur le bouton "Annuler"
		this.jqCreationGroupeForm.find("#form_creer_groupe_annuler").click(function() {
			me.jqCreationGroupeForm.dialog("close");
		});
        
		// Affiche la boîte dialogue de recherche d'une salle libre
		this.jqCreationGroupeForm.dialog({
			autoOpen: false,
			width: 440,
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
	
	return DialogCreationGroupeParticipants;

});