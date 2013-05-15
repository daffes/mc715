/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.zookeeper.recipes.barrier;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Scanner;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.ZooDefs.Ids;

public class DoubleBarrier {

    private ZooKeeper zk;
    private String dir;
    private String nodeName;
    private Integer mutex;
    private BarrierWatcher watcher;
    private int size;
    private boolean debug;

    public DoubleBarrier(ZooKeeper zk, String dir, int size) {
        this(zk, dir, size, false);
    }

    public DoubleBarrier(ZooKeeper zk, String dir, int size, boolean debug) {
        this.zk = zk;
        this.dir = dir;
        this.size = size;
        this.mutex = new Integer(-1);
        this.debug = debug;
        this.watcher = new BarrierWatcher();
        
        try {
            if (zk.exists(dir, false) == null) {
                zk.create(dir, new byte[0], Ids.OPEN_ACL_UNSAFE,
                          CreateMode.PERSISTENT);
            }
        } catch (KeeperException e) {
            System.out.println(
              "Keeper exception when instantiating queue: " + e.toString());
        } catch (InterruptedException e) {
            System.out.println("Interrupted exception");
        }

        // My node name
        try {
            this.nodeName = new String(InetAddress.getLocalHost().getCanonicalHostName().toString());
        } catch (UnknownHostException e) {
            System.out.println(e.toString());
        }
    }
    
    private class BarrierWatcher implements Watcher {
        public void process(WatchedEvent event) {
            synchronized (mutex) {
                mutex.notify();
            }
        }
        public void await() throws java.lang.InterruptedException {
            synchronized (mutex) {
                mutex.wait();
            } 
        }
    }
    
    public boolean enter() throws KeeperException, InterruptedException{
        zk.create(dir + "/" + nodeName, new byte[0], Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        while (true) {
            synchronized (mutex) {
                List<String> list = zk.getChildren(dir, watcher);
                if (zk.exists(dir + "/ready", watcher) == null && list.size() < size) {
                    watcher.await();
                } else {
                    try {
                        zk.create(dir + "/ready", new byte[0], Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                    } catch (KeeperException e){ 
                        // TODO
                    }
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
    
    public boolean leave() throws KeeperException, InterruptedException {
        try {
            zk.delete(dir + "/" + nodeName, 0);
        } catch (KeeperException e) {
            // TODO
        }
        while (true) {
            synchronized (mutex) {
                if (this.debug) {
                    System.out.println("De um enter para verificarmos se podemos sair da barreira");
                    new java.util.Scanner(System.in).nextLine();
                }
                List<String> list = zk.getChildren(dir, watcher);
                if (this.debug) {
                    System.out.println("tamanho: " + (new Integer(list.size()).toString()));
                }
                if (list.size() > 1 && zk.exists(dir + "/ready", watcher) != null) {
                    watcher.await();
                } else {
                    try {
                        zk.delete(dir + "/ready", 0);
                    } catch (KeeperException e) { 
                        // TODO
                    }
                    return true;
                }
            }
        }
    }
}
