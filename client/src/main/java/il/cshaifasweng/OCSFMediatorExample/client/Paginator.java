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
    private List<Boolean> productsToShow;
    private int currentPageSize;

    public Paginator(List<T> items, int pageSize) {
        this.items = items;
        this.pageSize = pageSize;
        productsToShow = Utils.initList(items.size(), true);
    }


    private int pageStartIndex(){
        int itemCount = 0;
        for(int i=currentIndex-1; i>=0; i--){
            if(productsToShow.get(i)){
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
            if(productsToShow.get(j)){
                count++;
            }
            if(count == i+1){
                return items.get(j);
            }
        }
        return null;
    }


    public void printItemsSize(){
        System.out.println("items size: " + items.size());
    }

    public void addItem(T item) {
        items.add(item);
        updateShowProducts(true, 0, true);
    }

    public void updateShowProducts(boolean add, int index, boolean value){
        if(add){
            productsToShow.add(value);
        }
        else{
            productsToShow.remove(index);
        }
    }

    public void removeItem(T item) {
        int index = items.indexOf(item);
        items.remove(item);
        updateShowProducts(false, index, false);
    }

    public int productsLeftToShow(){
        int count = 0;
        for(int i=currentIndex; i<productsToShow.size(); i++){
            if(productsToShow.get(i)){
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
            if(productsToShow.get(currentIndex)){
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
            if(productsToShow.get(i)){
                count++;
            }
            if(count == currentPageSize+1){
                return true;
            }
        }
        return false;
    }


    public void changeShowProducts(int index, boolean value){
        productsToShow.set(index,value);
    }

    public boolean getShowProducts(int index){
        return productsToShow.get(index);
    }


    public void prevPage(){
        int itemCount = 0;
        for(int i=currentIndex-1; i>=0; i--){
            if(productsToShow.get(i)){
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
        productsToShow = new ArrayList<>();
    }

}
