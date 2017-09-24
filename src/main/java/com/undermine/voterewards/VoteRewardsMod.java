package com.undermine.voterewards;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.undermine.voterewards.votifier.Votifier;
import com.undermine.voterewards.votifier.model.Vote;


import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;

@Mod(modid = VoteRewardsMod.MODID, version = VoteRewardsMod.VERSION, acceptableRemoteVersions = "*")
public class VoteRewardsMod
{
    public static final String MODID = "voterewards";
    public static final String VERSION = "1.0";
    
    public static String CONFIG_FOLDER;
    
    public static Configuration config;
    
    //Lazy Config
    public static String VotifierIP;
    public static int VotifierPort;    
    public static String[] VoteWebsites;
    public static String VoteRemind;
    public static String VoteThank;
    public static String VoteCommand;    
    public static int VoteCheckInterval;
    public static int VoteRemindInterval;
    
    public static BlockingQueue<Vote> CurrentVotes;
    
    static void LoadConfig() {    	
    	config.load();
    	VotifierIP = config.get("Votifier", "Host", "0.0.0.0").getString();
    	VotifierPort = config.get("Votifier", "Port", 8192).getInt();
    	
    	VoteWebsites = config.getStringList("Votifier", "VotingWebsites", new String[]{"ExampleWebsite.org [LINK]", "ExampleWebsite.org/ServerVoteLink", "ExampleWebsite.com [LINK]", "ExampleWebsite.com/ServerVoteLink"}, "ServiceName,VotingLink,so on");
    	    	
    	VoteRemind = config.get("Votifier", "Remind", "<GRAY>Please remember to vote @ <SERVICE_LIST> <GRAY>and earn <GOLD>100 <GRAY>CashDollas.").getString();
    	VoteThank = config.get("Votifier", "Thank", "<GREEN><NAME> <GRAY>just voted on <SERVICE> and earned <GOLD>100 <GRAY>CashDollas.").getString();
    	VoteCommand = config.get("Votifier", "Command", "wallet <NAME> add 100").getString();

    	VoteCheckInterval = config.get("Votifier", "VoteCheck", 50, "2.5 Seconds").getInt();
    	VoteRemindInterval = config.get("Votifier", "VoteRemind", 20*60*60, "1 Hour").getInt();
    	config.save();    	
    }
    
    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
    	config = new Configuration(event.getSuggestedConfigurationFile());
    	LoadConfig();
    	
    	CurrentVotes = new LinkedBlockingQueue<Vote>();
    	CONFIG_FOLDER = event.getModConfigurationDirectory().getPath() + "/VoteRewards/";
    	ServerTickHandler.InitializeVotingWebsites();
    	
    	Votifier vt = new Votifier();
    	vt.initialise();
    	
        FMLCommonHandler.instance().bus().register(new ServerTickHandler());
    }
    
    @EventHandler
    public void serverStart(FMLServerStartingEvent event) {
    	event.registerServerCommand(new CommandReload());
    }
}
