require(["RechercheSalle", "text!../templates/page_accueil.html", "RestManager", "test/mockRestManager", "qunit"], 
		function(RechercheSalle, pageAccueilHtml, RestManager, mockRestManager) {
	
	var jqDialog = null;
	var rechercheSalle = null;
	
	module("Dialog de recherche de salle", {
		setup: function() {
			var pageAccueil = $(pageAccueilHtml).appendTo($("#qunit-fixture"));
			jqDialog = pageAccueil.find("#form_chercher_salle");
			
			var restManager = new RestManager();
			rechercheSalle = new RechercheSalle(restManager, jqDialog);
		},
		teardown: function() {
			// qUnit vide #qunit-fixture tout seul mais ne supprime pas le html ajouté par la dialog
			jqDialog.dialog("destroy").remove();
			$("#qunit-fixture").children().remove();
		}
	});
	
	test("Initialisation", function() {
		expect(0);
		
		rechercheSalle.init();
	});
	
	asyncTest("Liste de matériels correspondant aux données du mockRestManager", function() {
		
		rechercheSalle.init();
		
		// Attente de la disponibilité des matériels dans le listing
		function launchTest() {
			if(jqDialog.find("#form_chercher_salle_valid").attr("disabled")) {
				// Attente avant de relancer le test
				setTimeout(launchTest, 100);
			}
			else {
				// D'après le mockRestManager, il doit y avoir des ordis et rétros en option de matériels
				
				var tdLibelles = jqDialog.find("#form_chercher_salle_liste_materiel td.libelle");
				strictEqual(tdLibelles.length, 2, "Nombre de matériels affichés correspondant à ceux du mockRestManager");
				
				tdLibelles.each(function() {
					var txtMateriel = $(this).text();
					ok(txtMateriel == "Ordinateur" || txtMateriel == "Vidéoprojecteur", "Matériel " + txtMateriel + " correspondant à ceux du mockRestManager");
				});
				
				
				start();
			}
			
		};
		
		launchTest();
	});
	
	asyncTest("Recherche", function() {
		
		rechercheSalle.init();
		
		// Attente de la disponibilité des matériels dans le listing
		function launchTest() {
			if(jqDialog.find("#form_chercher_salle_valid").attr("disabled")) {
				// Attente avant de relancer le test
				setTimeout(launchTest, 100);
			}
			else {
				// Ajout de 2 vidéoprojecteurs dans les matériels
				var trMateriel = jqDialog.find("#form_chercher_salle_liste_materiel tr");
				
				var materielTrouve = false;
				
				trMateriel.each(function() {
					if($(this).find("td.libelle").text() == "Vidéoprojecteur") {
						$(this).find("input").val(2);
						materielTrouve = true;
					}
				});
				
				ok(materielTrouve, "Vidéoprojecteur nécessaire ajouté avec succès");
				
				// Validation de la demande
				$("#form_chercher_salle_valid").trigger("click");
				
				// TODO : vérifier que la requête est effectuée avec les bons paramètres
				// mockRestManager.getMockedCalls("recherchesallelibre", "GET")
				
				start();
			}
			
		};
		
		
		jqDialog.find("#form_recherche_salle_date").val("31/10/2013");
		jqDialog.find("#form_recherche_salle_debut").val("12:45");
		jqDialog.find("#form_recherche_salle_fin").val("13:45");
		jqDialog.find("#form_recherche_salle_capacite").val("42");
		
		launchTest();
		
	});
	
	
	
});