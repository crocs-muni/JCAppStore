package cz.muni.crocs.appletstore.database;

import apdu4j.HexUtils;
import cz.muni.crocs.appletstore.Config;
import pro.javacard.gp.GPData;

import javax.xml.crypto.Data;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.stream.Collectors;

import static pro.javacard.gp.GPData.CPLC.toDateFailsafe;

/**
 * @author Jiří Horák
 * @version 1.0
 */
public class DatabaseManager {

    public static final int CONNECT = 1;
    public static final int CONNECT_AND_CREATE_TABLE_IF_NOT_EXISTS = 2;
    public static final int INSERT_CARD_IF_NOT_EXISTS = 3;

    public DatabaseManager() {
        try {
            SQLiteDatabase database = new SQLiteDatabase(Config.DATABASE_URL);
            //once check for tables presence
            database.connectAndCreateTableIfNotExists();
            database.close();
        } catch (SQLException e) {
            e.printStackTrace();

            //todo error could not run database
        }

    }

    public static boolean insertQuery() {
        try {
            SQLiteDatabase database = new SQLiteDatabase(Config.DATABASE_URL);
            database.connect();


            database.close();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


}
