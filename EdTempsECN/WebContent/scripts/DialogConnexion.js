define(["RestManager", "jquery", "jqueryui"], function(RestManager) {
	
	/**
	 * Initialisation de l'interface de connexion.
	 * jqParent : bloc parent contenant le HTML nécessaire (chargé depuis les templates)
	 */
	var DialogConnection = function(restManager, jqDialog, connectionCallback) {
		this.restManager = restManager;
		this.jqDialog = jqDialog;
		
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
			}
		});
		
		var me = this;
		
		// Callback de connexion
		jqDialog.find("#btn_connexion").click(function(event) {
			event.preventDefault();
			var username = jqDialog.find("#txt_identifiant").val();
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
					jqDialog.dialog("close");
					Davis.location.assign("agenda");
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
				
				if(me.connectionCallback) {
					me.connectionCallback(resultCode == RestManager.resultCode_Success);
				}
			});
		});
	};
	
	DialogConnection.prototype.show = function(title, connectionCallback) {
		this.connectionCallback = connectionCallback;
		this.jqDialog.dialog("option", "title", title);
		this.jqDialog.dialog("open");
	};
	
	return DialogConnection;
});

