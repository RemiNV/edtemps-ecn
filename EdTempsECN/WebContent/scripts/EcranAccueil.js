define(["Calendrier", "EvenementGestion", "ListeGroupesParticipants", "RechercheSalle", 
        "AjoutEvenement", "RestManager", "jquery", "jqueryui"], function(Calendrier, EvenementGestion, ListeGroupesParticipants, 
        		RechercheSalle, AjoutEvenement, RestManager) {
	
	/**
	 * Cet écran est associé au HTML templates/page_accueil.html.
	 * Il s'agit de la page principale d'affichage des évènements. */
	function EcranAccueil(restManager) { // Constructeur
		this.restManager = restManager;
		this.abonnementsRecuperes = false;
		this.evenementGestion = new EvenementGestion(this.restManager);
		this.rechercheSalle = new RechercheSalle(this.restManager, $("#recherche_salle_libre"));
		this.ajoutEvenement = new AjoutEvenement(restManager, $("#dialog_ajout_evenement"), this.rechercheSalle);
		// TODO : enlever ce test
		window.ajoutEvenement = this.ajoutEvenement;
	};
	
	EcranAccueil.MODE_GROUPE = 1;
	EcranAccueil.MODE_SALLE = 2;
	EcranAccueil.MODE_MES_EVENEMENTS = 3;
	EcranAccueil.MODE_MES_ABONNEMENTS = 4;
	
	EcranAccueil.prototype.init = function() {
		var me = this;
		
		// Initialisation des listeners
		$("#btn_gerer_agendas").click(function(e) {
			Davis.location.assign("parametres");
		});
		
		$("#btn_chercher_salle").click(function(e) {
			me.rechercheSalle.show();
		});
		
		$("#btn_ajout_evenement").click(function(e) {
			me.ajoutEvenement.show();
		});

		this.setVue("mes_abonnements");
		this.calendrier = new Calendrier(function(start, end, callback) { me.onCalendarFetchEvents(start, end, callback); }, this.ajoutEvenement);
		
		this.listeGroupesParticipants = new ListeGroupesParticipants(this.restManager, this.calendrier);
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
		
		if(this.abonnementsRecuperes) {
			this.calendrier.refetchEvents();
		}
	};
	
	/**
	 * Fonction appelée par fullCalendar lorsque le mécanisme de "fetch" est déclenché
	 * @param start Début de la période pendant laquelle les évènements doivent être récupérés
	 * @param end Fin de la période pendant laquelle les évènements doivent être récupérés
	 * @param callback Callback de fullcalendar auquel il faut fournir les évènements (au format compatible fullcalendar)
	 */
	EcranAccueil.prototype.onCalendarFetchEvents = function(start, end, callback) {

		switch(this.mode) {
		case EcranAccueil.MODE_MES_ABONNEMENTS:
			if(!this.abonnementsRecuperes) { // Récupération des abonnements (premier affichage de la page)
				this.remplirMesAbonnements(start, end, callback);
			}
			else { // Récupération uniquement des évènements (pas besoin des calendriers & groupes). Utilisation du cache.
				this.remplirEvenementsAbonnements(start, end, callback);
			}
			break;
			
		case EcranAccueil.MODE_MES_EVENEMENTS:
			
			// TODO : compléter
			
			
			break;
			
		default: 
			// TODO : gérer
			callback(new Array());
		
		}
	};
	
	
	
	
	/**
	 * Fonction fournissant au callback de fullcalendar les évènements de la période demandée
	 * 
	 * @param dateDebut date de début de la période
	 * @param dateFin date de fin de la période
	 * @param callbackCalendrier callback de fullcalendar
	 */
	EcranAccueil.prototype.remplirEvenementsAbonnements = function(dateDebut, dateFin, callbackCalendrier) {
		var me = this;
		this.evenementGestion.getEvenementsAbonnements(dateDebut, dateFin, false, function(resultCode, data) {
			if(resultCode == RestManager.resultCode_Success) {
				
				// Filtrage et passage à fullcalendar
				var evenementsGroupesActifs = me.listeGroupesParticipants.filtrerEvenementsGroupesActifs(data);
				callback(me.calendrier.filtrerMatiereTypeRespo(evenementsGroupesActifs));
			}
			else if(resultCode == RestManager.resultCode_NetworkError) {
				window.showToast("Erreur de chargement de vos évènements ; vérifiez votre connexion.");
			}
			else {
				window.showToast("Erreur de chargement de vos évènements. Votre session a peut-être expiré ?");
			}
		});
	};

	/**
	 * Fonction fournissant au callback de fullcalendar les évènements de la période demandée, et remplissant
	 * les informations d'abonnement de l'utilisateur (calendriers, groupes). Est typiquement appelée une unique fois,
	 * remplirEvenementsAbonnements peut être utilisée pour récupérer les évènements des autres périoders.
	 * 
	 * @param dateDebut Date de début de la période
	 * @param dateFin Date de fin de la période
	 * @param callbackCalendrier Callback de fullcalendar
	 */
	EcranAccueil.prototype.remplirMesAbonnements = function(dateDebut, dateFin, callbackCalendrier) {
		// Récupération des abonnements pendant la période affichée
		var me = this;
		
		this.evenementGestion.getAbonnements(dateDebut, dateFin, function(resultCode, data) {
			if(resultCode == RestManager.resultCode_Success) {
				
				// Créer liste vos agendas pour affichage dans le bloc "Vos agendas"
				me.listeGroupesParticipants.initBlocVosAgendas(data.groupes);

				// Afficher le bloc "Vos agendas"
				me.listeGroupesParticipants.afficherBlocVosAgendas();

				// Afficher les événements après filtres
				var evenementsGroupesActifs = me.listeGroupesParticipants.filtrerEvenementsGroupesActifs(data.evenements);
				callbackCalendrier(me.calendrier.filtrerMatiereTypeRespo(evenementsGroupesActifs));

				// On pourra ne récupérer que les évènements à l'avenir
				me.abonnementsRecuperes = true;
			}
			else if(resultCode == RestManager.resultCode_NetworkError) {
				window.showToast("Erreur de chargement de vos agendas ; vérifiez votre connexion.");
			}

			else {
				window.showToast("Erreur de chargement de vos agendas. Votre session a peut-être expiré ?");
			}
		});
	};
	
	return EcranAccueil;
});