package org.pengfei.zk.basics.source.cluster_monitor;

import org.apache.zookeeper.*;

import java.io.IOException;

public class ClusterClient implements Watcher, Runnable {
        private static String membershipRoot = "/Members";
        boolean close=false;
        ZooKeeper zk;

        public ClusterClient(String hostPort, Long pid) {
            String processId = pid.toString();
            try {
                zk = new ZooKeeper(hostPort, 2000, this);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (zk != null) {
                try {
                    zk.create(membershipRoot + '/' + processId,
                            processId.getBytes(),
                            ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
                } catch (
                        KeeperException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }


    @Override
    public void process(WatchedEvent event) {
        System.out.printf("\n Client watcher event received: %s \n", event.toString());
    }

    @Override
    public void run() {
// if client is not closed, suspend this thread and wait for close signal.
        try {
            synchronized (this) {
                while (!close) {
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

    public synchronized void close() {
            this.close=true;
        try {
            zk.close();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        notifyAll();
    }
}
