/**
 * Module d'affichage de l'écran de gestion des jours bloqués
 * Associé au HTML templates/page_jours_bloques.html
 * @module EcranJoursBloques
 */
define([ "planning_cours/CalendrierAnnee", "RestManager", "jquery"], function(CalendrierAnnee, RestManager) {
	
	/**
	 * @constructor
	 * @alias EcranJoursBloques
	 */
	var EcranJoursBloques = function(restManager) {
		this.restManager = restManager;
		this.jqEcran = $("#jours_bloques");
		var me = this;

		// Si l'utilisateur n'a pas droit à gérer les jours bloqués, on le redirige
		if (!this.restManager.aDroit(RestManager.actionsEdtemps_CreerGroupeCours)) {
			document.location.href = "#agenda";
		}

		// Récupère l'année scolaire à afficher en fonction de la date du jour
		var today = new Date();
		var annee = today.getFullYear();	// C'est l'année de départ de l'année scolaire. Par exemple, pour l'année scolaire 2013-2014, this.annee vaut 2013.
		if (today.getMonth() >= 0 && today.getMonth() <= 7) annee = today.getFullYear()-1;

		// Initialise le calendrier
		this.recupererJoursBloquesAnnee(annee, function(events) {
			me.calendrierAnnee = new CalendrierAnnee(restManager, me.jqEcran, $("#calendar_jours_boques"), annee, events, function(jour) {
				alert(jour.attr("id"));
			});
		});
		
	    // Affecte les fonctions aux flêches
	    this.jqEcran.find("#annee_precedente").click(function() {
	    	var annee = me.calendrierAnnee.getAnnee() - 1;
	    	me.recupererJoursBloquesAnnee(annee, function(events) {
		    	me.calendrierAnnee.chargerAnnee(annee, events);
	    	});
	    });
	    this.jqEcran.find("#annee_suivante").click(function() {
	    	var annee = me.calendrierAnnee.getAnnee() + 1;
	    	me.recupererJoursBloquesAnnee(annee, function(events) {
		    	me.calendrierAnnee.chargerAnnee(annee, events);
	    	});
	    });

	};
	

	/**
	 * Récupèrer les jours bloqués d'une année
	 * @param {int} annee Numéro de l'année
	 * @param {function} callback Méthode exécutée en retour
	 */
	EcranJoursBloques.prototype.recupererJoursBloquesAnnee = function(annee, callback) {

		var dateDebut = new Date(annee, 8, 1).getTime();	// Premier septembre
		var dateFin = new Date(annee+1, 7, 31).getTime();	// Dernier jour du mois d'aout
		
		// Exécute la requête pour récupérer la liste des jours fériés de l'année
		this.restManager.effectuerRequete("GET", "joursferies/getJoursFeriesPeriode", {
			token: this.restManager.getToken(), debut: dateDebut, fin: dateFin
		}, function(data) {
			if(data.resultCode == RestManager.resultCode_Success) {
				callback(data.data.listeJoursFeries);
			} else if(data.resultCode == RestManager.resultCode_AuthorizationError) {
				window.showToast("Vous n'êtes pas autorisé à récupérer la liste des jours fériés.");
				callback(null);
			} else {
				window.showToast("Erreur lors de la récupération de la liste des jours fériés ; vérifiez votre connexion.");
				callback(null);
			}
		});

	};
	
	
	return EcranJoursBloques;
});