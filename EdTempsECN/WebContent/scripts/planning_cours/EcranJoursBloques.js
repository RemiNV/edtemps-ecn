/**
 * Module d'affichage de l'écran de gestion des jours bloqués
 * Associé au HTML templates/page_jours_bloques.html
 * @module EcranJoursBloques
 */
define([ "planning_cours/CalendrierAnnee", "planning_cours/JourBloqueGestion",
         "planning_cours/DialogAjoutJourFerie", "text!../../templates/dialog_ajout_jour_ferie.html",
         "planning_cours/DialogAjoutPeriodeBloquee", "text!../../templates/dialog_ajout_periode_bloquee.html",
         "planning_cours/DialogAjoutVacances", "text!../../templates/dialog_ajout_vacances.html",
         "text!../../templates/dialog_details_jourbloque.tpl",  "text!../../templates/dialog_gestion_vacances.tpl",
         "RestManager", "underscore", "lib/fullcalendar.translated.min", "jquery" ],
         function(CalendrierAnnee, JourBloqueGestion, DialogAjoutJourFerie, dialogAjoutJourFerieHtml, DialogAjoutPeriodeBloquee, dialogAjoutPeriodeBloqueeHtml,
        		  DialogAjoutVacances, dialogAjoutVacancesHtml, tplDialogDetailsJourBloque, tplDialogGestionVacances, RestManager, _) {
	
	/**
	 * @constructor
	 * @alias EcranJoursBloques
	 */
	var EcranJoursBloques = function(restManager) {
		var me = this;
		this.restManager = restManager;
		this.jqEcran = $("#jours_bloques");
		this.jqDialogDetailsJourBloque = $("#dialog_detail_jourbloque");
		this.jqDialogGestionVacances = $("#dialog_gestion_vacances");
		this.jourBloqueGestion = new JourBloqueGestion(this.restManager, this.jqEcran);

		// Si l'utilisateur n'a pas droit à gérer les jours bloqués, on le redirige
		if (!this.restManager.aDroit(RestManager.actionsEdtemps_CreerGroupeCours)) {
			document.location.href = "#agenda";
			return;
		}

		/* ------------
		 * Boîte de dialogue de détail des jours bloqués
		 * ------------ */
		
		// Template
		this.templateDialogDetails = _.template(tplDialogDetailsJourBloque);

		// Fermer la boîte de dialogue lors d'un clic externe
		var closeDialogDetailsCallback = function(event) {
			if(!me.jqDialogDetailsJourBloque.dialog("isOpen")) {
				return;
			}

			var jqTarget = $(event.target);
			if(!jqTarget.is(".ui_dialog")
					&& jqTarget.closest(".ui-dialog").length == 0
					&& jqTarget.closest(".bloque").length == 0) {
				me.jqDialogDetailsJourBloque.dialog("close");
				return false;
			}
		};
		
		// Préparation de la boîte de dialogue
		this.jqDialogDetailsJourBloque.dialog({
			autoOpen: false,
			appendTo: "#dialog_hook",
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

		
		/* ------------
		 * Boîte de dialogue de gestion des vacances
		 * ------------ */
		
		// Template
		this.templateDialogVacances = _.template(tplDialogGestionVacances);
		
		// Fermer la boîte de dialogue lors d'un clic externe
		var closeGestionVacancesCallback = function(event) {
			if(!me.jqDialogGestionVacances.dialog("isOpen")) {
				return;
			}
			
			// On n'est pas à l'intérieur d'une dialog
			var jqTarget = $(event.target);
			if(!jqTarget.is(".ui_dialog")
					&& jqTarget.closest(".ui-dialog").length == 0
					&& jqTarget.closest("#bt_gestion_vacances").length == 0) {
				me.jqDialogGestionVacances.dialog("close");
				return false;
			}
		};

		// Préparation de la boîte de dialogue
		this.jqDialogGestionVacances.dialog({
			autoOpen: false,
			appendTo: "#dialog_hook",
			draggable: false,
			width: 500,
			open: function(){
				$(document).bind("click", closeGestionVacancesCallback);
			},
			close: function() {
				$(document).unbind("click", closeGestionVacancesCallback);
			},
			title: "Gestion des vacances de l'année"
		});
		this.jqDialogGestionVacances.dialog("widget").find(".ui-dialog-titlebar").addClass("dialog_gestion_vacances_header");

		
		// Préparation de la boîte de dialogue d'ajout de jours fériés
		var jqDialogAjoutJourFerie = $("#dialog_ajout_jour_ferie").html(dialogAjoutJourFerieHtml);
		this.dialogAjoutJourFerie = new DialogAjoutJourFerie(restManager, jqDialogAjoutJourFerie, this);

		// Préparation de la boîte de dialogue d'ajout de périodes bloquées
		var jqDialogAjoutPeriodeBloquee = $("#dialog_ajout_periode_bloquee").html(dialogAjoutPeriodeBloqueeHtml);
		this.dialogAjoutPeriodeBloquee = new DialogAjoutPeriodeBloquee(restManager, jqDialogAjoutPeriodeBloquee, this);

		// Préparation de la boîte de dialogue d'ajout de vacances
		var jqDialogAjoutVacances = $("#dialog_ajout_vacances").html(dialogAjoutVacancesHtml);
		this.dialogAjoutVacances = new DialogAjoutVacances(restManager, jqDialogAjoutVacances, this);

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
	    this.jqEcran.find("#bt_gestion_vacances").click(function() {
	    	me.afficherDialogGestionVacances(me.jourBloqueGestion.vacances);
	    });
	    
	    // Affecte une action au bouton d'ajout automatique
	    this.jqEcran.find("#bt_ajout_auto").click(function() {
	    	me.jourBloqueGestion.ajouterAutoJourFerie(me.calendrierAnnee.getAnnee(), function(nbJourAjoutes) {
	    		if (nbJourAjoutes!=0) me.actualiserPage(0);
	    	});
	    });
	    
	    // Affecte une action au bouton d'ajout
	    this.jqEcran.find("#bt_ajouter_jour_ferie").click(function() { 
	    	me.dialogAjoutJourFerie.show(null, function (libelle, date) {
	    		me.jourBloqueGestion.ajouterJourFerie(libelle, date, false, function() {
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

		    	me.dialogAjoutJourFerie.show(me.jourBloqueGestion.joursFeriesTries[id], function (libelle, date, type) {
		    		me.jourBloqueGestion.modifierJourFerie(id, libelle, date, type, function () {
			    		me.actualiserPage(0);
		    		});
		    	});
		    });

		    // Affecte une action aux boutons de suppression
		    me.jqEcran.find(".supprimer_jour_ferie").click(function() {
		    	var id = $(this).parents("tr").attr("data-id");
		    	
		    	confirm("Etes-vous sûr(e) de vouloir supprimer ce jour férié : '" + me.jourBloqueGestion.joursFeriesTries[id].libelle + "' ?", function () {
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
		var me = this;
		
		// Récupère la liste des événements bloquants sur cette journée
		var listeEvenementsBloquants = this.jourBloqueGestion.getJoursBloquesParJour(date);
		
		if (listeEvenementsBloquants.length == 0) {
			
			if (date.getDay()==0) {
				confirm("Le jour séléctionné est un dimanche, êtes vous sûr de vouloir ajouter une période bloquée ?", function() {
					me.ouvrirAjoutPeriodeBloquee(date);
				});
			} else {
				me.ouvrirAjoutPeriodeBloquee(date);
			}
			
		} else {
			this.afficherDialogDetailJourBloque(listeEvenementsBloquants, date, jqElement);
		}
    	
	};
	
	
	/**
	 * Affiche la dialog d'ajout d'un jour bloqué 
	 * 
	 * @param {date} date Date du jour cliqué
	 */
	EcranJoursBloques.prototype.ouvrirAjoutPeriodeBloquee = function(date) {
		var me = this;
		this.dialogAjoutPeriodeBloquee.show(null, date, function (libelle, dateDebut, dateFin, listeGroupes) {
			me.jourBloqueGestion.ajouterPeriodeBloquee(libelle, dateDebut, dateFin, listeGroupes, false, function () {
	    		me.actualiserPage(0);
    		});
		});
	};

	
	/**
	 * Affiche la bulle de détail d'un jour bloqué
	 * 
	 * @param {Array} listeEvenementsBloquants liste des événemnts bloquants sur cette journée
	 * @param {date} date Date du jour cliqué
	 * @param {object} jqElement objet jquery de l'élément cliqué
	 */
	EcranJoursBloques.prototype.afficherDialogDetailJourBloque = function(listeEvenementsBloquants, date, jqElement) {
		var me = this;
		
		// Remplissage du template
		this.jqDialogDetailsJourBloque.find("#dialog_details_jourbloque_hook").html(this.templateDialogDetails({elements: listeEvenementsBloquants}));

		// Positionnement de la dialogue
		this.jqDialogDetailsJourBloque.dialog("option", {
			position: {
				my: "center bottom",
				at: "top-10",
				of: jqElement
			},
			title: this.calendrierAnnee.dateEnTouteLettres(date)
		});
		
		// Affecte une action aux boutons de modification
		this.jqDialogDetailsJourBloque.find(".modifier").click(function() {
			me.jqDialogDetailsJourBloque.dialog("close");
	    	var id = $(this).parents("tr").attr("data-id");
			
			me.dialogAjoutPeriodeBloquee.show(me.jourBloqueGestion.joursBloquesTries[id], date, function (libelle, dateDebut, dateFin, listeGroupes) {
				me.jourBloqueGestion.modifierPeriodeBloquee(id, libelle, dateDebut, dateFin, listeGroupes, false, function () {
		    		me.actualiserPage(0);
	    		});
			});
		});
		
		// Affecte une action aux boutons de suppression
		this.jqDialogDetailsJourBloque.find(".supprimer").click(function() {
			me.jqDialogDetailsJourBloque.dialog("close");

	    	var id = $(this).parents("tr").attr("data-id");
	    	
	    	confirm("Etes-vous sûr(e) de vouloir supprimer cette période bloquée : '" + me.jourBloqueGestion.joursBloquesTries[id].libelle + "' ?", function () {
		    	me.jourBloqueGestion.supprimerPeriodeBloquee(id, function() {
		    		me.actualiserPage(0);
		    	});
	    	});

		});
		
		// Affecte une action au bouton d'ajout
		this.jqDialogDetailsJourBloque.find("#btnAjouterPeriodeBloquee").click(function() {
			me.jqDialogDetailsJourBloque.dialog("close");
			me.dialogAjoutPeriodeBloquee.show(null, date, function (libelle, dateDebut, dateFin, listeGroupes) {
				me.jourBloqueGestion.ajouterPeriodeBloquee(libelle, dateDebut, dateFin, listeGroupes, false, function () {
		    		me.actualiserPage(0);
	    		});
			});
		});

		
		// Ouverture de la dialogue
		this.jqDialogDetailsJourBloque.dialog("open");
	};
	
	
	/**
	 * Affiche la bulle de gestion des vacances
	 * 
	 * @param {Array} listeVacances liste des vacances
	 */
	EcranJoursBloques.prototype.afficherDialogGestionVacances = function(listeVacances) {
		var me = this;
		
		// Préparation des données pour le template
		for (var i=0, maxI=listeVacances.length; i<maxI; i++) {
			var periode = listeVacances[i];
			
			// Ajoute des attributs string de date et heure - date de début
			periode.strDateDebut = $.fullCalendar.formatDate(new Date(periode.dateDebut), "dd/MM/yyyy");

			// Ajoute des attributs string de date et heure - date de fin
			periode.strDateFin = $.fullCalendar.formatDate(new Date(periode.dateFin), "dd/MM/yyyy");

			// Créer une chaîne de caractère des noms des groupes associés à ce jour
			var str = "";
			for (var j=0, maxJ=periode.listeGroupes.length; j<maxJ; j++) {
				if (str!="") str += ", ";
				str += periode.listeGroupes[j].nom;
			}
			periode.strGroupesAssocies = str;
			periode.strGroupesAssociesSmall = racourcirChaine(str, 25);
		}
		
		// Remplissage du template
		this.jqDialogGestionVacances.find("#dialog_gestion_vacances_hook").html(this.templateDialogVacances({elements: listeVacances}));

		// Positionnement de la dialogue
		this.jqDialogGestionVacances.dialog("option", {
			position: {
				my: "center bottom",
				at: "top-10",
				of: me.jqEcran.find("#bt_gestion_vacances")
			}
		});

		// Affecte une action aux boutons de modification
		this.jqDialogGestionVacances.find(".modifier").click(function() {
			me.jqDialogGestionVacances.dialog("close");
	    	var id = $(this).parents("tr").attr("data-id");
			
			me.dialogAjoutVacances.show(me.jourBloqueGestion.vacancesTriees[id], function (libelle, dateDebut, dateFin, listeGroupes) {
				me.jourBloqueGestion.modifierPeriodeBloquee(id, libelle, dateDebut, dateFin, listeGroupes, true, function () {
		    		me.actualiserPage(0);
	    		});
			});
		});
		
		// Affecte une action aux boutons de suppression
		this.jqDialogGestionVacances.find(".supprimer").click(function() {
			me.jqDialogGestionVacances.dialog("close");

	    	var id = $(this).parents("tr").attr("data-id");
	    	
	    	confirm("Etes-vous sûr(e) de vouloir supprimer cette période de vacances : '" + me.jourBloqueGestion.vacancesTriees[id].libelle + "' ?", function () {
		    	me.jourBloqueGestion.supprimerPeriodeBloquee(id, function() {
		    		me.actualiserPage(0);
		    	});
	    	});

		});
		
		// Affecte une action au bouton d'ajout
		this.jqDialogGestionVacances.find("#bt_ajouter_gestion_vacances").click(function() {
			me.jqDialogGestionVacances.dialog("close");
			me.dialogAjoutVacances.show(null, function (libelle, dateDebut, dateFin, listeGroupes) {
				me.jourBloqueGestion.ajouterPeriodeBloquee(libelle, dateDebut, dateFin, listeGroupes, true, function () {
		    		me.actualiserPage(0);
	    		});
			});
		});

		
		// Ouverture de la dialogue
		this.jqDialogGestionVacances.dialog("open");
	};
	

	/**
	 * Pends le début de la chaine et mets trois points si elle est trop longue
	 */
	function racourcirChaine(str, size) {
		var res;
		
		if (str.length <= size) {
			res = str;
		} else {
			res = str.substring(0, size) + " ...";
		}
		
		return res;
	}
	
	
	return EcranJoursBloques;
});