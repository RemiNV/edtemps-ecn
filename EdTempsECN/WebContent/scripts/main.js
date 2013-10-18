
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
	
	var currentPage = { nom: null, manager: null };
	
	var init = function() {
	
		// Plugin hashrouting : routage par hash (le serveur ne contient qu'une page, pas d'accès possible sans JS)
		Davis.extend(Davis.hashRouting({ forceHashRouting: true })); 
		
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
				restManager.checkConnection(function(networkSuccess, validConnection) {
					if(networkSuccess && validConnection) {
						req.redirect(req.params["target"]); // Déjà connecté : redirection
					}
					else {
						chargerInterfaceConnection();
					}
				});
			});
			
			this.get("deconnexion", function(req) {
				restManager.deconnexion(function(networkSuccess, resultCode) {
					if(!networkSuccess) {
						alert("Erreur réseau : vérifiez votre connexion.");
						req.redirect("agenda");
						return;
					}
					
					if(resultCode != RestManager.resultCode_Success && resultCode != RestManager.resultCode_IdentificationError) {
						alert("Erreur de la déconnexion. Code retour : " + resultCode);
						req.redirect("agenda");
						return;
					}
					
					// Pas d'erreur
					req.redirect("connexion/agenda");
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
			$(htmlFormulaireConnexion).appendTo($("body").empty());
			currentPage.manager = null;
			currentPage.nom = "connexion";
			
			// Callback de connexion
			$("#btn_connexion").click(function(event) {
				event.preventDefault();
				var username = $("#txt_identifiant").val();
				var pass = $("#txt_password").val();
				$("#msg_erreur").css("display", "none");
				$("#msg_connexion").css("display", "block");
				$(this).attr("disabled", "disabled");
				
				// Connexion
				restManager.connexion(username, pass, function(success, resultCode) {
					$("#msg_connexion").css("display", "none");
					$("#btn_connexion").removeAttr("disabled");
					if(success) {
						switch(resultCode) {
						case RestManager.resultCode_Success:
							// Redirection vers la page d'agenda
							Davis.location.assign("agenda");
							break;
							
						case RestManager.resultCode_LdapError:
							$("#msg_erreur").html("Erreur de connexion au serveur LDAP").css("display", "inline");
							break;
						
						case RestManager.resultCode_IdentificationError:
							$("#msg_erreur").html("Identifiants invalides").css("display", "inline");
							break;
							
						default:
							$("#msg_erreur").html("Erreur du serveur").css("display", "inline");
							break;
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
	
	var chargerInterfacePrincipale = function(vue) {
	
		transitionInterface(["EcranAccueil", "text!../templates/page_accueil.html"], function(EcranAccueil, pageAccueilHtml) {
			$("body").empty().append($(pageAccueilHtml));
			
			// Initialisation
			currentPage.manager = new EcranAccueil(restManager);
			currentPage.manager.init();
			currentPage.manager.setVue(vue);
			currentPage.nom = "agenda";
		});
	};
	
	var chargerInterfaceParametres = function() {
		transitionInterface(["EcranParametres", "text!../templates/page_parametres.html"], function(EcranParametres, pageAccueilHtml) {
			$("body").empty().append($(pageAccueilHtml));
			
			// Initialisation
			currentPage.manager = new EcranParametres(restManager);
			currentPage.manager.init();
			currentPage.nom="parametres";
		});
	};
	
	init();
});