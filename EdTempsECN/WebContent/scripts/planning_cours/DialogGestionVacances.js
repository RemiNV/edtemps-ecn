/**
 * Module de contrôle de la boîte de dialogue de gestion des vacances
 * Les modifications faites avec cette boîte de dialogue ne sont pas répercutées sur le calendrier année en live.
 * C'est seulement à la fermeture que le calendrier année est mis à jour.
 * 
 * @module DialogGestionVacances
 */
define([ "planning_cours/EcranJoursBloques" ], function(EcranJoursBloques) {

	/**
	 * @constructor
	 * @alias DialogGestionVacances
	 */
	var DialogGestionVacances = function(restManager, jqDialog, ecranJoursBloques) {
		this.restManager = restManager;
		this.ecranJoursBloques = ecranJoursBloques;
		this.jqDialog = jqDialog;
		this.jqTableau = jqDialog.find("#listeVacances");
		this.vacances = null;		// Liste des vacances à afficher
		this.callback = null;		// Méthode appellée à la fermeture, elle reçoit "needReload" en paramètre
		this.needReload = false;	// Booléen qui vaut vrai si il y a eu une modification
		
		this.initAppele = false;
	};

	
	/**
	 * Affiche la boîte de dialogue
	 * 
	 * @param {function} callback Méthode appelée en retour et recevant "needReload" en paramètre
	 */
	DialogGestionVacances.prototype.show = function(callback) {
		if(!this.initAppele) {
			this.init(callback);
			return;
		}
		
		// Récupère la méthode de callback
		this.callback = callback;
		
		// Récupère les vacances puis charge le contenu de la dialog
		var me = this;
		this.recupererVacances(function() {
			me.chargerContenuDialog();
		});
		
		// Ouvre la boîte de dialogue
		this.jqDialog.dialog("open");
	};
	
	
	/**
	 * Initialise la boîte de dialogue (appelée une seule fois)
	 *
	 * @param {function} callback Méthode appelée en retour et recevant "needReload" en paramètre
	 */
	DialogGestionVacances.prototype.init = function(callback) {
		var me=this;
		
		// Créer la boîte de dialogue
		this.jqDialog.dialog({
			autoOpen: false,
			appendTo: "#dialog_hook",
			width: 750,
			modal: true,
			title: "Gestion des périodes de vacances",
			show: { effect: "fade", duration: 200 },
			hide: { effect: "explode", duration: 200 }
		});
		
		// Listener du bouton "Fermer"
		this.jqDialog.find("#bt_fermer_gestion_vacances").click(function() {
			me.jqDialog.dialog("close");
			me.callback(me.needReload);
		});

		// Retourne à la méthode show()
		this.initAppele = true;
		this.show(callback);
	};
	

	/**
	 * Charge le contenur de la boîte de dialogue : rempli le tableau et initialise le formulaire
	 */
	DialogGestionVacances.prototype.chargerContenuDialog = function() {
		var me = this;
		
		if (this.vacances.length == 0) {
			this.jqTableau.html("Aucune période de vacances ...").show();
			return;
		}

		// Préparation du template pour un affichage uniforme
		var template = 
			"<% _.each(periodes, function(periode) { %> <tr data-id='<%= periode.id %>'>" +
				"<td class='libelle'><%= periode.libelle %></td>" +
				"<td class='groupes' title='<%= periode.strGroupesAssocies %>'><%= periode.strGroupesAssociesSmall %></td>" +
				"<td class='date'><%= periode.strDateDebut %></td>" +
				"<td class='date'><%= periode.strDateFin %></td>" +
				"<td class='actions'><img src='./img/modifier.png' title='Modifier' class='modifier' /><img src='./img/supprimer.png' title='Supprimer' class='supprimer' /></td>" +
			"</tr> <% }); %>";
		
		// Affichage du tableau
		me.jqTableau.html(_.template(template, {periodes: me.vacances}));

	    // Affecte une action aux boutons de modification
	    me.jqTableau.find(".modifier").click(function() {
	    	var id = $(this).parents("tr").attr("data-id");

	    });

	    // Affecte une action aux boutons de suppression
	    me.jqTableau.find(".supprimer").click(function() {
	    	var id = $(this).parents("tr").attr("data-id");
	    	
	    	confirm("Etes-vous sûr(e) de vouloir supprimer ces vacances ?", function () {

	    	});
	    });
		
	};
	
	
	/**
	 * Récupère les périodes de vacances dans le gestionnaire des jours bloqués
	 * Ajoute des attributs pour l'affichage
	 * 
	 * @param {function} callback Méthode appelée une fois le traitement terminé
	 */
	DialogGestionVacances.prototype.recupererVacances = function(callback) {
		
		this.vacances = this.ecranJoursBloques.jourBloqueGestion.vacances;
		
		for (var i=0, maxI=this.vacances.length; i<maxI; i++) {
			var periode = this.vacances[i];
			
			// Ajoute des attributs string de date et heure - date de début
			periode.strDateDebut = $.fullCalendar.formatDate(new Date(periode.dateDebut), "dd/MM/yyyy");
			periode.strHeureDebut = $.fullCalendar.formatDate(new Date(periode.dateDebut), "HH:mm");

			// Ajoute des attributs string de date et heure - date de fin
			periode.strDateFin = $.fullCalendar.formatDate(new Date(periode.dateFin), "dd/MM/yyyy");
			periode.strHeureFin = $.fullCalendar.formatDate(new Date(periode.dateFin), "HH:mm");

			// Créer une chaîne de caractère des noms des groupes associés à ce jour
			var str = "";
			for (var j=0, maxJ=periode.listeGroupes.length; j<maxJ; j++) {
				if (str!="") str += ", ";
				str += periode.listeGroupes[j].nom;
			}
			periode.strGroupesAssocies = str;
			periode.strGroupesAssociesSmall = racourcirChaine(str, 25);
		}
		
		callback();
		
	};
	
	function racourcirChaine(str, size) {
		var res;
		
		if (str.length <= size) {
			res = str;
		} else {
			res = str.substring(0, size) + " ...";
		}
		
		return res;
	}

	
	return DialogGestionVacances;

});
