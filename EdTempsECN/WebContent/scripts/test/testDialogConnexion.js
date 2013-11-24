require(["DialogConnexion", "RestManager", "text!../index.html", "jquery", "jqueryui"], function(DialogConnexion, RestManager, htmlIndex) {
	
	var jqDialog = null;
	
	module("Dialog de connexion", {
		setup: function() {
			
			var res = /<!-- UNITTEST_CONNECTION_DIALOG -->([\s\S]*)<!-- UNITTEST_CONNECTION_DIALOG -->/.exec(htmlIndex);
			var htmlFormulaireConnexion = res[1];
			
			jqDialog = $("<div id='dialogConnexionUnitTest'></div>").append(htmlFormulaireConnexion).appendTo($("#qunit-fixture"));
		},
		teardown: function() {
			// qUnit vide #qunit-fixture tout seul mais ne supprime pas le html ajouté par la dialog
			jqDialog.dialog("destroy").remove();
		}
	});
	
	asyncTest("Test avec identifiants corrects", function() {
		// 2 assertions
		expect(2);
		
		var restManager = new RestManager();
		var dialogConnexion = new DialogConnexion(restManager, jqDialog);
		
		var connexionCallback = function(success) {
			ok(success, "Réussite de la connexion");
			start(); // Toutes les assertions ont été appelées (même dans les callback) : bilan du test
		};
		
		notEqual(dialogConnexion, null, "Dialog de connexion construite");
		
		dialogConnexion.show("Test de connexion", connexionCallback, false);
		
		// Remplissage des identifiants
		jqDialog.find("#txt_identifiant").val("unitTest");
		jqDialog.find("#txt_password").val("unitTest");
		
		// Connexion
		jqDialog.find("#btn_connexion").trigger("click");
	});
	
	asyncTest("Test avec identifiants incorrects", function() {
		// 2 assertions
		expect(2);
		
		var restManager = new RestManager();
		var dialogConnexion = new DialogConnexion(restManager, jqDialog);
		
		var connexionCallback = function(success) {
			ok(!success, "Echec de la connexion");
			start(); // Toutes les assertions ont été appelées (même dans les callback) : bilan du test
		};
		
		notEqual(dialogConnexion, null, "Dialog de connexion construite");
		
		dialogConnexion.show("Test de connexion", connexionCallback, false);
		
		// Remplissage des identifiants
		
		jqDialog.find("#txt_identifiant").val("unitTest");
		jqDialog.find("#txt_password").val("passwordinvalide");
		
		// Connexion
		jqDialog.find("#btn_connexion").trigger("click");
	});
	
});