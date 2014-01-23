/* Fonction d'entrée du programme. 
 * Le plugin davis est appelé par le mot-clé "davis" (configuré dans index.html)
 * Placer jquery en dernier dans la liste (ne se charge pas dans les arguments de la fonction) */
require(["lib/stacktrace", "davis", "RestManager", "DialogConnexion", 
         "lib/davis.hashrouting", "jquery"], function(stacktrace, Davis, RestManager, DialogConnexion) {
	/* Davis est chargé de manière locale avec le mot-clé "Davis" dans cette fonction (passé en argument) : 
	 * le plugin est configuré pour être chargé de cette manière dans le index.html
	 * 
	 * jquery est accessible de manière globale par $ (mais il faut tout de même préciser la dépendance
	 * dans les arguments de require() !), pour ne pas avoir de problème de dépendances (avec jQuery UI notamment) */

	// Gestion d'erreurs signalées au serveur
	window.onerror = function(message, url, lineNb, colNb, e) {
		// Chrome supporte le passage de l'exception javascript
		var stack = false;
		if(e) {
			stack = stacktrace({ e: e }).join("\n");
		}
		
		var reportMessage = "erreur : " + message + "\nScript " + url + " ligne " + lineNb;
		if(colNb) {
			reportMessage += " colonne " + colNb;
		}
		if(stack) {
			reportMessage += "\nStack : \n" + stack;
		}
		
		if(window.console && console.log) {
			console.log("Erreur signalée au serveur : ", reportMessage);
		}
		$.ajax("logging", {
			type: "POST",
			data: {
				message: reportMessage
			}
		});
	};

	// Modification du comportement de requirejs pour les erreurs (ne pas signaler les erreurs de chargement au serveur)
	requirejs.onError = function(e) {
		// Pas d'erreur lancée pour les timeout : log manuel
		if(e.requireType == "timeout") {
			if(window.console && console.log) {
				console.log("requireJS : Timeout au chargement des scripts : ", e);
			}
		}
		else {
			throw e;
		}
	};

	var currentPage = { nom: null, manager: null };
	var restManager = new RestManager();
	
	/** Remplace toutes les infobulles par celle de jQuery UI */
	$(document).tooltip({
	    content: function() { return $(this).attr('title'); }, /* permet d'insérer du html dans le title */
	    show: {
	    	duration: 200
	    },
	    hide: {
	    	duration: 200
	    }
	});
	
	/**
	 * Affichage de messages de notification dans l'application
	 * Paramètre text : texte à afficher
	 */
	window.showToast = function(text) {
		$("#toast").queue(function(next) {
			$(this).html(text);
			next();
		}).animate({ bottom: 40 }, 500)
		.delay(3000)
		.animate({ bottom: -40 }, 500);
	};
	
	/**
	 * Dialogue personnalisée pour les confirmations
	 * 
	 * @param {String} mess Message à afficher
	 * @param {function} actionOui Action à effectuer en cas de clique sur le bouton "Oui"
	 * @param {function} actionNon Action à effectuer en cas de clique sur le bouton "Non"
	 */
	window.confirm = function(mess, actionOui, actionNon) {
		$("#confirm_text").html(mess);
		
		$("#confirm").dialog({
			autoOpen: true,
			width: 400,
			height: 150,
			modal: true,
			show: { effect: "fade", duration: 100 },
			hide: { effect: "fade", duration: 100 }
		});
		
		$("#confirm_oui").unbind("click").bind("click", function() {
			$("#confirm").dialog("close");
			if (actionOui!=null) actionOui();
		});
		
		$("#confirm_non").unbind("click").bind("click", function() {
			$("#confirm").dialog("close");
			if (actionNon!=null) actionNon();
		});
    };
    
    
	function init() {
		
		/**
		 * Reconnexion dynamique après erreur de requête à cause d'un problème d'identification
		 */
		var reconnectionFallback = function(callback) {
			dialogConnexion.show("Session expirée : reconnexion", callback, true);
		};
		
		restManager.setIdentificationErrorFallback(reconnectionFallback);
	
		// Plugin hashrouting : routage par hash (le serveur ne contient qu'une page, pas d'accès possible sans JS)
		Davis.extend(Davis.hashRouting({ forceHashRouting: true })); 
		
		// Initialisation de la dialog de connexion
		var jqDialog = $("#connection_dialog");
		var dialogConnexion = new DialogConnexion(restManager, jqDialog);
		
		/*** Routes de l'application ***/
		this.app = new Davis.App();
		
		this.app.configure(function(config) {
			config.formSelector = "form.davis";
		});
		
		var initApp = function() {
			
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
			this.get("parametres/:tab", function(req) {
				
				var tab = req.params["tab"];
				if(restManager.isConnected()) { // RestManager.checkConnection() ou RestManager.connexion() appelé
					if(currentPage.nom != "parametres") {
						chargerInterfaceParametres(tab);
					}
				}
				else {
					req.redirect("connexion/parametres/" + tab);
				}
			});
			
			// Page de planning des cours (pour les professeurs)
			this.get("planning_cours/:vue", function(req) {
				
				var vue = req.params["vue"];
				if(restManager.isConnected()) {
					if(currentPage.nom != "planning_cours") {
						chargerInterfacePlanningCours(vue);
					}
					else {
						currentPage.manager.setVue(vue);
					}
				}
				else {
					req.redirect("connexion/planning_cours/" + vue);
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
						chargerInterfaceConnection(dialogConnexion, req.params["target"]);
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
		};
		
		initApp.call(app);
		
		$("#div_chargement_application").remove();

		this.app.start();
		
		// Parsing de la position actuelle
		var currentLocation = Davis.location.current();
		app.lookupRoute("get", currentLocation).run(new Davis.Request(currentLocation));
	};
	
	/**
	 * Effectue une transition d'interface par fadeOut - fadeIn : 
	 * 1) fadeOut, et chargement des dépendances en parallèle
	 * 2) Animation terminée et dépendances chargées : appel de callback
	 * 3) Callback terminé : fadeIn
	 * Arguments : 
	 * - dependencies : tableau de chaînes indiquant les dépendances à charger par requirejs
	 * - callback : fonction appelée à l'étape 2), avec les dépendances demandées chargées en argument */
	function transitionInterface(dependencies, callback) {
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
	
	function chargerInterfaceConnection(dialogConnexion, target) {
		// Suppression de l'interface actuelle
		$("#main_interface_hook").empty();
		dialogConnexion.show("Connexion à l'emploi du temps", function(success) {
			if(success) {
				if(target) {
					Davis.location.assign(target);
				}
				else {
					Davis.location.assign("agenda");
				}
			}
		});
		
		currentPage.manager = dialogConnexion;
		currentPage.nom = "connexion";
	};
	
	function chargerInterfacePrincipale(vue) {
	
		transitionInterface(["EcranAccueil", "text!../templates/page_accueil.html"], function(EcranAccueil, pageAccueilHtml) {
			$("#main_interface_hook").empty().append(pageAccueilHtml);
			
			// Initialisation
			currentPage.nom = "agenda";
			currentPage.manager = new EcranAccueil(restManager);
			currentPage.manager.setVue(vue);
			currentPage.manager.init();
		});
	};
	
	function chargerInterfaceParametres(tab) {
		transitionInterface(["EcranParametres", "text!../templates/page_parametres.html"], function(EcranParametres, pageAccueilHtml) {
			$("#main_interface_hook").empty().append($(pageAccueilHtml));
			
			// Initialisation
			currentPage.nom="parametres";
			currentPage.manager = new EcranParametres(restManager);
			currentPage.manager.init(tab);
		});
	};
	
	function chargerInterfacePlanningCours(vue) {
		transitionInterface(["planning_cours/EcranPlanningCours", "text!../templates/page_planning_cours.html"], function(EcranPlanningCours, pagePlanningCoursHtml) {
			$("#main_interface_hook").empty().append($(pagePlanningCoursHtml));
			
			currentPage.nom = "planning_cours";
			currentPage.manager = new EcranPlanningCours(restManager);
			currentPage.manager.setVue(vue);
			// TODO : recharger la vue (normale ou groupes) depuis l'URL comme avec la page principale
		});
	};
	
	init();

});