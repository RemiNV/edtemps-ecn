/**
 * @module DialogGererGroupeParticipants
 */
define([ "RestManager", "GroupeGestion", "EcranParametres" ], function(RestManager, GroupeGestion, EcranParametres) {

	/**
	 * @constructor
	 * @alias DialogGererGroupeParticipants
	 */
	var DialogGererGroupeParticipants = function(restManager, ecranParametres, jqDialog) {
		this.restManager = restManager;
		this.ecranParametres = ecranParametres;
		this.jqDialog = jqDialog;
		
		this.groupeGestion = new GroupeGestion(this.restManager);

		// Permet de ne lancer l'initialisation de la dialogue qu'une seule fois
		this.initAppele = false;
	};

	/**
	 * Affiche la boîte de dialogue de gestion d'un groupe de participants
	 * @param listeGroupesEnAttenteDeValidation Liste des groupes en attente de validation du groupe à gérer
	 */
	DialogGererGroupeParticipants.prototype.show = function(listeGroupesEnAttenteDeValidation) {
		if(!this.initAppele) {
			this.init();
		}
		
		// Ecrit le contenu de la boîte de dialogue
		this.chargerContenu(listeGroupesEnAttenteDeValidation);
		
		// Ouvre la boîte de dialogue
		this.jqDialog.dialog("open");
		
	};
	
	
	/**
	 * Initialise la boîte de dialogue de gestion d'un groupe de participants
	 */
	DialogGererGroupeParticipants.prototype.init = function() {
		var me=this;
		
		// Affiche la boîte dialogue de gestion d'un groupe de participants
		this.jqDialog.dialog({
			autoOpen: false,
			width: 510,
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
		
		// Listener du bouton "Fermer"
		this.jqDialog.find("#dialog_gerer_groupe_fermer").click(function() {
			me.jqDialog.dialog("close");
		});

		this.initAppele = true;
	};
	
	/**
	 * Ecrit le contenu de la boite de dialogue
	 * @param listeGroupesEnAttenteDeValidation Liste des groupes en attente de validation du groupe à gérer
	 */
	DialogGererGroupeParticipants.prototype.chargerContenu = function(listeGroupesEnAttenteDeValidation) {
		var me=this;
		
		// Préparation du template de remplissage
		var listRattachementTemplate = 
			"<% _.each(groupes, function(groupe) { %> <tr id='dialog_gerer_groupe_table_ligne_<%= groupe.id %>'>" +
				"<td class='dialog_gerer_groupe_table_noms' data-id='<%= groupe.id %>'><%= groupe.nom %></td>" +
				"<td class='dialog_gerer_groupe_table_boutons'><input type='button' data-id='<%= groupe.id %>' class='button dialog_gerer_groupe_accepter' value='Accepter' /><input type='button' data-id='<%= groupe.id %>' class='button dialog_gerer_groupe_refuser' value='Refuser' /></td>" +
			"</tr> <% }); %>";

		// Ecriture du contenu de la dialogue
		this.jqDialog.find("table").html(_.template(listRattachementTemplate, {groupes: listeGroupesEnAttenteDeValidation}));
		
		// Listeners
		this.jqDialog.find(".dialog_gerer_groupe_accepter").click(function (){
			me.deciderRattachement(true, $(this).attr("data-id"));
		});
		this.jqDialog.find(".dialog_gerer_groupe_refuser").click(function (){
			me.deciderRattachement(false, $(this).attr("data-id"));
		});
		this.jqDialog.find(".dialog_gerer_groupe_table_noms").click(function() {
			me.ecranParametres.dialogDetailGroupeParticipants.show($(this).attr("data-id"));
		});


	};
	
	/**
	 * Fait le lien avec le serveur et met à jour la boite de dialogue
	 * @param choix Choix pour le rattachement
	 * @param groupeId Identifiant du groupe à traiter
	 */
	DialogGererGroupeParticipants.prototype.deciderRattachement = function(choix, groupeId) {
		var me=this;
		
		this.groupeGestion.queryDeciderRattachementGroupe(choix, groupeId, function (resultCode) {
			if (resultCode == RestManager.resultCode_Success) {
				window.showToast("Votre choix a été enregistré");
				
				// Met à jour la liste des groupes
				me.ecranParametres.afficheListeMesGroupes();
				
				// Suppression de la ligne dans le tableau
				me.jqDialog.find("#dialog_gerer_groupe_table_ligne_"+groupeId).remove();
				
				// S'il n'y a plus de ligne, fermeture de la boîte de dialogue
				if ($('#dialog_gerer_groupe table > *').length==0) {
					me.jqDialog.dialog("close");
				}
			} else {
				window.showToast("L'enregistrement de votre choix a échoué ; vérifiez votre connexion.");
			}
		});
	};

	return DialogGererGroupeParticipants;

});
