define([ "RestManager", "jquerymaskedinput" ], function(RestManager) {

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
	
	
	DialogCreationGroupeParticipants.prototype.ecritListeGroupesParentsDisponibles = function(object, callback) {

		// Récupération de la liste des groupes parents potentiels
		this.restManager.effectuerRequete("GET", "groupesparentspotentiels", {
			token: this.restManager.getToken()
		}, function(data) {
			if (data.resultCode == RestManager.resultCode_Success) {

				var maxI = data.data.listeGroupes.length;

				if (maxI>0) {
					var str = ""; /* variable du contenu de la select */
					for (var i=0; i<maxI; i++) {
						str += "<option value='"+data.data.listeGroupes[i].id+"'>"+data.data.listeGroupes[i].nom+"</option>";
					}
					$(object).append(str);
				} else {
					$(object).append("<option value=''>Aucun rattachement possible</option>");
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
	
	
	return DialogCreationGroupeParticipants;

});