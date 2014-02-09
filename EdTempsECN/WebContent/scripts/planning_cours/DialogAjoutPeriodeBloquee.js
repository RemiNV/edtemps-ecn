/**
 * Module de contrôle de la boîte de dialogue d'ajout/modification d'une période bloquée
 * @module DialogAjoutPeriodeBloquee
 */
define([ "planning_cours/EcranJoursBloques", "jquerymaskedinput" ], function(EcranJoursBloques) {

	/**
	 * @constructor
	 * @alias DialogAjoutPeriodeBloquee
	 */
	var DialogAjoutPeriodeBloquee = function(restManager, jqDialog, ecranJoursBloques) {
		this.restManager = restManager;
		this.ecranJoursBloques = ecranJoursBloques;
		this.jqDialog = jqDialog;
		this.jqLibelle = jqDialog.find("#txt_libelle");
		this.jqHeureDebut = jqDialog.find("#heure_debut_periode_bloquee");
		this.jqHeureFin = jqDialog.find("#heure_debut_fin_bloquee");
		this.jqJourLettres = jqDialog.find("#date_jour_bloque");
		this.periode = null; // Est rempli dans le cas d'une modification
		
		this.initAppele = false;
	};

	
	/**
	 * Affiche la boîte de dialogue
	 * 
	 * @param {object} periode Objet periode qui peut être null dans le cas d'ajout,
	 * 						   sinon, il contient toutes les informations d'un jour bloqué standard : libelle, dateDebut, dateFin
	 * @param {long} date Date du jour à éditer
	 * @param {function} callback Méthode appellée au clic sur Valider
	 */
	DialogAjoutPeriodeBloquee.prototype.show = function(periode, date, callback) {
		if(!this.initAppele) {
			this.init(periode, date, callback);
			return;
		}
		
		// Récupère les paramètres
		this.callback = callback;
		this.periode = periode;
		this.date = date;
		
		// Rempli les champs dans le cas de la modification
		if (this.periode != null) {
			this.jqDialog.dialog({ title: "Modification d'une période bloquée" });
			this.jqLibelle.val(periode.libelle);
			this.jqHeureDebut.val(periode.strHeureDebut);
			this.jqHeureFin.val(periode.strHeureFin);
			this.jqJourLettres.html(this.ecranJoursBloques.calendrierAnnee.dateEnTouteLettres(date));
		} else {
			this.jqDialog.dialog({ title: "Ajout d'une période bloquée" });
			this.jqJourLettres.html(this.ecranJoursBloques.calendrierAnnee.dateEnTouteLettres(date));
		}

		// Ouvre la boîte de dialogue
		this.jqDialog.dialog("open");
	};
	
	
	/**
	 * Initialise la boîte de dialogue
	 * 
	 * @param {object} periode Objet periode qui peut être null dans le cas d'ajout,
	 * 						   sinon, il contient toutes les informations d'un jour bloqué standard : libelle, dateDebut, dateFin
	 * @param {long} date Date du jour à éditer
	 * @param {function} callback Méthode appellée au clic sur Valider
	 */
	DialogAjoutPeriodeBloquee.prototype.init = function(periode, date, callback) {
		var me=this;
		
		// Créer la boîte de dialogue
		this.jqDialog.dialog({
			autoOpen: false,
			width: 360,
			modal: true,
			show: { effect: "fade", duration: 200 },
			hide: { effect: "explode", duration: 200 },
			close: function() {
				me.jqLibelle.val("");
				me.jqHeureDebut.val("");
				me.jqHeureFin.val("");
				me.periode = null;
				me.jqDialog.find(".message_alerte").hide();
			}
		});
		
		// Masque sur les heures de début et fin
		this.jqHeureDebut.mask("99:99");
		this.jqHeureFin.mask("99:99");
		
		// Listener du bouton "Fermer"
		this.jqDialog.find("#btn_annuler_ajout_periode_bloquee").click(function() {
			me.jqDialog.dialog("close");
		});

		// Listener du bouton "Valider"
		this.jqDialog.find("#btn_valider_ajout_periode_bloquee").click(function() {
			if (me.isCorrect()) {
				me.callback(libelle, dateDebut, dateFin);
				me.jqDialog.dialog("close");
			}
		});

		// Retourne à la méthode show()
		this.initAppele = true;
		this.show(periode, date, callback);
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
		
		// Validation de l'heure de début
		var decoupageHeureMinute = this.jqHeureDebut.val().split(":");
		var calculMinutesDebut = 60*decoupageHeureMinute[0] + decoupageHeureMinute[1];
		if (this.jqHeureDebut.val().length==0 || decoupageHeureMinute[0]>23 || isNaN(decoupageHeureMinute[0]) || decoupageHeureMinute[1]>59 || isNaN(decoupageHeureMinute[1])) {
			this.jqDialog.find("#span_alert_heure_debut_incorrect").show();
			correct = false;
		}
		
		// Validation de l'heure de fin
		decoupageHeureMinute = this.jqHeureFin.val().split(":");
		var calculMinutesFin = 60*decoupageHeureMinute[0] + decoupageHeureMinute[1];
		if (this.jqHeureFin.val().length==0 || decoupageHeureMinute[0]>23 || isNaN(decoupageHeureMinute[0]) || decoupageHeureMinute[1]>59 || isNaN(decoupageHeureMinute[1])) {
			this.jqDialog.find("#span_alert_heure_fin_incorrect").show();
			correct = false;
		}
		
		// Validation de la cohérence entre l'heure de début et l'heure de fin
		if (calculMinutesFin-calculMinutesDebut<=0) {
			this.jqDialog.find("#span_alert_heure_fin_incoherent").show();
			correct = false;
		}
		
		return correct;
	};
	
	return DialogAjoutPeriodeBloquee;

});
