package com.comze_instancelabs.colormatch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.Wool;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import com.google.common.collect.Maps;


public class Main extends JavaPlugin implements Listener {

	/*
	 * 
	 * SETUP
	 * 
	 * cm setmainlobby
	 * 
	 * for each new arena:
	 * 
	 * cm setlobby arena
	 * cm setup arena
	 * 
	 */
	public static Economy econ = null;
	
	public static HashMap<Player, String> arenap = new HashMap<Player, String>(); // player -> arena
	public static HashMap<String, String> arenap_ = new HashMap<String, String>(); // player -> arena
	public static HashMap<Player, ItemStack[]> pinv = new HashMap<Player, ItemStack[]>();
	public static HashMap<Player, String> lost = new HashMap<Player, String>();
	public static HashMap<Player, Integer> xpsecp = new HashMap<Player, Integer>();
	public static HashMap<String, Integer> a_round = new HashMap<String, Integer>();
	public static HashMap<String, Integer> a_n = new HashMap<String, Integer>();
	public static HashMap<String, Integer> a_currentw = new HashMap<String, Integer>();
	
	int rounds_per_game = 10;
	int minplayers = 4;

	
	boolean economy = true;
	int reward = 30;
	int itemid = 264;
	int itemamount = 1;
	
	@Override
	public void onEnable(){
		getServer().getPluginManager().registerEvents(this, this);
		
		getConfig().options().header("I recommend you to set auto_updating to true for possible future bugfixes.");
		getConfig().addDefault("config.auto_updating", true);
		getConfig().addDefault("config.rounds_per_game", 10);
		getConfig().addDefault("config.min_players", 4);
		getConfig().addDefault("config.use_economy", true);
		getConfig().addDefault("config.money_reward", 30);
		getConfig().addDefault("config.itemid", 264); // diamond
		getConfig().addDefault("config.itemamount", 1);
		getConfig().options().copyDefaults(true);
		this.saveConfig();
		
		getConfigVars();
		
		try{
			Metrics metrics = new Metrics(this);
			metrics.start();
		} catch (IOException e) { }
		
		if(getConfig().getBoolean("config.auto_updating")){
        	Updater updater = new Updater(this, 71774, this.getFile(), Updater.UpdateType.DEFAULT, false);
        }
		

		if(economy){
			if (!setupEconomy()) {
	            getLogger().severe(String.format("[%s] - No iConomy dependency found! Disabling Economy.", getDescription().getName()));
	            economy = false;
	        }
		}
	}
	
