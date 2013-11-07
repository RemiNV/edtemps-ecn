define([ "RestManager", "jquerycombobox" ], function(RestManager) {

	/**
	 * Constructeur
	 */
	function DialogCreationGroupeParticipants(restManager) {
		this.restManager = restManager;
		
		this.jqCreationGroupeForm = $("#form_creer_groupe");
		this.jqChampNom = this.jqCreationGroupeForm.find("#form_creer_groupe_nom");
		
		this.initAppele = false;
	};

	/**
	 * Affiche la boîte de dialogue de création d'un groupe de participants
	 */
	DialogCreationGroupeParticipants.prototype.show = function() {
		if(!this.initAppele) {
			this.init();
			this.initAppele = true;
		}
		
		this.jqCreationGroupeForm.dialog("open");
	};
	
	
	/**
	 * Initialise et affiche la boîte de dialogue de création d'un groupe de participants
	 */
	DialogCreationGroupeParticipants.prototype.init = function() {
		var me = this;

		// Affectation d'une méthode au clic sur le bouton "Ajouter"
		this.jqCreationGroupeForm.find("#form_creation_groupe_ajouter").click(function() {
			if (me.validationFormulaire()) {
				me.ajouterGroupe(
						me.jqChampNom.val(),
						me.jqCreationGroupeForm.find("#form_creer_groupe_parent").val(),
						me.jqCreationGroupeForm.find("#form_creer_groupe_rattachement").is(':checked'),
						me.jqCreationGroupeForm.find("#form_creer_groupe_cours").is(':checked'));
			}
		});

		// Affectation d'une méthode au clic sur le bouton "Annuler"
		this.jqCreationGroupeForm.find("#form_creer_groupe_annuler").click(function() {
			me.jqCreationGroupeForm.dialog("close");
		});
        
		// Blocage du bouton de validation avant le chargement des groupes parents disponibles
		this.jqCreationGroupeForm.find("#form_creation_groupe_ajouter").attr("disabled", "disabled");
		this.jqCreationGroupeForm.find("#form_creer_groupe_chargement").css("display", "block");
		this.jqCreationGroupeForm.find("#form_creer_groupe_message_chargement").html("Chargement des groupes parents disponibles ...");
		
		this.ecritListeGroupesParentsDisponibles(this.jqCreationGroupeForm.find("#form_creer_groupe_parent"), function(success) {
			if (success) {
				// Reactivation du bouton "Valider"
				me.jqCreationGroupeForm.find("#form_creation_groupe_ajouter").removeAttr("disabled");
				me.jqCreationGroupeForm.find("#form_creer_groupe_chargement").css("display", "none");
			}
		});
		
		// Affiche la boîte dialogue de recherche d'une salle libre
		this.jqCreationGroupeForm.dialog({
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
			}
		});

		this.initAppele = true;
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
		this.restManager.effectuerRequete("GET", "groupesparentspotentiels", {
			token: this.restManager.getToken()
		}, function(data) {
			if (data.resultCode == RestManager.resultCode_Success) {

				var maxI = data.data.listeGroupes.length;

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

				$("#form_creer_groupe_parent").combobox();
				 
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

		// Validation du champ "Nom"
		if (this.jqChampNom.val()=="") {
			this.jqChampNom.css("box-shadow", "#FF0000 0 0 10px");
			this.jqChampNom.css("border", "1px solid #FF0000");
			valid = false;
		} else {
			this.jqChampNom.css("box-shadow", "#60C003 0 0 10px");
			this.jqChampNom.css("border", "1px solid #60C003");
		}

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
	 */
	DialogCreationGroupeParticipants.prototype.ajouterGroupe = function(nom, idGroupeParent, rattachementAutorise, estCours) {

		this.restManager.effectuerRequete("GET", "recherchesallelibre", {
			nom: nom, idGroupeParent: idGroupeParent, rattachementAutorise: rattachementAutorise, estCours: estCours, token: this.restManager.getToken()
		}, function(response) {
			if (response.resultCode == RestManager.resultCode_Success) {
				window.showToast("Le groupe de participant à été créé avec succès.");
			} else if (response.resultCode == RestManager.resultCode_NetworkError) {
				window.showToast("Erreur lors de la création du groupe de participants ; vérifiez votre connexion.");
			} else {
				window.showToast(response.resultCode + " Erreur lors de la création du groupe de participants ; votre session a peut-être expiré ?");
			}
		});
		
	};


	return DialogCreationGroupeParticipants;

});