package pkgDomain.pkgLifecycle;

import pkgDomain.pkgEntities.clsQuestion;

/**
 * Patrón State (GoF): cada estado del ciclo de vida de una pregunta es una
 * clase que decide qué se puede hacer con la pregunta mientras está en él y
 * a qué estado pasa. La pregunta (contexto) solo delega en su estado actual,
 * así no hay condicionales del tipo "if (estado == ...)" repartidos por el
 * código.
 * <p>
 * Por defecto ninguna acción está permitida; cada estado concreto
 * sobrescribe solo las que acepta.
 */
public abstract class clsQuestionState {

    /** Código que se guarda en la base de datos (p. ej. "DRAFT"). */
    public abstract String opGetCode();

    /** Nombre que ve el usuario (p. ej. "Borrador"). */
    public abstract String opGetName();

    /** ¿Se puede modificar el contenido de la pregunta? */
    public boolean opIsEditable() {
        return false;
    }

    /** ¿El autor la puede enviar a revisión? */
    public boolean opCanSubmitForReview() {
        return false;
    }

    /** ¿El administrador le puede asignar revisores? */
    public boolean opCanAssignReviewers() {
        return false;
    }

    /** HU02: el autor envía la pregunta a revisión. */
    public void opSubmitForReview(clsQuestion prmQuestion) {
        throw new IllegalStateException("Una pregunta en \"" + opGetName() + "\" no se puede enviar a revisión.");
    }

    /** HU04: el administrador asigna revisores y empieza la revisión. */
    public void opStartReview(clsQuestion prmQuestion) {
        throw new IllegalStateException("A una pregunta en \"" + opGetName() + "\" no se le pueden asignar revisores.");
    }

    @Override
    public String toString() {
        return opGetName();
    }
}
