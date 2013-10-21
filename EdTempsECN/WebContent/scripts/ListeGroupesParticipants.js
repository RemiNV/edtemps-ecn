define([ "RestManager" ], function(RestManager) {

	/**
	 * Constructeur
	 */
	var ListeGroupesParticipants = function(restManager, calendrier) {
		this.restManager = restManager;

		// Liste des groupes masqués initialisée avec la mémoire localStorage du navigateur
		this.groupesMasques = new Object();
		if (localStorage && localStorage["GroupesMasques"]) {
			// Parse du localStorage sur les virgules
			var idMasques = localStorage["GroupesMasques"].split(",");
			for (var i = 0, maxI=idMasques.length ; i<maxI ; i++) {
				// Si un id de groupe est présent, c'est qu'il doit être masqué
				this.groupesMasques[idMasques[i]] = true;
			}
		}
		
		// Liste des groupes non triés des abonnements de l'utilisateur
		this.listeGroupes = new Array();

		// Liste des groupes repérés par leur identifiant
		this.groupes = new Object();
		
		// Accès à l'objet calendrier pour rafraichir l'affichage
		this.calendrier = calendrier;
		
		
		// Liste des groupes d'un calendrier (calendriers repérés par ID dans l'objet)
		// uniquement les calendriers auxquels l'utilisateur est abonné
		this.groupesCalendriers = new Object();
		
	};


	/**
	 * Initialise gestionnaire d'affichage des agendas
	 * 
	 * @param groupes
	 */
	ListeGroupesParticipants.prototype.initBlocVosAgendas = function(groupes) {
		
		// Initialise la liste des groupes avec les groupes passés en paramètres
		this.listeGroupes=groupes;
		
		// Pour chaque groupe, ajout d'un attribut affiche qui indique si ses événements doivent être affichés
		for (var i = 0, maxI=groupes.length ; i < maxI ; i++) {
			
			// Récupère la valeur de l'affichage dans la liste des groupes masqués (provenant de localStorage)
			this.listeGroupes[i].affiche = (this.groupesMasques[groupes[i].id] !== true);
			
			// Alimente la liste des groupes repérés par leur identifiant
			this.groupes[groupes[i].id] = groupes[i];
			
			// Pour chaque calendrier du groupe
			for(var j=0, maxJ=groupes[i].calendriers.length; j<maxJ; j++) {
				var idCalendrier = groupes[i].calendriers[j];

				// Initialisation d'une liste de groupes auxquels sont rattachés le calendrier en cours de traitement
				if(!this.groupesCalendriers[idCalendrier]) {
					this.groupesCalendriers[idCalendrier] = new Array();
				}

				// Alimentation de la liste des groupes auxquels sont rattachés le calendrier en cours de traitement
				this.groupesCalendriers[idCalendrier].push(groupes[i]);
			}
			
		}
		
	};


	/**
	 * Affiche le bloc Vos agendas sur la page
	 */
	ListeGroupesParticipants.prototype.afficherBlocVosAgendas = function() {
		var me = this;
		
		// Génération du code html pour afficher la liste des agendas
		var html = "";
		for (var i = 0, max = this.listeGroupes.length ; i < max ; i++) {
			// Image du checkbox en fonction de l'état d'affichage du groupe
			var image = this.listeGroupes[i].affiche ? "<img src='./img/checkbox_on.png' />" : "<img src='./img/checkbox_off.png' />";
			
			// Ajout dans la variable de code html
			html += "<span data-id-groupe='" + this.listeGroupes[i].id + "' class='afficher_cacher_groupe'>" + image + this.listeGroupes[i].nom + "</span><br/>";			
		}
		$("#liste_groupes").html(html);
		
		// Affectation à chaque checkbox de la liste une action au clic
		$(".afficher_cacher_groupe").click(function() {
			me.afficheCacheAgenda($(this).attr("data-id-groupe"), this);
		});
	};


	/**
	 * Méthode appellée lors du click sur les checkbox
	 * 
	 * @param groupeId
	 * 			id du groupe à afficher/cacher
	 * @param span
	 * 			objet jQuery qui contient l'image à changer
	 */
	ListeGroupesParticipants.prototype.afficheCacheAgenda = function(groupeId, span) {
		
		// Changement de la valeur affiche pour ce groupe
		this.groupes[groupeId].affiche = !this.groupes[groupeId].affiche;
		
		// Ajout dans la liste des groupes masqués
		this.groupesMasques[groupeId] = !this.groupes[groupeId].affiche;
		
		// Changement de l'image
		var src = this.groupes[groupeId].affiche ? "./img/checkbox_on.png" : "./img/checkbox_off.png";
		$(span).find("img:first-child").attr("src", src);
		
		// Met à jour le LocalStorage
		if (localStorage) {
			var localStor = null;
			for (var idGroupe in this.groupesMasques) {
				if (this.groupesMasques[idGroupe]) {
					if (localStor!=null) localStor += ","+idGroupe; 
					else localStor = idGroupe;
				}
			}
			localStorage["GroupesMasques"] = localStor;
		}
		
		// Rafraichit le calendrier
		this.calendrier.refetchEvents();
	};


	/**
	 * Filtre les événements à afficher
	 * 
	 * @param evenements
	 *			tous les événements 
	 *
	 * @returns les événements filtrés
	 */
	ListeGroupesParticipants.prototype.filtrerEvenementsGroupesActifs = function(evenements) {
		
		// Initialisation de la liste des événements à afficher
		var res = new Array();
		
		// Pour chaque événement de la liste passée en paramètre
		for (var i = 0, maxI = evenements.length ; i < maxI ; i++) {
			// Initialisation d'un flag pour arrêter le traitement d'un événement s'il doit être affiché
			var eventOk = false;
			
			// Pour chaque calendrier lié à cet événement
			for (var j = 0, maxJ = evenements[i].calendriers.length ; j < maxJ && !eventOk ; j++) {
				
				 // Si le calendrier existe pour l'utilisateur (il y est abonné indirectement)
				if(this.groupesCalendriers[evenements[i].calendriers[j]]) {
					
					// Pour chaque groupe lié à ce calendrier
					for (var k = 0, maxK = this.groupesCalendriers[evenements[i].calendriers[j]].length ; k<maxK && !eventOk ; k++) {
						
						// Si l'événement est lié à un groupe qui doit être affiché
						if(this.groupesCalendriers[evenements[i].calendriers[j]][k].affiche) {
							// Ajout de l'événement dans la liste des événements à afficher
							res.push(evenements[i]);
							
							// Levé du flag pour arrêter le traitement de cet événement
							eventOk = true;
						}
						
					}
				}
				
			}
			
		}
		
		// Retourne la liste des événements à afficher
		return res;
	};

	return ListeGroupesParticipants;

});