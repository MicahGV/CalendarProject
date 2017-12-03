package Calendar.selection;

import Calendar.Controllers.CalendarController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;

import java.util.ArrayList;
import java.util.List;

public class SelectionHandler {
    private EventHandler<MouseEvent> mousePressedEventEventHandler;
    private CalendarController calendarController;
    private Clipboard clipboard;
    public EventHandler<MouseEvent> getMousePressedEventEventHandler() {
        return mousePressedEventEventHandler;
    }

    public SelectionHandler(final Parent root, CalendarController calendarController) {
        this.clipboard = new Clipboard();
        this.calendarController = calendarController;
        this.mousePressedEventEventHandler = new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                SelectionHandler.this.doOnMousePressed(root,event);
                event.consume();
            }
        };
    }
    public void doOnMousePressed(Parent root, MouseEvent event){
        Node target = (Node) event.getTarget();
        while(!(target instanceof AnchorPane)){
            if(target instanceof SelectableNode){
                break;
            } else {
                 target = target.getParent();
            }
        }
        if(!(target instanceof SelectableNode)) {
            clipboard.unselectAll();
            calendarController.setCalendarAppointment(null);
        }
        if(target instanceof SelectableNode) {
            SelectableNode selectableTarget = (SelectableNode) target;
            if(!clipboard.getSelectedItems().contains(selectableTarget))
                clipboard.unselectAll();
            clipboard.select(selectableTarget, true);
        }

    }



    private class Clipboard {
        private ObservableList<SelectableNode> selectedItems = FXCollections.observableArrayList();

        public ObservableList<SelectableNode> getSelectedItems() {
            return selectedItems;
        }

        public boolean select(SelectableNode n, boolean selected) {
            if(n.requestSelection(selected)) {
                if (selected) {
                    selectedItems.add(n);
                    calendarController.setCalendarAppointment(n.getAppointment());
                } else {
                    selectedItems.remove(n);
                    calendarController.setCalendarAppointment(null);
                }
                n.notifySelection(selected);
                return true;
            } else {
                return false;
            }
        }

        public void unselectAll() {
            List<SelectableNode> unselectList = new ArrayList<>();
            unselectList.addAll(selectedItems);

            for (SelectableNode sN : unselectList) {
                select(sN, false);
            }
        }
    }
}
