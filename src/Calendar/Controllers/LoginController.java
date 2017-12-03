package Calendar.Controllers;

import Calendar.CalendarApp;
import Calendar.MysqlConnection;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.text.Text;

import java.io.BufferedWriter;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ResourceBundle;

public class LoginController implements Initializable{

    @FXML private Text UsernameText;
    @FXML private TextField usernameTextField;
    @FXML private Text passwordText;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Text error;
    @FXML private Text loginText;
    private CalendarApp calendarApp;
    private ResourceBundle resourceBundle;
    private MysqlConnection mysqlConnection;

    public void setCalendarApp(CalendarApp calendarApp) {
        this.calendarApp = calendarApp;
    }

    public void setLocaleWords(){
        UsernameText.setText(resourceBundle.getString("username"));
        usernameTextField.setPromptText(resourceBundle.getString("username"));
        passwordText.setText(resourceBundle.getString("password"));
        passwordField.setPromptText(resourceBundle.getString("password"));
        loginButton.setText(resourceBundle.getString("login"));
        loginText.setText(resourceBundle.getString("login"));
    }

    public void setMysqlConnection(MysqlConnection mysqlConnection) {
        this.mysqlConnection = mysqlConnection;
    }

    public void login(ActionEvent event) {
        String userName = usernameTextField.getText();
        String password = passwordField.getText();
        try(Connection connection = mysqlConnection.getConnection();
            PreparedStatement ps = createPreparedStatement(connection,userName,password);
            ResultSet rs = ps.executeQuery()) {
            LocalDateTime timestamp = LocalDateTime.now();
            if(rs.next()){
                calendarApp.getCurrentStage().close();
                calendarApp.setUser(userName);
                calendarApp.showCalendar();
                System.out.println(timestamp);
                calendarApp.setUserLogin(timestamp);
                writeTofile(true,timestamp.toString(),userName);
            } else {
                writeTofile(false,timestamp.toString(),userName);
                throw new IllegalArgumentException(resourceBundle.getString("invalidU"));
            }
        } catch (IllegalArgumentException | SQLException e){
            (new Alert(Alert.AlertType.ERROR,e.getLocalizedMessage(), ButtonType.OK)).showAndWait();
            e.printStackTrace();
        }
    }

    private PreparedStatement createPreparedStatement(Connection connection, String username, String password) throws SQLException{
        PreparedStatement preparedStatement = connection.prepareStatement("SELECT userName, password FROM user WHERE userName = ? AND password = ?");
        preparedStatement.setString(1, username);
        preparedStatement.setString(2,password);
        return preparedStatement;
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        resourceBundle = resources;
        setLocaleWords();
    }
    public void writeTofile(boolean loggedIn,String data,String username){
        if(loggedIn){
            data += " "+username+" Logged in"+System.lineSeparator();
        } else {
            data += " "+username+" Failed to log in"+System.lineSeparator();
        }
        Path path = Paths.get(System.getProperty("user.home")+"\\Documents\\c195 Login Log.txt");
        boolean fileExists = Files.exists(path);
        StandardOpenOption standardOpenOption = null;
        if(fileExists){
            standardOpenOption = StandardOpenOption.APPEND;
        } else{
            standardOpenOption = StandardOpenOption.CREATE;
        }
        try(BufferedWriter bw = Files.newBufferedWriter(path, Charset.defaultCharset(), standardOpenOption)){
            bw.write(data);
        } catch (IOException e){
            e.printStackTrace();
        }
    }
}
