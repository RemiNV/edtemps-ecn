/**
 * Module d'affichage de l'écran de gestion des jours bloqués
 * Associé au HTML templates/page_jours_bloques.html
 * @module EcranJoursBloques
 */
define([ "planning_cours/CalendrierAnnee", "RestManager", "underscore", "jquery" ], function(CalendrierAnnee, RestManager, _) {
	
	/**
	 * @constructor
	 * @alias EcranJoursBloques
	 */
	var EcranJoursBloques = function(restManager) {
		this.restManager = restManager;
		this.jqEcran = $("#jours_bloques");
		this.joursFeries = null;
		this.joursBloques = null;
		var me = this;

		// Si l'utilisateur n'a pas droit à gérer les jours bloqués, on le redirige
		if (!this.restManager.aDroit(RestManager.actionsEdtemps_CreerGroupeCours)) {
			document.location.href = "#agenda";
		}

		// Récupère l'année scolaire à afficher en fonction de la date du jour
		var today = new Date();
		var annee = today.getFullYear();	// C'est l'année de départ de l'année scolaire. Par exemple, pour l'année scolaire 2013-2014, this.annee vaut 2013.
		if (today.getMonth() >= 0 && today.getMonth() <= 7) annee = today.getFullYear()-1;
		
		// Initialise le calendrier
		this.recupererJoursSpeciauxAnnee(annee, function(joursSpeciaux) {

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
	    	me.recupererJoursSpeciauxAnnee(annee, function(joursSpeciaux) {
		    	me.calendrierAnnee.chargerAnnee(annee, joursSpeciaux);
	    	});
	    });
	    this.jqEcran.find("#annee_suivante").click(function() {
	    	var annee = me.calendrierAnnee.getAnnee() + 1;
	    	me.recupererJoursSpeciauxAnnee(annee, function(joursSpeciaux) {
		    	me.calendrierAnnee.chargerAnnee(annee, joursSpeciaux);
	    	});
	    });

	    // Affecte les fonctions aux boutons de gestion et d'ajout
	    this.jqEcran.find("#bt_gestion_vacances").click(function() {  });
	    this.jqEcran.find("#bt_ajout_auto").click(function() {  });
	    this.jqEcran.find("#bt_ajouter_jour_ferie").click(function() {  });
	    
	};
	

	/**
	 * Récupèrer les jours spéciaux d'une année
	 * @param {int} annee Numéro de l'année
	 * @param {function} callback Méthode exécutée en retour
	 */
	EcranJoursBloques.prototype.recupererJoursSpeciauxAnnee = function(annee, callback) {
		var me = this;

		var dateDebut = new Date(annee, 8, 1).getTime();	// Premier septembre
		var dateFin = new Date(annee+1, 7, 31).getTime();	// Dernier jour du mois d'aout

		// Affiche le message de chargement en cours
		this.jqEcran.find("#chargement_en_cours").show();
		
		// Recherche les jours fériés puis les jours bloqués
		this.getJoursFeries(annee, dateDebut, dateFin, function() {
			
			// Ajoute les jours fériés dans la liste des jours spéciaux
			var joursSpeciaux = new Array();
			for (var i=0, maxI=me.joursFeries.length; i<maxI; i++) {
				me.joursFeries[i].dateString = dateToString(me.joursFeries[i].date);
				joursSpeciaux.push(me.joursFeries[i]);
			}
			
			me.getJoursBloques(annee, dateDebut, dateFin, function() {
				
				// Ajoute les jours bloqués dans la liste des jours spéciaux
				for (var i=0, maxI=me.joursBloques.length; i<maxI; i++) {
					joursSpeciaux.push(me.joursBloques[i]);
				}

				// Cache le message de chargement en cours
				me.jqEcran.find("#chargement_en_cours").hide();
				
				// On a tout récupéré, on appelle la méthode de retour
				callback(joursSpeciaux);
			});
		});

	};
	
	
	/**
	 * Récupèrer les jours fériés d'une année
	 * 
	 * @param {int} annee Numéro de l'année
	 * @param {date} dateDebut Date de début pour la recherche
	 * @param {date} dateFin Date de fin pour la recherche
	 * @param {function} callback Méthode exécutée en retour : elle reçoit les jours fériés
	 */
	EcranJoursBloques.prototype.getJoursFeries = function(annee, dateDebut, dateFin, callback) {
		var me = this;
		
		this.restManager.effectuerRequete("GET", "joursferies/getJoursFeries", {
			token: this.restManager.getToken(), debut: dateDebut, fin: dateFin
		}, function(data) {
			if(data.resultCode == RestManager.resultCode_Success) {
				
				// Stocke la liste des jours fériés dans une variable de module
				me.joursFeries = data.data.listeJoursFeries;

				// Appelle la méthode de retour
				callback();
				
			} else {
				window.showToast("Erreur lors de la récupération de la liste des jours fériés ; vérifiez votre connexion.");
			}
		});
		
	};
	
	
	/**
	 * Récupèrer les jours bloqués et vacances d'une année
	 * 
	 * @param {int} annee Numéro de l'année
	 * @param {date} dateDebut Date de début pour la recherche
	 * @param {date} dateFin Date de fin pour la recherche
	 * @param {function} callback Méthode exécutée en retour : elle reçoit les jours bloqués
	 */
	EcranJoursBloques.prototype.getJoursBloques = function(annee, dateDebut, dateFin, callback) {
		var me = this;
		
		this.restManager.effectuerRequete("GET", "joursbloques/getJoursBloques", {
			token: this.restManager.getToken(), debut: dateDebut, fin: dateFin, vacances: null
		}, function(data) {
			if(data.resultCode == RestManager.resultCode_Success) {

				// Stocke la liste des jours fériés dans une variable de module
				me.joursBloques = data.data.listeJoursBloques;

				// Appelle la méthode de retour
				callback();
				
			} else {
				window.showToast("Erreur lors de la récupération de la liste des jours bloqués et des vacances ; vérifiez votre connexion.");
			}
		});
		
	};
	
	
	/**
	 * Afficher le tableau des jours fériés
	 */
	EcranJoursBloques.prototype.afficherTableauJoursFeries = function() {
		//<%= groupe.id %>
		// Préparation du template pour un affichage uniforme
		var template = 
			"<% _.each(jours, function(jour) { %> <tr>" +
				"<td><%= jour.libelle %></td>" +
				"<td><%= jour.dateString %></td>" +
				"<td><span class='button'><img src='./img/modifier.png' /> Modifier</span><span class='button'><img src='./img/supprimer.png' /> Supprimer</span></td>" +
			"</tr> <% }); %>";

		var titre = "<tr><th>Libellé</th><th>Date</th><th width='250'>Actions</th></tr>";
		
		// Affichage du tableau
		this.jqEcran.find("#liste_jours_feries").html(titre + _.template(template, {jours: this.joursFeries})).fadeIn(500);

	};
	

	/**
	 * Formatter une date (en JJ/MM/AAAA) à partir d'un getTime de date 
	 */
	function dateToString(getTime) {
		var date = new Date(getTime);
		
		return (date.getDate() >= 10 ? "" : "0") + date.getDate() + "/" +
			   (date.getMonth()+1 >= 10 ? "" : "0") + (date.getMonth()+1) + "/" +
			   date.getFullYear();
	}
	
		
	return EcranJoursBloques;
});