define([], function() {

	var EvenementGestion = function() {
	
	};

	/**
	 * Listing des évènements auxquels un utilisateur est abonné. Donne aussi les calendriers et groupes auxquels il est abonné.
	 * Exemple de format de l'objet fourni : 
	 * { evenements:
		[
			{id: 1, nom:"cours THERE", dateDebut: 1384708500000, dateFin: 1384712100000, calendriers: [7], 
				salles: [{id: 123, nom: "Salle B1", batiment: "B", capacite: 30, niveau: 1, numero: 1, materiels: []}]},
			{id: 5, nom:"cours THERF", dateDebut: 1384798500000, dateFin: 1384802100000, calendriers: [54],
				salles: [{id: 155, nom: "Salle B2", batiment: "B", capacite: 30, niveau: 1, numero: 2, materiels: []}]}
					
		],
	 *   calendriers: 
	 * 	[
	 * 		{id: 7, nom:"THERE Groupe L", type: "TD", matiere: "THERE", proprietaires: [1,5,8,7]},
			{id: 54, nom:"THERF Groupe L", type: "TD", matiere: "THERF", proprietaires: [24]}
	 * 	],
	 *   groupes: 
	 * 	[
	 * 		{id: 42, nom:"Groupe L", parentId: 24, rattachementAutorise: true, estCours: true, estCalendrierUnique: false, 
	 * 			calendriers: [1, 5], proprietaires: [2]},
	 * 		{id: 24, nom:"Promo B", parentId: 12, rattachementAutorise: false, estCours: true, estCalendrierUnique: false, 
	 * 			calendriers: [7], proprietaires: [2]},
	 * 		{id: 12, nom:"EI1", parentId: 6, rattachementAutorise: false, estCours: true, estCalendrierUnique: false, 
	 * 			calendriers: [], proprietaires: [2]},
	 * 		{id: 12, nom:"Elèves ingénieur", parent: null, rattachementAutorise: false, estCours: true, 
	 * 			estCalendrierUnique: false, calendriers: [], proprietaires: [2]},
	 * 	]
	 * }
	 */
	EvenementGestion.prototype.listerEvenementsAbonnement = function(dateDebut, dateFin, callback) {
	
	
	};
	
	return EvenementGestion;
});