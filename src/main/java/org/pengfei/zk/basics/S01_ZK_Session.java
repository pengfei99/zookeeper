package org.pengfei.zk.basics;

import org.pengfei.zk.basics.source.MyBasicZKExp;

import java.io.IOException;

public class S01_ZK_Session {
    public static void main(String[] args) throws IOException {

    /****************************************1.1 Create ZooKeeper client Session *********************************/

    /*
    * The ZooKeeper Java API provide three constructors to create client sessions with zk servers:
    *
    * - ZooKeeper(String connectString, int sessionTimeout, Watcher watcher)
    * - ZooKeeper(String connectString, int sessionTimeout, Watcher watcher, boolean canBeReadOnly)
    * - ZooKeeper(String connectString, int sessionTimeout, Watcher watcher, long sessionId, byte[] sessionPasswd)
    * - ZooKeeper(String connectString, int sessionTimeout, Watcher watcher, long sessionId, byte[] sessionPasswd,
    *   boolean canBeReadOnly)
    *
    * Here the:
    * - connectString: specifies the zk node connection information(e.g. localhost:2181, zk1.pengfei.org:2181)
    * - sessionTimeout: is the amount of time ZooKeeper waits without getting a heartbeat from the client
    *                   before declaring the session as dead in millisecond. In our case, it's two sec.
    * - Watcher: A watcher object, which, if created, will be notified of state changes and node events. This
    *            watcher object needs to be created separately through a user-defined class by implementing the
    *            Watcher interface and passing the instantiated object to the ZooKeeper constructor. A client
    *            application can get a notification for various types of events such as connection loss, session
    *            expiry, and so on.
    * - canBeReadOnly: If set to true , allows the created client to go to the read-only mode in case of network
    *                  partitioning. The read-only mode is a scenario in which a client can't find any majority
    *                  servers (e.g. leaders, followers) but there's a partitioned server (i.e. observer) that it
    *                  can reach; it connects to it in a read-only mode such that read requests to the server
    *                  are allowed but write requests are not. The client continues to attempt to connect
    *                  to majority servers in the background, while still maintaining the read-only mode.
    * - sessionId: In case the client is reconnecting to the ZooKeeper server, a specific session ID can be used to
    *              refer to the previously connected session
    *
    * - sessionPasswd : If the specified session requires a password, this can be specified here.
    * */

        /* Check MyBasicZKExp.exp1(); for a simple zk session example*/
        // MyBasicZKExp.exp1();

        /****************************************1.2 Customize a Watcher  *********************************/

        /*
        * Watches enable a client to receive notifications from the ZooKeeper server and process these events
        * upon occurrence. ZooKeeper Java APIs provide a public interface called Watcher , which a client event
        * handler class must implement in order to receive notifications about events from the ZooKeeper server
        * it connects to. Programmatically, an application that uses such a client handles these events by
        * registering a callback object with the client.
        *
        * In package org.apache.zookeeper, you can find this interface
        * public interface Watcher {
        *     void process(WatchedEvent event);
        * }
        *
        * To illustrate the znode data watcher, we will have two Java classes: DataWatcher and DataUpdater .
        * DataWatcher will run continuously and listen for the NodeDataChange events from the ZooKeeper server
        * in a specific znode path called /MyConfig . The DataUpdater class will periodically update the data
        * field in this znode path, which will generate events, and upon receiving these events, the DataWatcher
        * class will print the changed data onto the console.
        * */
MyBasicZKExp.exp2();
    }
}
