package org.pengfei.zk.basics.source;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;
import java.util.List;

public class MyBasicZKExp {
    //host can be the url or ip of a zk node
    private static final String host = "localhost";

    // port number of a zk node
    private static final int port = 2181;

    private static final int sessionTimeout=2000;

    /** In exp1, we create a session without login and password. We use this session to get all child of a znode*/
    public static void exp1(){

        Object watcher=null;

        //This calls the ZooKeeper constructor, which tries to connect to the ZooKeeper server and
        //returns a handle to it
        ZooKeeper zk = null;
        try {
            zk = new ZooKeeper(host + ":" + port, sessionTimeout, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String zpath = "/";
        if (zk != null) {
            try {
                // get all child of zpath, as its a read action, we can set a watch. In our case, we don't want to set
                // a watch, so we put a false here.
                List<String> zooChildren = zk.getChildren(zpath, false);
                System.out.println("Znodes of "+zpath+": ");
                for (String child : zooChildren) {
                    //print the children
                    System.out.println(child);
                }
            } catch (KeeperException e) {
                System.out.println(e);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void exp2(){

        // start first the dataWatcher in a separate thread
        DataWatcher dataWatcher = new DataWatcher();
        try {
            dataWatcher.printData();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (KeeperException e) {
            e.printStackTrace();
        }
    new Thread(dataWatcher).start();

        // start the updater
        try {
            DataUpdater dataUpdater= new DataUpdater();
            dataUpdater.run();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (KeeperException e) {
            e.printStackTrace();
        }
    }


}
