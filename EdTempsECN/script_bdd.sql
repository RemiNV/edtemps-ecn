CREATE SEQUENCE edt.salle_salle_id_seq;

CREATE TABLE edt.Salle (
                salle_id INTEGER NOT NULL DEFAULT nextval('edt.salle_salle_id_seq'),
                salle_batiment VARCHAR,
                salle_niveau INTEGER,
                salle_nom VARCHAR,
                salle_numero INTEGER,
                salle_capacite INTEGER,
                CONSTRAINT salle_id PRIMARY KEY (salle_id)
);


ALTER SEQUENCE edt.salle_salle_id_seq OWNER TO "edtemps-ecn";

CREATE SEQUENCE edt.materiel_materiel_id_seq;

CREATE TABLE edt.Materiel (
                materiel_id INTEGER NOT NULL DEFAULT nextval('edt.materiel_materiel_id_seq'),
                materiel_nom VARCHAR,
                CONSTRAINT materiel_id PRIMARY KEY (materiel_id)
);


ALTER SEQUENCE edt.materiel_materiel_id_seq OWNER TO "edtemps-ecn";

CREATE TABLE edt.ContientMateriel (
                salle_id INTEGER NOT NULL,
                materiel_id INTEGER NOT NULL,
                contientMateriel_quantite INTEGER,
                CONSTRAINT salle_id_materiel_id PRIMARY KEY (salle_id, materiel_id)
);


CREATE SEQUENCE edt.typecalendrier_typecal_id_seq;

CREATE TABLE edt.TypeCalendrier (
                typeCal_id INTEGER NOT NULL DEFAULT nextval('edt.typecalendrier_typecal_id_seq'),
                typeCal_libelle VARCHAR UNIQUE,
                CONSTRAINT typecal_id PRIMARY KEY (typeCal_id)
);


ALTER SEQUENCE edt.typecalendrier_typecal_id_seq OWNER TO "edtemps-ecn";

CREATE SEQUENCE edt.groupeparticipant_groupeparticipant_id_seq;

CREATE TABLE edt.GroupeParticipant (
                groupeParticipant_id INTEGER NOT NULL DEFAULT nextval('edt.groupeparticipant_groupeparticipant_id_seq'),
                groupeParticipant_nom VARCHAR,
                groupeParticipant_rattachementAutorise BOOLEAN NOT NULL,
                groupeParticipant_id_parent INTEGER,
				groupeParticipant_estCours BOOLEAN,
				groupeParticipant_estCalendrierUnique BOOLEAN NOT NULL,
                CONSTRAINT groupeparticipant_id PRIMARY KEY (groupeParticipant_id)
);


ALTER SEQUENCE edt.groupeparticipant_groupeparticipant_id_seq OWNER TO "edtemps-ecn";

CREATE SEQUENCE edt.evenement_eve_id_seq;

CREATE TABLE edt.Evenement (
                eve_id INTEGER NOT NULL DEFAULT nextval('edt.evenement_eve_id_seq'),
                eve_nom VARCHAR,
                eve_dateDebut TIMESTAMP,
                eve_dateFin TIMESTAMP,
                CONSTRAINT eve_id PRIMARY KEY (eve_id)
);


ALTER SEQUENCE edt.evenement_eve_id_seq OWNER TO "edtemps-ecn";

CREATE TABLE edt.ALieuenSalle (
                eve_id INTEGER NOT NULL,
                salle_id INTEGER NOT NULL,
                CONSTRAINT eve_id_salle_id PRIMARY KEY (eve_id, salle_id)
);


CREATE TABLE edt.NecessiteMateriel (
                eve_id INTEGER NOT NULL,
                materiel_id INTEGER NOT NULL,
				necessitemateriel_quantite INTEGER NOT NULL,
                CONSTRAINT eve_id_materiel_id PRIMARY KEY (eve_id, materiel_id)
);


CREATE SEQUENCE edt.matiere_matiere_id_seq;

CREATE TABLE edt.Matiere (
                matiere_id INTEGER NOT NULL DEFAULT nextval('edt.matiere_matiere_id_seq'),
                matiere_nom VARCHAR UNIQUE,
                CONSTRAINT matiere_id PRIMARY KEY (matiere_id)
);


ALTER SEQUENCE edt.matiere_matiere_id_seq OWNER TO "edtemps-ecn";

