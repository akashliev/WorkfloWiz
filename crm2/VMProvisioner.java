package crm2;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import utility.Utility;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.CreateKeyPairRequest;
import com.amazonaws.services.ec2.model.CreateKeyPairResult;
import com.amazonaws.services.ec2.model.CreateTagsRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.KeyPair;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.StartInstancesRequest;
import com.amazonaws.services.ec2.model.StopInstancesRequest;
import com.amazonaws.services.ec2.model.Tag;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;
import com.amazonaws.services.rds.AmazonRDSClient;

public class VMProvisioner {
	private Logger log = Logger.getInstance(VMProvisioner.class);

	// require variable for every operation
	private String accessKey = "AKIAJNIZ5K5WD7CVTZNA";
	private String secretKey = "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";
	private AWSCredentials credentials;
	// private String endPoint ;
	private Region region;
	private AmazonEC2Client ec2client;
	private AmazonRDSClient rdsclient;

	// EC2 Security Group Variables
	private String groupName = "group1";
	// private String groupDescription = "default group";
	//
	// private String sshIpRange = "0.0.0.0/0";
	// private String sshprotocol = "tcp";
	// private int sshFromPort = 22;
	// private int sshToPort =22;
	//
	// private String httpIpRange = "0.0.0.0/0";
	// private String httpProtocol = "tcp";
	// private int httpFromPort = 80;
	// private int httpToPort = 80;
	//
	// private String httpsIpRange = "0.0.0.0/0";
	// private String httpsProtocol = "tcp";
	// private int httpsFromPort = 443;
	// private int httpsToProtocol = 443;

	// KeyPair variables
	private String keyName = "apr17";
	// private String pemFilePath = "/home/andrey/Dropbox/1WayneState_Research/2014_SCC/6_Amazon/2_ec2_keyPair"; //
	// /Users/kpbird/Desktop
	// private String pemFileName = "pemFileName_12-17";

	// EC2 Instance variables
	// private String imageId ="ami-7748661e";
	// private String imageId = "ami-f1536798";
	// private String imageId = "ami-af3d03c6";
	// private String imageId = "ami-77340a1e";
	// private String imageId = "ami-c98cb4a0";
	// private String imageId = "ami-5587bf3c";
	// private String imageId = "ami-9d87bff4";
	// private String imageId = "ami-29112a40";
	// private String imageId = "ami-17a2997e" ; // see if logging is only local, iterations of running
	//private String imageId = "ami-75a69d1c";
//	private String imageId = "ami-d1f4e9b0";
	private String imageId = "ami-a9f600c9";

	private String instanceType = "t1.micro";
	private String instanceName = "viewTestJava";

	public static void main(String[] args) {
		VMProvisioner m = new VMProvisioner();
		m.init();
		// m.createEC2OnDemandInstance();
		// String result = m.createEC2Instance(3);
		// System.out.println(result);

		ArrayList<String> allButMasterNode = m.getAvailableInstIds();
		System.out.println(allButMasterNode);
		String masterNodeInstanceId = "i-772ba059";
		allButMasterNode.remove(masterNodeInstanceId);
		System.out.println("final:");
		System.out.println(allButMasterNode);
		for (String id : allButMasterNode)
			m.terminateInstance(id);

		// m.terminateInstance("i-a4696bd8");

	}

	public static void terminateAllButMasterNode() {
		VMProvisioner m = new VMProvisioner();
		m.init();

		ArrayList<String> allButMasterNode = m.getAvailableInstIds();
		String masterNodeInstanceId = "i-772ba059";
		allButMasterNode.remove(masterNodeInstanceId);
		System.out.println("terminating " + allButMasterNode);
		for (String id : allButMasterNode)
			m.terminateInstance(id);
	}
	
	public static void terminateAllVMs() throws Exception {
		VMProvisioner m = new VMProvisioner();
		m.init();

		ArrayList<String> allButMasterNode = m.getAvailableInstIds();
		System.out.println("terminating " + allButMasterNode);
		for (String id : allButMasterNode)
			m.terminateInstance(id);
		
		ArrayList<String> vmIPs = getAvailableVMIPs();
		System.out.println("Done. Number of active VMs: " + vmIPs.size());
	}

	private void init() {
		credentials = new BasicAWSCredentials(accessKey, secretKey);
		// end point for singapore
		// endPoint = "https://rds.ap-southeast-1.amazonaws.com";
		// regions for singapore
		// region = Region.getRegion(Regions.US_EAST_1);
		region = Region.getRegion(Regions.US_WEST_2);
		// EC2Client object
		ec2client = new AmazonEC2Client(credentials);
		// ec2client.setEndpoint(endPoint);
		ec2client.setRegion(region);
		// RDSClient object
		// rdsclient = new AmazonRDSClient(credentials);
		// rdsclient.setRegion(region);
		// rdsclient.setEndpoint(endPoint);

	}

