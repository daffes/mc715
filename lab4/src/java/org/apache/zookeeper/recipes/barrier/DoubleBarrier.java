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
import java.util.Collections;
import java.util.Comparator;



import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.ZooDefs.Ids;

public class DoubleBarrier {

    private ZooKeeper zk;
    private String dir;
    private String prefix;
    private String readyNode;
    private String myId;
    private Integer mutex;
    private BarrierWatcher watcher;
    private int size;
    private long maxId;

    public DoubleBarrier(ZooKeeper zk, String dir, int size) {
        this.zk = zk;
        this.dir = dir;
        this.size = size;
        this.mutex = new Integer(-1);
        this.watcher = new BarrierWatcher();
        this.readyNode = null;

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

        this.prefix = "b-";
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
    
    class NodeComparator implements Comparator<String> {
        public int compare(String a, String b) {
            Long ida = new Long(a.substring(prefix.length()));
            Long idb = new Long(b.substring(prefix.length()));
            return (int)(ida - idb);
        }
    }

    public boolean enter() throws KeeperException, InterruptedException {
        this.myId = zk.create(dir + "/" + prefix, new byte[0], Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
        this.myId = this.myId.substring((dir + "/" + prefix).length());

        while (true) {
            synchronized (mutex) {
                List<String> list = zk.getChildren(dir, watcher);
                for (String name : list) {
                    if (name.substring(0, 2).equals("r-")) {
                        this.readyNode = name;
                        this.maxId = new Long(name.substring(2));
                        if ((new Long(this.myId)) <= maxId) {
                            return true;
                        }
                    }
                }

                if (list.size() < size) {
                    watcher.await();
                    continue;
                }

                Collections.sort(list, new NodeComparator());
                if (this.myId.equals(list.get(0).substring(prefix.length()))) { // I'm the smallest
                    this.maxId = new Long(list.get(size - 1).substring(this.prefix.length()));
                    try {
                        this.readyNode = "r-" + (new Long(maxId)).toString();
                        zk.create(dir + "/" + this.readyNode, new byte[0], Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
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
            zk.delete(dir + "/" + prefix + this.myId, 0);
        } catch (KeeperException e) {
            // TODO
        }
        while (true) {
            synchronized (mutex) {
                int cnt = 0;

                List<String> list = zk.getChildren(dir, watcher);
                for (String name : list) {
                    if (!name.substring(0, 2).equals("r-") && 
                        new Long(name.substring(prefix.length())) <= this.maxId) {
                        cnt++;
                    }
                }
                if (cnt > 0) {
                    watcher.await();
                } else {
                    try {
                        zk.delete(dir + "/" + this.readyNode, 0);
                    } catch (KeeperException e) { 
                        // TODO
                    }
                    return true;
                }
            }
        }
    }
}
