package org.pengfei.zk.basics.source.cluster_monitor;

import org.apache.zookeeper.*;

import java.io.IOException;
import java.util.List;

public class ClusterMonitor implements Runnable {
    private static String membershipRoot = "/Members";
    private  Watcher connectionWatcher=null;
    private final Watcher childrenWatcher;
    private ZooKeeper zk;
    boolean alive = true;


    public ClusterMonitor(String HostPort) throws IOException, InterruptedException, KeeperException {

        // The connection watcher is optional, we don't really need it to run the monitor. We write it only to show
        // how to set a watch for new connection
        connectionWatcher = new Watcher() {
            @Override
            public void process(WatchedEvent event) {
                if (event.getType() == Watcher.Event.EventType.None &&
                        event.getState() ==
                                Watcher.Event.KeeperState.SyncConnected) {
                    System.out.printf("\nConnection Watcher event Received: %s",
                            event.toString());
                }
            }
        };

        /* This watcher watches if a new child created under /Members, if notified, print all
        * existing child members. */
        childrenWatcher = new Watcher() {
            @Override
            public void process(WatchedEvent event) {
                System.out.printf("\n Child watcher received event : %s",
                        event.toString());
                if (event.getType() ==
                        Event.EventType.NodeChildrenChanged) {
                    try {
//Get current list of child znode,
//reset the watch
                        List<String> children = zk.getChildren(
                                membershipRoot, this);
                        wall("!!!Cluster Membership Change!!!");
                        wall("Members: " + children);
                    } catch (KeeperException e) {
                        throw new RuntimeException(e);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        alive = false;
                        throw new RuntimeException(e);
                    }
                }
            }
        };

        // add a connection watch at time of the session connection.
        zk = new ZooKeeper(HostPort, 2000, connectionWatcher);

// Ensure the parent znode exists, here we don't add watcher, because we don't want be notified if the root content
// changes
        if (zk.exists(membershipRoot, false) == null) {
            zk.create(membershipRoot, "ClusterMonitorRoot".getBytes(),
                    ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        }
// Set a watch on the parent znode, which will be notified, if new child are added
        List<String> children = zk.getChildren(membershipRoot, childrenWatcher);
        System.err.println("Members: " + children);
    }

    public void run() {
        try {
            synchronized (this) {
                while (alive) {
                    wait();
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        } finally {
            this.close();
        }
    }

    public void wall (String message) {
        System.out.printf("\nMESSAGE: %s", message);
    }

    public synchronized void close() {
        alive=false;
        try {
            zk.close();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        notifyAll();
    }
}
