/**
 * Module de gestion de l'interface du tableau de planning des groupes
 * @module PlanningGroupes
 */
define(["text!../../templates/planning_groupes.tpl", "underscore", "jquery"], function(tplPlanningGroupes, _) {

	/**
	 * @constructor
	 * @alias module:PlanningGroupes
	 */
	var PlanningGroupes = function(jqPlanningGroupes) {
		this.jqPlanningGroupes = jqPlanningGroupes;
		this.template = _.template(tplPlanningGroupes);
		
		jqPlanningGroupes.append(this.template({ jours: ["lundi", "mardi", "mercredi", "jeudi", "vendredi", "samedi"], 
			groupes: [{ nom : "groupe test 1" }, { nom: "groupe test 2" }]
		}));
	};
	
	PlanningGroupes.prototype.refetchEvents = function() {
		console.log("Refetch !");
		// TODO : remplir correctement
	};
	
	PlanningGroupes.prototype.getDate = function() {
		// TODO : remplir correctement
		return new Date();
	};


	return PlanningGroupes;
});