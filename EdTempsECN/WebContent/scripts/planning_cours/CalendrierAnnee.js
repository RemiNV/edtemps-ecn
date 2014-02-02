/**
 * Module calendrier annuel pour la gestion des jours spéciaux
 * @module CalendrierAnnee
 */
define([ "RestManager" ], function(RestManager) {

	/**
	 * @constructor
	 * @alias CalendrierAnnee
	 */
	var CalendrierAnnee = function(restManager, jqEcran, jqCalendar, events, callback) {
		this.restManager = restManager;
		this.jqEcran = jqEcran;
		this.jqCalendar = jqCalendar;
		this.premierAffichage = true;
		this.callback = callback;
		
		// Récupère l'année scolaire à afficher en fonction de la date du jour
		var today = new Date();
		this.annee = today.getFullYear();
		if (today.getMonth() >= 0 && today.getMonth() <= 7) this.annee = today.getFullYear()-1;

		// Initialise les noms des mois, des jours et les nombres de jours par mois
	    this.listeMois = new Array('Septembre', 'Octobre', 'Novembre', 'Décembre', 'Janvier', 'Février', 'Mars', 'Avril', 'Mai', 'Juin', 'Juillet', 'Août');
	    this.listeMoisCourts = new Array('Sep.', 'Oct.', 'Nov.', 'Déc.', 'Jan.', 'Fév.', 'Mar.', 'Avr.', 'Mai', 'Juin', 'Juil.', 'Août');
	    this.listeMoisNumero = new Array(9, 10, 11, 12, 1, 2, 3, 4, 5, 6, 7, 8);
	    this.listeNbJours = new Array(30, 31, 30, 31, 31, 28, 31, 30, 31, 30, 31, 31);
	};
	
	
	/**
	 * Getter de l'année
	 */
	CalendrierAnnee.prototype.getAnnee = function() {
		return this.annee;
	};
	
	
	/**
	 * Initialisation du calendrier
	 */
	CalendrierAnnee.prototype.afficherCalendrier = function() {
		var me = this;
		
		// Met à jour le nomrbre de jour du mois de février en fonction de l'année
		this.listeNbJours[5] = this.nbJoursFevrier();
		
		// Prépare le contenu du calendrier à afficher
		var content = "<table id='calendrierAnnee'>";

	    // Ligne de titre
	    content += "<tr>";
		for (var i=0; i<12; i++) {
			if (i<4) {
				content += "<th title='"+this.listeMois[i]+"'>"+this.listeMoisCourts[i]+"</th>";
			} else {
				content += "<th title='"+this.listeMois[i]+"'>"+this.listeMoisCourts[i]+"</th>";
			}
		}
		content += "</tr>";

		// Parcours les jours
		for (var i=0; i<31; i++) {
			
			// Parcours les mois
			content += "<tr>";
			for (var j=0; j<12; j++) {
				if (this.listeNbJours[j] >= (i+1)) {
					var an = this.annee;
					if (this.listeMoisNumero[j] >= 1 && this.listeMoisNumero[j] <= 8) { an = this.annee+1; }
					content += "<td class='jour_bloque_clic' date='"+an+"-"+this.listeMoisNumero[j]+"-"+(i+1)+"'>"+(i+1)+"</td>";
				} else {
					content += "<td></td>";
				}
			}
			content += "</tr>";
			
		}

		content += "</table>";
		
		
		
		// Affichage du calendrier
		this.jqCalendar.html(content).fadeIn(700);
		
		
		// Affichage de l'année scolaire
		this.jqEcran.find("#numero_annee_scolaire").html(this.annee + " - " + (this.annee + 1));
		
		
	    // Affecte la fonction de callback sur les jours cliquables
	    this.jqEcran.find(".jour_bloque_clic").click(function() {
	    	me.callback($(this));
	    });
	    
	};

	
	/**
	 * Charger une année particulière
	 * @param {int} annee Numéro de l'année
	 * @param {events} events Liste des événements à afficher
	 */
	CalendrierAnnee.prototype.chargerAnnee = function(annee, events) {
		this.annee = annee;
		this.events = events; // Les événements à afficher (jours bloqués)

		if (this.premierAffichage) {
			this.premierAffichage = false;
			
			this.afficherCalendrier();
		} else {
			var me = this;
			this.jqCalendar.fadeOut(200, function () {
				me.afficherCalendrier();
			});
		}
	};


	/**
	 * Récupère le nombre de jours du mois de février en fonction de l'année
	 */
	CalendrierAnnee.prototype.nbJoursFevrier = function() {
		if ((new Date(this.annee + 1, 1, 29, 0, 0, 0, 0)).getDate() == 29) {
			return 29;
		} else {
			return 28;
		}
	};

	
	return CalendrierAnnee;

});
