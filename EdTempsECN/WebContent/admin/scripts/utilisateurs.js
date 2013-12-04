$(document).ready(function() {
	
	// Filtre des prénoms
	$("#filtre_prenom").keyup(function() {
		filtrerText($(this), $('.data-collumn-prenom'));
	});
	
	// Filtre des Noms
	$("#filtre_nom").keyup(function() {
		filtrerText($(this), $('.data-collumn-nom'));
	});
	
	// Filtre des Mails
	$("#filtre_mail").keyup(function() {
		filtrerText($(this), $('.data-collumn-mail'));
	});
	
	// Filtre des Types d'utilisateur
	$("#filtre_type").keyup(function() {
		filtrerText($(this), $('.data-collumn-type'));
	});
	
	// Filtre des Statut d'utilisateur
	$("#filtre_statut").click(function() {
		filtrerCheckBox($(this), $('.data-collumn-statut'));
	});
	
});


/**
 * Filtrer le tableau des utilisateurs par un champ text
 * @param idFiltreText Le filtre qui a été activé
 * @param classTri Classe des contenus sur lesquels le filte doit être appliqué
 */
function filtrerText(idFiltreText, classTri) {
	
	// Récupère la valeur du filtre
    var filtreValeur = idFiltreText.val().toLowerCase();
    
    // Pour chaque ligne du tableau, affiche ou cache en fonction du résultat
    var couleurPrecedente = "#E3EDEF";
	classTri.each(function() {
        if (filtreValeur=="" || $(this).html().toLowerCase().indexOf(filtreValeur) >= 0) {
        	if (couleurPrecedente=="#E3EDEF") couleurPrecedente = "white";
        	else couleurPrecedente="#E3EDEF";
        	$(this).parent().css("background-color", couleurPrecedente).fadeIn(200);
        } else {
            $(this).parent().fadeOut(200);
        }
    });
    
}


/**
 * Filtrer le tableau des utilisateurs par un champ checkbox
 * @param idFiltreCheck Le filtre qui a été activé
 * @param classTri Classe des contenus sur lesquels le filte doit être appliqué
 */
function filtrerCheckBox(idFiltreCheck, classTri) {
	
	// Récupère la valeur du filtre
    var filtreValeur = idFiltreCheck.is(":checked") ? "desactiver" : "activer";

    // Pour chaque ligne du tableau, affiche ou cache en fonction du résultat
    var couleurPrecedente = "#E3EDEF";
	classTri.each(function() {
        if (filtreValeur=="" || $(this).html().indexOf(filtreValeur) >= 0) {
        	if (couleurPrecedente=="#E3EDEF") couleurPrecedente = "white";
        	else couleurPrecedente="#E3EDEF";
        	$(this).parent().css("background-color", couleurPrecedente).fadeIn(200);
        } else {
            $(this).parent().fadeOut(200);
        }
    });
    
}