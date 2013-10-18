define(["RestManager", "lib/fullcalendar.translated.min"], function(RestManager) {

	var Calendrier = function(eventsSource, restManager, evenementGestion) {
	
		this.cachedEvents = Object();
		this.cachedEvents[Calendrier.CACHE_MODE_MES_ABONNEMENTS] = Array();
		this.cachedEvents[Calendrier.CACHE_MODE_GROUPE] = Array();
		this.cachedEvents[Calendrier.CACHE_MODE_SALLE] = Array();
		this.cachedEvents[Calendrier.CACHE_MODE_MES_EVENEMENTS] = Array();
	
		this.restManager = restManager;
		this.evenementGestion = evenementGestion;
	
		var date = new Date();
		var d = date.getDate();
		var m = date.getMonth();
		var y = date.getFullYear();
		
		var me = this;

		this.jqCalendar = $("#calendar");
		// Initialisation du calendrier
		this.jqCalendar.fullCalendar({
			weekNumbers: true,
			weekNumberTitle: "Sem.",
			firstDay: 0,
			editable: true,
			defaultView: "agendaWeek",
			timeFormat: "HH'h'(:mm)",
			axisFormat: "HH'h'(:mm)",
			titleFormat: {
				month: 'MMMM yyyy',                             // Septembre 2013
				week: "d [ MMM] [ yyyy] '&#8212;' {d MMM yyyy}", // 7 - 13 Sep 2013
				day: 'dddd d MMM yyyy'                  // Mardi 8 Sep 2013
			},
			columnFormat: {
				month: 'ddd',    // Lun
				week: 'ddd d/M', // Lun 7/9
				day: 'dddd M/d'  // Lundi 7/9
			},
			header: {
				right: '',
				center: 'title',
				left: 'prev,next today month,agendaWeek,agendaDay'
			},
			height: Math.max(window.innerHeight - 150, 500),
			windowResize: function(view) {
				me.jqCalendar.fullCalendar("option", "height", Math.max(window.innerHeight - 150, 500));
			},
			events: eventsSource
		});
	};
	
	Calendrier.CACHE_MODE_GROUPE = 1;
	Calendrier.CACHE_MODE_SALLE = 2;
	Calendrier.CACHE_MODE_MES_EVENEMENTS = 3;
	Calendrier.CACHE_MODE_MES_ABONNEMENTS = 4;
	
	
	/**
	 * Retourne un tableau d'évènements compatibles fullCalendar
	 * a partir d'un objet d'abonnements */
	Calendrier.prototype.parseEvents = function(abonnements) {
		
		var evenements = abonnements.evenements;
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
				salle: strSalles,
				allDay: false
			};
		}
	
		return res;
	};
	
	Calendrier.prototype.getEvenementsAbonnementsIntervalle = function(start, end, callback) {
		var me = this;
		
		// Récupération depuis le cache si disponible
		var evenements = this.getEventsFromCache(Calendrier.CACHE_MODE_MES_ABONNEMENTS, start, end);
		
		if(evenements !== null) {
			callback(evenements);
		}
		else {
			// Récupération depuis le serveur
			
			// TODO : requête ne récupérant que les évènements
			this.evenementGestion.listerEvenementsAbonnement(start, end, function(networkSuccess, resultCode, data) {
				if(networkSuccess) {
					if(resultCode == RestManager.resultCode_Success) {
						// On fournit les évènements au calendrier
						var parsedEvents = me.parseEvents(data);
						callback(parsedEvents);
						
						// Ajout des évènements récupérés au cache
						me.cacheEvents(Calendrier.CACHE_MODE_MES_ABONNEMENTS, start, end, parsedEvents);
					}
					else {
						$("#zone_info").html("Erreur de chargement des évènements. Votre session a peut-être expiré ?");
					}
				}
				else {
					$("#zone_info").html("Erreur de chargement des évènements ; vérifiez votre connexion.");
				}
			});
			
		}
	};
	
	var fusionnerIntervallesCacheEvenements = function(currentEvenements, nouveauxEvenements) {
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
	Calendrier.prototype.cacheEvents = function(modeVue, dateDebut, dateFin, events) {
		
		// Si l'intervalle récupéré contient des intervalles en cache, on les fusionne
		for(var i=this.cachedEvents[modeVue].length-1; i>=0; i--) { // Parcours en sens inverse car on supprime des éléments
			var current = this.cachedEvents[modeVue][i];
			
			// Si les intervalles se recoupent
			if(current.dateDebut <= dateFin && current.dateFin >= dateDebut) {
				this.cachedEvents[modeVue].splice(i, 1); // Suppression de l'intervalle existant de la liste
				
				// Si l'intervalle existant n'est pas complètement contenu dans le nouveau : fusion
				if(!(current.dateDebut >= dateDebut && current.dateFin <= dateFin)) {
					events = fusionnerIntervallesCacheEvenements(current.evenements, events);
					
					// Modification des dates pour coller avec la fusion des intervalles (min, max)
					dateDebut = dateDebut < current.dateDebut ? dateDebut : current.dateDebut;
					dateFin = dateFin > current.dateFin ? dateFin : current.dateFin;
				}
			}
		}
	
		this.cachedEvents[modeVue].push({dateDebut: dateDebut, dateFin: dateFin, evenements: events});
	};
	
	/**
	 * Recherche dans les évènements en cache d'un intervalle d'évènements
	 * Renvoie : un tableau d'évènements si trouvé, sinon null */
	Calendrier.prototype.getEventsFromCache = function(modeVue, dateDebut, dateFin) {
		
		for(var i=0, max=this.cachedEvents[modeVue].length; i<max; i++) {
			var current = this.cachedEvents[modeVue][i];
			// L'intervalle demandé est contenu dans l'intervalle en cache
			if(current.dateDebut <= dateDebut && current.dateFin >= dateFin) {
				return current.evenements;
			}
		}
		return null;
	};


	return Calendrier;
});