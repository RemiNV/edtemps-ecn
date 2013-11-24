/**
 * Module d'affichage de l'écran d'accueil (page principale d'affichage des évènements).
 * Associé au HTML templates/page_accueil.html
 * @module EcranAccueil
 */
define(["Calendrier", "EvenementGestion", "ListeGroupesParticipants", "RechercheSalle", "GroupeGestion", 
        "DialogAjoutEvenement", "RestManager", "jquery", "jqueryui"], function(Calendrier, EvenementGestion, ListeGroupesParticipants, 
        		RechercheSalle, GroupeGestion, DialogAjoutEvenement, RestManager) {
	
	/**
	 * @constructor
	 * @alias module:EcranAccueil 
	 */
	var EcranAccueil = function(restManager) { // Constructeur
		var me = this;
		this.restManager = restManager;
		this.abonnementsRecuperes = false;
		this.evenementGestion = new EvenementGestion(this.restManager);
		this.groupeGestion = new GroupeGestion(this.restManager);
		this.rechercheSalle = new RechercheSalle(this.restManager, $("#recherche_salle_libre"));
		
		this.dialogAjoutEvenement = new DialogAjoutEvenement(restManager, $("#dialog_ajout_evenement"), this.rechercheSalle, this.evenementGestion, function() { me.rafraichirCalendrier(); });
		
		this.calendrier = null;
		this.listeGroupesParticipants = null;
	};
	
	EcranAccueil.MODE_GROUPE = 1;
	EcranAccueil.MODE_SALLE = 2;
	EcranAccueil.MODE_MES_EVENEMENTS = 3;
	EcranAccueil.MODE_MES_ABONNEMENTS = 4;
	
	EcranAccueil.prototype.init = function() {
		var me = this;
		
		var jqDialogDetailsEvenement = $("#dialog_details_evenement").dialog({
			autoOpen: false,
			draggable: false,
			width: 500
		});
		
		jqDialogDetailsEvenement.dialog("widget").find(".ui-dialog-titlebar").addClass("dialog_details_evenement_header");
		
		// Initialisation des listeners
		$("#btn_chercher_salle").click(function(e) {
			me.rechercheSalle.show(me.dialogAjoutEvenement);
		});
		
		$("#btn_ajout_evenement").click(function(e) {
			me.dialogAjoutEvenement.show();
		});

		if(!this.mode) {
			this.setVue("mes_abonnements");
		}
		
		this.calendrier = new Calendrier(function(start, end, callback) { me.onCalendarFetchEvents(start, end, callback); }, this.dialogAjoutEvenement, this.evenementGestion, jqDialogDetailsEvenement);
		
		this.listeGroupesParticipants = new ListeGroupesParticipants(this.restManager, this.calendrier, $("#liste_groupes"));
		
		this.verifieGroupeEnAttenteRattachement();
	};
	
	EcranAccueil.prototype.rafraichirCalendrier = function() {
		if(this.calendrier != null) {
			this.calendrier.refetchEvents();
		}
	};
	
	EcranAccueil.prototype.getRechercheSalle = function() {
		return this.rechercheSalle;
	};
	
	EcranAccueil.prototype.getEvenementGestion = function() {
		return this.evenementGestion;
	};
	
	EcranAccueil.prototype.getCalendrier = function() {
		return this.calendrier;
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
		
		if(this.calendrier != null) {
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
				this.listeGroupesParticipants.afficherBlocVosAgendas(); // Ne fait rien si déjà appelé
			}
			break;
			
		case EcranAccueil.MODE_MES_EVENEMENTS:
			
			if(this.listeGroupesParticipants) {
				this.listeGroupesParticipants.clear();
			}
			
			this.remplirMesEvenements(start, end, callback);
			break;
			
		default: 
			
			if(this.listeGroupesParticipants) {
				this.listeGroupesParticipants.clear();
			}
			// TODO : gérer les autres modes d'affichage
			callback(new Array());
		
		}
	};
	
	/**
	 * Méthode fournissant au callback de fullcalendar les évènements de la période demandée,
	 * pour les évènements d'abonnement de l'utilisateur
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
				callbackCalendrier(me.calendrier.filtrerMatiereTypeRespo(evenementsGroupesActifs));
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
	 * Méthode fournissant au callback de fullcalendar les évènements de la période demandée,
	 * pour les évènements dont l'utilisateur est propriétaire
	 * 
	 * @param dateDebut date de début de la période
	 * @param dateFin date de fin de la période
	 * @param callbackCalendrier callback de fullcalendar
	 */
	EcranAccueil.prototype.remplirMesEvenements = function(dateDebut, dateFin, callbackCalendrier) {
		
		var me = this;
		this.evenementGestion.getMesEvenements(dateDebut, dateFin, false, function(resultCode, data) {
			if(resultCode == RestManager.resultCode_Success) {
				// Filtrage et passage à fullcalendar
				callbackCalendrier(me.calendrier.filtrerMatiereTypeRespo(data));
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
	
	
	/**
	 * Vérifie s'il y a des groupes en attente de rattachement et s'il y en a l'information est affiché
	 */
	EcranAccueil.prototype.verifieGroupeEnAttenteRattachement = function() {

		this.groupeGestion.queryGroupesEnAttenteRattachement(function(resultCode, data) {
			
			if(resultCode == RestManager.resultCode_Success) {
				if (data.length>0) {
					$("#bulle_information")
						.html("<img id='bulle_information_fermer' src='img/fermer.png' title='Fermer' />")
						.append("Vous avez des demandes de rattachement.<br/><span id='bulle_information_clique'>Cliquez pour les gérer.</span>")
						.draggable({opacity: 0.5})
						.fadeTo(300, 0.9);
					
					// Listeners
					$("#bulle_information_fermer").click(function() {
						$("#bulle_information").fadeOut(300);
					});
					$("#bulle_information_clique").click(function() {
						Davis.location.assign("parametres/mes_groupes");
						$("#bulle_information").fadeOut(300);
					});
				}
			}
			else {
				window.showToast("Erreur de récupération des groupes en attente de rattachement. Code d'erreur " + resultCode);
			}
		});
	};
	
	return EcranAccueil;
});
