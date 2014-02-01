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
		
		this.calendrierAnnee = new CalendrierAnnee(restManager, $("#calendar_jours_boques"), 2014);
	};
	
	
	return EcranJoursBloques;
});