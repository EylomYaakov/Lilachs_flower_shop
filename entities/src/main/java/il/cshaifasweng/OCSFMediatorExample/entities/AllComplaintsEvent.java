package il.cshaifasweng.OCSFMediatorExample.entities;

import java.io.Serializable;
import java.util.List;

public class AllComplaintsEvent  implements Serializable{
    private List<Complaint> complaints;

    public AllComplaintsEvent(List<Complaint> complaints) {
        this.complaints = complaints;
    }

    public List<Complaint> getComplaints() {
        return complaints;
    }
}
