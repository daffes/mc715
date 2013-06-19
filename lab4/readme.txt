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
- Each element trying to join the barrier should instantiate a barrier object with a common name and a common size. The identifier is generated at the time that an element tries to join the barrier, so multiple elements can be in different hosts/processes/threads or the same.
- An element should always enter the barrier before leaving it (duh), the same element should not try to reenter the barrier before leaving it.
- An element can rejoin a barrier with the same name after leaving it, there is no need to instantiate a new object or to create a barrier with a new name.
- The barrier is restricted, in the sense that if more elements than the declared size try to join the barrier, only the amount declared will be allowed to pass. Once all those elements leave, a new group will be allowed to pass in a FIFO order.
- Barriers can be set one inside the other, as far as they are instantiated with different names.
- If an element looses the connection with the zookeeper while inside the barrier it will be assumed that it has left it.

Known problems:
- If a barrier is (re)used a massive amount of times (around 1 billion elements joining), the counter will overflow. This will only create problems if your implementation needs the restricted behavior. In this case, it's very possible that a starvation scenario occurs, also, some elements may break the restriction and just join the barrier. A possible solution is to schedule the deletion of the root node of the barrier in a cronjob.
- If all elements inside a barrier loose their connections with the zookeeper a deadlock situation will occur, in the sense that in the rescrited behavior the next group will not be allowed to pass.

Bugs, Feedback, Contribution:
- Check our git repository on https://github.com/daffes/mc715/
