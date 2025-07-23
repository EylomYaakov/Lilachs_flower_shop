package il.cshaifasweng.OCSFMediatorExample.entities;

import java.io.Serializable;
import java.util.List;

public class ComplaintsListEvent implements Serializable {
    private final List<Complaint> complaints;

    public ComplaintsListEvent(List<Complaint> complaints) {
        this.complaints = complaints;
    }

    public List<Complaint> getComplaints() {
        return complaints;
    }
}
