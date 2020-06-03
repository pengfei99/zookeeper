package org.pengfei.zk.S02_Recipes;

public class S02_ZK_Recipes {

    public static void main(String[] args) {
        /****************************************2.1 ZooKeeper Recipes *********************************/

        /*
         * ZooKeeper enables us to implement high-level primitives for distributed systems. These high-level constructs
         * are also known as ZooKeeper recipes. For example:
         * - barrier
         * - queue
         * - lock
         * - leader election
         * - group memberships
         * - two-phase commit
         * - service discovery
         *
         * These are implemented on the client side using ZooKeeper's programming model and require no special support
         * from the server side. Again, in the absence of ZooKeeper and its APIs, the implementation of these recipes
         * would have been quite complex and difficult.
         *
         * Some of the third-party and community-developed ZooKeeper client bindings also provide these high-level
         * distributed systems' constructs as a part of their client library. For example, Netflix Curator, a feature-rich
         * Java client framework for ZooKeeper, provides many of the recipes mentioned above. Kazoo, the Python client
         * library, also implements some of these recipes that developers can directly use in their client applications.
         *
         * The ZooKeeper distribution is shipped with recipes:
         * - leader election
         * - distributed lock
         * - distributed queue
         * These can be used inside distributed applications. The Java implementations for the three recipes can be
         * found in the recipes folder of the distribution.
         * */

        /****************************************2.2 Simple Barrier *********************************/

        /*
         * Barrier is a type of synchronization method used in distributed systems to block the processing of a set
         * of nodes until a condition is satisfied. It defines a point where all nodes must stop their processing
         * and cannot proceed until all the other nodes reach this barrier.
         *
         * The algorithm to implement a barrier using ZooKeeper is as follows:
         * 1. To start with, a znode is designated to be a barrier znode, say /zk_barrier .
         * 2. The barrier is said to be active in the system if this barrier znode exists.
         * 3. Each client calls the ZooKeeper API's exists() function on /zk_barrier by registering for watch events
         *    on the barrier znode (the watch event is set to true).
         * 4. If the exists() method returns false , the barrier no longer exists, and the client proceeds with its
         *    computation.
         * 5. Else, if the exists() method returns true , the clients just waits for watch events.
         * 6. Whenever the barrier exit condition is met, the client in charge of the barrier will delete /zk_barrier.
         * 7. The deletion triggers a watch event, and on getting this notification, the client calls the exists()
         *    function on /zk_barrier again.
         * 8. Step 7 returns true , and the clients can proceed further.
         * */

        /****************************************2.3 Double Barrier *********************************/

        /*
         * The logic of a double barrier states that a computation is started when the required number of processes
         * join the barrier. The processes leave after completing the computation, and when the number of processes
         * participating in the barrier become zero, the computation is stated to end.
         *
         * The algorithm for a double barrier is implemented by having a barrier znode that serves the purpose of
         * being a parent for individual process znodes participating in the computation. Its algorithm is outlined
         * as follows:
         *
         * Phase 1: Joining the barrier znode can be done as follows:
         * 1. Suppose the barrier znode is represented by znode/barrier . Every client process registers with the
         *    barrier znode by creating an ephemeral znode with /barrier as the parent. In real scenarios, clients
         *    might register using their hostnames.
         * 2. The client process sets a watch event for the existence of another znode called ready under the
         *    /barrier znode and waits for the node to appear.
         * 3. A number N is predefined in the system; this governs the minimum number of clients to join the barrier
         *    before the computation can start.
         * 4. While joining the barrier, each client process finds the number of child znodes of /barrier :
         *    M = getChildren(/barrier, watch=false)
         * 5. If M is less than N, the client waits for the watch event registered in step 3.
         * 6. Else, if M is equal to N, then the client process creates the ready znode under /barrier .
         * 7. The creation of the ready znode in step 5 triggers the watch event, and each client starts the
         *    computation that they were waiting so far to do.
         *
         * Phase 2: Leaving the barrier can be done as follows:
         * 1. Client processing on finishing the computation deletes the znode it created under /barrier
         *    (in step 2 of Phase 1: Joining the barrier).
         * 2. The client process then finds the number of children under /barrier : M = getChildren(/barrier, watch=True)
         *    If M is not equal to 0, this client waits for notifications (observe that we have set the watch event
         *    to True in the preceding call). If M is equal to 0, then the client exits the barrier znode.
         *
         *
         * The preceding procedure suffers from a potential herd effect where all client processes wake up to check
         * the number of children left in the barrier when a notification is triggered. To get away with this, we
         * can use a sequential ephemeral znode to be created in step 2 of Phase 1: Joining the barrier. Every client
         * process watches its next lowest sequential ephemeral znode to go away as an exit criterion. This way,
         * only a single event is generated for any client completing the computation, and hence, not all clients
         * need to wake up together to check on its exit condition.
         *
         * For a large number of client processes participating in a barrier, the herd effect can negatively impact
         * the scalability of the ZooKeeper service, and developers should be aware of such scenarios.
         * */

        /**************************************** 2.4 Queue ****************************************/

        /*
         * A distributed queue is a very common data structure used in distributed systems. A special implementation
         * of a queue, called a producer-consumer queue, is where a collection of processes called producers
         * generate or create new items and put them in the queue, while consumer processes remove the items from
         * the queue and process them. The addition and removal of items in the queue follow a strict ordering of FIFO.
         *
         * The pseudocode for the algorithm to implement a producer-consumer queue using ZooKeeper is shown here:
         * 1. Let /_QUEUE_ represent the top-level znode for our queue implementation, which is also called the
         *    queue-node.
         * 2. Clients acting as producer processes put something into the queue by calling the create() method with
         *    the znode name as "queue-" and set the sequence and ephemeral flags if the create() method call is set
         *    true : create( "queue-", SEQUENCE_EPHEMERAL). The sequence flag lets the new znode get a name like
         *    queue- N, where N is a monotonically increasing number.
         * 3. Clients acting as consumer processes process a getChildren() method call on the queue-node with a
         *    watch event set to true : M = getChildren(/_QUEUE_, true) It sorts the children list M , takes out the
         *    lowest numbered child znode from the list, starts processing on it by taking out the data from the znode,
         *    and then deletes it.
         * 4. The client picks up items from the list and continues processing on them. On reaching the end of the
         *    list, the client should check again whether any new items are added to the queue by issuing another
         *    get_children() method call.
         * 5. The algorithm continues when get_children() returns an empty list; this means that no more znodes or
         *    items are left under /_QUEUE_
         *
         * It's quite possible that in step 3, the deletion of a znode by a client will fail because some other client
         * has gained access to the znode while this client was retrieving the item. In such scenarios, the client
         * should retry the delete call.
         *
         * Using this algorithm for implementation of a generic queue, we can also build a priority queue out of it,
         * where each item can have a priority tagged to it. The algorithm and implementation is left as an exercise
         * to the readers.
         * */

        /**************************************** 2.5 Lock ****************************************/

        /*
         * A lock in a distributed system is an important primitive that provides the applications with a means to
         * synchronize their access to shared resources. Distributed locks need to be globally synchronous to ensure
         * that no two clients can hold the same lock at any instance of time.
         *
         * Typical scenarios where locks are inevitable are when the system as a whole needs to ensure that only one
         * node of the cluster is allowed to carry out an operation at a given time, such as:
         * - Write to a shared database or file
         * - Act as a decision subsystem
         * - Process all I/O requests from other nodes
         *
         * The pseudocode for the algorithm to implement a distributed lock service with ZooKeeper is shown here:
         * Let the parent lock node be represented by a persistent znode, /_locknode_ , in the Zookeeper tree.
         * Phase 1: Acquire a lock with the following steps:
         * 1. When a client wants to access the shared resource, it needs to create a ephemeral sequential znode.
         *    To do that, it calls the create("/_locknode_/lock-",CreateMode=EPHEMERAL_SEQUENTIAL) method.
         * 2. Call the getChildren("/_locknode_/lock-", false) method on the lock node. Here, the watch flag is set to
         *    false , as otherwise it can lead to a herd effect.
         * 3. If the znode created by the client in step 1 has the lowest sequence number suffix, then the client is
         *    owner of the lock, and it exits the algorithm.
         * 4. Call the exists("/_locknode_/<znode path with next lowest sequence number>, True) method.
         * 5. If the exists() method returns false , go to step 2.
         * 6. If the exists() method returns true , wait for notifications for the watch event set in step 4.
         *
         * Phase 2: Release a lock as follows:
         * 1. The client holding the lock deletes the node, thereby triggering the next client in line to acquire the
         *    lock.
         * 2. The client that created the next higher sequence node will be notified and hold the lock. The watch for
         *    this event was set in step 4 of Phase 1: Acquire a lock.
         *
         * While it's not recommended that you use a distributed system with a large number of clients due to the
         * herd effect, if the other clients also need to know about the change of lock ownership, they could set a
         * watch on the /_locknode_ lock node for events of the NodeChildrenChanged type and can determine the
         * current owner.
         *
         * If there was a partial failure in the creation of znode due to connection loss, it's possible that the
         * client won't be able to correctly determine whether it successfully created the child znode. To resolve
         * such a situation, the client can store its session ID in the znode data field or even as a part of the
         * znode name itself. As a client retains the same session ID after a reconnect, it can easily determine
         * whether the child znode was created by it by looking at the session ID.
         *
         * The idea of creating an ephemeral znode prevents a potential dead-lock situation that might arise when a
         * client dies while holding a lock. However, as the property of the ephemeral znode dictates that it gets
         * deleted when the session times out or expires, ZooKeeper will delete the znode created by the dead client,
         * and the algorithm runs as usual. However, if the client hangs for some reason but the ZooKeeper session is
         * still active, then we might get into a deadlock. This can be solved by having a monitor client that triggers
         * an alarm when the lock holding time for a client crosses a predefined time out.
         * */
        /**************************************** 2.6 Leader election ****************************************/

        /*
         * In distributed systems, leader election is the process of designating a single server as the organizer,
         * coordinator, or initiator of some task distributed among several individual servers (nodes). After a
         * leader election algorithm is run, a leader or a coordinator among the set of nodes is selected, and the
         * algorithm must ascertain that all the nodes in the system acknowledge its candidature without any
         * discrepancies for the correct functioning of the system.
         *
         * A leader is a single point of failure, and during failure, it can lead to an anomaly in the system. Hence, a
         * correct and robust leader election algorithm is required to choose a new coordinator or leader on failure
         * of the existing one.
         *
         * A leader election algorithm has the following two required properties:
         * - Liveness: This ensures that most of the time, there is a leader.
         * - Safety: This ensures that at any given time, there is either no leader or one leader.
         *
         * A leader-election algorithm needs a leader-election strategy. For example, the ZooKeeper use the strategy
         * which is the leader node must have the last transaction number.
         *
         * The simplest strategy is the the first connected node. The algorithm will be similar to the lock.
         * The pseudocode for the algorithm is outlined here.
         * Let /_election_ be the election znode path that acts as the root for all clients participating in the
         * leader election algorithm. Clients with proposals for their nomination in the leader election procedure
         * perform the following steps:
         * 1. Create a znode with the /_election_/candidate-sessionID_ path, with both the SEQUENCE and EPHEMERAL
         *    flags. The sessionID identifier, as a part of the znode name, helps in recognizing znodes in the case
         *    of partial failures due to connection loss. Now, say that ZooKeeper assigns a sequence number N to the
         *    znode when the create() call succeeds.
         * 2. Retrieve the current list of children in the election znode as follows: L = getChildren("/_election_", false)
         *    Here, L represents the list of children of "/_election_" . The watch is set to false to prevent any herd
         *    effect.
         * 3. Set a watch for changes in /_election_/candidate-sessionID_M , where M is the largest sequence number
         *    such that M is less than N, and candidate-sessionID_M is a znode in L as follows:
         *    exists("/_election_/candidate-sessionID_M", true)
         * 4. Upon receiving a notification of znode deletion for the watches set in step 3, execute the
         *    getChildren(("/_election_", false) method on the election znode.
         * 5. Let L be the new list of children of _election_ . The leader is then elected as follows:
         *    1. If candidate-sessionID_N (this client) is the smallest node in L , then declare itself as the leader.
         *    2. Watch for changes on /_election_/candidate-sessionID_M , where M is the largest sequence number such
         *       that M is less than N and candidate-sessionID_M is a znode in L.
         * 6. If the current leader crashes, the client having the znode with the next highest sequence number becomes
         *    the leader and so on.
         *
         * Optionally, a persistent znode is also maintained where the client declaring itself as the leader can store
         * its identifier so that other clients can query who the current leader is by reading this znode at any given
         * time. Such a znode also ensures that that the newly elected leader has acknowledged and executed the
         * leader election procedure correctly.
         * */

        /**************************************** 2.7 Group membership ****************************************/

        /*
         * A group membership protocol in a distributed system enables processes to reach a consensus on a group of
         * processes that are currently alive and operational in the system. It allows other processes to know when
         * a process joins the system and leaves the system, thereby allowing the whole cluster to be aware of the
         * current system state.
         *
         * The pseudocode for the algorithm to implement this group membership protocol is shown here.
         * Let a persistent znode, /membership , represent the root of the group in the ZooKeeper tree. A group
         * membership protocol can then be implemented as follows:
         * 1. Clients joining the group create ephemeral nodes under the group root to indicate membership.
         * 2. All the members of the group will register for watch events on /membership, thereby being aware of other
         *    members in the group. This is done as shown in the following code: L = getChildren("/membership", true)
         * 3. When a new client arrives and joins the group, all other members are notified.
         * 4. Similarly, when a client leaves due to failure or otherwise, ZooKeeper automatically deletes the
         *    ephemeral znodes created in step 2. This triggers an event, and other group members get notified.
         * 5. Live members know which node joined or left by looking at the list of children L.
         *
         * The preceding algorithm suffers from the herd effect, as events of NodeChildrenChanged emitted due to the
         * joining or leaving of members will cause all other members to wake up and find the current membership of
         * the system.
         * */

        /**************************************** 2.8 Two-phase commit ****************************************/

        /*
         * The two-phase commit (2PC) protocol is a distributed algorithm that coordinates all the processes that
         * participate in a distributed atomic transaction on whether to commit or abort (roll back) the transaction.
         *
         * The 2PC protocol consists of two phases, which are as follows:
         * - In the first phase, the coordinator node asks all the transaction's participating processes to prepare
         *   and vote to either commit or abort the transaction.
         * - In the second phase, the coordinator decides whether to commit or abort the transaction, depending on
         *   the result of the voting in the first phase. If all participants voted for commit, it commits the
         *   transaction; otherwise, it aborts it. It finally notifies the result to all the participants.
         *
         * Let /2PC_Transactions represent the root node to run the 2PC algorithm in ZooKeeper. The algorithm to
         * do so is as follows.
         * 1. A coordinator node creates a transaction znode, say /2PC_Transactions/TX . We can use the leader
         *    election algorithm to elect the coordinator using ZooKeeper. The coordinator node sets a watch on the
         *    transaction node.
         * 2. Another persistent znode, tx_result , is created under /2PC_Transactions/TX by the coordinator to post
         *    the result of the protocol, commit, or abort, and any additional outcomes of the transactions.
         * 3. Each participating client node sets a watch on the /2PC_Transactions as well as /2PC_Transactions
         *    /TX/tx_result znode paths.
         * 4. When the coordinator node creates the transaction znode, it notifies the participating client nodes
         *    that the coordinator node is requesting for voting on the transaction.
         * 5. The participants then create an ephemeral child znode in the /2PC_Transactions/TX path, with their
         *    own identifier (say hostnames) and vote for commit or abort by writing to the data field of their
         *    specific znodes.
         * 6. The coordinator is notified of the creation of all the child znodes, and when the number of child
         *    znodes in /2PC_Transactions/TX equals the number of participants, it checks the votes of all the
         *    participants by reading the participants' znodes.
         * 7. If all the participants voted for commit, the coordinator commits the transaction; otherwise, it aborts
         *    it. Subsequently, it posts the result of the transaction by writing to the /2PC_Transactions/TX/tx_result
         *    znode.
         * 8. The participant znodes get to know the outcome of the transaction when it gets a notification of
         *    NodeDataChanged for /2PC_Transactions/TX/tx_result
         * */

        /**************************************** 2.9 Service discovery ****************************************/

         /*
          * Service discovery is one of the key components of distributed systems and service-oriented architectures
          * where services need to find each other. In the simplest way, service discovery helps clients determine
          * the IP and port for a service that exists on multiple hosts.
          *
          * Important properties of a service discovery system are mentioned here:
          * - It allows services to register their availability
          * - It provides a mechanism to locate a live instance of a particular service
          * - It propagates a service change notification when the instances of a service change
          *
          * A simple service discovery model with ZooKeeper is illustrated as follows:
          * - Service registration: For service registrations, hosts that serve a particular service create an
          *   ephemeral znode in the relevant path under /services . For example, if a server is hosting a
          *   web-caching service, it creates an ephemeral znode with its hostname in /services/web_cache . Again,
          *   if some other server hosts a file-serving service, it creates another ephemeral znode with its
          *   hostname in /services/file_server and so on.
          * - Service discovery: Now, clients joining the system, register for watches in the znode path for the
          *   particular service. If a client wants to know the servers in the infrastructure that serve a
          *   web-caching service, the client will keep a watch in /services/web_cache. If a new host is added to
          *   serve web caching under this path, the client will automatically know the details about the new
          *   location. Again, if an existing host goes down, the client gets the event notification and can take the
          *   necessary action of connecting to another host.
          *
          * A service discovery system provides a seamless mechanism to guarantee service continuity in the case of
          * failures and is an indispensable part of building a robust and scalable distributed platform. Apache
          * Curator provides an extension called curator-x-discovery in its ZooKeeper library; this implements a
          * service registration and discovery model. It also provides a service discovery server called
          * curator-x-discovery-server that exposes a RESTful web service to register, remove, and query services
          * for non-Java or legacy applications to use the service discovery functionalities.
          * */

        /**************************************** 2.9 Service discovery ****************************************/

        /*
        * You can find the implementation of
        * - Barrier
        * - Lock
        * - Queue
        * - Two phase commit
        * - leader election
        * via the zookeeper java api https://zookeeper.apache.org/doc/r3.4.6/recipes.html
        *
        * There are other framework or literary such as Apache Curator which also provide their own implementation.
        * We will talk about in another section
        * */
    }
}
