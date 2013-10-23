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
		
		$("#form_chercher_salle #dateRechercheSalle, #timepickerDebut, #timepickerFin").click(function() {
			$(this).css({border: "1px solid gray"});
		});
		
	};

	RechercheSalle.prototype.validationFormulaire = function() {
		var valid = true;
		
		// Validation de la date
		if ($("#form_chercher_salle #dateRechercheSalle").val()=="") {
			$("#form_chercher_salle #dateRechercheSalle").css({border: "2px solid red"});
			$("label[for='dateRechercheSalle']").css({color: "red"});
			valid = false;
		}
		
		// Validation de l'heure de début
		if (jQuery.trim($("#form_chercher_salle #timepickerDebut").val())=="") {
			$("#form_chercher_salle #timepickerDebut").css({border: "2px solid red"});
			$("label[for='timepickerDebut']").css({color: "red"});
			valid = false;
		}
		
		// Validation de l'heure de fin
		if (jQuery.trim($("#form_chercher_salle #timepickerFin").val())=="") {
			$("#form_chercher_salle #timepickerFin").css({border: "2px solid red"});
			$("label[for='timepickerFin']").css({color: "red"});
			valid = false;
		}

		// Si le formulaire est valide, la requête est effectuée
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