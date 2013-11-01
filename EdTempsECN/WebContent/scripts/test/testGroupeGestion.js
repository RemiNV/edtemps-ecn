require(["GroupeGestion", "RestManager", "test/mockRestManager", "qunit"], function(GroupeGestion, RestManager, mockRestManager) {
	
	var groupeGestion = null;
	
	module("GroupeGestion", {
		setup: function() {
			var restManager = new RestManager();
			
			groupeGestion = new GroupeGestion(restManager);
		},
		teardown: function() {
			
		}
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