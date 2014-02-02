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
		this.recupererJoursSpeciauxAnnee(annee, function() {
			me.calendrierAnnee = new CalendrierAnnee(restManager, me.jqEcran, $("#calendar_jours_boques"), annee, me.joursFeries, me.joursBloques, function(jour) {
				alert(jour.attr("id"));
			});
		});
		
	    // Affecte les fonctions aux flêches
	    this.jqEcran.find("#annee_precedente").click(function() {
	    	var annee = me.calendrierAnnee.getAnnee() - 1;
	    	me.recupererJoursSpeciauxAnnee(annee, function(joursFeries, joursBloques) {
		    	me.calendrierAnnee.chargerAnnee(annee, events);
	    	});
	    });
	    this.jqEcran.find("#annee_suivante").click(function() {
	    	var annee = me.calendrierAnnee.getAnnee() + 1;
	    	me.recupererJoursSpeciauxAnnee(annee, function() {
		    	me.calendrierAnnee.chargerAnnee(annee, me.joursFeries, me.joursBloques);
	    	});
	    });

	};
	

	/**
	 * Récupèrer les jours spéciaux d'une année
	 * @param {int} annee Numéro de l'année
	 * @param {function} callback Méthode exécutée en retour
	 */
	EcranJoursBloques.prototype.recupererJoursSpeciauxAnnee = function(annee, callback) {

		var dateDebut = new Date(annee, 8, 1).getTime();	// Premier septembre
		var dateFin = new Date(annee+1, 7, 31).getTime();	// Dernier jour du mois d'aout
		
		this.joursFeries = null;
		this.joursBloques = null;
		var me = this;
		
		this.getJoursFeries(annee, dateDebut, dateFin, function() {
			me.getJoursBloques(annee, dateDebut, dateFin, function() {
				callback();
			});
		});

	};
	
	
	/**
	 * Récupèrer les jours fériés d'une année
	 * 
	 * @param {int} annee Numéro de l'année
	 * @param {date} dateDebut Date de début pour la recherche
	 * @param {date} dateFin Date de fin pour la recherche
	 * @param {function} callback Méthode exécutée en retour
	 */
	EcranJoursBloques.prototype.getJoursFeries = function(annee, dateDebut, dateFin, callback) {
		var me = this;

		this.restManager.effectuerRequete("GET", "joursferies/getJoursFeries", {
			token: this.restManager.getToken(), debut: dateDebut, fin: dateFin
		}, function(data) {
			if(data.resultCode == RestManager.resultCode_Success) {
				me.joursFeries = data.data.listeJoursFeries;
				callback();
			} else {
				window.showToast("Erreur lors de la récupération de la liste des jours fériés ; vérifiez votre connexion.");
				me.joursFeries = null;
			}
		});
		
	};
	
	
	/**
	 * Récupèrer les jours bloqués et vacances d'une année
	 * 
	 * @param {int} annee Numéro de l'année
	 * @param {date} dateDebut Date de début pour la recherche
	 * @param {date} dateFin Date de fin pour la recherche
	 * @param {function} callback Méthode exécutée en retour
	 */
	EcranJoursBloques.prototype.getJoursBloques = function(annee, dateDebut, dateFin, callback) {
		var me = this;
		
		this.restManager.effectuerRequete("GET", "joursbloques/getJoursBloques", {
			token: this.restManager.getToken(), debut: dateDebut, fin: dateFin, vacances: null
		}, function(data) {
			if(data.resultCode == RestManager.resultCode_Success) {
				me.joursBloques = data.data.listeJoursBloques;
				callback();
			} else {
				window.showToast("Erreur lors de la récupération de la liste des jours bloqués et des vacances ; vérifiez votre connexion.");
				me.joursBloques = null;
			}
		});
		
	};
	
	
	return EcranJoursBloques;
});