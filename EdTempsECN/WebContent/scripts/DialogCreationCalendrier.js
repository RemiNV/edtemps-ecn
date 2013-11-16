/**
 * @module DialogCreationCalendrier
 */
define([ "RestManager", "CalendrierGestion", "jquerymaskedinput" ], function(RestManager, CalendrierGestion) {

	/**
	 * @constructor
	 * @alias module:DialogCreationCalendrier
	 */
	var DialogCreationCalendrier = function(restManager) {
		this.restManager = restManager;
		this.calendrierGestion = new CalendrierGestion(this.restManager);
		// Accès direct aux champs du formulaire
		this.nom = $("#form_creer_calendrier_nom");
		this.type = $("#form_creer_calendrier_type");
		this.matiere = $("#form_creer_calendrier_matiere");
		// Liste des propriétaires potentiels = de tous les utilisateurs
		this.listeProprietairesPotentiels = new Array();
	};

	/**
	 * Initialise et affiche la boîte de dialogue de création d'un calendrier
	 */
	DialogCreationCalendrier.prototype.init = function() {
		var me = this;

		// Remplir les combobox matieres et types
		me.remplirComboboxes();
		
		// Récupérer propriétaires potentiels
		me.recupererProprietairesPotentiels(function(success, nbUtilisateurs) {
			if (success) {
				// On active le bouton "Valider"
				$("#form_creer_calendrier_valider").click(function() {
					// Si le formulaire est valide, la requête est effectuée
					if (me.validationFormulaire()) {
						me.effectuerRequete();
					}
				});
			}
		});
		
		// Listener bouton "Annuler"
		$("#form_creer_calendrier_annuler").click(function() {
			$("#form_creer_calendrier").dialog("close");
		});
		
		// Listener "ajouter propriétaire"
		$("#form_creer_calendrier_ajouter_proprietaire").click(function() {
			me.ajouterProprietaire();
		});
		
		// Affiche dialog de création d'un calendrier
		$("#form_creer_calendrier").dialog({
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
				// on supprime les listeners pour ne pas les avoir en double si on ferme/réouvre le "dialog"
				$("#form_creer_calendrier_ajouter_proprietaire").unbind("click");
				$("#form_creer_calendrier_annuler").unbind("click");
				$("#form_creer_calendrier_valider").unbind("click");
				// on vide la case "nom"
				me.nom.val('');
			}
		});

	};



	/**
	 * Méthode qui remplit les comboboxes contenant matieres et types
	 */
	DialogCreationCalendrier.prototype.remplirComboboxes = function() {

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
	
			} else if (data.resultCode == RestManager.resultCode_NetworkError) {
				window.showToast("Erreur de récupération des matières/types ; vérifiez votre connexion.");
			} else {
				window.showToast("Erreur" + data.resultCode + ". La récupération des matières/types a échoué ; votre session a peut-être expiré ?");
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
		var html = '<input class="form_creer_calendrier_proprietaire_utilisateurencours"' 
			     + 'type="text" value="Vous-même" disabled="disabled" />'
			     + '<img alt="Ajout" src="img/ajout.png"' 
			     +  'id="form_creer_calendrier_ajouter_proprietaire"'
		     	 +  'title="Ajout un propriétaire" />';
		$("#form_creer_calendrier_proprietaires").html(html);
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
	 * Méthode qui effectue la requête de création du calendrier
	 */
	DialogCreationCalendrier.prototype.effectuerRequete = function() {
		
		//Récupérer nom, matiere et type
		var nom = this.nom.val();
		var matiere = this.matiere.val();
		var type = this.type.val();
		
		// Parcourt des id des proprio (qu'on place dans un tableau, qui sera transformé en JSON)
		// NOTE : L'utilisateur est obligatoirement propriétaire du calendrier qu'il crée
		var idProprietaires = [];
		if (window.localStorage) {
			idProprietaires.push(localStorage["userId"]);
		}
		$(".form_creer_calendrier_proprietaire").each(function() {
			var idUtilisateurCourant = $(this).attr("value");
			idProprietaires.push(idUtilisateurCourant);
		});
		var idProprietairesJson = JSON.stringify(idProprietaires);
			
		this.calendrierGestion.creerCalendrier(nom, matiere, type, idProprietairesJson, function(resultCode) {
			if(resultCode == RestManager.resultCode_Success) {
				window.showToast("Le calendrier a bien été créé");
			}
			else {
				window.showToast("Erreur lors de la création du calendrier");
			}
		});	
		// Pensez à recharger la page, sur laquelle un nouveau calendrier est apparu
		// Peut etre fermer la dialog aussi 
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
