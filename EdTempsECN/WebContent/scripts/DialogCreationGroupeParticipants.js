/**
 * Module de contrôle de la boîte de dialogue d'ajout de groupe de participants
 * @module DialogCreationGroupeParticipants
 */
define([ "RestManager", "MultiWidget", "UtilisateurGestion" ], function(RestManager, MultiWidget, UtilisateurGestion) {

	/**
	 * @constructor
	 * @alias module:DialogCreationGroupeParticipants
	 */
	var DialogCreationGroupeParticipants = function(restManager, jqCreationGroupeForm) {
		this.restManager = restManager;
		this.utilisateurGestion = new UtilisateurGestion(this.restManager);

		// Liens vers les objets javascript
		this.jqCreationGroupeForm = jqCreationGroupeForm;
		this.jqChampNom = this.jqCreationGroupeForm.find("#form_creer_groupe_nom");

		// Est-ce que la fonction init a déjà été appelée ?
		this.initAppele = false;

		// Méthode appelée à l'appui sur le bouton principal de la dialogue (créer / modifier)
		this.callback = null;

		// Identifiant du groupe en cours de modification (-1 si c'est un ajout)
		this.idGroupeModification = -1;
		
		// MultiWidget pour les propriétaires
		this.multiWidgetProprietaires = null;
	};

	/**
	 * Affiche la boîte de dialogue de création d'un groupe de participants
	 * 
	 * @param {string} titre Titre de la boîte de dialogue
	 * @param {string} texteBouton Texte du bouton de validation de la boîte de dialogue
	 * @param {groupe} groupe Groupe de participants dans le cas de la modification, null sinon
	 * @param {function} callback Méthode à exécuter lorsque du clique sur le bouton de validation de la boîte de dialogue
	 */
	DialogCreationGroupeParticipants.prototype.show = function(titre, texteBouton, groupe, callback) {
		if (!this.initAppele) {
			this.init(titre, texteBouton, groupe, callback);
		} else {
			var me=this;
			
			// Blocage du bouton de validation pendant le chargement de la boîte de dialogue
			this.jqCreationGroupeForm.find("#form_creation_groupe_ajouter").attr("disabled", "disabled");
			this.jqCreationGroupeForm.find("#form_creer_groupe_chargement").css("display", "block");
			this.jqCreationGroupeForm.find("#form_creer_groupe_message_chargement").html("Chargement en cours ...");
			
			// Ecriture du titre de la boîte de dialogue et du nom du bouton d'action principale
			this.jqCreationGroupeForm.dialog("option", "title", titre);
			this.jqCreationGroupeForm.find("#form_creation_groupe_ajouter").attr("value", texteBouton);

			// Si l'utilisateur n'a pas le droit de créer un groupe de cours, la case à cocher est désactivée
			if (!this.restManager.aDroit(RestManager.actionsEdtemps_CreerGroupeCours)) {
				this.jqCreationGroupeForm.find("#form_creer_groupe_cours").parents("tr").hide();
				this.jqCreationGroupeForm.find("#form_creer_groupe_cours").prop("checked", false);
			}

			// Charge la liste des groupes parents disponibles
			this.ecritListeGroupesParentsDisponibles(me.jqCreationGroupeForm.find("#form_creer_groupe_parent"), groupe, function(success) {
				if (success) {
					// Réactivation du bouton "Ajouter"/"Modifier"
					me.jqCreationGroupeForm.find("#form_creation_groupe_ajouter").removeAttr("disabled");
					me.jqCreationGroupeForm.find("#form_creer_groupe_chargement").css("display", "none");
				}
				
				// Pré-rempli la boite de dialogue
				me.preRemplirDialog(groupe);
			});

			// Récupère la méthode de callback de la dialogue
			this.callback = callback;
			
			// Ouvre la boîte de dialogue
			this.jqCreationGroupeForm.dialog("open");
		}
	};

	
	/**
	 * Initialise la boîte de dialogue de création d'un groupe de participants (uniquement la première fois)
	 * 
	 * @param {string} titre Titre de la boîte de dialogue
	 * @param {string} texteBouton Texte du bouton de validation de la boîte de dialogue
	 * @param {groupe} groupe Groupe de participants dans le cas de la modification, null sinon
	 * @param {function} callback Méthode à exécuter lorsque du clique sur le bouton de validation de la boîte de dialogue
	 */
	DialogCreationGroupeParticipants.prototype.init = function(titre, texteBouton, groupe, callback) {
		var me = this;

		// Affectation d'une méthode au clic sur le bouton "Annuler"
		this.jqCreationGroupeForm.find("#form_creer_groupe_annuler").click(function() {
			me.jqCreationGroupeForm.dialog("close");
		});
		
		// Affectation d'une méthode au clic sur le bouton d'action principale (créer/modifier)
		this.jqCreationGroupeForm.find("#form_creation_groupe_ajouter").click(function() {
			if (me.validationFormulaire()) {
				me.callback();
			} else {
				window.showToast("Veuillez vérifier et corriger les champs entourés en rouge");
			}
		});
		
		// Prépare la boîte dialogue
		this.jqCreationGroupeForm.dialog({
			autoOpen: false,
			appendTo: "#dialog_hook",
			width: 500,
			modal: true,
			show: {
				effect: "fade",
				duration: 200
			},
			hide: {
				effect: "explode",
				duration: 200
			},
			beforeClose: function(event, ui) {
				me.reinitialiserDialog();
			}
		});

		// Récupération des propriétaires potentiels
		this.recupererProprietairesPotentiels(function () {
			me.initAppele = true;
			
			// Rappelle la méthode show pour afficher la boite de dialogue
			me.show(titre, texteBouton, groupe, callback);
		});
	};


	/**
	 * Ecrit la liste des groupes parents potentiels dans le select dédié
	 * 
	 * @param {object} object Objet jquery où il faut écrire la liste des groupes
	 * @param {groupe} groupe Groupe de participants dans le cas de la modification, null sinon
	 * @param {function} callback
	 */
	DialogCreationGroupeParticipants.prototype.ecritListeGroupesParentsDisponibles = function(object, groupe, callback) {
		var me = this;
		
		// Récupération de la liste des groupes parents potentiels
		var params = { token: this.restManager.getToken() };
		if(groupe) {
			params.idGroupeIgnorerEnfants = groupe.id;
		}
		
		this.restManager.effectuerRequete("GET", "groupesparentspotentiels", params, function(data) {
			if (data.resultCode == RestManager.resultCode_Success) {

				var maxI = data.data.listeGroupes.length;

				$(object).html("");
				if (maxI>0) {
					var str = "<option value='-1'>---</option>";
					for (var i=0; i<maxI; i++) {
						str += "<option value='"+data.data.listeGroupes[i].id+"'>"+data.data.listeGroupes[i].nom+"</option>";
					}
					$(object).append(str);
				} else {
					$(object).append("<option value='-1'>---</option>");
					me.jqCreationGroupeForm.find("#form_creer_groupe_parent_message").html("Aucun rattachement possible").show();
					$(object).attr("disabled", "disabled");
				}

				callback(true);
			} else if (data.resultCode == RestManager.resultCode_NetworkError) {
				window.showToast("Erreur de récupération de la liste des groupes parents disponibles ; vérifiez votre connexion.");
				callback(false);
			} else {
				window.showToast(data.resultCode + " Erreur de récupération de la liste des groupes parents disponibles ; votre session a peut-être expiré ?");
				callback(false);
			}
		});

	};

	/**
	 * Valide le formulaire
	 * 
	 * @return {boolean} VRAI si le formulaire est valide, FAUX sinon
	 */
	DialogCreationGroupeParticipants.prototype.validationFormulaire = function() {
		var correct = true;
		if (this.jqChampNom.val()=="") {
			this.jqChampNom.css("box-shadow", "#FF0000 0 0 10px");
			this.jqChampNom.css("border", "1px solid #FF0000");
			this.jqChampNom.attr("title", "Le nom du groupe doit être spécifié.");
			correct = false;
		} else if (!/^[a-z \u00C0-\u00FF0-9]+$/i.test(this.jqChampNom.val())) {
			this.jqChampNom.css("box-shadow", "#FF0000 0 0 10px");
			this.jqChampNom.css("border", "1px solid #FF0000");
			this.jqChampNom.attr("title", "Le nom du groupe ne doit contenir que des caractères alphanumériques et des espaces");
			correct = false;
		} else {
			this.jqChampNom.css("box-shadow", "#60C003 0 0 10px");
			this.jqChampNom.css("border", "1px solid #60C003");
			this.jqChampNom.attr("title", "");
		}
		
		return correct;
	};

	/**
	 * Méthode qui ajoute un groupe en base de données
	 * 
	 * @param {string} nom Nom du groupe
	 * @param {number} idGroupe Identifiant du groupe parent
	 * @param {boolean} rattachementAutorise Booleen qui indique si un rattachement est autorisé
	 * @param {boolean} estCours Booleen qui indique si le groupe est un cours
	 * @param {number[]} listeIdProprietaires Liste des identifiants des propriétaires
	 * @param {function} callback Méthode appelé en cas de réussite
	 */
	DialogCreationGroupeParticipants.prototype.ajouterGroupe = function(nom, idGroupeParent, rattachementAutorise, estCours, listeIdProprietaires, callback) {
		var me = this;

		// Bloque le bouton d'ajout et affiche un message de chargement
		this.jqCreationGroupeForm.find("#form_creation_groupe_ajouter").attr("disabled", "disabled");
		this.jqCreationGroupeForm.find("#form_creer_groupe_chargement").css("display", "block");
		this.jqCreationGroupeForm.find("#form_creer_groupe_message_chargement").html("Traitement en cours ...");

		this.restManager.effectuerRequete("POST", "groupeparticipants/ajouter", {
			token: this.restManager.getToken(),
			groupe: JSON.stringify({
				nom: nom,
				idGroupeParent: idGroupeParent,
				rattachementAutorise: rattachementAutorise,
				estCours: estCours,
				proprietaires: listeIdProprietaires
			})
		}, function (response) {
			me.jqCreationGroupeForm.find("#form_creation_groupe_ajouter").removeAttr("disabled");
			me.jqCreationGroupeForm.find("#form_creer_groupe_chargement").css("display", "none");
			
			if (response.resultCode == RestManager.resultCode_Success) {
				window.showToast("Le groupe de participant à été créé avec succès.");
				me.jqCreationGroupeForm.dialog("close");
				callback();
			} else if (response.resultCode == RestManager.resultCode_QuotaExceeded) {
				window.showToast("Vous avez atteint votre quota de création de groupes de participants");
			} else if (response.resultCode == RestManager.resultCode_AuthorizationError) {
				window.showToast("Vous n'êtes pas autorisé a effectuer cette action");
			} else if (response.resultCode == RestManager.resultCode_AlphanumericRequired) {
				window.showToast("Le nom du groupe ne doit comporter que des caractères alphanumériques et des espaces");
			} else if (response.resultCode == RestManager.resultCode_NetworkError) {
				window.showToast("Erreur lors de la création du groupe de participants ; vérifiez votre connexion");
			} else if (response.resultCode == RestManager.resultCode_NameTaken) {
				window.showToast("Le nom du groupe est déjà utilisé, veuillez en choisir un autre");
			} else {
				window.showToast(response.resultCode + " Erreur lors de la création du groupe de participants ; votre session a peut-être expiré ?");
			}
		});
		
	};
	
	/**
	 * Méthode qui modifie un groupe en base de données
	 * 
	 * @param {number} id Idenfitiant du groupe à modifier
	 * @param {string} nom Nom du groupe
	 * @param {number} idGroupe Identifiant du groupe parent
	 * @param {boolean} rattachementAutorise Booleen qui indique si un rattachement est autorisé
	 * @param {boolean} estCours Booleen qui indique si le groupe est un cours
	 * @param {number[]} listeIdProprietaires Liste des identifiants des propriétaires
	 * @param {function} callback Méthode appelé en cas de réussite
	 */
	DialogCreationGroupeParticipants.prototype.modifierGroupe = function(id, nom, idGroupeParent, rattachementAutorise, estCours, listeIdProprietaires, callback) {
		var me = this;

		// Bloque le bouton de modification et affiche un message de chargement
		this.jqCreationGroupeForm.find("#form_creation_groupe_ajouter").attr("disabled", "disabled");
		this.jqCreationGroupeForm.find("#form_creer_groupe_chargement").css("display", "block");
		this.jqCreationGroupeForm.find("#form_creer_groupe_message_chargement").html("Traitement en cours ...");

		this.restManager.effectuerRequete("POST", "groupeparticipants/modifier", {
			token: this.restManager.getToken(),
			groupe: JSON.stringify({
				id: id,
				nom: nom,
				idGroupeParent: idGroupeParent,
				rattachementAutorise: rattachementAutorise,
				estCours: estCours,
				proprietaires: listeIdProprietaires
			})
		}, function (response) {
			me.jqCreationGroupeForm.find("#form_creation_groupe_ajouter").removeAttr("disabled");
			me.jqCreationGroupeForm.find("#form_creer_groupe_chargement").css("display", "none");
			
			if (response.resultCode == RestManager.resultCode_Success) {
				window.showToast("Le groupe de participant à été modifié avec succès.");
				me.jqCreationGroupeForm.dialog("close");
				callback();
			} else if (response.resultCode == RestManager.resultCode_AuthorizationError) {
				window.showToast("Vous n'êtes pas autorisé a effectuer cette action");
			} else if (response.resultCode == RestManager.resultCode_AlphanumericRequired) {
				window.showToast("Le nom du groupe ne doit comporter que des caractères alphanumériques et des espaces");
			} else if (response.resultCode == RestManager.resultCode_NetworkError) {
				window.showToast("Erreur lors de la modification du groupe de participants ; vérifiez votre connexion.");
			} else if (response.resultCode == RestManager.resultCode_NameTaken) {
				window.showToast("Le nom du groupe est déjà utilisé, veuillez en choisir en un autre.");
			} else {
				window.showToast(response.resultCode + " Erreur lors de la modification du groupe de participants ; votre session a peut-être expiré ?");
			}
		});
		
	};
	
	/**
	 * Remplir la liste des utilisateurs potentiellement propriétaires
	 * 
	 * @param {function} callback
	 */
	DialogCreationGroupeParticipants.prototype.recupererProprietairesPotentiels = function(callback) {
		var me = this;

		this.utilisateurGestion.recupererProprietairesPotentielsAutocomplete(function(resultCode, utilisateurs) {
			if (resultCode == RestManager.resultCode_Success) {
				me.multiWidgetProprietaires = new MultiWidget(
						me.jqCreationGroupeForm.find("#form_creer_groupe_proprietaire"), 
						MultiWidget.AUTOCOMPLETE_OPTIONS(utilisateurs, 3, 230));
				
				callback();
			} else if (data.resultCode == RestManager.resultCode_NetworkError) {
				window.showToast("Erreur de récupération de la liste des utilisateurs potentiellement propriétaires ; vérifiez votre connexion.");
			} else {
				window.showToast(data.resultCode + " Erreur de récupération de la liste des utilisateurs potentiellement propriétaires ; votre session a peut-être expiré ?");
			}
		});

	};

	/**
	 * Réinitialiser la boîte de dialogue
	 */
	DialogCreationGroupeParticipants.prototype.reinitialiserDialog = function() {

		// Vide les champs
		this.jqChampNom.val("");
		this.jqCreationGroupeForm.find("#form_creer_groupe_parent").val(-1);
		this.jqCreationGroupeForm.find("#form_creer_groupe_rattachement").prop("checked", false);
		this.jqCreationGroupeForm.find("#form_creer_groupe_cours").prop("checked", false);
		if (this.multiWidgetProprietaires != null) {
			this.multiWidgetProprietaires.clear();
		}

		// Enlève les bordures sur le champ nom
		this.jqChampNom.css("box-shadow", "transparent 0 0 10px");
		this.jqChampNom.css("border", "1px solid black");
	};
	
	
	/**
	 * Pré-remplie la boîte de dialogue avec le groupe 
	 * @param {groupe} groupe Groupe de participant à pré-remplir
	 */
	DialogCreationGroupeParticipants.prototype.preRemplirDialog = function(groupe) {
		
		if (groupe!=null) {
			// Identifiant du groupe
			this.idGroupeModification = groupe.id;

			// Nom du groupe
			this.jqChampNom.val(groupe.nom);
			
			// Sélection du groupe parent (s'il y en a un)
			if (groupe.parent) {
				this.jqCreationGroupeForm.find("#form_creer_groupe_parent option[value="+groupe.parent.id+"]").prop('selected', true);
			}
			
			// Checkbox pour l'autorisation du rattachement
			this.jqCreationGroupeForm.find("#form_creer_groupe_rattachement").prop("checked", groupe.rattachementAutorise);

			// Checkbox pour les groupes de cours
			this.jqCreationGroupeForm.find("#form_creer_groupe_cours").prop("checked", groupe.estCours);
			
			// Sélection des propriétaires
			var listeProprietaires = UtilisateurGestion.makeUtilisateursAutocomplete(groupe.proprietaires);
			for (var i=0, maxI = listeProprietaires.length; i<maxI; i++) {
				if (listeProprietaires[i].value!=groupe.createur) {
					listeProprietaires[i].readOnly = false;
				} else {
					listeProprietaires.splice(i, 1);
				}
			}
			listeProprietaires.unshift({ label: "Vous-même", value: this.restManager.getUserId(), readOnly: true });
			
			this.multiWidgetProprietaires.setValues(listeProprietaires);

		} else {
			this.multiWidgetProprietaires.setValues([{ label: "Vous-même", value: this.restManager.getUserId(), readOnly: true }]);
		}

	};
	
	return DialogCreationGroupeParticipants;
});
