/**
 * @module DialogGererGroupeParticipants
 */
define([ "RestManager", "GroupeGestion", "EcranParametres" ], function(RestManager, GroupeGestion) {

	/**
	 * @constructor
	 * @alias DialogGererGroupeParticipants
	 */
	var DialogGererGroupeParticipants = function(restManager, ecranParametres) {
		this.restManager = restManager;
		this.groupeGestion = new GroupeGestion(this.restManager);
		this.ecranParametres = ecranParametres;

		this.initAppele = false; /* Permet de ne lancer l'initialisation de la dialogue une seule fois */
		this.jqGererGroupeParticipants = $("#dialog_gerer_groupe"); /* Pointeur jQuery vers la dialogue */
	};

	/**
	 * Affiche la boîte de dialogue de gestion d'un groupe de participants
	 * @param idGroupe Identifiant du groupe à gérer
	 * @param listeGroupesEnAttenteDeValidation Liste des groupes en attente de validation du groupe à gérer
	 */
	DialogGererGroupeParticipants.prototype.show = function(idGroupe, listeGroupesEnAttenteDeValidation) {
		if(!this.initAppele) {
			this.init();
			this.initAppele = true;
		}
		
		var me=this;
		
		// Récupération des données sur le groupe
		this.groupeGestion.queryGetGroupeComplet(idGroupe, function(resultCode, data) {

			if (resultCode == RestManager.resultCode_Success) {
				// Ecrit le contenu de la boîte de dialogue
				me.chargerContenu(data.groupe, listeGroupesEnAttenteDeValidation);
				
				// Ouvre la boîte de dialogue
				me.jqGererGroupeParticipants.dialog("open");
			} else {
				window.showToast("La récupération des informations sur le groupe a échoué ; vérifiez votre connexion.");
			}
			
		});
		
	};
	
	
	/**
	 * Initialise la boîte de dialogue de gestion d'un groupe de participants
	 */
	DialogGererGroupeParticipants.prototype.init = function() {
		var me=this;
		
		// Affiche la boîte dialogue de gestion d'un groupe de participants
		this.jqGererGroupeParticipants.dialog({
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
		this.jqGererGroupeParticipants.find("#dialog_gerer_groupe_fermer").click(function() {
			me.jqGererGroupeParticipants.dialog("close");
		});

		this.initAppele = true;
	};
	
	/**
	 * Ecrit le contenu de la boite de dialogue
	 * @param listeGroupesEnAttenteDeValidation Liste des groupes en attente de validation du groupe à gérer
	 */
	DialogGererGroupeParticipants.prototype.chargerContenu = function(groupe, listeGroupesEnAttenteDeValidation) {
		var me=this;
		
		// Préparation du template de remplissage
		var listRattachementTemplate = 
			"<% _.each(groupes, function(groupe) { %> <tr id='dialog_gerer_groupe_table_ligne_<%= groupe.id %>'>" +
				"<td><%= groupe.nom %></td>" +
				"<td class='dialog_gerer_groupe_table_boutons'><input type='button' data-id='<%= groupe.id %>' class='button dialog_gerer_groupe_accepter' value='Accepter' /><input type='button' data-id='<%= groupe.id %>' class='button dialog_gerer_groupe_refuser' value='Refuser' /></td>" +
			"</tr> <% }); %>";

		// Ecriture du contenu de la dialogue
		this.jqGererGroupeParticipants.find("table").html(_.template(listRattachementTemplate, {groupes: listeGroupesEnAttenteDeValidation}));
		
		// Listeners
		this.jqGererGroupeParticipants.find(".dialog_gerer_groupe_accepter").click(function (){
			me.deciderRattachement(true, $(this).attr("data-id"));
		});
		this.jqGererGroupeParticipants.find(".dialog_gerer_groupe_refuser").click(function (){
			me.deciderRattachement(false, $(this).attr("data-id"));
		});

	};
	
	/**
	 * Fait le lien avec le serveur et met à jour la boite de dialogue
	 * @param choix Choix pour le rattachement
	 * @param groupeId Identifiant du groupe à traiter
	 */
	DialogGererGroupeParticipants.prototype.deciderRattachement = function(choix, groupeId) {
		var me=this;
		
		this.groupeGestion.queryDeciderRattachement(choix, groupeId, function (resultCode) {
			if (resultCode == RestManager.resultCode_Success) {
				window.showToast("Votre choix a été enregistré");
				
				// Met à jour la liste des groupes
				me.ecranParametres.afficheListeMesGroupes();
				
				// Suppression de la ligne dans le tableau
				me.jqGererGroupeParticipants.find("#dialog_gerer_groupe_table_ligne_"+groupeId).remove();
				
				// S'il n'y a plus de ligne, fermeture de la boîte de dialogue
				if ($('#dialog_gerer_groupe table > *').length==0) {
					me.jqGererGroupeParticipants.dialog("close");
				}
			} else {
				window.showToast("L'enregistrement de votre choix a échoué ; vérifiez votre connexion.");
			}
		});
	};

	return DialogGererGroupeParticipants;

});
