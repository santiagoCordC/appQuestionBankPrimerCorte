package pkgServices.pkgNotification;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import pkgServices.clsConfig;

/**
 * Envía correos por SMTP con SSL (puerto 465, p. ej. Gmail con una
 * "contraseña de aplicación") usando solo el JDK, sin librerías externas.
 * <p>
 * Si en app.properties no se configura {@code correo.smtp.host}, el correo
 * no se envía por red: se guarda en data/correos_enviados.txt (bandeja de
 * salida local) para poder demostrar la notificación sin internet.
 */
public class clsSmtpEmailSender implements IEmailSender {

    public static final String OUTBOX_FILE = "data" + File.separator + "correos_enviados.txt";

    private final String attHost;
    private final int attPort;
    private final String attUser;
    private final String attPassword;
    private final String attFrom;

    public clsSmtpEmailSender(clsConfig prmConfig) {
        attHost = prmConfig.opGet("correo.smtp.host", "");
        attPort = prmConfig.opInt("correo.smtp.puerto", 465);
        attUser = prmConfig.opGet("correo.usuario", "");
        attPassword = prmConfig.opGet("correo.clave", "");
        attFrom = prmConfig.opGet("correo.remitente", attUser);
    }

    @Override
    public boolean opSend(String prmTo, String prmSubject, String prmBody) {
        if (attHost.isEmpty()) {
            return opSaveToOutbox(prmTo, prmSubject, prmBody, "SMTP no configurado: guardado en la bandeja de salida");
        }
        try {
            opSendSmtp(prmTo, prmSubject, prmBody);
            opSaveToOutbox(prmTo, prmSubject, prmBody, "Enviado por " + attHost);
            return true;
        } catch (IOException e) {
            System.err.println("No se pudo enviar el correo a " + prmTo + ": " + e.getMessage());
            opSaveToOutbox(prmTo, prmSubject, prmBody, "ERROR al enviar: " + e.getMessage());
            return false;
        }
    }

    /** Conversación SMTP básica: EHLO, AUTH LOGIN, MAIL FROM, RCPT TO, DATA, QUIT. */
    private void opSendSmtp(String prmTo, String prmSubject, String prmBody) throws IOException {
        SSLSocketFactory varFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
        try (SSLSocket varSocket = (SSLSocket) varFactory.createSocket(attHost, attPort)) {
            varSocket.setSoTimeout(15000);
            BufferedReader varIn = new BufferedReader(new InputStreamReader(varSocket.getInputStream(), StandardCharsets.UTF_8));
            Writer varOut = new OutputStreamWriter(varSocket.getOutputStream(), StandardCharsets.UTF_8);
            opExpect(varIn, 220);
            opCommand(varIn, varOut, "EHLO localhost", 250);
            opCommand(varIn, varOut, "AUTH LOGIN", 334);
            opCommand(varIn, varOut, opBase64(attUser), 334);
            opCommand(varIn, varOut, opBase64(attPassword), 235);
            opCommand(varIn, varOut, "MAIL FROM:<" + attFrom + ">", 250);
            opCommand(varIn, varOut, "RCPT TO:<" + prmTo + ">", 250);
            opCommand(varIn, varOut, "DATA", 354);
            String varMessage = "From: " + attFrom + "\r\n"
                    + "To: " + prmTo + "\r\n"
                    + "Subject: =?UTF-8?B?" + opBase64(prmSubject) + "?=\r\n"
                    + "MIME-Version: 1.0\r\n"
                    + "Content-Type: text/plain; charset=UTF-8\r\n"
                    + "Content-Transfer-Encoding: base64\r\n\r\n"
                    + Base64.getMimeEncoder().encodeToString(prmBody.getBytes(StandardCharsets.UTF_8))
                    + "\r\n.";
            opCommand(varIn, varOut, varMessage, 250);
            opCommand(varIn, varOut, "QUIT", 221);
        }
    }

    private static void opCommand(BufferedReader prmIn, Writer prmOut, String prmLine, int prmExpected) throws IOException {
        prmOut.write(prmLine + "\r\n");
        prmOut.flush();
        opExpect(prmIn, prmExpected);
    }

    /** Lee la respuesta del servidor (puede tener varias líneas) y verifica el código. */
    private static void opExpect(BufferedReader prmIn, int prmExpected) throws IOException {
        String varLine;
        do {
            varLine = prmIn.readLine();
            if (varLine == null) {
                throw new IOException("El servidor cerró la conexión.");
            }
        } while (varLine.length() > 3 && varLine.charAt(3) == '-');
        if (!varLine.startsWith(String.valueOf(prmExpected))) {
            throw new IOException("Respuesta inesperada del servidor: " + varLine);
        }
    }

    private static String opBase64(String prmText) {
        return Base64.getEncoder().encodeToString(prmText.getBytes(StandardCharsets.UTF_8));
    }

    private static boolean opSaveToOutbox(String prmTo, String prmSubject, String prmBody, String prmStatus) {
        new File("data").mkdirs();
        try (PrintWriter varWriter = new PrintWriter(new OutputStreamWriter(
                new FileOutputStream(OUTBOX_FILE, true), StandardCharsets.UTF_8))) {
            varWriter.println("==================================================");
            varWriter.println("Fecha: " + LocalDateTime.now().withNano(0));
            varWriter.println("Estado: " + prmStatus);
            varWriter.println("Para: " + prmTo);
            varWriter.println("Asunto: " + prmSubject);
            varWriter.println();
            varWriter.println(prmBody);
            return true;
        } catch (IOException e) {
            System.err.println("No se pudo escribir la bandeja de salida: " + e.getMessage());
            return false;
        }
    }
}