CREATE SEQUENCE edt.calendrier_cal_id_seq;

CREATE TABLE edt.Calendrier (
                cal_id INTEGER NOT NULL DEFAULT nextval('edt.calendrier_cal_id_seq'),
                matiere_id INTEGER,
                cal_nom VARCHAR UNIQUE,
                typeCal_id INTEGER,
                CONSTRAINT cal_id PRIMARY KEY (cal_id)
);


ALTER SEQUENCE edt.calendrier_cal_id_seq OWNER TO "edtemps-ecn";

CREATE TABLE edt.CalendrierAppartientGroupe (
                groupeParticipant_id INTEGER NOT NULL,
                cal_id INTEGER NOT NULL,
                CONSTRAINT groupeparticipant_id_cal_id PRIMARY KEY (groupeParticipant_id, cal_id)
);


CREATE TABLE edt.EvenementAppartient (
                eve_id INTEGER NOT NULL,
                cal_id INTEGER NOT NULL,
                CONSTRAINT eve_id_cal_id PRIMARY KEY (eve_id, cal_id)
);


CREATE SEQUENCE edt.utilisateur_utilisateur_id_seq;

CREATE TABLE edt.Utilisateur (
                utilisateur_id INTEGER NOT NULL DEFAULT nextval('edt.utilisateur_utilisateur_id_seq'),
                utilisateur_url_ical VARCHAR,
                utilisateur_id_ldap INTEGER UNIQUE,
                utilisateur_token VARCHAR UNIQUE,
                utilisateur_token_expire TIMESTAMP,
				utilisateur_nom TEXT NOT NULL,
				utilisateur_prenom TEXT NOT NULL,
				utilisateur_email TEXT,
                CONSTRAINT utilisateur_id PRIMARY KEY (utilisateur_id)
);


ALTER SEQUENCE edt.utilisateur_utilisateur_id_seq OWNER TO "edtemps-ecn";

CREATE TABLE edt.ProprietaireGroupeParticipant (
                utilisateur_id INTEGER NOT NULL,
                groupeParticipant_id INTEGER NOT NULL,
                CONSTRAINT proprietaire_utilisateur_id_groupeparticipant_id PRIMARY KEY (utilisateur_id, groupeParticipant_id)
);


CREATE TABLE edt.AbonneGroupeParticipant (
                utilisateur_id INTEGER NOT NULL,
                groupeParticipant_id INTEGER NOT NULL,
                abonnementGroupeParticipant_obligatoire BOOLEAN,
                CONSTRAINT abonnement_utilisateur_id_groupeparticipant_id PRIMARY KEY (utilisateur_id, groupeParticipant_id)
);


CREATE TABLE edt.ResponsableEvenement (
                utilisateur_id INTEGER NOT NULL,
                eve_id INTEGER NOT NULL,
                CONSTRAINT responsable_utilisateur_id_eve_id PRIMARY KEY (utilisateur_id, eve_id)
);


CREATE TABLE edt.IntervenantEvenement (
                utilisateur_id INTEGER NOT NULL,
                eve_id INTEGER NOT NULL,
                CONSTRAINT intervenant_utilisateur_id_eve_id PRIMARY KEY (utilisateur_id, eve_id)
);


CREATE TABLE edt.ProprietaireCalendrier (
                utilisateur_id INTEGER NOT NULL,
                cal_id INTEGER NOT NULL,
                CONSTRAINT utilisateur_id_cal_id PRIMARY KEY (utilisateur_id, cal_id)
);


CREATE TABLE edt.ProprietaireMatiere (
                utilisateur_id INTEGER NOT NULL,
                matiere_id INTEGER NOT NULL,
                CONSTRAINT matiere_id_utilisateur_id PRIMARY KEY (utilisateur_id, matiere_id)
);


CREATE SEQUENCE edt.typeutilisateur_type_id_seq;

CREATE TABLE edt.TypeUtilisateur (
                type_id INTEGER NOT NULL DEFAULT nextval('edt.typeutilisateur_type_id_seq'),
                type_libelle VARCHAR,
                CONSTRAINT type_id PRIMARY KEY (type_id)
);


ALTER SEQUENCE edt.typeutilisateur_type_id_seq OWNER TO "edtemps-ecn";

