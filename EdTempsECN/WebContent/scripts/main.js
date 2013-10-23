
/* Fonction d'entrée du programme. 
 * Le plugin davis est appelé par le mot-clé "davis" (configuré dans index.html)
 * Placer jquery en dernier dans la liste (ne se charge pas dans les arguments de la fonction) */
require(["lib/davis.min", "RestManager", "text!../templates/formulaire_connexion.html", "DialogConnexion", 
         "lib/davis.hashrouting", "jquery"], function(Davis, RestManager, htmlFormulaireConnexion, DialogConnexion) {
	/* Davis est chargé de manière locale avec le mot-clé "Davis" dans cette fonction (passé en argument) : 
	 * le plugin est configuré pour être chargé de cette manière dans le index.html
	 * 
	 * jquery est accessible de manière globale par $ (mais il faut tout de même préciser la dépendance
	 * dans les arguments de require() !), pour ne pas avoir de problème de dépendances (avec jQuery UI notamment) */
	

	var currentPage = { nom: null, manager: null };
	
	var restManager = new RestManager();
	
	var init = function() {
		
		/**
		 * Reconnexion dynamique après erreur de requête à cause d'un problème d'identification
		 */
		var reconnectionFallback = function(callback) {
			dialogConnexion.show("Session expirée : reconnexion", callback);
		};
		
		restManager.setIdentificationErrorFallback(reconnectionFallback);
	
		// Plugin hashrouting : routage par hash (le serveur ne contient qu'une page, pas d'accès possible sans JS)
		Davis.extend(Davis.hashRouting({ forceHashRouting: true })); 
		
		// Initialisation de la dialog de connexion
		var jqDialog = $("#connection_dialog");
		$(htmlFormulaireConnexion).appendTo(jqDialog.empty());
		var dialogConnexion = new DialogConnexion(restManager, jqDialog);
		
		/*** Routes de l'application ***/
		this.app = Davis(function() {
			
			// Page principale
			var routePagePrincipale = function(req) {
				if(restManager.isConnected()) { // RestManager.checkConnection() ou RestManager.connection() appelé
					if(currentPage.nom != "agenda")
						chargerInterfacePrincipale(req.params["vue"]);
					else
						currentPage.manager.setVue(req.params["vue"]);
				}
				else {
					if(req.params["vue"])
						req.redirect("connexion/agenda/" + req.params["vue"]);
					else
						req.redirect("connexion/agenda");
				}
				
			};
			this.get("agenda/:vue", routePagePrincipale); // Valeurs pour :vue : "mes_abonnements", "mes_evenements", "vue_groupe", "vue_salle"
			this.get("agenda", routePagePrincipale);
			
			// Page de paramètres
			this.get("parametres", function(req) {
				if(restManager.isConnected()) { // RestManager.checkConnection() ou RestManager.connexion() appelé
					chargerInterfaceParametres();
				}
				else {
					req.redirect("connexion/parametres");
				}
			});
			
			// Page de connexion
			this.get("connexion/*target", function(req) {
				// Déjà connecté ?
				restManager.checkConnection(function(resultCode) {
					if(resultCode == RestManager.resultCode_Success) {
						req.redirect(req.params["target"]); // Déjà connecté : redirection
					}
					else {
						chargerInterfaceConnection(dialogConnexion);
					}
				});
			});
			
			this.get("deconnexion", function(req) {
				restManager.deconnexion(function(resultCode) {
					if(resultCode == RestManager.resultCode_NetworkError) {
						alert("Erreur réseau : vérifiez votre connexion.");
						req.redirect("agenda");
					}
					else if(resultCode != RestManager.resultCode_Success && resultCode != RestManager.resultCode_IdentificationError) {
						alert("Erreur de la déconnexion. Code retour : " + resultCode);
						req.redirect("agenda");
					}
					else {
						// Pas d'erreur
						req.redirect("connexion/agenda");
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
	
	/**
	 * Effectue une transition d'interface par fadeOut - fadeIn : 
	 * 1) fadeOut, et chargement des dépendances en parallèle
	 * 2) Animation terminée et dépendances chargées : appel de callback
	 * 3) Callback terminé : fadeIn
	 * Arguments : 
	 * - dependencies : tableau de chaînes indiquant les dépendances à charger par requirejs
	 * - callback : fonction appelée à l'étape 2), avec les dépendances demandées chargées en argument */
	var transitionInterface = function(dependencies, callback) {
		var jqInterface = $("#main_interface_hook");
		
		// Pas d'utilisation de .fadeOut() pour éviter le display: none (incompatible avec chargement fullcalendar)
		jqInterface.animate({ opacity: 0 }, 200); 

		require(dependencies, function() {
		
			var obtainedDependencies = arguments;
		
			// A n'exécuter que si l'animation terminée
			jqInterface.queue(function(next) {
				callback.apply(jqInterface.get(0), obtainedDependencies);
				
				next();
			});
			
			jqInterface.animate({ opacity: 1 }, 200); // Ajouté à la suite de la queue (après la fonction précédente)
		});
	};
	
	var chargerInterfaceConnection = function(dialogConnexion) {
		// Suppression de l'interface actuelle
		$("#main_interface_hook").empty();
		dialogConnexion.show("Connexion");
		
		currentPage.manager = dialogConnexion;
		currentPage.nom = "connexion";
	};
	
	var chargerInterfacePrincipale = function(vue) {
	
		transitionInterface(["EcranAccueil", "text!../templates/page_accueil.html"], function(EcranAccueil, pageAccueilHtml) {
			$("#main_interface_hook").empty().append(pageAccueilHtml);
			
			// Initialisation
			currentPage.manager = new EcranAccueil(restManager);
			currentPage.manager.init();
			currentPage.manager.setVue(vue);
			currentPage.nom = "agenda";
		});
	};
	
	var chargerInterfaceParametres = function() {
		transitionInterface(["EcranParametres", "text!../templates/page_parametres.html"], function(EcranParametres, pageAccueilHtml) {
			$("#main_interface_hook").empty().append($(pageAccueilHtml));
			
			// Initialisation
			currentPage.manager = new EcranParametres(restManager);
			currentPage.manager.init();
			currentPage.nom="parametres";
		});
	};
	
	init();
});