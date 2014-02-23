/**
 * Module d'affichage de l'écran de planification de cours
 * Associé au HTML templates/page_planning_cours.html
 * @module EcranPlanningCours
 */
define(["EvenementGestion", "DialogAjoutEvenement", "RechercheSalle", "Calendrier", "text!../../templates/dialog_ajout_evenement.html", "text!../../templates/dialog_recherche_salle.html",
        "RestManager", "CalendrierGestion", "planning_cours/BlocStatistiques", "planning_cours/DialogRepeter", "planning_cours/PlanningGroupes", "underscore", "jquery"], 
        function(EvenementGestion, DialogAjoutEvenement, RechercheSalle, Calendrier, 
        		dialogAjoutEvenementHtml, dialogRechercheSalleHtml, RestManager, CalendrierGestion, 
        		BlocStatistiques, DialogRepeter, PlanningGroupes, _) {
	
	/**
	 * @constructor
	 * @alias EcranPlanningCours 
	 */
	var EcranPlanningCours = function(restManager) {
		var me = this;
		this.restManager = restManager;
		
		var jqDialogRechercheSalle = $("#recherche_salle_libre").append(dialogRechercheSalleHtml);
		this.estVueGroupes = false;
		this.rechercheSalle = new RechercheSalle(restManager, jqDialogRechercheSalle);
		this.evenementGestion = new EvenementGestion(restManager);
		this.calendrierGestion = new CalendrierGestion(restManager);
		this.blocStatistiques = new BlocStatistiques(restManager, $("#bloc_statistiques"));
		this.dialogRepeter = new DialogRepeter(restManager, $("#dialog_repeter"), this.rechercheSalle, 
				this.evenementGestion, function() { me.callbackAjoutEvenement(); });
		
		this.mesCalendriers = null; // Ensemble des calendriers indexés par ID
		this.calendrierSelectionne = null;
		this.calendriersVueGroupe = null;
		this.dateDebutStatistiquesAJour = null; // Date de début des statistiques affichées

		
		var jqDialogAjoutEvenement = $("#dialog_ajout_evenement").append(dialogAjoutEvenementHtml);
		this.dialogAjoutEvenement = new DialogAjoutEvenement(restManager, jqDialogAjoutEvenement, this.rechercheSalle, 
				this.evenementGestion, function() { me.callbackAjoutEvenement(); });
		
		var jqDatepicker = null; // TODO : ajouter le datepicker sur la gauche
		
		this.calendrier = new Calendrier(function(start, end, callback) { 
				me.onCalendarFetchEvents(start, end, callback);
			}, this.dialogAjoutEvenement, this.evenementGestion, $("#dialog_details_evenement"), jqDatepicker, this.dialogRepeter);
		
		this.planningGroupes = new PlanningGroupes($("#planning_groupes"), $("#planning_groupes_btn_precedent"), 
				$("#planning_groupes_btn_suivant"), $("#planning_groupes_btn_aujourdhui"), $("#planning_groupes_label_date"), function(start, end, callback) {
			me.onCalendarFetchEvents(start, end, callback);
		});
		
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
					if(data[i].matiere) {
						objMatieres[data[i].matiere] = true;
					}
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
			if(me.estVueGroupes) {
				me.updateMatiereVueGroupe();
			}
		});
		
		$("#select_calendrier").change(function() {
			me.callbackSelectCalendrier();
		});
	};
	
	/**
	 * Callback appelé après l'ajout d'un événement, pour mettre à jour l'affichage
	 */
	EcranPlanningCours.prototype.callbackAjoutEvenement = function() {
		
		this.dateDebutStatistiquesAJour = null;
		
		// Re-récupérer les événements met aussi à jour les statistiques avec onCalendarFetchEventss
		if(this.estVueGroupes) {
			this.calendrier.refetchEvents();
		}
		else {
			this.planningGroupes.refetchEvents();
		}
	};
	
	/**
	 * Remplissage du select des calendriers en fonction du select des matières (qui sert de filtre)
	 */
	EcranPlanningCours.prototype.remplirSelectCalendriers = function() {
		var matiere = $("#select_matiere").val();
		var cals;
		
		if(matiere === '') {
			cals = _.values(this.mesCalendriers);
		}
		else {
			cals = _.where(this.mesCalendriers, { matiere: matiere });
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
			this.calendrierSelectionne = null;
		}
		else {
			var idCalendrierSelectionne = parseInt(calId);
			this.calendrierSelectionne = this.mesCalendriers[idCalendrierSelectionne];
			$("#select_matiere").val(this.calendrierSelectionne.matiere);
			
			// Remplissage de la liste des groupes sélectionnés
			$("#lst_groupes_associes").text(this.calendrierSelectionne.nomsGroupesParents.join(", "));
			
			// Mise à jour du bloc de statistiques
			this.updateStatistiques();
			
			// Renseignement du calendrier pour la répétition
			this.dialogRepeter.setCalendrier(this.calendrierSelectionne);
		}
		
		this.evenementGestion.clearCache(EvenementGestion.CACHE_MODE_PLANNING_CALENDRIER);
		this.calendrier.refetchEvents(); // La sélection du calendrier n'est disponible qu'en vue normale
	};
	
	/**
	 * Récupération des statistiques si un calendrier est sélectionné, et mise à jour du bloc de statistiques
	 */
	EcranPlanningCours.prototype.updateStatistiques = function() {
		if(this.calendrierSelectionne) {
			
			// Récupération de la date en cours d'affichage
			var dateAffichage = this.estVueGroupes ? this.planningGroupes.getDate() : this.calendrier.getDate();
			var aout = new Date(dateAffichage.getFullYear(), 8, 15); // 1 an du 15 août au 15 août
			var dateDebut = new Date(aout > dateAffichage ? dateAffichage.getFullYear() - 1 : dateAffichage.getFullYear(), 8, 15);
			var dateFin = new Date(dateDebut.getFullYear() + 1, 8, 15);
			
			if(this.dateDebutStatistiquesAJour == dateDebut) {
				return; // Déjà mis à jour (sauf erreur de mise à jour : pas pris en compte)
			}
			else {
				this.dateDebutStatistiquesAJour = dateDebut;
			}
			
			this.blocStatistiques.setGroupes(this.calendrierSelectionne.groupesParents, this.calendrierSelectionne.nomsGroupesParents);
			this.blocStatistiques.refreshStatistiques(this.calendrierSelectionne.matiere, dateDebut, dateFin);
		}
	};
	
	EcranPlanningCours.VUE_NORMALE = "vue_normale";
	EcranPlanningCours.VUE_GROUPES = "vue_groupes";
	
	EcranPlanningCours.prototype.setVue = function(vue) {
		
		$("#nav_vue_agenda li").removeClass("selected");
		if(vue === EcranPlanningCours.VUE_GROUPES) {
			this.estVueGroupes = true;
			$("#nav_vue_agenda #tab_vue_groupes").addClass("selected");
			$("#ligne_select_calendrier, #calendar").hide();
			$("#page_planning_groupes").show();
			this.updateMatiereVueGroupe();
		}
		else {
			this.estVueGroupes = false;
			$("#nav_vue_agenda #tab_vue_normale").addClass("selected");
			$("#ligne_select_calendrier, #calendar").show();
			$("#page_planning_groupes").hide();
		}
	};
	
	/**
	 * Mise à jour des groupes affichés dans la "vue groupes" et des calendriers sélectionnés (this.calendriersVueGroupe)
	 * en utilisant la matière sélectionnée (tous les groupes des calendriers de la matière)
	 */
	EcranPlanningCours.prototype.updateMatiereVueGroupe = function() {
		var matiere = $("#select_matiere").val();
		
		// Récupération des calendriers de la matière
		if(matiere) {
			this.calendriersVueGroupe = _.where(this.mesCalendriers, { matiere: matiere });
		}
		else {
			this.calendriersVueGroupe = new Array();
		}
		
		// Récupération des groupes de ce calendrier
		var groupesVueGroupe = new Array();
		for(var i=0, maxI=this.calendriersVueGroupe.length; i<maxI; i++) {
			for(var j=0, maxJ=this.calendriersVueGroupe[i].groupesParents.length; j<maxJ; j++) {
				groupesVueGroupe.push({ id: this.calendriersVueGroupe[i].groupesParents[j], nom: this.calendriersVueGroupe[i].nomsGroupesParents[j] });
			}
		}
		
		$("#lst_groupes_associes").text(_.pluck(groupesVueGroupe, "nom").join(", "));
		
		this.planningGroupes.resetGroupes(groupesVueGroupe);
	};
	
	EcranPlanningCours.prototype.onCalendarFetchEvents = function(start, end, callback) {
		var me = this;

		if((!this.estVueGroupes && this.calendrierSelectionne) || (this.calendriersVueGroupe && this.estVueGroupes)) {
			
			// Fonction de récupération en fonction de la vue
			var funcReq = this.estVueGroupes ? this.evenementGestion.getEvenementsGroupesCalendriers : this.evenementGestion.getEvenementsGroupesCalendrier;
			var arg = this.estVueGroupes ? _.pluck(this.calendriersVueGroupe, "id") : this.calendrierSelectionne.id;
			
			funcReq.call(this.evenementGestion, start, end, arg, false, function(resultCode, evenements) {
				if(resultCode === RestManager.resultCode_Success) {
					// Evénements des autres calendriers en gris
					for(var i=0,maxI=evenements.length; i<maxI; i++) {
						
						if(!_.contains(evenements[i].calendriers, me.calendrierSelectionne.id)) {
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
			
			// Mise à jour des statistiques (pas de requête effectuée si déjà à jour)
			this.updateStatistiques();
		}
	};
	
	return EcranPlanningCours;
});