/**
 * Module calendrier annuel pour la gestion des jours spéciaux
 * @module CalendrierAnnee
 */
define([ "RestManager", "jqueryui" ], function(RestManager) {

	/**
	 * @constructor
	 * @alias CalendrierAnnee
	 */
	var CalendrierAnnee = function(restManager, jqCalendar, annee) {
		this.restManager = restManager;
		this.jqCalendar = jqCalendar;
		this.annee = annee;

		// Initialise les noms des mois, des jours et les nombres de jours par mois
	    this.listeMois = new Array('Janvier', 'Février', 'Mars', 'Avril', 'Mai', 'Juin', 'Juillet', 'Août', 'Septembre', 'Octobre', 'Novembre', 'Décembre');
	    this.listeJours = new Array('Dimanche', 'Lundi', 'Mardi', 'Mercredi', 'Jeudi', 'Vendredi', 'Samedi');
	    this.listeNbJours = new Array(31, nbJoursFevrier(), 31, 30, 31, 30, 31, 31, 30, 31, 30, 31);
	    
	    // Initialise le calendrier et l'affiche dans l'objet jqCalendar
		this.init();
	};
	
	
	/**
	 * Initialisation du calendrier dans la div fournie en paramètre
	 */
	CalendrierAnnee.prototype.init = function() {
	    var content = "<table id='calendrierAnnee'>";

	    // Ligne de titre
	    content += "<tr>";
		for (var i=0; i<12; i++) {
			content += "<th>"+this.listeMois[i]+"</th>";
		}
		content += "</tr>";

		// Parcours les jours
		for (var i=0; i<31; i++) {
			
			// Parcours les mois
			content += "<tr>";
			for (var j=0; j<12; j++) {
				if (this.listeNbJours[j] >= (i+1)) {
					content += "<td>"+(i+1)+"</td>";
				} else {
					content += "<td></td>";
				}
			}
			content += "</tr>";
			
		}

		content += "</table>";
		
		// Affichage du calendrier
		this.jqCalendar.html(content);
		
	};


	/**
	 * Récupère le nombre de jours du mois de février en fonction de l'année
	 */
	function nbJoursFevrier() {
		if ((new Date(this.annee, 1, 29, 0, 0, 0, 0)).getDate() == 29) {
			return 29; 
		} else {
			return 28;
		}
	}

	
	return CalendrierAnnee;

});
