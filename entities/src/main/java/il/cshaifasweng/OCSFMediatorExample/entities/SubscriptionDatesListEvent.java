package il.cshaifasweng.OCSFMediatorExample.entities;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

public class SubscriptionDatesListEvent implements Serializable {
    List<LocalDateTime> SubscriptionDates;

    SubscriptionDatesListEvent(List<LocalDateTime> subscriptionDates) {
        SubscriptionDates = subscriptionDates;
    }

    public List<LocalDateTime> getSubscriptionDates() {
        return SubscriptionDates;
    }

}
