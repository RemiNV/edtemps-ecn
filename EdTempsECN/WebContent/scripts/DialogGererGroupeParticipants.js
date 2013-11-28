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
	 * @param listeGroupes Liste des groupes en attente de validation
	 * @param listeCalendriers Liste des calendriers en attente de validation
	 */
	DialogGererGroupeParticipants.prototype.show = function(listeGroupes, listeCalendriers, idGroupe) {
		if(!this.initAppele) {
			this.init();
		}
		
		this.idGroupe = idGroupe;
		this.nbGroupes = listeGroupes.length;
		this.nbCalendriers = listeCalendriers.length;
		
		// Ecrit le contenu de la boîte de dialogue
		this.chargerContenu(listeGroupes, listeCalendriers);
		
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
			width: 570,
			modal: true,
			show: { effect: "fade", duration: 200 },
			hide: { effect: "explode", duration: 200 },
			beforeClose: function(event, ui) {me.ecranParametres.afficheListeMesGroupes();}
		});
		
		// Listener du bouton "Fermer"
		this.jqDialog.find("#dialog_gerer_groupe_fermer").click(function() {
			me.jqDialog.dialog("close");
		});

		this.initAppele = true;
	};
	
	
	/**
	 * Ecrit le contenu de la boite de dialogue
	 * @param listeGroupes Liste des groupes en attente de validation
	 * @param listeCalendriers Liste des calendriers en attente de validation
	 */
	DialogGererGroupeParticipants.prototype.chargerContenu = function(listeGroupes, listeCalendriers) {
		var me=this;

		// Préparation du template pour remplir le tableau des demandes de rattachement
		var tableRattachementTemplate = 
			"<% _.each(elements, function(element) { %> <tr id='dialog_gerer_groupe_table_ligne_<%= element.id %>'>" +
				"<td class='dialog_gerer_groupe_table_noms'><%= element.nom %></td>" +
				"<td class='dialog_gerer_groupe_table_boutons'>" +
					"<input type='button' demandeur-id='<%= element.id %>' class='button dialog_gerer_groupe_accepter' value='Accepter' />" +
					"<input type='button' demandeur-id='<%= element.id %>' class='button dialog_gerer_groupe_refuser' value='Refuser' />" +
				"</td>" +
			"</tr> <% }); %>";

		// Ecriture de la partie sur les groupes
		if (listeGroupes.length>0) {

			var html = "<tr><td colspan='2' class='dialog_gerer_groupe_titre'>Liste de(s) groupe(s) qui demande(nt) le rattachement à ce groupe :</td></tr>";
			html += _.template(tableRattachementTemplate, {elements: listeGroupes});
			this.jqDialog.find("#dialog_gerer_groupe_groupes").html(html);
			
			// Listeners
			this.jqDialog.find("#dialog_gerer_groupe_groupes .dialog_gerer_groupe_accepter").click(function (){
				me.deciderRattachementGroupe(true, $(this).attr("demandeur-id"));
			});
			this.jqDialog.find("#dialog_gerer_groupe_groupes .dialog_gerer_groupe_refuser").click(function (){
				me.deciderRattachementGroupe(false, $(this).attr("demandeur-id"));
			});
			
		} else {
			this.jqDialog.find("#dialog_gerer_groupe_groupes").html("");
		}

		// Ecriture de la partie sur les calendriers
		if (listeCalendriers.length>0) {

			var html = "<tr><td colspan='2' class='dialog_gerer_groupe_titre'>Liste de(s) calendrier(s) qui demande(nt) le rattachement à ce groupe :</td></tr>";
			html += _.template(tableRattachementTemplate, {elements: listeCalendriers});
			this.jqDialog.find("#dialog_gerer_groupe_calendriers").html(html);
			
			// Listeners
			this.jqDialog.find("#dialog_gerer_groupe_calendriers .dialog_gerer_groupe_accepter").click(function (){
				me.deciderRattachementCalendrier(true, $(this).attr("demandeur-id"));
			});
			this.jqDialog.find("#dialog_gerer_groupe_calendriers .dialog_gerer_groupe_refuser").click(function (){
				me.deciderRattachementCalendrier(false, $(this).attr("demandeur-id"));
			});
			
		} else {
			this.jqDialog.find("#dialog_gerer_groupe_calendriers").html("");
		}

	};
	
	
	/**
	 * Fait le lien avec le serveur et met à jour la boite de dialogue
	 * @param choix Choix pour le rattachement
	 * @param groupeId Identifiant du groupe demandeur de rattachement
	 */
	DialogGererGroupeParticipants.prototype.deciderRattachementGroupe = function(choix, groupeId) {
		var me=this;
		
		this.groupeGestion.queryDeciderRattachementGroupe(choix, groupeId, function (resultCode) {
			if (resultCode == RestManager.resultCode_Success) {
				window.showToast("Votre choix a été enregistré");
				
				// Suppression de la ligne dans le tableau
				me.jqDialog.find("#dialog_gerer_groupe_table_ligne_"+groupeId).remove();
				me.nbGroupes--;
				
				// S'il n'y a plus de calendrier en attente de rattachement, la partie est cachée
				if (me.nbGroupes==0) {
					me.jqDialog.find("#dialog_gerer_groupe_groupes").html("");
				}
				
				// S'il n'y a plus aucun rattachement, fermeture de la fenêtre
				if (me.nbCalendriers==0 && me.nbGroupes==0) {
					me.jqDialog.dialog("close");
				}

			} else {
				window.showToast("L'enregistrement de votre choix a échoué ; vérifiez votre connexion.");
			}
		});
	};
	
	
	/**
	 * Fait le lien avec le serveur et met à jour la boite de dialogue
	 * @param choix Choix pour le rattachement
	 * @param calendrierId Identifiant du calendrier demandeur de rattachement
	 */
	DialogGererGroupeParticipants.prototype.deciderRattachementCalendrier = function(choix, calendrierId) {
		var me=this;
		
		this.groupeGestion.queryDeciderRattachementCalendrier(choix, calendrierId, this.idGroupe, function (resultCode) {
			if (resultCode == RestManager.resultCode_Success) {
				window.showToast("Votre choix a été enregistré");
				
				// Suppression de la ligne dans le tableau
				me.jqDialog.find("#dialog_gerer_groupe_table_ligne_"+calendrierId).remove();
				me.nbCalendriers--;
				
				// S'il n'y a plus de calendrier en attente de rattachement, la partie est cachée
				if (me.nbCalendriers==0) {
					me.jqDialog.find("#dialog_gerer_groupe_calendriers").html("");
				}
				
				// S'il n'y a plus aucun rattachement, fermeture de la fenêtre
				if (me.nbCalendriers==0 && me.nbGroupes==0) {
					me.jqDialog.dialog("close");
				}

			} else {
				window.showToast("L'enregistrement de votre choix a échoué ; vérifiez votre connexion.");
			}
		});
	};

	return DialogGererGroupeParticipants;

});
