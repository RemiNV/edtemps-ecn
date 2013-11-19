
/**
 * Module de contrôle de la boîte de dialogue d'ajout d'évènement
 * @module DialogAjoutEvenement
 */
define(["CalendrierGestion", "RestManager", "MultiWidget", "UtilisateurGestion", "jquery", "jqueryui", 
        "jquerymaskedinput", "lib/fullcalendar.translated.min"], function(CalendrierGestion, RestManager, MultiWidget, UtilisateurGestion) {
	
	/**
	 * Création d'un module d'ajout d'évènements (contrôle la boîte de dialogue associée)
	 * @param {module:RestManager} restManager Référence au restManager instancié
	 * @param {jQuery} jqDialog Objet jQuery du DOM dans lequel afficher la dialog
	 * @param {module:RechercheSalle} rechercheSalle Référence à l'objet rechercheSalle instancié
	 * @param {module:EvenementGestion} evenementGestion Référence à l'objet evenementGestion instancié
	 * @param {function} callbackRafraichirCalendrier Fonction à appeler pour déclencher le rafraîchissement du calendrier après ajout d'un évènement
	 * 
	 * @constructor
	 * @alias module:DialogAjoutEvenement
	 */
	var DialogAjoutEvenement = function(restManager, jqDialog, rechercheSalle, evenementGestion, callbackRafraichirCalendrier) {
		
		this.jqDialog = jqDialog;
		this.restManager = restManager;
		this.rechercheSalle = rechercheSalle;
		this.evenementGestion = evenementGestion;
		this.callbackRafraichirCalendrier = callbackRafraichirCalendrier;
		this.strOptionsCalendriers = null; // HTML à ajouter au select pour sélectionner les calendriers
		this.sallesSelectionnees = new Array();
		this.sallesLibres = new Array();
		this.initAppele = false;
		this.multiWidgetProprietaires = null;
		this.multiWidgetIntervenants = null;
		this.multiWidgetCalendriers = null;
		this.listeCalendriers = new Array(); /* Liste des calendriers récupérée en base de données */
		this.rechercheDisponibiliteSalles = {
			versionSalles: 0, // Incrémenté à chaque changement des salles pour ignorer le résultat de la recherche effectué entre-temps
			numeroRecherche: 0 // Incrémenté à chaque recherche pour ignorer les résultats des recherches en cours en en lançant une nouvelle
		};
		
		var me = this;
		
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
                firstDay: 1,
                onSelect: function() {
                	me.verifierDisponibiliteSalles();
                }
        });
		
		// Listeners
		jqDialog.find("#btn_rechercher_salle_evenement").click(function() {
			me.lancerRechercheSalle();
		});
		
		jqDialog.find("#btn_valider_ajout_evenement").click(function() {
			me.validationDialog();
		});
		
		var handlerChangementHeure = function(e) {
			// Vérification que la saisie est complète (pas le masque "_" à compléter)
			if($(this).val().charAt(4) != "_") {
				me.verifierDisponibiliteSalles();
			}
		};
		
		jqDialog.find("#heure_debut").on("paste keyup", handlerChangementHeure);
		jqDialog.find("#heure_fin").on("paste keyup", handlerChangementHeure);
		jqDialog.find("#calendriers_evenement").change(function() {
			me.verifierDisponibiliteSalles();
		});
	};
	
	var SALLE_LIBRE = "salle_libre";
	var SALLE_OCCUPEE_NONCOURS = "salle_occupee_noncours";
	var SALLE_OCCUPEE = "salle_occupee";
	
	/**
	 * Définit les salles à afficher en les marquant toutes comme libres
	 * @param salles
	 * @param calendrierCours Indique si l'évènement à ajouter est rattaché à un calendrier de cours
	 */
	DialogAjoutEvenement.prototype.setSalles = function(salles) {
		this.rechercheDisponibiliteSalles.versionSalles++;
		this.sallesSelectionnees = salles;
		this.sallesLibres = new Array();
		
		// Suppression des notes sur les salles occupées
		this.jqDialog.find("#notes_salles_occupees_non_cours").css("display", "none");
		this.jqDialog.find("#lst_evenements_salles_occupees_non_cours").children().remove();
		this.jqDialog.find("#notes_salles_occupees").css("display", "none");
		this.jqDialog.find("#lst_evenements_salles_occupees").children().remove();
		
		for(var i = 0, maxI = salles.length; i<maxI; i++) {
			if(salles[i].evenementsEnCours == null || salles[i].evenementsEnCours.length == 0) {
				this.sallesLibres[i] = SALLE_LIBRE;
			}
			else {
				this.sallesLibres[i] = SALLE_OCCUPEE_NONCOURS;
				
				// Si les salles retournées par la recherche sont occupées, c'est qu'on est en train de définir un cours
				this.afficherEvenementsSalleOccupee(null, salles[i].evenementsEnCours, true, salles[i].nom);
			}
		}
		
		// Affichage des salles dans la zone de texte
		this.affichageSalles();
	};
	
	/**
	 * Rafraîchit l'affichage des salles sélectionnées en fonction de
	 * this.sallesSelectionnees et this.sallesLibres
	 */
	DialogAjoutEvenement.prototype.affichageSalles = function() {
		var strSalles;
		if(this.sallesSelectionnees.length > 0) {
			strSalles = "";
			for(var i=0, maxI = this.sallesSelectionnees.length; i<maxI; i++) {
				if(i != 0) {
					strSalles += ", ";
				}
				
				strSalles += "<span class='" + this.sallesLibres[i] + "'>" + this.sallesSelectionnees[i].nom + "</span>";
			}
		}
		else {
			strSalles = "(Recherchez une ou plusieurs salle(s))";
		}
		
		this.jqDialog.find("#salles_evenement").html(strSalles);
	};
	
	
	/**
	 * Fonction utilitaire permettant d'ajouter un évènement à une des listes d'évènements
	 * des salles occupées
	 */
	function ajouterEvenListeSalleOccupee(jqListe, even, nomSalle) {
		jqListe.append("<li>" + 
				$.fullCalendar.formatDate(new Date(even.dateDebut), "HH:mm") + " - " + $.fullCalendar.formatDate(new Date(even.dateFin), "HH:mm") + " " +
				even.nom + " (" + nomSalle + ")" + 
				"</li>");
	}
	
	/**
	 * Affichage des événements déjà prévus dans une salle à l'heure sélectionnée
	 *  
	 * @param evenementsCours Evénements de cours (peut être null)
	 * @param evenementsNonCours Evénements non cours (peut être null)
	 * @param calendrierCours L'évènement en cours de sélection est un cours
	 */
	DialogAjoutEvenement.prototype.afficherEvenementsSalleOccupee = function(evenementsCours, evenementsNonCours, calendrierCours, nomSalle) {
		var lstEvenementsSallesOccupeesNonCours = this.jqDialog.find(
				calendrierCours ? "#lst_evenements_salles_occupees_non_cours" : "#lst_evenements_salles_occupees");
		
		var divSallesOccupeesNonCours = this.jqDialog.find(
				calendrierCours ? "#notes_salles_occupees_non_cours" : "#notes_salles_occupees");
		
		if(evenementsNonCours != null && evenementsNonCours.length > 0) {
			divSallesOccupeesNonCours.css("display", "block");
		
			for(var j=0, maxJ=evenementsNonCours.length; j<maxJ; j++) {
				ajouterEvenListeSalleOccupee(lstEvenementsSallesOccupeesNonCours, 
						evenementsNonCours[j], nomSalle);
			}
		}
		
		if(evenementsCours != null && evenementsCours.length > 0) {
			this.jqDialog.find("#notes_salles_occupees").css("display", "block");
		
			var lstEvenementsSallesOccupees = this.jqDialog.find("#lst_evenements_salles_occupees");
			for(var j=0, maxJ=evenementsCours.length; j<maxJ; j++) {
				ajouterEvenListeSalleOccupee(lstEvenementsSallesOccupees, 
						evenementsCours[j], nomSalle);
			}
		}
	};
	
	DialogAjoutEvenement.prototype.verifierDisponibiliteSalles = function() {
		
		if(this.sallesSelectionnees.length == 0) {
			return;
		}
		
		// Récupération des dates du formulaire
		var formData = this.getDonneesFormulaire(true);
		
		if(!formData.valideRechercheSalle) {
			return;
		}
		
		var me = this;
		this.rechercheDisponibiliteSalles.numeroRecherche++;
		var numeroRecherche = this.rechercheDisponibiliteSalles.numeroRecherche;
		var versionSalles = this.rechercheDisponibiliteSalles.versionSalles;
		var nbVerificationsRestantes = this.sallesSelectionnees.length;
		
		this.jqDialog.find("#notes_salles_occupees_non_cours").css("display", "none");
		this.jqDialog.find("#lst_evenements_salles_occupees_non_cours").children().remove();
		this.jqDialog.find("#notes_salles_occupees").css("display", "none");
		this.jqDialog.find("#lst_evenements_salles_occupees").children().remove();
		
		this.jqDialog.find("#btn_valider_ajout_evenement").attr("disabled", "disabled");
		this.jqDialog.find("#dialog_ajout_evenement_chargement").css("display", "block");
		this.jqDialog.find("#dialog_ajout_evenement_message_chargement").html("Vérification de la disponibilité des salles...");
		
		for(var i=0, maxI=this.sallesSelectionnees.length; i<maxI; i++) {
			this.rechercheDisponibiliteSallesEnCours++;
			
			this.restManager.effectuerRequete("GET", "disponibilitesalle", {
				token: this.restManager.getToken(),
				debut: formData.dateDebut.getTime(),
				fin: formData.dateFin.getTime(),
				idSalle: this.sallesSelectionnees[i].id
			}, (function(i) { // Closure pour prendre la valeur de i au moment de l'appel à effectuerRequete, et pas après la requête
				return function(data) {
					
					// On ignore le résultat de la recherche si une nouvelle est en cours, ou si les salles ont été changées
					if(numeroRecherche != me.rechercheDisponibiliteSalles.numeroRecherche 
							|| versionSalles != me.rechercheDisponibiliteSalles.versionSalles) {
						return;
					}
					
					if(data.resultCode == RestManager.resultCode_Success) {
						
						// Modification des évènements dans la salle pour le nouvel intervalle sélectionné
						if(formData.calendrierCours) {
							me.sallesSelectionnees[i].evenementsEnCours = data.data.evenementsNonCours;
						}
						
						// Affichage des évènements qui occupent les salles
						me.afficherEvenementsSalleOccupee(data.data.evenementsCours, data.data.evenementsNonCours, 
								formData.calendrierCours, me.sallesSelectionnees[i].nom);
						
						// Marquage de la salle comme disponible ou non
						if(data.data.disponibleNonCours) {
							me.sallesLibres[i] = SALLE_LIBRE;
						}
						else if(data.data.disponibleCours && formData.calendrierCours) {
							me.sallesLibres[i] = SALLE_OCCUPEE_NONCOURS;
						}
						else {
							me.sallesLibres[i] = SALLE_OCCUPEE;
						}
					}
					else {
						window.showToast("Echec de vérification de la disponibilité d'une salle");
					}
					
					nbVerificationsRestantes--;
					if(nbVerificationsRestantes == 0) {
						me.jqDialog.find("#dialog_ajout_evenement_chargement").css("display", "none");
						me.jqDialog.find("#btn_valider_ajout_evenement").removeAttr("disabled");
						
						// Mise à jour de l'affichage des salles
						me.affichageSalles();
					}
				};
			})(i));
		}
	};
	
	DialogAjoutEvenement.prototype.lancerRechercheSalle = function() {
		
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
			
			this.rechercheSalle.getSalle(formData.dateDebut, formData.dateFin, effectif, formData.materiels, formData.calendrierCours, function(succes) {
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
	
	DialogAjoutEvenement.prototype.validationDialog = function() {
		// Récupération des données du formulaire
		var formData = this.getDonneesFormulaire(false);
		var me = this;
		
		if(formData.valide && (formData.salles.length > 0 || confirm("Ajouter un évènement sans salle ?"))) {
			
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
					window.showToast("Erreur d'ajout de l'événement ; salle(s) occupée(s) pendant ce créneau");
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
	 * @property {boolean] calendrierCours - Indique si au moins un des calendriers sélectionnés est un calendrier de cours
	 * @property {number[]} idEvenementsSallesALiberer - Tableau d'IDs des évènements dont les salles sont à libérer
	 */
	
	/**
	 * Vérifie les données du formulaire et les renvoie
	 * 
	 * @param {boolean} pourRechercheSalle Signaler les champs non remplis nécessaires pour rechercher une salle uniquement
	 * @return {DonneesFormulaireAjoutEvenement} Données du formulaire
	 */
	DialogAjoutEvenement.prototype.getDonneesFormulaire = function(pourRechercheSalle) {
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
		
		res.responsables = this.multiWidgetProprietaires.val();
		
		res.intervenants = this.multiWidgetIntervenants.val();
		
		// Récupération de la liste des calendriers
		res.calendriers = this.multiWidgetCalendriers.val();
		
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
		
		// Remplissage de res.valide et res.valideRechercheSalle
		if(res.dateDebut != null && res.dateFin != null) {
			res.valideRechercheSalle = true;
			
			if(res.nom !== "" && res.calendriers.length > 0) {
				res.valide = true;
			}
		}
		
		// TODO : gérer plusieurs calendriers
		// Vérifie si les calendriers sélectionnés pour le rattachement de l'événement contiennent des cours
		res.calendrierCours = false;
		var idCalendrierSelectionne = this.jqDialog.find("#calendriers_evenement .select_calendriers").val();
		for (var i=0, maxI=this.listeCalendriers.length; i<maxI; i++) {
			if (this.listeCalendriers[i].id==idCalendrierSelectionne && this.listeCalendriers[i].estCours) {
				res.calendrierCours = true;
				break;
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
	DialogAjoutEvenement.prototype.remplirCalendriers = function(callback) {
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
					var selectCalendriers = me.jqDialog.find("#calendriers_evenement .select_calendriers"); 
					selectCalendriers.html(strRemplissageSelect);
					
					me.multiWidgetCalendriers = new MultiWidget(selectCalendriers, {
						setFunction: function(jqControl, value) {
							if(value == null) { // On n'accepte pas une valeur vide
								jqControl.val(jqControl.find("option:first").val());
							}
							else {
								jqControl.val(value);
							}
						}, 
						getValFunction: function(jqControl) {
							return parseInt(jqControl.val());
						},
						width: 250
					});
					
				}
				
				me.listeCalendriers = data;
				
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
	 * Méthode de remplissage des propriétaires et intervenants de la dialog, à l'initialisation
	 * @param callback Fonction appelée une fois le remplissage effectué (asychrone). Prend un booléen en argument indiquant le succès.
	 */
	DialogAjoutEvenement.prototype.remplirProprietairesIntervenants = function(callback) {
		var utilisateurGestion = new UtilisateurGestion(this.restManager);
		var me = this;
		
		utilisateurGestion.recupererProprietairesPotentielsAutocomplete(function(resultCode, proprietaires) {
			if(resultCode == RestManager.resultCode_Success) {
				
				me.multiWidgetProprietaires = new MultiWidget(me.jqDialog.find("#input_proprietaires_evenement"), 
						MultiWidget.AUTOCOMPLETE_OPTIONS(proprietaires, 3, { label: "vous-même", value: me.restManager.getUserId() }, 250));
				
				me.multiWidgetIntervenants = new MultiWidget(me.jqDialog.find("#input_intervenants_evenement"), 
						MultiWidget.AUTOCOMPLETE_OPTIONS(proprietaires, 3, null, 250));
				
				callback(true);
			}
			else if(resultCode == RestManager.resultCode_NetworkError) {
				window.showToast("Erreur de récupération des propriétaires et intervenants ; vérifiez votre connexion");
				callback(false);
			}
			else {
				window.showToast("Erreur de récupération des propriétaires et intervenants.");
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
	DialogAjoutEvenement.prototype.show = function(dateDebut, dateFin, salles) {
		
		if(!this.initAppele) {
			this.init();
		}
		
		if(salles) {
			this.setSalles(salles);
		}
		else {
			this.setSalles(new Array());
		}
		
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
		
		this.jqDialog.dialog("open");
	};
	
	/**
	 * Initialisation de la boîte de dialogue d'ajout d'évènement, ne l'affiche pas.
	 * Doit être appelé uniquement une fois.
	 * Est automatiquement appelé par show() si nécessaire.
	 */
	DialogAjoutEvenement.prototype.init = function() {
		// Récupération des calendriers auxquels l'utilisateur peut ajouter des évènements
		// this.jqDialog.find("#btn_valider_ajout_evenement").attr("disabled", "disabled");
		// TODO : remettre la désactivation du bouton
		this.jqDialog.find("#dialog_ajout_evenement_chargement").css("display", "block");
		this.jqDialog.find("#dialog_ajout_evenement_message_chargement").html("Chargement des options de matériel...");
		
		// Les remplissages s'effectueront dans un ordre "aléatoire" (asynchrone)
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
		
		// Remplissage de la liste des propriétaires et intervenants
		this.remplirProprietairesIntervenants(function(success) {
			successChargementGlobal = succesChargementGlobal && success;
			enable();
		});
		
		// Réactivation des contrôles une fois tout chargé
		var aCharger = 3;
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
	
	return DialogAjoutEvenement;
});