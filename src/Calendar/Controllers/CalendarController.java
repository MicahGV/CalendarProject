package Calendar.Controllers;

import Calendar.Appointment;
import Calendar.Customer;
import Calendar.MysqlConnection;
import Calendar.selection.CalendarFlowPane;
import Calendar.CalendarApp;
import Calendar.selection.SelectionHandler;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;

import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;


public class CalendarController implements Initializable {

    @FXML private TableView customerTable;
    @FXML private TableColumn customerActiveColumn;
    @FXML private TableColumn customerNameColumn;
    @FXML private TableColumn customerIdColumn;
    @FXML private TableColumn customerAddressColumn;
    @FXML private TableColumn customerAddressTwoColumn;
    @FXML private TableColumn customerCityColumn;
    @FXML private TableColumn customerCountryColumn;
    @FXML private TableColumn customerPhoneColumn;
    @FXML private TableColumn customerZipcodeColumn;

    @FXML private TableView appointmentTable;
    @FXML private TableColumn apptCustomerIdColumn;
    @FXML private TableColumn apptCustomerNameColumn;
    @FXML private TableColumn appointmentTitleColumn;
    @FXML private TableColumn appointmentIdColumn;
    @FXML private TableColumn appointmentDescColumn;
    @FXML private TableColumn appointmentLocationColumn;
    @FXML private TableColumn appointmentContactColumn;
    @FXML private TableColumn appointmentStartColumn;
    @FXML private TableColumn appointmentEndColumn;
    @FXML private TableColumn appointmentConsultantColumn;

    @FXML private TableView ScheduleTable;
    @FXML private TableColumn userColumn;
    @FXML private TableColumn userTitleColumn;
    @FXML private TableColumn userTypeColumn;
    @FXML private TableColumn userStartColumn;
    @FXML private TableColumn userEndColumn;
    @FXML private ComboBox userSearch;

    @FXML private StackedBarChart byMonthChart;
    @FXML private CategoryAxis monthXAxis;
    @FXML private NumberAxis YnumberAxis;

    @FXML private PieChart activeUserChart;

    @FXML private GridPane calendarGrid;
    @FXML private Text monthYear;

    private MysqlConnection mysqlConnection;
    private boolean showWeek = false;
    private int rowNum = 0;
    private int numOfMonthAdjustment = 0;
    private CalendarApp calendarApp;
    private ObservableList<Customer> customerData = FXCollections.observableArrayList();
    private ObservableList<Appointment> appointmentData = FXCollections.observableArrayList();
    private Set<String> userSet = new HashSet<String>();
    private ObservableList<String> userList = FXCollections.observableArrayList();
    private Appointment calendarAppointment;


    public CalendarController(CalendarApp calendarApp, MysqlConnection mysqlConnection, ObservableList customerData, ObservableList appointmentObservableList) {
        this.calendarApp = calendarApp;
        this.mysqlConnection = mysqlConnection;
        this.customerData = customerData;
        this.appointmentData = appointmentObservableList;
    }

