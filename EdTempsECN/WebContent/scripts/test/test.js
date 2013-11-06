
/**
 * Configuration supplémentaire pour requireJS
 */

requirejs.config({
	
	paths: {
		mockjax: "test/jquery.mockjax"
	},
	
	shim: {
		"mockjax": {
			deps: ["jquery"]
		}
	}
});


/**
 * Test unitaires JavaScript : script point d'entrée (voir test.html)
 */

// Simulation de window.showToast
window.showToast = function(text) {
	console.log("[Tests unitaires] Toast demandé par l'application : " + text);
};

require([ 
          "test/mockRestManager",
         "test/testRestManager",
         "test/testDialogConnexion",
         "test/testRechercheSalle",
         "test/testGroupeGestion"
         ],
		function(mockRestManager) {
	
	mockRestManager.mock(); // Simulation des requêtes AJAX
	
	QUnit.start(); // Lancement des tests
});