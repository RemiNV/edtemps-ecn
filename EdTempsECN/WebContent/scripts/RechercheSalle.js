define([ "RestManager", "jquerymaskedinput" ], function(RestManager) {

	/**
	 * Constructeur
	 */
	var RechercheSalle = function(restManager, jqFormChercherSalle) {
		this.restManager = restManager;
		this.jqFormChercherSalle = jqFormChercherSalle;
		
		// Variable qui permettent d'accéder facilement aux différents champs du formulaire
		this.date = jqFormChercherSalle.find("#form_recherche_salle_date");
		this.heureDebut = jqFormChercherSalle.find("#form_recherche_salle_debut");
		this.heureFin = jqFormChercherSalle.find("#form_recherche_salle_fin");
		this.capacite = jqFormChercherSalle.find("#form_recherche_salle_capacite");

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
		this.jqFormChercherSalle.find("#form_chercher_salle_valid").click(function() {
			// Si le formulaire est valide, la requête est effectuée
			if (me.validationFormulaire()) {
				me.effectuerRecherche();
			}
		});

		// Affectation d'une méthode au clic sur le bouton "Annuler"
		this.jqFormChercherSalle.find("#form_chercher_salle_annuler").click(function() {
			me.jqFormChercherSalle.find("#form_chercher_salle").dialog("close");
		});

		// Affectation d'une méthode au clique sur les différents champs
		this.jqFormChercherSalle.find("#form_recherche_salle_date, #form_recherche_salle_debut, #form_recherche_salle_fin, #form_recherche_salle_capacite").click(function() {
			$(this).css({border: "1px solid black"});
		});

		// Affiche la boîte dialogue de recherche d'une salle libre
		this.jqFormChercherSalle.dialog({
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
			this.date.css({border: "1px solid red"});
			valid = false;
		} else {
			this.date.css({border: "1px solid black"});
		}

		// Validation de l'heure de début
		var decoupageHeureMinute = this.heureDebut.val().split(":");
		if (this.heureDebut.val().length==0 || decoupageHeureMinute[0]>23 || isNaN(decoupageHeureMinute[0]) || decoupageHeureMinute[1]>59 || isNaN(decoupageHeureMinute[1])) {
			this.heureDebut.css({border: "1px solid red"});
			valid = false;
		} else {
			this.heureDebut.css({border: "1px solid black"});
		}

		// Validation de l'heure de fin
		decoupageHeureMinute = this.heureFin.val().split(":");
		if (this.heureFin.val().length==0 || decoupageHeureMinute[0]>23 || isNaN(decoupageHeureMinute[0]) || decoupageHeureMinute[1]>59 || isNaN(decoupageHeureMinute[1])) {
			this.heureFin.css({border: "1px solid red"});
			valid = false;
		} else {
			this.heureFin.css({border: "1px solid black"});
		}
		
		// Validation de la capacité
		if (isNaN(this.capacite.val())) {
			this.capacite.css({border: "1px solid red"});
			valid = false;
		} else {
			this.capacite.css({border: "1px solid black"});
		}
		
		// Validation des quantités de matériel
		this.jqFormChercherSalle.find(".quantite input[type=number]").each(function() {
			if (isNaN($(this).val()) || $(this).val()<0 || $(this).val()=="") {
				$(this).css({border: "1px solid red"});
				valid = false;
			} else {
				$(this).css({border: "1px solid white"});
			}
		});

		return valid;

	};


	/**
	 * Ecrit la liste des matériels
	 */
	RechercheSalle.prototype.ecritListeMateriel = function() {

		var me = this;
		
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
					me.jqFormChercherSalle.find("#form_chercher_salle_liste_materiel table").append(str);
			
					// Ajout des masques sur les quantités de matériel
					me.jqFormChercherSalle.find(".quantite input[type=number]").each(function() {
						$(this).mask("9?999");
						$(this).click(function() {
							$(this).css({border: "1px solid white"});
						});
					});

				} else {
					// S'il n'y a pas de produits en base de données, on cache le tableau dans le formulaire
					me.jqFormChercherSalle.find("#form_chercher_salle_liste_materiel").hide();
				}

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
		this.jqFormChercherSalle.find(".quantite input[type=number]").each(function() {
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
				me.afficherResultatalert(response.data);
			} else if (response.resultCode == RestManager.resultCode_NetworkError) {
				window.showToast("Erreur lors de la recheche d'une salle libre ; vérifiez votre connexion.");
			} else {
				window.showToast(response.resultCode + " Erreur lors de la recheche d'une salle libre ; votre session a peut-être expiré ?");
			}
		});
		
		
	};
	
	/**
	 * Méthode qui affiche le résultat
	 */
	RechercheSalle.prototype.afficherResultat = function(data) {
		alert('Affiche le résultat');
	};

	return RechercheSalle;

});