    public void setCalendarAppointment(Appointment calendarAppointment) {
        this.calendarAppointment = calendarAppointment;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        calendarApp.reminder();
        //Calendar
        String currentMonth = LocalDateTime.now().getMonth().toString();
        String currentYear = String.valueOf(LocalDateTime.now().getYear());
        monthYear.setText(currentMonth+" "+currentYear);
        updateGrid(LocalDateTime.now());

        //CustomerTable
        customerActiveColumn.setCellValueFactory(new PropertyValueFactory<Customer,Boolean>("active"));
        customerNameColumn.setCellValueFactory(new PropertyValueFactory<Customer,Integer>("name"));
        customerIdColumn.setCellValueFactory(new PropertyValueFactory<Customer,String>("customerId"));
        customerAddressColumn.setCellValueFactory(new PropertyValueFactory<Customer,String>("address"));
        customerAddressTwoColumn.setCellValueFactory(new PropertyValueFactory<Customer,String>("addressTwo"));
        customerCityColumn.setCellValueFactory(new PropertyValueFactory<Customer,String>("city"));
        customerCountryColumn.setCellValueFactory(new PropertyValueFactory<Customer,String>("country"));
        customerPhoneColumn.setCellValueFactory(new PropertyValueFactory<Customer,String>("phone"));
        customerZipcodeColumn.setCellValueFactory(new PropertyValueFactory<Customer,String>("zipcode"));
        customerTable.setItems(customerData);

        //AppointmentTable
        appointmentConsultantColumn.setCellValueFactory(new PropertyValueFactory<Appointment,String>("user"));
        apptCustomerIdColumn.setCellValueFactory(new PropertyValueFactory<Appointment,Integer>("customerId"));
        appointmentTitleColumn.setCellValueFactory(new PropertyValueFactory<Appointment,String>("title"));
        appointmentIdColumn.setCellValueFactory(new PropertyValueFactory<Appointment,Integer>("appointmentId"));
        appointmentDescColumn.setCellValueFactory(new PropertyValueFactory<Appointment,String>("description"));
        appointmentLocationColumn.setCellValueFactory(new PropertyValueFactory<Appointment,String>("location"));
        appointmentContactColumn.setCellValueFactory(new PropertyValueFactory<Appointment,String>("contact"));
        appointmentStartColumn.setCellValueFactory(new PropertyValueFactory<Appointment,Timestamp>("start"));
        appointmentEndColumn.setCellValueFactory(new PropertyValueFactory<Appointment,Timestamp>("end"));
        appointmentTable.setItems(appointmentData);

        //Consultant schedule table
        userColumn.setCellValueFactory(new PropertyValueFactory<Appointment,String>("user"));
        userTitleColumn.setCellValueFactory(new PropertyValueFactory<Appointment,String>("title"));
        userTypeColumn.setCellValueFactory(new PropertyValueFactory<Appointment,String>("description"));
        userStartColumn.setCellValueFactory(new PropertyValueFactory<Appointment,Timestamp>("start"));
        userEndColumn.setCellValueFactory(new PropertyValueFactory<Appointment,Timestamp>("end"));
        resetConsultant();
        //Stacked month chart
        monthXAxis.getCategories().addAll("Blowing stuff up","Consultation Meeting","Leisure","Strategy Meeting");
        calculateMonthData();
        //Area active chart
        calculateActiveUserData();
    }

    private void resetConsultant(){
        userList.clear();
        for(Appointment appointment: appointmentData){
            userSet.add(appointment.getUser());
        }
        for(String user: userSet){
            userList.add(user);
        }
        userSearch.setItems(userList);
    }

    public void searchConsultantdata() {
        if(!userSearch.getValue().toString().isEmpty()) {
            ObservableList<Appointment> result = appointmentData.stream()
                    .filter(appointment -> appointment.getUser().equals(userSearch.getValue().toString()))
                    .collect(Collectors.toCollection(FXCollections::observableArrayList));
            ScheduleTable.setItems(result);
        }
    }

    private void calculateActiveUserData(){
        activeUserChart.getData().clear();
        int[] activeList = new int[]{0,0};
        for(Customer customer: customerData){
            if(customer.isActive()){
                activeList[1] += 1;
            } else {
                activeList[0] += 1;
            }
        }
        PieChart.Data active = new PieChart.Data("Active",activeList[1]);
        PieChart.Data inactive = new PieChart.Data("Inactive",activeList[0]);
        activeUserChart.getData().addAll(active,inactive);
        activeUserChart.getData().forEach(data ->
                data.nameProperty().bind(
                        Bindings.concat(data.getName()," ",data.getPieValue())
                ));
    }

