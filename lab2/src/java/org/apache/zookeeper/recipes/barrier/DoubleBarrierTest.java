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

//package org.apache.zookeeper.recipes.barrier;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Random;

import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.data.Stat;

import java.util.Scanner;

public class DoubleBarrierTest {
    private ZooKeeper zk;
    private String name = "TODO";
    private Integer score = new Integer(0);

    private class DummyWatcher implements Watcher {
        public void process(WatchedEvent event) {
            // pass
        }
    }

    DoubleBarrierTest(String address) {
        try {
            this.zk = new ZooKeeper(address, 3000, new DummyWatcher());
        } catch (Exception e) {
            System.err.println(e);
        }
    }

    private void print(String s) {
        System.out.println(s);
    }

    void questions() {
        Scanner scanner = new Scanner(System.in);
        print("Quando eh 2 + 2?");
        Integer res = scanner.nextInt();
        if (res == 4) {
            score++;
        }
    }

    private void go() {
        Integer pos = new Integer(0);

        String nodeName = null;
        try {
            nodeName = new String(InetAddress.getLocalHost().getCanonicalHostName().toString());
        } catch (Exception e) {

        }
        print("Bem vindo aluno " + name);
        print("Aguardando que todos os alunos loguem");
        DoubleBarrier b = new DoubleBarrier(this.zk, "/b-prova", 2);
        try {
            b.enter();
        } catch (Exception e) {
            System.err.println("Erro ao entrar na barreira");
        }
        print("Todos os alunos logaram, pode comecar a prova");
        questions();
        print("Sua nota foi " + score.toString());
        
        try {
            zk.create("/notas", new byte[0], Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        } catch (Exception e) {

        }
        try {
            zk.create("/notas/" + nodeName, score.toString().getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            SimpleBarrier bsimple = new SimpleBarrier(this.zk, "/b-notas", 2);
            bsimple.barrier_wait();
            List<String> childNames = zk.getChildren("/notas", false);
            for (String child : childNames) {
                Integer grade = Integer.parseInt(new String(zk.getData("/notas/" + child, false, null)));
                if (grade > score) {
                    pos++;
                }
            }
        } catch (Exception e) {
            System.out.println(e);
        }

        try {
            b.leave();
        } catch (Exception e) {
            System.err.println("Erro ao sair da barreira");
        }
        try {
            zk.delete("/notas/" + nodeName, -1);
        } catch (Exception e) {

        }        
        print("Sua classificacao foi " + pos.toString());
    }

    public static void main(String args[]) {
        DoubleBarrierTest db = new DoubleBarrierTest(args[0]);
        db.go();
    }
}