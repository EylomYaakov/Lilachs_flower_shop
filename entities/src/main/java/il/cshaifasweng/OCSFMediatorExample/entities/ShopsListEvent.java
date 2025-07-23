package il.cshaifasweng.OCSFMediatorExample.entities;

import java.util.List;
import java.io.Serializable;

public class ShopsListEvent implements Serializable {
    private static List<String> shops;

    public ShopsListEvent(List<String> shops) {
        ShopsListEvent.shops = shops;
    }

    public List<String> getShops() {
        return shops;
    }

}
