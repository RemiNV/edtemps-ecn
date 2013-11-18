/**
 * @module DialogCreationCalendrier
 */
define([ "RestManager", "CalendrierGestion", "jquerymaskedinput" ], function(RestManager, CalendrierGestion) {

	/**
	 * @constructor
	 * @alias module:DialogCreationCalendrier
	 */
	var DialogCreationCalendrier = function(restManager, ecranParametres) {
		this.restManager = restManager;
		this.ecranParametres = ecranParametres;
		this.calendrierGestion = new CalendrierGestion(this.restManager);
		// Element "boite dialogue"
		this.boitedialog = $("#form_creer_calendrier");
		// Accès direct aux champs du formulaire
		this.nom = $("#form_creer_calendrier_nom");
		this.type = $("#form_creer_calendrier_type");
		this.matiere = $("#form_creer_calendrier_matiere");
		// Liste des propriétaires potentiels = de tous les utilisateurs
		this.listeProprietairesPotentiels = new Array(); //pour l'autocomplete JqueryUI
		this.listeProprietairesPotentielsIndexeeParID = new Array(); //tableau indexé par l'ID des utilisateurs, et contenant leur nom
		// Boolean pour savoir si la boite de dialogue a déjà été initialisée
		this.dejaInitialise = false;
	};
	
	/**
	 * Initialise la boîte de dialogue de création d'un calendrier
	 */
	DialogCreationCalendrier.prototype.init = function(matiere, type, proprietaires) {
		var me = this;

		// Remplir les combobox matieres et types
		me.remplirComboboxes(function(success) {
			if (success) {
				// On initialise les champs aux valeurs demandés
				me.matiere.val(matiere); 
				me.type.val(type);
			}
		});
		
		// Récupérer propriétaires potentiels
		me.recupererProprietairesPotentiels(function(success) {
			if (success) {
				// On remplir les proprietaires
				$.each(proprietaires, function(indiceArray, idProprietaire) {
					  me.ajouterProprietaire();
					  var nouveauChampProprietaire = $(".form_creer_calendrier_proprietaire").last();
					  nouveauChampProprietaire.attr("value", idProprietaire);
					  nouveauChampProprietaire.val(me.listeProprietairesPotentielsIndexeeParID[idProprietaire]);
				});
				// On active le bouton "ajouter propriétaire"
				$("#form_creer_calendrier_ajouter_proprietaire").click(function() {
					me.ajouterProprietaire();
				});
			}
		});
		
		// Listener bouton "Annuler"
		$("#form_creer_calendrier_annuler").click(function() {
			$("#form_creer_calendrier").dialog("close");
		});
		
		// Affiche la boite de dialogue de création/modification d'un calendrier
		me.boitedialog.dialog({
			autoOpen: false,
			width: 440,
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
				me.reinitialiserProprietaires();
				// on désactive le bouton valider
				$("#form_creer_calendrier_valider").unbind( "click" );
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
			if (!(me.dejaInitialise)) {
				me.init(calendrierAModifier.matiere, calendrierAModifier.type, calendrierAModifier.proprietaires); 
				me.dejaInitialise = true;
			}
			else {
				// On met affiche la matiere / le type du calendrier
				me.matiere.val(calendrierAModifier.matiere); 
				me.type.val(calendrierAModifier.type);
				// On remplir les proprietaires
				$.each(calendrierAModifier.proprietaires, function(indiceArray, idProprietaire) {
					  me.ajouterProprietaire();
					  var nouveauChampProprietaire = $(".form_creer_calendrier_proprietaire").last();
					  nouveauChampProprietaire.attr("value", idProprietaire);
					  nouveauChampProprietaire.val(me.listeProprietairesPotentielsIndexeeParID[idProprietaire]);
				});
			}
			// Ecriture du titre de la boîte de dialogue et du nom du bouton d'action principale
			me.boitedialog.dialog("option", "title", "Modifier le calendrier");
			me.boitedialog.find("#form_creer_calendrier_valider").attr("value", "Modifier");
			// On affiche le nom du calendrier dans la case Nom
			me.nom.val(calendrierAModifier.nom);
			// Récupérer l'ID du calendrier à modifier
			var idCal = calendrierAModifier.id;
		}	
		// CAS CREATION d'un calendrier
		else {
			// Si pas déjà fait, initialiser la boite de dialogue (listener, recuperation matiere/type/proprio) 
			if (!(me.dejaInitialise)) {
				me.init("","", new Array()); //Matière = Aucune et type = Aucun
				me.dejaInitialise = true;
			}
			// Ecriture du titre de la boîte de dialogue et du nom du bouton d'action principale
			me.boitedialog.dialog("option", "title", "Création d'un nouveau calendrier");
			me.boitedialog.find("#form_creer_calendrier_valider").attr("value", "Créer");
		}
		
		// Listener bouton "Valider"
		$("#form_creer_calendrier_valider").click(function() {
			me.effectuerRequeteCreationModification(casModifier, idCal);
		});
		
		// Ouverture de la boîte dialogue
		me.boitedialog.dialog("open");

	};



	/**
	 * Méthode qui remplit les comboboxes contenant matieres et types
	 */
	DialogCreationCalendrier.prototype.remplirComboboxes = function(callback) {

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
				$("#form_creer_calendrier_matiere").html(matieres);
				
				// Remplir combobox contenant les types
				var types = '<option value=""> Aucun </option>' ;
				maxI = data.data.types.length;
				for (var i = 0 ; i < maxI ; i++) {
					var nomType = data.data.types[i];
					types += '<option value="' + nomType + '">' 
						   + nomType 
						   + "</option>" ;
				}
				$("#form_creer_calendrier_type").html(types);
	
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
	 * Méthode qui permet l'ajout d'un proprietaire
	 */
	DialogCreationCalendrier.prototype.ajouterProprietaire = function() {
		var me = this;
		
		var html = '<input type="text" class="form_creer_calendrier_proprietaire" />'
			     + "<img alt='Supression' src='img/corbeille.png' " 
			     +		"class='form_creer_calendrier_supprimer_proprietaire' " 
			     +		     "title='Supprimer le propriétaire' />";
		$("#form_creer_calendrier_proprietaires").append(html);
		
		// Listener pour le nouveau bouton "Supprimer"
		$(".form_creer_calendrier_supprimer_proprietaire").last().click(function() {
			$(this).prev().remove();
			$(this).remove();
		});
		// Autocomplete
		var champAjoute = $(".form_creer_calendrier_proprietaire").last();
		champAjoute.autocomplete({
			source: me.listeProprietairesPotentiels,
			select: function (event, ui) { champAjoute.attr( "value", ui.item.id ); }
		});
	};
	
	
	/**
	 * Méthode qui reinitialise la cellule "proprietaires" (auquel on a pu ajouter des champs de texte)
	 */
	DialogCreationCalendrier.prototype.reinitialiserProprietaires = function() {
		var me=this;
		// On réinitialise le contenu "proprietaires"
		var html = '<input class="form_creer_calendrier_proprietaire_utilisateurencours"' 
			     + 'type="text" value="Vous-même" disabled="disabled" />'
			     + '<img alt="Ajout" src="img/ajout.png"' 
			     +  'id="form_creer_calendrier_ajouter_proprietaire"'
		     	 +  'title="Ajout un propriétaire" />';
		$("#form_creer_calendrier_proprietaires").html(html);
		// On réactive le bouton "ajouter propriétaire"	
		$("#form_creer_calendrier_ajouter_proprietaire").click(function() {
			me.ajouterProprietaire();
		});
	};
	
	
	/**
	 * Méthode qui vérifie que le formulaire est remplit correctement
	 * 
	 * @return VRAI si le formulaire est valide et FAUX sinon
	 */
	DialogCreationCalendrier.prototype.validationFormulaire = function() {
		var me = this;
		var valid = true;

		// Nom du calendrier non nul ?
		if (this.nom.val()=="") {
			this.nom.css({border: "1px solid red"});
			valid = false;
		} else {
			this.nom.css({border: "1px solid black"});
		}
		
		/* Propriétaires valides ?
		   => vérifier si le nom de l'utilisateur n'a pas été modifié manuellement 
		      et si l'ID correspond bien
		*/
		$(".form_creer_calendrier_proprietaire").each(function() {
			var estUnVraiUtilisateurPotentiel = false;
			for (var i=0, maxI=me.listeProprietairesPotentiels.length; i<maxI; i++) {
				if ($(this).attr("value") == me.listeProprietairesPotentiels[i].id && $(this).val() == me.listeProprietairesPotentiels[i].label) {
					estUnVraiUtilisateurPotentiel = true;
					break;
				}
			}
			if (estUnVraiUtilisateurPotentiel) {
				$(this).css({border: "1px solid black"});
			}
			else {
				$(this).css({border: "1px solid red"});
				valid = false;
			}
		});

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
			
			// Parcourt des id des proprio (qu'on place dans un tableau, qui sera transformé en JSON)
			// NB : L'utilisateur est obligatoirement propriétaire du calendrier qu'il crée
			var idProprietaires = [];
			if (window.localStorage) {
				idProprietaires.push(localStorage["userId"]);
			}
			$(".form_creer_calendrier_proprietaire").each(function() {
				var idUtilisateurCourant = $(this).attr("value");
				idProprietaires.push(idUtilisateurCourant);
			});
			var idProprietairesJson = JSON.stringify(idProprietaires);
				
			// Cas d'un MODIFICATION d'un calendrier
			if (casModifier) {
				this.calendrierGestion.modifierCalendrier(idCal, nom, matiere, type, idProprietairesJson, function(resultCode) {
					if(resultCode == RestManager.resultCode_Success) {
						// afficher message
						window.showToast("Le calendrier a bien été modifié");
						// recharger les calendriers de l'utilisateur
						me.ecranParametres.afficheListeMesCalendriers();
					}
					else {
						// afficher message
						window.showToast("Erreur lors de la modification du calendrier");
					}
				});	
			}
			// Cas de la CREATION d'un calendrier
			else {
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
					var label_value = new Object();
					label_value.label = utilisateurs[i].prenom + " " + utilisateurs[i].nom;
					label_value.id = utilisateurs[i].id;
					me.listeProprietairesPotentiels.push(label_value);
					// Création d'un autre tableau, indexé par les ID des utilisateurs
					me.listeProprietairesPotentielsIndexeeParID[utilisateurs[i].id] = utilisateurs[i].prenom + " " + utilisateurs[i].nom;
				}
				
				// Appelle la méthode de retour
				callback(true);	
			} else if (data.resultCode == RestManager.resultCode_NetworkError) {
				window.showToast("Erreur de récupération de la liste des utilisateurs potentiellement propriétaires ; vérifiez votre connexion.");
				callback(false);
			} else {
				window.showToast(data.resultCode + " Erreur de récupération de la liste des utilisateurs potentiellement propriétaires ; votre session a peut-être expiré ?");
				callback(false, 0);
			}
		});

	};

	return DialogCreationCalendrier;

});
