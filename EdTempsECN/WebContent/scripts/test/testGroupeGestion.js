require(["GroupeGestion", "RestManager", "test/mockRestManager"], function(GroupeGestion, RestManager, mockRestManager) {
	
	var groupeGestion = null;
	
	module("GroupeGestion", {
		setup: function() {
			var restManager = new RestManager();
			
			groupeGestion = new GroupeGestion(restManager);
		},
		teardown: function() {
			
		}
	});
	
	asyncTest("queryAbonnementsEtNonAbonnements", function() {
		
		groupeGestion.queryAbonnementsEtNonAbonnements(function(resultCode, data) {
			deepEqual(resultCode, RestManager.resultCode_Success, "Succès de la requête émulée par mockjax");
			
			// Vérification de la correspondance du résultat avec les données de mockjax
			ok(data.groupesAbonnements, "Le résultat contient les groupes d'abonnements");
			ok(data.groupesNonAbonnements, "Le résultat contient les groupes de non abonnement");
			
			equal(data.groupesAbonnements.length, 2, "Le nombre de groupes abonnement correspond avec mockjax");
			equal(data.groupesNonAbonnements.length, 1, "Le nombre de groupes non abonnement correspond avec mockjax");
			
			equal(data.groupesAbonnements[0].id, 7, "Groupe abonnement correspondant avec mockjax");
			equal(data.groupesAbonnements[1].id, 6, "Groupe abonnement correspondant avec mockjax");
			equal(data.groupesNonAbonnements[0].id, 1, "Groupe non abonnement correspondant avec mockjax");
			
			start();
		});
	});
	
	asyncTest("sAbonner", function() {
		
		groupeGestion.sAbonner(12347, function(resultCode) {
			deepEqual(resultCode, RestManager.resultCode_Success, "Succès de la requête émulée par mockjax");
			
			// Vérification qu'une requête d'abonnement pour le groupe 12347 a été effectuée
			var calls = mockRestManager.getMockedCalls("sabonner", "POST");
			
			var nbAppels = 0;
			for(var i=0, maxI=calls.length; i<maxI; i++) {
				if(calls[i].data.idGroupe === 12347) {
					nbAppels++;
				}
			}
			
			deepEqual(nbAppels, 1, "Une unique requête d'abonnement effectuée pour l'ID de groupe de test");
				
			start();
		});
	});
	
	asyncTest("seDesabonner", function() {
		
		groupeGestion.seDesabonner(12347, function(resultCode) {
			deepEqual(resultCode, RestManager.resultCode_Success, "Succès de la requête émulée par mockjax");
			
			// Vérification qu'une requête d'abonnement pour le groupe 12347 a été effectuée
			var calls = mockRestManager.getMockedCalls("sedesabonner", "POST");
			
			var nbAppels = 0;
			for(var i=0, maxI=calls.length; i<maxI; i++) {
				if(calls[i].data.idGroupe === 12347) {
					nbAppels++;
				}
			}
			
			deepEqual(nbAppels, 1, "Une unique requête de désabonnement effectuée pour l'ID de groupe de test");
				
			start();
		});
	});
	
});