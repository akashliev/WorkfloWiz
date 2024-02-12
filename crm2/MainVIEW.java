package crm2;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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

public class MainVIEW {
	private Logger log = Logger.getInstance(MainVIEW.class);

	// require variable for every operation
	private String accessKey = "AKIAJ3PJN22GOJ5VXKGA";
	private String secretKey = "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";
	private AWSCredentials credentials;
	// private String endPoint ;
	private Region region;
	private AmazonEC2Client ec2client;
	private AmazonRDSClient rdsclient;

	// EC2 Security Group Variables
	private String groupName = "22_80_8080";
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
	private String keyName = "ec2-pair-12-11";
	// private String pemFilePath = "/home/andrey/Dropbox/1WayneState_Research/2014_SCC/6_Amazon/2_ec2_keyPair"; //
	// /Users/kpbird/Desktop
	// private String pemFileName = "pemFileName_12-17";

	// EC2 Instance variables
	// private String imageId ="ami-7748661e";
	private String imageId = "ami-7748661e";
	private String instanceType = "t1.micro";
	private String instanceName = "viewTestJava";

	public static void main(String[] args) {
		MainVIEW m = new MainVIEW();
		m.init();
		m.createEC2OnDemandInstance();

		// m.terminateInstance("i-a4696bd8");

	}

	private void init() {
		credentials = new BasicAWSCredentials(accessKey, secretKey);
		// end point for singapore
		// endPoint = "https://rds.ap-southeast-1.amazonaws.com";
		// regions for singapore
		region = Region.getRegion(Regions.US_EAST_1);
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

			// / Creating Tag for New Instance ////
			log.Info("Creating Tags for New Instance");
			CreateTagsRequest crt = new CreateTagsRequest();
			ArrayList<Tag> arrTag = new ArrayList<Tag>();
			arrTag.add(new Tag().withKey("Name").withValue(instanceName));
			crt.setTags(arrTag);

			ArrayList<String> arrInstances = new ArrayList<String>();
			arrInstances.add(instanceId);
			crt.setResources(arrInstances);
			ec2client.createTags(crt);

		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
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

}
