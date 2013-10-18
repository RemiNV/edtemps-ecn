define(["Calendrier", "EvenementGestion", "RestManager", "jquery"], function(Calendrier, EvenementGestion, RestManager) {
	
	/**
	 * Cet écran est associé au HTML templates/page_accueil.html.
	 * Il s'agit de la page principale d'affichage des évènements. */
	var EcranAccueil = function(restManager) { // Constructeur
		this.restManager = restManager;
		this.abonnementsRecuperes = false;
		this.evenementGestion = new EvenementGestion(this.restManager);
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
			if(!this.abonnementsRecuperes) { // Récupération des abonnements (premier affichage de la page)
				this.remplirMesAbonnements(start, end, callback);
			}
			else { // Récupération uniquement des évènements (pas besoin des calendriers & groupes). Utilisation du cache.
				this.evenementGestion.getEvenementsAbonnements(start, end, false, function(resultCode, data) {
					if(resultCode == RestManager.resultCode_Success) {
						callback(data);
					}
					else if(resultCode == RestManager.resultCode_NetworkError) {
						$("#zone_info").html("Erreur de chargement de vos évènements ; vérifiez votre connexion.");
					}
					else {
						$("#zone_info").html("Erreur de chargement de vos évènements. Votre session a peut-être expiré ?");
					}
				});
			}
			break;
			
		default: 
			// TODO : gérer
		
		}
	
	};

	EcranAccueil.prototype.remplirMesAbonnements = function(dateDebut, dateFin, callbackCalendrier) {
		// Récupération des abonnements pendant la période affichée
		var me = this;
		
		this.evenementGestion.getAbonnements(dateDebut, dateFin, function(resultCode, data) {
			if(resultCode == RestManager.resultCode_Success) {
				callbackCalendrier(data.evenements);
				
				// On pourra ne récupérer que les évènements à l'avenir
				me.abonnementsRecuperes = true;
				
				// TODO : remplir les autres vues
			}
			else if(resultCode == RestManager.resultCode_NetworkError) {
				$("#zone_info").html("Erreur de chargement de vos agendas ; vérifiez votre connexion.");
			}

			else {
				$("#zone_info").html("Erreur de chargement de vos agendas. Votre session a peut-être expiré ?");
			}
		});
	};
	
	return EcranAccueil;
});