package pkgServices.pkgNotification;

import pkgDomain.pkgEntities.clsReviewAssignment;

/**
 * Patrón Observer (GoF): observador que se entera cada vez que se asigna un
 * revisor a una pregunta. El controlador de revisiones (sujeto) no sabe cómo
 * se notifica (correo, mensaje en pantalla, auditoría...): solo avisa a sus
 * observadores.
 */
public interface IAssignmentObserver {

    void opOnReviewerAssigned(clsReviewAssignment prmAssignment);
}