    private void  calculateMonthData() {
        //Setting up structures
        byMonthChart.getData().clear();
        String[] Months = "January February March April May June July August September October November December".toUpperCase().split(" ");
        HashMap<String,Integer> blowingUpStuff = new HashMap<String, Integer>();
        HashMap<String,Integer> consultation = new HashMap<String, Integer>();
        HashMap<String,Integer> leisure = new HashMap<String, Integer>();
        HashMap<String,Integer> strategy = new HashMap<String, Integer>();
        for(int i = 0; i < Months.length; i++){
            blowingUpStuff.put(Months[i],0);
            consultation.put(Months[i],0);
            leisure.put(Months[i],0);
            strategy.put(Months[i],0);
        }
        for(Appointment appointment: appointmentData){
            String category = appointment.getDescription();
            LocalDateTime ldt = appointment.getStart().toLocalDateTime();
            String month = ldt.getMonth().toString();
            if(category.equals("Blowing stuff up")){
                blowingUpStuff.put(month,blowingUpStuff.get(month)+1);
            } else if(category.equals("Consultation Meeting")){
                consultation.put(month,consultation.get(month)+1);
            } else if(category.equals("Leisure")){
                leisure.put(month,leisure.get(month)+1);
            } else if(category.equals("Strategy Meeting")){
                strategy.put(month,strategy.get(month)+1);
            }
        }
        XYChart.Series bus = new XYChart.Series();
        bus.setName("Blowing Stuff Up");
        XYChart.Series cons = new XYChart.Series();
        cons.setName("Consultation Meeting");
        XYChart.Series lei = new XYChart.Series();
        lei.setName("Leisure");
        XYChart.Series str = new XYChart.Series();
        str.setName("Strategy Meeting");
        for(int i = 0; i < Months.length;i++){
            bus.getData().addAll(new XYChart.Data(Months[i],blowingUpStuff.get(Months[i])));
            cons.getData().addAll(new XYChart.Data(Months[i],consultation.get(Months[i])));
            lei.getData().addAll(new XYChart.Data(Months[i],leisure.get(Months[i])));
            str.getData().addAll(new XYChart.Data(Months[i],strategy.get(Months[i])));
        }
        byMonthChart.getData().addAll(bus, cons, lei, str);
    }


    private void updateCalendar(){
        LocalDateTime localDateTime = null;
        localDateTime = LocalDateTime.now().plusMonths(numOfMonthAdjustment);
        String currentMonth = localDateTime.getMonth().toString();
        String currentYear = String.valueOf(localDateTime.getYear());
        monthYear.setText(currentMonth+" "+currentYear);
        calendarGrid.getChildren().clear();
        updateGrid(localDateTime);
    }

    private void updateGrid(LocalDateTime localDateTime){
        SelectionHandler selectionHandler = new SelectionHandler(calendarGrid,this);
        calendarGrid.addEventHandler(MouseEvent.MOUSE_CLICKED,selectionHandler.getMousePressedEventEventHandler());
        //Adding current month
        LocalDateTime firstOfMonth = localDateTime.with(TemporalAdjusters.firstDayOfMonth());
        int day = firstOfMonth.getDayOfWeek().getValue();
        firstOfMonth = firstOfMonth.minusDays(day);
        for(int i = 0; i < 6; i++){
            for(int j = 0; j < 7; j++){
                ScrollPane scrollPane = new ScrollPane();
                FlowPane flowPane = new FlowPane();
                AnchorPane anchorPane = new AnchorPane();
                flowPane.setPrefSize(118,20);
                AnchorPane.setTopAnchor(flowPane,0.0);
                AnchorPane.setLeftAnchor(flowPane,0.0);
                AnchorPane.setRightAnchor(flowPane,0.0);
                Text text = new Text("  "+String.valueOf(firstOfMonth.getDayOfMonth()));
                flowPane.getChildren().add(text);
                anchorPane.getChildren().add(flowPane);
                FlowPane appointmentPane = buildAppointmentPane(firstOfMonth, firstOfMonth.getDayOfMonth());
                anchorPane.getChildren().add(appointmentPane);
                scrollPane.setContent(anchorPane);
                if(localDateTime.getMonthValue() != firstOfMonth.getMonthValue()){
                    scrollPane.setStyle("-fx-border-color: #d6d6d6; -fx-border-width: 1; -fx-border-insets:1;");
                } else {
                    scrollPane.setStyle("-fx-border-color: grey; -fx-border-width: 1; -fx-border-insets:1;");
                }
                if(showWeek){
                    if(i != rowNum) scrollPane.setVisible(false);
                    else scrollPane.setVisible(true);
                } else scrollPane.setVisible(true);
                firstOfMonth = firstOfMonth.plusDays(1);
                calendarGrid.add(scrollPane,j,i);
            }
        }
    }

