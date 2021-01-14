package org.kodluyoruz;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class Restaurant {
    static final int RESTAURANT_TABLE_COUNT = 5;
    static final int RESTAURANT_WAITER_COUNT = 3;
    static final int RESTAURANT_COOK_COUNT = 2;

    static LinkedList<Integer> orderList = new LinkedList<>();
    private static HashMap<Integer, String> orderPlaced = new HashMap<>();

    static LinkedList<Integer> waiterOrderList = new LinkedList<>();
    private static HashMap<Integer, String> waiterOrderPlaced = new HashMap<>();
    private static HashMap<Waiter, String> waiterOrderClaim = new HashMap<>();
    static HashMap<Waiter, Integer> waiterOrderNumClaim = new HashMap<>();

    static HashMap<Cook, String> cookOrderClaim = new HashMap<>();
    static HashMap<Cook, Integer> cookOrderNumClaim = new HashMap<>();

    static List<Integer> cookCompletedOrders = new ArrayList<>();
    private static List<Integer> waiterCompletedOrders = new ArrayList<>();

    private Semaphore semaphoreCustomer = new Semaphore(RESTAURANT_TABLE_COUNT, false);
    private Semaphore semaphoreWaiter = new Semaphore(RESTAURANT_WAITER_COUNT, true);
    private Semaphore semaphoreCooker = new Semaphore(RESTAURANT_COOK_COUNT, true);

    private static final ExecutorService executorServiceCustomer = Executors.newFixedThreadPool(RESTAURANT_TABLE_COUNT);
    private static final ExecutorService executorServiceWaiter = Executors.newFixedThreadPool(RESTAURANT_WAITER_COUNT);
    private static final ExecutorService executorServiceCook = Executors.newFixedThreadPool(RESTAURANT_COOK_COUNT);
    static int customerCount;

    public Restaurant(int customerCount) {
        Restaurant.customerCount = customerCount;
    }

    void startSimulation() {
        for (int i = 0; i < customerCount; i++) {
            executorServiceCustomer.submit(new Customer("Customer " + (i + 1), i + 1, semaphoreCustomer));
        }
        for (int i = 0; i < RESTAURANT_WAITER_COUNT; i++) {
            executorServiceWaiter.submit(new Waiter("Waiter " + (i + 1), semaphoreWaiter));
        }
        for (int i = 0; i < RESTAURANT_COOK_COUNT; i++) {
            executorServiceCook.submit(new Cook("Cook " + (i + 1), semaphoreCooker));
        }
        executorServiceCustomer.shutdown();
        executorServiceWaiter.shutdown();
        executorServiceCook.shutdown();

        try {
            executorServiceCustomer.awaitTermination(1, TimeUnit.DAYS);
            executorServiceWaiter.awaitTermination(1, TimeUnit.DAYS);
            executorServiceCook.awaitTermination(1, TimeUnit.DAYS);
        } catch (InterruptedException exception) {
            exception.printStackTrace();
        }
        System.out.println("The Simulation is terminating.");
    }

    static int customerNumber;

    static boolean enterToTheRestaurant() {
        if (customerNumber < RESTAURANT_TABLE_COUNT) {
            customerNumber++;
            return true;
        } else
            return false;
    }

    static void customerLeaveRestaurant() {
        customerNumber--;
    }

    static void customerPlaceOrder(int orderNum, String food) {
        System.out.println("Customer order " + orderNum + " and " + food + " to order");
        orderList.add(orderNum);
        orderPlaced.put(orderNum, food);
    }

    static boolean orderAvailable(Waiter waiter) {
        if (!orderPlaced.isEmpty()) {
            try {
                int orderNum = orderList.pop();
                waiterOrderList.add(orderNum);
                waiterOrderPlaced.put(orderNum, orderPlaced.get(orderNum));
                waiterOrderClaim.put(waiter, orderPlaced.get(orderNum));
                waiterOrderNumClaim.put(waiter, orderNum);
                return true;
            } catch (RuntimeException exception) {
                System.out.println("Order is not available. We are sorry.");
                return false;
            }

        } else return false;
    }

    static boolean cookOrderAvailable(Cook cook) {
        if (!waiterOrderPlaced.isEmpty()) {
            try {
                int waiterOrderNum = waiterOrderList.pop();
                cookOrderClaim.put(cook, waiterOrderPlaced.remove(waiterOrderNum));
                cookOrderNumClaim.put(cook, waiterOrderNum);
                return true;
            } catch (RuntimeException exception) {
                System.out.println("Order is not available. We are sorry.");
                return false;
            }
        } else return false;
    }

    static int waiterGetOrderNum(Waiter waiter) {
        return waiterOrderNumClaim.remove(waiter);
    }

    static String waiterGetOrder(Waiter waiter) {
        return waiterOrderClaim.remove(waiter);
    }

    static int cookGetOrderNum(Cook cook) {
        return cookOrderNumClaim.remove(cook);
    }

    static String cookGetOrder(Cook cook) {
        return cookOrderClaim.remove(cook);
    }

    static void cookOrderCompleted(Cook cook, int orderNum, String food) {
        cookCompletedOrders.add(orderNum);
        System.out.println(cook.toString() + " completed the " + food + " order number " + orderNum);
    }

    static boolean checkWaiterOrderStatus(int orderNum) {
        return cookCompletedOrders.contains(orderNum);
    }

    static void waiterOrderCompleted(Waiter waiter, int orderNum) {
        waiterCompletedOrders.add(orderNum);
        System.out.println(waiter.toString() + " received the completed order number " + orderNum + " from the cook.");
    }

    static boolean checkOrderCompleted(int orderNum) {
        return waiterCompletedOrders.contains(orderNum);
    }
}
