define(["jquery", "jqueryui"], function() {
	
	var AjoutEvenement = function(jqDialog) {
		
		this.jqDialog = jqDialog;
		
		// Initialisation de la dialog
		jqDialog.dialog({
			autoOpen: false,
			modal: true,
			width: 600
		});
	};
	
	AjoutEvenement.prototype.show = function() {
		
		// Récupération des calendriers auxquels l'utilisateur peut ajouter des évènements
		// TODO : besoin du listing des calendriers appartenant à l'utilisateur côté serveur
		
		
		
		this.jqDialog.dialog("open");
		
		
	};
	
	return AjoutEvenement;
});