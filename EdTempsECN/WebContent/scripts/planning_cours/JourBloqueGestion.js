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
		
		// Listes non ordonnées
		this.joursFeries = new Array();
		this.joursBloques = new Array();

		// Listes indexées par les identifiants des jours
		this.joursFeriesTries = new Object();
		this.joursBloquesTries = new Object();
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
				
				// Stocke la liste des jours fériés dans la variable de module
				me.joursFeries = data.data.listeJoursFeries;

				// Trie les jours dans un objet
				for (var i=0, maxI=me.joursFeries.length; i<maxI; i++) {
					me.joursFeries[i].dateString = dateToString(me.joursFeries[i].date);
					me.joursFeriesTries[me.joursFeries[i].id] = me.joursFeries[i];
				}

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

				// Stocke la liste des jours fériés dans la variable de module
				me.joursBloques = data.data.listeJoursBloques;

				// Trie les jours dans un objet
				for (var i=0, maxI=me.joursBloques.length; i<maxI; i++) {
					me.joursBloquesTries[me.joursBloques[i].id] = me.joursBloques[i];
				}

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
				window.showToast("Jour férié supprimé");
			} else if (data.resultCode == RestManager.resultCode_AuthorizationError) {
				window.showToast("Vous n'êtes pas autorisé à supprimer un jour férié.");
			} else {
				window.showToast("Erreur lors de la suppression du jour férié ; vérifiez votre connexion.");
			}
		});
		
	};
	
	
	/**
	 * Ajouter un jour férié
	 * 
	 * @param {string} libelle Libellé du jour férié à ajouter
	 * @param {date} date Date du jour férié
	 * @param {function} callback Méthode exécutée en retour
	 */
	JourBloqueGestion.prototype.ajouterJourFerie = function(libelle, date, callback) {
		
		this.restManager.effectuerRequete("POST", "joursferies/ajouter", {
			token: this.restManager.getToken(), libelle: libelle, date: date.getTime()
		}, function(data) {
			if(data.resultCode == RestManager.resultCode_Success) {
				callback();
				window.showToast("Jour férié ajouté");
			} else if (data.resultCode == RestManager.resultCode_AlphanumericRequired) {
				window.showToast("Le libellé du jour doit être alphanumérique.");
			} else if (data.resultCode == RestManager.resultCode_DayTaken) {
				window.showToast("Un jour férié est déjà défini à cette date.");
			} else if (data.resultCode == RestManager.resultCode_AuthorizationError) {
				window.showToast("Vous n'êtes pas autorisé à ajouter un jour férié.");
			} else {
				window.showToast("Erreur lors de l'ajout du jour férié ; vérifiez votre connexion.");
			}
		});
		
	};
	
	
	/**
	 * Modifier un jour férié
	 * 
	 * @param {int} id Identifiant du jour férié à modifier
	 * @param {string} libelle Libellé du jour férié
	 * @param {date} date Date du jour férié
	 * @param {function} callback Méthode exécutée en retour
	 */
	JourBloqueGestion.prototype.modifierJourFerie = function(id, libelle, date, callback) {
		
		this.restManager.effectuerRequete("POST", "joursferies/modifier", {
			token: this.restManager.getToken(), idJourFerie: id, libelle: libelle, date: date.getTime()
		}, function(data) {
			if(data.resultCode == RestManager.resultCode_Success) {
				callback();
				window.showToast("Jour férié modifié");
			} else if (data.resultCode == RestManager.resultCode_AlphanumericRequired) {
				window.showToast("Le libellé du jour doit être alphanumérique.");
			} else if (data.resultCode == RestManager.resultCode_DayTaken) {
				window.showToast("Un jour férié est déjà défini à cette date.");
			} else if (data.resultCode == RestManager.resultCode_AuthorizationError) {
				window.showToast("Vous n'êtes pas autorisé à modifier un jour férié.");
			} else {
				window.showToast("Erreur lors de la modification du jour férié ; vérifiez votre connexion.");
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
