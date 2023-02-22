package me.neoblade298.neopaystub.commands;

import me.neoblade298.neocore.bungee.util.Util;
import me.neoblade298.neocore.bungee.commands.Subcommand;
import me.neoblade298.neocore.shared.commands.Arg;
import me.neoblade298.neocore.shared.commands.SubcommandRunner;
import me.neoblade298.neopaystub.NeoPaystub;
import me.neoblade298.neopaystub.PayRequest;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class CmdPaystubApprove extends Subcommand {

	public CmdPaystubApprove(String key, String desc, String perm, SubcommandRunner runner) {
		super(key, desc, perm, runner);
		args.add(new Arg("id"));
		color = ChatColor.DARK_RED;
	}

	@Override
	public void run(CommandSender s, String[] args) {
		int id = Integer.parseInt(args[0]);
		PayRequest req = NeoPaystub.getRequests().get((r) -> {
			return id - r.getId();
		});
		if (req == null) {
			Util.msg(s, "&cThat pay request doesn't exist! Maybe it was already processed?");
			return;
		}
		if (req.isProcessed()) {
			Util.msg(s, "&cThat pay request was already processed!");
			return;
		}
		req.process(true);
		Util.msg(s, "&7Attempting to pay &c" + req.getDisplay() + "&7...");
		
		NeoPaystub.inst().getProxy().getScheduler().runAsync(NeoPaystub.inst(), () -> {
			if (NeoPaystub.pay(req.getUniqueId(), req.getAmount())) {
				Util.msg(s, "&7Successfully paid &c" + req.getDisplay() + " &e$" + req.getAmount() + " &7for " + req.getNote());
				ProxiedPlayer recipient = NeoPaystub.inst().getProxy().getPlayer(req.getUniqueId());
				if (recipient != null) {
					Util.msg(recipient, "&7You just received &e$" + req.getAmount() + " &7for " + req.getNote() + "&7! &c/paystub");
				}
				NeoPaystub.proxy().getPluginManager().dispatchCommand(NeoPaystub.proxy().getConsole(), "mail " + req.getDisplay() +
						" Your pay request " + req.getId() + " (" + req.getNote() + ") was approved. /paystub");
				NeoPaystub.getRequests().remove((r) -> {
					return id - r.getId();
				});
			}
			else {
				req.revertProcess();
				Util.msg(s, "&cFailed to complete payment! Check the console");
			}
		});
	}

}
