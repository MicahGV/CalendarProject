package Calendar;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MysqlConnection {
    private static final String url =  null;
    private static final String user = null;
    private static final String pass = null;
    public Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e){
            e.printStackTrace();
        }
        return DriverManager.getConnection(url,user,pass);
    }
}
