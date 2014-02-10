/**
 * Module de contrôle de la boîte de dialogue d'ajout/modification d'une période bloquée
 * @module DialogAjoutPeriodeBloquee
 */
define([ "RestManager", "planning_cours/EcranJoursBloques", "MultiWidget", "jquerymaskedinput" ], function(RestManager, EcranJoursBloques, MultiWidget) {

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
		this.jqHeureFin = jqDialog.find("#heure_fin_periode_bloquee");
		this.jqJourLettres = jqDialog.find("#date_jour_bloque");
		this.jqGroupes = jqDialog.find("#groupes_participants_periode_bloquee");
		this.periode = null; // Est rempli dans le cas d'une modification
		this.multiWidgetGroupes = null;
		
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
		var me = this;

		this.verifieSiDimancheOuFerie(date, function() {

			// Affiche le nom du jour en toutes lettres
			me.jqJourLettres.html(me.ecranJoursBloques.calendrierAnnee.dateEnTouteLettres(date));

			// Rempli les champs dans le cas de la modification
			if (me.periode != null) {
				me.jqDialog.dialog({ title: "Modification d'une période bloquée" });
				me.jqLibelle.val(periode.libelle);
				me.jqHeureDebut.val(periode.strHeureDebut);
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
				me.jqDialog.dialog({ title: "Ajout d'une période bloquée" });
			}
			
			// Ouvre la boîte de dialogue
			me.jqDialog.dialog("open");
		});

	};
	

	/**
	 * Affiche la boîte de dialogue
	 * 
	 * @param {long} date Date du jour à éditer
	 * @param {function} callback Méthode appellée si le jour est valide
	 */
	DialogAjoutPeriodeBloquee.prototype.verifieSiDimancheOuFerie = function(date, callback) {
		
		if (date.getDay()==0) {
			confirm("Le jour séléctionné est un dimanche, êtes vous sûr de vouloir ajouter une période bloquée ?", function() {
				callback();
			});
		} else if (!this.ecranJoursBloques.jourBloqueGestion.isFerie(date)) {
			callback();
		}
		
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
			appendTo: "#dialog_hook",
			width: 380,
			modal: true,
			show: { effect: "fade", duration: 200 },
			hide: { effect: "explode", duration: 200 },
			close: function() {
				me.jqLibelle.val("");
				me.jqHeureDebut.val("");
				me.jqHeureFin.val("");
				me.periode = null;
				me.jqDialog.find(".message_alerte").hide();
				me.multiWidgetGroupes.clear();
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

				// Création des objets date
				var year = me.date.getFullYear();
				var month = me.date.getMonth();
				var day = me.date.getDate();
				
				var strHeureDebut = me.jqHeureDebut.val();
				var strHeureFin = me.jqHeureFin.val();
				
				var heureDebut = parseInt(strHeureDebut.substring(0, 2));
				var minutesDebut = parseInt(strHeureDebut.substring(3));
				
				var heureFin = parseInt(strHeureFin.substring(0, 2));
				var minutesFin = parseInt(strHeureFin.substring(3));
				
				var dateDebut = new Date(year, month, day, heureDebut, minutesDebut, 0);
				var dateFin = new Date(year, month, day, heureFin, minutesFin, 0);

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
				me.show(periode, date, callback);

			} else {
				window.showToast("Erreur lors de la récupération des groupes de participants ; vérifiez votre connexion.");
			}
		});

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
		
		// Vérifie qu'il y a un groupe
		if (this.multiWidgetGroupes.val()=="") {
			this.jqDialog.find("#span_alert_choix_groupe_manquant").show();
			correct = false;
		}

		return correct;
	};
	
	return DialogAjoutPeriodeBloquee;

});
