
/* Fonction d'entrée du programme. 
 * Le plugin davis est appelé par le mot-clé "davis" (configuré dans index.html)
 * Placer jquery en dernier dans la liste (ne se charge pas dans les arguments de la fonction) */
require(["lib/davis.min", "RestManager", "jquery"], function(Davis, RestManager) {
	/* Davis est chargé de manière locale avec le mot-clé "Davis" dans cette fonction (passé en argument) : 
	 * le plugin est configuré pour être chargé de cette manière dans le index.html
	 * 
	 * jquery est accessible de manière globale par $ (mais il faut tout de même préciser la dépendance
	 * dans les arguments de require() !), pour ne pas avoir de problème de dépendances (avec jQuery UI notamment) */
	
	var restManager = new RestManager();
	
	var init = function() {
	
		if(!restManager.isConnected()) {
			chargerInterfaceConnection();
		}
		else {
			chargerInterfacePrincipale();
		}
	};
	
	var chargerInterfaceConnection = function() {
		// Récupération de l'interface depuis les templates
		require(["text!../templates/formulaire_connexion.html"], function(htmlFormulaireConnexion) {
			// Ajout au DOM
			$(htmlFormulaireConnexion).appendTo($("#main_content").empty());
			
			// Callback de connexion
			$("#btn_connexion").click(function(event) {
				event.preventDefault();
				var username = $("#txt_identifiant").val();
				var pass = $("#txt_password").val();
				$("#msg_identifiants_invalides").css("display", "none");
				
				// Connexion
				restManager.connexion(username, pass, function(success, identifiantsValides) {
					if(success) {
						if(identifiantsValides) {
							chargerInterfacePrincipale();
						}
						else {
							$("#msg_identifiants_invalides").css("display", "inline");
						}
					}
					else {
						alert("Erreur de connexion au serveur. Vérifiez votre connexion.");
					}
				});
			});
		});
	};
	
	var chargerInterfacePrincipale = function() {
		// A implémenter
		$("#main_content").fadeOut(200, function() {
			$(this).empty().append("<h1>Connecté. Affichage non implémenté</h1>").fadeIn(200);
		});
	};
	
	init();
});