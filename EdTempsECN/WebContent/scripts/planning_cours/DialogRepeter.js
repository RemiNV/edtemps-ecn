/**
 * Dialog permettant de répéter un événement
 * @module DialogRepeter
 */
define(["underscore", "RestManager", "text!../../templates/dialog_repeter_evenement.tpl", "jquery", "jqueryui"], function(_, RestManager, dialogRepeterEvenementTpl) {
	
	/**
	 * @constructor
	 * @alias DialogRepeter 
	 */
	var DialogRepeter = function(restManager, jqBloc) {
		this.restManager = restManager;
		this.jqBloc = jqBloc;
		
		var contenuDialog = $(dialogRepeterEvenementTpl);
		this.divSynthese = contenuDialog.find("#div_synthese");
		this.templateSynthese = _.template(this.divSynthese.attr("data-template"));
		this.divSynthese.removeAttr("data-template");
		
		jqBloc.append(contenuDialog).dialog({
			autoOpen: false,
			width: 700
		});
		
	};

	
	
	DialogRepeter.prototype.show = function(evenement) {
		
		this.divSynthese.empty().append(this.templateSynthese({ synthese: new Array() }));
		
		this.jqBloc.dialog("open");
	};
	
	return DialogRepeter;
});