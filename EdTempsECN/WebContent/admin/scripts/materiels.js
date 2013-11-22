/*
 * Affiche la demande de confirmation de suppression de matériel
 */
function confirmationSupprimerMateriel() {
	return confirm("Etes-vous sur de vouloir supprimer le materiel ?");
}

/*
 * Validation du formulaire d'ajout d'un matériel
 */
function validationAjouterMateriel() {
	var valid = true;

	var jqNom = $("#ajouter_materiel_nom");

	// Vérifie que le nom n'est pas déjà utilisé
	var nomDejaUtilise = false;
	$.each($(".liste_materiel_nom"), function() {
		if (jqNom.val()==$(this).html()) {
			nomDejaUtilise = true;
		}
	});
	
	// Champ nom
	if (jqNom.val()=="") {
		jqNom.css("box-shadow", "#FF0000 0 0 10px").css("border", "1px solid #FF0000");
		jqNom.attr("title", "Le nom du materiel est obligatoire");
		valid = false;
	} else if (!/^[a-z \u00C0-\u00FF0-9]+$/i.test(jqNom.val())) {
		jqNom.css("box-shadow", "#FF0000 0 0 10px").css("border", "1px solid #FF0000");
		jqNom.attr("title", "Le nom du materiel doit uniquement comporter des caracteres alphanumeriques");
		valid = false;
	} else if (nomDejaUtilise) {
		jqNom.css("box-shadow", "#FF0000 0 0 10px").css("border", "1px solid #FF0000");
		jqNom.attr("title", "Ce nom de materiel est deja utilise");
		valid = false;
	} else {
		jqNom.css("box-shadow", "#60C003 0 0 10px").css("border", "1px solid #60C003").attr("title", "");
	}

	return valid;
}


/*
 * Validation du formulaire de modification d'un materiel
 */
function validationModifierMateriel() {
	var valid = true;
	
	var jqNom = $("#modifier_materiel_nom");

	// Vérifie que le nom n'est pas déjà utilisé
	var nomDejaUtilise = false;
	$.each($(".liste_materiel_nom"), function() {
		if (jqNom.val()==$(this).html()) {
			nomDejaUtilise = true;
		}
	});
	
	// Champ nom
	if (jqNom.val()=="") {
		jqNom.css("box-shadow", "#FF0000 0 0 10px").css("border", "1px solid #FF0000");
		jqNom.attr("title", "Le nom du materiel est obligatoire");
		valid = false;
	} else if (!/^[a-z \u00C0-\u00FF0-9]+$/i.test(jqNom.val())) {
		jqNom.css("box-shadow", "#FF0000 0 0 10px").css("border", "1px solid #FF0000");
		jqNom.attr("title", "Le nom du materiel doit uniquement comporter des caracteres alphanumeriques");
		valid = false;
	} else if (nomDejaUtilise) {
		jqNom.css("box-shadow", "#FF0000 0 0 10px").css("border", "1px solid #FF0000");
		jqNom.attr("title", "Ce nom de materiel est deja utilise");
		valid = false;
	} else {
		jqNom.css("box-shadow", "#60C003 0 0 10px").css("border", "1px solid #60C003").attr("title", "");
	}

	return valid;
}

/*
 * Affiche/Cache le formulaire d'ajout de matériel
 */
function afficheCacheFormulaireAjouterMateriel() {
	if ($('#ajouter_materiel_form').is(":visible")) {
		$('#ajouter_materiel_form').slideUp(200);
		$("#ajouter_materiel_form").css("box-shadow", "white 0 0 10px").css("border", "1px solid #A9A9A9").attr("title", "");
	} else {
		$('#ajouter_materiel_form').slideDown(200);
	}
}