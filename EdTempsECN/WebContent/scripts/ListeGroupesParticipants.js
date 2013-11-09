/**
 * @module ListeGroupesParticipants
 */
define([ "RestManager", "jqueryrotate" ], function(RestManager) {

	/**
	 * @constructor
	 * @alias module:ListeGroupesParticipants
	 */
	var ListeGroupesParticipants = function(restManager, calendrier, jqListe) {
		this.restManager = restManager;
		this.jqListe = jqListe;

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

		// Liste des groupes ouverts dans l'arborescence avec la mémoire localStorage (les autres seront fermés par défaut)
		this.arbreGroupesOuverts = new Object();
		if (localStorage && localStorage["ArborescenceOuverte"]) {
			// Parse du localStorage sur les virgules
			var idsGroupes = localStorage["ArborescenceOuverte"].split(",");
			for (var i = 0, maxI=idsGroupes.length ; i<maxI ; i++) {
				// Si un id de groupe est présent, c'est qu'il doit être ouvert
				this.arbreGroupesOuverts[idsGroupes[i]] = true;
			}
		}
		
		// Liste des groupes non triés des abonnements de l'utilisateur
		this.listeGroupes = null;

		// Liste des groupes repérés par leur identifiant
		this.groupes = null;

		// Accès à l'objet calendrier pour rafraichir l'affichage
		this.calendrier = calendrier;

		// Liste des groupes d'un calendrier (calendriers repérés par ID dans l'objet)
		// uniquement les calendriers auxquels l'utilisateur est abonné
		this.groupesCalendriers = null;
		
		// Tableau qui contient un arbre des groupes pour facilite l'affichage de l'arborescence
		this.arbre = null;
		
		// Ignore les appels à afficherBlocVosAgendas si déjà affiché
		this.estAffiche = false;
	};


	/**
	 * Initialise gestionnaire d'affichage des agendas
	 * 
	 * @param groupes
	 */
	ListeGroupesParticipants.prototype.initBlocVosAgendas = function(groupes) {

		// Initialise la liste des groupes avec les groupes passés en paramètres
		this.listeGroupes=groupes;
		
		this.groupesCalendriers = new Object();
		this.groupes = new Object();
		this.arbre = new Object();
		
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

		// Appelle la méthode de création de l'arbre à partir des cours en vrac
		this.creerArborescenceDesGroupes(groupes);

	};

	
	ListeGroupesParticipants.prototype.afficherNoeud = function(noeud, str) {

		// Nombre de fils
		var nbFils=noeud.fils.length;
		
		if (noeud.id==0) {
			// Si c'est le premier noeud, seulement l'affichage des fils
			for (var i = 0 ; i<nbFils ; i++) {
				str = this.afficherNoeud(noeud.fils[i], str);
			}
		} else {

			// Début du paquet de groupe
			str += "<div>";
			
			// S'il y a des fils, affichage d'une icone pour dérouler
			if ( nbFils > 0 ){
				str += "<img class='liste_groupes_triangle' src='./img/triangle.png' title='Cliquer pour afficher/cacher l&apos;arborescence' id='liste_groupes_triangle_"+noeud.id+"' data-groupe-id='"+noeud.id+"' />";
			}

			// Affiche les checkbox en fonction de l'état d'affichage du groupe
			str += "<input data-groupe-id='" + noeud.id + "' class='liste_groupes_checkbox' type='checkbox' title='Cliquer pour afficher/cacher les événements de cet agenda' ";
			str += this.groupes[noeud.id].affiche ? "checked >" : " >";

			// Affiche le nom du groupe
			str += this.groupes[noeud.id].nom;

			if ( nbFils > 0 ) {
				// Début du sous-groupe pour ouvrir/fermer
				str += "<div class='liste_groupes_sous_groupe' id='liste_groupes_sous_groupe_"+noeud.id+"' data-groupe-id='"+noeud.id+"'>";

				// Pour chaque fils, appel de la méthode
				for (var i = 0 ; i<nbFils ; i++) {
					str = this.afficherNoeud(noeud.fils[i], str);
				}

				// Fin du sous-bloc
				str += "</div>";
			}

			// Fin du paquet
			str += "</div>";
		}
		
		return str;

	};


	/**
	 * Affiche le bloc Vos agendas sur la page.
	 * Ne s'exécute plus si a déjà été appelé, et que clear() n'a pas été appelé
	 */
	ListeGroupesParticipants.prototype.afficherBlocVosAgendas = function() {

		if(this.estAffiche)
			return;
		
		var me = this;

		// Affiche l'arbre dans la zone "Vos agendas"
		this.jqListe.html(this.afficherNoeud(this.arbre, ""));

		// Initialise l'ouverture de l'arborescence avec le localStorage
		this.jqListe.find(".liste_groupes_sous_groupe").each(function(i) {
			var idGroupe = $(this).attr("data-groupe-id");
			// Si le groupe doit être fermé
			if (!me.arbreGroupesOuverts[idGroupe]) {
				me.jqListe.find("#liste_groupes_sous_groupe_" + $(this).attr("data-groupe-id")).hide();
				me.jqListe.find("#liste_groupes_triangle_" + $(this).attr("data-groupe-id")).rotate(-90);
			}
		});
		
		// Listener pour les checkbox
		this.jqListe.find(".liste_groupes_checkbox").click(function() {
			me.afficheCacheAgenda($(this).attr("data-groupe-id"));
		});

		// Listener pour les triangles
		this.jqListe.find(".liste_groupes_triangle").click(function() {
			me.afficheCacheArborescence($(this).attr("data-groupe-id"));
		});

		this.estAffiche = true;
	};
	
	/**
	 * Vide la liste de groupes de participants, et autorise un nouvel appel à afficherBlocVosAgendas
	 */
	ListeGroupesParticipants.prototype.clear = function() {
		this.estAffiche = false;
		this.jqListe.children().remove();
	};

	/**
	 * Méthode appellée lors du click sur un triangle
	 * 
	 * @param groupeId
	 * 			identifiant du groupe
	 */
	ListeGroupesParticipants.prototype.afficheCacheArborescence = function(groupeId) {
		var sousGroupe = this.jqListe.find("#liste_groupes_sous_groupe_" + groupeId);

		if (sousGroupe.is(":visible")) {
			
			// Cache le bloc
			sousGroupe.slideUp(200);
			
			// Tourne le triangle en position horizontale
			this.jqListe.find("#liste_groupes_triangle_" + groupeId).rotate(-90);
			
			// Met à jour le tableau des groupes qui doivent être ouverts 
			this.arbreGroupesOuverts[groupeId]=false;
			
		} else  {
			
			// Affiche le bloc
			sousGroupe.slideDown(300);

			// Tourne le triangle en position verticale
			this.jqListe.find("#liste_groupes_triangle_" + groupeId).rotate(0);

			// Met à jour le tableau des groupes qui doivent être ouverts 
			this.arbreGroupesOuverts[groupeId]=true;
			
		}

		// Met à jour le LocalStorage
		if (localStorage) {
			var localStor = "";
			for (var id in this.arbreGroupesOuverts) {
				if (this.arbreGroupesOuverts[id]) {
					if (localStor!="") localStor += ","+id; 
					else localStor = id;
				}
			}
			localStorage["ArborescenceOuverte"] = localStor;
		}
	};


	/**
	 * Méthode appellée lors du click sur les checkbox
	 * 
	 * @param groupeId
	 * 			identifiant du groupe
	 */
	ListeGroupesParticipants.prototype.afficheCacheAgenda = function(groupeId) {

		// Changement de la valeur affiche pour ce groupe
		this.groupes[groupeId].affiche = !this.groupes[groupeId].affiche;
		
		// Ajout dans la liste des groupes masqués
		this.groupesMasques[groupeId] = !this.groupes[groupeId].affiche;

		// Met à jour le LocalStorage
		if (localStorage) {
			var localStor = "";
			for (var idGroupe in this.groupesMasques) {
				if (this.groupesMasques[idGroupe]) {
					if (localStor!="") localStor += ","+idGroupe; 
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
	 * @return les événements filtrés
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


	/**
	 * Créer l'arbre des groupes pour l'affichage de l'arborescence
	 */
	ListeGroupesParticipants.prototype.creerArborescenceDesGroupes = function(groupes) {

		// Copie du tableau des groupes dans une variable temporaire pour ne pas impacter le reste de l'application
		var copieGroupes = groupes.slice();

		// Liste des groupes ordonnée en vrac et indexée par l'identifiant des groupes.
		// cela permet d'accéder à un groupe plus facilement.
		var listeNoeuds = new Array();

		// Initialisation de l'arbre
		this.arbre.id = 0;
		this.arbre.fils = new Array();
		
		// Tant qu'il reste des groupes à traiter
		while (copieGroupes.length > 0) {

			// Pour chaque groupe restant
			for (var i = 0, maxI = copieGroupes.length ; i < maxI ; i++ ) {

				if (copieGroupes[i].parentId==0) {
					// Si le groupe n'a pas de parent

					// Création d'un nouveau
					var noeud = new Object();
					noeud.id = copieGroupes[i].id;
					noeud.fils = new Array();

					// Ajout du noeud dans l'arbre, à la suite
					this.arbre.fils.push(noeud);

					// Ajout du noeud dans la liste indexée par les identifiants de groupe
					listeNoeuds[copieGroupes[i].id] = noeud;

					// Suppression du groupe dans le tableau des groupes restant à traiter
					copieGroupes.splice(i, 1);
					
					// Retourne à la boucle while
					break;

				} else if (listeNoeuds[copieGroupes[i].parentId]) {
					// Si le groupe a un parent et que ce parent est présent dans l'arbre

					// Création d'un nouveau noeud sans fils
					var noeud = new Object();
					noeud.id = copieGroupes[i].id;
					noeud.fils = new Array();

					// Ajout du noeud dans le tableau des fils du parent
					listeNoeuds[copieGroupes[i].parentId].fils.push(noeud);
					
					// Ajout le noeud dans la liste des noeuds de l'arbre
					listeNoeuds[copieGroupes[i].id]=noeud;
					
					// Suppression du groupe dans le tableau des groupes restant à traiter
					copieGroupes.splice(i, 1);

					// Retourne à la boucle while
					break;
					
				}
				
			}
		}
		
	};

	
	return ListeGroupesParticipants;

});