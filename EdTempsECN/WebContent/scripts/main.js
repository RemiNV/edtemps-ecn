
/* Fonction d'entrée du programme. 
 * Le plugin davis est appelé par le mot-clé "davis" (configuré dans index.html)
 * Placer jquery en dernier dans la liste (ne se charge pas dans les arguments de la fonction) */
require(["lib/davis.min", "RestManager", "lib/davis.hashrouting", "jquery"], function(Davis, RestManager) {
	/* Davis est chargé de manière locale avec le mot-clé "Davis" dans cette fonction (passé en argument) : 
	 * le plugin est configuré pour être chargé de cette manière dans le index.html
	 * 
	 * jquery est accessible de manière globale par $ (mais il faut tout de même préciser la dépendance
	 * dans les arguments de require() !), pour ne pas avoir de problème de dépendances (avec jQuery UI notamment) */
	
	var restManager = new RestManager();
	
	var init = function() {
	
		// Plugin hashrouting : routage par hash (le serveur ne contient qu'une page, pas d'accès possible sans JS)
		Davis.extend(Davis.hashRouting({ forceHashRouting: true })); 
		
		/*** Routes de l'application ***/
		this.app = Davis(function() {
			
			// Page principale
			this.get("agenda", function(req) {
				if(restManager.isConnected()) { // RestManager.checkConnection() ou RestManager.connection() appelé
					chargerInterfacePrincipale();
				}
				else {
					req.redirect("connexion/agenda");
				}
			});
			
			// Page de paramètres
			this.get("parametres", function(req) {
				if(restManager.isConnected()) { // RestManager.checkConnection() ou RestManager.connection() appelé
					chargerInterfaceParametres();
				}
				else {
					req.redirect("connexion/parametres");
				}
			});
			
			// Page de connexion
			this.get("connexion/:target", function(req) {

				// Déjà connecté ?
				restManager.checkConnection(function(networkSuccess, validConnection) {
					if(networkSuccess && validConnection) {
						req.redirect(req.params["target"]); // Déjà connecté : redirection
					}
					else {
						chargerInterfaceConnection();
					}
				});
			});
			
			// Page racine : redirection vers la page de connexion
			this.get("/", function(req) {
				req.redirect("connexion/agenda");
			});
		});
		
		this.app.start();
		
		// Parsing de la position actuelle
		Davis.location.assign(Davis.location.current());
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
				restManager.connection(username, pass, function(success, identifiantsValides) {
					if(success) {
						if(identifiantsValides) {
							// Redirection vers la page d'agenda
							Davis.location.assign("agenda");
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
	
	/**
	 * Effectue une transition d'interface par fadeOut - fadeIn : 
	 * 1) fadeOut, et chargement des dépendances en parallèle
	 * 2) Animation terminée et dépendances chargées : appel de callback
	 * 3) Callback terminé : fadeIn
	 * Arguments : 
	 * - dependencies : tableau de chaînes indiquant les dépendances à charger par requirejs
	 * - callback : fonction appelée à l'étape 2), avec les dépendances demandées chargées en argument */
	var transitionInterface = function(dependencies, callback) {
		var jqBody = $("body");
		jqBody.fadeOut(200);

		require(dependencies, function() {
		
			var obtainedDependencies = arguments;
		
			// A n'exécuter que si l'animation terminée
			jqBody.queue(function(next) {
				callback.apply(jqBody.get(0), obtainedDependencies);
				
				next();
			});
			
			jqBody.fadeIn(200); // Ajouté à la suite de la queue (après la fonction précédente)
		});
	};
	
	var chargerInterfacePrincipale = function() {
	
		transitionInterface(["EcranAccueil", "text!../templates/page_accueil.html"], function(EcranAccueil, pageAccueilHtml) {
			$("body").empty().append($(pageAccueilHtml))
			
			// Initialisation
			new EcranAccueil().init();
		});
	};
	
	var chargerInterfaceParametres = function() {
		transitionInterface(["EcranParametres", "text!../templates/page_parametres.html"], function(EcranParametres, pageAccueilHtml) {
			$("body").empty().append($(pageAccueilHtml))
			
			// Initialisation
			new EcranParametres().init();
		});
	};
	
	init();
});