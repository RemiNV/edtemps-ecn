$(document).ready(function() {

	$(".types_utilisateurs_modifier_select").multiSelect({
		afterInit: function(ms){
			$(".ms-container").css("width", 720);
			$(".ms-container").css("margin-top", 0);
			ms.hide();
		},
		selectableHeader: "<p class='titre_mutli-select'>Toutes les actions :</p>",
		selectionHeader: "<p class='titre_mutli-select'>Actions autoris&eacute;es pour '<span></span>' :</p>"
	});

});

/**
 * Affiche le multi select pour le type voulu
 * @param typeId L'identifiant du type
 * @param typeNom Nom du type
 */
function afficheMultiSelect(typeId, typeNom) {
	$.each($(".ms-container"), function () {
		if ($(this).attr("id")=="ms-types_utilisateurs_modifier_form_"+typeId) {
			$(this).show();
			$(this).find(".titre_mutli-select span").html(typeNom);
		} else {
			$(this).hide();
		}
	});
	$("#types_utilisateurs_modifier_titre").hide();
}