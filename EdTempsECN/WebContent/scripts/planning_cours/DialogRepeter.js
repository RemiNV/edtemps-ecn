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
	var DialogRepeter = function(restManager, jqBloc, rechercheSalle) {
		var me = this;
		this.restManager = restManager;
		this.jqBloc = jqBloc;
		this.evenement = null; // Evénement en cours d'édition
		this.calendrier = null; // Calendrier en cours d'édition
		this.synthese = null;
		this.rechercheSalle = rechercheSalle;
		
		var contenuDialog = $(dialogRepeterEvenementTpl);
		this.divSynthese = contenuDialog.find("#div_synthese");
		this.templateSynthese = _.template(this.divSynthese.attr("data-template"));
		this.divSynthese.removeAttr("data-template");
		
		jqBloc.append(contenuDialog).dialog({
			autoOpen: false,
			appendTo: "#dialog_hook",
			width: 700
		});
		
		// Listeners
		jqBloc.find("#btn_previsualiser").click(function(e) {
			me.lancerPrevisualisation();
			jqBloc.find("#btn_executer").attr("disabled", "disabled");
		});
		
		jqBloc.find("#btn_annuler").click(function(e) {
			jqBloc.dialog("close");
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
		
		this.afficherChargement("Calcul des répétitions...");
		
		this.restManager.effectuerRequete("GET", "repeterevenement/previsualiser", {
			token: this.restManager.getToken(),
			idEvenement: this.evenement.id,
			nbRepetitions: nbRepetitions,
			periode: periode
		}, function(response) {
			me.cacherChargement();
			if(response.resultCode == RestManager.resultCode_Success) {
				me.synthese = response.data;
				for(var i=0; i<me.synthese.length; i++) {
					me.synthese[i].id = i;
					me.parseTest(me.synthese[i]);
				}
				
				me.updateSynthese();
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
	 * Affichage d'un message de chargement dans la zone dédiée de la dialog
	 * @param {string} message Message à afficher
	 */
	DialogRepeter.prototype.afficherChargement = function(message) {
		this.jqBloc.find("#dialog_repeter_chargement").show().find("#dialog_repeter_message_chargement").text(message);
	};
	
	/**
	 * Masquage du message de chargement affiché
	 */
	DialogRepeter.prototype.cacherChargement = function() {
		this.jqBloc.find("#dialog_repeter_chargement").hide();
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
	 * Callback appelé lors du clic sur un bouton "Rech. salle"
	 * @param jqButton
	 */
	DialogRepeter.prototype.callbackRechercheSalle = function(jqButton) {
		var me = this;
		var id = parseInt(jqButton.attr("data-id"));
		var synthese = this.synthese[id];
		
		this.rechercheSalle.show(null, null, function(data) {
			me.rechercheSalle.hide();
			
			synthese.nouvellesSalles = data;
			var nomsSalles = new Array();
			for(var i=0,max=data.length; i<max; i++) {
				nomsSalles.push(data[i].nom);
			}
			synthese.strNouvellesSalles = nomsSalles.join(", ");
			
			me.updateSynthese();
		}, synthese.dateDebut, synthese.dateFin, this.calendrier.estCours);
	};
	
	/**
	 * Mise à jour du tableau de synthèse
	 */
	DialogRepeter.prototype.updateSynthese = function() {
		// Remplissage du template
		this.divSynthese.empty().append(this.templateSynthese({ synthese: this.synthese }));
		
		// Ajout des listeners
		var me = this;
		this.divSynthese.find(".btn_forcer_ajout").click(function(e) {
			me.callbackForcerAjout($(this));
		});
		this.divSynthese.find(".btn_rechercher_salle").click(function(e) {
			me.callbackRechercheSalle($(this));
		});
		
		// Activation du bouton "Exécuter" si plus de problèmes
		var probleme = false;
		for(var i=0,max=this.synthese.length; i<max; i++) {
			if(this.synthese[i].resteProblemes) {
				probleme = true;
				break;
			}
		}
		
		if(probleme) {
			this.find("#btn_executer").removeAttr("disabled");
		}
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
		test.dateDebut = new Date(test.debut);
		test.dateFin = new Date(test.fin);
		test.nouvellesSalles = null;
		test.strNouvellesSalles = null;
		test.resteProblemes = (test.problemes.length > 0);
		test.strDate = $.fullCalendar.formatDate(test.dateDebut, "dd/MM/yyyy");
		test.strProblemes = test.problemes.length > 0 ? "" : "OK";
		test.afficherBoutonForcer = false;
		test.afficherBoutonRechercheSalle = false;
		for(var i=0,max=test.problemes.length; i<max; i++) {
			if(test.strProblemes) {
				test.strProblemes += "<br/>";
			}
			
			switch(test.problemes[i].status) {
			case 1: // Salle occupée par un événement non cours
				test.strProblemes += "Salle occupée (cours) : " + test.problemes[i].message;
				test.afficherBoutonRechercheSalle = true;
				break;
				
			case 2: // Salle occupée par un événement non cors
				test.strProblemes += "Salle occupée (pas un cours) : " + test.problemes[i].message;
				test.afficherBoutonRechercheSalle = true;
				break;
			case 3: // Public occupé
				test.afficherBoutonForcer = true;
				test.strProblemes += "Public occupé : " + test.problemes[i].message;
				break;
			case 4: // Jour bloqué
				test.afficherBoutonForcer = true;
				test.strProblemes += "Jour ou créneau bloqué : " + test.problemes[i].message;
				break;
			}
		}
	};
	
	/**
	 * Définit le calendrier à utiliser pour la création d'événement et la recherche de salle.
	 */
	DialogRepeter.prototype.setCalendrier = function(calendrier) {
		this.calendrier = calendrier;
	};
	
	/**
	 * Affichage de la boîte de dialogue. setCalendrier doit avoir été appelé précédemment.
	 * @param evenement L'événement à répéter. Doit appartenir au calendrier défini par setCalendrier
	 */
	DialogRepeter.prototype.show = function(evenement) {
		this.evenement = evenement;
		this.divSynthese.empty();
		
		if(this.calendrier == null) {
			throw "Demande de répétition d'événement sans préciser de calendrier";
		}
		
		if(evenement.calendriers.length > 1) {
			this.divSynthese.find("#msg_repetition_plusieurs_calendriers")
				.text("L'événement d'origne est rattaché à plusieurs calendriers, mais ne sera dupliqué que dans " + this.calendrier.nom)
				.show();
		}
		else {
			this.divSynthese.find("#msg_repetition_plusieurs_calendriers").hide();
		}
		
		this.jqBloc.find("#btn_executer").attr("disabled", "disabled");
		
		this.jqBloc.dialog("open");
	};
	
	return DialogRepeter;
});