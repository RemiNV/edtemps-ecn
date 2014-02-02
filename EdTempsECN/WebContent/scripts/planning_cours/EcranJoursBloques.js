/**
 * Module d'affichage de l'écran de gestion des jours bloqués
 * Associé au HTML templates/page_jours_bloques.html
 * @module EcranJoursBloques
 */
define([ "planning_cours/CalendrierAnnee", "jquery"], function(CalendrierAnnee) {
	
	/**
	 * @constructor
	 * @alias EcranJoursBloques 
	 */
	var EcranJoursBloques = function(restManager) {
		this.RestManager = restManager;
		this.jqEcran = $("#jours_bloques");
		var me = this;

		// Initialise le calendrier
		this.calendrierAnnee = new CalendrierAnnee(restManager, this.jqEcran, $("#calendar_jours_boques"), null, function(jour) {
			alert(jour.attr("date"));
		});
		
		var annee = this.calendrierAnnee.getAnnee();
		this.calendrierAnnee.chargerAnnee(annee, this.recupererJoursBloquesAnnee(annee));
		
	    // Affecte les fonctions aux flêches
	    this.jqEcran.find("#annee_precedente").click(function() {
	    	var annee = me.calendrierAnnee.getAnnee() - 1;
	    	me.calendrierAnnee.chargerAnnee(annee, me.recupererJoursBloquesAnnee(annee));
	    });
	    this.jqEcran.find("#annee_suivante").click(function() {
	    	var annee = me.calendrierAnnee.getAnnee() + 1;
	    	me.calendrierAnnee.chargerAnnee(annee, me.recupererJoursBloquesAnnee(annee));
	    });

	};
	

	/**
	 * Récupère les jours bloqués d'une année
	 * @param {int} annee Numéro de l'année
	 */
	EcranJoursBloques.prototype.recupererJoursBloquesAnnee = function(annee) {
		
	};
	
	
	return EcranJoursBloques;
});