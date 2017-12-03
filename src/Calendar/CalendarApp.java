package Calendar;

import Calendar.Controllers.AppointmentInfoController;
import Calendar.Controllers.CalendarController;
import Calendar.Controllers.CustomerInfoController;
import Calendar.Controllers.LoginController;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Locale;
import java.util.ResourceBundle;

public class CalendarApp extends Application {
    private Stage primaryStage;
    private Stage currentStage;
    private MysqlConnection mysqlConnection = new MysqlConnection();
    private static ResourceBundle resourceBundle;
    private String user;
    private LocalDateTime userLogin;
    private ObservableList<Customer> customerData = FXCollections.observableArrayList();
    private ObservableList<Appointment> appointmentData = FXCollections.observableArrayList();

    @Override
    public void start(Stage primaryStage) throws Exception{
        this.primaryStage = primaryStage;
        primaryStage.setTitle("Calendar");
        getCustomerData();
        getAppointmentData();
        showLogin();
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public Stage getCurrentStage() {
        return currentStage;
    }

    public void setUserLogin(LocalDateTime userLogin) {
        this.userLogin = userLogin;
    }

    public void setCurrentStage(Stage currentStage) {
        this.currentStage = currentStage;
    }

    public void showLogin(){
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setResources(resourceBundle);
            loader.setLocation(getClass().getResource("FXMLs/login.fxml"));
            LoginController loginController = new LoginController();
            loginController.setMysqlConnection(mysqlConnection);
            loginController.setCalendarApp(this);
            loader.setController(loginController);
            Scene loginScene = new Scene(loader.load());
            Stage loginStage = new Stage();
            loginStage.setTitle("Login");
            loginStage.initOwner(primaryStage);
            loginStage.setScene(loginScene);
            currentStage = loginStage;
            loginStage.showAndWait();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public void reminder(){
        Timeline reminder = new Timeline(new KeyFrame(Duration.seconds(10), event -> {
            Long loginSeconds = userLogin.toEpochSecond(ZoneOffset.UTC);
            for(Appointment appointment: appointmentData){
                Long startSeconds = appointment.getStart().toLocalDateTime().toEpochSecond(ZoneOffset.UTC);
                Long difference = startSeconds - loginSeconds;
                /*System.out.println(difference);
                System.out.println(startSeconds);
                System.out.println(loginSeconds);*/
                if(user.equals(appointment.getUser())) {
                    if (difference <= 900 && difference > 0) {
                        if (!appointment.getReminded()) {
                            String message = "Appointment Reminder for " + appointment.getTitle() + " at " + appointment.getStart();
                            (new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK)).show();
                            appointment.setReminded(true);
                        }
                    }
                }
            }
        }));
        reminder.setCycleCount(Timeline.INDEFINITE);
        reminder.play();
    }

    public void showCalendar(){
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("FXMLs/Calendar.fxml"));
            CalendarController calendarController = new CalendarController(this,mysqlConnection, customerData,appointmentData);
            loader.setController(calendarController);
            Stage calendarStage = new Stage();
            Scene calenderScene = new Scene(loader.load());
            primaryStage = calendarStage;
            calendarStage.setTitle("Calendar");
            calendarStage.setScene(calenderScene);
            calendarStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showCustomerInfo(boolean editing, Customer customer){
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("FXMLs/CustomerInfo.fxml"));
            CustomerInfoController customerInfoController = new CustomerInfoController(this,mysqlConnection,editing, customer);
            loader.setController(customerInfoController);
            Stage customerStage = new Stage();
            Scene customerScene = new Scene(loader.load());
            customerStage.setTitle("Customer Info");
            customerStage.setScene(customerScene);
            currentStage = customerStage;
            customerStage.showAndWait();
        } catch(IOException e){
            e.printStackTrace();
        }
    }
    public void showAppointmentInfo(boolean editing, Appointment appointment){
        try{
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("FXMLs/appointmentInfo.fxml"));
            AppointmentInfoController appointmentInfoController = new AppointmentInfoController(this,customerData, mysqlConnection,editing, appointment,appointmentData);
            loader.setController(appointmentInfoController);
            Stage appointmentStage = new Stage();
            appointmentStage.setTitle("Appointment Info");
            Scene appointmentScene = new Scene(loader.load());
            appointmentStage.setScene(appointmentScene);
            currentStage = appointmentStage;
            appointmentStage.showAndWait();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public ObservableList getCustomerData(){
        try(Connection connection = mysqlConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement("SELECT active,c.customerId,customerName,phone,a.addressId,address,address2,postalCode,ci.cityId,city,co.countryId,country " +
                    "FROM customer c " +
                    "LEFT JOIN address a ON c.addressId = a.addressId " +
                    "LEFT JOIN city ci ON a.cityId = ci.cityId " +
                    "LEFT JOIN country co ON ci.countryId = co.countryId;");
            ResultSet rs = statement.executeQuery()
        ){
            while (rs.next()) {
                Customer customer = new Customer();
                customer.setActive(rs.getBoolean("active"));
                customer.setCustomerId(rs.getInt("c.customerId"));
                customer.setAddressId(rs.getInt("a.addressId"));
                customer.setCountryId(rs.getInt("co.countryId"));
                customer.setCityId(rs.getInt("ci.cityId"));
                customer.setName(rs.getString("customerName"));
                customer.setPhone(rs.getString("phone"));
                customer.setAddress(rs.getString("address"));
                customer.setAddressTwo(rs.getString("address2"));
                customer.setZipcode(rs.getString("postalCode"));
                customer.setCity(rs.getString("city"));
                customer.setCountry(rs.getString("country"));
                customerData.add(customer);
            }
        }catch (SQLException e){
            e.printStackTrace();
        }
        return customerData;
    }
    public ObservableList getAppointmentData(){
        try(Connection connection = mysqlConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM appointment");
            ResultSet rs = statement.executeQuery()){
            while (rs.next()){
                Appointment appointment = new Appointment();
                appointment.setCustomerId(rs.getInt("customerId"));
                appointment.setAppointmentId(rs.getInt("appointmentId"));
                appointment.setTitle(rs.getString("title"));
                appointment.setDescription(rs.getString("description"));
                appointment.setLocation(rs.getString("location"));
                appointment.setContact(rs.getString("contact"));
                //CONVERSION TIMEZONES
                appointment.setStart(timeZoneConversion(rs.getTimestamp("start")));
                appointment.setEnd(timeZoneConversion(rs.getTimestamp("end")));
                appointment.setUser(rs.getString("createdBy"));
                appointmentData.add(appointment);
            }
        } catch (SQLException e){
                e.printStackTrace();
        }
        return appointmentData;
    }

    public void deleteAppointment(Appointment appointment){
        try(Connection connection = mysqlConnection.getConnection();
        PreparedStatement statement = deleteStatement(connection,appointment);){
            statement.execute();
        }catch (SQLException e){
            e.printStackTrace();
        }
    }
    private PreparedStatement deleteStatement(Connection c, Appointment appointment) throws SQLException{
        PreparedStatement st = c.prepareStatement("DELETE FROM appointment " +
                "WHERE appointmentId = ?;");
        st.setInt(1,appointment.getAppointmentId());
        return st;
    }
    public Timestamp timeZoneConversion(Timestamp timestamp){
        ZoneOffset zo = OffsetDateTime.now().getOffset();
        int offsetseconds = zo.getTotalSeconds()*1000;
        Long change = timestamp.getTime()+offsetseconds;
        timestamp.setTime(change);
        return timestamp;
    }

    public static void main(String[] args) {
        //Locale.setDefault(new Locale("es","MX"));
        resourceBundle = ResourceBundle.getBundle("calendar.Resources.Languages");
        launch(args);
    }
}
