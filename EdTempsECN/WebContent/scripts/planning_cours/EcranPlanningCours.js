/**
 * Module d'affichage de l'écran de planification de cours
 * Associé au HTML templates/page_planning_cours.html
 * @module EcranPlanningCours
 */
define(["EvenementGestion", "DialogAjoutEvenement", "RechercheSalle", "Calendrier", "text!../../templates/dialog_ajout_evenement.html", "text!../../templates/dialog_recherche_salle.html",
        "RestManager", "CalendrierGestion", "jquery"], function(EvenementGestion, DialogAjoutEvenement, RechercheSalle, Calendrier, 
        		dialogAjoutEvenementHtml, dialogRechercheSalleHtml, RestManager, CalendrierGestion) {
	
	/**
	 * @constructor
	 * @alias EcranPlanningCours 
	 */
	var EcranPlanningCours = function(restManager) {
		var me = this;
		this.restManager = restManager;
		this.evenementGestion = new EvenementGestion(restManager);
		this.calendrierGestion = new CalendrierGestion(restManager);
		
		var jqDialogRechercheSalle = $("#recherche_salle_libre").append(dialogRechercheSalleHtml);
		this.rechercheSalle = new RechercheSalle(restManager, jqDialogRechercheSalle);
		
		var jqDialogAjoutEvenement = $("#dialog_ajout_evenement").append(dialogAjoutEvenementHtml);
		this.dialogAjoutEvenement = new DialogAjoutEvenement(restManager, jqDialogAjoutEvenement, this.rechercheSalle, this.evenementGestion, function() { me.calendrier.refetchEvents(); });
		
		var jqDatepicker = null; // TODO : ajouter le datepicker sur la gauche
		
		this.calendrier = new Calendrier(function(start, end, callback) { me.onCalendarFetchEvents(start, end, callback); }, 
				this.dialogAjoutEvenement, this.evenementGestion, $("#dialog_details_evenement"), jqDatepicker);
		
		// Si l'utilisateur a le droit, on affiche le bouton pour accéder à l'écran de gestion des jours bloqués
		if (this.restManager.aDroit(RestManager.actionsEdtemps_GererJoursBloques)) {
			$("#bouton_jours_bloques").html('<a href="#jours_bloques" class="button">Jours bloqués</a>');
		}
		
		// Récupération des calendriers de l'utilisateur pour remplir les select
		this.calendrierGestion.listerMesCalendriers(function(resultCode, data) {
			if(resultCode == RestManager.resultCode_Success) {
				console.log(data);
			}
			else if(resultCode == RestManager.resultCode_NetworkError) {
				window.showToast("Erreur de récupération de vos calendriers ; vérifiez votre connexion");
			}
			else {
				window.showToast("Erreur de récupération de vos calendriers.");
			}
		});
	};
	
	EcranPlanningCours.VUE_NORMALE = "vue_normale";
	EcranPlanningCours.VUE_GROUPES = "vue_groupes";
	
	EcranPlanningCours.prototype.setVue = function(vue) {
		
		$("#nav_vue_agenda li").removeClass("selected");
		if(vue === EcranPlanningCours.VUE_GROUPES) {
			$("#nav_vue_agenda #tab_vue_groupes").addClass("selected");
			$("#ligne_select_calendrier").hide();
			// TODO : changer le contenu
		}
		else {
			$("#nav_vue_agenda #tab_vue_normale").addClass("selected");
			$("#ligne_select_calendrier").show();
			// TODO : changer le contenu
		}
	};
	
	EcranPlanningCours.prototype.onCalendarFetchEvents = function(start, end, callback) {
		// TODO : compléter
		callback(new Array()); // doit fournir les événements à affiche dans le calendrier
	};
	
	return EcranPlanningCours;
});