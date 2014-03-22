/**
 * Module de gestion de l'interface du tableau de planning des groupes
 * @module PlanningGroupes
 */
define(["text!../../templates/planning_groupes.tpl", "underscore", "moment", "EvenementGestion", "moment_fr", "jquery"], function(tplPlanningGroupes, _, moment, EvenementGestion) {

	/**
	 * @constructor
	 * @alias module:PlanningGroupes
	 */
	var PlanningGroupes = function(dialogDetailsEvenement, jqPlanningGroupes, jqBtnPrecedent, jqBtnSuivant, jqBtnAujourdhui, jqLabelJour, onFetchCallback, evenementGestion, jqDatepicker) {
		this.dialogDetailsEvenement = dialogDetailsEvenement;
		this.jqPlanningGroupes = jqPlanningGroupes;
		this.jqLabelJour = jqLabelJour;
		this.groupes = new Array();
		this.template = _.template(tplPlanningGroupes);
		this.date = null;
		this.onFetchCallback = onFetchCallback;
		this.evenementGestion = evenementGestion;
		this.jqDatepicker = jqDatepicker;
		this.currentShownEvents = new Array();
		
		// Réglage de la langue
		moment.lang("fr");
			
		// Listeners
		var me = this;
		jqBtnPrecedent.click(function(e) {
			me.changeDatePicker(moment(me.jqDatepicker.DatePickerGetDate()).add("days", -7).toDate());
			me.setDate(moment(me.date).add("days", -7).toDate());
			me.renderDate();
		});
		
		jqBtnSuivant.click(function(e) {
			me.changeDatePicker(moment(me.jqDatepicker.DatePickerGetDate()).add("days", 7).toDate());
			me.setDate(moment(me.date).add("days", 7).toDate());
			me.renderDate();
		});
		
		jqBtnAujourdhui.click(function(e) {
			me.changeDatePicker(new Date());
			me.setDate(new Date());
			me.renderDate();
		});
		
		this.render();
		this.setDate(new Date());
	};
	
	PlanningGroupes.prototype.render = function() {
		var groupes;
		var nbGroupes = _.keys(this.groupes).length; 
		if(nbGroupes > 0) {
			groupes = this.groupes;
		}
		else {
			groupes = { 0: "(Aucun groupe)" };
		}
		
		this.jqPlanningGroupes.empty().append(this.template({  groupes: groupes, nbGroupes: nbGroupes }));
		
		this.renderDate();
	};
	
	/**
	 * Définition des nouveaux groupes concernés par l'affichage
	 * @param newGroupes Nouveaux groupes à utiliser
	 */
	PlanningGroupes.prototype.resetGroupes = function(newGroupes) {
		this.groupes = newGroupes;
		this.render();
		this.refetchEvents();
	};
	
	PlanningGroupes.prototype.showEvents = function(events) {
		
		var me = this;
		this.currentShownEvents = events;
		
		// Vidage des événements déjà affichés
		this.jqPlanningGroupes.find(".evenement_groupe").remove();
		
		for(var i=0, maxI=events.length; i<maxI; i++) {
			// Les événements peuvent être en double pour chaque groupe
			// Ajout de chaque événement à la ligne de son groupe
			var jourEven = events[i].start.getDay(); // 0 -> 6 : dimanche -> samedi
			var caseJour = this.jqPlanningGroupes.find("#ligne_groupe_" + events[i].idGroupe + " td.jour:eq(" + (jourEven + 1) + ")");
			
			// Calcul de l'offset pour l'heure de début (en pourcentage)
			var momDebut = moment(events[i].start);
			var momFin = moment(events[i].end);
			var diff0h = momDebut.diff(moment(events[i].start).startOf("day"), "hours", true); // Nombre d'heures (avec virgule) depuis 0h00
			var offsetDebut = (diff0h - 8) * 100 / (20 - 8);
			
			if(offsetDebut >= 100) continue; // L'événement commence après 20h
			
			// Durée (largeur en pourcentage)
			var duree = momFin.diff(momDebut, "hours", true);
			var width = duree * 100 / (20-8);
			
			if(offsetDebut + width <= 0) continue; // L'événement finit avant 8h
			if(offsetDebut < 0) offsetDebut = 0;
			if(offsetDebut + width > 100) width = 100 - offsetDebut;
			
			// Ajout de l'événement
			var event = events[i];
			
			var jqEven = $("<div class='evenement_groupe'></div>").css({ left: offsetDebut + '%', width: width + '%', backgroundColor: events[i].color })
				.attr("title", events[i].nom)
				.attr("data-event-index", i);
			
			caseJour.prepend(jqEven);
			
			if (!event.specialDay) {
				jqEven.click(function(e) {
					var jqThis = $(this);
					var eventIndex = parseInt(jqThis.attr("data-event-index"));
					me.dialogDetailsEvenement.show(me.currentShownEvents[eventIndex], $(this));
				});
			}
		}
	};
	
	PlanningGroupes.prototype.refetchEvents = function() {
		var me = this;
		var dateFin = moment(this.date).add("days", 6).toDate();
		this.onFetchCallback(this.date, dateFin, function(events) {
			
			// Récupération des jours spéciaux filtrés (car déjà récupérés dans EcranPlanningCours)
			var listeJoursSpeciauxFiltres = me.evenementGestion.filtrerJoursSpeciaux(me.evenementGestion.joursSpeciaux);
			
			// Pour chaque jour spécial, on créer un événement par groupe pour afficher le jour spécial dans chaque ligne concernée
			for (var i=0; i<listeJoursSpeciauxFiltres.length; i++) {
				var e = listeJoursSpeciauxFiltres[i];
				for (var j=0; j<e.groupes.length; j++) {
					var copieE = new Object();
					copieE.nom = e.nom;
					copieE.start = e.start;
					copieE.end = e.end;
					copieE.idGroupe = e.groupes[j].id;
					copieE.color = e.color;
					copieE.specialDay = true;
					events.push(copieE);
				}
			}
			
			me.showEvents(events);
		});
	};
	
	PlanningGroupes.prototype.getDate = function() {
		return this.date;
	};
	
	/**
	 * Mise à jour de l'affichage des dates dans les colonnes
	 */
	PlanningGroupes.prototype.renderDate = function() {
		var mom = moment(this.date);
		this.jqPlanningGroupes.find("th.jour").each(function() {
			$(this).text(mom.format("dddd D/MM"));
			mom.add("days", 1);
		});
		
		mom.add("days", -1);
		
		// Numéro de semaine affiché
		var momDebut = moment(this.date);
		this.jqPlanningGroupes.find("#planning_groupes_num_semaine").text(momDebut.format("w"));
		
		// Plage de dates affichée
		var strIntervalle = mom.month() == momDebut.month() ? 
				momDebut.date() + " - " + mom.date() + mom.format(" MMMM YYYY") 
				: momDebut.format("Do MMMM - ") + mom.format("Do MMMM YYYY");
		
		this.jqLabelJour.text(strIntervalle);
	};

	PlanningGroupes.prototype.setDate = function(date) {
		this.date = moment(date).startOf("isoweek").toDate();
		
		this.refetchEvents();
	};
	
	PlanningGroupes.prototype.gotoDate = function(date) {
		this.setDate(moment(date).toDate());
		this.renderDate();
	};

	PlanningGroupes.prototype.changeDatePicker = function(date) {
		if (this.jqDatepicker) {
			this.jqDatepicker.DatePickerSetDate(date, date);
		}
	};

	return PlanningGroupes;
});

