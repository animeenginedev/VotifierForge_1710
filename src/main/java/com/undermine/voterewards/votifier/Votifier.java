package com.undermine.voterewards.votifier;


import java.io.*;
import java.security.KeyPair;

import org.apache.logging.log4j.Level;

import com.undermine.voterewards.VoteRewardsMod;
import com.undermine.voterewards.votifier.crypto.RSAIO;
import com.undermine.voterewards.votifier.crypto.RSAKeygen;
import com.undermine.voterewards.votifier.net.VoteReceiver;

import cpw.mods.fml.common.FMLLog;

/**
 * The main Votifier plugin class.
 * 
 * @author Blake Beaupain
 * @author Kramer Campbell
 * 
 * modified by
 * @author kadzu
 */
public class Votifier {
	private static Votifier instance;

	private String version;

	private VoteReceiver voteReceiver;

	private KeyPair keyPair;


	public void initialise() {
		Votifier.instance = this;
				
		version = VoteRewardsMod.VERSION;
		
		boolean MadeRSADirectory = new File(VoteRewardsMod.CONFIG_FOLDER + "/rsa").mkdirs();
		File rsaDirectory = new File(VoteRewardsMod.CONFIG_FOLDER + "/rsa");

		try {
			if (MadeRSADirectory) {
				rsaDirectory.mkdir();
				keyPair = RSAKeygen.generate(2048);
				RSAIO.save(rsaDirectory, keyPair);
			} else {
				keyPair = RSAIO.load(rsaDirectory);
			}
		} catch (Exception ex) {
			FMLLog.log(Level.ERROR, "Error reading configuration file or RSA keys");
			gracefulExit();
			return;
		}

		// Initialize the receiver.
		String host = VoteRewardsMod.VotifierIP;
		int port = VoteRewardsMod.VotifierPort;

		try {
			voteReceiver = new VoteReceiver(host, port);
			voteReceiver.start();
		} catch (Exception ex) {
			gracefulExit();
			return;
		}
	}
	
	private void gracefulExit() {
		FMLLog.log(Level.ERROR, "Votifier did not initialize properly!");
	}

	public static Votifier getInstance() {
		return instance;
	}

	public String getVersion() {
		return version;
	}

	public VoteReceiver getVoteReceiver() {
		return voteReceiver;
	}

	public KeyPair getKeyPair() {
		return keyPair;
	}
}