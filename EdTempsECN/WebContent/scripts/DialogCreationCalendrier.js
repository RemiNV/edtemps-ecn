/**
 * @module DialogCreationCalendrier
 */
define([ "RestManager", "CalendrierGestion", "MultiWidget", "jquerymaskedinput" ], function(RestManager, CalendrierGestion, MultiWidget) {

	/**
	 * @constructor
	 * @alias module:DialogCreationCalendrier
	 */
	var DialogCreationCalendrier = function(restManager, ecranParametres, jqDialog) {
		this.restManager = restManager;
		this.ecranParametres = ecranParametres;
		this.calendrierGestion = new CalendrierGestion(this.restManager);
		this.jqDialog = jqDialog;
		// Accès direct aux champs du formulaire
		this.nom = jqDialog.find("#form_creer_calendrier_nom");
		this.type = jqDialog.find("#form_creer_calendrier_type");
		this.matiere = jqDialog.find("#form_creer_calendrier_matiere");
		// Liste des propriétaires potentiels = de tous les utilisateurs
		this.listeProprietairesPotentiels = new Array();
		this.initAppele = false;
		this.multiWidgetProprietaires = null;
	};
	
	/**
	 * Affichage de la boîte de dialogue
	 */
	DialogCreationCalendrier.prototype.show = function() {
		if(!this.initAppele) {
			this.init();
		}
		
		this.jqDialog.dialog("open");
	};

	/**
	 * Initialise la boîte de dialogue de création d'un calendrier
	 */
	DialogCreationCalendrier.prototype.init = function() {
		var me = this;

		// Remplir les combobox matieres et types
		me.remplirComboboxes();
		
		// Récupérer propriétaires potentiels
		me.recupererProprietairesPotentiels(function(success, nbUtilisateurs) {
			if (success) {
				
				// Création des autocomplete pour les propriétaires
				me.multiWidgetProprietaires = new MultiWidget(me.jqDialog.find("#form_creer_calendrier_input_proprietaire"), 
						MultiWidget.AUTOCOMPLETE_OPTIONS(me.listeProprietairesPotentiels, 3, { label: "Vous-même", value: me.restManager.getUserId() }, 230));
				
				
				// On active le bouton "Valider"
				me.jqDialog.find("#form_creer_calendrier_valider").click(function() {
					me.effectuerRequeteCreation();
				});
			}
		});
		
		// Listener bouton "Annuler"
		this.jqDialog.find("#form_creer_calendrier_annuler").click(function() {
			me.jqDialog.dialog("close");
		});
		
		// Affiche dialog de création d'un calendrier
		this.jqDialog.dialog({
			autoOpen: false,
			width: 440,
			modal: true,
			show: {
				effect: "fade",
				duration: 200
			},
			hide: {
				effect: "explode",
				duration: 200
			},
			close: function(event, ui) {
				me.reinitialiserProprietaires();
				// on vide la case "nom"
				me.nom.val('');
			}
		});

		this.initAppele = true;
	};



	/**
	 * Méthode qui remplit les comboboxes contenant matieres et types
	 */
	DialogCreationCalendrier.prototype.remplirComboboxes = function() {

		var me = this;
		// Récupération des données 
		this.restManager.effectuerRequete("GET", "matieresettypes", {
			token: this.restManager.getToken()
		}, function(data) {
			if (data.resultCode == RestManager.resultCode_Success) {
				
				// Remplir combobox contenant les matieres
				var matieres = '<option value=""> Aucune </option>' ;
				var maxI = data.data.matieres.length; 
				for (var i = 0 ; i < maxI ; i++) {
					var nomMatiere = data.data.matieres[i];
					matieres += '<option value="' + nomMatiere + '">' 
							  + nomMatiere
					          + "</option>" ;
				}
				me.jqDialog.find("#form_creer_calendrier_matiere").html(matieres);
				
				// Remplir combobox contenant les types
				var types = '<option value=""> Aucun </option>' ;
				maxI = data.data.types.length;
				for (var i = 0 ; i < maxI ; i++) {
					var nomType = data.data.types[i];
					types += '<option value="' + nomType + '">' 
						   + nomType 
						   + "</option>" ;
				}
				me.jqDialog.find("#form_creer_calendrier_type").html(types);
	
			} else if (data.resultCode == RestManager.resultCode_NetworkError) {
				window.showToast("Erreur de récupération des matières/types ; vérifiez votre connexion.");
			} else {
				window.showToast("Erreur" + data.resultCode + ". La récupération des matières/types a échoué ; votre session a peut-être expiré ?");
			}
		});

	};
	
	
	/**
	 * Méthode qui reinitialise le contrôle "proprietaires"
	 */
	DialogCreationCalendrier.prototype.reinitialiserProprietaires = function() {
		
		if(this.multiWidgetProprietaires != null) {
			this.multiWidgetProprietaires.clear();
		}
	};
	
	/**
	 * Méthode qui vérifie que le formulaire est remplit correctement
	 * 
	 * @return VRAI si le formulaire est valide et FAUX sinon
	 */
	DialogCreationCalendrier.prototype.validationFormulaire = function() {
		var valid = true;

		// Nom du calendrier non nul ?
		if (this.nom.val()=="") {
			this.nom.css({border: "1px solid red"});
			valid = false;
		} else {
			this.nom.css({border: "1px solid black"});
		}

		return valid;
	};
	
	
	/**
	 * Méthode qui effectue la requête de création du calendrier
	 */
	DialogCreationCalendrier.prototype.effectuerRequeteCreation = function() {
		var me = this;
		
		// Si le formulaire est valide, la requête est effectuée
		if (me.validationFormulaire()) {

			//Récupérer nom, matiere et type
			var nom = this.nom.val();
			var matiere = this.matiere.val();
			var type = this.type.val();
			
			var idProprietaires = me.multiWidgetProprietaires.val();
			var idProprietairesJson = JSON.stringify(idProprietaires);
				
			this.calendrierGestion.creerCalendrier(nom, matiere, type, idProprietairesJson, function(resultCode) {
				if(resultCode == RestManager.resultCode_Success) {
					// afficher message
					window.showToast("Le calendrier a bien été créé");
					// recharger les calendriers de l'utilisateur
					me.ecranParametres.afficheListeMesCalendriers();
				}
				else {
					// afficher message
					window.showToast("Erreur lors de la création du calendrier");
				}
			});
		}
	};


	/**
	 * Remplir la liste des utilisateurs potentiellement propriétaires
	 * 
	 * @param callback
	 */
	DialogCreationCalendrier.prototype.recupererProprietairesPotentiels = function(callback) {
		var me = this;
		
		// Récupération de la liste des propriétaires potentiels
		this.restManager.effectuerRequete("POST", "proprietairespotentiels", {
			token: this.restManager.getToken()
		}, function(data) {
			if (data.resultCode == RestManager.resultCode_Success) {
				
				// Création du tableau des valeurs pour le plugin Autocomplete de Jquery UI
				var utilisateurs = data.data.listeUtilisateurs;
				for (var i=0, maxI=utilisateurs.length; i<maxI; i++) {
					var label_value = {
							label: utilisateurs[i].prenom + " " + utilisateurs[i].nom + " (" + (utilisateurs[i].email != null ? utilisateurs[i].email : "email inconnu") + ")",
							value: utilisateurs[i].id
					};
					
					me.listeProprietairesPotentiels.push(label_value);
				}
				
				// Appelle la méthode de retour
				callback(true, utilisateurs.length);	
			} else if (data.resultCode == RestManager.resultCode_NetworkError) {
				window.showToast("Erreur de récupération de la liste des utilisateurs potentiellement propriétaires ; vérifiez votre connexion.");
				callback(false, 0);
			} else {
				window.showToast(data.resultCode + " Erreur de récupération de la liste des utilisateurs potentiellement propriétaires ; votre session a peut-être expiré ?");
				callback(false, 0);
			}
		});

	};

	return DialogCreationCalendrier;

});
