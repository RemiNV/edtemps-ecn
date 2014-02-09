/**
 * Module de gestion des jours bloqués
 * @module JourBloqueGestion
 */
define([ "RestManager", "lib/fullcalendar.translated.min" ], function(RestManager) {

	/**
	 * @constructor
	 * @alias JourBloqueGestion
	 */
	var JourBloqueGestion = function(restManager, jqEcran) {
		this.restManager = restManager;
		this.jqEcran = jqEcran;
	};
	

	/**
	 * Récupérer les vacances, jours bloqués et jours fériés d'une année scolaire
	 * 
	 * @param {int} annee Numéro de l'année de début
	 * @param {function} callback Méthode exécutée en cas de réussite
	 */
	JourBloqueGestion.prototype.recupererJoursSpeciauxAnnee = function(annee, callback) {
		var me = this;

		var dateDebut = new Date(annee, 8, 1).getTime();	// Premier septembre
		var dateFin = new Date(annee+1, 7, 31).getTime();	// Dernier jour du mois d'août

		// Listes non ordonnées
		this.joursFeries = new Array();
		this.joursBloques = new Array();
		this.vacances = new Array();

		// Listes indexées par les identifiants des jours
		this.joursFeriesTries = new Object();
		this.joursBloquesTries = new Object();
		this.vacancesTriees = new Object();
		
		// Affiche le message de chargement en cours
		this.jqEcran.find("#chargement_en_cours").show();

		// Récupère les jours fériés
		this.getJoursFeries(annee, dateDebut, dateFin, function() {
			
			var joursSpeciaux = me.joursFeries;
			
			// Récupère les vacances et jours bloqués
			me.getPeriodesBloquees(annee, dateDebut, dateFin, function() {
				
				joursSpeciaux = joursSpeciaux.concat(me.joursBloques);
				joursSpeciaux = joursSpeciaux.concat(me.vacances);
				
				// Cache le message de chargement en cours
				me.jqEcran.find("#chargement_en_cours").hide();
				
				// On a tout récupéré, on appelle la méthode de retour
				callback(joursSpeciaux);
			});
		});

	};
	
	
	/**
	 * Récupérer les jours fériés d'une année scolaire
	 * 
	 * @param {int} annee Numéro de l'année de début
	 * @param {date} dateDebut Date de début pour la recherche
	 * @param {date} dateFin Date de fin pour la recherche
	 * @param {function} callback Méthode exécutée en cas de réussite
	 */
	JourBloqueGestion.prototype.getJoursFeries = function(annee, dateDebut, dateFin, callback) {
		var me = this;
		
		this.restManager.effectuerRequete("GET", "joursferies/getJoursFeries", {
			token: this.restManager.getToken(), debut: dateDebut, fin: dateFin
		}, function(data) {
			if(data.resultCode == RestManager.resultCode_Success) {
				
				// Stocke la liste des jours fériés dans la variable de module
				me.joursFeries = data.data.listeJoursFeries;

				// Trie les jours dans un objet indexé par identifiant
				for (var i=0, maxI=me.joursFeries.length; i<maxI; i++) {
					me.joursFeries[i].dateString = $.fullCalendar.formatDate(new Date(me.joursFeries[i].date), "dd/MM/yyyy");
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
	 * Récupérer les périodes bloquées d'une année scolaire
	 * 
	 * @param {int} annee Numéro de l'année de début
	 * @param {date} dateDebut Date de début pour la recherche
	 * @param {date} dateFin Date de fin pour la recherche
	 * @param {function} callback Méthode exécutée en cas de réussite
	 */
	JourBloqueGestion.prototype.getPeriodesBloquees = function(annee, dateDebut, dateFin, callback) {
		var me = this;
		
		this.restManager.effectuerRequete("GET", "periodesbloquees/getperiodesbloquees", {
			token: this.restManager.getToken(), debut: dateDebut, fin: dateFin, vacances: null
		}, function(data) {
			if(data.resultCode == RestManager.resultCode_Success) {

				// Sépare les vacances et les jours bloqués
				for (var i=0, maxI=data.data.listePeriodesBloquees.length; i<maxI; i++) {
					var jour = data.data.listePeriodesBloquees[i];
					
					if (jour.vacances) {
						
						// Ajoute les vacances dans la liste non triée
						me.vacances.push(jour);
						
						// Trie les jours dans un objet indexé par identifiant
						me.vacancesTriees[jour.id] = jour;
						
					} else {

						// Ajoute les vacances dans la liste non triée
						me.joursBloques.push(jour);

						// Trie les jours dans un objet indexé par identifiant
						me.joursBloquesTries[jour.id] = jour;
						
					}
				}
				
				// Appelle la méthode de retour
				callback();
				
			} else {
				window.showToast("Erreur lors de la récupération de la liste des périodes bloquées ; vérifiez votre connexion.");
			}
		});
		
	};
	
	
	/**
	 * Supprimer un jour férié
	 * 
	 * @param {int} id Identifiant du jour férié à supprimer
	 * @param {function} callback Méthode exécutée en cas de réussite
	 */
	JourBloqueGestion.prototype.supprimerJourFerie = function(id, callback) {
		var me = this;
		this.jqEcran.find("#chargement_en_cours").show();	// Affiche le message de chargement en cours

		this.restManager.effectuerRequete("POST", "joursferies/supprimer", {
			token: this.restManager.getToken(), idJourFerie: id
		}, function(data) {
			if(data.resultCode == RestManager.resultCode_Success) {
				callback();
				me.jqEcran.find("#chargement_en_cours").hide();	// Cache le message de chargement en cours
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
	 * @param {boolean} fermeture Vrai si c'est un jour de fermeture, Faux sinon
	 * @param {function} callback Méthode exécutée en cas de réussite
	 */
	JourBloqueGestion.prototype.ajouterJourFerie = function(libelle, date, fermeture, callback) {
		var me = this;
		this.jqEcran.find("#chargement_en_cours").show();	// Affiche le message de chargement en cours

		this.restManager.effectuerRequete("POST", "joursferies/ajouter", {
			token: this.restManager.getToken(), libelle: libelle,
			date: date.getTime(), fermeture: fermeture
		}, function(data) {
			if(data.resultCode == RestManager.resultCode_Success) {
				callback();
				me.jqEcran.find("#chargement_en_cours").hide();	// Cache le message de chargement en cours
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
	 * @param {boolean} fermeture Vrai si c'est un jour de fermeture, Faux sinon
	 * @param {function} callback Méthode exécutée en cas de réussite
	 */
	JourBloqueGestion.prototype.modifierJourFerie = function(id, libelle, date, fermeture, callback) {
		var me = this;
		this.jqEcran.find("#chargement_en_cours").show();	// Affiche le message de chargement en cours

		this.restManager.effectuerRequete("POST", "joursferies/modifier", {
			token: this.restManager.getToken(), idJourFerie: id,
			libelle: libelle, date: date.getTime(), fermeture: fermeture
		}, function(data) {
			if(data.resultCode == RestManager.resultCode_Success) {
				callback();
				me.jqEcran.find("#chargement_en_cours").hide();	// Cache le message de chargement en cours
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
	 * Ajouter automatiquement les jours fériés
	 * 
	 * @param {int} annee Annee de création, pour l'année scolaire annee<>annee+1
	 * @param {function} callback Méthode exécutée en cas de réussite
	 */
	JourBloqueGestion.prototype.ajouterAutoJourFerie = function(annee, callback) {
		var me = this;
		this.jqEcran.find("#chargement_en_cours").show();	// Affiche le message de chargement en cours

		this.restManager.effectuerRequete("POST", "joursferies/ajoutautomatique", {
			token: this.restManager.getToken(), annee: annee
		}, function(data) {
			if(data.resultCode == RestManager.resultCode_Success) {
				var nbJourAjoutes = data.data.listeAjoutes.length;
				callback(nbJourAjoutes);
				
				var message;
				if (nbJourAjoutes==0) {
					message = "Aucun jour férié ajouté";
				} else if (nbJourAjoutes==1) {
					message = "1 jour férié a été ajouté";
				} else {
					message = nbJourAjoutes + " jours fériés ont été ajoutés";
				}
				
				me.jqEcran.find("#chargement_en_cours").hide();	// Cache le message de chargement en cours

				window.showToast(message);
			} else if (data.resultCode == RestManager.resultCode_AuthorizationError) {
				window.showToast("Vous n'êtes pas autorisé à ajouter des jours fériés.");
			} else {
				window.showToast("Erreur lors de la création des jours fériés ; vérifiez votre connexion.");
			}
		});
		
	};
	
	
	/**
	 * Ajouter une période bloquée / vacances
	 * 
	 * @param {string} libelle Libellé de la période bloquée
	 * @param {date} dateDebut Date de début de la période bloquée
	 * @param {date} dateFin Date de fin de la période bloquée
	 * @param {boolean} vacances Vrai si ce sont des vacances, Faux sinon
	 * @param {function} callback Méthode exécutée en cas de réussite
	 */
	JourBloqueGestion.prototype.ajouterPeriodeBloquee = function(libelle, dateDebut, dateFin, listeGroupes, vacances, callback) {
		var me = this;
		this.jqEcran.find("#chargement_en_cours").show();	// Affiche le message de chargement en cours

		this.restManager.effectuerRequete("POST", "periodesbloquees/ajouter", {
			token: this.restManager.getToken(),
			periode: JSON.stringify({
				libelle: libelle,
				listeGroupes: listeGroupes,
				dateDebut: dateDebut.getTime(),
				dateFin: dateFin.getTime(),
				vacances: vacances
			})
		}, function(data) {
			if(data.resultCode == RestManager.resultCode_Success) {
				callback();
				me.jqEcran.find("#chargement_en_cours").hide();	// Cache le message de chargement en cours
				window.showToast("Période bloquée ajoutée");
			} else if (data.resultCode == RestManager.resultCode_AlphanumericRequired) {
				window.showToast("Le libellé de la période bloquée doit être alphanumérique.");
			} else if (data.resultCode == RestManager.resultCode_InvalidObject) {
				window.showToast("Les paramètres de la période sont erronés.");
			} else if (data.resultCode == RestManager.resultCode_AuthorizationError) {
				window.showToast("Vous n'êtes pas autorisé à ajouter une période bloquée.");
			} else {
				window.showToast("Erreur lors de l'ajout de la période bloquée ; vérifiez votre connexion.");
			}
		});
		
	};
	
	
	/**
	 * Modifier une période bloquée / vacances
	 * 
	 * @param {int} id Identifiant de la période bloquée
	 * @param {string} libelle Libellé de la période bloquée
	 * @param {date} dateDebut Date de début de la période bloquée
	 * @param {date} dateFin Date de fin de la période bloquée
	 * @param {boolean} vacances Vrai si ce sont des vacances, Faux sinon
	 * @param {function} callback Méthode exécutée en cas de réussite
	 */
	JourBloqueGestion.prototype.modifierPeriodeBloquee = function(id, libelle, dateDebut, dateFin, listeGroupes, vacances, callback) {
		var me = this;
		this.jqEcran.find("#chargement_en_cours").show();	// Affiche le message de chargement en cours

		this.restManager.effectuerRequete("POST", "periodesbloquees/modifier", {
			token: this.restManager.getToken(),
			periode: JSON.stringify({
				idPeriodeBloquee: parseInt(id),
				libelle: libelle,
				listeGroupes: listeGroupes,
				dateDebut: dateDebut.getTime(),
				dateFin: dateFin.getTime(),
				vacances: vacances
			})
		}, function(data) {
			if(data.resultCode == RestManager.resultCode_Success) {
				callback();
				me.jqEcran.find("#chargement_en_cours").hide();	// Cache le message de chargement en cours
				window.showToast("Période bloquée modifiée");
			} else if (data.resultCode == RestManager.resultCode_AlphanumericRequired) {
				window.showToast("Le libellé de la période bloquée doit être alphanumérique.");
			} else if (data.resultCode == RestManager.resultCode_InvalidObject) {
				window.showToast("Les paramètres de la période sont erronés.");
			} else if (data.resultCode == RestManager.resultCode_AuthorizationError) {
				window.showToast("Vous n'êtes pas autorisé à modifier une période bloquée.");
			} else {
				window.showToast("Erreur lors de la modification de la période bloquée ; vérifiez votre connexion.");
			}
		});
		
	};
	
	
	/**
	 * Supprimer une période bloquée
	 * 
	 * @param {int} id Identifiant de la période à supprimer
	 * @param {function} callback Méthode exécutée en cas de réussite
	 */
	JourBloqueGestion.prototype.supprimerPeriodeBloquee = function(id, callback) {
		var me = this;
		this.jqEcran.find("#chargement_en_cours").show();	// Affiche le message de chargement en cours

		this.restManager.effectuerRequete("POST", "periodesbloquees/supprimer", {
			token: this.restManager.getToken(), idPeriodeBloquee: id
		}, function(data) {
			if(data.resultCode == RestManager.resultCode_Success) {
				callback();
				me.jqEcran.find("#chargement_en_cours").hide();	// Cache le message de chargement en cours
				window.showToast("Période bloquée supprimée");
			} else if (data.resultCode == RestManager.resultCode_AuthorizationError) {
				window.showToast("Vous n'êtes pas autorisé à supprimer une période bloquée.");
			} else {
				window.showToast("Erreur lors de la suppression de la période bloquée ; vérifiez votre connexion.");
			}
		});
		
	};
	
	
	/**
	 * Rechercher les jours bloqués à une date donnée dans la liste des jours bloqués du module
	 * 
	 * @param {date} date Date à laquelle il faut chercher
	 */
	JourBloqueGestion.prototype.getJoursBloquesParJour = function(date) {

		var liste = new Array();
		
		for (var i=0, maxI=this.joursBloques.length; i<maxI; i++) {
			var jour = this.joursBloques[i];

			var debut = new Date(date.getFullYear(), date.getMonth(), date.getDate(), 0, 0, 0);
			var fin = new Date(date.getFullYear(), date.getMonth(), date.getDate(), 23, 59, 59);
			
			if (jour.dateDebut >= debut.getTime() && jour.dateDebut <= fin.getTime()) {
				jour.strHeureDebut = $.fullCalendar.formatDate(new Date(jour.dateDebut), "HH:mm");
				jour.strHeureFin = $.fullCalendar.formatDate(new Date(jour.dateFin), "HH:mm");

				// Créer une chaîne de caractère des noms de groupes associés à ce jour
				var str = "";
				for (var j=0, maxJ=jour.listeGroupes.length; j<maxJ; j++) {
					if (str!="") str += ", ";
					str += jour.listeGroupes[j].nom;
				}
				jour.strGroupesAssocies = str;

				liste.push(jour);
			}
		}
		
		return liste;
		
	};
	

	/**
	 * Teste si le jour passé en paramètre est un jour férié (non fermeture)
	 * 
	 * @param {date} date Date de recherche
	 */
	JourBloqueGestion.prototype.isFerie = function(date) {
		
		for (var i=0, maxI=this.joursFeries.length; i<maxI; i++) {
			if (this.joursFeries[i].date==date.getTime() && !this.joursFeries[i].fermeture) return true;
		}
		
		return false;
		
	};
	
	
	return JourBloqueGestion;

});