CREATE TABLE edt.EstDeType (
                type_id INTEGER NOT NULL,
                utilisateur_id INTEGER NOT NULL,
                CONSTRAINT type_id_utilisateur_id PRIMARY KEY (type_id, utilisateur_id)
);


CREATE SEQUENCE edt.droits_droits_id_seq;

CREATE TABLE edt.Droits (
                droits_id INTEGER NOT NULL DEFAULT nextval('edt.droits_droits_id_seq'),
                droits_libelle VARCHAR,
                CONSTRAINT droits_id PRIMARY KEY (droits_id)
);


ALTER SEQUENCE edt.droits_droits_id_seq OWNER TO "edtemps-ecn";

CREATE TABLE edt.ALeDroitDe (
                type_id INTEGER NOT NULL,
                droits_id INTEGER NOT NULL,
                CONSTRAINT type_id_droits_id PRIMARY KEY (type_id, droits_id)
);


ALTER TABLE edt.ALieuenSalle ADD CONSTRAINT salle_alieuensalle_fk
FOREIGN KEY (salle_id)
REFERENCES edt.Salle (salle_id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE edt.ContientMateriel ADD CONSTRAINT salle_contientmateriel_fk
FOREIGN KEY (salle_id)
REFERENCES edt.Salle (salle_id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE edt.NecessiteMateriel ADD CONSTRAINT materiel_necessitemateriel_fk
FOREIGN KEY (materiel_id)
REFERENCES edt.Materiel (materiel_id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE edt.ContientMateriel ADD CONSTRAINT materiel_contientmateriel_fk
FOREIGN KEY (materiel_id)
REFERENCES edt.Materiel (materiel_id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE edt.Calendrier ADD CONSTRAINT typecalendrier_calendrier_fk
FOREIGN KEY (typeCal_id)
REFERENCES edt.TypeCalendrier (typeCal_id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE edt.AbonneGroupeParticipant ADD CONSTRAINT groupeparticipant_abonnegroupeparticipant_fk
FOREIGN KEY (groupeParticipant_id)
REFERENCES edt.GroupeParticipant (groupeParticipant_id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE edt.ProprietaireGroupeParticipant ADD CONSTRAINT groupeparticipant_proprietairegroupeparticipant_fk
FOREIGN KEY (groupeParticipant_id)
REFERENCES edt.GroupeParticipant (groupeParticipant_id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE edt.CalendrierAppartientGroupe ADD CONSTRAINT groupeparticipant_calendrierappartiengroupe_fk
FOREIGN KEY (groupeParticipant_id)
REFERENCES edt.GroupeParticipant (groupeParticipant_id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE edt.GroupeParticipant ADD CONSTRAINT groupeparticipant_groupeparticipant_fk
FOREIGN KEY (groupeParticipant_id_parent)
REFERENCES edt.GroupeParticipant (groupeParticipant_id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE edt.IntervenantEvenement ADD CONSTRAINT evenement_intervenantevenement_fk
FOREIGN KEY (eve_id)
REFERENCES edt.Evenement (eve_id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE edt.ResponsableEvenement ADD CONSTRAINT evenement_responsableevenement_fk
FOREIGN KEY (eve_id)
REFERENCES edt.Evenement (eve_id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE edt.EvenementAppartient ADD CONSTRAINT evenement_evenementappartient_fk
FOREIGN KEY (eve_id)
REFERENCES edt.Evenement (eve_id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE edt.NecessiteMateriel ADD CONSTRAINT evenement_necessitemateriel_fk
FOREIGN KEY (eve_id)
REFERENCES edt.Evenement (eve_id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE edt.ALieuenSalle ADD CONSTRAINT evenement_alieuensalle_fk
FOREIGN KEY (eve_id)
REFERENCES edt.Evenement (eve_id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE edt.ProprietaireMatiere ADD CONSTRAINT matiere_estproprietaire_fk
FOREIGN KEY (matiere_id)
REFERENCES edt.Matiere (matiere_id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE edt.Calendrier ADD CONSTRAINT matiere_calendrier_fk
FOREIGN KEY (matiere_id)
REFERENCES edt.Matiere (matiere_id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE edt.ProprietaireCalendrier ADD CONSTRAINT calendrier_proprietairecalendrier_fk
FOREIGN KEY (cal_id)
REFERENCES edt.Calendrier (cal_id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE edt.EvenementAppartient ADD CONSTRAINT calendrier_evenementappartient_fk
FOREIGN KEY (cal_id)
REFERENCES edt.Calendrier (cal_id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE edt.CalendrierAppartientGroupe ADD CONSTRAINT calendrier_calendrierappartiengroupe_fk
FOREIGN KEY (cal_id)
REFERENCES edt.Calendrier (cal_id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE edt.EstDeType ADD CONSTRAINT utilisateur_estdetype_fk
FOREIGN KEY (utilisateur_id)
REFERENCES edt.Utilisateur (utilisateur_id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE edt.ProprietaireMatiere ADD CONSTRAINT utilisateur_estproprietaire_fk
FOREIGN KEY (utilisateur_id)
REFERENCES edt.Utilisateur (utilisateur_id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE edt.ProprietaireCalendrier ADD CONSTRAINT utilisateur_proprietairecalendrier_fk
FOREIGN KEY (utilisateur_id)
REFERENCES edt.Utilisateur (utilisateur_id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE edt.IntervenantEvenement ADD CONSTRAINT utilisateur_intervenantevenement_fk
FOREIGN KEY (utilisateur_id)
REFERENCES edt.Utilisateur (utilisateur_id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE edt.ResponsableEvenement ADD CONSTRAINT utilisateur_responsableevenement_fk
FOREIGN KEY (utilisateur_id)
REFERENCES edt.Utilisateur (utilisateur_id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE edt.AbonneGroupeParticipant ADD CONSTRAINT utilisateur_abonnegroupeparticipant_fk
FOREIGN KEY (utilisateur_id)
REFERENCES edt.Utilisateur (utilisateur_id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE edt.ProprietaireGroupeParticipant ADD CONSTRAINT utilisateur_proprietairegroupeparticipant_fk
FOREIGN KEY (utilisateur_id)
REFERENCES edt.Utilisateur (utilisateur_id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE edt.ALeDroitDe ADD CONSTRAINT typeutilisateur_aledroitde_fk
FOREIGN KEY (type_id)
REFERENCES edt.TypeUtilisateur (type_id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE edt.EstDeType ADD CONSTRAINT typeutilisateur_estdetype_fk
FOREIGN KEY (type_id)
REFERENCES edt.TypeUtilisateur (type_id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE edt.ALeDroitDe ADD CONSTRAINT droits_aledroitde_fk
FOREIGN KEY (droits_id)
REFERENCES edt.Droits (droits_id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE edt.salle OWNER TO "edtemps-ecn";
ALTER TABLE edt.materiel OWNER TO "edtemps-ecn";
ALTER TABLE edt.contientmateriel OWNER TO "edtemps-ecn";
ALTER TABLE edt.typecalendrier OWNER TO "edtemps-ecn";
ALTER TABLE edt.groupeparticipant OWNER TO "edtemps-ecn";
ALTER TABLE edt.evenement OWNER TO "edtemps-ecn";
ALTER TABLE edt.aLieuensalle OWNER TO "edtemps-ecn";
ALTER TABLE edt.necessitemateriel OWNER TO "edtemps-ecn";
ALTER TABLE edt.matiere OWNER TO "edtemps-ecn";
ALTER TABLE edt.calendrier OWNER TO "edtemps-ecn";
ALTER TABLE edt.calendrierappartientgroupe OWNER TO "edtemps-ecn";
ALTER TABLE edt.evenementappartient OWNER TO "edtemps-ecn";
ALTER TABLE edt.utilisateur OWNER TO "edtemps-ecn";
ALTER TABLE edt.proprietairegroupeparticipant OWNER TO "edtemps-ecn";
ALTER TABLE edt.abonnegroupeparticipant OWNER TO "edtemps-ecn";
ALTER TABLE edt.responsableevenement OWNER TO "edtemps-ecn";
ALTER TABLE edt.intervenantevenement OWNER TO "edtemps-ecn";
ALTER TABLE edt.proprietairecalendrier OWNER TO "edtemps-ecn";
ALTER TABLE edt.proprietairematiere OWNER TO "edtemps-ecn";
ALTER TABLE edt.typeutilisateur OWNER TO "edtemps-ecn";
ALTER TABLE edt.estdetype OWNER TO "edtemps-ecn";
ALTER TABLE edt.droits OWNER TO "edtemps-ecn";
ALTER TABLE edt.aledroitde OWNER TO "edtemps-ecn";
