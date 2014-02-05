/**
 * Module d'affichage de l'écran de planification de cours
 * Associé au HTML templates/page_planning_cours.html
 * @module EcranPlanningCours
 */
define(["EvenementGestion", "DialogAjoutEvenement", "RechercheSalle", "Calendrier", "text!../../templates/dialog_ajout_evenement.html", "text!../../templates/dialog_recherche_salle.html",
        "RestManager", "CalendrierGestion", "planning_cours/BlocStatistiques", "jquery"], function(EvenementGestion, DialogAjoutEvenement, RechercheSalle, Calendrier, 
        		dialogAjoutEvenementHtml, dialogRechercheSalleHtml, RestManager, CalendrierGestion, BlocStatistiques) {
	
	/**
	 * @constructor
	 * @alias EcranPlanningCours 
	 */
	var EcranPlanningCours = function(restManager) {
		var me = this;
		this.restManager = restManager;
		this.evenementGestion = new EvenementGestion(restManager);
		this.calendrierGestion = new CalendrierGestion(restManager);
		this.blocStatistiques = new BlocStatistiques(restManager, $("#bloc_statistiques"));
		this.mesCalendriers = null; // Ensemble des calendriers indexés par ID
		this.idCalendrierSelectionne = 0;
		
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
		
		var selectMatiere = $("#select_matiere");
		
		// Récupération des calendriers de l'utilisateur pour remplir les select
		this.calendrierGestion.listerMesCalendriers(function(resultCode, data) {
			if(resultCode == RestManager.resultCode_Success) {
				var objMatieres = new Object();
				me.mesCalendriers = new Object();
				for(var i=0, max=data.length; i<max; i++) {
					objMatieres[data[i].matiere] = true;
					me.mesCalendriers[data[i].id] = data[i];
				}
				
				selectMatiere.append("<option value=''>---</option>");
				for(var matiere in objMatieres) {
					selectMatiere.append("<option value='" + matiere + "'>" + matiere + "</option>");
				}
				
				me.remplirSelectCalendriers();
			}
			else if(resultCode == RestManager.resultCode_NetworkError) {
				window.showToast("Erreur de récupération de vos calendriers ; vérifiez votre connexion");
			}
			else {
				window.showToast("Erreur de récupération de vos calendriers.");
			}
		});
		
		// Listeners
		selectMatiere.change(function() {
			me.remplirSelectCalendriers();
		});
		
		$("#select_calendrier").change(function() {
			me.callbackSelectCalendrier();
		});
	};
	
	/**
	 * Remplissage du select des calendriers en fonction du select des matières (qui sert de filtre)
	 */
	EcranPlanningCours.prototype.remplirSelectCalendriers = function() {
		var matiere = $("#select_matiere").val();
		var cals;
		
		cals = new Array();
		for(var id in this.mesCalendriers) {
			if(matiere === '' || this.mesCalendriers[id].matiere == matiere) {
				cals.push(this.mesCalendriers[id]);	
			}
		}
		
		var selectCalendrier = $("#select_calendrier").empty().append("<option value=''>---</option>");
		for(var i=0, max=cals.length; i<max; i++) {
			selectCalendrier.append("<option value='" + cals[i].id + "'>" + cals[i].nom + "</option>");
		}
	};
	
	/**
	 * Callback appelé à la sélection d'un calendrier dans le select
	 */
	EcranPlanningCours.prototype.callbackSelectCalendrier = function() {
		var calId = $("#select_calendrier").val();
		if(calId === "") {
			this.idCalendrierSelectionne = 0;
		}
		else {
			this.idCalendrierSelectionne = parseInt(calId);
			var calendrier = this.mesCalendriers[this.idCalendrierSelectionne];
			$("#select_matiere").val(calendrier.matiere);
			
			// Remplissage de la liste des groupes sélectionnés
			$("#lst_groupes_associes").text(calendrier.nomsGroupesParents.join(", "));
			
			// Mise à jour du bloc de statistiques
			this.blocStatistiques.setGroupes(calendrier.groupesParents, calendrier.nomsGroupesParents);
			// TODO : mettre de vraies dates
			this.blocStatistiques.refreshStatistiques(calendrier.matiere, new Date(2013, 3, 1), new Date(2014, 3, 1));
		}
		
		this.calendrier.refetchEvents();
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
		var me = this;
		if(this.idCalendrierSelectionne != 0) {
			this.evenementGestion.getEvenementsGroupesCalendrier(start, end, this.idCalendrierSelectionne, false, function(resultCode, evenements) {
				if(resultCode === RestManager.resultCode_Success) {
					
					// Evénements des autres calendriers en gris
					for(var i=0,maxI=evenements.length; i<maxI; i++) {
						var hasCalendrier = false;
						for(var j=0,maxJ=evenements[i].calendriers.length; j<maxJ; j++) {
							if(evenements[i].calendriers[j] === me.idCalendrierSelectionne) {
								hasCalendrier = true;
								break;
							}
						}
						
						if(!hasCalendrier) {
							evenements[i].color = "#999";
						}
					}
					callback(evenements);
				}
				else if(resultCode === RestManager.resultCode_NetworkError) {
					window.showToast("Erreur de récupération des événements ; vérifiez votre connection");
				}
				else {
					window.showToast("Erreur de récupération des événements");
				}
			});
		}
	};
	
	return EcranPlanningCours;
});