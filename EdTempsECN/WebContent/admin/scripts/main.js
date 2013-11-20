function confirmationSupprimerSalle() {
	return confirm("Etes-vous sur de vouloir supprimer la salle ?");
}

function validationModifierSalle() {
	return false;
}

function afficheCacheFormulaireModifierSalle() {
	if ($('#ajouter_salle_form').is(":visible")) {
		$('#ajouter_salle_form').slideUp(200);
	} else {
		$('#ajouter_salle_form').slideDown(200);
	}
}