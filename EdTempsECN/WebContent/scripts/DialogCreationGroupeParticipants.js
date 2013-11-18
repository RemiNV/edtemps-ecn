/**
 * @module DialogCreationGroupeParticipants
 */
define([ "RestManager", "EcranParametres" ], function(RestManager) {

	/**
	 * @constructor
	 * @alias module:DialogCreationGroupeParticipants
	 */
	var DialogCreationGroupeParticipants = function(restManager, ecranParametres) {
		this.restManager = restManager;
		this.ecranParametres = ecranParametres;
		
		// Des liens vers les objets javascript
		this.jqCreationGroupeForm = $("#form_creer_groupe");
		this.jqChampNom = this.jqCreationGroupeForm.find("#form_creer_groupe_nom");
		this.jqCreationGroupeTable = this.jqCreationGroupeForm.find("#form_creer_groupe_table");

		// Liste des propriétaires potentiels (récupérée en base de données)
		this.listeProprietairesPotentiels = new Array();
		this.compteurProprietaires = 0;
		this.nomPrenomUtilisateurEnCours = "";		

		// Est-ce que la fonction init a déjà été appelée ?
		this.initAppele = false;
		
		// Méthode appelée à l'appui sur le bouton principal de la dialogue (créer / modifier)
		this.callback = null;
		
		// Identifiantdu groupe en cours de modification (-1 si c'est un ajout)
		this.idGroupeModification = -1;
	};

	/**
	 * Affiche la boîte de dialogue de création d'un groupe de participants
	 * @param titre : le titre de la boîte de dialogue
	 * @param texteBouton : le texte du bouton de validation de la boîte de dialogue
	 * @param groupe : groupe de participants dans le cas de la modification, null sinon
	 * @param callback : méthode à exécuter lorsque du clique sur le bouton de validation de la boîte de dialogue
	 */
	DialogCreationGroupeParticipants.prototype.show = function(titre, texteBouton, groupe, callback) {
		if(!this.initAppele) {
			this.init();
		}
		
		var me=this;
		
		// Blocage du bouton de validation pendant le chargement de la boîte de dialogue
		this.jqCreationGroupeForm.find("#form_creation_groupe_ajouter").attr("disabled", "disabled");
		this.jqCreationGroupeForm.find("#form_creer_groupe_chargement").css("display", "block");
		this.jqCreationGroupeForm.find("#form_creer_groupe_message_chargement").html("Chargement en cours ...");
		
		// Ecriture du titre de la boîte de dialogue et du nom du bouton d'action principale
		this.jqCreationGroupeForm.dialog("option", "title", titre);
		this.jqCreationGroupeForm.find("#form_creation_groupe_ajouter").attr("value", texteBouton);
		
		// Récupération des propriétaires potentiels
		this.recupererProprietairesPotentiels(function(success, nbUtilisateurs) {
			if (success) {
				
				// Charge la liste des groupes parents disponibles
				me.chargementListeGroupesParents(function() {
					// Si un objet est passé en paramètre, la fenêtre est pré-remplie avec les informations
					if (groupe) {
						me.preRemplirDialog(groupe);
					}
				});

				// Réactivation du bouton "Ajouter"/"Modifier"
				me.jqCreationGroupeForm.find("#form_creation_groupe_ajouter").removeAttr("disabled");
				me.jqCreationGroupeForm.find("#form_creer_groupe_chargement").css("display", "none");
			}
		});
		
		// Récupère la méthode de callback de la dialogue
		this.callback = callback;
		
		// Ouvre la boîte dialogue
		this.jqCreationGroupeForm.dialog("open");
	};
	
	
	/**
	 * Initialise la boîte de dialogue de création d'un groupe de participants
	 */
	DialogCreationGroupeParticipants.prototype.init = function() {
		var me = this;

		// Affectation d'une méthode au clic sur le bouton "Ajouter un propriétaire"
		this.jqCreationGroupeForm.find("#form_creer_groupe_proprietaire_ajouter").click(function() {
			me.ajouterLigneProprietaire();
		});

		// Affectation d'une méthode au clic sur le bouton "Annuler"
		this.jqCreationGroupeForm.find("#form_creer_groupe_annuler").click(function() {
			me.jqCreationGroupeForm.dialog("close");
		});
		
		// Affectation d'une méthode au clic sur le bouton d'action principale
		this.jqCreationGroupeForm.find("#form_creation_groupe_ajouter").click(function() {
			if (me.validationFormulaire()) {
				me.callback();
			} else {
				window.showToast("Veuillez vérifier et corriger les champs entourés en rouge");
			}
		});

		// Affiche la boîte dialogue de recherche d'une salle libre
		this.jqCreationGroupeForm.dialog({
			autoOpen: false,
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

		this.initAppele = true;
	};

	
	/**
	 * Met à jour la liste des groupes parents potentiels dans le select dédié
	 */
	DialogCreationGroupeParticipants.prototype.chargementListeGroupesParents = function(callback) {
		var me = this;
		
		// Blocage du bouton de validation avant le chargement des groupes parents disponibles
		this.jqCreationGroupeForm.find("#form_creation_groupe_ajouter").attr("disabled", "disabled");
		this.jqCreationGroupeForm.find("#form_creer_groupe_chargement").css("display", "block");
		this.jqCreationGroupeForm.find("#form_creer_groupe_message_chargement").html("Chargement des groupes parents potentiels en cours ...");

		// Récupération et écriture des groupes parents disponibles
		this.ecritListeGroupesParentsDisponibles(this.jqCreationGroupeForm.find("#form_creer_groupe_parent"), function(success) {
			if (success) {
				// Reactivation du bouton "Valider"
				me.jqCreationGroupeForm.find("#form_creation_groupe_ajouter").removeAttr("disabled");
				me.jqCreationGroupeForm.find("#form_creer_groupe_chargement").css("display", "none");
				
				callback();
			}
		});
	};
	

	/**
	 * Ecrit la liste des groupes parents potentiels dans le select dédié
	 *  
	 * @param object
	 * 			l'objet jquery où il faut écrire la liste des groupes
	 * @param callback
	 * 			fonction à appeler au retour avec l'état de la requête en paramètre
	 */
	DialogCreationGroupeParticipants.prototype.ecritListeGroupesParentsDisponibles = function(object, callback) {
		var me = this;
		
		// Récupération de la liste des groupes parents potentiels
		this.restManager.effectuerRequete("POST", "groupesparentspotentiels", {
			token: this.restManager.getToken()
		}, function(data) {
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
	 * @return VRAI si le formulaire est valide, FAUX sinon
	 */
	DialogCreationGroupeParticipants.prototype.validationFormulaire = function() {
		var valid = true;
		var me = this;
		
		// Validation du champ "Nom"
		if (this.jqChampNom.val()=="") {
			this.jqChampNom.css("box-shadow", "#FF0000 0 0 10px");
			this.jqChampNom.css("border", "1px solid #FF0000");
			valid = false;
		} else {
			this.jqChampNom.css("box-shadow", "#60C003 0 0 10px");
			this.jqChampNom.css("border", "1px solid #60C003");
		}
		
		// Vérifie les champs de propriétaires
		this.jqCreationGroupeTable.find(".form_creer_groupe_proprietaire_sup").each(function(index) {
			valid &= me.verifierValeurChampProprietaire($(this));
		});
		
		return valid;
	};


	/**
	 * Méthode qui ajoute un groupe en base de données
	 * 
	 * @param nom
	 * 			nom du groupe
	 * @param idGroupe
	 * 			identifiant du groupe parent
	 * @param rattachementAutorise
	 * 			booleen qui indique si un rattachement est autorisé
	 * @param estCours
	 * 			booleen qui indique si le groupe est un cours
	 * @param listeIdProprietaires
	 * 			liste des identifiants des propriétaires
	 */
	DialogCreationGroupeParticipants.prototype.ajouterGroupe = function(nom, idGroupeParent, rattachementAutorise, estCours, listeIdProprietaires) {
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
				me.ecranParametres.afficheListeMesGroupes();
				me.jqCreationGroupeForm.dialog("close");
			} else if (response.resultCode == RestManager.resultCode_NetworkError) {
				window.showToast("Erreur lors de la création du groupe de participants ; vérifiez votre connexion.");
			} else if (response.resultCode == RestManager.resultCode_NameTaken) {
				window.showToast("Le nom du groupe est déjà utilisé, veuillez en choisir un autre.");
			} else {
				window.showToast(response.resultCode + " Erreur lors de la création du groupe de participants ; votre session a peut-être expiré ?");
			}
		});
		
	};
	
	
	/**
	 * Méthode qui modifie un groupe en base de données
	 * 
	 * @param id
	 * 			idenfitiant du groupe à modifier
	 * @param nom
	 * 			nom du groupe
	 * @param idGroupe
	 * 			identifiant du groupe parent
	 * @param rattachementAutorise
	 * 			booleen qui indique si un rattachement est autorisé
	 * @param estCours
	 * 			booleen qui indique si le groupe est un cours
	 * @param listeIdProprietaires
	 * 			liste des identifiants des propriétaires
	 */
	DialogCreationGroupeParticipants.prototype.modifierGroupe = function(id, nom, idGroupeParent, rattachementAutorise, estCours, listeIdProprietaires) {
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
				me.ecranParametres.afficheListeMesGroupes();
				me.jqCreationGroupeForm.dialog("close");
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
	 * @param callback
	 */
	DialogCreationGroupeParticipants.prototype.recupererProprietairesPotentiels = function(callback) {
		var me = this;
		
		// Récupération de la liste des propriétaires potentiels
		this.restManager.effectuerRequete("POST", "proprietairespotentiels", {
			token: this.restManager.getToken()
		}, function(data) {
			if (data.resultCode == RestManager.resultCode_Success) {
				
				// Création du tableau des valeurs pour le plugin Autocomplete de Jquery UI
				for (var i=0, maxI=data.data.listeUtilisateurs.length; i<maxI; i++) {
					var user = new Object();
					user.label = data.data.listeUtilisateurs[i].prenom + " " + data.data.listeUtilisateurs[i].nom;
					user.id = data.data.listeUtilisateurs[i].id;
					me.listeProprietairesPotentiels.push(user);
				}

				// Appelle la méthode de retour
				callback(true, maxI);
			} else if (data.resultCode == RestManager.resultCode_NetworkError) {
				window.showToast("Erreur de récupération de la liste des utilisateurs potentiellement propriétaires ; vérifiez votre connexion.");
				callback(false, 0);
			} else {
				window.showToast(data.resultCode + " Erreur de récupération de la liste des utilisateurs potentiellement propriétaires ; votre session a peut-être expiré ?");
				callback(false, 0);
			}
		});

	};
	
	
	/**
	 * Affiche une ligne pour un propriétaire
	 */
	DialogCreationGroupeParticipants.prototype.ajouterLigneProprietaire = function() {
		var me = this;
		
		// Incrémente le compteur des propriétaires
		this.compteurProprietaires++;

		// Ajoute une ligne dans le tableau du formulaire
		this.jqCreationGroupeTable.append(
			"<tr>" +
				"<td><label>Propriétaire supplémentaire</label></td>" +
				"<td>" +
					"<input type='text' id='form_creer_groupe_proprietaire_sup_"+this.compteurProprietaires+"' class='form_creer_groupe_proprietaire_sup' />" +
					"<img alt='Supression' src='img/corbeille.png' title='Supprimer le propriétaire' class='form_creer_groupe_proprietaire_supprimer' />" +
				"</td>" +
			"</tr>");

		// Ajoute l'autocomplete sur le champ ajouté
		var champ = this.jqCreationGroupeTable.find("#form_creer_groupe_proprietaire_sup_"+this.compteurProprietaires);
		champ.autocomplete({
			source: this.listeProprietairesPotentiels,
			select: function (event, ui) { champ.attr( "value", ui.item.id ); }
		});
		champ.focusout(function() {
			me.verifierValeurChampProprietaire($(this));
		});
		
		// Listener sur le bouton de suppression du propriétaire
		this.jqCreationGroupeForm.find(".form_creer_groupe_proprietaire_supprimer").click(function() {
			$(this).parents("tr").remove();
		});

	};


	/**
	 * Vérifie qu'un champ propriétaire est correct (le nom est bien présent dans la liste des propriétaires potentiels
	 * @param object Champ à vérifier
	 * @returns VRAI si le champ est valide
	 */
	DialogCreationGroupeParticipants.prototype.verifierValeurChampProprietaire = function(object) {
		var valid = false;

		// Vérifie que le nom du propriétaire est correct
		for (var i=0, maxI=this.listeProprietairesPotentiels.length; i<maxI; i++) {
			if (this.listeProprietairesPotentiels[i].label == $(object).val() || $(object).val()=="") {
				valid = true;
				break;
			}
		}

		// Vérifie que le propriétaire n'est présent qu'une seule fois
		if (valid) {
			
			// Balaye tous les champs
			
			if ($(object).val()!="") {
				if (jQuery.inArray($(object).attr("value"), this.listeProprietairesSelectionnes)<0) {
					// Ajoute le propriétaire dans la liste des propriétaires sélectionnés
					this.listeProprietairesSelectionnes.push($(object).attr("value"));
				} else {
					$(object).attr("title", "Le nom d'utilisateur que vous avez saisi est déjà présent.");
					valid = false;
				}
			}
			
		} else {
			$(object).attr("title", "Le nom d'utilisateur que vous avez saisi est incorrect.");
		}

		// Modifie l'affichage
		if (valid) {
			// Enlève la bordure rouge
			$(object).css("box-shadow", "transparent 0 0 10px");
			$(object).css("border", "1px solid black");
			$(object).attr("title", "");
		} else {
			// Met une bordure rouge
			$(object).css("box-shadow", "red 0 0 10px");
			$(object).css("border", "1px solid red");
		}

		return valid;
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
		this.jqCreationGroupeTable.find(".form_creer_groupe_proprietaire_sup").each(function(index) {
			$(this).parents("tr").remove();
		});
		
		// Enlève les bordures sur le champ nom
		this.jqChampNom.css("box-shadow", "transparent 0 0 10px");
		this.jqChampNom.css("border", "1px solid black");
		
	};

	
	/**
	 * Pré-remplie la boîte de dialogue avec le groupe 
	 * @param groupe
	 */
	DialogCreationGroupeParticipants.prototype.preRemplirDialog = function(groupe) {
		
		// Identifiant du groupe
		this.idGroupeModification = groupe.id;

		// Nom du groupe
		this.jqChampNom.val(groupe.nom);
		
		// Sélection du groupe parent (s'il y en a un)
		if (groupe.parent) {
			this.jqCreationGroupeForm.find("#form_creer_groupe_parent").val(groupe.parent.id);
		}
		
		// Checkbox pour l'autorisation du rattachement
		this.jqCreationGroupeForm.find("#form_creer_groupe_rattachement").prop("checked", groupe.rattachementAutorise);

		// Checkbox pour les groupes de cours
		this.jqCreationGroupeForm.find("#form_creer_groupe_cours").prop("checked", groupe.estCours);
		
		// Sélection des propriétaires
/*		for (var i=0, maxI=groupe.proprietaires.length; i<maxI; i++) {
			if (groupe.proprietaires[i].id!=window.localStorage["userId"]) {
				this.ajouterLigneProprietaire(groupe.proprietaires[i].prenom+" "+groupe.proprietaires[i].nom, false);
			}
		}*/
	};
	
	return DialogCreationGroupeParticipants;
});
