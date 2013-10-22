define([ "RestManager" ], function(RestManager) {

	/**
	 * Constructeur
	 */
	var RechercheSalle = function(restManager) {
		this.restManager = restManager;
	};
	
	RechercheSalle.prototype.afficherFormulaire = function() {
		$("#form_chercher_salle").html("<p>TOTO</p>");
		$("#form_chercher_salle").dialog();
	};
	
	return RechercheSalle;

});