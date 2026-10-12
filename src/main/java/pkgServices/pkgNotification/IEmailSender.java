package pkgServices.pkgNotification;

/**
 * Envío de correos. Separarlo permite probar el notificador sin un servidor
 * de correo real.
 */
public interface IEmailSender {

    /** @return true si el correo se entregó al servidor. */
    boolean opSend(String prmTo, String prmSubject, String prmBody);
}
