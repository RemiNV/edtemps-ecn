define(["Calendrier", "EvenementGestion", "RestManager", "jquery"], function(Calendrier, EvenementGestion, RestManager) {
	
	/**
	 * Cet écran est associé au HTML templates/page_accueil.html.
	 * Il s'agit de la page principale d'affichage des évènements. */
	var EcranAccueil = function(restManager) { // Constructeur
		this.restManager = restManager;
		this.abonnementsRecuperes = false;
		this.cachedEvents = Object();
		this.cachedEvents[EcranAccueil.MODE_MES_ABONNEMENTS] = Array();
		this.cachedEvents[EcranAccueil.MODE_GROUPE] = Array();
		this.cachedEvents[EcranAccueil.MODE_SALLE] = Array();
		this.cachedEvents[EcranAccueil.MODE_MES_EVENEMENTS] = Array();
	};
	
	EcranAccueil.MODE_GROUPE = 1;
	EcranAccueil.MODE_SALLE = 2;
	EcranAccueil.MODE_MES_EVENEMENTS = 3;
	EcranAccueil.MODE_MES_ABONNEMENTS = 4;
	
	EcranAccueil.prototype.init = function() {
	
		// Initialisation des listeners
		$("#btn_gerer_agendas").click(function(e) {
			Davis.location.assign("parametres");
		});
		
		var me = this;
		this.calendrier = new Calendrier(function(start, end, callback) { me.onCalendarFetchEvents(start, end, callback); });
		this.setVue("mes_abonnements");
	};
	
	EcranAccueil.prototype.setVue = function(vue) {
	
		// Sélection de l'onglet
		$("#nav_vue_agenda li").removeClass("selected");
		switch(vue) {
		case "vue_groupe":
			$("#nav_vue_agenda #tab_vue_groupe").addClass("selected");
			this.mode = EcranAccueil.MODE_GROUPE;
			break;
		case "vue_salle":
			$("#nav_vue_agenda #tab_vue_salle").addClass("selected");
			this.mode = EcranAccueil.MODE_SALLE;
			break;
		
		case "mes_evenements":
			$("#nav_vue_agenda #tab_mes_evenements").addClass("selected");
			this.mode = EcranAccueil.MODE_MES_EVENEMENTS;
			break;
			
		case "mes_abonnements":
		default:
			$("#nav_vue_agenda #tab_mes_abonnements").addClass("selected");
			this.mode = EcranAccueil.MODE_MES_ABONNEMENTS;
			break;
		}
	};
	
	EcranAccueil.prototype.onCalendarFetchEvents = function(start, end, callback) {
	
		switch(this.mode) {
		case EcranAccueil.MODE_MES_ABONNEMENTS:
			if(!this.abonnementsRecuperes) {
				this.remplirMesAbonnements(start, end, callback);
			}
			else {
				this.getEvenementsAbonnementsIntervalle(start, end, callback);
			}
			break;
			
		default: 
			// TODO : gérer
		
		}
	
	};
	
	EcranAccueil.prototype.getEvenementsAbonnementsIntervalle = function(start, end, callback) {
		var me = this;
		
		// Récupération depuis le cache si disponible
		var evenements = this.getEventsFromCache(EcranAccueil.MODE_MES_ABONNEMENTS, start, end);
		
		if(evenements !== null) {
			callback(evenements);
		}
		else {
			// Récupération depuis le serveur
			
			// TODO : requête ne récupérant que les évènements
			evenementGestion.listerEvenementsAbonnement(start, end, function(networkSuccess, resultCode, data) {
				if(networkSuccess) {
					if(resultCode == RestManager.resultCode_Success) {
						// On fournit les évènements au calendrier
						var parsedEvents = me.calendrier.parseEvents(data);
						callback(parsedEvents);
						
						// Ajout des évènements récupérés au cache
						me.cacheEvents(EcranAccueil.MODE_MES_ABONNEMENTS, start, end, parsedEvents);
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
	EcranAccueil.prototype.cacheEvents = function(modeVue, dateDebut, dateFin, events) {
		
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
	EcranAccueil.prototype.getEventsFromCache = function(modeVue, dateDebut, dateFin) {
		
		for(var i=0, max=this.cachedEvents[modeVue].length; i<max; i++) {
			var current = this.cachedEvents[modeVue][i];
			// L'intervalle demandé est contenu dans l'intervalle en cache
			if(current.dateDebut <= dateDebut && current.dateFin >= dateFin) {
				return current.evenements;
			}
		}
		return null;
	};

	EcranAccueil.prototype.remplirMesAbonnements = function(dateDebut, dateFin, callbackCalendrier) {
		// Récupération des abonnements pendant la période affichée
		evenementGestion = new EvenementGestion(this.restManager);
		var me = this;
		
		evenementGestion.listerEvenementsAbonnement(dateDebut, dateFin, function(networkSuccess, resultCode, data) {
			if(networkSuccess) {
				if(resultCode == RestManager.resultCode_Success) {
					// On fournit les évènements au calendrier
					var parsedEvents = me.calendrier.parseEvents(data);
					callbackCalendrier(parsedEvents);
					
					// Ajout de ce premier set d'évènements à la liste des évènements récupérés
					me.cacheEvents(EcranAccueil.MODE_MES_ABONNEMENTS, dateDebut, dateFin, parsedEvents);
					
					// Par la suite, ne récupérer que les évènements sera possible
					me.abonnementsRecuperes = true;
					
					// TODO : remplissage du reste des zones
				}
				else {
					$("#zone_info").html("Erreur de chargement de vos agendas. Votre session a peut-être expiré ?");
				}
			}
			else {
				$("#zone_info").html("Erreur de chargement de vos agendas ; vérifiez votre connexion.");
			}
		});
	};
	
	return EcranAccueil;
});