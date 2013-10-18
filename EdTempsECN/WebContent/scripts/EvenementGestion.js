define(["RestManager"], function(RestManager) {

	var EvenementGestion = function(restManager) {
		this.restManager = restManager
		
		this.cachedEvents = Object();
		this.cachedEvents[EvenementGestion.CACHE_MODE_MES_ABONNEMENTS] = Array();
		this.cachedEvents[EvenementGestion.CACHE_MODE_GROUPE] = Array();
		this.cachedEvents[EvenementGestion.CACHE_MODE_SALLE] = Array();
		this.cachedEvents[EvenementGestion.CACHE_MODE_MES_EVENEMENTS] = Array();
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
		var me = this;
	
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
	 * Fonction identique à queryAbonnements mais ne fournit que les évènements sous forme d'un tableau dans callback.
	 * Plus efficace une fois les calendriers et groupes déjà chargés */
	EvenementGestion.prototype.queryEvenementsAbonnements = function(dateDebut, dateFin, callback) {
		var me = this;
	
		this.restManager.effectuerRequete("GET", "abonnements/evenements", {
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
				var parsedEvents = me.parseEventsFullcalendar(data.evenements);
				
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
	 * Retourne un tableau d'évènements compatibles fullCalendar
	 * a partir d'un objet d'abonnements */
	EvenementGestion.prototype.parseEventsFullcalendar = function(evenements) {
		
		var res = Array();
		for(var i=0, max = evenements.length; i<max; i++) {
			// Chaîne de salles
			var strSalles = "";
			for(var j=0, maxj = evenements[i].salles.length; j<maxj; j++) {
				if(j != 0)
					strSalles += ", ";
				strSalles += evenements[i].salles[j].nom;
			}
		
			res[i] = {
				id: evenements[i].id,
				title: evenements[i].nom,
				start: new Date(evenements[i].dateDebut),
				end: new Date(evenements[i].dateFin),
				salles: evenements[i].salles,
				strSalle: strSalles,
				calendriers: evenements[i].calendriers,
				intervenants: evenements[i].intervenants,
				allDay: false
			};
		}
	
		return res;
	};
	
	/**
	 * Récupération des évènements auxquels l'utilisateur est abonné pour l'intervalle donné.
	 * Les évènements sont éventuellements récupérés depuis le cache si disponibles.
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
		
		// Récupération depuis le cache si disponible
		var evenements = ignoreCache ? null : this.getEventsFromCache(EvenementGestion.CACHE_MODE_MES_ABONNEMENTS, start, end);
		
		if(evenements !== null) {
			callback(true, RestManager.resultCode_Success, evenements);
		}
		else {
			// Récupération depuis le serveur
			
			this.queryEvenementsAbonnements(start, end, function(resultCode, data) {
				if(resultCode == RestManager.resultCode_Success) {
					var parsedEvents = me.parseEventsFullcalendar(data);
					
					// Ajout des évènements récupérés au cache
					me.cacheEvents(EvenementGestion.CACHE_MODE_MES_ABONNEMENTS, start, end, parsedEvents);
					
					callback(resultCode, parsedEvents);
				}
				else {
					callback(resultCode);
				}
			});
		}
	};
	
	/**
	 * Fusionne les évènements de deux tableaux d'évènements au format fullCalendar.
	 * En cas de doublon (même ID) l'évènement de nouveauxEvenements est utilisé.
	 * Retour une tableau d'évènements au format fullCalendar. */
	EvenementGestion.prototype.fusionnerIntervallesCacheEvenements = function(currentEvenements, nouveauxEvenements) {
		var eventsById = Object();
		
		for(var i=0, max = currentEvenements.length; i<max; i++) {
			eventsById[currentEvenements[i].id] = currentEvenements[i];
		}
		
		// Ajout des évènements de nouveauxEvenements ensuite 
		// les évènements avec le même ID sont remplacés (mis à jour)
		for(var i=0, max=nouveauxEvenements.length; i<max; i++) {
			eventsById[nouveauxEvenements[i].id] = nouveauxEvenements[i];
		}
		
		var res = Array();
		for(var eventId in eventsById) {
			res.push(eventsById[eventId]);
		}
		
		return res;
	}
	
	/**
	 * Enregistrement des évènements récupérés pour un intervalle donné
	 * pour éviter de refaire une requête au serveur si ils sont re-demandés */
	EvenementGestion.prototype.cacheEvents = function(modeVue, dateDebut, dateFin, events) {
		
		// Si l'intervalle récupéré contient des intervalles en cache, on les fusionne
		for(var i=this.cachedEvents[modeVue].length-1; i>=0; i--) { // Parcours en sens inverse car on supprime des éléments
			var current = this.cachedEvents[modeVue][i];
			
			// Si les intervalles se recoupent
			if(current.dateDebut <= dateFin && current.dateFin >= dateDebut) {
				this.cachedEvents[modeVue].splice(i, 1); // Suppression de l'intervalle existant de la liste
				
				// Si l'intervalle existant n'est pas complètement contenu dans le nouveau : fusion
				if(!(current.dateDebut >= dateDebut && current.dateFin <= dateFin)) {
					events = this.fusionnerIntervallesCacheEvenements(current.evenements, events);
					
					// Modification des dates pour coller avec la fusion des intervalles (min, max)
					dateDebut = dateDebut < current.dateDebut ? dateDebut : current.dateDebut;
					dateFin = dateFin > current.dateFin ? dateFin : current.dateFin;
				}
			}
		}
	
		this.cachedEvents[modeVue].push({dateDebut: dateDebut, dateFin: dateFin, evenements: events});
		console.log("cache : ", this.cachedEvents);
	};
	
	/**
	 * Recherche dans les évènements en cache d'un intervalle d'évènements
	 * Renvoie : un tableau d'évènements si trouvé, sinon null */
	EvenementGestion.prototype.getEventsFromCache = function(modeVue, dateDebut, dateFin) {
		
		for(var i=0, max=this.cachedEvents[modeVue].length; i<max; i++) {
			var current = this.cachedEvents[modeVue][i];
			// L'intervalle demandé est contenu dans l'intervalle en cache
			if(current.dateDebut <= dateDebut && current.dateFin >= dateFin) {
				return current.evenements;
			}
		}
		return null;
	};
	
	return EvenementGestion;
});