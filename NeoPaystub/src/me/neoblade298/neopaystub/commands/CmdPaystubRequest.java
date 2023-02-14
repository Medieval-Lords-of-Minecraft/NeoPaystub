package me.neoblade298.neopaystub.commands;

import me.neoblade298.neocore.bungee.util.Util;
import me.neoblade298.neocore.bungee.commands.Subcommand;
import me.neoblade298.neocore.shared.commands.Arg;
import me.neoblade298.neocore.shared.commands.SubcommandRunner;
import me.neoblade298.neocore.shared.util.SharedUtil;
import me.neoblade298.neopaystub.NeoPaystub;
import me.neoblade298.neopaystub.PayRequest;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class CmdPaystubRequest extends Subcommand {

	public CmdPaystubRequest(String key, String desc, String perm, SubcommandRunner runner) {
		super(key, desc, perm, runner);
		args.add(new Arg("amount"), new Arg("note"));
		args.setMax(-1);
	}

	@Override
	public void run(CommandSender s, String[] args) {
		NeoPaystub.addRequest(new PayRequest((ProxiedPlayer) s, Integer.parseInt(args[0]), SharedUtil.connectArgs(args, 1)));
		Util.msg(s, "&7Successfully submitted pay request!");
		for (ProxiedPlayer p : NeoPaystub.proxy().getPlayers()) {
			if (p.hasPermission("neopaystub.admin")) {
				Util.msg(p, "&7A new pay request has been submitted! &c/paystub list&7!");
			}
		}
	}

}
