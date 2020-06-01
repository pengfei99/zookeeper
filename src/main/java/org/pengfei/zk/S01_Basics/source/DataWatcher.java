package org.pengfei.zk.S01_Basics.source;

import org.apache.zookeeper.*;

import java.io.IOException;

public class DataWatcher implements Watcher, Runnable {
    private static String hostPort = "localhost:2181";
    private static String zooDataPath = "/MyConfig";
    byte zoo_data[] = null;
    ZooKeeper zk;

    // Constructor creates the znode if it doesn't exist
    public DataWatcher() {
        try {
            zk = new ZooKeeper(hostPort, 2000, this);
            if (zk != null) {
                try {
                    // check if a znode exists or not
                    if (zk.exists(zooDataPath, this) == null) {
                        //create a persistent znode with empty data and open_acl_unsafe acl.
                        zk.create(zooDataPath, "".getBytes(),
                                ZooDefs.Ids.OPEN_ACL_UNSAFE,
                                CreateMode.PERSISTENT);
                    }
                } catch (KeeperException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void run() {
        try {
            synchronized (this) {
                while (true) {
                    // the watchedEvent will notify this, so we can do wait
                    wait();
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
    }


    /* if the data content of znode is changed, we print the latest version*/
    @Override
    public void process(WatchedEvent event) {
        System.out.printf("\nDataWatcher Received event: %s", event.toString());
//We will process only events of type NodeDataChanged
        if (event.getType() == Event.EventType.NodeDataChanged) {
            try {
                printData();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (KeeperException e) {
                e.printStackTrace();
            }
        }
    }

   /* The following code prints the current content of the znode to the console
   * */
    public void printData()
            throws InterruptedException, KeeperException {
        zoo_data = zk.getData(zooDataPath, this, null);
        String zString = new String(zoo_data);
        System.out.printf("\nCurrent Data @ ZK Path %s: %s",
                zooDataPath, zString);
    }
}
