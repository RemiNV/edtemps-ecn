/**
 * @module DialogCreationGroupeParticipants
 */
define([ "RestManager", "multiwidget" ], function(RestManager, MultiWidget) {

	/**
	 * @constructor
	 * @alias module:DialogCreationGroupeParticipants
	 */
	var DialogCreationGroupeParticipants = function(restManager) {
		this.restManager = restManager;
		
		// Liens vers les objets javascript
		this.jqCreationGroupeForm = $("#form_creer_groupe");
		this.jqChampNom = this.jqCreationGroupeForm.find("#form_creer_groupe_nom");
		this.jqCreationGroupeTable = this.jqCreationGroupeForm.find("#form_creer_groupe_table");

		// Liste des propriétaires potentiels (récupérée en base de données)
		this.listeProprietairesPotentiels = new Array();

		// Est-ce que la fonction init a déjà été appelée ?
		this.initAppele = false;
		
		// Méthode appelée à l'appui sur le bouton principal de la dialogue (créer / modifier)
		this.callback = function() {};
		
		// Identifiant du groupe en cours de modification (-1 si c'est un ajout)
		this.idGroupeModification = -1;
		
		// MultiWidget pour les propriétaires
		this.proprietaireWidget = null;
		
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

		// Charge la liste des groupes parents disponibles
		this.ecritListeGroupesParentsDisponibles(me.jqCreationGroupeForm.find("#form_creer_groupe_parent"));

		// Récupération des propriétaires potentiels
		this.recupererProprietairesPotentiels(function (success) {
			if (success) {
				// Si un objet est passé en paramètre, la dialogue est pré-remplie avec les informations
				if (groupe) {
					me.preRemplirDialog(groupe);
				}
				
				// Réactivation du bouton "Ajouter"/"Modifier"
				me.jqCreationGroupeForm.find("#form_creation_groupe_ajouter").removeAttr("disabled");
				me.jqCreationGroupeForm.find("#form_creer_groupe_chargement").css("display", "none");
			}
		});
		
		// Récupère la méthode de callback de la dialogue
		this.callback = callback;
		
		// Ouvre la boîte de dialogue
		this.jqCreationGroupeForm.dialog("open");
	};
	
	
	/**
	 * Initialise la boîte de dialogue de création d'un groupe de participants (uniquement la première fois)
	 */
	DialogCreationGroupeParticipants.prototype.init = function() {
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

		// Affiche la boîte dialogue
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
	 * Ecrit la liste des groupes parents potentiels dans le select dédié
	 * @param object Objet jquery où il faut écrire la liste des groupes
	 */
	DialogCreationGroupeParticipants.prototype.ecritListeGroupesParentsDisponibles = function(object) {
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

			} else if (data.resultCode == RestManager.resultCode_NetworkError) {
				window.showToast("Erreur de récupération de la liste des groupes parents disponibles ; vérifiez votre connexion.");
			} else {
				window.showToast(data.resultCode + " Erreur de récupération de la liste des groupes parents disponibles ; votre session a peut-être expiré ?");
			}
		});

	};

	/**
	 * Valide le formulaire
	 * @return VRAI si le formulaire est valide, FAUX sinon
	 */
	DialogCreationGroupeParticipants.prototype.validationFormulaire = function() {
		if (this.jqChampNom.val()=="") {
			this.jqChampNom.css("box-shadow", "#FF0000 0 0 10px");
			this.jqChampNom.css("border", "1px solid #FF0000");
			return false;
		} else {
			this.jqChampNom.css("box-shadow", "#60C003 0 0 10px");
			this.jqChampNom.css("border", "1px solid #60C003");
			return true;
		}
	};

	/**
	 * Méthode qui ajoute un groupe en base de données
	 * @param nom Nom du groupe
	 * @param idGroupe Identifiant du groupe parent
	 * @param rattachementAutorise Booleen qui indique si un rattachement est autorisé
	 * @param estCours Booleen qui indique si le groupe est un cours
	 * @param listeIdProprietaires Liste des identifiants des propriétaires
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
	 * @param id Idenfitiant du groupe à modifier
	 * @param nom Nom du groupe
	 * @param idGroupe Identifiant du groupe parent
	 * @param rattachementAutorise Booleen qui indique si un rattachement est autorisé
	 * @param estCours Booleen qui indique si le groupe est un cours
	 * @param listeIdProprietaires Liste des identifiants des propriétaires
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

				// Widget pour les propriétaires
				me.proprietaireWidget = new MultiWidget(
						me.jqCreationGroupeTable.find("#form_creer_groupe_proprietaire"),
						MultiWidget.AUTOCOMPLETE_VAL, 
						MultiWidget.AUTOCOMPLETE_INIT(me.listeProprietairesPotentiels, 1),
						{ label: "Vous-même", value: localStorage["userId"] });

				// Appelle la méthode de retour
				callback(true);
			} else if (data.resultCode == RestManager.resultCode_NetworkError) {
				window.showToast("Erreur de récupération de la liste des utilisateurs potentiellement propriétaires ; vérifiez votre connexion.");
				callback(false);
			} else {
				window.showToast(data.resultCode + " Erreur de récupération de la liste des utilisateurs potentiellement propriétaires ; votre session a peut-être expiré ?");
				callback(false);
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
		this.jqCreationGroupeForm.find("#form_creer_groupe_proprietaire_td").html("<input type='text' id='form_creer_groupe_proprietaire' name='form_creer_groupe_proprietaire' />");
		
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
