package pkgPersistence;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Patrón Singleton (GoF): única instancia que sabe dónde está la base de
 * datos SQLite y entrega conexiones a los repositorios. La base se crea en
 * data/banco_preguntas.db (relativa a la carpeta del proyecto).
 */
public final class clsConnectionSQL {

    public static final String DB_FILE = "data" + File.separator + "banco_preguntas.db";

    private static clsConnectionSQL attInstance;

    private final String attUrl;

    private clsConnectionSQL() {
        new File("data").mkdirs();
        attUrl = "jdbc:sqlite:" + DB_FILE;
    }

    public static synchronized clsConnectionSQL opGetInstance() {
        if (attInstance == null) {
            attInstance = new clsConnectionSQL();
        }
        return attInstance;
    }

    public Connection opGetConnection() throws SQLException {
        return DriverManager.getConnection(attUrl);
    }

    /** Crea las tablas si no existen (la pregunta guarda el código de su estado y de su tipo). */
    public void opCreateTables() {
        String[] varTables = {
            "CREATE TABLE IF NOT EXISTS tbl_user ("
            + " id TEXT PRIMARY KEY,"
            + " full_name TEXT NOT NULL,"
            + " nickname TEXT NOT NULL UNIQUE,"
            + " email TEXT,"
            + " password_hash TEXT NOT NULL,"
            + " role TEXT NOT NULL,"
            + " active INTEGER NOT NULL)",
            "CREATE TABLE IF NOT EXISTS tbl_question ("
            + " id TEXT PRIMARY KEY,"
            + " id_author TEXT NOT NULL REFERENCES tbl_user(id),"
            + " state TEXT NOT NULL,"
            + " type TEXT NOT NULL,"
            + " context TEXT, statement TEXT,"
            + " option_a TEXT, option_b TEXT, option_c TEXT, option_d TEXT,"
            + " right_answer TEXT, justification TEXT, bibliography TEXT,"
            + " competence TEXT, topic TEXT, subtopic TEXT, difficulty TEXT,"
            + " created_at TEXT, updated_at TEXT)",
            "CREATE TABLE IF NOT EXISTS tbl_review_assignment ("
            + " id TEXT PRIMARY KEY,"
            + " id_question TEXT NOT NULL REFERENCES tbl_question(id),"
            + " id_reviewer TEXT NOT NULL REFERENCES tbl_user(id),"
            + " id_assigned_by TEXT REFERENCES tbl_user(id),"
            + " assigned_at TEXT NOT NULL)"};
        try (Connection varCon = opGetConnection(); Statement varSt = varCon.createStatement()) {
            for (String varSql : varTables) {
                varSt.execute(varSql);
            }
        } catch (SQLException e) {
            System.err.println("Error al crear las tablas: " + e.getMessage());
        }
    }
}
