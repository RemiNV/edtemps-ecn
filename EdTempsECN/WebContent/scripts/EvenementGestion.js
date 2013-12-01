/**
 * Module de gestion/récupération des évènements, utilise un système de cache
 * @module EvenementGestion
 */
define(["RestManager"], function(RestManager) {

	/**
	 * @constructor
	 * @alias module:EvenementGestion
	 */
	var EvenementGestion = function(restManager) {
		this.restManager = restManager;
		
		this.cachedEvents = Object();
		this.cachedEvents[EvenementGestion.CACHE_MODE_MES_ABONNEMENTS] = Array();
		this.cachedEvents[EvenementGestion.CACHE_MODE_GROUPE] = Array();
		this.cachedEvents[EvenementGestion.CACHE_MODE_SALLE] = Array();
		this.cachedEvents[EvenementGestion.CACHE_MODE_MES_EVENEMENTS] = Array();
		
		// Groupes et calendriers récupérés mémorisés
		this.matieresCalendriers = null;
		this.typesCalendriers = null;
	};
	
	EvenementGestion.CACHE_MODE_GROUPE = 1;
	EvenementGestion.CACHE_MODE_SALLE = 2;
	EvenementGestion.CACHE_MODE_MES_EVENEMENTS = 3;
	EvenementGestion.CACHE_MODE_MES_ABONNEMENTS = 4;

	/**
	 * Listing des évènements auxquels un utilisateur est abonné. Donne aussi les calendriers et groupes auxquels il est abonné.
	 * Arguments : 
	 * dateDebut/dateFin : intervalle de recherche pour les évènements
	 * callback : fonction appelée une fois la requête effectuée. Paramètres de callback : 
	 * - resultCode (entier) : code de retour de la requête. Vaut RestManager.resultCode_Success en cas de succès.
	 * - data : objet contenant les évènements, calendriers et groupes demandés.
	 * 	Non fourni si resultCode != RestManager.resultCode_Success
	 * 
	 * Exemple de format de l'objet fourni : 
	 * { evenements:
		[
			{id: 1, nom:"cours THERE", dateDebut: 1384708500000, dateFin: 1384712100000, calendriers: [7], 
				salles: [{id: 123, nom: "Salle B1", batiment: "B", capacite: 30, niveau: 1, numero: 1, materiels: []}]},
			{id: 5, nom:"cours THERF", dateDebut: 1384798500000, dateFin: 1384802100000, calendriers: [54],
				salles: [{id: 155, nom: "Salle B2", batiment: "B", capacite: 30, niveau: 1, numero: 2, materiels: []}]}
					
		],
	 *   calendriers: 
	 * 	[
	 * 		{id: 7, nom:"THERE Groupe L", type: "TD", matiere: "THERE", proprietaires: [1,5,8,7]},
			{id: 54, nom:"THERF Groupe L", type: "TD", matiere: "THERF", proprietaires: [24]}
	 * 	],
	 *   groupes: 
	 * 	[
	 * 		{id: 42, nom:"Groupe L", parentId: 24, rattachementAutorise: true, estCours: true, estCalendrierUnique: false, 
	 * 			calendriers: [1, 5], proprietaires: [2]},
	 * 		{id: 24, nom:"Promo B", parentId: 12, rattachementAutorise: false, estCours: true, estCalendrierUnique: false, 
	 * 			calendriers: [7], proprietaires: [2]},
	 * 		{id: 12, nom:"EI1", parentId: 6, rattachementAutorise: false, estCours: true, estCalendrierUnique: false, 
	 * 			calendriers: [], proprietaires: [2]},
	 * 		{id: 12, nom:"Elèves ingénieur", parent: null, rattachementAutorise: false, estCours: true, 
	 * 			estCalendrierUnique: false, calendriers: [], proprietaires: [2]},
	 * 	]
	 * }
	 */
	EvenementGestion.prototype.queryAbonnements = function(dateDebut, dateFin, callback) {
	
		this.restManager.effectuerRequete("GET", "abonnements", {
			token: this.restManager.getToken(),
			debut: dateDebut.getTime(),
			fin: dateFin.getTime()
		}, function(data) {
			if(data.resultCode == RestManager.resultCode_Success) {
				callback(data.resultCode, data.data);
			}
			else {
				callback(data.resultCode);
			}
		});
	};
	
	/**
	 * Fonction générique de récupération d'évènements. Utilisable pour la récupération de : 
	 * - Evénements de mes abonnements
	 * - Evénements dont je suis intervenant
	 * - Evénements d'une salle
	 * - Evénements d'un groupe
	 * 
	 * @param url URL à laquelle faire la requête
	 * @param dateDebut Début de la fenêtre de recherche
	 * @param dateFin Fin de la fenêtre de recherche
	 * @param callback Fonction à appeler pour fournir le résultat de la requête (paramètres resultCode et data)
	 * @param idSalle ID de la salle pour laquelle les évènements sont à récupérer ; à ne préciser que pour lister les évènements d'une salle
	 * @param idGroupe ID du groupe pour lequel les évènements sont à récupérer ; à ne préciser que pour lister les évènements d'un groupe
	 */
	EvenementGestion.prototype.queryEvenements = function(url, dateDebut, dateFin, callback, idSalle, idGroupe) {
		
		var params = {
			token: this.restManager.getToken(),
			debut: dateDebut.getTime(),
			fin: dateFin.getTime()
		};
		
		if(idSalle) {
			params.idSalle = idSalle;
		}
		
		if(idGroupe) {
			params.idGroupe = idGroupe;
		}
		
		this.restManager.effectuerRequete("GET", url, params, function(data) {
			if(data.resultCode == RestManager.resultCode_Success) {
				callback(data.resultCode, data.data);
			}
			else {
				callback(data.resultCode);
			}
		});
	};
	
	/**
	 * Récupération des abonnements de l'utilisateur (evenements + calendriers + groupes)
	 * Arguments start, end : bornes de recherche pour les évènements
	 * callback : fonction prenant les arguments : 
	 * - resultCode (entier) : code de retour de la requête. Vaut RestManager.resultCode_Success en cas de succès.
	 * - data : objet contenant les évènements, calendriers et groupes demandés.
	 * data contient 3 attributs : 
	 * -- evenements (évènements au format de parseEventsFullcalendar)
	 * -- calendriers (au format de queryAbonnements)
	 * -- groupes (groupes au format de queryAbonnements)
	 * 	Data est non fourni si resultCode != RestManager.resultCode_Success */
	EvenementGestion.prototype.getAbonnements = function(start, end, callback) {
		var me = this;
		
		this.queryAbonnements(start, end, function(resultCode, data) {
			if(resultCode == RestManager.resultCode_Success) {
				
				// Parsing des calendriers pour les matières & types d'évènement
				me.matieresCalendriers = Object();
				me.typesCalendriers = Object();
				for(var i=0, maxI = data.calendriers.length; i<maxI; i++) {
					me.matieresCalendriers[data.calendriers[i].id] = data.calendriers[i].matiere;
					me.typesCalendriers[data.calendriers[i].id] = data.calendriers[i].type;
				}
				
				var parsedEvents = me.parseEventsSimplesFullcalendar(data.evenements);
				
				// Ajout des évènements au cache
				me.cacheEvents(EvenementGestion.CACHE_MODE_MES_ABONNEMENTS, start, end, parsedEvents);
				
				callback(resultCode, { evenements: parsedEvents, calendriers: data.calendriers, groupes: data.groupes });
			}
			else {
				callback(resultCode);
			}
		});
	};
	
	
	/**
	 * Fonction indiquant si l'utilisateur est propriétaire de l'évènement.
	 * @return Un booléen indiquant si l'utilisateur est propriétaire
	 */
	EvenementGestion.prototype.estProprietaire = function(evenement) {
		
		var userId = this.restManager.getUserId();
		
		for (var j=0, maxJ = evenement.responsables.length; j<maxJ; j++) {
			if (evenement.responsables[j].id === userId) {
				return true;
			}
		}
		
		return false;
	};
	
	var makeStrSalle = function(salles) {
		var strSalles = "";
		for(var j=0, maxj = salles.length; j<maxj; j++) {
			if(j != 0)
				strSalles += ", ";
			strSalles += salles[j].nom;
		}
		
		return strSalles;
	};
	
	/**
	 * Retourne un tableau d'évènements compatibles fullCalendar à partir d'évènements renvoyés par le serveur
	 * (format complet, avec matières et types, classe EvenementComplet)
	 * 
	 * @param evenements Evènements renvoyés par le serveur
	 * @return Tableau d'évènements parsés
	 */
	EvenementGestion.prototype.parseEventsCompletsFullCalendar = function(evenements) {
		
		var res = new Array();
		
		for(var i=0, maxI = evenements.length; i<maxI; i++) {
			
			var estProprietaire = this.estProprietaire(evenements[i]);
			// TODO : remplacer "title" avec plus de texte (matière ? salle ?)
			res.push({
				id: evenements[i].id,
				title: evenements[i].nom,
				nom: evenements[i].nom,
				start: new Date(evenements[i].dateDebut),
				end: new Date(evenements[i].dateFin),
				idCreateur: evenements[i].idCreateur,
				salles: evenements[i].salles,
				strSalle: makeStrSalle(evenements[i].salles),
				calendriers: evenements[i].calendriers,
				intervenants: evenements[i].intervenants,
				responsables: evenements[i].responsables,
				matieres: evenements[i].matieres,
				types: evenements[i].types,
				allDay: false,
				editable: estProprietaire,
				color: "#3a87ad"
			});
		}
		
		return res;
	};
	
	
	/**
	 * Retourne un tableau d'évènements compatibles fullCalendar
	 * à partir d'évènements renvoyés par le serveur (format simple, sans matières et types, classe EvenementIdentifie)
	 * 
	 * getAbonnements doit avoir été appelé avant cette méthode pour avoir les calendriers,
	 * et pouvoir remplir la matière et le type de chaque évènement. */
	EvenementGestion.prototype.parseEventsSimplesFullcalendar = function(evenements) {
		
		if(this.matieresCalendriers == null || this.typesCalendriers == null)
			throw "getAbonnements doit avoir été appelé avant parseEventsSimplesFullCalendar : " +
					"les calendriers sont nécessaires pour remplir les matières & types des évènements.";
		
		var res = new Array();
		for(var i=0, max = evenements.length; i<max; i++) {
			// Récupération des types et matières pour cet évènement
			var types = new Array();
			var matieres = new Array();
			var idCalendriers = evenements[i].calendriers;
			for(var j=0, maxJ = idCalendriers.length; j<maxJ; j++) {
				var type = this.typesCalendriers[idCalendriers[j]];
				if(type) {
					types.push();
				}
				
				var matiere = this.matieresCalendriers[idCalendriers[j]];
				if(matiere) {
					matieres.push(matiere);
				}
			}
		
			// Est-ce que l'utilisateur est propriétaire
			var estProprietaire = this.estProprietaire(evenements[i]);
			// TODO : remplacer "title" avec plus de texte (matière ? salle ?)
			res[i] = {
				id: evenements[i].id,
				title: evenements[i].nom,
				nom: evenements[i].nom,
				start: new Date(evenements[i].dateDebut),
				end: new Date(evenements[i].dateFin),
				idCreateur: evenements[i].idCreateur,
				salles: evenements[i].salles,
				strSalle: makeStrSalle(evenements[i].salles),
				calendriers: evenements[i].calendriers,
				intervenants: evenements[i].intervenants,
				responsables: evenements[i].responsables,
				matieres: matieres,
				types: types,
				allDay: false,
				editable: estProprietaire,
				color: "#3a87ad"
			};
		}
	
		return res;
	};
	
	/**
	 * Récupération générique d'évènements
	 * @param url URL de récupération de ces évènements
	 * @param modeCache Mode de cache des évènements. Doit correspondre à l'URL fourni avec le paramètre url
	 * @param dateDebut Début de la fenêtre de recherche
	 * @param dateFin Fin de la fenêtre de recherche
	 * @param parsingMethod Méthode à utiliser pour convertir les évènements retournés par le serveur au format fullCalendar
	 * @param ignoreCache Ignorer les évènements stockés en cache et forcer la requête vers le serveur
	 * @param callback Fonction de rappel à appeler avec les résultats. Prend les paramètres resultCode et evenements (tableau des évènements)
	 * @param idSalle ID de la salle pour laquelle les évènements sont à récupérer ; à ne préciser que pour lister les évènements d'une salle
	 * @param idGroupe ID du groupe pour lequel les évènements sont à récupérer ; à ne préciser que pour lister les évènements d'un groupe
	 */
	EvenementGestion.prototype.getEvenements = function(url, modeCache, dateDebut, dateFin, parsingMethod, ignoreCache, callback, idSalle, idGroupe) {
		var me = this;
		
		// Récupération depuis le cache si disponible
		var evenements = ignoreCache ? null : this.getEventsFromCache(modeCache, dateDebut, dateFin);
		
		if(evenements !== null) {
			callback(RestManager.resultCode_Success, evenements);
		}
		else {
			// Récupération depuis le serveur
			this.queryEvenements(url, dateDebut, dateFin, function(resultCode, data) {
				if(resultCode == RestManager.resultCode_Success) {
					var parsedEvents = parsingMethod(data);
					
					// Ajout des évènements récupérés au cache
					me.cacheEvents(modeCache, dateDebut, dateFin, parsedEvents);
					
					callback(resultCode, parsedEvents);
				}
				else {
					callback(resultCode);
				}
			}, idSalle, idGroupe);
		}
	};
	
	/**
	 * Récupération des évènements auxquels l'utilisateur est abonné pour l'intervalle donné.
	 * Les évènements sont éventuellements récupérés depuis le cache si disponibles.
	 * Les matières des évènements sont remplis avec les calendriers précédemment chargés
	 * depuis le serveur, donc getAbonnements doit avoir été appelé précédemment.
	 * 
	 * Arguments start/end : intervalle de recherche (dates)
	 * ignoreCache : true pour forcer la récupération depuis le serveur
	 * callback : fonction appelée pour fournir les résultats une fois la requête effectuée
	 * Arguments de callback : 
	 * - resultCode : code de retour de la requête (RestManager.resultCode_Success en cas de succès)
	 * - data : évènements récupérés, au format de parseEventsFullcalendar
	 * 
	 */
	EvenementGestion.prototype.getEvenementsAbonnements = function(start, end, ignoreCache, callback) {
		var me = this;
		this.getEvenements("abonnements/evenements", EvenementGestion.CACHE_MODE_MES_ABONNEMENTS, start, end, 
				function(events) { return me.parseEventsSimplesFullcalendar(events); }, ignoreCache, callback);
	};
	
	EvenementGestion.prototype.getMesEvenements = function(start, end, ignoreCache, callback) {
		var me = this;
		this.getEvenements("listerevenements/intervenant", EvenementGestion.CACHE_MODE_MES_EVENEMENTS, start, end, 
				function(events) { return me.parseEventsCompletsFullCalendar(events); }, ignoreCache, callback);
	};
	
	EvenementGestion.prototype.getEvenementsGroupe = function(start, end, idGroupe, ignoreCache, callback) {
		var me = this;
		this.getEvenements("listerevenements/groupe", EvenementGestion.CACHE_MODE_GROUPE, start, end, 
				function(events) { return me.parseEventsCompletsFullCalendar(events); }, ignoreCache, callback, null, idGroupe);
	};
	
	EvenementGestion.prototype.getEvenementsSalle = function(start, end, idSalle, ignoreCache, callback) {
		var me = this;
		this.getEvenements("listerevenements/salle", EvenementGestion.CACHE_MODE_SALLE, start, end, 
				function(events) { return me.parseEventsCompletsFullCalendar(events); }, ignoreCache, callback, idSalle);
	};
	
	/**
	 * Enregistrement des évènements récupérés pour un intervalle donné
	 * pour éviter de refaire une requête au serveur si ils sont re-demandés */
	EvenementGestion.prototype.cacheEvents = function(modeVue, dateDebut, dateFin, events) {
		
		// Si l'intervalle récupéré contient des intervalles en cache, on les supprime
		for(var i=this.cachedEvents[modeVue].length-1; i>=0; i--) { // Parcours en sens inverse car on supprime des éléments
			var current = this.cachedEvents[modeVue][i];
			
			// Si l'intervalle déjà en cache est contenu dans celui récupéré
			if(dateDebut <= current.dateDebut && dateFin >= current.dateFin) {
				this.cachedEvents[modeVue].splice(i, 1); // Suppression de l'intervalle existant de la liste
			}
		}
	
		this.cachedEvents[modeVue].push({dateDebut: dateDebut, dateFin: dateFin, evenements: events});
	};
	
	/**
	 * Filtrage des évènements pour ne garder que ceux situés dans l'intervalle donné.
	 */
	var filtrerEvenementsIntervalle = function(dateDebut, dateFin, evenements) {
		
		var res = Array();
		for(var i=0, maxI = evenements.length; i<maxI; i++) {
			if(evenements[i].end >= dateDebut && evenements[i].start <= dateFin) {
				res.push(evenements[i]);
			}
		}
		
		return res;
	};
	
	/**
	 * Recherche dans les évènements en cache d'un intervalle d'évènements
	 * Renvoie : un tableau d'évènements si trouvé, sinon null */
	EvenementGestion.prototype.getEventsFromCache = function(modeVue, dateDebut, dateFin) {
		
		for(var i=0, max=this.cachedEvents[modeVue].length; i<max; i++) {
			var current = this.cachedEvents[modeVue][i];
			// L'intervalle demandé est contenu dans l'intervalle en cache
			if(current.dateDebut <= dateDebut && current.dateFin >= dateFin) {
				
				// Si l'intervalle est seulement contenu dans le cache, on ne prend que les évènements dans l'intervalle demandé
				if(current.dateDebut.getTime() != dateDebut.getTime() || current.dateFin.getTime() != dateFin.getTime()) {
					return filtrerEvenementsIntervalle(dateDebut, dateFin, current.evenements);
				}
				else {
					return current.evenements;
				}
			}
		}
		return null;
	};
	
	/**
	 * Invalidation du cache pendant un intervalle donné. Tous les modes de cache sont concernés
	 * (CACHE_MODE_MES_EVENEMENTS, CACHE_MODE_MES_ABONNEMENTS...)
	 * 
	 * @param dateDebut début de la fenêtre à invalider
	 * @param dateFin fin de la fenêtre à invalider
	 */
	EvenementGestion.prototype.invalidateCache = function(dateDebut, dateFin) {
		var me = this;
		
		function invalidateMode(mode) {
			// Parcours dans le sens inverse puisqu'on enlève des éléments
			for(var i=me.cachedEvents[mode].length - 1; i>=0; i--) {
				if(dateDebut < me.cachedEvents[mode][i].dateFin && dateFin > me.cachedEvents[mode][i].dateDebut) {
					// Suppression de l'intervalle de cache
					me.cachedEvents[mode].splice(i, 1);
				}
			}
		}
		
		invalidateMode(EvenementGestion.CACHE_MODE_GROUPE);
		invalidateMode(EvenementGestion.CACHE_MODE_SALLE);
		invalidateMode(EvenementGestion.CACHE_MODE_MES_EVENEMENTS);
		invalidateMode(EvenementGestion.CACHE_MODE_MES_ABONNEMENTS);
	};
	
	/**
	 * Suppression de tout le cache d'un mode donné
	 * @param modeCache Mode de cache concerné (CACHE_MODE_MES_EVENEMENTS, CACHE_MODE_MES_ABONNEMENTS...)
	 */
	EvenementGestion.prototype.clearCache = function(modeCache) {
		this.cachedEvents[modeCache] = new Array();
	};
	
	/**
	 * Ajout d'un évènement en base de données
	 * 
	 * @param {string} nom Nom de l'évènement
	 * @param {Date} dateDebut date de début de l'évènement
	 * @param {Date} dateFin date de fin de l'évènement
	 * @param {number[]} idCalendriers tableau d'IDs des calendriers
	 * @param {number[]} idSalles tableau d'IDs des salles de l'évènement
	 * @param {number[]} idIntervenants tableau d'IDs des intervenants
	 * @param {number[]} idResponsables tableau d'IDs des responsables
	 * @param {number[]} idEvenementsSallesALiberer tableau d'IDs des évènements dont les salles sont à libérer pour cet évènement
	 * @param {function} callback Fonction de rappel appelée une fois la requête effectuée. Prend un argument resultCode (resultCode du RestManager)
	 */
	EvenementGestion.prototype.ajouterEvenement = function(nom, dateDebut, dateFin, idCalendriers, idSalles, idIntervenants, 
			idResponsables, idEvenementsSallesALiberer, callback) {
		var me = this;
		this.restManager.effectuerRequete("POST", "evenement/ajouter", {
			token: me.restManager.getToken(),
			evenement: JSON.stringify({
				nom: nom,
				dateDebut: dateDebut.getTime(),
				dateFin: dateFin.getTime(),
				calendriers: idCalendriers,
				salles: idSalles,
				intervenants: idIntervenants,
				responsables: idResponsables,
				evenementsSallesALiberer: idEvenementsSallesALiberer
			})
		}, function(response) {
			
			if(response.resultCode === RestManager.resultCode_Success) {
				// Invalidation du cache pendant l'intervalle de l'évènement ajouté
				me.invalidateCache(dateDebut, dateFin);
			}
			
			callback(response.resultCode);
		});
	};
	
	/**
	 * Modification d'un évènement en base de données.
	 * Les paramètres peuvent être null ou non précisés pour indiquer "aucune modification" (sauf l'ID d'évènement).
	 * <b>Ne pas oublier d'invalider le cache d'évènements</b> via EvenementGestion.invalidateCache() une fois
	 * l'évènement modifié, pendant l'ancienne période de l'évènement (la nouvelle est automatiquement invalidée) 
	 * 
	 * @param {number] id ID de l'évènement à modifier
	 * @param {function} callback Fonction de rappel appelée une fois la requête effectuée. Prend un argument resultCode (resultCode du RestManager)
	 * @param {Date} dateDebut Nouvelle date de début de l'évènement
	 * @param {Date} dateFin Nouvelle date de fin de l'évènement
	 * @param {string} nom Nouveau nom de l'évènement
	 * @param {number[]} idCalendriers Nouveau tableau d'IDs des calendriers
	 * @param {number[]} idSalles Nouveau tableau d'IDs des salles de l'évènement
	 * @param {number[]} idIntervenants Nouveau tableau d'IDs des intervenants
	 * @param {number[]} idResponsables Nouveau tableau d'IDs des responsables
	 * @param {number[]} idEvenementsSallesALiberer tableau d'IDs des évènements dont les salles sont à libérer pour cet évènement
	 */
	EvenementGestion.prototype.modifierEvenement = function(id, callback, dateDebut, dateFin, nom, idCalendriers, 
			idSalles, idIntervenants, idResponsables, idEvenementsSallesALiberer) {

		var me = this;
		this.restManager.effectuerRequete("POST", "evenement/modifier", {
			token: me.restManager.getToken(),
			evenement: JSON.stringify({
				id: id,
				nom: nom,
				dateDebut: dateDebut.getTime(),
				dateFin: dateFin.getTime(),
				calendriers: idCalendriers,
				salles: idSalles,
				intervenants: idIntervenants,
				responsables: idResponsables,
				evenementsSallesALiberer: idEvenementsSallesALiberer
			})
		}, function(response) {
			
			if(response.resultCode === RestManager.resultCode_Success) {
				// Invalidation du cache pendant le nouvel intervalle de l'évènement modifié
				// L'ancien intervalle n'est pas invalidé et doit être fait par l'appelant
				me.invalidateCache(dateDebut, dateFin);
			}
			
			callback(response.resultCode);
		});
	};
	
	/**
	 * Suppression d'un évènement en base de données. Effectue la purge du cache.
	 * 
	 * @param {Object} event Evénement à supprimer. Doit contenir les propriétés id, start, end
	 * @param {function} callback Fonction appelée une fois la suppression effectuée. Prend un argument resultCode.
	 */
	EvenementGestion.prototype.supprimerEvenement = function(event, callback) {
		var me = this;
		this.restManager.effectuerRequete("POST", "evenement/supprimer", {
			token: this.restManager.getToken(),
			idEvenement: event.id
		}, function(response) {
			
			if(response.resultCode === RestManager.resultCode_Success) {
				me.invalidateCache(event.start, event.end);
			}
			
			callback(response.resultCode);
		});
	};
	
	
	return EvenementGestion;
});
