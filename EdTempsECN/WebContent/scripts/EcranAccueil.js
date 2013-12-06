/**
 * Module d'affichage de l'écran d'accueil (page principale d'affichage des évènements).
 * Associé au HTML templates/page_accueil.html
 * @module EcranAccueil
 */
define(["Calendrier", "EvenementGestion", "ListeGroupesParticipants", "RechercheSalle", "GroupeGestion", 
        "DialogAjoutEvenement", "RestManager", "underscore", "jquery", "jqueryui", "jquerycombobox", "datepicker"], function(Calendrier, EvenementGestion, ListeGroupesParticipants, 
        		RechercheSalle, GroupeGestion, DialogAjoutEvenement, RestManager, _) {
	
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
		this.initListeGroupesFait = false;
		this.initListeSallesFait = false;
		
		this.idGroupeSelectionne = 0;
		this.idSalleSelectionee = 0;
		
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
		
		$("#accueil_datepicker").DatePicker({
			flat: true,
			date: new Date(),
			locale: {
				days: ["Dimanche", "Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi"],
				daysShort: ["Dim", "Lun", "Mar", "Mer", "Jeu", "Ven", "Sam"],
				daysMin: ["Di", "Lu", "Ma", "Me", "Je", "Ve", "Sa"],
				months: ["Janvier", "Février", "Mars", "Avril", "Mai", "Juin", "Juillet", "Août", "Septembre", "Octobre", "Novembre", "Décembre"],
				monthsShort: ["Jan", "Fév", "Mar", "Avr", "Mai", "Juin", "Juil", "Août", "Sep", "Oct", "Nov", "Déc"],
				weekMin: 'sem'
			}
		});
		
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
		
		this.calendrier = new Calendrier(function(start, end, callback) { me.onCalendarFetchEvents(start, end, callback); }, 
				this.dialogAjoutEvenement, this.evenementGestion, $("#dialog_details_evenement"));
		
		this.listeGroupesParticipants = new ListeGroupesParticipants(this.restManager, this.calendrier, $("#liste_groupes"));
		
		this.verifieAttentesRattachements();
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
			$("#choix_groupe").css("display", "block");
			this.initListeGroupes();
			this.mode = EcranAccueil.MODE_GROUPE;
			break;
		case "vue_salle":
			$("#nav_vue_agenda #tab_vue_salle").addClass("selected");
			$("#choix_salle").css("display", "block");
			this.initListeSalles();
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
		
		if(vue != "mes_abonnements" && this.listeGroupesParticipants) {
			this.listeGroupesParticipants.clear();
		}
		
		if(vue != "vue_salle") {
			$("#choix_salle").css("display", "none");
		}
		
		if(vue != "vue_groupe") {
			$("#choix_groupe").css("display", "none");
		}
		
		if(this.calendrier != null) {
			this.calendrier.refetchEvents();
		}
	};
	
	EcranAccueil.prototype.initListeGroupes = function() {
		if(this.initListeGroupesFait) {
			return;
		}
		
		var me = this;
		this.restManager.effectuerRequete("GET", "groupeparticipants/lister", {
			token: this.restManager.getToken()
		}, function(data) {
			if(data.resultCode == RestManager.resultCode_Success) {
				// Utilisation d'un template compilé pour accélérer (j'imagine ?) la génération des lignes
				var optionTpl = _.template("<option value='<%= value %>'><%= label %></option>");
				var groupes = data.data;
				var lstGroupes = $("#select_choix_groupe");
				
				lstGroupes.append(optionTpl({
					value: 0,
					label: "(Choisissez un groupe)"
				}));
				
				for(var i=0, maxI=groupes.length; i<maxI; i++) {
					lstGroupes.append(optionTpl({
						value: groupes[i].id,
						label: groupes[i].nom
					}));
				}
				
				lstGroupes.combobox({
					select: function(event, ui) {
						
						me.idGroupeSelectionne = parseInt(ui.item.value);
						
						// Suppression du cache (on change de groupe -> événements plus valides)
						me.evenementGestion.clearCache(EvenementGestion.CACHE_MODE_GROUPE);
						me.calendrier.refetchEvents();
					}
				});
				$("#message_chargement_groupes").css("display", "none");
				me.initListeGroupesFait = true;
			}
			else if(data.resultCode == RestManager.resultCode_NetworkError) {
				window.showToast("Echec de la récupération des groupes ; vérifiez votre connexion");
			}
			else {
				window.showToast("Erreur lors de la récupération des groupes");
			}
		});
	};
	
	EcranAccueil.prototype.initListeSalles = function() {
		if(this.initListeSallesFait) {
			return;
		}

		var me = this;
		this.restManager.effectuerRequete("GET", "salles", {
			token: this.restManager.getToken()
		}, function(data) {
			if(data.resultCode == RestManager.resultCode_Success) {
				// Utilisation d'un template compilé pour accélérer (j'imagine ?) la génération des lignes
				var optionTpl = _.template("<option value='<%= value %>'><%= label %></option>");
				var salles = data.data;
				var lstSalles = $("#select_choix_salle");
				
				lstSalles.append(optionTpl({
					value: 0,
					label: "(Choisissez une salle)"
				}));
				
				for(var i=0, maxI=salles.length; i<maxI; i++) {
					lstSalles.append(optionTpl({
						value: salles[i].id,
						label: salles[i].nom
					}));
				}
				
				lstSalles.combobox({
					select: function(event, ui) {
						
						me.idSalleSelectionee = parseInt(ui.item.value);
						
						// Suppression du cache (on change de salle -> événements plus valides)
						me.evenementGestion.clearCache(EvenementGestion.CACHE_MODE_SALLE);
						me.calendrier.refetchEvents();
					}
				});
				$("#message_chargement_salles").css("display", "none");
				
				me.initListeSallesFait = true;
			}
			else if(data.resultCode == RestManager.resultCode_NetworkError) {
				window.showToast("Echec de la récupération des salles ; vérifiez votre connexion");
			}
			else {
				window.showToast("Erreur lors de la récupération des salles");
			}
		});
	};
	
	
	/**
	 * Fonction appelée par fullCalendar lorsque le mécanisme de "fetch" est déclenché
	 * @param start Début de la période pendant laquelle les évènements doivent être récupérés
	 * @param end Fin de la période pendant laquelle les évènements doivent être récupérés
	 * @param callback Callback de fullcalendar auquel il faut fournir les évènements (au format compatible fullcalendar)
	 */
	EcranAccueil.prototype.onCalendarFetchEvents = function(start, end, callback) {

		switch(this.mode) {	
		case EcranAccueil.MODE_MES_EVENEMENTS:
			this.remplirMesEvenements(start, end, callback);
			break;
			
		case EcranAccueil.MODE_GROUPE:
			if(!this.initListeGroupesFait || this.idGroupeSelectionne === 0) {
				callback(new Array());
				return;
			}
			
			this.remplirEvenementsGroupe(start, end, callback);
			break;
			
		case EcranAccueil.MODE_SALLE:
			if(!this.initListeSallesFait || this.idSalleSelectionee === 0) {
				callback(new Array());
				return;
			}
			
			this.remplirEvenementsSalle(start, end, callback);
			break;
			
		default:
		case EcranAccueil.MODE_MES_ABONNEMENTS:
			if(!this.abonnementsRecuperes) { // Récupération des abonnements (premier affichage de la page)
				this.remplirMesAbonnements(start, end, callback);
			}
			else { // Récupération uniquement des évènements (pas besoin des calendriers & groupes). Utilisation du cache.
				this.remplirEvenementsAbonnements(start, end, callback);
				this.listeGroupesParticipants.afficherBlocVosAgendas(); // Ne fait rien si déjà appelé
			}
			break;
		}
	};
	
	/**
	 * Callback générique appelé après la récupération d'événements,
	 * pour les fournir à fullcalendar
	 */
	var callbackRemplirEvenements = function(resultCode, data, callbackCalendrier) {
		if(resultCode == RestManager.resultCode_Success) {
			// Filtrage et passage à fullcalendar
			callbackCalendrier(this.calendrier.filtrerMatiereTypeRespo(data));
		}
		else if(resultCode == RestManager.resultCode_MaxRowCountExceeded) {
			window.showToast("&Eacute;vénements trop nombreux pour être affichés (plus de 100) ; vérifiez vos abonnements ou votre requête");
		}
		else if(resultCode == RestManager.resultCode_NetworkError) {
			window.showToast("Impossible de charger vos évènements ; vérifiez votre connexion.");
		}
		else {
			window.showToast("Erreur de chargement de vos événements");
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
				callbackRemplirEvenements.apply(me, [resultCode, evenementsGroupesActifs, callbackCalendrier]);
			}
			else {
				callbackRemplirEvenements.apply(me, [resultCode, data, callbackCalendrier]);
				window.showToast("Erreur de chargement de vos évènements. Votre session a peut-être expiré ?");
			}
		});
	};
	
	/**
	 * Méthode fournissant au callback de fullcalendar les évènements de la période demandée,
	 * pour les évènements dans lesquels l'utilisateur est intervenant
	 * 
	 * @param dateDebut date de début de la période
	 * @param dateFin date de fin de la période
	 * @param callbackCalendrier callback de fullcalendar
	 */
	EcranAccueil.prototype.remplirMesEvenements = function(dateDebut, dateFin, callbackCalendrier) {
		var me = this;
		this.evenementGestion.getMesEvenements(dateDebut, dateFin, false, 
				function(resultCode, data) { callbackRemplirEvenements.apply(me, [resultCode, data, callbackCalendrier]); });
	};
	
	/**
	 * Méthode fournissant au callback de fullcalendar les évènements de la période demandée,
	 * pour les évènements d'un groupe
	 * 
	 * @param dateDebut date de début de la période
	 * @param dateFin date de fin de la période
	 * @param callbackCalendrier callback de fullcalendar
	 */
	EcranAccueil.prototype.remplirEvenementsGroupe = function(dateDebut, dateFin, callbackCalendrier) {
		var me = this;
		this.evenementGestion.getEvenementsGroupe(dateDebut, dateFin, this.idGroupeSelectionne, false, 
				function(resultCode, data) { callbackRemplirEvenements.apply(me, [resultCode, data, callbackCalendrier]); });
	};
	
	/**
	 * Méthode fournissant au callback de fullcalendar les évènements de la période demandée,
	 * pour les évènements d'une salle
	 * 
	 * @param dateDebut date de début de la période
	 * @param dateFin date de fin de la période
	 * @param callbackCalendrier callback de fullcalendar
	 */
	EcranAccueil.prototype.remplirEvenementsSalle = function(dateDebut, dateFin, callbackCalendrier) {
		var me = this;
		this.evenementGestion.getEvenementsSalle(dateDebut, dateFin, this.idSalleSelectionee, false, 
				function(resultCode, data) { callbackRemplirEvenements.apply(me, [resultCode, data, callbackCalendrier]); });
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
	 * Vérifie s'il y a des groupes ou des calendriers en attente de rattachement
	 * S'il y en a l'information est affichée dans une bulle rouge
	 */
	EcranAccueil.prototype.verifieAttentesRattachements = function() {

		this.groupeGestion.queryGroupesEtCalendriersEnAttenteRattachement(function(resultCode, listeGroupes, listeCalendriers) {
			
			if(resultCode == RestManager.resultCode_Success) {
				if (listeGroupes.length>0 || listeCalendriers.length>0) {
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
