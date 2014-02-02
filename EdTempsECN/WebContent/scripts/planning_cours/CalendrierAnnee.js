/**
 * Module calendrier annuel pour la gestion des jours spéciaux
 * @module CalendrierAnnee
 */
define([ "RestManager" ], function(RestManager) {

	/**
	 * @constructor
	 * @alias CalendrierAnnee
	 */
	var CalendrierAnnee = function(restManager, jqEcran, jqCalendar, annee, joursFeries, joursBloques, callback) {
		this.restManager = restManager;
		this.jqEcran = jqEcran;			// Objet jQuery qui pointe sur le contenu global de la page
		this.jqCalendar = jqCalendar;	// Objet jQuery qui pointe sur la div qui contiendra le calendrier
		this.premierAffichage = true;	// Vrai si c'est la première fois que le calendrier est affiché
		this.callback = callback;		// Action sur le clique sur un objet. Elle reçoit en paramètre la div qui a été cliquée
		
		// Initialise les noms des mois, des jours et les nombres de jours par mois
	    this.listeMois = new Array('Septembre', 'Octobre', 'Novembre', 'Décembre', 'Janvier', 'Février', 'Mars', 'Avril', 'Mai', 'Juin', 'Juillet', 'Août');
	    this.listeMoisCourts = new Array('Sep.', 'Oct.', 'Nov.', 'Déc.', 'Jan.', 'Fév.', 'Mar.', 'Avr.', 'Mai', 'Juin', 'Juil.', 'Août');
	    this.listeMoisNumero = new Array(9, 10, 11, 12, 1, 2, 3, 4, 5, 6, 7, 8);
	    this.listeNbJours = new Array(30, 31, 30, 31, 31, 28, 31, 30, 31, 30, 31, 31);
	    
	    this.chargerAnnee(annee, joursFeries, joursBloques);
	};
	
	
	/**
	 * Afficher le calendrier avec l'année et les événements actuels
	 */
	CalendrierAnnee.prototype.afficherCalendrier = function() {
		
		// Mets à jour le nomrbre de jour du mois de février en fonction de l'année
		this.listeNbJours[5] = this.nbJoursFevrier();
		
	    // Prépare la ligne de titre avec les noms des mois
		var ligneMois = "<tr>";
		for (var i=0; i<12; i++) {
			ligneMois += "<th title='"+this.listeMois[i]+" "+((this.listeMoisNumero[i] < 9) ? (this.annee+1) : this.annee)+"'>"+this.listeMoisCourts[i]+"</th>";
		}
		ligneMois += "</tr>";

		// Parcours les jours
		var tabJours = "";
		for (var i=0; i<31; i++) {
			
			// Parcours les mois
			tabJours += "<tr>";
			var an, dimanche, classes;
			for (var j=0; j<12; j++) {
				if (this.listeNbJours[j] >= (i+1)) {
					an = (this.listeMoisNumero[j] < 9) ? (this.annee+1) : this.annee;
					dimanche = (new Date(an, this.listeMoisNumero[j]-1, i+1).getDay()==0) ? " dimanche" : "";
					classes = "jour" + dimanche;
					
					tabJours += "<td><div class='"+classes+"' id='"+(i+1)+"-"+this.listeMoisNumero[j]+"-"+an+"'>"+(i+1)+"</div></td>";
				} else {
					tabJours += "<td></td>";
				}
			}
			tabJours += "</tr>";
		}
		
		// Affiche le calendrier
		this.jqCalendar.html("<table id='calendrierAnnee'>" + ligneMois + tabJours + "</table>");
		
		// Affiche les jours spéciaux
		var me = this;
		afficherJoursSpeciaux(this.jqCalendar, this.joursFeries, this.joursBloques, function() {
			me.jqCalendar.fadeIn(700);
		});
		
		// Affiche le numéro de l'année scolaire
		this.jqEcran.find("#numero_annee_scolaire").html(this.annee + " - " + (this.annee + 1));
		
	    // Affecte la fonction de callback sur les jours cliquables
	    this.jqEcran.find(".jour").click(function() {
	    	me.callback($(this));
	    });
	    
	};

	
	/**
	 * Charger une année dans le calendrier
	 * @param {int} annee Numéro de l'année
	 * @param {event} events Liste des événements à afficher
	 */
	CalendrierAnnee.prototype.chargerAnnee = function(annee, joursFeries, joursBloques) {
		this.annee = annee;
		this.joursFeries = joursFeries;
		this.joursBloques = joursBloques;

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
	 * Récupère le nombre de jours du mois de février en fonction de l'année (bissexsile ou non)
	 */
	CalendrierAnnee.prototype.nbJoursFevrier = function() {
		return ((new Date(this.annee + 1, 1, 29)).getDate() == 29) ? 29 : 28;
	};

	
	/**
	 * Afficher les jours spéciaux dans le calendrier
	 * 
	 * @param {jQueryObject} jqCalendar Numéro de l'année
	 * @param {Array} joursFeries Liste des jours fériés
	 * @param {Array} joursBloques Liste des jours bloqués
	 * @param {function} callback Méthode exécutée en retour
	 */
	function afficherJoursSpeciaux(jqCalendar, joursFeries, joursBloques, callback) {
		
		// Jours fériés
		for (var i=0, maxI=joursFeries.length; i<maxI; i++) {
			var date = new Date(joursFeries[i].date);
			var dateFormatee = date.getDate() + "-" + (date.getMonth()+1) + "-" + date.getFullYear();
			jqCalendar.find("#"+dateFormatee).addClass("ferie");
		}
		
		// Jours bloqués
		for (var i=0, maxI=joursBloques.length; i<maxI; i++) {
			var dateDebut = new Date(joursBloques[i].dateDebut);
			var dateFin = new Date(joursBloques[i].dateFin);
			
			if (joursBloques[i].vacances) {
				var date = dateDebut;
				while (date.getTime() < dateFin.getTime()) {
					jqCalendar.find("#"+formaterDate(date)).addClass("vacances");
					date.setTime(date.getTime()+(24*60*60*1000));
				}
			} else {
				jqCalendar.find("#"+formaterDate(dateDebut)).addClass("bloque");
			}
		}
		
		// Appelle la méthode de retour
		callback();
	};

	
	/**
	 * Getter de l'année
	 */
	CalendrierAnnee.prototype.getAnnee = function() {
		return this.annee;
	};
	
	function formaterDate(date) {
		return date.getDate() + "-" + (date.getMonth()+1) + "-" + date.getFullYear();
	}
	
	
	return CalendrierAnnee;

});