	private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }
	
	public void getConfigVars(){
		rounds_per_game = getConfig().getInt("config.rounds_per_game");
		minplayers = getConfig().getInt("config.min_players");
		reward = getConfig().getInt("config.money_reward");
		itemid = getConfig().getInt("config.itemid");
		itemamount = getConfig().getInt("config.itemamount");
		economy = getConfig().getBoolean("config.use_economy");
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("cm") || cmd.getName().equalsIgnoreCase("colormatch")) {
			if (args.length > 0) {
				String action = args[0];
				if (action.equalsIgnoreCase("createarena")) {
					// create arena
					if (args.length > 1) {
						if(sender.hasPermission("colormatch.setup")){
							String arenaname = args[1];
							getConfig().set(arenaname + ".name", arenaname);
							this.saveConfig();
							sender.sendMessage("§2Successfully saved arena.");	
						}
					}
				} /*
				 * else if (action.equalsIgnoreCase("setspawn")) { if
				 * (args.length > 1) { Player p = (Player) sender; String
				 * arenaname = args[1]; getConfig().set(arenaname +
				 * ".spawn.world", p.getWorld().getName());
				 * getConfig().set(arenaname + ".spawn.loc.x",
				 * p.getLocation().getBlockX()); getConfig().set(arenaname +
				 * ".spawn.loc.y", p.getLocation().getBlockY());
				 * getConfig().set(arenaname + ".spawn.loc.z",
				 * p.getLocation().getBlockZ()); this.saveConfig();
				 * sender.sendMessage("§2Successfully saved spawn."); } }
				 */else if (action.equalsIgnoreCase("setlobby")) {
					if (args.length > 1) {
						if(sender.hasPermission("colormatch.setup")){
							Player p = (Player) sender;
							String arenaname = args[1];
							getConfig().set(arenaname + ".lobby.world",
									p.getWorld().getName());
							getConfig().set(arenaname + ".lobby.loc.x",
									p.getLocation().getBlockX());
							getConfig().set(arenaname + ".lobby.loc.y",
									p.getLocation().getBlockY());
							getConfig().set(arenaname + ".lobby.loc.z",
									p.getLocation().getBlockZ());
							this.saveConfig();
							sender.sendMessage("§2Successfully saved lobby.");	
						}
					}
				} else if (action.equalsIgnoreCase("setup")) {
					if (args.length > 1) {
						if(sender.hasPermission("colormatch.setup")){
							Player p = (Player) sender;
							String arenaname = args[1];
							getConfig().set(arenaname + ".spawn.world",
									p.getWorld().getName());
							getConfig().set(arenaname + ".spawn.loc.x",
									p.getLocation().getBlockX());
							getConfig().set(arenaname + ".spawn.loc.y",
									p.getLocation().getBlockY());
							getConfig().set(arenaname + ".spawn.loc.z",
									p.getLocation().getBlockZ());
							this.saveConfig();
							sender.sendMessage("§6Successfully saved spawn. Now setting up, may §2lag§6 a little bit.");
							setup(p.getLocation(), this, arenaname);	
						}
					}
				} else if (action.equalsIgnoreCase("setmainlobby")) {
					if(sender.hasPermission("colormatch.setup")){
						Player p = (Player) sender;
						getConfig().set("mainlobby.world",
								p.getWorld().getName());
						getConfig().set("mainlobby.loc.x",
								p.getLocation().getBlockX());
						getConfig().set("mainlobby.loc.y",
								p.getLocation().getBlockY());
						getConfig().set("mainlobby.loc.z",
								p.getLocation().getBlockZ());
						this.saveConfig();
						sender.sendMessage("§2Successfully saved main lobby.");	
					}
				} else if (action.equalsIgnoreCase("leave")) {
					Player p = (Player) sender;
					if (arenap.containsKey(p)) {
						leaveArena(p, true);
					} else {
						p.sendMessage("§cYou don't seem to be in an arena right now.");
					}
				} else if(action.equalsIgnoreCase("join")){
					if(args.length > 1){
						if(isValidArena(args[1])){
							Sign s = null;
							try{
								s = this.getSignFromArena(args[1]);
							}catch(Exception e){
								getLogger().warning("No sign found for arena " + args[1] + ". May lead to errors.");
							}
							if(s != null){
								if(s.getLine(1).equalsIgnoreCase("§2[join]")){
									joinLobby((Player)sender, args[1]);
								}else{
									sender.sendMessage("§cThe arena appears to be ingame.");
								}
							}else{
								sender.sendMessage("§cThe arena appears to be invalid, because a join sign is missing.");
							}
						}else{
							sender.sendMessage("§cThe arena appears to be invalid.");
						}
					}
				} else if(action.equalsIgnoreCase("start")){
					if(args.length > 1){
						if(sender.hasPermission("colormatch.start")){
							final String arena = args[1];
							for (Player p_ : arenap.keySet()) {
								if (arenap.get(p_).equalsIgnoreCase(arena)) {
									final Player p__ = p_;
									Bukkit.getScheduler().runTaskLater(this, new Runnable() {
										public void run() {
											p__.teleport(getSpawnForPlayer(arena));
										}
									}, 5);
								}
							}
							Bukkit.getScheduler().runTaskLater(this, new Runnable() {
								public void run() {
									start(arena);
								}
							}, 10);	
						}
					}
				} else if(action.equalsIgnoreCase("reload")){
					if(sender.hasPermission("colormatch.reload")){
						this.reloadConfig();
						getConfigVars();	
					}
				} else if(action.equalsIgnoreCase("list")){
					if(sender.hasPermission("colormatch.list")){
						sender.sendMessage("§6-= Arenas =-");
						for(String arena : getConfig().getKeys(false)){
							if(!arena.equalsIgnoreCase("mainlobby")){
								sender.sendMessage("§2" + arena);
							}
						}
					}
				} else {
					sender.sendMessage("§6-= ColorMatch §2help: §6=-");
					sender.sendMessage("§2To §6setup the main lobby §2, type in §c/cm setmainlobby");
					sender.sendMessage("§2To §6setup §2a new arena, type in the following commands:");
					sender.sendMessage("§2/cm createarena [name]");
					sender.sendMessage("§2/cm setlobby [name] §6 - for the waiting lobby");
					sender.sendMessage("§2/cm setup [name]");
					sender.sendMessage("");
					sender.sendMessage("§2You can join with §c/cm join [name] §2and leave with §c/cm leave§2.");
					sender.sendMessage("§2You can force an arena to start with §c/cm start [name]§2.");
				}
			}
			return true;
		}
		return false;
	}

	@EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event){
		if(arenap_.containsKey(event.getPlayer().getName())){
			event.setCancelled(true);
		}
    }
	
	@EventHandler
    public void onHunger(FoodLevelChangeEvent event){
    	if(event.getEntity() instanceof Player){
    		Player p = (Player)event.getEntity();
    		if(arenap_.containsKey(p.getName())){
    			event.setCancelled(true);
    		}
    	}
    }
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		if (arenap_.containsKey(event.getPlayer().getName())) {
			if(lost.containsKey(event.getPlayer())){
				Location l = getSpawn(lost.get(event.getPlayer()));
				final Location spectatorlobby = new Location(l.getWorld(), l.getBlockX(), l.getBlockY() + 30, l.getBlockZ());
				if(event.getPlayer().getLocation().getBlockY() < spectatorlobby.getBlockY() || event.getPlayer().getLocation().getBlockY() > spectatorlobby.getBlockY()){
					final Player p = event.getPlayer();
					final float b = p.getLocation().getYaw();
					final float c = p.getLocation().getPitch();
					Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
						@Override
						public void run() {
							p.setAllowFlight(true);
							p.setFlying(true);
							p.teleport(new Location(p.getWorld(), p.getLocation().getBlockX(), spectatorlobby.getBlockY(), p.getLocation().getBlockZ(), b, c));
						}
					}, 5);
					p.sendMessage("§3You fell! Type §6/cm leave §3to leave.");
				}
			}
			if(event.getPlayer().getLocation().getBlockY() < getSpawn(arenap_.get(event.getPlayer().getName())).getBlockY() - 2){
				lost.put(event.getPlayer(), arenap.get(event.getPlayer()));
				final Player p__ = event.getPlayer();
				final String arena = arenap.get(event.getPlayer());
				Bukkit.getScheduler().runTaskLater(this, new Runnable() {
					public void run() {
						Location l = getSpawn(arena);
						p__.teleport(new Location(l.getWorld(), l.getBlockX(), l.getBlockY() + 30, l.getBlockZ()));
						p__.setAllowFlight(true);
						p__.setFlying(true);
					}
				}, 5);

				int count = 0;
				
				for(Player p : arenap.keySet()){
					if(arenap.get(p).equalsIgnoreCase(arena)){
						if(!lost.containsKey(p)){
							count++;
						}
					}
				}
				
				if(count == 1){
					// last man standing!
					stop(h.get(arena), arena);
				}
			}
		}
	}
	

    @EventHandler
    public void onSignUse(PlayerInteractEvent event){
    	if (event.hasBlock())
	    {
	        if (event.getClickedBlock().getType() == Material.SIGN_POST || event.getClickedBlock().getType() == Material.WALL_SIGN)
	        {
	            final Sign s = (Sign) event.getClickedBlock().getState();
	            if(s.getLine(0).toLowerCase().contains("colormatch")){
	            	if(s.getLine(1).equalsIgnoreCase("§2[join]")){
	            		joinLobby(event.getPlayer(), s.getLine(2));
	            	}
	            }
	        }
	    }
	}
    
    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        Player p = event.getPlayer();
        if(event.getLine(0).toLowerCase().equalsIgnoreCase("colormatch")){
        	if(event.getPlayer().hasPermission("cm.sign")){
	        	event.setLine(0, "§6§lColorMatch");
	        	if(!event.getLine(2).equalsIgnoreCase("")){
	        		String arena = event.getLine(2);
	        		if(isValidArena(arena)){
	        			getConfig().set(arena + ".sign.world", p.getWorld().getName());
	        			getConfig().set(arena + ".sign.loc.x", event.getBlock().getLocation().getBlockX());
						getConfig().set(arena + ".sign.loc.y", event.getBlock().getLocation().getBlockY());
						getConfig().set(arena + ".sign.loc.z", event.getBlock().getLocation().getBlockZ());
						this.saveConfig();
						p.sendMessage("§2Successfully created arena sign.");
	        		}else{
	        			p.sendMessage("§2The arena appears to be invalid (missing components or misstyped arena)!");
	        			event.getBlock().breakNaturally();
	        		}
	        		event.setLine(1, "§2[Join]");
	        		event.setLine(2, arena);
	        		event.setLine(3, "0/" + Integer.toString(this.minplayers));
	        	}
        	}
        }
	}

    public Sign getSignFromArena(String arena){
		Location b_ = new Location(getServer().getWorld(getConfig().getString(arena + ".sign.world")), getConfig().getInt(arena + ".sign.loc.x"), getConfig().getInt(arena + ".sign.loc.y"), getConfig().getInt(arena + ".sign.loc.z"));
    	BlockState bs = b_.getBlock().getState();
    	Sign s_ = null;
    	if(bs instanceof Sign){
    		s_ = (Sign)bs;
    	}else{
    		getLogger().info("Could not find sign: " + bs.getBlock().toString());
    	}
		return s_;
	}
    
	public Location getLobby(String arena) {
		Location ret = null;
		if (isValidArena(arena)) {
			ret = new Location(Bukkit.getWorld(getConfig().getString(
					arena + ".lobby.world")), getConfig().getInt(
					arena + ".lobby.loc.x"),
					getConfig().getInt(arena + ".lobby.loc.y"), getConfig().getInt(
							arena + ".lobby.loc.z"));
		}
		return ret;
	}

	public Location getMainLobby() {
		Location ret = new Location(Bukkit.getWorld(getConfig().getString(
				"mainlobby.world")), getConfig().getInt("mainlobby.loc.x"),
				getConfig().getInt("mainlobby.loc.y"), getConfig().getInt(
						"mainlobby.loc.z"));
		return ret;
	}

	public Location getSpawn(String arena) {
		Location ret = null;
		if (isValidArena(arena)) {
			ret = new Location(Bukkit.getWorld(getConfig().getString(
					arena + ".spawn.world")), getConfig().getInt(
					arena + ".spawn.loc.x"),
					getConfig().getInt(arena + ".spawn.loc.y"), getConfig().getInt(
							arena + ".spawn.loc.z"));
		}
		return ret;
	}
	
	public Location getSpawnForPlayer(String arena) {
		Location ret = null;
		if (isValidArena(arena)) {
			ret = new Location(Bukkit.getWorld(getConfig().getString(
					arena + ".spawn.world")), getConfig().getInt(
					arena + ".spawn.loc.x"),
					getConfig().getInt(arena + ".spawn.loc.y") + 2, getConfig().getInt(
							arena + ".spawn.loc.z"));
		}
		return ret;
	}

	public boolean isValidArena(String arena) {
		if (getConfig().isSet(arena + ".spawn") && getConfig().isSet(arena + ".lobby")) {
			return true;
		}
		return false;
	}

	
	public HashMap<Player, Boolean> winner = new HashMap<Player, Boolean>();
	
	public void leaveArena(final Player p, boolean flag) {
		Bukkit.getScheduler().runTaskLater(this, new Runnable() {
			public void run() {
				p.teleport(getMainLobby());
			}
		}, 5);
		
		try{
			lost.remove(p);
		}catch(Exception e){}
		
		p.setAllowFlight(false);

		String arena = arenap.get(p);

		if(flag){
			if (arenap.containsKey(p)) {
				arenap.remove(p);
			}	
		}
		if (arenap_.containsKey(p.getName())) {
			arenap_.remove(p.getName());
		}
		
		p.getInventory().setContents(pinv.get(p));
		
		//TODO try out
		if(winner.containsKey(p)){
			if(economy){
				EconomyResponse r = econ.depositPlayer(p.getName(), getConfig().getDouble("config.money_reward_per_game"));
    			if(!r.transactionSuccess()) {
    				getServer().getPlayer(p.getName()).sendMessage(String.format("An error occured: %s", r.errorMessage));
                }
			}else{
				p.getInventory().addItem(new ItemStack(Material.getMaterial(itemid), itemamount));
				p.updateInventory();
			}
		}
		
		int count = 0;
		for (Player p_ : arenap.keySet()) {
			if (arenap.get(p_).equalsIgnoreCase(arena)) {
				count++;
			}
		}
		
		if(count < 1){
			if(flag){
				stop(h.get(arena), arena);
			}
		}
	}

	public void joinLobby(final Player p, final String arena) {
		arenap.put(p, arena);
		pinv.put(p, p.getInventory().getContents());
		p.setGameMode(GameMode.SURVIVAL);
		p.getInventory().clear();
		p.updateInventory();
		Bukkit.getScheduler().runTaskLater(this, new Runnable() {
			public void run() {
				p.teleport(getLobby(arena));
			}
		}, 5);

		int count = 0;
		for (Player p_ : arenap.keySet()) {
			if (arenap.get(p_).equalsIgnoreCase(arena)) {
				count++;
			}
		}
		if (count > 3) {
			for (Player p_ : arenap.keySet()) {
				final Player p__ = p_;
				if (arenap.get(p_).equalsIgnoreCase(arena)) {
					Bukkit.getScheduler().runTaskLater(this, new Runnable() {
						public void run() {
							p__.teleport(getSpawnForPlayer(arena));
						}
					}, 5);
				}
			}
			Bukkit.getScheduler().runTaskLater(this, new Runnable() {
				public void run() {
					start(arena);
				}
			}, 10);
		}
		
		try{
			Sign s = this.getSignFromArena(arena);
			if(s != null){
				s.setLine(3, Integer.toString(count) + "/" + Integer.toString(this.minplayers));
				s.update();
			}
		}catch(Exception e){
			getLogger().warning("You forgot to set a sign for arena " + arena + "! This may lead to errors.");
		}
		
	}

	// COPIED FROM MINIGAMES PARTY
	public static void setup(Location start, Main main, String name_) {
		int x = start.getBlockX() - 32;
		int y = start.getBlockY();
		int y_ = start.getBlockY() - 4;
		int z = start.getBlockZ() - 32;

		int current = 0;
		int temp = 4;
		boolean cont = false;

		for (int i = 0; i < 16; i++) {
			for (int j = 0; j < 16; j++) {
				int x_ = x + i * 4;
				int z_ = z + j * 4;

				current = r.nextInt(colors.size());
				ints.add(current);

				for (int i_ = 0; i_ < 4; i_++) {
					for (int j_ = 0; j_ < 4; j_++) {
						Block b = start.getWorld().getBlockAt(new Location(start.getWorld(), x_ + i_, y, z_ + j_));
						Block b_ = start.getWorld().getBlockAt(new Location(start.getWorld(), x_ + i_, y_, z_ + j_));
						b_.setType(Material.GLOWSTONE);
						b.setType(Material.WOOL);
						b.setData(colors.get(current).getData());
					}
				}
			}
		}
	}

	final Main m = this;

	static ArrayList<Integer> ints = new ArrayList<Integer>();
	static ArrayList<DyeColor> colors = new ArrayList<DyeColor>(Arrays.asList(
			DyeColor.BLUE, DyeColor.RED, DyeColor.CYAN, DyeColor.BLACK,
			DyeColor.GREEN, DyeColor.YELLOW, DyeColor.ORANGE));
	static Random r = new Random();

	//TODO PUT THESE INTO A HASHMAP -> MULTIPLE ARENAS WONT WORK OTHERWISE
	//long n = 0;
	//int currentw = 0;
	
	final public HashMap<String, BukkitTask> h = new HashMap<String, BukkitTask>();
	
	public BukkitTask start(final String arena) {
		//setup arena
		a_round.put(arena, 0);
		a_n.put(arena, 0);
		a_currentw.put(arena, 0);
		
		// setup ints arraylist
		getAll(getSpawn(arena));
		
		Sign s = this.getSignFromArena(arena);
		if(s != null){
			s.setLine(1, "§4[Ingame]");
			s.update();
		}
		
		BukkitTask id__ = null;
		id__ = Bukkit.getServer().getScheduler().runTaskTimerAsynchronously(m, new Runnable() {
			@Override
			public void run(){
				a_round.put(arena, a_round.get(arena) + 1);
				int n = a_n.get(arena);
				if(a_round.get(arena) > rounds_per_game){
					a_round.put(arena, 0);
					stop(h.get(arena), arena);
				}
				
				final ArrayList<BukkitTask> tasks = new ArrayList<BukkitTask>();
				
				//currentw = r.nextInt(colors.size());
				a_currentw.put(arena, r.nextInt(colors.size()));
				int currentw = a_currentw.get(arena);
				for (final Player p : arenap.keySet()) {
					if (arenap.get(p).equalsIgnoreCase(arena)) {
						arenap_.put(p.getName(), arena);
						// set inventory and exp bar
						p.getInventory().clear();
						p.updateInventory();
						Wool w = new Wool();
						w.setColor(colors.get(currentw));

						p.setExp(0.97F);
						if(!xpsecp.containsKey(p)){
							xpsecp.put(p, 1);
						}
						tasks.add(Bukkit.getServer().getScheduler().runTaskTimer(m, new Runnable(){
							public void run(){
								int xpsec = xpsecp.get(p);
								p.setExp(1 - (0.16F * xpsec));
								xpsecp.put(p, xpsec + 1);
							}
						}, (40L -n) / 6, (40L -n) / 6));
						
						DyeColor dc = colors.get(currentw);
						ItemStack wool = new ItemStack(Material.WOOL, 1, dc.getData());
						ItemMeta m = wool.getItemMeta();
						m.setDisplayName(dyeToChat(dc) + dc.name());
						wool.setItemMeta(m);
						for (int i = 0; i < 9; i++) {
							p.getInventory().setItem(i, wool);
						}
						// p.getInventory().all(wool);
						p.updateInventory();
					}
				}
				// remove all wools except current one
				Bukkit.getServer().getScheduler().runTaskLater(m, new Runnable() {
					// Bukkit.getServer().getScheduler().runTaskLaterAsynchronously(m,
					// new Runnable(){
					public void run() {
						removeAllExceptOne(getSpawn(arena), arena);
						for(BukkitTask t : tasks){
							t.cancel();
						}
						for(Player p : xpsecp.keySet()){
							if(arenap.get(p).equalsIgnoreCase(arena)){
								xpsecp.put(p, 1);
							}
						}
					}
				}, 40L - n);

				// BukkitTask id =
				// Bukkit.getServer().getScheduler().runTaskLaterAsynchronously(m,
				// new Runnable() {
				BukkitTask id = Bukkit.getServer().getScheduler().runTaskLater(m, new Runnable() {
					@Override
					public void run() {
						reset(getSpawn(arena));
					}
				}, 120);
				// update count
				a_n.put(arena, a_n.get(arena) + 1);
			}
		}, 20, 140); // 7 seconds

		h.put(arena, id__);
		return id__;
	}

	public static void getAll(Location start) {
		ints.clear();

		int x = start.getBlockX() - 32;
		int y = start.getBlockY();
		int z = start.getBlockZ() - 32;

		int current = 0;
		int count = 0;

		for (int i = 0; i < 16; i++) {
			for (int j = 0; j < 16; j++) {
				int x_ = x + i * 4;
				int z_ = z + j * 4;

				Block b = start.getWorld().getBlockAt(
						new Location(start.getWorld(), x_, y, z_));

				ints.add((int) b.getData());
			}
		}
	}

	public void reset(final Location start) {
		try {
			final MassBlockUpdate mbu = CraftMassBlockUpdate.createMassBlockUpdater(this, start.getWorld());

			mbu.setRelightingStrategy(MassBlockUpdate.RelightingStrategy.NEVER);

			if (ints.size() < 1) {
				getAll(start);
			}

			int x = start.getBlockX() - 32;
			int y = start.getBlockY();
			int y_ = start.getBlockY() - 4;
			int z = start.getBlockZ() - 32;

			World w = start.getWorld();

			int current = 0;
			int count = 0;

			for (int i = 0; i < 16; i++) {
				for (int j = 0; j < 16; j++) {
					int x_ = x + i * 4;
					int z_ = z + j * 4;

					// current = r.nextInt(colors.size());
					current = ints.get(count);
					count += 1;

					for (int i_ = 0; i_ < 4; i_++) {
						for (int j_ = 0; j_ < 4; j_++) {
							// Block b = start.getWorld().getBlockAt(new
							// Location(start.getWorld(), x_ + i_, y, z_ + j_));

							mbu.setBlock(x_ + i_, y, z_ + j_, 35, current);
							mbu.setBlock(x_ + i_, y_, z_ + j_, 89);
							// b.setType(Material.WOOL);
							// b.setData((byte)current);
						}
					}
				}
			}

			mbu.notifyClients();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void removeAllExceptOne(Location start, String arena) {
		final MassBlockUpdate mbu = CraftMassBlockUpdate.createMassBlockUpdater(m, start.getWorld());

		mbu.setRelightingStrategy(MassBlockUpdate.RelightingStrategy.NEVER);

		int x = start.getBlockX() - 32;
		int y = start.getBlockY();
		int z = start.getBlockZ() - 32;
		Byte data = colors.get(a_currentw.get(arena)).getData();

		for (int i = 0; i < 64; i++) {
			for (int j = 0; j < 64; j++) {
				Block b = start.getWorld().getBlockAt(new Location(start.getWorld(), x + i, y, z + j));
				if (b.getData() != data) {
					// b.setType(Material.AIR);
					mbu.setBlock(x + i, y, z + j, 0);
				}
			}
		}

		mbu.notifyClients();
	}
	// [END] COPIED FROM MINIGAMESPARTY
	
	
	public void stop(BukkitTask t, String arena){
		try{
			t.cancel();
		}catch(Exception e){
			
		}
		
		getLogger().info(Integer.toString(arenap.size()));
		
		ArrayList<Player> torem = new ArrayList<Player>();
		determineWinners(arena);
		for(Player p : arenap.keySet()){
			if(arenap.get(p).equalsIgnoreCase(arena)){
				leaveArena(p, false);
				torem.add(p);
			}
		}
		
		for(Player p : torem){
			arenap.remove(p);
		}
		torem.clear();
		
		
		// bugfix
		/*for(Player p : lost.keySet()){
			if(lost.get(p).equalsIgnoreCase(arena)){
				try{
					leaveArena(p, false);
				}catch(Exception e){
					
				}
			}
		}*/
		
		winner.clear();
		
		Sign s = this.getSignFromArena(arena);
		if(s != null){
			s.setLine(1, "§2[Join]");
			s.setLine(3, "0/" + Integer.toString(minplayers));
			s.update();
		}
		
		h.remove(arena);
		
		// reset arena
		for(Player p : xpsecp.keySet()){
			xpsecp.put(p, 1);
		}
		a_round.put(arena, 0);
		a_n.put(arena, 0);
		a_currentw.put(arena, 0);
		
		reset(getSpawn(arena));
	}
	
	public void determineWinners(String arena){
		for(Player p : arenap.keySet()){
			if(arenap.get(p).equalsIgnoreCase(arena)){
				if(!lost.containsKey(p)){
					// this player is a winner
					p.sendMessage("§2You won this round, awesome man! Here, enjoy your reward.");
					winner.put(p, true);
				}else{
					lost.remove(p);
				}
			}
		}
	}
	
	
	
	
	private static Map<DyeColor, ChatColor> dyeChatMap;
    static
    {
            dyeChatMap = Maps.newHashMap();
            dyeChatMap.put(DyeColor.BLACK, ChatColor.DARK_GRAY);
            dyeChatMap.put(DyeColor.BLUE, ChatColor.DARK_BLUE);
            dyeChatMap.put(DyeColor.BROWN, ChatColor.GOLD);
            dyeChatMap.put(DyeColor.CYAN, ChatColor.AQUA);
            dyeChatMap.put(DyeColor.GRAY, ChatColor.GRAY);
            dyeChatMap.put(DyeColor.GREEN, ChatColor.DARK_GREEN);
            dyeChatMap.put(DyeColor.LIGHT_BLUE, ChatColor.BLUE);
            dyeChatMap.put(DyeColor.LIME, ChatColor.GREEN);
            dyeChatMap.put(DyeColor.MAGENTA, ChatColor.LIGHT_PURPLE);
            dyeChatMap.put(DyeColor.ORANGE, ChatColor.GOLD);
            dyeChatMap.put(DyeColor.PINK, ChatColor.LIGHT_PURPLE);
            dyeChatMap.put(DyeColor.PURPLE, ChatColor.DARK_PURPLE);
            dyeChatMap.put(DyeColor.RED, ChatColor.DARK_RED);
            dyeChatMap.put(DyeColor.SILVER, ChatColor.GRAY);
            dyeChatMap.put(DyeColor.WHITE, ChatColor.WHITE);
            dyeChatMap.put(DyeColor.YELLOW, ChatColor.YELLOW);
    }

    public static ChatColor dyeToChat(DyeColor dclr)
    {
            if (dyeChatMap.containsKey(dclr))
                    return dyeChatMap.get(dclr);
            return ChatColor.MAGIC;
    }
}
