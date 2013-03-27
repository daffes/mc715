import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Random;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.data.Stat;

public class SyncPrimitive implements Watcher {

    static ZooKeeper zk = null;
    static Integer mutex;

    String root;

    static void log(String s) {
        try {
            String name = new String(InetAddress.getLocalHost().getCanonicalHostName().toString());
            System.out.println("GREP: " + name + "\t" + s);
        }
        catch (UnknownHostException e) {
            System.out.println(e.toString());
        }
    }

    SyncPrimitive(String address) {
        if(zk == null){
            try {
                System.out.println("Starting ZK:");
                zk = new ZooKeeper(address, 3000, this);
                mutex = new Integer(-1);
                System.out.println("Finished starting ZK: " + zk);
            } catch (IOException e) {
                System.out.println(e.toString());
                zk = null;
            }
        }
        //else mutex = new Integer(-1);
    }

    synchronized public void process(WatchedEvent event) {
        synchronized (mutex) {
            //System.out.println("Process: " + event.getType());
            mutex.notify();
        }
    }

    /**
     * Barrier
     */
    static public class Barrier extends SyncPrimitive {
        int size;
        String name;

        /**
         * Barrier constructor
         *
         * @param address
         * @param root
         * @param size
         */
        Barrier(String address, String root, int size) {
            super(address);
            this.root = root;
            this.size = size;

            // Create barrier node
            if (zk != null) {
                try {
                    Stat s = zk.exists(root, false);
                    if (s == null) {
                        zk.create(root, new byte[0], Ids.OPEN_ACL_UNSAFE,
                                  CreateMode.PERSISTENT);
                    }
                } catch (KeeperException e) {
                    System.out
                        .println("Keeper exception when instantiating queue: "
                                 + e.toString());
                } catch (InterruptedException e) {
                    System.out.println("Interrupted exception");
                }
            }

            // My node name
            try {
                name = new String(InetAddress.getLocalHost().getCanonicalHostName().toString());
            } catch (UnknownHostException e) {
                System.out.println(e.toString());
            }

        }

        /**
         * Join barrier
         *
         * @return
         * @throws KeeperException
         * @throws InterruptedException
         */

        boolean enter(Integer k) throws KeeperException, InterruptedException{
            zk.create(root + "/" + name, new byte[0], Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            // MARKER
            log("Entrei na fila da barreira");
            log("Aguardando " + k + " segundos antes de verificiar");
            try {
                Thread.sleep(k * 1000);
            } catch (InterruptedException ts) {
            }
            while (true) {
                synchronized (mutex) {
                    List<String> list = zk.getChildren(root, true);
                    log("Verificando se posso entrar, tamanho da fila = " + list.size());
                    if (list.size() < size) {
                        mutex.wait();
                    } else {
                        return true;
                    }
                }
            }
        }

        /**
         * Wait until all reach barrier
         *
         * @return
         * @throws KeeperException
         * @throws InterruptedException
         */

        boolean leave() throws KeeperException, InterruptedException{
            try {
                zk.delete(root + "/" + name, 0);
            } catch (KeeperException e){
                log("Codigo lanca excecao mas o zookeeper realmente deleta, ignorando");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ts) {}
            }
            log("Entrei na fila de saida");
            while (true) {
                synchronized (mutex) {
                    List<String> list = zk.getChildren(root, true);
                    log("Verificando se posso sair, fila = " + list.size());
                    if (list.size() > 0) {
                        mutex.wait();
                    } else {
                        return true;
                    }
                }
            }
        }
    }

    public static void main(String args[]) {
        if (args[0].equals("bTest"))
            barrierTest(args);

    }

    public static void barrierTest(String args[]) {
        Barrier b = new Barrier(args[1], "/b1", 2);
        try{
            boolean flag = b.enter(new Integer(args[2]));
            log("Entrei na Barreira");
            if(!flag) System.out.println("Error when entering the barrier");
        } catch (KeeperException e){

        } catch (InterruptedException e){

        }
        try{
            b.leave();
        } catch (KeeperException e){
            log(e.toString());
        } catch (InterruptedException e){
            log(e.toString());
        }
        log("Sai da barreira");
    }
}
