$(document).ready(function() {
	// Masques sur les champs du formulaire d'ajout d'une salle
	$("#ajouter_salle_capacite").mask("9?999");
	$("#ajouter_salle_numero").mask("9?999");
	$("#ajouter_salle_niveau").mask("9?999");
	$("#ajouter_salle_batiment").mask("a?aaa");
	$(".ajouter_salle_quantite_materiel input").mask("?9999", { placeholder: "" });

	// Masques sur les champs du formulaire de modification d'une salle
	$("#modifier_salle_capacite").mask("9?999");
	$("#modifier_salle_numero").mask("9?999");
	$("#modifier_salle_niveau").mask("9?999");
	$("#modifier_salle_batiment").mask("a?aaa");
	$(".salle_quantite_materiel input").mask("?9999", { placeholder: "" });
});

/**
 * Affiche la demande de confirmation de suppression de salles
 */
function confirmationSupprimerSalle() {
	return confirm("Etes-vous sur de vouloir supprimer la salle ?");
}

/**
 * Validation du formulaire d'ajout d'une salle
 */
function validationAjouterSalle() {
	var valid = true;
	
	$("#ajouter_salle_form_chargement").show();
	$("#ajouter_salle_form_ajouter").attr("disabled", "disabled");
	$("#ajouter_salle_form_annuler").attr("disabled", "disabled");
	$("#ajouter_salle_nom").removeAttr("disabled");

	var jqNom = $("#ajouter_salle_nom");

	// Vérifie que le nom n'est pas déjà utilisé
	var nomDejaUtilise = false;
	$.each($(".liste_salles_nom"), function() {
		if (jqNom.val()==$(this).html()) {
			nomDejaUtilise = true;
		}
	});
	
	// Champ nom
	if (jqNom.val()=="") {
		jqNom.css("box-shadow", "#FF0000 0 0 10px").css("border", "1px solid #FF0000");
		jqNom.attr("title", "Le nom de la salle est obligatoire");
		valid = false;
	} else if (!/^[a-z \u00C0-\u00FF0-9]+$/i.test(jqNom.val())) {
		jqNom.css("box-shadow", "#FF0000 0 0 10px").css("border", "1px solid #FF0000");
		jqNom.attr("title", "Le nom de la salle doit uniquement comporter des caracteres alphanumeriques");
		valid = false;
	} else if (nomDejaUtilise) {
		jqNom.css("box-shadow", "#FF0000 0 0 10px").css("border", "1px solid #FF0000");
		jqNom.attr("title", "Ce nom de salle est deja utilise");
		valid = false;
	} else {
		jqNom.css("box-shadow", "#60C003 0 0 10px").css("border", "1px solid #60C003").attr("title", "");
	}
	
	// Si le formulaire n'est pas valide, la main est redonnée à l'utilisateur
	if (!valid) {
		$("#ajouter_salle_form_chargement").hide();
		$("#ajouter_salle_form_ajouter").removeAttr("disabled");
		$("#ajouter_salle_form_annuler").removeAttr("disabled");
		$("#ajouter_salle_nom").attr("disabled", "disabled");
	}

	return valid;
}


/**
 * Validation du formulaire de modification d'une salle
 */
function validationModifierSalle() {
	var valid = true;
	
	var jqNom = $("#modifier_salle_nom");

	// Vérifie que le nom n'est pas déjà utilisé
	var nomDejaUtilise = false;
	$.each($(".liste_salles_nom"), function() {
		if (jqNom.val()==$(this).html()) {
			nomDejaUtilise = true;
		}
	});
	
	// Champ nom
	if (jqNom.val()=="") {
		jqNom.css("box-shadow", "#FF0000 0 0 10px").css("border", "1px solid #FF0000");
		jqNom.attr("title", "Le nom de la salle est obligatoire");
		valid = false;
	} else if (!/^[a-z \u00C0-\u00FF0-9]+$/i.test(jqNom.val())) {
		jqNom.css("box-shadow", "#FF0000 0 0 10px").css("border", "1px solid #FF0000");
		jqNom.attr("title", "Le nom de la salle doit uniquement comporter des caracteres alphanumeriques");
		valid = false;
	} else if (nomDejaUtilise) {
		jqNom.css("box-shadow", "#FF0000 0 0 10px").css("border", "1px solid #FF0000");
		jqNom.attr("title", "Ce nom de salle est deja utilise");
		valid = false;
	} else {
		jqNom.css("box-shadow", "#60C003 0 0 10px").css("border", "1px solid #60C003").attr("title", "");
	}

	return valid;
}

/**
 * Affiche/Cache le formulaire d'ajout de salle
 */
function afficheCacheFormulaireAjouterSalle() {
	if ($('#ajouter_salle_form').is(":visible")) {
		$('#ajouter_salle_form').slideUp(200);
		// Efface les bordures rouges et vertes et enlève les infobulles
		$("#ajouter_salle_nom").css("box-shadow", "white 0 0 10px").css("border", "1px solid #A9A9A9").attr("title", "");
		$("#ajouter_salle_batiment").css("box-shadow", "white 0 0 10px").css("border", "1px solid #A9A9A9").attr("title", "");
		$("#ajouter_salle_niveau").css("box-shadow", "white 0 0 10px").css("border", "1px solid #A9A9A9").attr("title", "");
		$("#ajouter_salle_numero").css("box-shadow", "white 0 0 10px").css("border", "1px solid #A9A9A9").attr("title", "");
		$("#ajouter_salle_capacite").css("box-shadow", "white 0 0 10px").css("border", "1px solid #A9A9A9").attr("title", "");
	} else {
		$('#ajouter_salle_form').slideDown(200);
	}
}

/**
 * Fabrique et affiche le nom de la salle à partir des champs batiment, niveau et numéro
 */
function afficheNomSalle() {
	if ($("#ajouter_salle_nom").attr("disabled")) {
		$("#ajouter_salle_nom").val($("#ajouter_salle_batiment").val()+$("#ajouter_salle_niveau").val()+$("#ajouter_salle_numero").val());
	}
}

/**
 * Dévérouille le champ nom pour le modifier manuellement
 */
function activeChampNom() {
	$("#ajouter_salle_nom").removeAttr("disabled");
}