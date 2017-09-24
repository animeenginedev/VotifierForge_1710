package com.undermine.voterewards;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import com.undermine.voterewards.votifier.model.Vote;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.ServerTickEvent;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.ClickEvent.Action;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;

public class ServerTickHandler {
    int tickCurrent = 0;
    int tickVoteReminderCurrent = 0;
    
    static ArrayList<ChatComponentText> VotingWebsites;
    
    static private ChatComponentText GetLink(String name, String url) {
		ChatComponentText link = new ChatComponentText(name);
		ClickEvent clickEvent = new ClickEvent(Action.OPEN_URL, url);
		link.getChatStyle().setChatClickEvent(clickEvent);
		link.getChatStyle().setColor(EnumChatFormatting.GOLD);
		ChatComponentText HoverText = new ChatComponentText(url);
		HoverText.getChatStyle().setColor(EnumChatFormatting.GOLD);
		HoverEvent hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, HoverText);
		link.getChatStyle().setChatHoverEvent(hoverEvent);
		
		return link;
    }
    
    static void InitializeVotingWebsites() {
    	VotingWebsites = new ArrayList<ChatComponentText>();
    	
    	for(int i = 0; i < VoteRewardsMod.VoteWebsites.length; i += 2) {
        	VotingWebsites.add(GetLink(VoteRewardsMod.VoteWebsites[i], VoteRewardsMod.VoteWebsites[i + 1]));
    	}
    }
    
    public void Attach(String section, ChatComponentText Parent) {
    	if(section.contains("<SERVICE_LIST>")) {
			for(int i = 0; i < VotingWebsites.size(); i++) {
				Parent.appendSibling(VotingWebsites.get(i));
				ChatComponentText comma = new ChatComponentText(", ");
				comma.getChatStyle().setColor(EnumChatFormatting.GRAY);
				Parent.appendSibling(comma);
			}
    		return;
    	}
		
		ChatStyle Style = new ChatStyle();
    	if(section.contains("<GRAY>")) {
    		section = section.replace("<GRAY>", "");
    		Style.setColor(EnumChatFormatting.GRAY);
    	} else if(section.contains("<GREEN>")) {
    		section = section.replace("<GREEN>", "");    		
    		Style.setColor(EnumChatFormatting.GREEN);
    	} else if(section.contains("<GOLD>")) {
    		section = section.replace("<GOLD>", "");    		
    		Style.setColor(EnumChatFormatting.GOLD);
    	}
    	
		ChatComponentText child = new ChatComponentText(section);
		child.setChatStyle(Style);
		Parent.appendSibling(child);
    }
    
    @SubscribeEvent
    public void onServerTick(ServerTickEvent event) {

    	if(event.side == Side.SERVER && event.phase == TickEvent.Phase.END) {
    		tickCurrent++;
    		if(tickCurrent >= VoteRewardsMod.VoteCheckInterval) {
        		tickVoteReminderCurrent += tickCurrent;
    			tickCurrent = 0;
    			
    			while(! VoteRewardsMod.CurrentVotes.isEmpty()) {
    				Vote v = new Vote();
					try {
						v = VoteRewardsMod.CurrentVotes.poll(10, TimeUnit.MILLISECONDS);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

					String ThankString = VoteRewardsMod.VoteThank;
					ThankString = ThankString.replace("<NAME>",  v.getUsername());
					ThankString = ThankString.replace("<SERVICE>",  v.getServiceName());
			    	
					ChatComponentText msg = new ChatComponentText("");
					String[] Tokens = ThankString.split("(?=<GRAY>)|(?=<GOLD>)|(?=<GREEN>)|(?=<SERVICE_LIST>)");
					for(int i = 0; i < Tokens.length; i++) {
						Attach(Tokens[i], msg);
					}
					
					FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().sendChatMsg(msg);
					FMLCommonHandler.instance().getMinecraftServerInstance().getCommandManager().executeCommand(FMLCommonHandler.instance().getMinecraftServerInstance(), VoteRewardsMod.VoteCommand.replace("<NAME>", v.getUsername()));
    			}
        		if(tickVoteReminderCurrent >= VoteRewardsMod.VoteRemindInterval) {
        			tickVoteReminderCurrent = 0;

    				String ThankString = VoteRewardsMod.VoteRemind;
    		    	
    				ChatComponentText VoteRemind = new ChatComponentText("");
    				String[] Tokens = ThankString.split("(?=<GRAY>)|(?=<GOLD>)|(?=<GREEN>)|(?=<SERVICE_LIST>)");
    				for(int i = 0; i < Tokens.length; i++) {
    					Attach(Tokens[i], VoteRemind);
    				}
    				
    				FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().sendChatMsg(VoteRemind);
        		}
    		}
    	}
    }
}
