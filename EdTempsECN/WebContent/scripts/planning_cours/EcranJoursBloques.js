/**
 * Module d'affichage de l'écran de gestion des jours bloqués
 * Associé au HTML templates/page_jours_bloques.html
 * @module EcranJoursBloques
 */
define([ "planning_cours/CalendrierAnnee", "planning_cours/JourBloqueGestion", "planning_cours/DialogAjoutJourFerie",
         "text!../../templates/dialog_ajout_jour_ferie.html", "text!../../templates/dialog_details_jourbloque.tpl", "RestManager", "underscore", "lib/fullcalendar.translated.min", "jquery" ],
         function(CalendrierAnnee, JourBloqueGestion, DialogAjoutJourFerie, dialogAjoutJourFerieHtml, tplDialogDetailsJourBloque, RestManager, _) {
	
	/**
	 * @constructor
	 * @alias EcranJoursBloques
	 */
	var EcranJoursBloques = function(restManager) {
		var me = this;
		this.restManager = restManager;
		this.jqEcran = $("#jours_bloques");
		this.jqDialogDetailsJourBloque = $("#dialog_detail_jourbloque");
		this.jourBloqueGestion = new JourBloqueGestion(this.restManager, this.jqEcran);
		
		// Template pour la dialogue de détails des jours bloqués
		this.templateDialogDetails = _.template(tplDialogDetailsJourBloque);
		
		// Action lors du clic dans la page, lorsque la dialogue de détail est ouverte
		var closeDialogDetailsCallback = function(event) {
			if(!me.jqDialogDetailsJourBloque.dialog("isOpen")) {
				return;
			}
			
			// On n'est pas à l'intérieur d'une dialog
			var jqTarget = $(event.target);
			if(!jqTarget.is(".ui_dialog")
					&& jqTarget.closest(".ui-dialog").length == 0
					&& jqTarget.closest(".jour").length == 0) {
				me.jqDialogDetailsJourBloque.dialog("close");
				return false;
			}
		};
		
		// Préparation de la dialogue de détails des jours bloqués
		this.jqDialogDetailsJourBloque.dialog({
			autoOpen: false,
			draggable: false,
			width: 500,
			open: function(){
				$(document).bind("click", closeDialogDetailsCallback);
			},
			close: function() {
				$(document).unbind("click", closeDialogDetailsCallback);
			}
		});
		this.jqDialogDetailsJourBloque.dialog("widget").find(".ui-dialog-titlebar").addClass("dialog_detail_jourbloque_header");
		
		// Préparation de la boite de dialogue d'ajout de jours fériés
		var jqDialogAjoutJourFerie = $("#dialog_ajout_jour_ferie").html(dialogAjoutJourFerieHtml);
		this.dialogAjoutJourFerie = new DialogAjoutJourFerie(restManager, jqDialogAjoutJourFerie, this);

		// Si l'utilisateur n'a pas droit à gérer les jours bloqués, on le redirige
		if (!this.restManager.aDroit(RestManager.actionsEdtemps_CreerGroupeCours)) {
			document.location.href = "#agenda";
			return;
		}

		// Récupère l'année scolaire à afficher en fonction de la date du jour
		var today = new Date();
		var annee = today.getFullYear();	// C'est l'année de départ de l'année scolaire. Par exemple, pour l'année scolaire 2013-2014, this.annee vaut 2013.
		if (today.getMonth() >= 0 && today.getMonth() <= 7) annee = today.getFullYear()-1;
		
		// Initialise le calendrier
		this.jourBloqueGestion.recupererJoursSpeciauxAnnee(annee, function(joursSpeciaux) {

		    // Affiche des jours fériés dans le tableau en bas de page
		    me.afficherTableauJoursFeries();

		    // Affiche le calendrier
			me.calendrierAnnee = new CalendrierAnnee(restManager, me.jqEcran, $("#calendar_jours_bloques"), annee, joursSpeciaux, function(date, jqElement) {
				me.clickSurUnJour(date, jqElement);
			});
						
			// Affiche l'écran
			me.jqEcran.fadeIn(200);

		});
		
	    // Affecte les fonctions aux flêches de navigation entre années
	    this.jqEcran.find("#annee_precedente").click(function() {
	    	me.actualiserPage(-1);
	    });
	    this.jqEcran.find("#annee_suivante").click(function() {
	    	me.actualiserPage(1);
	    });

	    // Affecte une action au bouton de gestion des vacances
	    this.jqEcran.find("#bt_gestion_vacances").click(function() {  });
	    
	    // Affecte une action au bouton d'ajout automatique
	    this.jqEcran.find("#bt_ajout_auto").click(function() {
	    	me.jourBloqueGestion.ajouterAutoJourFerie(me.calendrierAnnee.getAnnee(), function() {
	    		me.actualiserPage(0);
	    	});
	    });
	    
	    // Affecte une action au bouton d'ajout
	    this.jqEcran.find("#bt_ajouter_jour_ferie").click(function() { 
	    	me.dialogAjoutJourFerie.show(null, function (libelle, date, type) {
	    		me.jourBloqueGestion.ajouterJourFerie(libelle, date, type, function() {
	    			me.actualiserPage(0);	
	    		});
	    	});
	    });
	    
	};
	

	
	/**
	 * Afficher le tableau des jours fériés
	 */
	EcranJoursBloques.prototype.afficherTableauJoursFeries = function() {
		var me = this;
		
		if (this.jourBloqueGestion.joursFeries.length == 0) {
			this.jqEcran.find("#liste_jours_feries").html("Aucun jour férié ...").show();
			return;
		}

		// Préparation du template pour un affichage uniforme
		var template = 
			"<% _.each(jours, function(jour) { %> <tr data-id='<%= jour.id %>'>" +
				"<td><div <% if (jour.fermeture>0) { %>class='item fermeture' title='Jour de fermeture'<% } else { %>class='item ferie' title='Jour férié'<% } %>>&nbsp;</div></td>" +
				"<td><%= jour.libelle %></td>" +
				"<td><%= jour.dateString %></td>" +
				"<td><span class='button modifier_jour_ferie'><img src='./img/modifier.png' /> Modifier</span><span class='button supprimer_jour_ferie'><img src='./img/supprimer.png' /> Supprimer</span></td>" +
			"</tr> <% }); %>";

		var titre = "<tr><th width='20'>Type</th><th>Libellé</th><th>Date</th><th width='250'>Actions</th></tr>";
		
		// Cache le tableau des jours fériés
		this.jqEcran.find("#liste_jours_feries").fadeOut(200, function () {

			// Affichage du tableau
			$(this).html(titre + _.template(template, {jours: me.jourBloqueGestion.joursFeries})).fadeIn(500);
			
		    // Affecte une action aux boutons de modification
		    me.jqEcran.find(".modifier_jour_ferie").click(function() {
		    	var id = $(this).parents("tr").attr("data-id");

		    	me.dialogAjoutJourFerie.show(me.jourBloqueGestion.joursFeriesTries[id], function (libelle, date, type, id) {
		    		me.jourBloqueGestion.modifierJourFerie(id, libelle, date, type, function () {
			    		me.actualiserPage(0);
		    		});
		    	});
		    });

		    // Affecte une action aux boutons de suppression
		    me.jqEcran.find(".supprimer_jour_ferie").click(function() {
		    	var id = $(this).parents("tr").attr("data-id");
		    	
		    	confirm("Etes-vous sûr(e) de vouloir supprimer ce jour férié ?", function () {
			    	me.jourBloqueGestion.supprimerJourFerie(id, function() {
			    		me.actualiserPage(0);
			    	});
		    	});
		    });

		});
		
	};
	
	
	/**
	 * Actualiser la page
	 * 
	 * @param {int} i Nombre d'année de décalage par rapport à l'actuelle
	 * 				  -1 : année précédente ; 0 : année en cours ; 1 : année suivante
	 */
	EcranJoursBloques.prototype.actualiserPage = function(i) {
		
		var me = this;
    	var annee = this.calendrierAnnee.getAnnee() + i;
    	
    	this.jourBloqueGestion.recupererJoursSpeciauxAnnee(annee, function(joursSpeciaux) {
	    	me.calendrierAnnee.chargerAnnee(annee, joursSpeciaux);
	    	me.afficherTableauJoursFeries();
    	});
    	
	};


	/**
	 * L'utiliateur a cliqué sur un jour
	 * 
	 * @param {date} date Date du jour cliqué
	 * @param {object} jqElement objet jquery de l'élément cliqué
	 */
	EcranJoursBloques.prototype.clickSurUnJour = function(date, jqElement) {
		
		// Récupère la liste des événemnts bloquants sur cette journée
		var listeEvenementsBloquants = this.jourBloqueGestion.getJoursBloquesParJour(date);
		
		if (listeEvenementsBloquants.length == 0) {
			// TODO afficher la boite de dialogue de création de périodes bloquées
		} else {
			this.afficherDialogDetailJourBloque(listeEvenementsBloquants, date, jqElement);
		}
    	
	};

	
	/**
	 * Affiche la bulle de détail d'un jour bloqué
	 * 
	 * @param {Array} listeEvenementsBloquants liste des événemnts bloquants sur cette journée
	 * @param {date} date Date du jour cliqué
	 * @param {object} jqElement objet jquery de l'élément cliqué
	 */
	EcranJoursBloques.prototype.afficherDialogDetailJourBloque = function(listeEvenementsBloquants, date, jqElement) {

		// Remplissage du template
		this.jqDialogDetailsJourBloque.find("#dialog_details_jourbloque_hook").html(this.templateDialogDetails({elements: listeEvenementsBloquants}));

		// Positionnement de la dialogue
		this.jqDialogDetailsJourBloque.dialog("option", {
			position: {
				my: "center bottom",
				at: "top-10",
				of: jqElement
			},
			title: $.fullCalendar.formatDate(date, "dd/MM/yyyy")
		});
		
		// Ouverture de la dialogue
		this.jqDialogDetailsJourBloque.dialog("open");
	};
		
	return EcranJoursBloques;
});