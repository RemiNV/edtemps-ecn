
/**
 * Module de contrôle de la boîte de dialogue d'ajout d'évènement
 * @module AjoutEvenement
 */
define(["CalendrierGestion", "RestManager", "jquery", "jqueryui", "jquerymaskedinput", "lib/fullcalendar.translated.min"], function(CalendrierGestion, RestManager) {
	
	/**
	 * Création d'un module d'ajout d'évènements (contrôle la boîte de dialogue associée)
	 * @param {module:RestManager} restManager Référence au restManager instancié
	 * @param {jQuery} jqDialog Objet jQuery du DOM dans lequel afficher la dialog
	 * @param {module:RechercheSalle} rechercheSalle Référence à l'objet rechercheSalle instancié
	 * @param {module:EvenementGestion} evenementGestion Référence à l'objet evenementGestion instancié
	 * @param {function} callbackRafraichirCalendrier Fonction à appeler pour déclencher le rafraîchissement du calendrier après ajout d'un évènement
	 * 
	 * @constructor
	 * @alias module:AjoutEvenement
	 */
	var AjoutEvenement = function(restManager, jqDialog, rechercheSalle, evenementGestion, callbackRafraichirCalendrier) {
		
		this.jqDialog = jqDialog;
		this.restManager = restManager;
		this.rechercheSalle = rechercheSalle;
		this.evenementGestion = evenementGestion;
		this.callbackRafraichirCalendrier = callbackRafraichirCalendrier;
		this.strOptionsCalendriers = null; // HTML à ajouter au select pour sélectionner les calendriers
		this.sallesSelectionnees = new Array();
		this.initAppele = false;
		
		// Initialisation de la dialog
		jqDialog.dialog({
			autoOpen: false,
			modal: true,
			width: 670,
			show: { effect: "fade", duration: 200 },
			hide: { effect: "explode", duration: 200 }
		});
		
		// Initialisation des champs
		jqDialog.find("#heure_debut").mask("99:99");
		jqDialog.find("#heure_fin").mask("99:99");
		jqDialog.find("#nb_personnes_evenement").mask("9?99", { placeholder: "" });
		
		// Ajout du datepicker sur le champ date
        jqDialog.find("#date_evenement").mask("99/99/9999").datepicker({
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
        });
		
		// Listeners
		var me = this;
		jqDialog.find("#btn_rechercher_salle_evenement").click(function() {
			me.lancerRechercheSalle();
		});
		
		jqDialog.find("#btn_valider_ajout_evenement").click(function() {
			me.validationDialog();
		});
		
		
	};
	
	AjoutEvenement.prototype.setSalles = function(salles) {
		this.sallesSelectionnees = salles;
		
		// Affichage des salles dans la zone de texte
		var strSalles;
		if(salles.length > 0) {
			strSalles = "";
			for(var i=0, maxI = salles.length; i<maxI; i++) {
				if(i != 0) {
					strSalles += ", ";
				}
				
				strSalles += salles[i].nom;
			}
		}
		else {
			strSalles = "(Recherchez une ou plusieurs salle(s))";
		}
		
		this.jqDialog.find("#salles_evenement").html(strSalles);
	};
	
	AjoutEvenement.prototype.lancerRechercheSalle = function() {
		
		var formData = this.getDonneesFormulaire(true);
		
		if(formData.valideRechercheSalle) {
			
			// Affichage message de chargement
			this.jqDialog.find("#btn_rechercher_salle_evenement").attr("disabled", "disabled");
			this.jqDialog.find("#dialog_ajout_evenement_chargement").css("display", "block");
			this.jqDialog.find("#dialog_ajout_evenement_message_chargement").html("Recherche des salles disponibles...");
			
			// Récupération des effectifs
			var me = this;
			var effectif = this.jqDialog.find("#nb_personnes_evenement").val();
			if(!effectif) {
				effectif = 0;
			}
			
			this.rechercheSalle.getSalle(formData.dateDebut, formData.dateFin, effectif, formData.materiels, true, function(succes) {
				if(succes) {
					me.jqDialog.find("#btn_rechercher_salle_evenement").removeAttr("disabled");
					me.jqDialog.find("#dialog_ajout_evenement_chargement").css("display", "none");
				}
			}, function(salles) {
				me.setSalles(salles);
			});
		}
		else {
			window.showToast("Remplissez les champs indiqués pour lancer la recherche");
		}
		
	};
	
	AjoutEvenement.prototype.validationDialog = function() {
		// Récupération des données du formulaire
		var formData = this.getDonneesFormulaire(false);
		var me = this;
		
		if(formData.valide) {
			
			// Message d'attente
			this.jqDialog.find("#dialog_ajout_evenement_chargement").css("display", "block");
			this.jqDialog.find("#dialog_ajout_evenement_message_chargement").html("Ajout de l'évènement...");
			
			this.evenementGestion.ajouterEvenement(formData.nom, formData.dateDebut, formData.dateFin, formData.calendriers, formData.salles, 
					formData.intervenants, formData.responsables, formData.idEvenementsSallesALiberer,
					function(resultCode) {
				
				// Masquage du message d'attente
				me.jqDialog.find("#dialog_ajout_evenement_chargement").css("display", "none");
				
				if(resultCode === RestManager.resultCode_Success) {
					window.showToast("Evènement ajouté avec succès");
					me.jqDialog.dialog("close");
					
					// Mise à jour du calendrier
					me.callbackRafraichirCalendrier();
					
				}
				else if(resultCode == RestManager.resultCode_NetworkError) {
					window.showToast("Erreur d'enregistrement de l'événement ; vérifiez votre connexion");
				}
				else if(resultCode == RestManager.resultCode_SalleOccupee) {
					window.showToast("Erreur d'ajout de l'événement : salle(s) occupée(s) (événement créé entre-temps ?)");
				}
				else {
					window.showToast("Erreur d'enregistrement de l'événement ; code retour " + resultCode);
				}
			});
		}
	};
	
	/**
	 * Vérifie qu'un contrôle est non vide, et le marque comme valide
	 * si c'est le cas, sinon le marque comme non valide
	 * @return valeur du champ du contrôle
	 */
	function validateNotEmpty(jqControl) {
		var val = jqControl.val(); 
		if(val == "") {
			jqControl.addClass("invalide").removeClass("valide");
		}
		else {
			jqControl.addClass("valide").removeClass("invalide");
		}
		
		return val;
	}
	
	/**
	 * Données rentrées dans le formulaire d'ajout d'évènement
	 * @typedef {Object} DonneesFormulaireAjoutEvenement
	 * @property {boolean} valide - true si le formulaire est complet
	 * @property {boolean} valideRechercheSalle - true si les informations nécessaires à la recherche d'une salle sont présentes
	 * @property {string} nom - Nom de l'évènement
	 * @property {number[]} responsables - tableau d'ID des responsables
	 * @property {number[]} intervenants - tableau d'ID des intervenants
	 * @property {number[]} calendriers - tableau d'ID des calendriers
	 * @property {Date} dateDebut - Date de début de l'évènement
	 * @property {Date} dateFin - Date de fin de l'évènement
	 * @property {Materiel[]} materiels - Matériels sélectionnés pour l'évènement
	 * @property {number[]} salles - Tableau d'IDs des salles sélectionnées
	 * @property {number[]} idEvenementsSallesALiberer - Tableau d'IDs des évènements dont les salles sont à libérer
	 */
	
	/**
	 * Vérifie les données du formulaire et les renvoie
	 * 
	 * @param {boolean} pourRechercheSalle Signaler les champs non remplis nécessaires pour rechercher une salle uniquement
	 * @return {DonneesFormulaireAjoutEvenement} Données du formulaire
	 */
	AjoutEvenement.prototype.getDonneesFormulaire = function(pourRechercheSalle) {
		var res = {
			valide: false,
			valideRechercheSalle: false
		};
		
		var jqNom = this.jqDialog.find("#txt_nom_evenement");
		var jqDate = this.jqDialog.find("#date_evenement");
		var jqHeureDebut = this.jqDialog.find("#heure_debut");
		var jqHeureFin = this.jqDialog.find("#heure_fin");
		
		if(pourRechercheSalle) {
			res.nom = jqNom.val();
		}
		else {
			// Validation du nom
			res.nom = validateNotEmpty(jqNom);
		}
		
		// TODO : Récupération de la liste des responsables à implémenter
		res.responsables = new Array();
		res.responsables.push(this.restManager.getUserId());
		
		// TODO : Récupération des intervenants à implémenter
		res.intervenants = new Array();
		
		// Récupération de la liste des calendriers
		res.calendriers = new Array();
		
		this.jqDialog.find("#calendriers_evenement .select_calendriers").each(function() {
			res.calendriers.push(parseInt($(this).val()));
		});
		
		// Validation du jour
		var jour = false;
		var year = null;
		var month = null;
		var day = null;
		
		try {
			jour = $.datepicker.parseDate("dd/mm/yy", jqDate.val());
			
			year = jour.getFullYear();
			month = jour.getMonth();
			day = jour.getDate();
			
			if(jqDate.val() != "") {
				jqDate.addClass("valide").removeClass("invalide");
			}
		}
		catch(parseError) {
			// Rien ici : date sera à false en cas d'erreur de parsing de la date
		}
		
		if(!jour) {
			jqDate.addClass("invalide").removeClass("valide");
		}
		
		// Validation des dates de début et fin
		var strHeureDebut = jqHeureDebut.val();
		var strHeureFin = jqHeureFin.val();
		
		var heureDebut = parseInt(strHeureDebut.substring(0, 2));
		var minutesDebut = parseInt(strHeureDebut.substring(3));
		
		var heureFin = parseInt(strHeureFin.substring(0, 2));
		var minutesFin = parseInt(strHeureFin.substring(3));
		
		
		if(strHeureDebut !== "" && heureDebut < 24 && minutesDebut < 60) {
			jqHeureDebut.addClass("valide").removeClass("invalide");
			
			if(jour) {
				res.dateDebut = new Date(year, month, day, heureDebut, minutesDebut, 0);
			}
		}
		else {
			res.dateDebut = null;
			jqHeureDebut.addClass("invalide").removeClass("valide");
		}
		
		if(strHeureFin !== "" && heureFin < 24 && minutesFin < 60) {
			
			if(jour) {
				res.dateFin = new Date(year, month, day, heureFin, minutesFin, 0);
			}
			jqHeureFin.addClass("valide").removeClass("invalide");
		}
		else {
			res.dateFin = null;
			jqHeureFin.addClass("invalide").removeClass("valide");
		}
		
		// Récupération des matériels et salles
		res.materiels = this.rechercheSalle.getContenuListeMateriel(this.jqDialog.find("#tbl_materiel"));
		
		res.salles = new Array();
		for(var i=0, max=this.sallesSelectionnees.length; i<max; i++) {
			res.salles.push(this.sallesSelectionnees[i].id);
		}
		
		if(!pourRechercheSalle) {
			if(res.salles.length == 0) {
				this.jqDialog.find("#btn_rechercher_salle_evenement").addClass("invalide").removeClass("valide");
			}
			else {
				this.jqDialog.find("#btn_rechercher_salle_evenement").addClass("valide").removeClass("invalide");
			}
		}
		
		// Remplissage de res.valide et res.valideRechercheSalle
		if(res.dateDebut != null && res.dateFin != null) {
			res.valideRechercheSalle = true;
			
			if(res.nom !== "" && res.calendriers.length > 0 && res.salles.length > 0) {
				res.valide = true;
			}
		}
		
		// Remplissage de res.idEvenementsSallesALiberer
		res.idEvenementsSallesALiberer = new Array();
		
		for(var i=0, maxI=this.sallesSelectionnees.length; i<maxI; i++) {
			if(this.sallesSelectionnees[i].evenementsEnCours != null) {
				for(var j=0, maxJ=this.sallesSelectionnees[i].evenementsEnCours.length; j<maxJ; j++) {
					res.idEvenementsSallesALiberer.push(this.sallesSelectionnees[i].evenementsEnCours[j].id);
				}
			}
		}
		
		return res;
	};
	
	/**
	 * Fonction rappelée une fois les calendriers remplis dans remplirCalendrier
	 * @typedef {function} CallbackRemplirCalendriers
	 * @param {boolean} Succès de l'opération
	 * @param {number} Nombre de calendriers trouvés
	 */
	
	/**
	 * Remplissage de la sélection des calendriers dans la boîte de dialogue d'ajout d'évènement
	 * @param {CallbackRemplirCalendriers} callback Fonction rappelée une fois les calendriers remplis
	 */
	AjoutEvenement.prototype.remplirCalendriers = function(callback) {
		var calendrierGestion = new CalendrierGestion(this.restManager);
		
		var me = this;
		
		calendrierGestion.listerMesCalendriers(function(resultCode, data) {
			if(resultCode === RestManager.resultCode_Success) {
				
				if(data.length > 0) {
					var strRemplissageSelect = "";
						
					for(var i=0, max=data.length; i<max; i++) {
						strRemplissageSelect += "<option value='" + data[i].id + "'>" + data[i].nom + "</option>\n";
					}
					
					me.strOptionsCalendriers = strRemplissageSelect;
					me.jqDialog.find("#calendriers_evenement .select_calendriers").html(strRemplissageSelect);
				}
				
				callback(true, data.length);
			}
			else if(resultCode === RestManager.resultCode_NetworkError) {
				window.showToast("Erreur de récupération de vos calendriers ; vérifiez votre connexion");
				callback(false);
			}
			else {
				window.showToast("Erreur de récupération de vos calendriers");
				callback(false);
			}
		});
	};
	
	/**
	 * Salles utilisées pour préremplir la dialog d'ajout d'évènement
	 * @typedef {Object} SalleRemplissageAjoutEvenement
	 * @property {number} id - ID de la salle
	 * @property {string} nom - Nom de la salle
	 * @property {Evenement[]} evenementsEnCours - Evènements prévus dans la salle au moment de la recherche
	 */
	
	/**
	 * Affichage de la boîte de dialogue d'ajout d'évènement.
	 * Les paramètres de pré-remplissage peuvent être null pour ne rien préremplir
	 * 
	 * @param {Date} dateDebut date de début à pré-remplir ; objet Date JavaScript
	 * @param {Date} dateFin date de fin à pré-remplir. <b>Doit être le même jour</b> que dateDebut si il est non null ; objet Date JavaScript
	 * @param {SalleRemplissageAjoutEvenement} salles salles à pré-remplir.
	 */
	AjoutEvenement.prototype.show = function(dateDebut, dateFin, salles) {
		
		if(!this.initAppele) {
			this.init();
		}
		
		console.log("params : ", dateDebut, dateFin, salles);
		
		this.jqDialog.find("#txt_nom_evenement").val("");
		this.jqDialog.find("#tbl_materiel td.quantite input").val("0");
		
		var jqDate = this.jqDialog.find("#date_evenement").val("");
		var jqHeureDebut = this.jqDialog.find("#heure_debut").val("");
		var jqHeureFin = this.jqDialog.find("#heure_fin").val("");
		
		if(dateDebut) {
			jqDate.val($.datepicker.formatDate("dd/mm/yy", dateDebut));
			jqHeureDebut.val($.fullCalendar.formatDate(dateDebut, "HH:mm"));
		}
		else if(dateFin) {
			jqDate.val($.datepicker.formatDate("dd/mm/yy", dateFin));
		}
		
		if(dateFin) {
			jqHeureFin.val($.fullCalendar.formatDate(dateFin, "HH:mm"));
		}
		
		if(salles) {
			this.setSalles(salles);
		}
		
		this.jqDialog.dialog("open");
	};
	
	/**
	 * Initialisation de la boîte de dialogue d'ajout d'évènement, ne l'affiche pas.
	 * Doit être appelé uniquement une fois.
	 * Est automatiquement appelé par show() si nécessaire.
	 */
	AjoutEvenement.prototype.init = function() {
		// Récupération des calendriers auxquels l'utilisateur peut ajouter des évènements
		// this.jqDialog.find("#btn_valider_ajout_evenement").attr("disabled", "disabled");
		// TODO : remettre la désactivation du bouton
		this.jqDialog.find("#dialog_ajout_evenement_chargement").css("display", "block");
		this.jqDialog.find("#dialog_ajout_evenement_message_chargement").html("Chargement des options de matériel...");
		
		// Les 2 remplissages s'effectueront dans un ordre "aléatoire" (asynchrone)
		var succesChargementGlobal = true;
		
		// Remplissage du tableau de sélection des matières
		this.rechercheSalle.ecritListeMateriel(this.jqDialog.find("#tbl_materiel"), function(success, nbMateriels) {
			succesChargementGlobal = succesChargementGlobal && success;
			enable();
		});
		
		// Remplissage des calendriers disponibles
		this.remplirCalendriers(function(success, nbCalendriers) {
			succesChargementGlobal = succesChargementGlobal && success && nbCalendriers > 0;
			if(nbCalendriers == 0) {
				window.showToast("Vous n'êtes propriétaire d'aucun calendrier dans lequel vous pouvez ajouter des évènements");
			}
			
			enable();
		});
		
		// Réactivation des contrôles une fois tout chargé
		var aCharger = 2;
		var me = this;
		function enable() {
			aCharger--;
			
			if(aCharger != 0) {
				return;
			}
			
			if(succesChargementGlobal) {
				me.jqDialog.find("#btn_valider_ajout_evenement").removeAttr("disabled");
			}
			
			me.jqDialog.find("#dialog_ajout_evenement_chargement").css("display", "none");
		}

		this.initAppele = true;
	};
	
	return AjoutEvenement;
});