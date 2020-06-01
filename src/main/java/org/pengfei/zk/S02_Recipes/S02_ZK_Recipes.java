package org.pengfei.zk.S02_Recipes;

public class S02_ZK_Recipes {

    public static void main(String[] args){
    /****************************************1.1 ZooKeeper Recipes *********************************/

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

        /****************************************1.2 Simple Barrier *********************************/

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

        /****************************************1.3 Double Barrier *********************************/

    }
}
