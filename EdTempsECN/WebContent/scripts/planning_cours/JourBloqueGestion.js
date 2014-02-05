/**
 * Module de gestion des jours bloqués
 * @module JourBloqueGestion
 */
define([ "RestManager" ], function(RestManager) {

	/**
	 * @constructor
	 * @alias JourBloqueGestion
	 */
	var JourBloqueGestion = function(restManager, jqEcran) {
		this.restManager = restManager;
		this.jqEcran = jqEcran;
		
		this.joursFeries = null;
		this.joursBloques = null;
	};
	

	/**
	 * Récupérer les jours bloqués et fériés d'une année
	 * 
	 * @param {int} annee Numéro de l'année
	 * @param {function} callback Méthode exécutée en retour
	 */
	JourBloqueGestion.prototype.recupererJoursSpeciauxAnnee = function(annee, callback) {
		var me = this;

		var dateDebut = new Date(annee, 8, 1).getTime();	// Premier septembre
		var dateFin = new Date(annee+1, 7, 31).getTime();	// Dernier jour du mois d'aout

		// Affiche le message de chargement en cours
		this.jqEcran.find("#chargement_en_cours").show();
		
		// Recherche les jours fériés puis les jours bloqués
		this.getJoursFeries(annee, dateDebut, dateFin, function() {
			
			// Ajoute les jours fériés dans la liste des jours spéciaux
			var joursSpeciaux = new Array();
			for (var i=0, maxI=me.joursFeries.length; i<maxI; i++) {
				me.joursFeries[i].dateString = dateToString(me.joursFeries[i].date);
				joursSpeciaux.push(me.joursFeries[i]);
			}
			
			me.getJoursBloques(annee, dateDebut, dateFin, function() {
				
				// Ajoute les jours bloqués dans la liste des jours spéciaux
				for (var i=0, maxI=me.joursBloques.length; i<maxI; i++) {
					joursSpeciaux.push(me.joursBloques[i]);
				}

				// Cache le message de chargement en cours
				me.jqEcran.find("#chargement_en_cours").hide();
				
				// On a tout récupéré, on appelle la méthode de retour
				callback(joursSpeciaux);
			});
		});

	};
	
	
	/**
	 * Récupérer les jours fériés d'une année
	 * 
	 * @param {int} annee Numéro de l'année
	 * @param {date} dateDebut Date de début pour la recherche
	 * @param {date} dateFin Date de fin pour la recherche
	 * @param {function} callback Méthode exécutée en retour : elle reçoit les jours fériés
	 */
	JourBloqueGestion.prototype.getJoursFeries = function(annee, dateDebut, dateFin, callback) {
		var me = this;
		
		this.restManager.effectuerRequete("GET", "joursferies/getJoursFeries", {
			token: this.restManager.getToken(), debut: dateDebut, fin: dateFin
		}, function(data) {
			if(data.resultCode == RestManager.resultCode_Success) {
				
				// Stocke la liste des jours fériés dans une variable de module
				me.joursFeries = data.data.listeJoursFeries;

				// Appelle la méthode de retour
				callback();
				
			} else {
				window.showToast("Erreur lors de la récupération de la liste des jours fériés ; vérifiez votre connexion.");
			}
		});
		
	};
	
	
	/**
	 * Récupérer les jours bloqués et vacances d'une année
	 * 
	 * @param {int} annee Numéro de l'année
	 * @param {date} dateDebut Date de début pour la recherche
	 * @param {date} dateFin Date de fin pour la recherche
	 * @param {function} callback Méthode exécutée en retour : elle reçoit les jours bloqués
	 */
	JourBloqueGestion.prototype.getJoursBloques = function(annee, dateDebut, dateFin, callback) {
		var me = this;
		
		this.restManager.effectuerRequete("GET", "joursbloques/getJoursBloques", {
			token: this.restManager.getToken(), debut: dateDebut, fin: dateFin, vacances: null
		}, function(data) {
			if(data.resultCode == RestManager.resultCode_Success) {

				// Stocke la liste des jours fériés dans une variable de module
				me.joursBloques = data.data.listeJoursBloques;

				// Appelle la méthode de retour
				callback();
				
			} else {
				window.showToast("Erreur lors de la récupération de la liste des jours bloqués et des vacances ; vérifiez votre connexion.");
			}
		});
		
	};
	
	
	/**
	 * Supprimer un jour férié
	 * 
	 * @param {int} id Identifiant du jour férié à supprimer
	 * @param {function} callback Méthode exécutée en retour
	 */
	JourBloqueGestion.prototype.supprimerJourFerie = function(id, callback) {
		
		this.restManager.effectuerRequete("POST", "joursferies/supprimer", {
			token: this.restManager.getToken(), idJourFerie: id
		}, function(data) {
			if(data.resultCode == RestManager.resultCode_Success) {
				callback();
			} else if (data.resultCode == RestManager.resultCode_AuthorizationError) {
				window.showToast("Vous n'êtes pas autorisé à supprimer un jour férié.");
			} else {
				window.showToast("Erreur lors de la suppression du jour férié ; vérifiez votre connexion.");
			}
		});
		
	};
	
	
	/**
	 * Formatter une date (en JJ/MM/AAAA) à partir d'un getTime de date 
	 */
	function dateToString(getTime) {
		var date = new Date(getTime);
		
		return (date.getDate() >= 10 ? "" : "0") + date.getDate() + "/" +
			   (date.getMonth()+1 >= 10 ? "" : "0") + (date.getMonth()+1) + "/" +
			   date.getFullYear();
	}

	
	return JourBloqueGestion;

});
