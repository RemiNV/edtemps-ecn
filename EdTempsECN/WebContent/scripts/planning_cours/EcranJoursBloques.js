/**
 * Module d'affichage de l'écran de gestion des jours bloqués
 * Associé au HTML templates/page_jours_bloques.html
 * @module EcranJoursBloques
 */
define([ "planning_cours/CalendrierAnnee", "planning_cours/JourBloqueGestion", "RestManager", "underscore", "jquery" ], function(CalendrierAnnee, JourBloqueGestion, RestManager, _) {
	
	/**
	 * @constructor
	 * @alias EcranJoursBloques
	 */
	var EcranJoursBloques = function(restManager) {
		this.restManager = restManager;
		this.jqEcran = $("#jours_bloques");
		this.jourBloqueGestion = new JourBloqueGestion(this.restManager, this.jqEcran);
		var me = this;
		
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
	    	var annee = me.calendrierAnnee.getAnnee() - 1;
	    	me.jourBloqueGestion.recupererJoursSpeciauxAnnee(annee, function(joursSpeciaux) {
		    	me.calendrierAnnee.chargerAnnee(annee, joursSpeciaux);
	    	});
	    });
	    this.jqEcran.find("#annee_suivante").click(function() {
	    	var annee = me.calendrierAnnee.getAnnee() + 1;
	    	me.jourBloqueGestion.recupererJoursSpeciauxAnnee(annee, function(joursSpeciaux) {
		    	me.calendrierAnnee.chargerAnnee(annee, joursSpeciaux);
	    	});
	    });

	    // Affecte les fonctions aux boutons de gestion et d'ajout
	    this.jqEcran.find("#bt_gestion_vacances").click(function() {  });
	    this.jqEcran.find("#bt_ajout_auto").click(function() {  });
	    this.jqEcran.find("#bt_ajouter_jour_ferie").click(function() {  });
	    
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
		
		// Affichage du tableau
		this.jqEcran.find("#liste_jours_feries").html(titre + _.template(template, {jours: this.jourBloqueGestion.joursFeries})).fadeIn(500);
		
	    // Affecte une action au bouton de modification
	    this.jqEcran.find(".modifier_jour_ferie").click(function() { 
	    	
	    });

	    // Affecte une action au bouton de suppression
	    this.jqEcran.find(".supprimer_jour_ferie").click(function() {
	    	me.jourBloqueGestion.supprimerJourFerie($(this).parents("tr").attr("data-id"), function() {
		    	var annee = me.calendrierAnnee.getAnnee();
		    	me.jourBloqueGestion.recupererJoursSpeciauxAnnee(annee, function(joursSpeciaux) {
		    		me.calendrierAnnee.chargerAnnee(annee, joursSpeciaux);
		    		me.afficherTableauJoursFeries();
		    	});
	    	});
	    });
	};
	
		
	return EcranJoursBloques;
});