    private FlowPane buildAppointmentPane(LocalDateTime ldt, int dayStart){
        FlowPane appointmentPane = new FlowPane();
        appointmentPane.setPrefWidth(118);
        appointmentPane.setMaxWidth(118);
        appointmentPane.setAlignment(Pos.CENTER_LEFT);
        AnchorPane.setTopAnchor(appointmentPane,20.0);
        for(Appointment appointment: appointmentData){
            LocalDateTime start = appointment.getStart().toLocalDateTime();
            LocalDateTime end = appointment.getEnd().toLocalDateTime();

            LocalDate startdate = appointment.getStart().toLocalDateTime().toLocalDate();;
            LocalDate ld = ldt.toLocalDate();

            String endMinutes = end.getMinute() < 10 ? end.getMinute()+"0": end.getMinute()+"";
            String startMinute = start.getMinute() < 10 ? start.getMinute()+"0" : start.getMinute()+"";
            String time = start.getHour() +":"+startMinute+"-"+end.getHour()+":"+endMinutes;
            if(ld.isEqual(startdate)){
                CalendarFlowPane calendarFlowPane = new CalendarFlowPane(appointment.getTitle(),appointment.getUser(),time,appointment);
                appointmentPane.getChildren().add(calendarFlowPane);
            }
        }
        return appointmentPane;
    }

    public void CalendarWeekView(ActionEvent event){
        showWeek = !showWeek;
        updateCalendar();
    }
    private void rowNum(int oneOrNegOne){
        rowNum += oneOrNegOne;
        if(rowNum > 5){
            rowNum = 0;
            numOfMonthAdjustment++;
        } else if(rowNum < 0){
            rowNum = 5;
            numOfMonthAdjustment--;
        }
    }

    public void backwardOneMonth(MouseEvent mouseEvent) {
        if(showWeek) {
            rowNum(-1);
        }
        else {
            numOfMonthAdjustment--;
            rowNum = 0;
        }
        updateCalendar();
    }

    public void forwardOneMonth(MouseEvent mouseEvent) {
        if(showWeek) {
            rowNum(1);
        }
        else {
            numOfMonthAdjustment++;
            rowNum = 0;
        }
        updateCalendar();
    }

    public void addAppointment(ActionEvent event){
        calendarApp.showAppointmentInfo(false,new Appointment());
        appointmentData.clear();
        appointmentData = calendarApp.getAppointmentData();
        calculateMonthData();
        updateCalendar();
        resetConsultant();
    }
    public void editAppointment(ActionEvent event){
        Appointment appointment = null;
        if(calendarAppointment!=null)
            appointment = calendarAppointment;
        else
            appointment = (Appointment) appointmentTable.getSelectionModel().getSelectedItem();
        calendarApp.showAppointmentInfo(true,appointment);
        appointmentData.clear();
        appointmentData = calendarApp.getAppointmentData();
        calculateMonthData();
        updateCalendar();
        resetConsultant();
    }

    public void removeAppointment(ActionEvent event){
        Appointment appointment = null;
        if(calendarAppointment!=null)
            appointment = calendarAppointment;
        else
            appointment = (Appointment) appointmentTable.getSelectionModel().getSelectedItem();
        calendarApp.deleteAppointment(appointment);
        appointmentData.clear();
        appointmentData = calendarApp.getAppointmentData();
        calculateMonthData();
        updateCalendar();
        resetConsultant();
    }

    public void editCustomer(ActionEvent event) {
        Customer customer = (Customer) customerTable.getSelectionModel().getSelectedItem();
        calendarApp.showCustomerInfo(true, customer);
        customerData.clear();
        customerData = calendarApp.getCustomerData();
        calculateActiveUserData();
    }

    public void addCustomer(ActionEvent event) {
        calendarApp.showCustomerInfo(false, new Customer());
        customerData.clear();
        customerData = calendarApp.getCustomerData();
        calculateActiveUserData();
    }
}
