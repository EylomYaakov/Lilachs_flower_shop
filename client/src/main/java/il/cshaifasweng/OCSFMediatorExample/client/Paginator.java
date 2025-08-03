package il.cshaifasweng.OCSFMediatorExample.client;


import il.cshaifasweng.OCSFMediatorExample.entities.Product;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.scene.control.*;

public class Paginator<T>{
    private final List<T> items;
    private final int pageSize;
    private int currentIndex = 0;
    private boolean[] productsToShow;
    private int currentPageSize;

    public Paginator(List<T> items, int pageSize) {
        this.items = items;
        this.pageSize = pageSize;
        productsToShow = new boolean[items.size()];
        Arrays.fill(productsToShow, true);

    }


    private int pageStartIndex(){
        int itemCount = 0;
        for(int i=currentIndex-1; i>=0; i--){
            if(productsToShow[i]){
                itemCount++;
            }
            if(itemCount == pageSize){
                return i;
            }
        }
        return 0;
    }

    public T getItem(int i){
        int count = 0;
        for(int j =pageStartIndex(); j<items.size(); j++){
            if(productsToShow[j]){
                count++;
            }
            if(count == i+1){
                return items.get(j);
            }
        }
        return null;
    }

    public void addItem(T item) {
        items.add(item);
        productsToShow = new boolean[items.size()];
        Arrays.fill(productsToShow, true);
    }

    public void removeItem(T item) {
        productsToShow = new boolean[items.size()];
        Arrays.fill(productsToShow, true);
    }

    public int productsLeftToShow(){
        int count = 0;
        for(int i=currentIndex; i<productsToShow.length; i++){
            if(productsToShow[i]){
                count++;
            }
        }
        return count;
    }
    public List<T> getCurrentPageItems(){
        int size = Math.min(productsLeftToShow(), pageSize);
        List<T> page = new ArrayList<T>();
        int i = 0;
        while(i < size){
            if(productsToShow[currentIndex]){
                page.add(items.get(currentIndex));
                i++;
            }
            currentIndex++;
        }
        currentPageSize = page.size();
        return page;
    }

    public boolean hasNextPage() {
        return productsLeftToShow() > 0;
    }


    public boolean hasPreviousPage(){
        int count = 0;
        for(int i=currentIndex-1; i>=0; i--){
            if(productsToShow[i]){
                count++;
            }
            if(count == currentPageSize+1){
                return true;
            }
        }
        return false;
    }


    public void changeShowProducts(int index, boolean value){
        productsToShow[index] = value;
    }

    public boolean getShowProducts(int index){
        return productsToShow[index];
    }


    public void prevPage(){
        int itemCount = 0;
        for(int i=currentIndex-1; i>=0; i--){
            if(productsToShow[i]){
                itemCount++;
            }
            if(itemCount == pageSize + currentPageSize){
                currentIndex = i;
                return;
            }
        }
    }

    public int getCurrentPageSize() {
        return currentPageSize;
    }

    public int getCurrentIndex() {
        return currentIndex;
    }

    public void setCurrentIndex(int currentIndex) {
        this.currentIndex = currentIndex;
    }

    public void reset(){
        currentIndex = 0;
    }

    public void clearItems(){
        items.clear();
        currentIndex = 0;
        productsToShow = new boolean[0];
    }

}