	private void createKeyPair() {
		try {
			CreateKeyPairRequest ckpr = new CreateKeyPairRequest();
			ckpr.withKeyName(keyName);

			CreateKeyPairResult ckpresult = ec2client.createKeyPair(ckpr);

			KeyPair keypair = ckpresult.getKeyPair();
			String privateKey = keypair.getKeyMaterial();
			log.Info("KeyPair :" + privateKey);
			// writePemFile(privateKey, pemFilePath, pemFileName);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
	}

	private void createEC2OnDemandInstance() {
		try {

			// request for new on demand instance
			RunInstancesRequest rir = new RunInstancesRequest();
			rir.withImageId(imageId);
			rir.withInstanceType(instanceType);
			rir.withMinCount(1);
			rir.withMaxCount(1);
			rir.withKeyName(keyName);
			rir.withMonitoring(true);
			rir.withSecurityGroups(groupName);

			RunInstancesResult riresult = ec2client.runInstances(rir);
			log.Info(riresult.getReservation().getReservationId());

			// / Find newly created instance id
			String instanceId = null;
			DescribeInstancesResult result = ec2client.describeInstances();
			Iterator<Reservation> i = result.getReservations().iterator();
			while (i.hasNext()) {
				Reservation r = i.next();
				List<Instance> instances = r.getInstances();
				for (Instance ii : instances) {
					log.Info(ii.getImageId() + "\t" + ii.getInstanceId() + "\t" + ii.getState().getName() + "\t" + ii.getPrivateDnsName());
					if (ii.getState().getName().equals("pending")) {
						instanceId = ii.getInstanceId();
					}
				}
			}

			log.Info("New Instance ID :" + instanceId);
			// / Waiting for Instance Running////
			boolean isWaiting = true;
			while (isWaiting) {
				log.Info("*** Waiting ***");
				Thread.sleep(1000);
				DescribeInstancesResult r = ec2client.describeInstances();
				Iterator<Reservation> ir = r.getReservations().iterator();
				while (ir.hasNext()) {
					Reservation rr = ir.next();
					List<Instance> instances = rr.getInstances();
					for (Instance ii : instances) {
						log.Info(ii.getImageId() + "\t" + ii.getInstanceId() + "\t" + ii.getState().getName() + "\t" + ii.getPrivateDnsName());
						if (ii.getState().getName().equals("running") && ii.getInstanceId().equals(instanceId)) {
							log.Info(ii.getPublicDnsName());
							isWaiting = false;
						}
					}
				}
			}

			// // / Creating Tag for New Instance ////
			// log.Info("Creating Tags for New Instance");
			// CreateTagsRequest crt = new CreateTagsRequest();
			// ArrayList<Tag> arrTag = new ArrayList<Tag>();
			// arrTag.add(new Tag().withKey("Name").withValue(instanceName));
			// crt.setTags(arrTag);
			//
			// ArrayList<String> arrInstances = new ArrayList<String>();
			// arrInstances.add(instanceId);
			// crt.setResources(arrInstances);
			// ec2client.createTags(crt);

		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
	}

	private String createEC2Instance(int noOfInstances) {
		try {

			// request for new on demand instance
			RunInstancesRequest rir = new RunInstancesRequest();
			rir.withImageId(imageId);
			rir.withInstanceType(instanceType);
			rir.withMinCount(noOfInstances);
			rir.withMaxCount(noOfInstances);
			rir.withKeyName(keyName);
			rir.withMonitoring(true);
			rir.withSecurityGroups(groupName);

			RunInstancesResult riresult = ec2client.runInstances(rir);
			log.Info(riresult.getReservation().getReservationId());

			// // / Find newly created instance id
			// String instanceId = null;
			// DescribeInstancesResult result = ec2client.describeInstances();
			// Iterator<Reservation> i = result.getReservations().iterator();
			// while (i.hasNext()) {
			// Reservation r = i.next();
			// List<Instance> instances = r.getInstances();
			// for (Instance ii : instances) {
			// log.Info(ii.getImageId() + "\t" + ii.getInstanceId() + "\t" + ii.getState().getName() + "\t" +
			// ii.getPrivateDnsName());
			// if (ii.getState().getName().equals("pending")) {
			// instanceId = ii.getInstanceId();
			// }
			// }
			// }
			//
			// log.Info("New Instance ID :" + instanceId);
			// / Waiting for Instance Running////
			// boolean isWaiting = true;
			// while (isWaiting) {
			// log.Info("*** Waiting ***");
			// Thread.sleep(1000);
			// DescribeInstancesResult r = ec2client.describeInstances();
			// Iterator<Reservation> ir = r.getReservations().iterator();
			// while (ir.hasNext()) {
			// Reservation rr = ir.next();
			// List<Instance> instances = rr.getInstances();
			// for (Instance ii : instances) {
			// log.Info(ii.getImageId() + "\t" + ii.getInstanceId() + "\t" + ii.getState().getName() + "\t" +
			// ii.getPrivateDnsName());
			// if (ii.getState().getName().equals("running") && ii.getInstanceId().equals(instanceId)) {
			// log.Info(ii.getPublicDnsName());
			// isWaiting = false;
			// }
			// }
			// }
			// }
			return null;
			// // / Creating Tag for New Instance ////
			// log.Info("Creating Tags for New Instance");
			// CreateTagsRequest crt = new CreateTagsRequest();
			// ArrayList<Tag> arrTag = new ArrayList<Tag>();
			// arrTag.add(new Tag().withKey("Name").withValue(instanceName));
			// crt.setTags(arrTag);
			//
			// ArrayList<String> arrInstances = new ArrayList<String>();
			// arrInstances.add(instanceId);
			// crt.setResources(arrInstances);
			// ec2client.createTags(crt);

		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
		return null;
	}

	public void stopInstance(String instanceId) {
		StopInstancesRequest sir = new StopInstancesRequest();
		sir.withInstanceIds(instanceId);
		ec2client.stopInstances(sir);
	}

	public void startInstance(String instanceId) {
		StartInstancesRequest sir = new StartInstancesRequest();
		sir.withInstanceIds(instanceId);
		ec2client.startInstances(sir);
	}

	public void terminateInstance(String instanceId) {
		TerminateInstancesRequest tir = new TerminateInstancesRequest();
		tir.withInstanceIds(instanceId);
		ec2client.terminateInstances(tir);
	}

	private void writePemFile(String privateKey, String pemFilePath, String keyname) {
		try {
			PrintWriter writer = new PrintWriter(pemFilePath + "/" + keyname + ".pem", "UTF-8");
			System.out.println("wwwwwwwwwwwwwwwriting file: \n" + pemFilePath + "/" + keyname + ".pem");
			writer.print(privateKey);
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static ArrayList<String> provisionVMs(String type, int noOfInstances) throws Exception {
		ArrayList<String> initialVMs = VMProvisioner.getAvailableInstIds();
		VMProvisioner.justLaunchVMs(type, noOfInstances);
		ArrayList<String> runningAndPendingVMs = VMProvisioner.getAvailableAndPendingInstIds();

		runningAndPendingVMs.removeAll(initialVMs);
		VMProvisioner.waitUntilAllPendingBecomeRunning(runningAndPendingVMs);
		ArrayList<String> ipAddresses = getIPaddresses(runningAndPendingVMs);
		ipAddresses.remove(Utility.getIpGlobal());
		System.out.println("iiiiiiiiiiiiiiiiiiiiiiiips: " + ipAddresses);
		return ipAddresses;
	}

	public static void justLaunchVMs(String type, int noOfInstances) {
		VMProvisioner provisioner = new VMProvisioner();
		provisioner.init();
		provisioner.instanceType = type;
		provisioner.createEC2Instance(noOfInstances);
	}

	// public static ArrayList<String> getAvailableInstanceIds() {
	// ArrayList<String> resultList = new ArrayList<String>();
	// VMProvisioner provisioner = new VMProvisioner();
	// provisioner.init();
	// String instanceId = null;
	// DescribeInstancesResult result = provisioner.ec2client.describeInstances();
	// Iterator<Reservation> i = result.getReservations().iterator();
	// while (i.hasNext()) {
	// Reservation r = i.next();
	// List<Instance> instances = r.getInstances();
	// for (Instance ii : instances) {
	// provisioner.log.Info(ii.getImageId() + "\t" + ii.getInstanceId() + "\t" + ii.getState().getName() + "\t" +
	// ii.getPrivateDnsName());
	// if (ii.getState().getName().equals("running")) {
	// resultList.add(ii.getInstanceId());
	// }
	// }
	// }
	// return resultList;
	// }

	public static ArrayList<String> getAvailableVMIPs() throws Exception {
		ArrayList<String> resultList = new ArrayList<String>();
		VMProvisioner provisioner = new VMProvisioner();
		provisioner.init();
		String instanceId = null;
		DescribeInstancesResult result = provisioner.ec2client.describeInstances();
		Iterator<Reservation> i = result.getReservations().iterator();
		while (i.hasNext()) {
			Reservation r = i.next();
			List<Instance> instances = r.getInstances();
			for (Instance ii : instances) {
				provisioner.log.Info(ii.getImageId() + "\t" + ii.getInstanceId() + "\t" + ii.getState().getName() + "\t" + ii.getPrivateDnsName());
				if (ii.getState().getName().equals("running")) {
					resultList.add(ii.getPublicIpAddress());
				}
			}
		}
		resultList.remove(Utility.getIpGlobal());
		return resultList;
	}

	public static ArrayList<String> getAvailableAndPendingVMIPs() {
		ArrayList<String> resultList = new ArrayList<String>();
		VMProvisioner provisioner = new VMProvisioner();
		provisioner.init();
		String instanceId = null;
		DescribeInstancesResult result = provisioner.ec2client.describeInstances();
		Iterator<Reservation> i = result.getReservations().iterator();
		while (i.hasNext()) {
			Reservation r = i.next();
			List<Instance> instances = r.getInstances();
			System.out.println("iiiiiiiiiiiiiiiiiiiiiinstances size: " + instances.size());
			for (Instance ii : instances) {
				provisioner.log.Info(ii.getImageId() + "\t" + ii.getInstanceId() + "\t" + ii.getState().getName() + "\t" + ii.getPrivateDnsName());
				if (ii.getState().getName().equals("running") || ii.getState().getName().equals("pending")) {
					System.out.println("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaadding " + ii.getPublicIpAddress());
					resultList.add(ii.getPublicIpAddress());
				}
			}
		}
		return resultList;
	}

	public static void waitUntilAllPendingBecomeRunning(ArrayList<String> pendingInstIds) throws Exception {
		int noOfPending = pendingInstIds.size();

		VMProvisioner provisioner = new VMProvisioner();
		provisioner.init();

		boolean isWaiting = true;
		while (isWaiting) {
			provisioner.log.Info("*** Waiting ***");
			Thread.sleep(1000);
			DescribeInstancesResult r = provisioner.ec2client.describeInstances();
			Iterator<Reservation> ir = r.getReservations().iterator();
			while (ir.hasNext()) {
				Reservation rr = ir.next();
				List<Instance> instances = rr.getInstances();
				for (Instance ii : instances) {
					provisioner.log.Info(ii.getImageId() + "\t" + ii.getInstanceId() + "\t" + ii.getState().getName() + "\t"
							+ ii.getPrivateDnsName());
					for (String pendingVMInstId : pendingInstIds) {
						if (ii.getInstanceId().trim().equals(pendingVMInstId.trim()))
							if (ii.getState().getName().trim().equals("running") && ii.getInstanceId().trim().equals(pendingVMInstId.trim())) {
								provisioner.log.Info(pendingVMInstId + " is now running");
								noOfPending--;
								if (noOfPending == 0)
									isWaiting = false;
							}
					}
				}
			}
		}

	}

	public static ArrayList<String> getAvailableInstIds() {
		ArrayList<String> resultList = new ArrayList<String>();
		VMProvisioner provisioner = new VMProvisioner();
		provisioner.init();
		String instanceId = null;
		DescribeInstancesResult result = provisioner.ec2client.describeInstances();
		Iterator<Reservation> i = result.getReservations().iterator();
		while (i.hasNext()) {
			Reservation r = i.next();
			List<Instance> instances = r.getInstances();
			for (Instance ii : instances) {
				provisioner.log.Info(ii.getImageId() + "\t" + ii.getInstanceId() + "\t" + ii.getState().getName() + "\t" + ii.getPrivateDnsName());
				if (ii.getState().getName().equals("running")) {
					resultList.add(ii.getInstanceId());
				}
			}
		}
		return resultList;
	}

	public static ArrayList<String> getAvailableAndPendingInstIds() {
		ArrayList<String> resultList = new ArrayList<String>();
		VMProvisioner provisioner = new VMProvisioner();
		provisioner.init();
		String instanceId = null;
		DescribeInstancesResult result = provisioner.ec2client.describeInstances();
		Iterator<Reservation> i = result.getReservations().iterator();
		while (i.hasNext()) {
			Reservation r = i.next();
			List<Instance> instances = r.getInstances();
			// System.out.println("iiiiiiiiiiiiiiiiiiiiiinstances size: " + instances.size());
			for (Instance ii : instances) {
				provisioner.log.Info(ii.getImageId() + "\t" + ii.getInstanceId() + "\t" + ii.getState().getName() + "\t" + ii.getPrivateDnsName());
				if (ii.getState().getName().equals("running") || ii.getState().getName().equals("pending")) {
					// System.out.println("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaadding " +
					// ii.getInstanceId());
					resultList.add(ii.getInstanceId());
				}
			}
		}
		return resultList;
	}

	public static ArrayList<String> getIPaddresses(ArrayList<String> instIds) {
		ArrayList<String> resultList = new ArrayList<String>();
		VMProvisioner provisioner = new VMProvisioner();
		provisioner.init();
		String instanceId = null;
		DescribeInstancesResult result = provisioner.ec2client.describeInstances();
		Iterator<Reservation> i = result.getReservations().iterator();
		while (i.hasNext()) {
			Reservation r = i.next();
			List<Instance> instances = r.getInstances();
			for (Instance ii : instances) {
				for (String instId : instIds) {
					if (ii.getInstanceId().trim().equals(instId))
						resultList.add(ii.getPublicIpAddress());
				}
			}
		}
		return resultList;
	}

	public static ArrayList<String> getPendingVMs() {
		ArrayList<String> resultList = new ArrayList<String>();
		VMProvisioner provisioner = new VMProvisioner();
		provisioner.init();
		String instanceId = null;
		DescribeInstancesResult result = provisioner.ec2client.describeInstances();
		Iterator<Reservation> i = result.getReservations().iterator();
		while (i.hasNext()) {
			Reservation r = i.next();
			List<Instance> instances = r.getInstances();
			// System.out.println("iiiiiiiiiiiiiiiiiiiiiinstances size: " + instances.size());
			for (Instance ii : instances) {
				provisioner.log.Info(ii.getImageId() + "\t" + ii.getInstanceId() + "\t" + ii.getState().getName() + "\t" + ii.getPrivateDnsName());
				if (ii.getState().getName().equals("pending")) {
					// System.out.println("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaadding " +
					// ii.getInstanceId());
					resultList.add(ii.getInstanceId());
				}
			}
		}

		return resultList;
	}

	public static void waitOutPendingVMs() throws Exception {
		while (getPendingVMs().size() > 0) {
			System.out.println(getPendingVMs());
			Thread.sleep(500);
		}
	}

	public static void hostIsReachableSocket(String ip, int port) {
		String hostAddress = ip;
		long timeToRespond = 0; // in milliseconds

		try {

			// if (args.length == 1)
			// timeToRespond = test(hostAddress);
			// else
			// timeToRespond = test(hostAddress, port);
			timeToRespond = test(hostAddress, port);
		} catch (NumberFormatException e) {
			// System.out.println("Problem with arguments, usage: " + usage);
			e.printStackTrace();
		}

		if (timeToRespond >= 0)
			System.out.println(hostAddress + " responded in " + timeToRespond + " ms");
		else
			System.out.println("Failed");
	}

	static long test(String hostAddress, int port) {
		InetAddress inetAddress = null;
		InetSocketAddress socketAddress = null;
		SocketChannel sc = null;
		long timeToRespond = -1;
		Date start, stop;

		try {
			inetAddress = InetAddress.getByName(hostAddress);
		} catch (UnknownHostException e) {
			System.out.println("Problem, unknown host:");
			e.printStackTrace();
		}

		try {
			socketAddress = new InetSocketAddress(inetAddress, port);
		} catch (IllegalArgumentException e) {
			System.out.println("Problem, port may be invalid:");
			e.printStackTrace();
		}

		// Open the channel, set it to non-blocking, initiate connect
		try {
			sc = SocketChannel.open();
			sc.configureBlocking(true);
			start = new Date();
			if (sc.connect(socketAddress)) {
				stop = new Date();
				timeToRespond = (stop.getTime() - start.getTime());
			}
		} catch (IOException e) {
			System.out.println("Problem, connection could not be made:");
			e.printStackTrace();
		}

		try {
			sc.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return timeToRespond;
	}

	public static boolean pingAllIPs() throws Exception {

		for (String ipAddress : getAvailableVMIPs()) {
			hostIsReachableSocket(ipAddress, 22);

			// try {
			// InetAddress inet = InetAddress.getByName(ipAddress);
			// System.out.println("Sending Ping Request to " + ipAddress);
			//
			// boolean status = inet.isReachable(5000); // Timeout = 5000 milli seconds
			//
			// if (status) {
			// System.out.println("Status : Host is reachable");
			// } else {
			// System.out.println("Status : Host is not reachable");
			// return false;
			// }
			// } catch (UnknownHostException e) {
			// System.err.println("Host does not exists");
			// } catch (IOException e) {
			// System.err.println("Error in reaching the Host");
			// }
		}

		return true;
	}

}
