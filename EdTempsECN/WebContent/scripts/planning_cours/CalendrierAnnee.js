/**
 * Module calendrier annuel pour la gestion des jours spéciaux
 * @module CalendrierAnnee
 */
define([  ], function() {

	/**
	 * @constructor
	 * @alias CalendrierAnnee
	 * 
	 * La méthode de callback recevra trois paramètres :
	 * 		- un objet date à la date du jour cliqué
	 * 		- un objet javascript qui contient l'évnément du jour s'il y en a un (peut être null)
	 * 		- l'objet jquery cliqué (une div)
	 * 
	 * La liste des jours spéciaux contient tous les jours fériés et les jours bloqués (donc les vacances)
	 * Pour détecter la différence entre jour férié et jour bloqué, on regarde si l'attribut Date est présent.
	 * Seul les jours fériés ont l'attribut Date.
	 */
	var CalendrierAnnee = function(restManager, jqEcran, jqCalendar, annee, joursSpeciaux, callback) {
		this.restManager = restManager;
		this.jqEcran = jqEcran;			// Objet jQuery qui pointe sur le contenu global de la page
		this.jqCalendar = jqCalendar;	// Objet jQuery qui pointe sur la div qui contiendra le calendrier
		this.premierAffichage = true;	// Vrai si c'est la première fois que le calendrier est affiché
		this.callback = callback;		// Action sur le clique sur un objet. Elle reçoit en paramètre la div qui a été cliquée
		this.joursSpeciaux = null;
		
		// Initialise les noms des mois, des jours et les nombres de jours par mois
	    this.listeMois = new Array('Septembre', 'Octobre', 'Novembre', 'Décembre', 'Janvier', 'Février', 'Mars', 'Avril', 'Mai', 'Juin', 'Juillet', 'Août');
	    this.listeMoisCourts = new Array('Sep.', 'Oct.', 'Nov.', 'Déc.', 'Jan.', 'Fév.', 'Mar.', 'Avr.', 'Mai', 'Juin', 'Juil.', 'Août');
	    this.listeMoisNumero = new Array(9, 10, 11, 12, 1, 2, 3, 4, 5, 6, 7, 8);
	    this.listeNbJours = new Array(30, 31, 30, 31, 31, 28, 31, 30, 31, 30, 31, 31);
	    this.initialesJours = new Array('D', 'L', 'M', 'M', 'J', 'V', 'S');
	    this.listeJours = new Array('Dimanche', 'Lundi', 'Mardi', 'Mercredi', 'Jeudi', 'Vendredi', 'Samedi');
	    
	    this.chargerAnnee(annee, joursSpeciaux);
	};
	
	
	/**
	 * Afficher le calendrier avec l'année et les événements actuels
	 */
	CalendrierAnnee.prototype.afficherCalendrier = function() {
		
		// Mets à jour le nomrbre de jour du mois de février en fonction de l'année
		this.listeNbJours[5] = nbJoursFevrier(this.annee);
		
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
			var an, dimanche, classes, initiale, date;
			for (var j=0; j<12; j++) {
				if (this.listeNbJours[j] >= (i+1)) {
					an = (this.listeMoisNumero[j] < 9) ? (this.annee+1) : this.annee;
					date = new Date(an, this.listeMoisNumero[j]-1, i+1);
					dimanche = (date.getDay()==0) ? " dimanche" : "";
					classes = "jour" + dimanche;
					initiale = this.initialesJours[date.getDay()];
					
					
					tabJours += "<td><div class='"+classes+"' id='"+an+"-"+this.listeMoisNumero[j]+"-"+(i+1)+"'><span class='initiale_jour'>"+initiale+"</span>"+(i+1)+"</div></td>";
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
		this.afficherJoursSpeciaux(function() {
			me.jqCalendar.fadeIn(700);
		});
		
		// Affiche le numéro de l'année scolaire
		this.jqEcran.find("#numero_annee_scolaire").html(this.annee + " - " + (this.annee + 1));
		
	    // Affecte la fonction de callback sur les jours cliquables
		this.jqEcran.find(".jour").click(function() {
			me.callback(stringToDate($(this).attr("id")), $(this));
		});
	    
	    // Supprime le callback sur les jours fériés
		this.jqEcran.find(".ferie").unbind();
	    
	};

	
	/**
	 * Charger une année dans le calendrier
	 * @param {int} annee Numéro de l'année
	 * @param {event} joursSpeciaux Liste des événements à afficher
	 */
	CalendrierAnnee.prototype.chargerAnnee = function(annee, joursSpeciaux) {
		this.annee = annee;
		this.joursSpeciaux = joursSpeciaux;

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
	 * Afficher les jours spéciaux dans le calendrier
	 * 
	 * @param {function} callback Méthode exécutée en retour
	 */
	CalendrierAnnee.prototype.afficherJoursSpeciaux = function (callback) {

		// Parcours la liste des jours spéciaux
		for (var i=0, maxI=this.joursSpeciaux.length; i<maxI; i++) {
			var jour = this.joursSpeciaux[i];
			
			if (jour.date) {
				var date = new Date(jour.date);
				
				if (jour.fermeture) {		// Jour de fermeture
					this.jqCalendar.find("#"+dateToString(date)).addClass("fermeture").attr("data", i).attr("title", this.dateEnTouteLettres(date));
				} else {					// Jour férié
					this.jqCalendar.find("#"+dateToString(date)).addClass("ferie").attr("data", i).attr("title", this.dateEnTouteLettres(date));
				}
			}
			else {
				var dateDebut = new Date(jour.dateDebut);
				var dateFin = new Date(jour.dateFin);
				
				if (jour.vacances) {		// Vacances
					var date = dateDebut;
					while (date.getTime() <= dateFin.getTime()) {
						this.jqCalendar.find("#"+dateToString(date)).addClass("vacances");
						date.setDate(date.getDate()+1);
					}
				} else {					// Période bloquée
					this.jqCalendar.find("#"+dateToString(dateDebut)).addClass("bloque").attr("title", this.dateEnTouteLettres(dateDebut));
				}
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
	
	
	/**
	 * Formatter une date (en AAAA-MM-JJ) à partir d'un objet Date javascript 
	 */
	function dateToString(date) {
		return date.getFullYear() + "-" + (date.getMonth()+1) + "-" + date.getDate();
	}

	
	/**
	 * Formatter une date (en AAAA-MM-JJ) à partir d'un objet Date javascript 
	 */
	CalendrierAnnee.prototype.dateEnTouteLettres = function(date) {
		var nomJour = this.listeJours[date.getDay()];
		var numJour = date.getDate();
		var mois = this.listeMois[(date.getMonth()+4)%12];
		var annee = date.getFullYear();
		
		return nomJour + " " + numJour + " " + mois + " " + annee;
	};
	
	/**
	 * Récupérer un objet date à partir d'une date au format : AAAA-MM-JJ
	 */
	function stringToDate(date) {
		return new Date(date);
	}


	/**
	 * Récupère le nombre de jours du mois de février en fonction de l'année (bissexsile ou non)
	 */
	function nbJoursFevrier(annee) {
		return ((new Date(annee + 1, 1, 29)).getDate() == 29) ? 29 : 28;
	};
	
	
	return CalendrierAnnee;

});
