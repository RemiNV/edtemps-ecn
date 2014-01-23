/**
 * Module d'affichage de l'écran de planification de cours
 * Associé au HTML templates/page_planning_cours.html
 * @module EcranPlanningCours
 */
define([], function() {
	
	/**
	 * @constructor
	 * @alias EcranPlanningCours 
	 */
	var EcranPlanningCours = function(restManager) {
		this.restManager = restManager;
		
		// TODO Placer le HTML de la dialog d'ajout d'événement dans un fichier HTML partagé avec l'écran principal (pas répéter le code)
		// TODO Placer le HTML de la dialog de recherche de salle dans un fichier HTML partagé avec l'écran principal (pas répéter le code)
	};
	
	return EcranPlanningCours;
});