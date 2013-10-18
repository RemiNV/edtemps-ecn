define(["Calendrier", "EvenementGestion", "RestManager", "jquery"], function(Calendrier, EvenementGestion, RestManager) {
	
	/**
	 * Cet écran est associé au HTML templates/page_accueil.html.
	 * Il s'agit de la page principale d'affichage des évènements. */
	var EcranAccueil = function(restManager) { // Constructeur
		this.restManager = restManager;
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
		this.calendrier = new Calendrier(function(view, container) { me.onCalendarViewRender(view, container); });
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
	
	EcranAccueil.prototype.onCalendarViewRender = function(view, container) {
	
		console.log("render");
	
		switch(this.mode) {
		case EcranAccueil.MODE_MES_ABONNEMENTS:
			this.remplirMesAbonnements(view);
			break;
			
		default: 
			// TODO : gérer
		
		}
	
	};

	EcranAccueil.prototype.remplirMesAbonnements = function(view) {
		// Récupération des abonnements pendant la période affichée
		evenementGestion = new EvenementGestion(this.restManager);
		var me = this;
		
		evenementGestion.listerEvenementsAbonnement(view.visStart, view.visEnd, function(networkSuccess, resultCode, data) {
			if(networkSuccess) {
				if(resultCode == RestManager.resultCode_Success) {
					// Remplissage de la zone calendrier
					me.calendrier.remplirCalendrier(data);
					
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