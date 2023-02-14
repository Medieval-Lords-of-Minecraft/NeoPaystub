package me.neoblade298.neopaystub.commands;

import me.neoblade298.neocore.bungee.util.Util;

import java.util.UUID;

import me.neoblade298.neocore.bungee.commands.Subcommand;
import me.neoblade298.neocore.shared.commands.Arg;
import me.neoblade298.neocore.shared.commands.SubcommandRunner;
import me.neoblade298.neopaystub.NeoPaystub;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class CmdPaystubPay extends Subcommand {

	public CmdPaystubPay(String key, String desc, String perm, SubcommandRunner runner) {
		super(key, desc, perm, runner);
		args.add(new Arg("player"), new Arg("amount"));
		color = ChatColor.DARK_RED;
	}

	@Override
	public void run(CommandSender s, String[] args) {
		UUID uuid = Util.getUniqueId(args[0]);
		if (uuid == null) {
			Util.msg(s, "&cThis player could not be found");
			return;
		}
		int amount = Integer.parseInt(args[1]);
		String display = Util.getUsername(uuid);
		Util.msg(s, "&7Attempting to pay user...");
		
		NeoPaystub.inst().getProxy().getScheduler().runAsync(NeoPaystub.inst(), () -> {
			if (NeoPaystub.pay(uuid, amount)) {
				Util.msg(s, "&7Successfully paid &c" + display + " &e$" + amount);
				ProxiedPlayer recipient = NeoPaystub.inst().getProxy().getPlayer(uuid);
				if (recipient != null) {
					Util.msg(recipient, "&7You just received &e$" + amount + " &7for usage in the MLMC shop! &c/paystub");
				}
				NeoPaystub.proxy().getPluginManager().dispatchCommand(NeoPaystub.proxy().getConsole(), "/mail " + display +
						" You received $" + amount + " for usage in the MLMC shop! /paystub");
			}
			else {
				Util.msg(s, "&cFailed to complete payment! Check the console");
			}
		});
	}

}
