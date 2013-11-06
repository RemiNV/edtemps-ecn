define(["CalendrierGestion", "RestManager", "jquery", "jqueryui", "jquerymaskedinput"], function(CalendrierGestion, RestManager) {
	
	function AjoutEvenement(restManager, jqDialog, rechercheSalle) {
		
		this.jqDialog = jqDialog;
		this.restManager = restManager;
		this.strOptionsCalendriers = null; // HTML à ajouter au select pour sélectionner les calendriers
		this.rechercheSalle = rechercheSalle;
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
			
			this.rechercheSalle.getSalle(formData.dateDebut, formData.dateFin, effectif, formData.materiels, function(succes) {
				if(succes) {
					me.jqDialog.find("#btn_rechercher_salle_evenement").removeAttr("disabled");
					me.jqDialog.find("#dialog_ajout_evenement_chargement").css("display", "none");
				}
			}, function(salles) {
				me.sallesSelectionnees = salles;
				
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
					strSalles = "(Sélectionnez une ou plusieurs salle(s))";
				}
				
				me.jqDialog.find("#salles_evenement").html(strSalles);
			});
		}
		else {
			window.showToast("Remplissez les champs indiqués pour lancer la recherche");
		}
		
	};
	
	AjoutEvenement.prototype.validationDialog = function() {
		// Récupération des données du formulaire
		var formData = this.getDonneesFormulaire(false);
		
		if(formData.valide) {
			
			// Message d'attente
			this.jqDialog.find("#dialog_ajout_evenement_chargement").css("display", "block");
			this.jqDialog.find("#dialog_ajout_evenement_message_chargement").html("Ajout de l'évènement...");
			
			this.restManager.effectuerRequete("POST", "ajoutEvenement"); // TODO : compléter)
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
	 * Vérifie les données du formulaire et les renvoie
	 * Format de l'objet renvoyé : contient les attributs : 
	 * - valide : booléen, true si le formulaire est complet
	 * - valideRechercheSalle: booléen, true si les informations nécessaires à la recherche
	 * 		d'une salle sont présentes
	 * - nom
	 * - proprietaires : non implémenté pour l'instant (null)
	 * - calendriers : tableau d'ID de calendriers
	 * - dateDebut : date JavaScript
	 * - dateFin : date JavaScript
	 * - materiels : matériels sélectionnés pour l'évènement
	 * - salles : tableau d'ID de salles
	 * 
	 * @param pourRechercheSalle Signaler les champs non remplis nécessaires pour rechercher une salle uniquement (booléen)
	 * @return Données du formulaire selon la syntaxe précédente
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
		
		// Récupération de la liste des propriétaires à implémenter
		res.proprietaires = null;
		
		// Récupération de la liste des calendriers
		res.calendriers = new Array();
		
		this.jqDialog.find("#calendriers_evenement .select_calendriers").each(function() {
			res.calendriers.push(parseInt($(this).val()));
		});
		
		// Validation du jour
		var strDate = false;
		
		try {
			var date = $.datepicker.parseDate("dd/mm/yy", jqDate.val());
			
			if(jqDate.val() != "") {
				strDate = $.datepicker.formatDate("yy-mm-dd", date);
				jqDate.addClass("valide").removeClass("invalide");
			}
		}
		catch(parseError) {
			// Rien ici : strDate sera à false en cas d'erreur de parsing de la date
		}
		
		if(!strDate) {
			jqDate.addClass("invalide").removeClass("valide");
		}
		
		// Validation des dates de début et fin
		var heureDebut = validateNotEmpty(jqHeureDebut);
		var heureFin = validateNotEmpty(jqHeureFin);
		
		
		if(strDate && heureDebut !== "") {
			// Chaîne au format ISO8601
			res.dateDebut = new Date(strDate + "T" + heureDebut);
		}
		else {
			res.dateDebut = null;
		}
		
		if(strDate && heureFin !== "") {
			res.dateFin = new Date(strDate + "T" + heureFin);
		}
		else {
			res.dateFin = null;
		}
		
		// Récupération des matériels et salles
		res.materiels = this.rechercheSalle.getContenuListeMateriel(this.jqDialog.find("#tbl_materiel"));
		res.salles = this.sallesSelectionnees;
		
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
		
		return res;
	};
	
	/**
	 * Remplissage de la sélection des calendriers dans la boîte de dialogue d'ajout d'évènement
	 * @param callback Fonction rappelée une fois les calendriers remplis, avec un booléen en argument indiquant le succès de l'opération et le nombre de calendriers trouvés
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
	 * Affichage de la boîte de dialogue d'ajout d'évènement
	 */
	AjoutEvenement.prototype.show = function() {
		
		if(!this.initAppele) {
			this.init();
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