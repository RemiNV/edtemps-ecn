$(document).ready(function() {

	$("#modifier_utilisateur_types").multiSelect({
		afterInit: function(ms){
			$(".ms-container").css("width", 700);
			$(".ms-container").css("margin-bottom", 20);
		},
		selectableHeader: "<p class='titre_multi-select'>Toutes les types d'utilisateurs :</p>",
		selectionHeader: "<p class='titre_multi-select'>Les types auxquels l'utilisateur est rattach&eacute; :</p>"
	});


});