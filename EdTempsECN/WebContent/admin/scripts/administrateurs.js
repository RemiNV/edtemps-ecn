/*
 * Affiche la demande de confirmation de suppression
 */
function confirmationSupprimerAdministrateur() {
	return confirm("Etes-vous sur de vouloir supprimer cet administrateur ?");
}

/*
 * Validation du formulaire d'ajout
 */
function validationAjouterAdministrateur() {
	var valid = true;

	var jqLogin = $("#ajouter_administrateur_login");
	var jqPassword1 = $("#ajouter_administrateur_password");
	var jqPassword2 = $("#ajouter_administrateur_password_again");

	// Vérifie que le nom n'est pas déjà utilisé
	var nomDejaUtilise = false;
	$.each($(".liste_administrateur_nom"), function() {
		if (jqLogin.val()==$(this).html()) {
			nomDejaUtilise = true;
		}
	});

	// Champ identifiant
	if (jqLogin.val()=="") {
		jqLogin.css("box-shadow", "#FF0000 0 0 10px").css("border", "1px solid #FF0000");
		jqLogin.attr("title", "L'identifiant est obligatoire");
		valid = false;
	} else if (!/^[a-z \u00C0-\u00FF0-9]+$/i.test(jqLogin.val())) {
		jqLogin.css("box-shadow", "#FF0000 0 0 10px").css("border", "1px solid #FF0000");
		jqLogin.attr("title", "L'identifiant ne doit comporter que des caracteres alphanumeriques");
		valid = false;
	} else if (nomDejaUtilise) {
		jqLogin.css("box-shadow", "#FF0000 0 0 10px").css("border", "1px solid #FF0000");
		jqLogin.attr("title", "Cet identifiant est deja utilise");
		valid = false;
	} else {
		jqLogin.css("box-shadow", "#60C003 0 0 10px").css("border", "1px solid #60C003").attr("title", "");
	}

	// Champ password 1
	var passwordValid = false;
	if (jqPassword1.val()=="") {
		jqPassword1.css("box-shadow", "#FF0000 0 0 10px").css("border", "1px solid #FF0000");
		jqPassword1.attr("title", "Le mot de passe est obligatoire");
		valid = false;
	} else if (!/^[a-z \u00C0-\u00FF0-9]+$/i.test(jqLogin.val())) {
		jqPassword1.css("box-shadow", "#FF0000 0 0 10px").css("border", "1px solid #FF0000");
		jqPassword1.attr("title", "Le mot de passe ne doit comporter que des caracteres alphanumeriques");
		valid = false;
	} else {
		jqPassword1.css("box-shadow", "#60C003 0 0 10px").css("border", "1px solid #60C003").attr("title", "");
		passwordValid = true;
	}

	// Champ password 2
	if (passwordValid && jqPassword2.val()!=jqPassword1.val()) {
		jqPassword2.css("box-shadow", "#FF0000 0 0 10px").css("border", "1px solid #FF0000");
		jqPassword2.attr("title", "Le mot de passe n'est pas identique");
		valid = false;
	} else if (passwordValid) {
		jqPassword2.css("box-shadow", "#60C003 0 0 10px").css("border", "1px solid #60C003").attr("title", "");
	}

	return valid;
}