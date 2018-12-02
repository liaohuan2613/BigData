package com.lhk.zkdist;

import org.apache.zookeeper.*;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.data.Stat;

public class DistributedServer {
	private static final String connectString = "hdp1:2181,hdp2:2181,hdp3:2181";
	private static final int sessionTimeout = 2000;
	private static final String parentNode = "/servers";

	private ZooKeeper zk = null;

	/**
	 * 创建到zk的客户端连接
	 * 
	 * @throws Exception
	 */
	public void getConnect() throws Exception {

		zk = new ZooKeeper(connectString, sessionTimeout, new Watcher() {
			@Override
			public void process(WatchedEvent event) {
				// 收到事件通知后的回调函数（应该是我们自己的事件处理逻辑）
				System.out.println(event.getType() + "---" + event.getPath());
				try {
					zk.getChildren("/", true);
				} catch (Exception e) {
				}
			}
		});

	}

	public void createParentNode() throws KeeperException, InterruptedException {
		// 参数1：要创建的节点的路径 参数2：节点大数据 参数3：节点的权限 参数4：节点的类型
		Stat stat =  zk.exists(parentNode,false);
		if (stat == null)
			zk.create(parentNode, "parentNode".getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
		else
			System.out.println("parentNode is exist!");
		//上传的数据可以是任何类型，但都要转成byte[]
	}

	/**
	 * 向zk集群注册服务器信息
	 * 
	 * @param hostname
	 * @throws Exception
	 */
	public void registerServer(String hostname) throws Exception {

		String create = zk.create(parentNode + "/server", hostname.getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
		System.out.println(hostname + "is online.." + create);

	}

	/**
	 * 业务功能
	 * 
	 * @throws InterruptedException
	 */
	public void handleBusiness(String hostname) throws InterruptedException {
		System.out.println(hostname + "start working.....");
		Thread.sleep(Long.MAX_VALUE);
	}

	public static void main(String[] args) throws Exception {
		// 获取zk连接
		DistributedServer server = new DistributedServer();
		server.getConnect();
		server.createParentNode();
		// 利用zk连接注册服务器信息
		server.registerServer(args[0]);

		// 启动业务功能
		server.handleBusiness(args[0]);

	}

}
