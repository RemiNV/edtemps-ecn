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
		this.recupererJoursSpeciauxAnnee(annee, function(joursSpeciaux) {
			me.calendrierAnnee = new CalendrierAnnee(restManager, me.jqEcran, $("#calendar_jours_boques"), annee, joursSpeciaux, function(jour) {
				alert(jour.attr("id"));
			});
		});
		
	    // Affecte les fonctions aux flêches
	    this.jqEcran.find("#annee_precedente").click(function() {
	    	var annee = me.calendrierAnnee.getAnnee() - 1;
	    	me.recupererJoursSpeciauxAnnee(annee, function(joursSpeciaux) {
		    	me.calendrierAnnee.chargerAnnee(annee, joursSpeciaux);
	    	});
	    });
	    this.jqEcran.find("#annee_suivante").click(function() {
	    	var annee = me.calendrierAnnee.getAnnee() + 1;
	    	me.recupererJoursSpeciauxAnnee(annee, function(joursSpeciaux) {
		    	me.calendrierAnnee.chargerAnnee(annee, joursSpeciaux);
	    	});
	    });

	};
	

	/**
	 * Récupèrer les jours spéciaux d'une année
	 * @param {int} annee Numéro de l'année
	 * @param {function} callback Méthode exécutée en retour
	 */
	EcranJoursBloques.prototype.recupererJoursSpeciauxAnnee = function(annee, callback) {
		var me = this;

		var dateDebut = new Date(annee, 8, 1).getTime();	// Premier septembre
		var dateFin = new Date(annee+1, 7, 31).getTime();	// Dernier jour du mois d'aout
		
		// Recherche les jours fériés puis les jours bloqués
		this.getJoursFeries(annee, dateDebut, dateFin, function(joursFeries) {
			var joursSpeciaux = new Array();
			for (var i=0, maxI=joursFeries.length; i<maxI; i++) {
				joursSpeciaux.push(joursFeries[i]);
			}
			me.getJoursBloques(annee, dateDebut, dateFin, function(joursBloques) {
				for (var i=0, maxI=joursBloques.length; i<maxI; i++) {
					joursSpeciaux.push(joursBloques[i]);
				}
				callback(joursSpeciaux);
			});
		});

	};
	
	
	/**
	 * Récupèrer les jours fériés d'une année
	 * 
	 * @param {int} annee Numéro de l'année
	 * @param {date} dateDebut Date de début pour la recherche
	 * @param {date} dateFin Date de fin pour la recherche
	 * @param {function} callback Méthode exécutée en retour : elle reçoit les jours fériés
	 */
	EcranJoursBloques.prototype.getJoursFeries = function(annee, dateDebut, dateFin, callback) {

		this.restManager.effectuerRequete("GET", "joursferies/getJoursFeries", {
			token: this.restManager.getToken(), debut: dateDebut, fin: dateFin
		}, function(data) {
			if(data.resultCode == RestManager.resultCode_Success) {
				callback(data.data.listeJoursFeries);
			} else {
				window.showToast("Erreur lors de la récupération de la liste des jours fériés ; vérifiez votre connexion.");
				callback(null);
			}
		});
		
	};
	
	
	/**
	 * Récupèrer les jours bloqués et vacances d'une année
	 * 
	 * @param {int} annee Numéro de l'année
	 * @param {date} dateDebut Date de début pour la recherche
	 * @param {date} dateFin Date de fin pour la recherche
	 * @param {function} callback Méthode exécutée en retour : elle reçoit les jours bloqués
	 */
	EcranJoursBloques.prototype.getJoursBloques = function(annee, dateDebut, dateFin, callback) {
		
		this.restManager.effectuerRequete("GET", "joursbloques/getJoursBloques", {
			token: this.restManager.getToken(), debut: dateDebut, fin: dateFin, vacances: null
		}, function(data) {
			if(data.resultCode == RestManager.resultCode_Success) {
				callback(data.data.listeJoursBloques);
			} else {
				window.showToast("Erreur lors de la récupération de la liste des jours bloqués et des vacances ; vérifiez votre connexion.");
				callback(null);
			}
		});
		
	};
	
	
	return EcranJoursBloques;
});