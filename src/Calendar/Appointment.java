package Calendar;


import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;

import java.sql.Timestamp;

public class Appointment{
    private SimpleIntegerProperty customerId = new SimpleIntegerProperty();
    private SimpleIntegerProperty appointmentId = new SimpleIntegerProperty();
    private SimpleStringProperty title = new SimpleStringProperty();
    private SimpleStringProperty description = new SimpleStringProperty();
    private SimpleStringProperty contact = new SimpleStringProperty();
    private SimpleStringProperty location = new SimpleStringProperty();
    private SimpleObjectProperty<Timestamp> start = new SimpleObjectProperty<>();
    private SimpleObjectProperty<Timestamp> end = new SimpleObjectProperty<>();
    private SimpleStringProperty user = new SimpleStringProperty();
    private Boolean reminded = false;

    public String getUser() {
        return user.get();
    }

    public SimpleStringProperty userProperty() {
        return user;
    }

    public void setUser(String user) {
        this.user.set(user);
    }

    public Boolean getReminded() {
        return reminded;
    }

    public void setReminded(Boolean reminded) {
        this.reminded = reminded;
    }

    public String getLocation() {
        return location.get();
    }

    public SimpleStringProperty locationProperty() {
        return location;
    }

    public void setLocation(String location) {
        this.location.set(location);
    }

    public int getCustomerId() {
        return customerId.get();
    }

    public SimpleIntegerProperty customerIdProperty() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId.set(customerId);
    }

    public int getAppointmentId() {
        return appointmentId.get();
    }

    public SimpleIntegerProperty appointmentIdProperty() {
        return appointmentId;
    }

    public void setAppointmentId(int appointmentId) {
        this.appointmentId.set(appointmentId);
    }

    public String getTitle() {
        return title.get();
    }

    public SimpleStringProperty titleProperty() {
        return title;
    }

    public void setTitle(String title) {
        this.title.set(title);
    }

    public String getDescription() {
        return description.get();
    }

    public SimpleStringProperty descriptionProperty() {
        return description;
    }

    public void setDescription(String description) {
        this.description.set(description);
    }

    public String getContact() {
        return contact.get();
    }

    public SimpleStringProperty contactProperty() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact.set(contact);
    }

    public Timestamp getStart() {
        return start.get();
    }

    public SimpleObjectProperty<Timestamp> startProperty() {
        return start;
    }

    public void setStart(Timestamp start) {
        this.start.set(start);
    }

    public Timestamp getEnd() {
        return end.get();
    }

    public SimpleObjectProperty<Timestamp> endProperty() {
        return end;
    }

    public void setEnd(Timestamp end) {
        this.end.set(end);
    }


}
