require(["DialogConnexion", "RestManager", "text!../templates/formulaire_connexion.html", "qunit", "jquery", "jqueryui"], function(DialogConnexion, RestManager, htmlFormulaireConnexion) {
	
	module("Dialog de connexion", {
		setup: function() {
			$("<div id='dialogConnexionUnitTest'></div>").append(htmlFormulaireConnexion).appendTo($("#qunit-fixture"));
		},
		teardown: function() {
			// qUnit vide #qunit-fixture tout seul mais ne supprime pas le html ajouté par la dialog
			$("#dialogConnexionUnitTest").dialog("destroy").remove();
		}
	});
	
	asyncTest("Connexion valide", function() {
		
		// 2 assertions
		expect(2);
		
		var restManager = new RestManager();
		
		var connexionCallback = function(success) {
			ok(success, "Réussite de la connexion");
			console.log("Callback réussite");
			start();
		};
		
		var dialogConnexion = new DialogConnexion(restManager, $("#dialogConnexionUnitTest"));
		
		notEqual(dialogConnexion, null, "Dialog de connexion construite");
		
		console.log("début show réussite");
		dialogConnexion.show("Test de connexion", connexionCallback, false);
		console.log("fin show réussite");
		
		// Remplissage des identifiants
		$("#txt_identifiant").val("unitTest");
		$("#txt_password").val("unitTest");
		
		// Connexion
		$("#btn_connexion").trigger("click");
	});
	
	asyncTest("Connexion invalide", function() {
		// 2 assertions
		expect(2);
		
		var restManager = new RestManager();
		
		var connexionCallback = function(success) {
			ok(!success, "Echec de la connexion");
			console.log("Callback échec");
			start();
		};
		
		var dialogConnexion = new DialogConnexion(restManager, $("#dialogConnexionUnitTest"));
		
		notEqual(dialogConnexion, null, "Dialog de connexion construite");
		
		console.log("Début show échec");
		dialogConnexion.show("Test de connexion", connexionCallback, false);
		console.log("Fin show échec");
		
		// Remplissage des identifiants
		$("#txt_identifiant").val("unitTest");
		$("#txt_password").val("passwordinvalide");
		
		console.log("Demande clic échec");
		
		// Connexion
		$("#btn_connexion").trigger("click");
	});
	
});