/**
 * Test unitaires JavaScript : script point d'entrée (voir test.html)
 */
require(["test/qunit-1.12.0"], function(q) {
	

	QUnit.config.autostart = false;
	/* QUnit utilise normalement l'évènement onLoad, mais est ici chargé de façon asynchrone par requireJS,
	 * il faut appeler manuellement la méthode load()
	 */
	QUnit.load();
	
	require([ // Chargement des tests
	         "test/testRestManager"
	         ],
			function() {
		QUnit.start(); // Lancement des tests
	});
});