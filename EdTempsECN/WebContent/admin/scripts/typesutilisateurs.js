$(document).ready(function() {


});


/**
 * Confirmation de suppression
 */
function confirmationSupprimerType() {
	return confirm("Etes-vous sur de vouloir supprimer ce type d'utilisateur ?");
}


/**
 * Validation du formulaire d'ajout
 */
function validationAjouterType() {
	var valid = true;

	var jqNom = $("#ajouter_type_utilisateurs_nom");

	// Vérifie que le nom n'est pas déjà utilisé
	var nomDejaUtilise = false;
	$.each($(".liste_types_nom"), function() {
		if (jqNom.val()==$(this).html()) {
			nomDejaUtilise = true;
		}
	});
	
	// Champ nom
	if (jqNom.val()=="") {
		jqNom.css("box-shadow", "#FF0000 0 0 10px").css("border", "1px solid #FF0000");
		jqNom.attr("title", "Le nom est obligatoire");
		valid = false;
	} else if (!/^[a-z \u00C0-\u00FF0-9]+$/i.test(jqNom.val())) {
		jqNom.css("box-shadow", "#FF0000 0 0 10px").css("border", "1px solid #FF0000");
		jqNom.attr("title", "Le nom doit uniquement comporter des caracteres alphanumeriques");
		valid = false;
	} else if (nomDejaUtilise) {
		jqNom.css("box-shadow", "#FF0000 0 0 10px").css("border", "1px solid #FF0000");
		jqNom.attr("title", "Ce nom est deja utilise");
		valid = false;
	} else {
		jqNom.css("box-shadow", "#60C003 0 0 10px").css("border", "1px solid #60C003").attr("title", "");
	}

	return valid;
}
