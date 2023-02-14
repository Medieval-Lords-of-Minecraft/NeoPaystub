package me.neoblade298.neopaystub.commands;

import me.neoblade298.neocore.bungee.util.Util;

import me.neoblade298.neocore.bungee.commands.Subcommand;
import me.neoblade298.neocore.shared.commands.Arg;
import me.neoblade298.neocore.shared.commands.SubcommandRunner;
import me.neoblade298.neocore.shared.util.PaginatedList;
import me.neoblade298.neopaystub.NeoPaystub;
import me.neoblade298.neopaystub.PayRequest;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.hover.content.Text;
import net.md_5.bungee.api.chat.ComponentBuilder.FormatRetention;

public class CmdPaystubList extends Subcommand {

	public CmdPaystubList(String key, String desc, String perm, SubcommandRunner runner) {
		super(key, desc, perm, runner);
		args.add(new Arg("page", false));
		color = ChatColor.DARK_RED;
	}

	@Override
	public void run(CommandSender s, String[] args) {
		PaginatedList<PayRequest> requests = NeoPaystub.getRequests();
		
		if (requests.size() == 0) {
			Util.msg(s, "&7No requests at the moment!");
			return;
		}
		
		if (args.length == 0) {
			showRequests(s, requests, 1);
		}
		else {
			int page = Integer.parseInt(args[0]);
			if (requests.pages() >= page) {
				Util.msg(s, "&cThere aren't that many pages!");
				return;
			}

			showRequests(s, requests, page);
		}
	}
	
	private void showRequests(CommandSender s, PaginatedList<PayRequest> requests, int page) {
		ComponentBuilder b = new ComponentBuilder();
		Util.msg(s, "&7===== Requests =====");
		boolean first = true;
		for (PayRequest req : requests.get(0)) {
			buildRequest(b, req, first);
			first = false;
		}
		s.sendMessage(b.create());
		s.sendMessage(requests.getFooter(page - 1, "/paystub requests " + (page+1), "/paystub requests " + (page-1)));
	}
	
	private void buildRequest(ComponentBuilder b, PayRequest req, boolean first) {
		if (!first) {
			b.append("\n", FormatRetention.NONE);
		}
		b.append("§4§l#" + req.getId() + ": §e" + req.getDisplay() + " §7(§e$" + req.getAmount() + "§7) - " + req.getNote() + " ")
		.append("§7[§aApprove§7] " , FormatRetention.NONE)
		.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("/paystub approve " + req.getId())))
		.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/paystub approve " + req.getId()))
		.append("§7[§cDeny§7]" , FormatRetention.NONE)
		.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("/paystub deny " + req.getId())))
		.event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/paystub deny " + req.getId() + " "));
	}
}
