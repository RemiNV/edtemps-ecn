/**
 * Module de contrôle de la boîte de dialogue de gestion des vacances
 * @module DialogGestionVacances
 */
define([ "planning_cours/EcranJoursBloques" ], function(EcranJoursBloques) {

	/**
	 * @constructor
	 * @alias DialogGestionVacances
	 */
	var DialogGestionVacances = function(restManager, jqDialog, ecranJoursBloques) {
		this.restManager = restManager;
		this.ecranJoursBloques = ecranJoursBloques;
		this.jqDialog = jqDialog;
		
		this.initAppele = false;
	};

	
	/**
	 * Affiche la boîte de dialogue
	 */
	DialogGestionVacances.prototype.show = function() {
		if(!this.initAppele) {
			this.init();
			return;
		}
		
		// Ouvre la boîte de dialogue
		this.jqDialog.dialog("open");
	};
	
	
	/**
	 * Initialise la boîte de dialogue (une seule fois)
	 */
	DialogGestionVacances.prototype.init = function() {
		var me=this;
		
		// Créer la boîte de dialogue
		this.jqDialog.dialog({
			autoOpen: false,
			appendTo: "#dialog_hook",
			width: 360,
			modal: true,
			title: "Gestion des périodes de vacances",
			show: { effect: "fade", duration: 200 },
			hide: { effect: "explode", duration: 200 }
		});
		
		// Listener du bouton "Fermer"
		this.jqDialog.find("#bt_fermer_gestion_vacances").click(function() {
			me.jqDialog.dialog("close");
		});

		// Retourne à la méthode show()
		this.initAppele = true;
		this.show();
	};
	

	return DialogGestionVacances;

});
