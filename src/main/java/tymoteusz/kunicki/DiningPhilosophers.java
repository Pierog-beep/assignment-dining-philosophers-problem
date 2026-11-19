
package tymoteusz.kunicki;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class Fork {
    private final int id;
    private final Lock lock;

    public Fork(int id, boolean fair) {
        this.id = id;
        this.lock = new ReentrantLock(fair);
    }

    public void pickUp() throws InterruptedException {
        lock.lockInterruptibly();
    }

    public void putDown() {
        lock.unlock();
    }

    public int getId() {
        return id;
    }
}

class Waiter {
    private int currentEating;
    private final int maxAllowed;
    private final Queue<Thread> waitingQueue;

    public Waiter(int maxAllowed) {
        this.currentEating = 0;
        this.maxAllowed = maxAllowed;
        this.waitingQueue = new LinkedList<>();
    }

    public synchronized void askForPermission()
            throws InterruptedException {

        Thread currentThread = Thread.currentThread();
        waitingQueue.add(currentThread);

        try {
            while (currentEating >= maxAllowed
                    || waitingQueue.peek() != currentThread) {
                wait();
            }

            waitingQueue.poll();
            currentEating++;

        } catch (InterruptedException e) {
            waitingQueue.remove(currentThread);
            notifyAll();
            throw e;
        }
    }

    public synchronized void releaseForks() {
        currentEating--;
        notifyAll();
    }
}

class Philosopher extends Thread {

    private final int id;
    private final Fork leftFork;
    private final Fork rightFork;

    private final Fork firstFork;
    private final Fork secondFork;

    private final Waiter waiter;

    private final boolean randomOrder;

    private volatile boolean running = true;

    private long totalNumberOfMealsEaten = 0;

    public Philosopher(
            int id,
            Fork leftFork,
            Fork rightFork,
            Waiter waiter,
            boolean randomOrder,
            boolean ordered
    ) {
        this.id = id;
        this.leftFork = leftFork;
        this.rightFork = rightFork;
        this.waiter = waiter;
        this.randomOrder = randomOrder;

        if (ordered) {
            if (leftFork.getId() < rightFork.getId()) {
                this.firstFork = leftFork;
                this.secondFork = rightFork;
            } else {
                this.firstFork = rightFork;
                this.secondFork = leftFork;
            }
        } else {
            this.firstFork = null;
            this.secondFork = null;
        }
    }

    @Override
    public void run() {

        Random random = new Random();

        try {
            while (running) {

                think();

                boolean permissionGranted = false;

                if (waiter != null) {
                    waiter.askForPermission();
                    permissionGranted = true;
                }

                try {

                    Fork first;
                    Fork second;

                    if (randomOrder) {

                        if (random.nextBoolean()) {
                            first = leftFork;
                            second = rightFork;
                        } else {
                            first = rightFork;
                            second = leftFork;
                        }

                    } else {
                        first = firstFork;
                        second = secondFork;
                    }

                    first.pickUp();

                    try {
                        Thread.sleep(500);

                        second.pickUp();

                        try {
                            eat();
                        } finally {
                            second.putDown();
                        }

                    } finally {
                        first.putDown();
                    }

                } finally {
                    if (permissionGranted) {
                        waiter.releaseForks();
                    }
                }
            }

        } catch (InterruptedException e) {
            System.out.println("Filozof " + id + " został przerwany.");
            Thread.currentThread().interrupt();
        }
    }

    private void think() throws InterruptedException {
        System.out.println("Filozof " + id + " myśli.");
        Thread.sleep((long) (Math.random() * 50));
    }

    private void eat() throws InterruptedException {

        long eatDuration = (long) (Math.random() * 50);

        System.out.println("Filozof " + id + " je (widelce: " + leftFork.getId() + " i " + rightFork.getId() + ").");

        try {
            Thread.sleep(eatDuration);
        } finally {
            totalNumberOfMealsEaten += 1;
        }
    }

    public void stopPhilosopher() {
        running = false;
        this.interrupt();
    }

    public long getTotalNumberOfMealsEaten() {
        return totalNumberOfMealsEaten;
    }

    public int getPhilosopherId() {
        return id;
    }
}

public class DiningPhilosophers {

    static void main(String[] args)
            throws InterruptedException {

        Scanner scanner = new Scanner(System.in);

        System.out.println();
        System.out.println("Wybierz wersję programu:");
        System.out.println();

        System.out.println("1. Wersja z kelnerem (Waiter)");
        System.out.println("2. Wersja z losową kolejnością widelców");
        System.out.println("3. Wersja z użyciem Hierarchii");
        System.out.println();
        System.out.println("0. Zakończ program");


        int choice = scanner.nextInt();

        if (choice == 0) {
            System.out.println("Zakończono program.");
            scanner.close();
            return;
        }

        if (choice < 1 || choice > 3) {
            System.out.println("Nieprawidłowy wybór!");
            scanner.close();
            return;
        }

        System.out.print("Podaj liczbę filozofów: ");
        int numberOfPhilosophers = scanner.nextInt();

        if (numberOfPhilosophers < 2) {
            System.out.println("Do uruchomienia symulacji potrzeba co najmniej 2 filozofów.");
            scanner.close();
            return;
        }

        System.out.print("Podaj czas działania programu (w sekundach): ");

        double runTimeSeconds = scanner.nextDouble();

        if (!Double.isFinite(runTimeSeconds)
                || runTimeSeconds <= 0
                || runTimeSeconds > Long.MAX_VALUE / 1000.0) {

            System.out.println("Nieprawidłowy czas działania!");
            scanner.close();
            return;
        }

        long runTimeMillis = (long) (runTimeSeconds * 1000);
        System.out.println("Uruchamianie wersji " + choice + " na " + runTimeSeconds + " sekund...");

        boolean useWaiter = (choice == 1);
        boolean randomOrder = (choice == 1 || choice == 2);
        boolean useHierarchy = (choice == 3);

        Fork[] forks = new Fork[numberOfPhilosophers];

        for (int i = 0; i < numberOfPhilosophers; i++) {
            forks[i] = new Fork(i, useHierarchy);
        }

        Waiter waiter = null;

        if (useWaiter) {
            waiter = new Waiter(numberOfPhilosophers - 1);
        }

        Philosopher[] philosophers =
                new Philosopher[numberOfPhilosophers];

        for (int i = 0; i < numberOfPhilosophers; i++) {

            Fork leftFork = forks[i];

            Fork rightFork =
                    forks[(i + 1) % numberOfPhilosophers];

            philosophers[i] = new Philosopher(
                    i,
                    leftFork,
                    rightFork,
                    waiter,
                    randomOrder,
                    choice == 3
            );

            philosophers[i].start();
        }

        Thread.sleep(runTimeMillis);

        System.out.println();
        System.out.println("Zatrzymywanie symulacji...");

        for (Philosopher p : philosophers) {
            p.stopPhilosopher();
        }

        for (Philosopher p : philosophers) {
            p.join();
        }

        System.out.println(
                "ID_Filozofa, Ilość_Zjedzonych_Posiłków"
        );

        for (int i = 0; i < philosophers.length ;i++) {
            Philosopher p = philosophers[i];
            if((i)%4 == 0) System.out.println();
            System.out.printf("%-3d,%-10d",p.getPhilosopherId(), p.getTotalNumberOfMealsEaten());
        }

        System.out.println();
        System.out.println("Symulacja zakończona.");

        scanner.close();
    }
}
