define([ "RestManager", "jquerytimeselector" ], function(RestManager) {

	/**
	 * Constructeur
	 */
	var RechercheSalle = function(restManager) {
		this.restManager = restManager;
	};

	RechercheSalle.prototype.afficherFormulaire = function() {
		var me = this;
		
		$("#form_chercher_salle").dialog({
			width: 440,
			modal: true
		});
		 
		$("#timepickerDebut, #timepickerFin").timepicker({
			hourText: 'Heures',
			minuteText: 'Minutes',
			timeSeparator: 'h',
			showPeriodLabels: false,
			showNowButton: false,
			showCloseButton: false,
			showDeselectButton: false
		});
		
		$("#form_chercher_salle_valid").click(function() {
			me.validationFormulaire();
		});
		
	};

	RechercheSalle.prototype.validationFormulaire = function() {
		var valid = false;
		
		
		
		if (valid) {
			this.effectuerRequete();
		}
	};
	
	RechercheSalle.prototype.effectuerRequete = function() {
		alert('654');
	};
	
	
	RechercheSalle.prototype.afficherResultat = function() {
		
		$("#form_chercher_salle").fadeOut();

	};

	return RechercheSalle;

});