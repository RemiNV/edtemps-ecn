/**
 * Module de gestion de l'interface du tableau de planning des groupes
 * @module PlanningGroupes
 */
define(["text!../../templates/planning_groupes.tpl", "underscore", "moment", "moment_fr", "jquery"], function(tplPlanningGroupes, _, moment) {

	/**
	 * @constructor
	 * @alias module:PlanningGroupes
	 */
	var PlanningGroupes = function(jqPlanningGroupes, jqBtnPrecedent, jqBtnSuivant, jqBtnAujourdhui, jqLabelJour, onFetchCallback) {
		this.jqPlanningGroupes = jqPlanningGroupes;
		this.jqLabelJour = jqLabelJour;
		this.groupes = new Array();
		this.template = _.template(tplPlanningGroupes);
		this.date = null;
		this.onFetchCallback = onFetchCallback;
		
		// Réglage de la langue
		moment.lang("fr");
			
		// Listeners
		var me = this;
		jqBtnPrecedent.click(function(e) {
			me.setDate(moment(me.date).add("days", -7).toDate());
		});
		
		jqBtnSuivant.click(function(e) {
			me.setDate(moment(me.date).add("days", 7).toDate());
		});
		
		jqBtnAujourdhui.click(function(e) {
			me.setDate(new Date());
		});
		
		this.render();
		this.setDate(new Date());
	};
	
	PlanningGroupes.prototype.render = function() {
		
		var groupes;
		if(this.groupes.length > 0) {
			groupes = this.groupes;
		}
		else {
			groupes = [{ nom: "(Aucun groupe)" }];
		}
		
		this.jqPlanningGroupes.empty().append(this.template({  groupes: groupes }));
	};
	
	PlanningGroupes.prototype.resetGroupes = function(newGroupes) {
		this.groupes = newGroupes;
		this.render();
		
		this.refetchEvents();
	};
	
	PlanningGroupes.prototype.showEvents = function(events) {
		console.log("Events : ", events);
	};
	
	PlanningGroupes.prototype.refetchEvents = function() {
		console.log("Refetch !");
		var me = this;
		var dateFin = moment(this.date).add("days", 6).toDate();
		this.onFetchCallback(this.date, dateFin, function(events) {
			me.showEvents(events);
		});
	};
	
	PlanningGroupes.prototype.getDate = function() {
		return this.date;
	};

	PlanningGroupes.prototype.setDate = function(date) {
		this.date = moment(date).startOf("isoweek").toDate();
		
		var mom = moment(this.date);
		this.jqPlanningGroupes.find("th.jour").each(function() {
			$(this).text(mom.format("dddd D/MM"));
			mom.add("days", 1);
		});
		
		mom.add("days", -1);
		
		// Numéro de semaine affiché
		this.jqPlanningGroupes.find("#planning_groupes_num_semaine").text(mom.format("w"));
		
		// Plage de dates affichée
		var momDebut = moment(this.date);
		var strIntervalle = mom.month() == momDebut.month() ? 
				momDebut.date() + " - " + mom.date() + mom.format(" MMMM YYYY") 
				: momDebut.format("Do MMMM - ") + mom.format("Do MMMM YYYY");
		
		this.jqLabelJour.text(strIntervalle);
		
		this.refetchEvents();
	};

	return PlanningGroupes;
});

