/**
 * Module de contrôle de la boîte de dialogue d'ajout/modification d'une période bloquée
 * @module DialogAjoutPeriodeBloquee
 */
define([ "planning_cours/EcranJoursBloques" ], function(EcranJoursBloques) {

	/**
	 * @constructor
	 * @alias DialogAjoutPeriodeBloquee
	 */
	var DialogAjoutPeriodeBloquee = function(restManager, jqDialog, ecranJoursBloques) {
		this.restManager = restManager;
		this.ecranJoursBloques = ecranJoursBloques;
		this.jqDialog = jqDialog;
		this.jqLibelle = jqDialog.find("#txt_libelle");
		this.jqDateDebut = jqDialog.find("#date_debut");
		this.jqDateFin = jqDialog.find("#date_fin");
		this.periode = null; // Est rempli dans le cas d'une modification
		
		this.initAppele = false;
	};

	
	/**
	 * Affiche la boîte de dialogue
	 * 
	 * @param {object} periode Objet periode qui peut être null dans le cas d'ajout.
	 * @param {function} callback Méthode appellée au clic sur Valider
	 * @param {boolean} vacances Vaut vrai si on est dans l'édition de vacances
	 */
	DialogAjoutPeriodeBloquee.prototype.show = function(periode, callback, vacances) {
		if(!this.initAppele) {
			this.init(jour, callback);
			return;
		}
		
		// Récupère les paramètres
		this.callback = callback;
		this.periode = periode;
		this.vacances = vacances;
		
		// Rempli les champs dans le cas de la modification
		if (this.jour != null) {
			this.jqDialog.dialog({ title: "Modification " + (vacances ? "d'une période de vacances" : "d'un jour bloqué") });
			this.jqLibelle.val(periode.libelle);
			this.jqDateDebut.val(periode.dateDebutString);
			this.jqDateFin.val(periode.dateFinString);
		} else {
			this.jqDialog.dialog({ title: "Ajout " + (vacances ? "d'une période de vacances" : "d'un jour bloqué") });
		}

		// Ouvre la boîte de dialogue
		this.jqDialog.dialog("open");
	};
	
	
	/**
	 * Initialise la boîte de dialogue
	 * 
	 * @param {object} jour Objet jour qui peut être null dans le cas d'ajout
	 * @param {function} callback Méthode appellée au clic sur Valider
	 */
	DialogAjoutPeriodeBloquee.prototype.init = function(jour, callback) {
		var me=this;
		
		// Créer la boîte de dialogue
		this.jqDialog.dialog({
			autoOpen: false,
			width: 360,
			modal: true,
			draggable: false,
			show: { effect: "fade", duration: 200 },
			hide: { effect: "explode", duration: 200 },
			close: function() {
				me.jqLibelle.val("");
				me.jqDateDebut.val("");
				me.jqDateFin.val("");
				me.periode = null;
				me.jqDialog.find(".message_alerte").hide();
			}
		});
		
        // Ajout du datepicker sur le champ date début
        var optionsDatepicker = {
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
        this.jqDateDebut.datepicker(optionsDatepicker);
        this.jqDateFin.datepicker(optionsDatepicker);
		
		// Listener du bouton "Fermer"
		this.jqDialog.find("#btn_annuler_ajout_jour_bloque").click(function() {
			me.jqDialog.dialog("close");
		});

		// Listener du bouton "Valider"
		this.jqDialog.find("#btn_valider_ajout_jour_ferie").click(function() {
			
			var date = $.datepicker.parseDate("dd/mm/yy", me.jqDate.val());
			var debutAnnneeScolaire = new Date(me.ecranJoursBloques.calendrierAnnee.getAnnee(), 8, 1);
			var finAnnneeScolaire = new Date(me.ecranJoursBloques.calendrierAnnee.getAnnee()+1, 7, 31);
			
			// Indiquer à l'utilisateur qu'il essaye de rentrer un jour férié pour une autre année que celle en cours...
			if (date.getTime() < debutAnnneeScolaire.getTime() || date.getTime() > finAnnneeScolaire.getTime()) {
				confirm("Etes vous sûr de vouloir ajouter un jour férié pour une autre année scolaire que celle en cours de modification ?", function () { me.valider(date); });
			} else {
				me.valider(date);
			}

		});

		// Retourne à la méthode show()
		this.initAppele = true;
		this.show(jour, callback);
	};
	


	/**
	 * Valider le formulaire (exécute la méthode de callback)
	 * 
	 * @param {date} date La date formatée
	 */
	DialogAjoutPeriodeBloquee.prototype.valider = function(date) {
		
		// Continue si tout va bien
		if (this.isCorrect()) {
			var id = this.jour==null ? null : this.jour.id;
			this.callback(this.jqLibelle.val(), date, id);
			this.jqDialog.dialog("close");
		}
		
	};

	
	/**
	 * Vérifie que les champs saisis sont corrects et retourne vrai ou faux
	 */
	DialogAjoutPeriodeBloquee.prototype.isCorrect = function() {
		var correct = true;

		// Cache tous les messages d'alerte
		this.jqDialog.find(".message_alerte").hide();
		
		// Vérifie le champ du libellé
		if (this.jqLibelle.val()=="") {
			this.jqDialog.find("#span_alert_libelle_absent").show();
			correct = false;
		} else if (!/^['a-z \u00C0-\u00FF0-9]+$/i.test(this.jqLibelle.val())) {
			this.jqDialog.find("#span_alert_libelle_alphanumerique").show();
			correct = false;
		}
		
		// Vérifie le champ date
		if (this.jqDate.val()=="") {
			this.jqDialog.find("#span_alert_date_absent").show();
			correct = false;
		}
		
		return correct;
	};
	
	return DialogAjoutPeriodeBloquee;

});
