/**
 * Module d'affichage de l'écran de gestion des jours bloqués
 * Associé au HTML templates/page_jours_bloques.html
 * @module EcranJoursBloques
 */
define([ "planning_cours/CalendrierAnnee", "planning_cours/JourBloqueGestion", "planning_cours/DialogAjoutJourFerie",
         "text!../../templates/dialog_ajout_jour_ferie.html", "RestManager", "underscore", "jquery" ],
         function(CalendrierAnnee, JourBloqueGestion, DialogAjoutJourFerie, dialogAjoutJourFerieHtml, RestManager, _) {
	
	/**
	 * @constructor
	 * @alias EcranJoursBloques
	 */
	var EcranJoursBloques = function(restManager) {
		var me = this;
		this.restManager = restManager;
		this.jqEcran = $("#jours_bloques");
		this.jourBloqueGestion = new JourBloqueGestion(this.restManager, this.jqEcran);
		
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
			me.calendrierAnnee = new CalendrierAnnee(restManager, me.jqEcran, $("#calendar_jours_bloques"), annee, joursSpeciaux, function(date, data, object) {
				if (data==null) {
					alert("le " + date);
				} else {
					alert("le " + date + " c'est : " + data.libelle);
				}
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
	    	me.dialogAjoutJourFerie.show(null, function (libelle, date) {
	    		me.jourBloqueGestion.ajouterJourFerie(libelle, date, function() {
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
				"<td><%= jour.libelle %></td>" +
				"<td><%= jour.dateString %></td>" +
				"<td><span class='button modifier_jour_ferie'><img src='./img/modifier.png' /> Modifier</span><span class='button supprimer_jour_ferie'><img src='./img/supprimer.png' /> Supprimer</span></td>" +
			"</tr> <% }); %>";

		var titre = "<tr><th>Libellé</th><th>Date</th><th width='250'>Actions</th></tr>";
		
		// Cache le tableau des jours fériés
		this.jqEcran.find("#liste_jours_feries").fadeOut(200, function () {

			// Affichage du tableau
			$(this).html(titre + _.template(template, {jours: me.jourBloqueGestion.joursFeries})).fadeIn(500);
			
		    // Affecte une action aux boutons de modification
		    me.jqEcran.find(".modifier_jour_ferie").click(function() {
		    	var id = $(this).parents("tr").attr("data-id");

		    	me.dialogAjoutJourFerie.show(me.jourBloqueGestion.joursFeriesTries[id], function (libelle, date, id) {
		    		me.jourBloqueGestion.modifierJourFerie(id, libelle, date, function () {
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

	
		
	return EcranJoursBloques;
});