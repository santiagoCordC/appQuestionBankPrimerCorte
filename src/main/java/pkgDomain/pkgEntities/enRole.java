package pkgDomain.pkgEntities;

/**
 * Roles que intervienen en el primer corte.
 */
public enum enRole {
    ADMIN("Administrador"),
    AUTHOR("Autor de preguntas"),
    REVIEWER("Revisor");

    private final String attLabel;

    enRole(String prmLabel) {
        attLabel = prmLabel;
    }

    public String opGetLabel() {
        return attLabel;
    }

    @Override
    public String toString() {
        return attLabel;
    }
}
