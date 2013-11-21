$(document).ready(function() {
	// Masques sur les champs du formulaire d'ajout d'une salle
	$("#ajouter_salle_capacite").mask("9?999");
	$("#ajouter_salle_numero").mask("9?999");
	$("#ajouter_salle_niveau").mask("9?999");
	$("#ajouter_salle_batiment").mask("a?aaa");
	$(".ajouter_salle_quantite_materiel input").mask("?9999", { placeholder: "" });
});

/*
 * Affiche la boite de confirmation de confirmation de salles
 */
function confirmationSupprimerSalle() {
	return confirm("Etes-vous sur de vouloir supprimer la salle ?");
}

/*
 * Validation du formulaire d'ajout d'une salle
 */
function validationModifierSalle() {
	var valid = true;
	
	// Champ nom
	var jqNom = $("#ajouter_salle_nom");
	if (jqNom.val()=="" || !/^[a-z \u00C0-\u00FF0-9]+$/i.test(jqNom.val())) {
		jqNom.css("box-shadow", "#FF0000 0 0 10px").css("border", "1px solid #FF0000");
		jqNom.attr("title", "Le nom du groupe doit etre specifie et doit uniquement comporter des caracteres alphanumeriques");
		valid = false;
	} else {
		jqNom.css("box-shadow", "#60C003 0 0 10px").css("border", "1px solid #60C003").attr("title", "");
	}
	
	// Champ bâtiment
	var jqBatiment = $("#ajouter_salle_batiment");
	if (jqBatiment.val()!="" && !/^[a-z]+$/i.test(jqBatiment.val())) {
		jqBatiment.css("box-shadow", "#FF0000 0 0 10px").css("border", "1px solid #FF0000");
		jqBatiment.attr("title", "Le batiment doit etre un caractere alphanumerique");
		valid = false;
	} else if (jqBatiment.val()!="") {
		jqBatiment.css("box-shadow", "#60C003 0 0 10px").css("border", "1px solid #60C003").attr("title", "");
	}
	
	// Champ niveau
	var jqNiveau = $("#ajouter_salle_niveau");
	if (jqNiveau.val()!="" && !/^[0-9]+$/i.test(jqNiveau.val())) {
		jqNiveau.css("box-shadow", "#FF0000 0 0 10px").css("border", "1px solid #FF0000");
		jqNiveau.attr("title", "Le niveau doit etre un nombre");
		valid = false;
	} else if (jqNiveau.val()!="") {
		jqNiveau.css("box-shadow", "#60C003 0 0 10px").css("border", "1px solid #60C003").attr("title", "");
	}
	
	// Champ numéro
	var jqNumero = $("#ajouter_salle_numero");
	if (jqNumero.val()!="" && !/^[0-9]+$/i.test(jqNumero.val())) {
		jqNumero.css("box-shadow", "#FF0000 0 0 10px").css("border", "1px solid #FF0000");
		jqNumero.attr("title", "Le numero doit etre un nombre");
		valid = false;
	} else if (jqNumero.val()!="") {
		jqNumero.css("box-shadow", "#60C003 0 0 10px").css("border", "1px solid #60C003").attr("title", "");
	}
	
	// Champ capacité
	var jqCapacite = $("#ajouter_salle_capacite");
	if (jqCapacite.val()!="" && !/^[0-9]+$/i.test(jqCapacite.val())) {
		jqCapacite.css("box-shadow", "#FF0000 0 0 10px").css("border", "1px solid #FF0000");
		jqCapacite.attr("title", "La capacite doit etre un nombre");
		valid = false;
	} else if (jqCapacite.val()!="") {
		jqCapacite.css("box-shadow", "#60C003 0 0 10px").css("border", "1px solid #60C003").attr("title", "");
	}

	return valid;
}

/*
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

/*
 * Fabrique et affiche le nom de la salle à partir des champs batiment, niveau et numéro
 */
function afficheNomSalle() {
	if ($("#ajouter_salle_nom").attr("disabled")) {
		$("#ajouter_salle_nom").val($("#ajouter_salle_batiment").val()+$("#ajouter_salle_niveau").val()+$("#ajouter_salle_numero").val());
	}
}

/*
 * Dévérouille le champ nom pour le modifier manuellement
 */
function activeChampNom() {
	$("#ajouter_salle_nom").removeAttr("disabled");
}