package Calendar.Controllers;

import Calendar.CalendarApp;
import Calendar.Customer;
import Calendar.MysqlConnection;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.sql.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ResourceBundle;

public class CustomerInfoController implements Initializable {

    @FXML private Button addButton;
    @FXML private Button saveButton;
    @FXML private RadioButton active;
    @FXML private RadioButton inactive;
    @FXML private TextField Id;
    @FXML private TextField Name;
    @FXML private TextField Phone;
    @FXML private TextField addressOne;
    @FXML private TextField addressTwo;
    @FXML private TextField Country;
    @FXML private TextField City;
    @FXML private TextField Zipcode;
    private CalendarApp calendarApp;
    private Connection connection = null;
    private PreparedStatement statement;
    private MysqlConnection mysqlConnection;
    private boolean editing;
    private Customer customer;
    public CustomerInfoController(CalendarApp calendarApp, MysqlConnection mysqlConnection, boolean editing, Customer customer) {
        this.calendarApp = calendarApp;
        this.mysqlConnection = mysqlConnection;
        this.editing = editing;
        this.customer = customer;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if(editing){
            addButton.setVisible(false);
            setData();
        } else {
            saveButton.setVisible(false);
        }
    }

    public void addCustomerInfo(ActionEvent event) {
        //This is one of the ugliest things I have ever seen

        int activeNum = 0;
        String customerName = "";
        String phone = "";
        String address = "";
        String address2 = "";
        String country = "";
        String city = "";
        String postalCode = "";
        Timestamp timestamp = null;
        String user = "";
        int countryId = 0;
        int cityId = 0;
        int addressId = 0;
        //int Id = Integer.parseInt(this.Id.getText());
        try {
            if (active.isSelected()) {
                activeNum = 1;
            } else if (inactive.isSelected()) {
                activeNum = 0;
            } else {
                throw new IllegalStateException("Please select active or inactive");
            }
            customerName = Name.getText().toUpperCase();
            phone = Phone.getText();
            address = this.addressOne.getText().toUpperCase();
            address2 = addressTwo.getText().toUpperCase();
            country = Country.getText().toUpperCase();
            city = this.City.getText().toUpperCase();
            postalCode = Zipcode.getText();
            timestamp = Timestamp.from(Instant.now());
            user = calendarApp.getUser();
            countryId = 0;
            cityId = 0;
            addressId = 0;
        } catch (IllegalStateException e){
            (new Alert(Alert.AlertType.ERROR,e.getLocalizedMessage(),ButtonType.OK)).showAndWait();
        }
        try {
            connection = mysqlConnection.getConnection();

            //Check for country existing
            statement = connection.prepareStatement("SELECT country,countryId FROM country WHERE country = ?");
            statement.setString(1,country);
            ResultSet rs = statement.executeQuery();
            //get countryId for possible City row
            if(rs.next()){
                countryId = rs.getInt("countryId");
            } else {
                //Add Country
                statement = connection.prepareStatement("INSERT INTO country(country,createDate,createdBy,lastUpdate,lastUpdateby) " +
                        "VALUES (?,?,?,?,?)",Statement.RETURN_GENERATED_KEYS);
                statement.setString(1,country);
                statement.setTimestamp(2,timestamp);
                statement.setString(3,user);
                statement.setTimestamp(4,timestamp);
                statement.setString(5,user);
                statement.executeUpdate();

                //get countryId for possible City row

                ResultSet keys = statement.getGeneratedKeys();
                if(keys.next()){
                    countryId = keys.getInt(1);
                    keys.close();
                }
            }
            rs.close();
            //Check for city existing
            statement = connection.prepareStatement("SELECT city,cityId FROM city WHERE city = ? AND countryId = ?");
            statement.setString(1,city);
            statement.setInt(2,countryId);
            rs = statement.executeQuery();
            //get cityId for possible address row
            if(rs.next()){
                cityId = rs.getInt("cityId");
            } else {
                //Add city

                statement = connection.prepareStatement("INSERT INTO city(city,countryId,createDate,createdBy,lastUpdate,lastUpdateBy) " +
                        "VALUES (?,?,?,?,?,?)",Statement.RETURN_GENERATED_KEYS);
                statement.setString(1,city);
                statement.setInt(2,countryId);
                statement.setTimestamp(3,timestamp);
                statement.setString(4,user);
                statement.setTimestamp(5,timestamp);
                statement.setString(6,user);
                statement.executeUpdate();
                //get cityId for possible address row
                ResultSet keys = statement.getGeneratedKeys();
                if(keys.next()){
                    cityId = keys.getInt(1);
                    keys.close();
                }
            }
            rs.close();
            //Check for address existing
            statement = connection.prepareStatement("SELECT address,addressId FROM address WHERE address = ? AND cityId = ?");
            statement.setString(1,address);
            statement.setInt(2,cityId);
            rs = statement.executeQuery();
            //get addressId for customer row
            if(rs.next()){
                addressId = rs.getInt("addressId");
            } else {
                //Add Address
                statement = connection.prepareStatement("INSERT INTO address(address,address2,cityId,postalCode,phone,createDate,createdBy,lastUpdate,lastUpdateBy) " +
                        "VALUES(?,?,?,?,?,?,?,?,?)",Statement.RETURN_GENERATED_KEYS);
                statement.setString(1,address);
                statement.setString(2,address2);
                statement.setInt(3,cityId);
                statement.setString(4,postalCode);
                statement.setString(5,phone);
                statement.setTimestamp(6,timestamp);
                statement.setString(7,user);
                statement.setTimestamp(8,timestamp);
                statement.setString(9,user);
                statement.executeUpdate();
                ResultSet keys = statement.getGeneratedKeys();
                //get addressId for customer row
                if(keys.next()){
                    addressId = keys.getInt(1);
                    keys.close();
                }
            }
            rs.close();

            statement = connection.prepareStatement("INSERT INTO customer(customerName,addressId,active,createDate,createdBy,lastUpdate,lastUpdateBy) " +
                    "VALUES(?,?,?,?,?,?,?)");
            statement.setString(1,customerName);
            statement.setInt(2,addressId);
            statement.setInt(3,activeNum);
            statement.setTimestamp(4,timestamp);
            statement.setString(5,user);
            statement.setTimestamp(6,timestamp);
            statement.setString(7,user);
            statement.executeUpdate();
        } catch (SQLException | IllegalStateException e){
            (new Alert(Alert.AlertType.ERROR,e.getLocalizedMessage(),ButtonType.OK)).showAndWait();
            e.printStackTrace();
        } finally {
            try {
                connection.close();
                statement.close();
            }
            catch (SQLException error){
                error.printStackTrace();
            }
        }
        calendarApp.getCurrentStage().close();
    }
    public void editCustomerInfo(ActionEvent event){
        int activeNum = 0;
        int id = 0;
        String customerName = "";
        String phone = "";
        String address = "";
        String address2 = "";
        String country = "";
        String city = "";
        String postalCode = "";
        Timestamp timestamp = null;
        String user = "";
        int countryId = customer.getCountryId();
        int cityId = customer.getCityId();
        int addressId = customer.getAddressId();
        try{
            if (active.isSelected()) {
                activeNum = 1;
            } else if (inactive.isSelected()) {
                activeNum = 0;
            } else {
                throw new IllegalStateException("Please select active or inactive");
            }
            id = Integer.parseInt(this.Id.getText());
            customerName = Name.getText().toUpperCase();
            phone = Phone.getText();
            address = addressOne.getText().toUpperCase();
            address2 = addressTwo.getText().toUpperCase();
            country = Country.getText().toUpperCase();
            city = City.getText().toUpperCase();
            postalCode = Zipcode.getText();
            timestamp = Timestamp.valueOf(LocalDateTime.now());
            user = calendarApp.getUser();

            connection = mysqlConnection.getConnection();
            connection.setAutoCommit(false);
            statement = connection.prepareStatement("UPDATE customer " +
                    "SET customerName = ?,active = ?,lastUpdateBy = ? " +
                    "WHERE customerId = ?");
            statement.setString(1,customerName);
            statement.setInt(2,activeNum);
            statement.setString(3,user);
            statement.setInt(4,id);
            statement.executeUpdate();

            statement = connection.prepareStatement("UPDATE address " +
                    "SET address = ?, address2 = ?, postalCode = ?,phone = ?,lastUpdateBy = ? " +
                    "WHERE addressId = ?");
            statement.setString(1,address);
            statement.setString(2,address2);
            statement.setString(3,postalCode);
            statement.setString(4,phone);
            statement.setString(5,user);
            statement.setInt(6,addressId);
            statement.executeUpdate();

            statement = connection.prepareStatement("UPDATE city " +
                    "SET city = ?,lastUpdateBy = ? " +
                    "WHERE cityId = ?");
            statement.setString(1,city);
            statement.setString(2,user);
            statement.setInt(3,cityId);
            statement.executeUpdate();

            statement = connection.prepareStatement("Update country " +
                    "SET country = ?,lastUpdateBy = ? " +
                    "WHERE countryId = ?");
            statement.setString(1,country);
            statement.setString(2,user);
            statement.setInt(3,countryId);
            statement.executeUpdate();
            connection.commit();
        }catch (SQLException e){
            e.printStackTrace();
        }finally {
            try {
                connection.close();
                statement.close();
                calendarApp.getCurrentStage().close();
            } catch (SQLException e){
                e.printStackTrace();
            }
        }
    }
    public void setData(){
        if(customer.isActive()){
            active.setSelected(true);
        } else {
            inactive.setSelected(true);
        }
        Id.setText(String.valueOf(customer.getCustomerId()));
        Name.setText(customer.getName());
        Phone.setText(customer.getPhone());
        addressOne.setText(customer.getAddress());
        addressTwo.setText(customer.getAddressTwo());
        Zipcode.setText(customer.getZipcode());
        City.setText(customer.getCity());
        Country.setText(customer.getCountry());
    }
}
