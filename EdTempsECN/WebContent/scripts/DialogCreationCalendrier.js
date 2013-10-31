define([ "RestManager", "jquerymask" ], function(RestManager) {

	/**
	 * Constructeur
	 */
	var DialogCreationCalendrier = function(restManager) {
		this.restManager = restManager;
		// Accès direct aux champs du formulaire
		this.nom = $("#form_creer_calendrier_nom");
		this.type = $("#form_creer_calendrier_type");
		this.matiere = $("#form_creer_calendrier_matiere");
		this.proprietaires = $("#form_creer_calendrier_proprietaires");
	};

	/**
	 * Initialise et affiche la boîte de dialogue de création d'un calendrier
	 */
	DialogCreationCalendrier.prototype.init = function() {
		var me = this;
	
		// Listener bouton "Rechercher"
		$("#form_creer_calendrier_valider").click(function() {
			// Si le formulaire est valide, la requête est effectuée
			if (me.validationFormulaire()) {
				me.effectuerRequete();
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
				// on supprime le listener ajouter_proprietaire, pour ne pas l'avoir en double si on ferme/réouvre le "dialog"
				$("#form_creer_calendrier_ajouter_proprietaire").unbind("click");
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
				var maxI = data.matieres.length;
				for (var i = 0 ; i < maxI ; i++) {
					matieres += '<option value="' + data.matieres[i].nom + '">' 
							  + data.matieres[i].nom 
					          + "</option>" ;
				}
				$("#form_creer_calendrier_matiere").html(matieres);
				
				// Remplir combobox contenant les types
				var types = '<option value=""> Aucun </option>' ;
				maxI = data.types.length;
				for (var i = 0 ; i < maxI ; i++) {
					types += '<option value="' + data.types[i].nom + '">' ;
						   + data.types[i].nom ;
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
		var html = '<input type="text" class="form_creer_calendrier_proprietaire" />';
		$("#form_creer_calendrier_proprietaires").append(html);
		// Requeter les personnes en base et mettre un filtre dans la zone de texte
		// TO-DO
	};
	
	
	/**
	 * Méthode qui reinitialise la cellule "proprietaires" (auquel on a pu ajouter des champs de texte)
	 */
	DialogCreationCalendrier.prototype.reinitialiserProprietaires = function() {
		var html = '<input class="form_creer_calendrier_proprietaire"' 
			     + 'type="text" value="Vous-même" disabled="disabled" />';
		$("#form_creer_calendrier_proprietaires").html(html);
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
	DialogCreationCalendrier.prototype.effectuerRequete = function() {
		alert('REQUETE A FAIRE => appel à une méthode sauverCalendrier dans CalendrierGestion?');
	};

	return DialogCreationCalendrier;

});