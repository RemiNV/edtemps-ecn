$(document).ready(function() {
	$("#creneau_debut").mask("99:99");
	$("#creneau_fin").mask("99:99");
});

/**
 * Affiche la demande de confirmation de suppression
 */
function confirmationSupprimerCreneau() {
	return confirm("Etes-vous sur de vouloir supprimer le créneau ?");
}

/**
 * Validation du formulaire d'ajout
 */
function validationAjouterModifierCreneaux() {
	var valid = true;

	var jqLibelle = $("#creneau_libelle");
	var jqDebut = $("#creneau_debut");
	var jqFin = $("#creneau_fin");

	// Libellé
	if (jqLibelle.val()=="") {
		jqLibelle.css("box-shadow", "#FF0000 0 0 10px").css("border", "1px solid #FF0000");
		jqLibelle.attr("title", "Champ obligatoire");
		valid = false;
	} else if (!/^[a-z '\u00C0-\u00FF0-9]+$/i.test(jqLibelle.val())) {
		jqLibelle.css("box-shadow", "#FF0000 0 0 10px").css("border", "1px solid #FF0000");
		jqLibelle.attr("title", "Ne doit comporter que des caracteres alphanumériques");
		valid = false;
	} else {
		jqLibelle.css("box-shadow", "#60C003 0 0 10px").css("border", "1px solid #60C003").attr("title", "");
	}
	
	// Début
	var debutH = parseInt(jqDebut.val().substring(0, 2));
	var debutM = parseInt(jqDebut.val().substring(3, 5));
	if (jqDebut.val()=="") {
		jqDebut.css("box-shadow", "#FF0000 0 0 10px").css("border", "1px solid #FF0000");
		jqDebut.attr("title", "Champ obligatoire");
		valid = false;
	} else if (debutH>23 || debutM>59) {
		jqDebut.css("box-shadow", "#FF0000 0 0 10px").css("border", "1px solid #FF0000");
		jqDebut.attr("title", "L'horaire n'est pas correct");
		valid = false;
	} else {
		jqDebut.css("box-shadow", "#60C003 0 0 10px").css("border", "1px solid #60C003").attr("title", "");
	}
	
	// Fin
	var finH = parseInt(jqFin.val().substring(0, 2));
	var finM = parseInt(jqFin.val().substring(3, 5));
	if (jqFin.val()=="") {
		jqFin.css("box-shadow", "#FF0000 0 0 10px").css("border", "1px solid #FF0000");
		jqFin.attr("title", "Champ obligatoire");
		valid = false;
	} else if (finH>23 || finM>59) {
		jqFin.css("box-shadow", "#FF0000 0 0 10px").css("border", "1px solid #FF0000");
		jqFin.attr("title", "L'horaire n'est pas correct");
		valid = false;
	} else if (((60*debutH) + debutM) > ((60*finH) + finM)) {
		jqDebut.css("box-shadow", "#FF0000 0 0 10px").css("border", "1px solid #FF0000");
		jqDebut.attr("title", "Les deux horaires sont incohérents");
		jqFin.css("box-shadow", "#FF0000 0 0 10px").css("border", "1px solid #FF0000");
		jqFin.attr("title", "Les deux horaires sont incohérents");
		valid = false;
	} else {
		jqFin.css("box-shadow", "#60C003 0 0 10px").css("border", "1px solid #60C003").attr("title", "");
	}

	if (valid) {
		var today = new Date();
		$("#debutCreneau").val(new Date(today.getFullYear(), today.getMonth(), today.getDate(), debutH, debutM, 0).getTime()); 
		$("#finCreneau").val(new Date(today.getFullYear(), today.getMonth(), today.getDate(), finH, finM, 0).getTime()); 
	}
	
	return valid;
}