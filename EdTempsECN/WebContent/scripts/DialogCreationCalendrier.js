/**
 * @module DialogCreationCalendrier
 */
define([ "RestManager", "CalendrierGestion", "MultiWidget", "UtilisateurGestion", "jquerymaskedinput" ], function(RestManager, CalendrierGestion, MultiWidget, UtilisateurGestion) {

	/**
	 * @constructor
	 * @alias module:DialogCreationCalendrier
	 */
	var DialogCreationCalendrier = function(restManager, ecranParametres, jqDialog) {
		this.restManager = restManager;
		this.ecranParametres = ecranParametres;
		this.calendrierGestion = new CalendrierGestion(this.restManager);
		this.utilisateurGestion = new UtilisateurGestion(this.restManager);
		this.jqDialog = jqDialog;
		// Accès direct aux champs du formulaire
		this.nom = jqDialog.find("#form_creer_calendrier_nom");
		this.type = jqDialog.find("#form_creer_calendrier_type");
		this.matiere = jqDialog.find("#form_creer_calendrier_matiere");
		// Liste des propriétaires potentiels = de tous les utilisateurs
		this.listeProprietairesPotentiels = new Array(); //pour l'autocomplete JqueryUI
		this.listeProprietairesPotentielsIndex = new Array(); //pour compléter les autocomplete
		this.listeGroupesParents = new Array();
		this.initAppele = false;
		this.multiWidgetProprietaires = null;
	};

	
	/**
	 * Initialise la boîte de dialogue de création d'un calendrier
	 */
	DialogCreationCalendrier.prototype.init = function(matiere, type, proprietaires, groupesParents) {
		var me = this;

		// Remplir les combobox matieres et types
		me.remplirComboboxes(function(success) {
			if (success) {
				// On initialise les champs aux valeurs demandés
				me.matiere.val(matiere); 
				me.type.val(type);
			}
		});
		
		// Récupérer propriétaires potentiels et remplir les proprietaires du calendrier à modifier (si modification)
		this.utilisateurGestion.recupererProprietairesPotentielsAutocomplete(function(resultCode, utilisateurs) {
			if (resultCode == RestManager.resultCode_Success) {
				// Enregistrement des utilisateurs potentiels récupérés dans une variable, pour accès futur
				me.listeProprietairesPotentiels = utilisateurs;
				// Création d'un autre tableau des proprietaires potentiels, indexé par id_utilisateur
				for (var i=0, maxI=utilisateurs.length; i<maxI; i++) {
					var user = new Object();
					user.label = utilisateurs[i].label;
					user.tooltip = utilisateurs[i].tooltip;
					me.listeProprietairesPotentielsIndex[utilisateurs[i].value] = user;
				}
				// Création des autocomplete pour les propriétaires
				me.multiWidgetProprietaires = new MultiWidget(me.jqDialog.find("#form_creer_calendrier_input_proprietaire"), 
						MultiWidget.AUTOCOMPLETE_OPTIONS(me.listeProprietairesPotentiels, 3, 225));
				// On remplir les proprietaires (dans le cas d'une modification de calendrier)
				if (proprietaires.length != 0) {
					me.remplirProprietaires(proprietaires);
				}
				else {
					me.multiWidgetProprietaires.setValues([{ label: "Vous-même", value: me.restManager.getUserId(), readOnly: true }]);
				}
				
			} else if (data.resultCode == RestManager.resultCode_NetworkError) {
				window.showToast("Erreur de récupération de la liste des utilisateurs potentiellement propriétaires ; vérifiez votre connexion.");
			} else {
				window.showToast(data.resultCode + " Erreur de récupération de la liste des utilisateurs potentiellement propriétaires ; votre session a peut-être expiré ?");
			}
		});
		
		// Remplir les groupes parents potentiels
		me.remplirGroupesParents(groupesParents);
		
		// Listener bouton "Annuler"
		this.jqDialog.find("#form_creer_calendrier_annuler").click(function() {
			me.jqDialog.dialog("close");
		});
		
		// Crée la boite de dialog de création/modification d'un calendrier
		this.jqDialog.dialog({
			autoOpen: false,
			width: 500,
			modal: true,
			show: {
				effect: "fade",
				duration: 200
			},
			hide: {
				effect: "fade",
				duration: 200
			},
			close: function(event, ui) {
				// on vide la case "nom" et reinitialise les comboboxes Matiere/Type
				me.nom.val("");
				me.matiere.val("");
				me.type.val("");
				// on réinitialise les propriétaires	
				if(me.multiWidgetProprietaires != null) {
					me.multiWidgetProprietaires.clear();
				}
				// on ne réinitialise pas les groupes parents : ils le seront à la réouverture du dialog
				// on désactive le bouton valider
				me.jqDialog.find("#form_creer_calendrier_valider").unbind( "click" );
			}
		});
		
	};
		
		

	/**
	 * Affiche la boîte de dialogue de création/modification d'un calendrier
	 * 
	 * @param casModifier : vrai si modification d'un calendrier / faux si création d'un calendrier
	 * @param calendrierAModifier : objet contenant les informations du calendrier à modifier (uniquement si casModifier = true)
	 * 
	 */
	DialogCreationCalendrier.prototype.show = function(casModifier, calendrierAModifier) {
		var me = this;	
		var idCal = -1; //id du calendrier, dans le cas d'une modification (= -1 dans le cas d'une création)
		
		// CAS MODIFICATION d'un calendrier, on affiche les infos de celui-ci
		if (casModifier) {
			// Si pas déjà fait, initialiser la boite de dialogue (listeners, recuperation matiere/type/proprio) 
			if(!this.initAppele) {
				this.init(calendrierAModifier.matiere, calendrierAModifier.type, calendrierAModifier.proprietaires, calendrierAModifier.groupesParents); 
				this.initAppele = true;
			}
			else {
				// On met affiche la matiere / le type du calendrier
				this.matiere.val(calendrierAModifier.matiere); 
				this.type.val(calendrierAModifier.type);
				// On remplit les proprietaires
				this.remplirProprietaires(calendrierAModifier.proprietaires);
				// On remplit les groupes parents
				this.remplirGroupesParents(calendrierAModifier.groupesParents);
			}
			// Ecriture du titre de la boîte de dialogue et du nom du bouton d'action principale
			this.jqDialog.dialog("option", "title", "Modifier le calendrier");
			this.jqDialog.find("#form_creer_calendrier_valider").attr("value", "Modifier");
			// On affiche le nom du calendrier dans la case Nom
			this.nom.val(calendrierAModifier.nom);
			// Récupérer l'ID du calendrier à modifier
			idCal = calendrierAModifier.id;
		}	
		// CAS CREATION d'un calendrier
		else {
			// Si pas déjà fait, initialiser la boite de dialogue (listener, recuperation matiere/type/proprio) 
			if(!this.initAppele) {
				this.init("","", new Array(), new Array()); //Matière = Aucune, Type = Aucun, pas de propriétaires et groupes parents
				this.initAppele = true;
			}
			else {
				// Mise à jour des groupes parents potentiels
				this.remplirGroupesParents(new Array());
			}
			// Ecriture du titre de la boîte de dialogue et du nom du bouton d'action principale
			this.jqDialog.dialog("option", "title", "Création d'un nouveau calendrier");
			this.jqDialog.find("#form_creer_calendrier_valider").attr("value", "Créer");
			
			if(me.multiWidgetProprietaires) {
				me.multiWidgetProprietaires.setValues([{ label: "Vous-même", value: me.restManager.getUserId(), readOnly: true }]);
			}
		}
		
		// Listener bouton "Valider"
		this.jqDialog.find("#form_creer_calendrier_valider").click(function() {
			me.effectuerRequeteCreationModification(casModifier, idCal);
		});
		
		// Ouverture de la boîte dialogue
		this.jqDialog.dialog("open");
	};



	/**
	 * Méthode qui remplit les comboboxes contenant matieres et types
	 */
	DialogCreationCalendrier.prototype.remplirComboboxes = function(callback) {

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
	
				// Appelle la méthode de retour
				callback(true);	
			} else if (data.resultCode == RestManager.resultCode_NetworkError) {
				window.showToast("Erreur de récupération des matières/types ; vérifiez votre connexion.");
				callback(false);	
			} else {
				window.showToast("Erreur" + data.resultCode + ". La récupération des matières/types a échoué ; votre session a peut-être expiré ?");
				callback(false);	
			}
		});

	};
	
	
	/**
	 * Méthode qui ajoute une ligne "groupe parent"
	 */
	DialogCreationCalendrier.prototype.ajouterGroupeParent = function() {
		var me = this;
		// on affiche une nouvelle liste déroulante de groupes parcipants potentiels
		var str = "<div>" + 
			       me.listeGroupesParents +
			      "<img src='img/corbeille.png' alt='Supprimer un parent' " +
			      "class='form_creer_calendrier_supprimer_parent multiwidget_btn' />" +
			      "</div>";
		me.jqDialog.find('#form_creer_calendrier_parents').append(str);
		// listener bouton suppression
		me.jqDialog.find(".form_creer_calendrier_supprimer_parent").click(function() {
			$(this).prev().remove();
			$(this).remove();
		});
	
	};
	
	/**
	 * Méthode qui charge le contenu des combobox groupes parents 
	 * Elle affiche par ailleurs les groupes parents existants (dans le cas d'une modification)
	 * 
	 * @param groupesParents (tableau) : id des groupes parents du calendrier
	 * 			S'il est vide, il n'y a aucun parent ou il s'agit d'une fenetre de création de calendrier
	 */
	DialogCreationCalendrier.prototype.remplirGroupesParents = function(groupesParents) {
		var me = this;
		this.restManager.effectuerRequete("POST", "groupesparentspotentiels", {
			token: this.restManager.getToken()
		}, function(data) {
			if (data.resultCode == RestManager.resultCode_Success) {
				// on crée une chaine contenant un select avec tous les parents disponibles
				var str = "<select class='form_creer_calendrier_parent'><option value='-1'>---</option>";
				for (var i=0; i<data.data.listeGroupes.length; i++) {
					str += "<option value='"+data.data.listeGroupes[i].id+"'>"+data.data.listeGroupes[i].nom+"</option>";
				}
				str += "</select>";
				// on stocke les groupes parents potentiels dans une variable
				me.listeGroupesParents = str;
				// on affiche la première liste déroulante de parents potentiels avec le bouton 'ajout groupe parent'
				str = "<div>" + 
					    str +
				      "<img src='img/ajout.png' alt='Ajouter un parent' " +
				      "id='form_creer_calendrier_ajout_parent' class='multiwidget_btn' />" +
				      "</div>";
				me.jqDialog.find('#form_creer_calendrier_parents').html(str);
					
				// Remplir groupes parents 
				if (groupesParents.length != 0) {
					me.jqDialog.find(".form_creer_calendrier_parent:last option[value=" + groupesParents[0] + "]").prop('selected', true);
					for (var i=1, maxI=groupesParents.length; i<maxI; i++) {
						me.ajouterGroupeParent();
						me.jqDialog.find(".form_creer_calendrier_parent:last option[value=" + groupesParents[i] + "]").prop('selected', true);
					}
				
				}
				
				// Listener bouton "Ajouter rattachement"
				me.jqDialog.find("#form_creer_calendrier_ajout_parent").click(function() {
					me.ajouterGroupeParent();
				});
				
			} else if (data.resultCode == RestManager.resultCode_NetworkError) {
				window.showToast("Erreur de récupération de la liste des groupes parents disponibles ; vérifiez votre connexion.");
			} else {
				window.showToast("Erreur de récupération de la liste des groupes parents disponibles ; votre session a peut-être expiré ?");
			}
		});
		
	};
	
	/**
	 * Méthode qui remplit les propriétaires d'un calendrier à modifier 
	 * 
	 * @param proprietaires (tableau) : id des proprietaires du calendrier
	 */
	DialogCreationCalendrier.prototype.remplirProprietaires = function(proprietaires) {
		var me = this;
		var listeProprietairesCalendrier = new Array();
		for (var i=0, maxI=proprietaires.length; i<maxI; i++) {
			var user = new Object();
			var idProprio = proprietaires[i];
			user.label = me.listeProprietairesPotentielsIndex[idProprio].label;
			user.value = idProprio;
			user.tooltip = me.listeProprietairesPotentielsIndex[idProprio].tooltip;
			user.readOnly = false; // TODO : mettre une valeur correcte
			// TODO : cette méthode pourrait utiliser UtilisateurGestion.makeUtilisateursAutocomplete  (méthode statique)
			if (user.value!=me.restManager.getUserId()) {
				listeProprietairesCalendrier.push(user);
			}
		}
		me.multiWidgetProprietaires.setValues(listeProprietairesCalendrier);
	};
	
	
	
	/**
	 * Méthode qui vérifie que le formulaire est remplit correctement
	 * 
	 * @return VRAI si le formulaire est valide et FAUX sinon
	 */
	DialogCreationCalendrier.prototype.validationFormulaire = function() {
		var valid = true;

		// Nom du calendrier non nul ?
		if (this.nom.val()=="" || !(/^[a-z \u00C0-\u00FF0-9]+$/i.test(this.nom.val()))) {
			this.nom.css({border: "1px solid red"});
			valid = false;
		} else {
			this.nom.css({border: "1px solid black"});
		}

		return valid;
	};
	
	
	/**
	 * Méthode qui effectue la requête de création OU modification d'un calendrier
	 * 
	 * @param casModifier : vaut true si on souhaite modifier un calendrier (calendrier déjà existant)
	 * et vaut false si on veut le créer
	 * @param idCal : id du calendrier à modifier (si on est dans le cas d'un modification d'un calendrier)
	 */
	DialogCreationCalendrier.prototype.effectuerRequeteCreationModification = function(casModifier, idCal) {
		var me = this;
		
		// Si le formulaire est valide, la requête est effectuée
		if (me.validationFormulaire()) {

			//Récupérer nom, matiere et type
			var nom = this.nom.val();
			var matiere = this.matiere.val();
			var type = this.type.val();
			//Récupérer les id des proprio
			var idProprietaires = me.multiWidgetProprietaires.val();
			var idProprietairesJson = JSON.stringify(idProprietaires);
			//Récupérer les id des groupes parents
			var idGroupesParents = new Array();
			this.jqDialog.find(".form_creer_calendrier_parent").each(function() {
				var val = parseInt($(this).val()); 
				if(val != -1) {
					idGroupesParents.push(val);
				}
			});
			var idGroupesParentsJson = JSON.stringify(idGroupesParents);
				
			// Cas d'un MODIFICATION d'un calendrier
			if (casModifier) {
				this.calendrierGestion.modifierCalendrier(idCal, nom, matiere, type, idProprietairesJson, idGroupesParentsJson, function(resultCode) {
					if(resultCode == RestManager.resultCode_Success) {
						// recharger les calendriers de l'utilisateur
						me.ecranParametres.afficheListeMesCalendriers();
						// fermer dialog
						me.jqDialog.dialog("close");
						// afficher message
						window.showToast("Le calendrier a bien été modifié");
					}
					else {
						// afficher message
						window.showToast("Erreur lors de la modification du calendrier");
					}
				});	
			}
			// Cas de la CREATION d'un calendrier
			else {
				this.calendrierGestion.creerCalendrier(nom, matiere, type, idProprietairesJson, idGroupesParentsJson, function(resultCode) {
					if(resultCode == RestManager.resultCode_Success) {
						// afficher message
						window.showToast("Le calendrier a bien été créé");
						// recharger les calendriers de l'utilisateur
						me.ecranParametres.afficheListeMesCalendriers();
					} else if (resultCode == RestManager.resultCode_AlphanumericRequired) {
						window.showToast("Le nom du calendrier ne doit comporter que des caractères alphanumériques et des espaces");
					} else {
						// afficher message
						window.showToast("Erreur lors de la création du calendrier");
					}
				});	
			}
			
		}
	};

	return DialogCreationCalendrier;

});
