package org.pengfei.zk.basics.source;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;
import java.util.UUID;

public class DataUpdater implements Watcher {
    private static String hostPort = "localhost:2181";
    private static String zooDataPath = "/MyConfig";

    ZooKeeper zk;

    /* constructor creates a session*/
    public DataUpdater() throws IOException {
        try {
            zk = new ZooKeeper(hostPort, 2000, this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /* every five secs, generate a new UUID and write it to /MyConfig*/
    public void run() throws InterruptedException, KeeperException {
        while (true) {
            String uuid = UUID.randomUUID().toString();
            byte zoo_data[] = uuid.getBytes();
            zk.setData(zooDataPath, zoo_data, -1);
            try {
                Thread.sleep(5000); // Sleep for 5 secs
            } catch(InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
    @Override
    public void process(WatchedEvent watchedEvent) {
        System.out.printf("\n DataUpdater received event: %s", watchedEvent.toString());
    }
}
