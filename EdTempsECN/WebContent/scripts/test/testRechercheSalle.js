require(["RechercheSalle", "text!../templates/page_accueil.html", "RestManager", "test/mockRestManager", "qunit"], 
		function(RechercheSalle, pageAccueilHtml, RestManager, mockRestManager) {
	
	var jqFormChercherSalle = null;
	var jqResultatChercherSalle = null; // Dialog à destroy manuellement si elle est affichée
	var rechercheSalle = null;
	
	module("Dialog de recherche de salle", {
		setup: function() {
			var pageAccueil = $(pageAccueilHtml).appendTo($("#qunit-fixture"));
			var jqSectionChercherSalle = pageAccueil.find("#recherche_salle_libre");
			jqFormChercherSalle = jqSectionChercherSalle.find("#form_chercher_salle");
			jqResultatChercherSalle = jqSectionChercherSalle.find("#resultat_chercher_salle");
			
			var restManager = new RestManager();
			rechercheSalle = new RechercheSalle(restManager, jqSectionChercherSalle);
		},
		teardown: function() {
			// qUnit vide #qunit-fixture tout seul mais ne supprime pas le html ajouté par la dialog
			jqFormChercherSalle.dialog("destroy").remove();
			$("#qunit-fixture").children().remove();
		}
	});
	
	test("Initialisation", function() {
		
		deepEqual(jqFormChercherSalle.length, 1, "Unique dialog de recherche de salle #form_chercher_salle trouvée");
		deepEqual(jqResultatChercherSalle.length, 1, "Unique dialog de résultats #resultat_chercher_salle trouvée");
		
		rechercheSalle.init();
	});
	
	asyncTest("Liste de matériels correspondant aux données du mockRestManager", function() {
		
		rechercheSalle.init();
		
		// Attente de la disponibilité des matériels dans le listing
		function launchTest() {
			if(jqFormChercherSalle.find("#form_chercher_salle_valid").attr("disabled")) {
				// Attente avant de relancer le test
				setTimeout(launchTest, 100);
			}
			else {
				// D'après le mockRestManager, il doit y avoir des ordis et rétros en option de matériels
				
				var tdLibelles = jqFormChercherSalle.find("#form_chercher_salle_liste_materiel td.libelle");
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
			if(jqFormChercherSalle.find("#form_chercher_salle_valid").attr("disabled")) {
				// Attente avant de relancer le test
				setTimeout(launchTest, 100);
			}
			else {
				// Ajout de 2 vidéoprojecteurs dans les matériels
				var trMateriel = jqFormChercherSalle.find("#form_chercher_salle_liste_materiel tr");
				
				var materielTrouve = false;
				
				trMateriel.each(function() {
					if($(this).find("td.libelle").text() == "Vidéoprojecteur") {
						$(this).find("input").val(2);
						materielTrouve = true;
					}
				});
				
				ok(materielTrouve, "Vidéoprojecteur nécessaire ajouté avec succès");
				
				jqFormChercherSalle.find("#form_recherche_salle_date").val("2013-10-31");
				jqFormChercherSalle.find("#form_recherche_salle_debut").val("12:45");
				jqFormChercherSalle.find("#form_recherche_salle_fin").val("13:45");
				jqFormChercherSalle.find("#form_recherche_salle_capacite").val("42");
				
				// Validation de la demande
				jqFormChercherSalle.find("#form_chercher_salle_valid").trigger("click");
				
				var mockedCalls = mockRestManager.getMockedCalls("recherchesallelibre", "GET");
				deepEqual(mockedCalls.length, 1, "Une unique recherche de salle effectuée");
				
				equal(mockedCalls[0].data.capacite, 42, "Capacité de salle dans la requête correspondant au formulaire");
				equal(mockedCalls[0].data.date, "2013-10-31", "Date dans la requête correspondant au formulaire");
				equal(mockedCalls[0].data.heureDebut, "12:45", "Heure début dans la requête correspondant au formulaire");
				equal(mockedCalls[0].data.heureFin, "13:45", "Heure fin dans la requête correspondant au formulaire");
				
				// Vérification du matériel
				var materiels = mockedCalls[0].data.materiel.split(",");
				
				for(var i=0, maxI = materiels.length; i<maxI; i++) {
					var mat = materiels[i].split(":");
					// Aucun matériel, sauf les vidéoprojecteurs (2)
					ok(mat[1] == 0 || (mat[0] == 2 && mat[1] == 2), "Matériel de la requête correspondant au formulaire");
				}
				
				
				function fermerDialogResultats() {
					// Attente fin de la recherche (réactivation du bouton) pour fermer la dialog 
					if(jqResultatChercherSalle.hasClass("ui-dialog-content") && jqResultatChercherSalle.dialog("isOpen")) {
						jqResultatChercherSalle.dialog("destroy").remove();
						
						start();
					}
					else {
						setTimeout(fermerDialogResultats, 100);
					}
				}
				
				fermerDialogResultats();
			}
			
		};
		
		launchTest();
		
	});
	
	
	
});