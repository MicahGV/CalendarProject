package Calendar;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class Customer {
    private SimpleBooleanProperty active = new SimpleBooleanProperty();
    private SimpleIntegerProperty customerId = new SimpleIntegerProperty();
    private SimpleIntegerProperty addressId = new SimpleIntegerProperty();
    private SimpleIntegerProperty cityId = new SimpleIntegerProperty();
    private SimpleIntegerProperty countryId = new SimpleIntegerProperty();
    private SimpleStringProperty name = new SimpleStringProperty();
    private SimpleStringProperty address = new SimpleStringProperty();
    private SimpleStringProperty addressTwo = new SimpleStringProperty();
    private SimpleStringProperty city = new SimpleStringProperty();
    private SimpleStringProperty country = new SimpleStringProperty();
    private SimpleStringProperty phone = new SimpleStringProperty();
    private SimpleStringProperty zipcode = new SimpleStringProperty();


    public boolean isActive() {
        return active.get();
    }

    public SimpleBooleanProperty activeProperty() {
        return active;
    }

    public void setActive(boolean active) {
        this.active.set(active);
    }

    public int getAddressId() {
        return addressId.get();
    }

    public SimpleIntegerProperty addressIdProperty() {
        return addressId;
    }

    public void setAddressId(int addressId) {
        this.addressId.set(addressId);
    }

    public int getCityId() {
        return cityId.get();
    }

    public SimpleIntegerProperty cityIdProperty() {
        return cityId;
    }

    public void setCityId(int cityId) {
        this.cityId.set(cityId);
    }

    public int getCountryId() {
        return countryId.get();
    }

    public SimpleIntegerProperty countryIdProperty() {
        return countryId;
    }

    public void setCountryId(int countryId) {
        this.countryId.set(countryId);
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

    public String getName() {
        return name.get();
    }

    public SimpleStringProperty nameProperty() {
        return name;
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public String getAddress() {
        return address.get();
    }

    public SimpleStringProperty addressProperty() {
        return address;
    }

    public void setAddress(String address) {
        this.address.set(address);
    }

    public String getAddressTwo() {
        return addressTwo.get();
    }

    public SimpleStringProperty addressTwoProperty() {
        return addressTwo;
    }

    public void setAddressTwo(String addressTwo) {
        this.addressTwo.set(addressTwo);
    }

    public String getCity() {
        return city.get();
    }

    public SimpleStringProperty cityProperty() {
        return city;
    }

    public void setCity(String city) {
        this.city.set(city);
    }

    public String getCountry() {
        return country.get();
    }

    public SimpleStringProperty countryProperty() {
        return country;
    }

    public void setCountry(String country) {
        this.country.set(country);
    }

    public String getPhone() {
        return phone.get();
    }

    public SimpleStringProperty phoneProperty() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone.set(phone);
    }

    public String getZipcode() {
        return zipcode.get();
    }

    public SimpleStringProperty zipcodeProperty() {
        return zipcode;
    }

    public void setZipcode(String zipcode) {
        this.zipcode.set(zipcode);
    }
}
