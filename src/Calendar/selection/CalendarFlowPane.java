package Calendar.selection;

import Calendar.Appointment;
import javafx.geometry.Pos;
import javafx.scene.layout.FlowPane;
import javafx.scene.text.Text;


public class CalendarFlowPane extends FlowPane implements SelectableNode {

    private String title;
    private String consultant;
    private String startTime;
    private Appointment appointment;

    public CalendarFlowPane(String title, String consultant, String startTime, Appointment appointment) {
        this.title = title;
        this.consultant = consultant;
        this.startTime = startTime;
        this.appointment = appointment;
        this.setPrefSize(118,40);

        FlowPane titleConsultant = new FlowPane();
        titleConsultant.setPrefSize(118,20);
        Text titlecons = new Text(" "+consultant+": "+title);

        titleConsultant.getChildren().add(titlecons);
        titleConsultant.setAlignment(Pos.CENTER_LEFT);

        FlowPane time = new FlowPane();
        time.setPrefSize(118,20);
        Text stTime = new Text(" "+startTime);
        time.getChildren().add(stTime);
        time.setAlignment(Pos.CENTER_LEFT);
        this.getChildren().addAll(titleConsultant,time);
        this.setStyle("-fx-border-color: grey; -fx-border-width: 1; -fx-border-insets:1;");
    }
    @Override
    public Appointment getAppointment() {
        return appointment;
    }

    @Override
    public boolean requestSelection(boolean select) {
        return true;
    }
    @Override
    public void notifySelection(boolean select) {
        if(select) {
            this.setStyle("-fx-background-color: #cccccc;-fx-border-width: 1; -fx-border-insets:1;-fx-text-fill: white;");
        }
        else
            this.setStyle("-fx-border-color: grey; -fx-border-width: 1; -fx-border-insets:1;");
    }

}
