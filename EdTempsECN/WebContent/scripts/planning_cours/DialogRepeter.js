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
		this.synthese = null;
		
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

	/**
	 * Effectue la requête de prévisualisation de la répétition et affiche le résultat
	 */
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
				me.synthese = response.data;
				for(var i=0; i<me.synthese.length; i++) {
					me.synthese[i].id = i;
					me.parseTest(me.synthese[i]);
				}
				
				me.updateSynthese();
				
				// Ajout des listeners
				me.divSynthese.find(".btn_forcer_ajout").click(function(e) {
					me.callbackForcerAjout($(this));
				});
			}
			else if(response.resultCode == RestManager.resultCode_NetworkError) {
				window.showToast("Erreur de récupération de la prévisualisation ; vérifiez votre connexion");
			}
			else {
				window.showToast("Erreur de récupération de la prévisualisation");
			}
		});
	};
	
	/**
	 * Callback appelé lors du clic sur un bouton "forcer l'ajout"
	 * @param jqButton Le bouton cliqué
	 */
	DialogRepeter.prototype.callbackForcerAjout = function(jqButton) {
		var id = parseInt(jqButton.attr("data-id"));
		this.synthese[id].forcerAjout = true;
		this.updateProblemes(this.synthese[id]);
		
		if(!this.synthese[id].resteProblemes) { // L'événement précédemment invalide sera ajouté
			// Suppression derniers tests inutiles et renumérotation
			var numMax = this.synthese[this.synthese.length-1].num;
			for(var i=this.synthese.length-1; i>id && this.synthese[i].num == numMax; i--) {
				this.synthese.pop();
			}
			
			for(i=id+1,max=this.synthese.length; i<max; i++) {
				this.synthese[i].num++;
			}
		}
		
		this.updateSynthese();
	};
	
	/**
	 * Mise à jour du tableau de synthèse
	 */
	DialogRepeter.prototype.updateSynthese = function() {
		// Remplissage du template
		this.divSynthese.empty().append(this.templateSynthese({ synthese: this.synthese }));
	};
	
	/**
	 * Mise à jour de test.resteProblemes en prenant en compte test.forcerAjout et test.nouvellesSalles
	 * @param test
	 */
	DialogRepeter.prototype.updateProblemes = function(test) {
		test.resteProblemes = false;
		for(var i=0,max=test.problemes.length; i<max; i++) {
			switch(test.problemes[i].status) {
			case 1:
			case 2:
				if(!test.nouvellesSalles) {
					test.resteProblemes = true;
				}
				break;
				
			case 3:
			case 4:
				if(!test.forcerAjout) {
					test.resteProblemes = true;
				}
				break;
			}
		}
	};
	
	/**
	 * Parsing d'une entrée de prévisualisation de la répétition. Ajoute les attributs nécessaires.
	 * @param test L'entrée à parser
	 */
	DialogRepeter.prototype.parseTest = function(test) {
		test.forcerAjout = false;
		test.nouvellesSalles = null;
		test.resteProblemes = (test.problemes.length > 0);
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
	
	/**
	 * Affichage de la boîte de dialogue
	 * @param evenement L'événement à répéter
	 */
	DialogRepeter.prototype.show = function(evenement) {
		this.evenement = evenement;
		this.divSynthese.empty();
		
		this.jqBloc.dialog("open");
	};
	
	return DialogRepeter;
});