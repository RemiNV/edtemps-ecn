define(["RestManager"], function(RestManager) {

	var EvenementGestion = function(restManager) {
		this.restManager = restManager
	};

	/**
	 * Listing des évènements auxquels un utilisateur est abonné. Donne aussi les calendriers et groupes auxquels il est abonné.
	 * Arguments : 
	 * dateDebut/dateFin : intervalle de recherche pour les évènements
	 * callback : fonction appelée une fois la requête effectuée. Paramètres de callback : 
	 * - networkSuccess (booléen) : succès de la connexion réseau
	 * - resultCode (entier) : code de retour de la requête. Vaut RestManager.resultCode_Success en cas de succès.
	 * 	Non fourni si networkSuccess vaut false.
	 * - data : objet contenant les évènements, calendriers et groupes demandés.
	 * 	Non fourni si networkSuccess vaut false, ou que resultCode != RestManager.resultCode_Success
	 * 
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
		this.restManager.effectuerRequete("GET", "abonnements", {
			token: this.restManager.getToken(),
			debut: dateDebut.getTime(),
			fin: dateFin.getTime()
		}, function(networkSuccess, data) {
			if(networkSuccess) {
				if(data.resultCode == RestManager.resultCode_Success) {
					callback(true, data.resultCode, data.data);
				}
				else {
					callback(true, data.resultCode);
				}
			}
			else
				callback(false);
		});
	
	};
	
	return EvenementGestion;
});