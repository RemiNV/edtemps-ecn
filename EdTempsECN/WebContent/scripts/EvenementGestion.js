define(["RestManager"], function(RestManager) {

	function EvenementGestion(restManager) {
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
	 * Fonction identique à queryAbonnements mais ne fournit que les évènements sous forme d'un tableau dans callback.
	 * Plus efficace une fois les calendriers et groupes déjà chargés */
	EvenementGestion.prototype.queryEvenementsAbonnements = function(dateDebut, dateFin, callback) {
	
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
				
				// Parsing des calendriers pour les matières & types d'évènement
				me.matieresCalendriers = Object();
				me.typesCalendriers = Object();
				for(var i=0, maxI = data.calendriers.length; i<maxI; i++) {
					me.matieresCalendriers[data.calendriers[i].id] = data.calendriers[i].matiere;
					me.typesCalendriers[data.calendriers[i].id] = data.calendriers[i].type;
				}
				
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
	 * à partir d'un objet d'abonnements.
	 * getAbonnements doit avoir été appelé avant cette méthode pour avoir les calendriers,
	 * et pouvoir remplir la matière et le type de chaque évènement. */
	EvenementGestion.prototype.parseEventsFullcalendar = function(evenements) {
		
		if(this.matieresCalendriers == null || this.typesCalendriers == null)
			throw "getAbonnements doit avoir été appelé avant parseEventsFullCalendar : " +
					"les calendriers sont nécessaires pour remplir les matières & types des évènements.";
		
		var res = Array();
		for(var i=0, max = evenements.length; i<max; i++) {
			// Chaîne de salles
			var strSalles = "";
			for(var j=0, maxj = evenements[i].salles.length; j<maxj; j++) {
				if(j != 0)
					strSalles += ", ";
				strSalles += evenements[i].salles[j].nom;
			}
			
			// Récupération des types et matières pour cet évènement
			var types = new Array();
			var matieres = new Array();
			var idCalendriers = evenements[i].calendriers;
			for(var j=0, maxJ = idCalendriers.length; j<maxJ; j++) {
				types.push(this.typesCalendriers[idCalendriers[j]]);
				matieres.push(this.matieresCalendriers[idCalendriers[j]]);
			}
		
			// Est-ce que l'utilisateur est propriétaire
			var proprietaire = false;
			if(window.localStorage && window.localStorage["userId"]) {
				for (var j=0, maxJ = evenements[i].responsables.length; j<maxJ; j++) {
					if (evenements[i].responsables[j].id==window.localStorage["userId"]) {
						proprietaire = true;
						break;
					}
				}
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
				responsables: evenements[i].responsables,
				matieres: matieres,
				types: types,
				allDay: false,
				editable: proprietaire ? true : false
			};
		}
	
		return res;
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
		
		if(this.matieresCalendriers == null || this.typesCalendriers == null)
			throw "getAbonnements doit avoir été appelé avant getEvenementsAbonnements : " +
					"les calendriers sont nécessaires pour remplir les matières & types des évènements.";
		
		// Récupération depuis le cache si disponible
		var evenements = ignoreCache ? null : this.getEventsFromCache(EvenementGestion.CACHE_MODE_MES_ABONNEMENTS, start, end);
		
		if(evenements !== null) {
			callback(RestManager.resultCode_Success, evenements);
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
	
	return EvenementGestion;
});
