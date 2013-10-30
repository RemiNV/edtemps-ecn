
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
require(["test/qunit-1.12.0"], function(q) {
	

	QUnit.config.autostart = false;
	/* QUnit utilise normalement l'évènement onLoad, mais est ici chargé de façon asynchrone par requireJS,
	 * il faut appeler manuellement la méthode load()
	 */
	QUnit.load();
	
	require([ 
	          "test/mockRestManager",
	          // Chargement des tests
	         "test/testRestManager"
	         ],
			function(mockRestManager) {
		
		mockRestManager.mock();
		
		QUnit.start(); // Lancement des tests
	});
});