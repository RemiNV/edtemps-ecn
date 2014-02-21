/**
 * Module de contrôle de la boîte de dialogue d'ajout/modification de vacances
 * @module DialogAjoutVacances
 */
define([ "RestManager", "planning_cours/EcranJoursBloques", "MultiWidget", "jquerymaskedinput" ], function(RestManager, EcranJoursBloques, MultiWidget) {

	/**
	 * @constructor
	 * @alias DialogAjoutVacances
	 */
	var DialogAjoutVacances = function(restManager, jqDialog, ecranJoursBloques) {
		this.restManager = restManager;
		this.ecranJoursBloques = ecranJoursBloques;
		this.jqDialog = jqDialog;
		this.jqLibelle = jqDialog.find("#txt_libelle");
		this.jqDateDebut = jqDialog.find("#date_debut_vacances");
		this.jqHeureDebut = jqDialog.find("#heure_debut_vacances");
		this.jqDateFin = jqDialog.find("#date_fin_vacances");
		this.jqHeureFin = jqDialog.find("#heure_fin_vacances");
		this.jqGroupes = jqDialog.find("#groupes_participants_vacances");
		this.periode = null; // Est rempli dans le cas d'une modification
		this.multiWidgetGroupes = null;
		
		this.initAppele = false;
	};

	
	/**
	 * Affiche la boîte de dialogue
	 * 
	 * @param {object} periode Objet periode qui peut être null dans le cas d'ajout,
	 * 						   sinon, il contient toutes les informations d'une période de vacances
	 * @param {function} callback Méthode appellée au clic sur Valider
	 */
	DialogAjoutVacances.prototype.show = function(periode, callback) {
		if(!this.initAppele) {
			this.init(periode, callback);
			return;
		}
		
		// Récupère les paramètres
		this.callback = callback;
		this.periode = periode;
		var me = this;

		// Rempli les champs dans le cas de la modification
		if (me.periode != null) {
			me.jqDialog.dialog({ title: "Modifier période de vacances" });
			me.jqDialog.find("#btn_valider_ajout_vacances").val("Modifier");
			me.jqLibelle.val(periode.libelle);
			me.jqDateDebut.val(periode.strDateDebut);
			me.jqHeureDebut.val(periode.strHeureDebut);
			me.jqDateFin.val(periode.strDateFin);
			me.jqHeureFin.val(periode.strHeureFin);

			// Affiche les groupes de participants avec le multiwidget
			var liste = new Array();
			for (var i=0, maxI=periode.listeGroupes.length; i<maxI; i++) {
				liste.push({
						label: periode.listeGroupes[i].nom,
						value: periode.listeGroupes[i].id
				});
			}
			me.multiWidgetGroupes.setValues(liste);
			
		} else {
			me.jqDialog.dialog({ title: "Ajouter période de vacances" });
			me.jqDialog.find("#btn_valider_ajout_vacances").val("Ajouter");
		}
		
		// Ouvre la boîte de dialogue
		me.jqDialog.dialog("open");

	};
	
	
	/**
	 * Initialise la boîte de dialogue
	 * 
	 * @param {object} periode Objet periode qui peut être null dans le cas d'ajout,
	 * 						   sinon, il contient toutes les informations d'une période de vacances
	 * @param {function} callback Méthode appellée au clic sur Valider
	 */
	DialogAjoutVacances.prototype.init = function(periode, callback) {
		var me=this;
		
		// Créer la boîte de dialogue
		this.jqDialog.dialog({
			autoOpen: false,
			appendTo: "#dialog_hook",
			width: 420,
			modal: true,
			show: { effect: "fade", duration: 200 },
			hide: { effect: "explode", duration: 200 },
			close: function() {
				me.jqLibelle.val("");
				me.jqDateDebut.val("");
				me.jqHeureDebut.val("");
				me.jqDateFin.val("");
				me.jqHeureFin.val("");
				me.periode = null;
				me.jqDialog.find(".message_alerte").hide();
				me.multiWidgetGroupes.clear();
				me.dateDebut = null;
				me.dateFin = null;
			}
		});

        // Ajout du datepicker sur les champs date de début et de fin
		var options = {
            showAnim : 'slideDown',
            showOn: 'both',
            buttonText: "Calendrier",
            dateFormat: "dd/mm/yy",
            buttonImage: "img/datepicker.png", // image pour le bouton d'affichage du calendrier
            buttonImageOnly: true, // affiche l'image sans bouton
            monthNamesShort: [ "Jan", "Fév", "Mar", "Avr", "Mai", "Jui", "Jui", "Aou", "Sep", "Oct", "Nov", "Dec" ],
            monthNames: [ "Janvier", "Février", "Mars", "Avril", "Mai", "Juin", "Juillet", "Août", "Septembre", "Octobre", "Novembre", "Décembre" ],
            dayNamesMin: [ "Di", "Lu", "Ma", "Me", "Je", "Ve", "Sa" ],
            dayNames: [ "Dimanche", "Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi" ],
            gotoCurrent: true,
            prevText: "Précédent",
            nextText: "Suivant",
            constrainInput: true,
            firstDay: 1
        };
		this.jqDateDebut.datepicker(options);        
		this.jqDateFin.datepicker(options);        
        
		// Masque sur les heures de début et fin
		this.jqHeureDebut.mask("99:99");
		this.jqHeureFin.mask("99:99");
		
		// Listener du bouton "Annuler"
		this.jqDialog.find("#btn_annuler_ajout_vacances").click(function() {
			me.jqDialog.dialog("close");
		});

		// Listener du bouton "Valider"
		this.jqDialog.find("#btn_valider_ajout_vacances").click(function() {
			
			if (me.isCorrect()) {

				var strDateDebut = me.jqDateDebut.val();
				var strHeureDebut = me.jqHeureDebut.val();
				var strDateFin = me.jqDateFin.val();
				var strHeureFin = me.jqHeureFin.val();
				
				var jourDebut = parseInt(strDateDebut.substring(0, 2));
				var moisDebut = parseInt(strDateDebut.substring(3, 5))-1;
				var anneeDebut = parseInt(strDateDebut.substring(6, 10));
				var heureDebut = parseInt(strHeureDebut.substring(0, 2));
				var minutesDebut = parseInt(strHeureDebut.substring(3));
				var dateDebut = new Date(anneeDebut, moisDebut, jourDebut, heureDebut, minutesDebut, 0);
				
				var jourFin = parseInt(strDateFin.substring(0, 2));
				var moisFin = parseInt(strDateFin.substring(3, 5))-1;
				var anneeFin = parseInt(strDateFin.substring(6, 10));
				var heureFin = parseInt(strHeureFin.substring(0, 2));
				var minutesFin = parseInt(strHeureFin.substring(3));
				var dateFin = new Date(anneeFin, moisFin, jourFin, heureFin, minutesFin, 0);
				
				// Vérifie la cohérence des dates
				if (dateDebut.getTime()>dateFin.getTime()) {
					alert("Les dates sont incohérentes. Veuillez les modifier.");
					return;
				}
				
				// Appelle la méthode de callback
				me.callback(me.jqLibelle.val(), dateDebut, dateFin, me.multiWidgetGroupes.val());

				// Ferme la boite de dialogue
				me.jqDialog.dialog("close");
			}
		});

		// Récupère la liste des groupes de participants
		this.restManager.effectuerRequete("GET", "groupeparticipants/lister", {
			token: this.restManager.getToken()
		}, function(data) {
			if(data.resultCode == RestManager.resultCode_Success) {
			
				// Préparation du tableau de données pour le multiwidget
				var liste = new Array();
				for (var i=0, maxI=data.data.length; i<maxI; i++) {
					liste.push({
							label: data.data[i].nom,
							value: data.data[i].id
					});
				}
				
				// Met en place le multi widget pour le choix des groupes de participants
				me.multiWidgetGroupes = new MultiWidget(me.jqGroupes, 
						MultiWidget.AUTOCOMPLETE_OPTIONS(liste, 3, 240));

				// Retourne à la méthode show()
				me.initAppele = true;
				me.show(periode, callback);

			} else {
				window.showToast("Erreur lors de la récupération des groupes de participants ; vérifiez votre connexion.");
			}
		});

	};

	
	/**
	 * Vérifie que les champs saisis sont corrects et retourne vrai ou faux
	 */
	DialogAjoutVacances.prototype.isCorrect = function() {
		var correct = true;

		// Cache tous les messages d'alerte
		this.jqDialog.find(".message_alerte").hide();
		
		// Vérifie le champ du libellé
		if (this.jqLibelle.val()=="") {
			this.jqDialog.find("#span_alert_libelle_absent").show();
			correct = false;
		} else if (!/^['a-z \u00C0-\u00FF0-9]+$/i.test(this.jqLibelle.val())) {
			this.jqDialog.find("#span_alert_libelle_non_alphanumerique").show();
			correct = false;
		}
		
		// Validation de la date de début
		var decoupageHeureMinute = this.jqHeureDebut.val().split(":");
		if (this.jqDateDebut.val().length==0 || this.jqHeureDebut.val().length==0) {
			this.jqDialog.find("#span_alert_date_debut_absente").show();
			correct = false;
		} else if (decoupageHeureMinute[0]>23 || isNaN(decoupageHeureMinute[0]) || decoupageHeureMinute[1]>59 || isNaN(decoupageHeureMinute[1])) {
			this.jqDialog.find("#span_alert_heure_debut_incorrecte").show();
			correct = false;
		}
		
		// Validation de la date de fin
		decoupageHeureMinute = this.jqHeureFin.val().split(":");
		if (this.jqDateFin.val().length==0 || this.jqHeureFin.val().length==0) {
			this.jqDialog.find("#span_alert_date_fin_absente").show();
			correct = false;
		} else if (decoupageHeureMinute[0]>23 || isNaN(decoupageHeureMinute[0]) || decoupageHeureMinute[1]>59 || isNaN(decoupageHeureMinute[1])) {
			this.jqDialog.find("#span_alert_heure_fin_incorrecte").show();
			correct = false;
		}
		
		// Vérifie qu'il y a un groupe
		if (this.multiWidgetGroupes.val()=="") {
			this.jqDialog.find("#span_alert_choix_groupe_manquant").show();
			correct = false;
		}

		return correct;
	};
	
	
	return DialogAjoutVacances;

});
