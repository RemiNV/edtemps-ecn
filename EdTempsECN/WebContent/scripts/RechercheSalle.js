define([ "RestManager", "jquerymaskedinput", "jqueryui" ], function(RestManager) {

	/**
	 * Constructeur
	 */
	function RechercheSalle(restManager, jqRechercheSalle) {
		this.restManager = restManager;
		this.jqRechercheSalleForm = jqRechercheSalle.find("#form_chercher_salle");
		this.jqRechercheSalleResultat = jqRechercheSalle.find("#resultat_chercher_salle");
		
		// Variable qui permettent d'accéder facilement aux différents champs du formulaire
		this.jqDate = this.jqRechercheSalleForm.find("#form_recherche_salle_date");
		this.jqHeureDebut = this.jqRechercheSalleForm.find("#form_recherche_salle_debut");
		this.jqHeureFin = this.jqRechercheSalleForm.find("#form_recherche_salle_fin");
		this.jqCapacite = this.jqRechercheSalleForm.find("#form_recherche_salle_capacite");

		// Ecrit la liste des matériels disponibles
		this.ecritListeMateriel();
	};

	/**
	 * Initialise et affiche la boîte de dialogue de recherche d'une salle libre
	 */
	RechercheSalle.prototype.init = function() {
		var me = this;

		// Ajout des masques aux différents champs
		this.jqHeureDebut.mask("99:99");
		this.jqHeureFin.mask("99:99");
		this.jqCapacite.mask("9?999");
		this.jqDate.mask("99-99-9999");

		// Affectation d'une méthode au clic sur le bouton "Rechercher"
		this.jqRechercheSalleForm.find("#form_chercher_salle_valid").click(function() {
			// Si le formulaire est valide, la requête est effectuée
			if (me.validationFormulaire()) {

				// Traitement des dates et heures au format "yyyy-MM-dd HH:mm:ss"
				var param_dateDebut = me.jqDate.val() + " " + me.jqHeureDebut.val() + ":00";
				var param_dateFin = me.jqDate.val() + " " + me.jqHeureFin.val() + ":00";

				// Création de la liste des matériels nécessaires
				var listeMateriel = new Array();
				me.jqRechercheSalleForm.find(".quantite input[type=number]").each(function() {
					var materiel = new Object();
					materiel.id=$(this).attr("materiel-id");
					materiel.quantite=$(this).val();
					listeMateriel.push(materiel);
				});

				// Appel de la méthode de recherche de salle
				me.getSalle(param_dateDebut, param_dateFin, me.jqCapacite.val(), listeMateriel);
			}
		});

		// Affectation d'une méthode au clic sur le bouton "Fermer"
		this.jqRechercheSalleForm.find("#form_chercher_salle_fermer").click(function() {
			me.jqRechercheSalleForm.dialog("close");
		});

		// Affectation d'une méthode au clique sur les différents champs
		this.jqRechercheSalleForm.find("#form_recherche_salle_date, #form_recherche_salle_debut, #form_recherche_salle_fin, #form_recherche_salle_capacite").click(function() {
			me.bordureSurChamp($(this), null);
		});

        // Ajout du datepicker sur le champ date
        this.jqDate.datepicker({
                showAnim : 'slideDown',
                showOn: 'button',
                buttonText: "Calendrier",
                dateFormat: "dd-mm-yy",
                buttonImage: "img/datepicker.png", // image pour le bouton d'affichage du calendrier
                buttonImageOnly: true, // affiche l'image sans bouton
                monthNamesShort: [ "Jan", "Fév", "Mar", "Avr", "Mai", "Jui", "Jui", "Aou", "Sep", "Oct", "Nov", "Dec" ],
                monthNames: [ "Janvier", "Février", "Mars", "Avril", "Mai", "Juin", "Juillet", "Août", "Septembre", "Octobre", "Novembre", "Décembre" ],
                dayNamesMin: [ "Di", "Lu", "Ma", "Me", "Je", "Ve", "Sa" ],
                dayNames: [ "Dimanche", "Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi" ],
                gotoCurrent: true,
                prevText: "Précédent",
                nextText: "Suivant",
                firstDay: 1
        });
        
		// Affiche la boîte dialogue de recherche d'une salle libre
		this.jqRechercheSalleForm.dialog({
			width: 440,
			modal: true,
			show: {
				effect: "fade",
				duration: 200
			},
			hide: {
				effect: "explode",
				duration: 200
			}
		});

	};

	/**
	 * Méthode qui vérifie que le formulaire est correct
	 * 
	 * @return VRAI si le formulaire est valide et FAUX sinon
	 */
	RechercheSalle.prototype.validationFormulaire = function() {
		var valid = true;

		// Validation de la date
		if (this.jqDate.val()=="") {
			this.bordureSurChamp(this.jqDate, "#FF0000");
			valid = false;
		} else {
			this.bordureSurChamp(this.jqDate, "#60C003");
		}

		// Validation de l'heure de début
		var decoupageHeureMinute = this.jqHeureDebut.val().split(":");
		var calculMinutesDebut = 60*decoupageHeureMinute[0] + decoupageHeureMinute[1];
		if (this.jqHeureDebut.val().length==0 || decoupageHeureMinute[0]>23 || isNaN(decoupageHeureMinute[0]) || decoupageHeureMinute[1]>59 || isNaN(decoupageHeureMinute[1])) {
			this.bordureSurChamp(this.jqHeureDebut, "#FF0000");
			valid = false;
		} else {
			this.bordureSurChamp(this.jqHeureDebut, "#60C003");
		}
		
		// Validation de l'heure de fin
		decoupageHeureMinute = this.jqHeureFin.val().split(":");
		var calculMinutesFin = 60*decoupageHeureMinute[0] + decoupageHeureMinute[1];
		if (this.jqHeureFin.val().length==0 || decoupageHeureMinute[0]>23 || isNaN(decoupageHeureMinute[0]) || decoupageHeureMinute[1]>59 || isNaN(decoupageHeureMinute[1]) || calculMinutesFin-calculMinutesDebut<=0) {
			this.bordureSurChamp(this.jqHeureFin, "#FF0000");
			valid = false;
		} else {
			this.bordureSurChamp(this.jqHeureFin, "#60C003");
		}

		// Validation de la capacité
		if (isNaN(this.jqCapacite.val()) || this.jqCapacite.val()>9999 || this.jqCapacite.val()<0) {
			this.bordureSurChamp(this.jqCapacite, "#FF0000");
			valid = false;
		} else {
			this.bordureSurChamp(this.jqCapacite, "#60C003");
		}
		
		// Validation des quantités de matériel
		var me = this;
		this.jqRechercheSalleForm.find(".quantite input[type=number]").each(function() {
			if (isNaN($(this).val()) || $(this).val()<0 || $(this).val()>9999 || $(this).val()=="" ) {
				me.bordureSurChamp($(this), "#FF0000");
				valid = false;
			} else {
				me.bordureSurChamp($(this), "#60C003");
			}
		});

		// Message d'erreur pour un problème d'ordre des heures
		if (calculMinutesFin-calculMinutesDebut<=0) {
			window.showToast("L'heure de fin doit être supérieure à l'heure de début");
		}
		
		// Message d'erreur général pour les champs incorrects
		if (!valid) {
			window.showToast("Veuillez vérifier et corriger les champs entourés en rouge");
		}
		
		return valid;

	};


	/**
	 * Ecrit la liste des matériels
	 */
	RechercheSalle.prototype.ecritListeMateriel = function() {
		var me = this;
		
		// Blocage du bouton de validation avant le chargement
		this.jqRechercheSalleForm.find("#form_chercher_salle_valid").attr("disabled", "disabled");
		this.jqRechercheSalleForm.find("#form_chercher_salle_chargement").css("display", "block");
		this.jqRechercheSalleForm.find("#form_chercher_salle_message_chargement").html("Chargement des options de matériel...");
		
		// Récupération de la liste des matériels en base de données
		this.restManager.effectuerRequete("GET", "listemateriels", {
			token: this.restManager.getToken()
		}, function(data) {
			if (data.resultCode == RestManager.resultCode_Success) {
				
				var maxI = data.data.listeMateriels.length;
				
				if (maxI!=0) {

					// Préparation du code html
					var str = "";
					for (var i = 0 ; i < maxI ; i++) {
						str += "<tr>";
						str += "<td class='libelle'>" + data.data.listeMateriels[i].nom + "</td>";
						str += "<td class='quantite'><input type='number' materiel-id='" + data.data.listeMateriels[i].id + "' value='0' /></td>";
						str += "</tr>";
					}
					
					// Ajout du code html dans la liste de matériels
					me.jqRechercheSalleForm.find("#form_chercher_salle_liste_materiel table").append(str);
			
					// Ajout des masques sur les quantités de matériel
					me.jqRechercheSalleForm.find(".quantite input[type=number]").each(function() {
						$(this).mask("?9999", { placeholder: "" });
						$(this).click(function() {
							me.bordureSurChamp($(this), "#FFFFFF");
						});
					});

				} else {
					// S'il n'y a pas de produits en base de données, on cache le tableau dans le formulaire
					me.jqRechercheSalleForm.find("#form_chercher_salle_liste_materiel").hide();
				}
				
				// Reactivation du bouton de recherche
				me.jqRechercheSalleForm.find("#form_chercher_salle_valid").removeAttr("disabled");
				me.jqRechercheSalleForm.find("#form_chercher_salle_chargement").css("display", "none");

			} else if (data.resultCode == RestManager.resultCode_NetworkError) {
				window.showToast("Erreur de récupération des matériels disponibles ; vérifiez votre connexion.");
			} else {
				window.showToast(data.resultCode + " Erreur de récupération des matériels disponibles ; votre session a peut-être expiré ?");
			}
		});

	};
	
	/**
	 * Méthode qui effectue la requête
	 * 
	 * @param dateDebut
	 * 		date de début de l'événement au format : "yyyy-MM-dd HH:mm:ss"
	 * 
	 * @param dateFin
	 * 		date de fin de l'événement au format : "yyyy-MM-dd HH:mm:ss"
	 * 
	 * @param effectif
	 * 		effectif requis pour l'événement
	 * 
	 * @param materiels
	 * 		liste du matériel nécessaire : une liste d'objets qui possèdent deux attributs : id et quantité
	 * 
	 * @param callback
	 * 		méthode appellée en retour et qui recevra les salles sélectionnées en paramètre
	 */
	RechercheSalle.prototype.getSalle = function(dateDebut, dateFin, effectif, materiels, callback) {
		var me = this;

		// Message d'attente
		this.jqRechercheSalleForm.find("#form_chercher_salle_valid").attr("disabled", "disabled");
		this.jqRechercheSalleForm.find("#form_chercher_salle_chargement").css("display", "block");
		this.jqRechercheSalleForm.find("#form_chercher_salle_message_chargement").html("Recherche...");

		// Création d'une chaine de caractère pour traiter la liste de matériel
		// La syntaxe choisie est :
		//    - pour chaque matériel, il y a son identifiant suivi de la quantité, séparés par ":"
		//    - les matériels sont séparés les uns des autres par ","
		var listeMaterielQuantite = "";
		for (var i=0, maxI=materiels.length; i<maxI; i++) {
			if (listeMaterielQuantite!="") {
				listeMaterielQuantite += ",";
			}
			listeMaterielQuantite += materiels[i].id + ":" + materiels[i].quantite;
		}
		
		// Récupération de la liste des matériels en base de données
		this.restManager.effectuerRequete("GET", "recherchesallelibre", {
			dateDebut: dateDebut, dateFin: dateFin, effectif: effectif, materiel: listeMaterielQuantite, token: this.restManager.getToken()
		}, function(response) {
			if (response.resultCode == RestManager.resultCode_Success) {
				me.afficherResultat(response.data, callback);
			} else if (response.resultCode == RestManager.resultCode_NetworkError) {
				window.showToast("Erreur lors de la recheche d'une salle libre ; vérifiez votre connexion.");
			} else {
				window.showToast(response.resultCode + " Erreur lors de la recheche d'une salle libre ; votre session a peut-être expiré ?");
			}
			
			// Supression message d'attente
			me.jqRechercheSalleForm.find("#form_chercher_salle_valid").removeAttr("disabled");
			me.jqRechercheSalleForm.find("#form_chercher_salle_chargement").css("display", "none");
		});
		
	};
	
	
	/**
	 * Méthode qui affiche le résultat
	 * 
	 * @param data
	 * 			liste des salles retournées par le serveur
	 * 
	 * @param callback
	 * 		méthode appellée en retour et qui recevra les salles sélectionnées en paramètre
	 */
	RechercheSalle.prototype.afficherResultat = function(data, callback) {

		// Calcule le nombre de salles
		var maxI = data.sallesDisponibles.length;
		

		// S'il y a des salles, on les affiche dans une boîte de dialogue
		if (maxI>0) {
			var me = this;

			var html = "<tr><th>Nom de la salle</th></tr>";
			for (var i=0 ; i<maxI ; i++) {
				// Préparation de l'infobulle qui contient la liste des matériels de la salle
				var infobulle = "";
				for (var j=0, maxJ=data.sallesDisponibles[i].materiels.length ; j<maxJ ; j++) {
					if (infobulle!="") {
						infobulle += "&#13;";
					}
					infobulle += data.sallesDisponibles[i].materiels[j].nom + " : " +data.sallesDisponibles[i].materiels[j].quantite; 
				}
				if (infobulle=="") {
					infobulle = "Aucun matériel spécifique";
				}
				// Créaion du code html de la ligne correspondant à cette salle
				html += "<tr>" +
							"<td class='resultat_chercher_salle_ligne' title='" + infobulle + "'>" +
								data.sallesDisponibles[i].nom +
							"</td></tr>";

			}
			// Ecrit le code html dans la boîte de dialogue
			this.jqRechercheSalleResultat.find("table").html(html);

			// Affectation d'une méthode au clic sur le bouton "Fermer"
			this.jqRechercheSalleResultat.find("#resultat_chercher_salle_fermer").click(function() {
				me.jqRechercheSalleResultat.dialog("close");
			});

			// Affectation d'une méthode au clic sur le bouton "Créer un événement"
			this.jqRechercheSalleResultat.find("#resultat_chercher_salle_creer").click(function() {
				// TODO récupérer les salles sélectionnées
				callback();
			});

			// Affichage de la boîte de dialogue résultat
			this.jqRechercheSalleResultat.dialog({
				width: 350,
				modal: true,
				show: { effect: "fade", duration: 200 },
				hide: { effect: "explode", duration: 200 }
			});
			
		} else {
			window.showToast("Aucune salle disponible avec ces critères");
		}

	};

	
	/**
	 * Met une bordure de couleur autour de l'élément
	 * 
	 * @param champ
	 * 			champ à entourer
	 * @param color
	 * 			couleur de la bordure, si elle vaut NULL la bordure est enlevée
	 */
	RechercheSalle.prototype.bordureSurChamp = function(champ, color) {
		if (color == null) {
			$(champ).css("box-shadow", "none");
			$(champ).css("border", "1px solid black");
		} else {
			$(champ).css("box-shadow", color+" 0 0 10px");
			$(champ).css("border", "1px solid "+color);
		}
	};

	return RechercheSalle;

});
