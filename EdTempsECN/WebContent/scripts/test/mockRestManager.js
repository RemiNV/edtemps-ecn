define(["mockjax"], function() {
	
	var connectionToken = "tokenUnitTestMockjax";
	var resultCode_Success = 0;
	var resultCode_IdentificationError = 1;
	var resultCode_InvalidParams = 6;
	
	// "base de données" utilisée
	var evenements = [
	  	{"id":1,"nom":"OBJET","dateDebut":1382338800000,"dateFin":1382349600000,
			"calendriers":[1],"salles":[{"id":5,"nom":"Salle D03","batiment":"D","capacite":42,"niveau":3,"numero":1}],"intervenants":[],
			"responsables":[{"id":1,"nom":"ProfObjet","prenom":"EtGelol","email":null}]},
		{"id":2,"nom":"OBJET","dateDebut":1382529600000,"dateFin":1382540400000,
			"calendriers":[1],"salles":[{"id":1,"nom":"Salle info B12","batiment":"B","capacite":20,"niveau":2,"numero":12},
				{"id":2,"nom":"Salle info B13","batiment":"B","capacite":18,"niveau":2,"numero":13}],
			"intervenants":[],"responsables":[{"id":1,"nom":"ProfObjet","prenom":"EtGelol","email":null}]}
	];
	
	var calendriers = [
   		{"id":1,"nom":"OBJET TD","type":"TD","matiere":"OBJET","proprietaires":[1,6]},
		{"id":2,"nom":"GELOL TD","type":"TD","matiere":"GELOL","proprietaires":[1]}
	];
	
	var groupes = [
  		{"id":7,"nom":"EI3 Info","parentId":6,"rattachementAutorise":true,"estCours":true,"estCalendrierUnique":false,"calendriers":[1,2],"proprietaires":[5]},
		{"id":6,"nom":"EI3","parentId":1,"rattachementAutorise":false,"estCours":true,"estCalendrierUnique":false,"calendriers":[],"proprietaires":[5]},
		{"id":1,"nom":"Elèves ingénieur","parentId":0,"rattachementAutorise":false,"estCours":true,"estCalendrierUnique":false,"calendriers":[],"proprietaires":[5]}
	];
	
	var groupesAbonnements = new Array(groupes[0], groupes[1]);
	var groupesNonAbonnements = new Array(groupes[2]);

	var materiels = [
		{"id":1,"nom":"Ordinateur","quantite":0},
		{"id":2,"nom":"Vidéoprojecteur","quantite":0}
	];
	
	var salles = [
		{"id":1,"nom":"Salle info B12","batiment":"B","capacite":20,"niveau":2,"numero":12,"materiels":[{"id":1,"nom":"Ordinateur","quantite":10},{"id":2,"nom":"Vidéoprojecteur","quantite":1}]},
		{"id":3,"nom":"Amphi L","batiment":"L","capacite":200,"niveau":1,"numero":0,"materiels":[]},
		{"id":4,"nom":"Salle C02","batiment":"C","capacite":35,"niveau":2,"numero":1,"materiels":[{"id":2,"nom":"Vidéoprojecteur","quantite":10}]}
	];
	
	var getEvenementsIntervalle = function(dateDebut, dateFin) {
		var res = Array();
		
		for(var i=0, maxI = evenements.length; i<maxI; i++) {
			if(evenements[i].dateFin > dateDebut.getTime() && evenements[i].dateDebut < dateFin.getTime()) {
				res.push(evenements[i]);
			}
		}
		
		return res;
	};
	
	
	return {
		
		mock: function() {
			
			// Simulation de la connexion
			$.mockjax({
				url: "identification/connection",
				responseTime: 500,
				contentType: "application/json",
				type: "POST",
				response: function(settings) {
					
					// Seule connexion possible : userID et pass "unitTest"
					if(settings.data.username == "unitTest" && settings.data.password == "unitTest") {
						this.responseText = JSON.stringify({ resultCode: resultCode_Success, message: "", 
							data: {
								token: connectionToken
							}
						});
					}
					else {
						this.responseText = JSON.stringify({ resultCode: resultCode_IdentificationError, message: "", data: null });
					}
				}
			});
			
			
			// Simulation de la récupération des abonnements
			$.mockjax({
				url: "abonnements",
				responseTime: 500,
				contentType: "application/json",
				type: "GET",
				response: function(settings) {
					
					if(settings.data.token && settings.data.debut && settings.data.fin 
							&& settings.data.debut instanceof Date && settings.data.fin instanceof Date) {
						this.responseText = JSON.stringify({ resultCode: resultCode_Success, message: "", 
							data: {
								calendriers: calendriers,
								groupes: groupes,
								evenements: getEvenementsIntervalle(settings.data.debut, settings.data.fin)
							}
						});
					}
					else {
						// Mauvais paramètres
						this.responseText = JSON.stringify({ resultCode: resultCode_InvalidParams, message: "", data: null });
					}
				}
			});
			
			// Simulation de la récupération des évènements d'abonnement
			$.mockjax({
				url: "evenements",
				responseTime: 500,
				contentType: "application/json",
				type: "GET",
				response: function(settings) {
					
					if(settings.data.token && settings.data.debut && settings.data.fin 
							&& settings.data.debut instanceof Date && settings.data.fin instanceof Date) {
						this.responseText = JSON.stringify({ resultCode: resultCode_Success, message: "", 
							data: getEvenementsIntervalle(settings.data.debut, settings.data.fin)
						});
					}
					else {
						// Mauvais paramètres
						this.responseText = JSON.stringify({ resultCode: resultCode_InvalidParams, message: "", data: null });
					}
				}
			});
			
			// Simulation de la récupération des matériels
			$.mockjax({
				url: "listemateriels",
				responseTime: 500,
				contentType: "application/json",
				type: "GET",
				responseText: JSON.stringify({resultCode: resultCode_Success, message:"", data:
					{ listeMateriels: materiels }
				})
			});
			
			// Simulation de la requête de récupération des abonnements & non abonnements
			$.mockjax({
				url: "abonnementsetnonabonnements",
				responseTime: 500,
				contentType: "application/json",
				type: "GET",
				responseText: JSON.stringify({resultCode: resultCode_Success, message: "", data: {
					groupesAbonnements: groupesAbonnements,
					groupesNonAbonnements: groupesNonAbonnements
					}
				})
			});
			
			// Simulation de la requête d'abonnement
			$.mockjax({
				url: "sabonner",
				responseTime: 500,
				contentType: "application/json",
				type: "POST",
				responseText: JSON.stringify({resultCode: resultCode_Success, message: "", data: null})
			});
			
			// Simulation de la requête de désabonnement
			$.mockjax({
				url: "sedesabonner",
				responseTime: 500,
				contentType: "application/json",
				type: "POST",
				responseText: JSON.stringify({resultCode: resultCode_Success, message: "", data: null})
			});
			
			// Simulation de la requête de recherche de salle
			$.mockjax({
				url: "recherchesallelibre",
				responseTime: 500,
				contentType: "application/json",
				type: "GET",
				responseText: JSON.stringify({resultCode: resultCode_Success, message: "", data: {
					sallesDisponibles: salles
				}})
			});
			
			
			console.log("*** Utilisation de mockjax. Les requêtes AJAX jQuery sont interceptées par le mockRestManager pour les tests unitaires ***");
		},
		
		/**
		 * Récupération des appels Ajax interceptés jusqu'à maintenant
		 * @param filtreUrl Filtre optionnel sur l'URL de requête pour les appels à renvoyer
		 * @param filtreTypeRequete Filtre optionnel sur le type de requête pour les appels à renvoyer
		 * @returns
		 */
		getMockedCalls: function(filtreUrl, filtreTypeRequete) {
			var res = new Array();
			
			var calls = $.mockjax.mockedAjaxCalls();
			
			for(var i=0, maxI=calls.length; i<maxI; i++) {
				if((!filtreUrl || filtreUrl == calls[i].url)
					&& (!filtreTypeRequete || filtreTypeRequete == calls[i].type)) {
					res.push(calls[i]);
				}
			}
			
			return res;
		}
	};
	
	
});