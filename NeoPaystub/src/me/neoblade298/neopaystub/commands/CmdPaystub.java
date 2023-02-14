package me.neoblade298.neopaystub.commands;

import me.neoblade298.neocore.bungee.util.Util;

import java.util.UUID;

import com.google.gson.JsonObject;

import me.neoblade298.neocore.bungee.commands.Subcommand;
import me.neoblade298.neocore.shared.commands.Arg;
import me.neoblade298.neocore.shared.commands.SubcommandRunner;
import me.neoblade298.neopaystub.NeoPaystub;
import me.neoblade298.neopaystub.PaystubAccount;
import me.neoblade298.neopaystub.PaystubIO;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.HoverEvent.Action;
import net.md_5.bungee.api.chat.hover.content.Text;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class CmdPaystub extends Subcommand {

	public CmdPaystub(String key, String desc, String perm, SubcommandRunner runner) {
		super(key, desc, perm, runner);
		args.add(new Arg("player", false));
	}

	@Override
	public void run(CommandSender s, String[] args) {
		if (args.length != 0) {
			ProxiedPlayer p = NeoPaystub.inst().getProxy().getPlayer("player");
			if (p == null) {
				Util.msg(s, "&cThat player isn't online!");
				return;
			}
			
			UUID uuid = p.getUniqueId();
			if (!PaystubIO.hasAccount(uuid)) {
				Util.msg(s, "&cThat player doesn't have an account yet!");
				return;
			}
			PaystubAccount acct = PaystubIO.getAccount(uuid);
			viewBalance(s, p, acct.getCodeId());
		}
		else {
			ProxiedPlayer p = (ProxiedPlayer) s;
			UUID uuid = p.getUniqueId();
			if (!PaystubIO.hasAccount(uuid)) {
				Util.msg(s, "&cYou don't have an account yet!");
				return;
			}
			PaystubAccount acct = PaystubIO.getAccount(uuid);
			viewBalance(s, p, acct.getCodeId());
		}
	}
	
	private void viewBalance(CommandSender s, ProxiedPlayer viewed, int id) {
		Util.msg(s, "&7Retrieving data...");
		NeoPaystub.inst().getProxy().getScheduler().runAsync(NeoPaystub.inst(), () -> {
			JsonObject data = NeoPaystub.viewBalance(id);
			Util.msg(s, "&7Player: &e" + viewed.getName());
			Util.msg(s, "&7Balance: &e" + data.get("amountRemaining").getAsInt());
			String code = data.get("code").getAsString();
			ComponentBuilder b = new ComponentBuilder("§7Code (Click for copyable text): §e" + code)
					.event(new HoverEvent(Action.SHOW_TEXT, new Text("Click to get code in your chat")))
					.event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, code));
			s.sendMessage(b.create());
		});
	}
}
