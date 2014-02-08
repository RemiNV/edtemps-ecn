/**
 * Dialog permettant de répéter un événement
 * @module DialogRepeter
 */
define(["underscore", "RestManager", "text!../../templates/dialog_repeter_evenement.tpl", "lib/fullcalendar.translated.min", "jquery", "jqueryui"], 
		function(_, RestManager, dialogRepeterEvenementTpl) {
	
	/**
	 * @constructor
	 * @alias DialogRepeter 
	 */
	var DialogRepeter = function(restManager, jqBloc) {
		var me = this;
		this.restManager = restManager;
		this.jqBloc = jqBloc;
		this.evenement = null;
		
		var contenuDialog = $(dialogRepeterEvenementTpl);
		this.divSynthese = contenuDialog.find("#div_synthese");
		this.templateSynthese = _.template(this.divSynthese.attr("data-template"));
		this.divSynthese.removeAttr("data-template");
		
		jqBloc.append(contenuDialog).dialog({
			autoOpen: false,
			width: 700
		});
		
		// Listeners
		jqBloc.find("#btn_previsualiser").click(function(e) {
			me.lancerPrevisualisation();
		});
	};

	DialogRepeter.prototype.lancerPrevisualisation = function() {
		var me = this;
		var jqNbEvenements = this.jqBloc.find("#input_nb_evenements");
		var jqPeriode = this.jqBloc.find("#input_frequence");
		var nbRepetitions = parseInt(jqNbEvenements.val());
		var periode = parseInt(jqPeriode.val());
		
		// Vérification des entrées
		function verifInput(input, val) {
			if(isNaN(val) || !val) {
				input.addClass("invalide");
				return false;
			}
			else {
				input.removeClass("invalide");
				return true;
			}
		} 
		
		if(!verifInput(jqNbEvenements, nbRepetitions)) return;
		if(!verifInput(jqPeriode, periode)) return;
		
		this.restManager.effectuerRequete("GET", "repeterevenement/previsualiser", {
			token: this.restManager.getToken(),
			idEvenement: this.evenement.id,
			nbRepetitions: nbRepetitions,
			periode: periode
		}, function(response) {
			if(response.resultCode == RestManager.resultCode_Success) {
				for(var i=0; i<response.data.length; i++) {
					me.parseTest(response.data[i]);
				}
				
				// Remplissage du template
				me.divSynthese.empty().append(me.templateSynthese({ synthese: response.data }));
			}
			else if(response.resultCode == RestManager.resultCode_NetworkError) {
				window.showToast("Erreur de récupération de la prévisualisation ; vérifiez votre connexion");
			}
			else {
				window.showToast("Erreur de récupération de la prévisualisation");
			}
		});
	};
	
	DialogRepeter.prototype.parseTest = function(test) {
		test.strDate = $.fullCalendar.formatDate(new Date(test.debut), "dd/MM/yyyy");
		test.strProblemes = test.problemes.length > 0 ? "" : "OK";
		test.afficherBoutonForcer = (test.problemes.length > 0);
		test.afficherBoutonRechercheSalle = false;
		for(var i=0,max=test.problemes.length; i<max; i++) {
			if(test.strProblemes) {
				test.strProblemes += "<br/>";
			}
			
			switch(test.problemes[i].status) {
			case 1: // Salle occupée par un événement non cours
				test.strProblemes += "Salle occupée par un cours : " + test.problemes[i].message;
				test.afficherBoutonForcer = false;
				test.afficherBoutonRechercheSalle = true;
				break;
				
			case 2: // Salle occupée par un événement non cors
				test.strProblemes += "Salle occupée (pas un cours) : " + test.problemes[i].message;
				test.afficherBoutonRechercheSalle = true;
				break;
			case 3: // Public occupé
				test.strProblemes += "Public occupé : " + test.problemes[i].message;
				break;
			case 4: // Jour bloqué
				test.strProblemes += "Jour ou créneau bloqué : " + test.problemes[i].message;
				break;
			}
		}
	};
	
	DialogRepeter.prototype.show = function(evenement) {
		this.evenement = evenement;
		this.divSynthese.empty();
		
		this.jqBloc.dialog("open");
	};
	
	return DialogRepeter;
});