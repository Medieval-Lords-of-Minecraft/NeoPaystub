package me.neoblade298.neopaystub.commands;

import me.neoblade298.neocore.bungee.util.Util;
import me.neoblade298.neocore.bungee.commands.Subcommand;
import me.neoblade298.neocore.shared.commands.Arg;
import me.neoblade298.neocore.shared.commands.SubcommandRunner;
import me.neoblade298.neocore.shared.util.SharedUtil;
import me.neoblade298.neopaystub.NeoPaystub;
import me.neoblade298.neopaystub.PayRequest;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class CmdPaystubDeny extends Subcommand {

	public CmdPaystubDeny(String key, String desc, String perm, SubcommandRunner runner) {
		super(key, desc, perm, runner);
		args.add(new Arg("id"), new Arg("reason", false));
		args.setMax(-1);
		color = ChatColor.DARK_RED;
	}

	@Override
	public void run(CommandSender s, String[] args) {
		int id = Integer.parseInt(args[0]);
		String reason = args.length > 1 ? SharedUtil.connectArgs(args, 1) : "No reason provided";
		PayRequest req = NeoPaystub.getRequests().remove((r) -> {
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
		req.process(false);
		Util.msg(s, "&cDenied pay request " + req.getId());
		ProxiedPlayer recipient = NeoPaystub.inst().getProxy().getPlayer(req.getUniqueId());
		if (recipient != null) {
			Util.msg(recipient, "&cYour pay request was just denied for: " + reason);
		}
		NeoPaystub.proxy().getPluginManager().dispatchCommand(NeoPaystub.proxy().getConsole(), "/mail " + req.getDisplay() +
				" Your pay request " + req.getId() + " (" + req.getNote() + ") was denied for: " + reason);
	}

}
