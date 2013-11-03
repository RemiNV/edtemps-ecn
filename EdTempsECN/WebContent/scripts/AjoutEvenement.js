define(["CalendrierGestion", "RestManager", "jquery", "jqueryui"], function(CalendrierGestion, RestManager) {
	
	function AjoutEvenement(restManager, jqDialog) {
		
		this.jqDialog = jqDialog;
		this.restManager = restManager;
		this.strOptionsCalendriers = null; // HTML à ajouter au select pour sélectionner les calendriers
		
		// Initialisation de la dialog
		jqDialog.dialog({
			autoOpen: false,
			modal: true,
			width: 600
		});
	};
	
	AjoutEvenement.prototype.show = function() {
		
		this.jqDialog.dialog("open");
		
		// Récupération des calendriers auxquels l'utilisateur peut ajouter des évènements
		this.jqDialog.find("#btn_valider_ajout_evenement").attr("disabled", "disabled");
		this.jqDialog.find("#dialog_ajout_evenement_chargement").css("display", "block");
		this.jqDialog.find("#dialog_ajout_evenement_message_chargement").html("Chargement des options de matériel...");
		
		var calendrierGestion = new CalendrierGestion(this.restManager);
		
		var me = this;
		
		calendrierGestion.listerMesCalendriers(function(resultCode, data) {
			if(resultCode === RestManager.resultCode_Success) {
				var strRemplissageSelect = "";
					
				for(var i=0, max=data.length; i<max; i++) {
					strRemplissageSelect += "<option value='" + data[i].id + "'>" + data[i].nom + "</option>\n";
				}
				
				me.strOptionsCalendriers = strRemplissageSelect;
				me.jqDialog.find("#calendriers_evenement .select_calendriers").html(strRemplissageSelect);
				
				me.jqDialog.find("#btn_valider_ajout_evenement").removeAttr("disabled");
			}
			else if(resultCode === RestManager.resultCode_NetworkError) {
				window.showToast("Erreur de récupération de vos agendas ; vérifiez votre connexion");
			}
			else {
				window.showToast("Erreur de récupération de vos agendas");
			}
			
			me.jqDialog.find("#dialog_ajout_evenement_chargement").css("display", "none");
		});
		
	};
	
	return AjoutEvenement;
});