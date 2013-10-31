
/**
 * Configuration supplémentaire pour requireJS
 */

requirejs.config({
	
	paths: {
		mockjax: "test/jquery.mockjax",
		qunit: "test/qunit-1.12.0"
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
require(["qunit"], function(q) {
	

	QUnit.config.autostart = false;
	/* QUnit utilise normalement l'évènement onLoad, mais est ici chargé de façon asynchrone par requireJS,
	 * il faut appeler manuellement la méthode load()
	 */
	QUnit.load();
	
	// Simulation de window.showToast
	window.showToast = function(text) {
		console.log("[Tests unitaires] Toast demandé par l'application : " + text);
	};
	
	require([ 
	          "test/mockRestManager",
	          // Chargement des tests
	         "test/testRestManager",
	         "test/testDialogConnexion",
	         "test/testRechercheSalle"
	         ],
			function(mockRestManager) {
		
		mockRestManager.mock(); // Simulation des requêtes AJAX
		
		QUnit.start(); // Lancement des tests
	});
});