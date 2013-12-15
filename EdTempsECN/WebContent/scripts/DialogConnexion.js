/**
 * Module de contrôle de la boîte de dialogue de connexion
 * @module DialogConnexion
 */
define(["RestManager", "jquery", "jqueryui"], function(RestManager) {
	
	/**
	 * Initialisation de l'interface de connexion.
	 * jqParent : bloc parent contenant le HTML nécessaire (chargé depuis les templates)
	 * 
	 * @constructor
	 * @alias module:DialogConnexion
	 */
	var DialogConnection = function(restManager, jqDialog) {
		this.restManager = restManager;
		this.jqDialog = jqDialog;
		this.connectionSuccess = false;
		
		var me = this;
		// Interface sous forme de dialog
		jqDialog.dialog({
			width: 700,
			modal: true,
			autoOpen: false,
			show: {
				effect: "fade",
				duration: 200
			},
			hide: {
				effect: "explode",
				duration: 200
			},
			beforeClose: function(event, ui) {
				return me.allowClose || me.connectionSuccess;
			},
			close: function(event, ui) {
				if(!me.connectionSuccess) {
					me.connectionCallback(false);
				}
			}
		});
		
		var me = this;

		// Callback de connexion
		jqDialog.find("#form_connexion").submit(function(event) {
			
			// On n'appelle pas preventDefault : le submit se fait dans une iframe cachée
			// (pour déclencher le "se souvenir des identifiants ?" du navigateur)
			var username = jqDialog.find("#txt_identifiant").val();
			
			// Enregistrement du username pour les reconnexions
			if(window.localStorage) {
				localStorage["username"] = username;
			}
			
			var pass = jqDialog.find("#txt_password").val();
			jqDialog.find("#msg_erreur").css("display", "none");
			jqDialog.find("#msg_connexion").css("display", "block");
			$(this).attr("disabled", "disabled");
			
			// Connexion
			restManager.connexion(username, pass, function(resultCode) {
				jqDialog.find("#msg_connexion").css("display", "none");
				jqDialog.find("#btn_connexion").removeAttr("disabled");
				
				switch(resultCode) {
				case RestManager.resultCode_Success:
					// Redirection vers la page d'agenda
					me.connectionSuccess = true;
					jqDialog.dialog("close");
					break;
					
				case RestManager.resultCode_NetworkError:
					jqDialog.find("#msg_erreur").html("Erreur de connexion au serveur. Vérifiez votre connexion.").css("display", "inline");
					break;
					
				case RestManager.resultCode_LdapError:
					jqDialog.find("#msg_erreur").html("Erreur de connexion au serveur LDAP").css("display", "inline");
					break;
				
				case RestManager.resultCode_IdentificationError:
					jqDialog.find("#msg_erreur").html("Identifiants invalides").css("display", "inline");
					break;
					
				default:
					jqDialog.find("#msg_erreur").html("Erreur du serveur").css("display", "inline");
					break;
				}
				
				me.connectionCallback(resultCode == RestManager.resultCode_Success);
			});
		});
	};
	
	/**
	 * Affichage de la dialog de connexion
	 * @param {string} title Titre à assigner à la dialog
	 * @param {function} connectionCallback Callback de connexion (obligatoire)
	 */
	DialogConnection.prototype.show = function(title, connectionCallback, isReconnection) {
		
		if(isReconnection) {
			this.allowClose = true;
			if(window.localStorage) {
				// Pré-remplissage du nom d'utilisateur (reconnexion)
				this.jqDialog.find("#txt_identifiant").val(window.localStorage["username"]).prop("disabled", true);
			}
			else {
				// Reconnexion compatible uniquement avec les navigateurs supportant localStorage
				connectionCallback(false);
				return;
			}
		}
		else {
			this.allowClose = false;
			this.jqDialog.find("#txt_identifiant").prop("disabled", false);
		}
		
		this.connectionSuccess = false;
		this.connectionCallback = connectionCallback;

		this.jqDialog.find("#btn_connexion").removeAttr("disabled");
		this.jqDialog.dialog("option", "title", title);
		this.jqDialog.dialog("open");
	};
	
	return DialogConnection;
});

