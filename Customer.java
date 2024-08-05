import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;

public class Customer implements Runnable {
    private Bakery bakery;
    private Random rnd;
    private List<BreadType> shoppingCart;
    private int shopTime;
    private int checkoutTime;
    private CountDownLatch doneSignal;


    /**
     * Initialize a customer object and randomize its shopping cart
     */
    public Customer(Bakery bakery, CountDownLatch l) {
        // TODO
        this.bakery = bakery;
        this.rnd = new Random();
        this.shoppingCart = new ArrayList<BreadType>();
        this.doneSignal = l;
        this.shopTime = rnd.nextInt(100);
        this.checkoutTime = rnd.nextInt(100);
        fillShoppingCart();
    }

    /**
     * Run tasks for the customer
     */
    public void run() {
        // TODO
        try{
            bakery.cust.acquire();
            Map<String, Integer> breadHashMap = new ConcurrentHashMap<>();
            breadHashMap.put("RYE",0);
            breadHashMap.put("WONDER",1);
            breadHashMap.put("SOURDOUGH",2);
            System.out.println(toString());
            simulateDelay(shopTime);
            int shopSize =this.shoppingCart.size();
            for (int i = 0; i<shopSize;i++){
                Semaphore sem = this.bakery.breadType[breadHashMap.get(shoppingCart.get(i).toString())];
                sem.acquire();
                this.bakery.takeBread(this.shoppingCart.get(i));
                sem.release();
            }
        } catch (InterruptedException e){
            e.printStackTrace();
        }
        try{
            simulateDelay(checkoutTime);
            bakery.registers.acquire();
            this.bakery.addSales(this.getItemsValue());
            bakery.registers.release();
        }catch (InterruptedException e){
            e.printStackTrace();
        }
        bakery.cust.release();
        doneSignal.countDown();
    }
    private void simulateDelay(int time) throws InterruptedException {
        // Simulate a delay without blocking the thread
        Thread.sleep(time);
    }
    /**
     * Return a string representation of the customer
     */
    public String toString() {
        return "Customer " + hashCode() + ": shoppingCart=" + Arrays.toString(shoppingCart.toArray()) + ", shopTime=" + shopTime + ", checkoutTime=" + checkoutTime;
    }

    /**
     * Add a bread item to the customer's shopping cart
     */
    private boolean addItem(BreadType bread) {
        // do not allow more than 3 items, chooseItems() does not call more than 3 times
        if (shoppingCart.size() >= 3) {
            return false;
        }
        shoppingCart.add(bread);
        return true;
    }

    /**
     * Fill the customer's shopping cart with 1 to 3 random breads
     */
    private void fillShoppingCart() {
        int itemCnt = 1 + rnd.nextInt(3);
        while (itemCnt > 0) {
            addItem(BreadType.values()[rnd.nextInt(BreadType.values().length)]);
            itemCnt--;
        }
    }

    /**
     * Calculate the total value of the items in the customer's shopping cart
     */
    private float getItemsValue() {
        float value = 0;
        for (BreadType bread : shoppingCart) {
            value += bread.getPrice();
        }
        return value;
    }
}
