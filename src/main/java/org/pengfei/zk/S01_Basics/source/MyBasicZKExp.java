package org.pengfei.zk.S01_Basics.source;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;
import org.pengfei.zk.S01_Basics.source.cluster_monitor.ClusterClient;
import org.pengfei.zk.S01_Basics.source.cluster_monitor.ClusterMonitor;

import java.io.IOException;
import java.util.List;
import java.util.Random;

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

public static void exp3(){
        String hostPort="localhost:2181";

        //run the cluster monitor
    ClusterMonitor clusterMonitor=null;
    try {
        clusterMonitor=new ClusterMonitor(hostPort);
        new Thread(clusterMonitor).start();
    } catch (IOException e) {
        e.printStackTrace();
    } catch (InterruptedException e) {
        e.printStackTrace();
    } catch (KeeperException e) {
        e.printStackTrace();
    }

    //create 10 client and run them on the cluster

    Long base = new Random().nextLong();
    ClusterClient[] clients=new ClusterClient[10];
    for(int i=0;i<10;i++){
        Long processId=base+i;
        ClusterClient clusterClient = new ClusterClient(hostPort, processId);
        clients[i]=clusterClient;
        new Thread(clusterClient).start();
    }

    // wait 10 sec
    try {
        Thread.sleep(10000);
    } catch (InterruptedException e) {
        e.printStackTrace();
    }

    // close 10 client
    for(int i=0;i<10;i++){
        clients[i].close();
    }

    // close the monitor
    clusterMonitor.close();
}
}
