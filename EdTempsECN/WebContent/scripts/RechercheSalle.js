define([ "RestManager", "jquerymaskedinput", "jqueryui" ], function(RestManager) {

	/**
	 * Constructeur
	 */
	var RechercheSalle = function(restManager, jqRechercheSalle) {
		this.restManager = restManager;
		this.jqRechercheSalleForm = jqRechercheSalle.find("#form_chercher_salle");
		this.jqRechercheSalleResultat = jqRechercheSalle.find("#resultat_chercher_salle");
		
		// Variable qui permettent d'accéder facilement aux différents champs du formulaire
		this.date = this.jqRechercheSalleForm.find("#form_recherche_salle_date");
		this.heureDebut = this.jqRechercheSalleForm.find("#form_recherche_salle_debut");
		this.heureFin = this.jqRechercheSalleForm.find("#form_recherche_salle_fin");
		this.capacite = this.jqRechercheSalleForm.find("#form_recherche_salle_capacite");

		// Ecrit la liste des matériels disponibles
		this.ecritListeMateriel();
	};

	/**
	 * Initialise et affiche la boîte de dialogue de recherche d'une salle libre
	 */
	RechercheSalle.prototype.init = function() {
		var me = this;

		// Ajout des masques aux différents champs
		this.heureDebut.mask("99:99");
		this.heureFin.mask("99:99");
		this.capacite.mask("9?999");

		// Affectation d'une méthode au clic sur le bouton "Rechercher"
		this.jqRechercheSalleForm.find("#form_chercher_salle_valid").click(function() {
			// Si le formulaire est valide, la requête est effectuée
			if (me.validationFormulaire()) {
				me.effectuerRecherche();
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
		if (this.date.val()=="") {
			this.bordureSurChamp(this.date, "#FF0000");
			valid = false;
		} else {
			this.bordureSurChamp(this.date, "#60C003");
		}

		// Validation de l'heure de début
		var decoupageHeureMinute = this.heureDebut.val().split(":");
		var calculMinutesDebut = 60*decoupageHeureMinute[0] + decoupageHeureMinute[1];
		if (this.heureDebut.val().length==0 || decoupageHeureMinute[0]>23 || isNaN(decoupageHeureMinute[0]) || decoupageHeureMinute[1]>59 || isNaN(decoupageHeureMinute[1])) {
			this.bordureSurChamp(this.heureDebut, "#FF0000");
			valid = false;
		} else {
			this.bordureSurChamp(this.heureDebut, "#60C003");
		}
		
		// Validation de l'heure de fin
		decoupageHeureMinute = this.heureFin.val().split(":");
		var calculMinutesFin = 60*decoupageHeureMinute[0] + decoupageHeureMinute[1];
		if (this.heureFin.val().length==0 || decoupageHeureMinute[0]>23 || isNaN(decoupageHeureMinute[0]) || decoupageHeureMinute[1]>59 || isNaN(decoupageHeureMinute[1]) || calculMinutesFin-calculMinutesDebut<=0) {
			this.bordureSurChamp(this.heureFin, "#FF0000");
			valid = false;
		} else {
			this.bordureSurChamp(this.heureFin, "#60C003");
		}

		// Validation de la capacité
		if (isNaN(this.capacite.val()) || this.capacite.val()>9999 || this.capacite.val()<0) {
			this.bordureSurChamp(this.capacite, "#FF0000");
			valid = false;
		} else {
			this.bordureSurChamp(this.capacite, "#60C003");
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
					$(".quantite input[type=number]").each(function() {
						me.jqRechercheSalleForm.find(".quantite input[type=number]").each(function() {
							$(this).mask("?9999", { placeholder: "" });
							$(this).click(function() {
								me.bordureSurChamp($(this), "#FFFFFF");
							});
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
	 */
	RechercheSalle.prototype.effectuerRecherche = function() {
		var me = this;
		
		// Message d'attente
		this.jqRechercheSalleForm.find("#form_chercher_salle_valid").attr("disabled", "disabled");
		this.jqRechercheSalleForm.find("#form_chercher_salle_chargement").css("display", "block");
		this.jqRechercheSalleForm.find("#form_chercher_salle_message_chargement").html("Recherche...");
		
		// Récupération des valeurs du formulaire
		var param_date = this.date.val();
		var param_heureDebut = this.heureDebut.val();
		var param_heureFin = this.heureFin.val();
		var param_capacite = this.capacite.val();
		
		// Récupération des quantités pour chaque item de matériel
		// La syntaxe choisie est :
		//    - pour chaque matériel, il y a son identifiant suivi de la quantité, séparés par ":"
		//    - les matériels sont séparés les uns des autres par ","
		var listeMaterielQuantite = "";
		this.jqRechercheSalleForm.find(".quantite input[type=number]").each(function() {
			if (listeMaterielQuantite!="") {
				listeMaterielQuantite += ",";
			}
			listeMaterielQuantite += $(this).attr("materiel-id") + ":" + $(this).val();
		});
		
		// Récupération de la liste des matériels en base de données
		this.restManager.effectuerRequete("GET", "recherchesallelibre", {
			date: param_date, heureDebut: param_heureDebut, heureFin: param_heureFin, capacite: param_capacite, materiel: listeMaterielQuantite, token: this.restManager.getToken()
		}, function(response) {
			if (response.resultCode == RestManager.resultCode_Success) {
				me.afficherResultat(response.data);
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
	 * 			résultat de la requête auprès du serveur
	 */
	RechercheSalle.prototype.afficherResultat = function(data) {

		var maxI = data.sallesDisponibles.length;
		
		if (maxI>0) {
			var me = this;

			// Affiche le résultat
			var html = "<tr><th>Nom de la salle</th></tr>";
			for (var i=0 ; i<maxI ; i++) {
				html += "<tr><td>"+data.sallesDisponibles[i].nom+"</td></tr>";
			}
			this.jqRechercheSalleResultat.find("table").html(html);

			// Affectation d'une méthode au clic sur le bouton "Fermer"
			this.jqRechercheSalleResultat.find("#resultat_chercher_salle_fermer").click(function() {
				me.jqRechercheSalleResultat.dialog("close");
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
