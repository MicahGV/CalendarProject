package Calendar.Controllers;

import Calendar.Appointment;
import Calendar.CalendarApp;
import Calendar.Customer;
import Calendar.MysqlConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import javafx.event.ActionEvent;
import javafx.scene.input.MouseEvent;
import javafx.util.StringConverter;

import java.net.URL;
import java.sql.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class AppointmentInfoController implements Initializable {

    @FXML private TableView customeraptTable;
    @FXML private TableColumn customerNameColumn;
    @FXML private TableColumn customerPhoneColumn;
    @FXML private TableColumn customerActiveColumn;

    @FXML private Button addAppointment;
    @FXML private Button editAppointment;

    @FXML private ChoiceBox startAMPM;
    @FXML private ChoiceBox endAMPM;
    @FXML private Spinner   startHoursTimeSpinerr;
    @FXML private Spinner   startMinutesTimeSpinner;
    @FXML private Spinner   endHoursTimeSpinner;
    @FXML private Spinner   endMinutesTimeSpinner;
    @FXML private ChoiceBox appointmentType;
    @FXML private DatePicker datePicker;
    @FXML private TextField title;
    @FXML private TextField customerName;
    @FXML private TextField customerId;
    @FXML private TextField location;
    @FXML private TextField customerPhone;

    private CalendarApp calendarApp;
    private ObservableList<Customer> customerTableData;
    private ObservableList<Appointment> appointmentData;
    private MysqlConnection mysqlConnection;
    private boolean editing;
    private Appointment appointment;

    public AppointmentInfoController(CalendarApp calendarApp, ObservableList customerTableData, MysqlConnection mysqlConnection, boolean editing, Appointment appointment, ObservableList appointmentData) {
        this.calendarApp = calendarApp;
        this.customerTableData = customerTableData;
        this.mysqlConnection = mysqlConnection;
        this.editing = editing;
        this.appointment = appointment;
        this.appointmentData = appointmentData;
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ObservableList<String> amPm = FXCollections.observableArrayList();
        amPm.addAll("AM","PM");
        ObservableList<String> appointmentTypes = FXCollections.observableArrayList();
        appointmentTypes.setAll("Blowing stuff up","Consultation Meeting","Leisure","Strategy Meeting");
        SpinnerValueFactory startHours = new SpinnerValueFactory.IntegerSpinnerValueFactory(1,12);
        startHours.setWrapAround(true);
        SpinnerValueFactory startMinutes = new SpinnerValueFactory.IntegerSpinnerValueFactory(0,60);
        startMinutes.setWrapAround(true);
        SpinnerValueFactory endHours = new SpinnerValueFactory.IntegerSpinnerValueFactory(1,12);
        endHours.setWrapAround(true);
        SpinnerValueFactory endMinutes = new SpinnerValueFactory.IntegerSpinnerValueFactory(0,60);
        endMinutes.setWrapAround(true);
        startHoursTimeSpinerr.setValueFactory(startHours);
        startMinutesTimeSpinner.setValueFactory(startMinutes);
        endHoursTimeSpinner.setValueFactory(endHours);
        endMinutesTimeSpinner.setValueFactory(endMinutes);

        customerActiveColumn.setCellValueFactory(new PropertyValueFactory<Customer,Boolean>("active"));
        customerNameColumn.setCellValueFactory(new PropertyValueFactory<Customer,Integer>("name"));
        customerPhoneColumn.setCellValueFactory(new PropertyValueFactory<Customer,String>("phone"));

        startAMPM.setItems(amPm);
        endAMPM.setItems(amPm);
        appointmentType.setItems(appointmentTypes);
        customeraptTable.setItems(customerTableData);

        //Lambda expressions for handling appointments
        addAppointment.setOnAction(e -> addAppointment(e));
        editAppointment.setOnAction(e -> editAppointment(e));

        if(editing){
            addAppointment.setVisible(false);
            setData();
        } else {
            editAppointment.setVisible(false);
        }

        datePicker.setConverter(new StringConverter<LocalDate>()
        {
            private DateTimeFormatter dateTimeFormatter=DateTimeFormatter.ofPattern("MM/dd/yyyy");

            @Override
            public String toString(LocalDate localDate)
            {
                if(localDate==null)
                    return "";
                return dateTimeFormatter.format(localDate);
            }

            @Override
            public LocalDate fromString(String dateString)
            {
                if(dateString==null || dateString.trim().isEmpty())
                {
                    return null;
                }
                return LocalDate.parse(dateString,dateTimeFormatter);
            }
        });
    }


    public void setData(){
        LocalDateTime start = appointment.getStart().toLocalDateTime();
        LocalDateTime end = appointment.getEnd().toLocalDateTime();
        String startAmPm;
        String endAmPm;
        int starthour = start.getHour();
        int endhour = end.getHour();
        System.out.println(starthour);
        if(start.getHour() > 12){
            startAmPm = "PM";
            starthour -= 12;
        } else {
            startAmPm = "AM";
        }
        if(end.getHour() > 12){
            endAmPm = "PM";
            endhour -= 12;
        } else {
            endAmPm = "AM";
        }
        Customer customer = null;
        for(Customer customer1: customerTableData){
            if(customer1.getCustomerId() == appointment.getCustomerId()){
                customer = customer1;
            }
        }
        startAMPM.getSelectionModel().select(startAmPm);
        endAMPM.getSelectionModel().select(endAmPm);
        startHoursTimeSpinerr.getEditor().setText(String.valueOf(starthour));
        startMinutesTimeSpinner.getEditor().setText(String.valueOf(start.getMinute()));
        endHoursTimeSpinner.getEditor().setText(String.valueOf(endhour));
        endMinutesTimeSpinner.getEditor().setText(String.valueOf(end.getMinute()));
        appointmentType.getSelectionModel().select(appointment.getDescription());
        datePicker.setValue(start.toLocalDate());
        title.setText(appointment.getTitle());
        customerName.setText(customer.getName());
        customerId.setText(String.valueOf(customer.getCustomerId()));
        customerPhone.setText(appointment.getContact());
        location.setText(appointment.getLocation());

        //A very strange bug caused me to do this
        startHoursTimeSpinerr.increment();
        startHoursTimeSpinerr.decrement();
        endHoursTimeSpinner.increment();
        endHoursTimeSpinner.decrement();

    }

    public void setSelectedCustomer(MouseEvent event) {
        Customer customer = (Customer) customeraptTable.getSelectionModel().getSelectedItem();
        if(!customer.isActive()){
            customeraptTable.getSelectionModel().clearSelection();
        } else {
            if (customer != null) {

                customerName.setText(customer.getName());
                customerId.setText(String.valueOf(customer.getCustomerId()));
                customerPhone.setText(customer.getPhone());
            }
        }
    }
     /* IMPORTANT UTC TO LOCAL TIME ZONE CONVERSION
                ZonedDateTime utc = ZonedDateTime.now(ZoneOffset.UTC);
                Timestamp timestamp = Timestamp.valueOf(utc.toLocalDateTime());
                ZoneOffset zo = OffsetDateTime.now().getOffset();
                int offsetseconds = zo.getTotalSeconds()*1000;
                Long change = timestamp.getTime();
                Long stuff = change + offsetseconds;
                timestamp.setTime(stuff);
           LOCAL TO UTC
                ZonedDateTime local = ZonedDateTime.of(2017,11,22,12,6,0,0,ZoneId.systemDefault());
                ZonedDateTime UTCturned = local.withZoneSameInstant(ZoneOffset.UTC);
                Timestamp time = Timestamp.valueOf(UTCturned.toLocalDateTime());
         */

    public void addAppointment(ActionEvent event){
        try(Connection connection = mysqlConnection.getConnection();
            PreparedStatement statement = addStatement(connection)){
            statement.executeUpdate();
        } catch (SQLException e){
                e.printStackTrace();
        }
        calendarApp.getCurrentStage().close();
    }
    public void editAppointment(ActionEvent event){
        try(Connection connection = mysqlConnection.getConnection();
            PreparedStatement statement = editStatement(connection)){
            statement.executeUpdate();
        } catch (SQLException e){
            e.printStackTrace();
        }
        calendarApp.getCurrentStage().close();
    }
    private PreparedStatement editStatement(Connection connection) throws SQLException{
        PreparedStatement statement = null;
        try {
            LocalDate localDate = datePicker.getValue();
            String name = customerName.getText();
            int startHours = (int) startHoursTimeSpinerr.getValue();
            int startMinutes = (int) startMinutesTimeSpinner.getValue();
            int endHours = (int) endHoursTimeSpinner.getValue();
            int endMinutes = (int) endMinutesTimeSpinner.getValue();
            String startAMPM = (String) this.startAMPM.getValue();
            String endAMPM = (String) this.endAMPM.getValue();
            if(startAMPM.equals("PM")){
                startHours += 12;
            }
            if(endAMPM.equals("PM")){
                endHours += 12;
            }
            int Year = localDate.getYear();
            int Month = localDate.getMonthValue();
            int Day = localDate.getDayOfMonth();
            String nameOfDay = localDate.getDayOfWeek().name();
            if(nameOfDay.equals("SATURDAY")||nameOfDay.equals("SUNDAY")||(startHours > 17 || startHours < 9)||(endHours > 17 || endHours < 9)){
                throw new IllegalArgumentException("Please choose a different time. Bussiness hours are Monday to Friday 9 AM to 5 PM");
            }

            ZonedDateTime startLocal = ZonedDateTime.of(Year,Month,Day,startHours,startMinutes,0,0, ZoneOffset.systemDefault());
            ZonedDateTime startUTC = startLocal.withZoneSameInstant(ZoneOffset.UTC);
            Timestamp possibleStart = Timestamp.valueOf(startUTC.toLocalDateTime());

            ZonedDateTime endLocal = ZonedDateTime.of(Year,Month,Day,endHours,endMinutes,0,0, ZoneOffset.systemDefault());
            ZonedDateTime endUTC = endLocal.withZoneSameInstant(ZoneOffset.UTC);
            Timestamp possibleEnd = Timestamp.valueOf(endUTC.toLocalDateTime());
            if(possibleStart.before(Timestamp.from(Instant.now()))){
                throw new IllegalArgumentException("Appointment is in the past. Please try again");
            }
            if(possibleStart.after(possibleEnd)){
                throw new IllegalArgumentException("Start appointment time is after end of appointment. Please try again");
            }

            for(Appointment appt : appointmentData){
                if(appt.getAppointmentId() == this.appointment.getAppointmentId()){
                    break;
                }
                Timestamp start = appt.getStart();
                Timestamp end = appt.getEnd();
                if ((Timestamp.valueOf(startLocal.toLocalDateTime()).after(start)||Timestamp.valueOf(startLocal.toLocalDateTime()).equals(start)) && (Timestamp.valueOf(endLocal.toLocalDateTime()).before(end)||Timestamp.valueOf(endLocal.toLocalDateTime()).equals(end)) && calendarApp.getUser().equals(appointment.getUser())){
                    throw new IllegalArgumentException("Unable to process: Overlapping appointment "+System.lineSeparator()+"Between "+ start.toString()+" and " + end.toString());
                }
            }
            String Type = (String) appointmentType.getValue();
            String Title = title.getText();
            int custId = Integer.parseInt(customerId.getText());
            String Contact = customerPhone.getText();
            String Location = location.getText();
            statement = connection.prepareStatement("UPDATE appointment " +
                    "SET customerId = ?, title = ?, description = ?, location = ?, contact = ?, start = ?, end = ?, lastUpdateBy = ?" +
                    "WHERE appointmentId = ?");
            statement.setInt(1,custId);
            statement.setString(2,Title);
            statement.setString(3,Type);
            statement.setString(4,Location);
            statement.setString(5,Contact);
            statement.setTimestamp(6,possibleStart);
            statement.setTimestamp(7,possibleEnd);
            statement.setString(8,calendarApp.getUser());
            statement.setInt(9,appointment.getAppointmentId());
        } catch (IllegalArgumentException e){
            (new Alert(Alert.AlertType.ERROR,e.getLocalizedMessage(), ButtonType.OK)).showAndWait();
            e.printStackTrace();
        }
        return statement;
    }
    private PreparedStatement addStatement(Connection connection) throws SQLException{
        PreparedStatement statement = null;
        try {
            LocalDate localDate = datePicker.getValue();
            String name = customerName.getText();
            int startHours = (int) startHoursTimeSpinerr.getValue();
            int startMinutes = (int) startMinutesTimeSpinner.getValue();
            int endHours = (int) endHoursTimeSpinner.getValue();
            int endMinutes = (int) endMinutesTimeSpinner.getValue();
            String startAMPM = (String) this.startAMPM.getValue();
            String endAMPM = (String) this.endAMPM.getValue();
            if(startAMPM.equals("PM")){
                startHours += 12;
            }
            if(endAMPM.equals("PM")){
                endHours += 12;
            }

            int Year = localDate.getYear();
            int Month = localDate.getMonthValue();
            int Day = localDate.getDayOfMonth();
            String nameOfDay = localDate.getDayOfWeek().name();
            if(nameOfDay.equals("SATURDAY")||nameOfDay.equals("SUNDAY")||(startHours > 17 || startHours < 9)||(endHours > 17 || endHours < 9)){
                throw new IllegalArgumentException("Please choose a different time. Bussiness hours are Monday to Friday 9 AM to 5 PM");
            }

            ZonedDateTime startLocal = ZonedDateTime.of(Year,Month,Day,startHours,startMinutes,0,0, ZoneOffset.systemDefault());
            ZonedDateTime startUTC = startLocal.withZoneSameInstant(ZoneOffset.UTC);
            Timestamp possibleStart = Timestamp.valueOf(startUTC.toLocalDateTime());

            ZonedDateTime endLocal = ZonedDateTime.of(Year,Month,Day,endHours,endMinutes,0,0, ZoneOffset.systemDefault());
            ZonedDateTime endUTC = endLocal.withZoneSameInstant(ZoneOffset.UTC);
            Timestamp possibleEnd = Timestamp.valueOf(endUTC.toLocalDateTime());
            if(possibleStart.before(Timestamp.from(Instant.now()))){
                throw new IllegalArgumentException("Appointment is in the past. Please try again");
            }
            if(possibleStart.after(possibleEnd)){
                throw new IllegalArgumentException("Start appointment time is after end of appointment. Please try again");
            }

            if(!appointmentData.isEmpty()) {
                for(Object appt : appointmentData){
                    Appointment appointment = (Appointment) appt;
                    Timestamp start = appointment.getStart();
                    Timestamp end = appointment.getEnd();
                    if ((Timestamp.valueOf(startLocal.toLocalDateTime()).after(start)||Timestamp.valueOf(startLocal.toLocalDateTime()).equals(start)) && (Timestamp.valueOf(endLocal.toLocalDateTime()).before(end)||Timestamp.valueOf(endLocal.toLocalDateTime()).equals(end)) && calendarApp.getUser().equals(appointment.getUser())){
                        throw new IllegalArgumentException("Unable to process: Overlapping appointment "+System.lineSeparator()+"Between "+ start.toString()+" and " + end.toString());
                    }
                }
            }
            String Type = (String) appointmentType.getValue();
            String Title = title.getText();
            int custId = Integer.parseInt(customerId.getText());
            String Contact = customerPhone.getText();
            String Location = location.getText();
            statement = connection.prepareStatement("INSERT INTO appointment(customerId,title,description,location,contact,start,end,createDate,createdBy,lastUpdate,lastUpdateBy,url) " +
                    "VALUES(?,?,?,?,?,?,?,?,?,?,?,?)");
            statement.setInt(1,custId);
            statement.setString(2,Title);
            statement.setString(3,Type);
            statement.setString(4,Location);
            statement.setString(5,Contact);
            statement.setTimestamp(6,possibleStart);
            statement.setTimestamp(7,possibleEnd);
            statement.setTimestamp(8,Timestamp.from(Instant.now()));
            statement.setString(9,calendarApp.getUser());
            statement.setTimestamp(10,Timestamp.from(Instant.now()));
            statement.setString(11,calendarApp.getUser());
            statement.setString(12,"");
        } catch (IllegalArgumentException e){
            (new Alert(Alert.AlertType.ERROR,e.getLocalizedMessage(), ButtonType.OK)).showAndWait();
            e.printStackTrace();
        }
        return statement;
    }

}
