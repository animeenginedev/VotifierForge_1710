package com.undermine.voterewards;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;

public class CommandReload implements ICommand {
    private final List<String> aliases;
  
    public CommandReload() 
    { 
        aliases = new ArrayList<String>(); 

        aliases.add("reloadvr"); 
        aliases.add("reloadvoterewards"); 

    } 
  

	@Override
	public int compareTo(Object arg0) {
		return 0;
	}

	@Override
	public String getCommandName() {
		return "reloadvoterewards";
	}

	@Override
	public String getCommandUsage(ICommandSender p_71518_1_) {
		return "reloadvoterewards";
	}

	@Override
	public List<String> getCommandAliases() {
        return this.aliases;
	}

	@Override
	public void processCommand(ICommandSender p_71515_1_, String[] p_71515_2_) {
    	VoteRewardsMod.LoadConfig();
		ServerTickHandler.InitializeVotingWebsites();		
	}

	@Override
	public boolean canCommandSenderUseCommand(ICommandSender sender) {
		if(sender instanceof EntityPlayer) {
			return MinecraftServer.getServer().getConfigurationManager().func_152596_g(((EntityPlayer) sender).getGameProfile());
		}
        return true;
	}

	@Override
	public List<?> addTabCompletionOptions(ICommandSender p_71516_1_, String[] p_71516_2_) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isUsernameIndex(String[] p_82358_1_, int p_82358_2_) {
		// TODO Auto-generated method stub
		return false;
	}

}
