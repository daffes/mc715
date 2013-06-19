Barrier recipe for Zookeeper

Overview:
- It's available two barrier class types in this lib
  1) Simple Barriers (SimpleBarrier.java)
     Constructor:
        SimpleBarrier(ZooKeeper zk, String BarrierName, int size)
     Public Methods:
        public void barrier_wait() throws KeeperException, InterruptedException

  2) Double Barriers
     Constructor:
        public DoubleBarrier(ZooKeeper zk, String BarrierName, int size)
     Public Methods:
        public boolean enter() throws KeeperException, InterruptedException
        public boolean leave() throws KeeperException, InterruptedException

- Simple Barrier implementation encapsulates a double barrier and just calls enter() followed by leave(), so, from now on, I'll just refer to Barrier without making any distinctions.

Usage:
- Each element trying to join the barrier should instantiate a barrier object with a common name. The identifier is generated at the time that an element tries to join the barrier so Multiple elements can be in different hosts or the same, they can also be in different threads of the same host.
- An element can rejoin a barrier with the same name after leaving it, there is no need to instantiate a new object or to create a barrier with a new name.
- The barrier is restricted, in the sense that if more elements than the declared size try to join the barrier, only the amount declared will be allowed to pass. Once all those elements leave, a new group will be allowed to pass. They will be allowed to pass in a FIFO order.


Known problems:
- If a barrier is (re)used a massive amount of times (around 1 billion elements), the counter will overflow. This will only create problems if your implementation needs the restricted behavior. In this case, it's very possible that an starvation scenario occurs, also, some elements may break the restriction. A possible solution is to schedule the deletion of the barrier node name in a cronjob.

Bugs, Feedback, Contribution:
- Check our git repository on https://github.com/daffes/mc715/
