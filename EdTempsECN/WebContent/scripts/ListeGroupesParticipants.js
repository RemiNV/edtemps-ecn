define([ "RestManager" ], function(RestManager) {

	/** Tableau à trois colonnes :
	 * 	     0 - identifiant du groupe
	 *       1 - nom du groupe
	 *       2 - liste des id des calendriers associés
	 *       3 - liste des id des événements associés
	 *       4 - true si affiché et false sinon
	 */
	this.vosAgendas;

	/**
	 * Mise en cache des événements qui sont cachés
	 */
	this.cacheAffichage;

	/**
	 * Constructeur
	 */
	var ListeGroupesParticipants = function(restManager) {
		this.restManager = restManager;
	};

	/**
	 * Ne retourne que les événements qui sont à afficher
	 * 
	 * @param data
	 * 			tous les événements de tous les agendas
	 * @returns les événements à afficher
	 */
	ListeGroupesParticipants.prototype.getGroupesActifsFetchEvents = function(data, callback) {

		// Affiche tous les événements
		callback(data);
		
		// Cache les événements à cacher, c'est à dire ceux qui sont présent dans le tableau de cache
		for (var i = 0 ; i < data.length ; i++) {
			if (this.estEnCache(data[i].id)) {
				$("#calendar").fullCalendar('removeEvents', data[i].id);
			}
		}
		
	};
	
	/**
	 * Affiche le bloc "Vos agendas" de l'écran d'accueil avec la liste des groupes de participants
	 * récupérés en base de donneés
	 * 
	 * @param data
	 */
	ListeGroupesParticipants.prototype.afficherBlocVosAgendas = function(data) {
		var me = this;

		// Récupération des noms des groupes et des identifiants et rangement dans un tableau
		this.vosAgendas = new Array(data.groupes.length);
		for (var i = 0 ; i < data.groupes.length ; i++) {
			this.vosAgendas[i] = new Array(data.groupes[i].id, data.groupes[i].nom, data.groupes[i].calendriers, me.listeIdEvenements(data.evenements, data.groupes[i].calendriers), true);
		}

		// Organisation par ordre alphabétique
		this.vosAgendas.sort(function(a, b) { return (a[1] < b[1] ? -1 : (a[1] > b[1] ? 1 : 0)); });

		// Génération du code html pour afficher la liste des agendas
		var html = "";
		for (var i = 0 ; i < this.vosAgendas.length ; i++) {
			// Image du checkbox en fonction de l'état d'affichage du groupe
			var image = "";
			if (this.vosAgendas[i][4]) image = "<img src='./img/checkbox_on.png' />";
			else image = "<img src='./img/checkbox_off.png' />";
			
			// Ajout dans la variable de code html
			html += "<span data-id-agenda='" + i + "' id='bloc_vos_agendas_" + i + "' class='afficher_cacher_groupe'>" + image + this.vosAgendas[i][1] + "</span><br/>";			
		}
		$("#liste_groupes").html(html);
		
		// Affectation à chaque checkbox de la liste une action au clic
		$(".afficher_cacher_groupe").click(function() {
			me.afficheCacheAgenda($(this).attr("data-id-agenda"));
		});

		this.cacheAffichage = new Array();
	};
	
	
	/**
	 * Récupère la liste des id des événements associés à une liste de calendriers
	 * 
	 * @param listeEvenements
	 * 			liste des événements à trier
	 * 
	 * @param listeIdCalendrier
	 * 			liste des calendriers à teste
	 * 
	 * @returns la liste des id des événements correspondants
	 */
	ListeGroupesParticipants.prototype.listeIdEvenements = function(listeEvenements, listeIdCalendrier) {

		// Initialisation du résultat
		var listeId = new Array();

		// Parcours des événements de la liste
		for (var i = 0 ; i < listeEvenements.length ; i++) {
			// Parcours des calendriers de l'événement
			for (var j = 0 ; j < listeEvenements[i].calendriers.length ; j++) {
				// Vérification si le calendrier est dans la liste des calendriers à tester
				if (jQuery.inArray(listeEvenements[i].calendriers[j], listeIdCalendrier)>-1) {
					listeId.push(listeEvenements[i].id);
				}
			}
		}

		// Retourne la liste des identifiants d'événements
		return listeId;
	};
	
	
	
	/**
	 * Vérifie si un événement est dans la cache des événements cachés
	 */
	ListeGroupesParticipants.prototype.estEnCache = function(idEvenement) {

		for (var j = 0 ; j < this.cacheAffichage.length ; j++) {
			if (this.cacheAffichage[j][0].id==idEvenement) {
				return true;
			}
		}

		return false;

	};
		
	/**
	 * Change l'icone et met à jour le tableau des valeurs affiche/cache pour l'agenda sélectionné
	 * 
	 * @param idTabVosAgendas
	 */
	ListeGroupesParticipants.prototype.afficheCacheAgenda = function(idTabVosAgendas) {

		if (this.vosAgendas[idTabVosAgendas][4]) {
			
			// Change l'image de la checkbox
			$("#bloc_vos_agendas_" + idTabVosAgendas + " img:first-child").attr("src", "./img/checkbox_off.png");

			// Change la valeur dans le tableau de "Vos agendas"
			this.vosAgendas[idTabVosAgendas][4] = false;

			// Supprime tous les événements liés à ce groupe dynamiquement dans l'affichage
			for (var i = 0 ; i<this.vosAgendas[idTabVosAgendas][3].length ; i++) {
				
				// Si l'élément n'est pas en cache, mise en cache pour réafficher plus tard
				if (!this.estEnCache(this.vosAgendas[idTabVosAgendas][3][i])) {
					this.cacheAffichage.push($("#calendar").fullCalendar('clientEvents', this.vosAgendas[idTabVosAgendas][3][i]));
				}
				
				// Suppression de l'affichage
				$("#calendar").fullCalendar('removeEvents', this.vosAgendas[idTabVosAgendas][3][i]);
			}
			
		} else {

			// Affiche le groupe dans la vue
			$("#bloc_vos_agendas_" + idTabVosAgendas + " img:first-child").attr("src", "./img/checkbox_on.png");
			this.vosAgendas[idTabVosAgendas][4] = true;

			// Affiche tous les événements liés à ce groupe dynamiquement dans l'affichage
			for (var i = 0 ; i < this.vosAgendas[idTabVosAgendas][3].length ; i++) {
				for (var j = 0 ; j < this.cacheAffichage.length ; j++) {
					if (this.cacheAffichage[j][0].id==this.vosAgendas[idTabVosAgendas][3][i]) {
						// Affichage
						$("#calendar").fullCalendar('addEventSource', this.cacheAffichage[j]);

						// Suppression de la cache pour garder la cohérence
						this.cacheAffichage.splice(j);
					}
				}
			}
			
		}

	};

	return ListeGroupesParticipants;
});