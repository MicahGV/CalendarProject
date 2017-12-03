package Calendar.selection;

import Calendar.Appointment;

public interface SelectableNode {
   boolean requestSelection(boolean select);
   void notifySelection(boolean select);
   Appointment getAppointment();
}
