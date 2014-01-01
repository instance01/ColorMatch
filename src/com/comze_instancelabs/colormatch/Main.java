package com.comze_instancelabs.colormatch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;

import org.bukkit.Bukkit;
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
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Wool;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;


public class Main extends JavaPlugin implements Listener {

	/*
	 * 
	 * SETUP
	 * 
	 * mp setmainlobby
	 * 
	 * for each new arena:
	 * 
	 * mp setlobby arena mp setup arena
	 */

	public static HashMap<Player, String> arenap = new HashMap<Player, String>();
	public static HashMap<Player, String> arenap_ = new HashMap<Player, String>();
	public static HashMap<Player, ItemStack[]> pinv = new HashMap<Player, ItemStack[]>();
	public static HashMap<Player, String> lost = new HashMap<Player, String>();

	int rounds_per_game = 10;
	int minplayers = 4;
	
	@Override
	public void onEnable(){
		getServer().getPluginManager().registerEvents(this, this);
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label,
			String[] args) {
		if (cmd.getName().equalsIgnoreCase("cm")) {
			if (args.length > 0) {
				String action = args[0];
				if (action.equalsIgnoreCase("createarena")) {
					// create arena
					if (args.length > 1) {
						String arenaname = args[1];
						getConfig().set(arenaname + ".name", arenaname);
						this.saveConfig();
						sender.sendMessage("§2Successfully saved arena.");
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
				} else if (action.equalsIgnoreCase("setup")) {
					if (args.length > 1) {
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
				} else if (action.equalsIgnoreCase("setmainlobby")) {
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
				} else if (action.equalsIgnoreCase("leave")) {
					Player p = (Player) sender;
					if (arenap.containsKey(p)) {
						leaveArena(p);
					} else {
						p.sendMessage("§cYo dawg you don't seem to be in an arena right now.");
					}
				} else if(action.equalsIgnoreCase("join")){
					if(args.length > 1){
						if(isValidArena(args[1])){
							Sign s = this.getSignFromArena(args[1]);
							if(s != null){
								if(s.getLine(1).equalsIgnoreCase("§2[join]")){
									joinLobby((Player)sender, args[1]);
								}else{
									sender.sendMessage("§cThe arena appears to be ingame.");
								}
							}
						}else{
							sender.sendMessage("§cThe arena appears to be invalid.");
						}
					}
				} else if(action.equalsIgnoreCase("start")){
					if(args.length > 1){
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
			}
			return true;
		}
		return false;
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		if (arenap_.containsKey(event.getPlayer())) {
			if(lost.containsKey(event.getPlayer())){
				Location l = getSpawn(lost.get(event.getPlayer()));
				final Location spectatorlobby = new Location(l.getWorld(), l.getBlockX(), l.getBlockY() + 30, l.getBlockZ());
				if(event.getPlayer().getLocation().getBlockY() < spectatorlobby.getBlockY() || event.getPlayer().getLocation().getBlockY() > spectatorlobby.getBlockY()){
					//current.spectate(event.getPlayer());
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
			if(event.getPlayer().getLocation().getBlockY() < getSpawn(arenap_.get(event.getPlayer())).getBlockY()){
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
	        	event.setLine(0, "§l§6ColorMatch");
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
		if (getConfig().isSet(arena + ".spawn")
				&& getConfig().isSet(arena + ".lobby")) {
			return true;
		}
		return false;
	}

	public void leaveArena(final Player p) {
		Bukkit.getScheduler().runTaskLater(this, new Runnable() {
			public void run() {
				p.teleport(getMainLobby());
			}
		}, 5);
		
		String arena = arenap.get(p);

		if (arenap.containsKey(p)) {
			arenap.remove(p);
		}
		if (arenap_.containsKey(p)) {
			arenap_.remove(p);
		}
		
		p.getInventory().setContents(pinv.get(p));
		
		int count = 0;
		for (Player p_ : arenap.keySet()) {
			if (arenap.get(p_).equalsIgnoreCase(arena)) {
				count++;
			}
		}
		
		if(count < 1){
			stop(h.get(arena), arena);
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
		
		Sign s = this.getSignFromArena(arena);
		if(s != null){
			s.setLine(3, Integer.toString(count) + "/" + Integer.toString(this.minplayers));
			s.update();
		}
	}

	// COPIED FROM MINIGAMES PARTY
	public static void setup(Location start, Main main, String name_) {
		int x = start.getBlockX() - 32;
		int y = start.getBlockY();
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
						Block b = start.getWorld().getBlockAt(
								new Location(start.getWorld(), x_ + i_, y, z_ + j_));
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

	long n = 0;
	int currentw = 0;

	int rounds = 0;
	
	final public HashMap<String, BukkitTask> h = new HashMap<String, BukkitTask>();
	
	public BukkitTask start(final String arena) {
		// setup ints arraylist
		getAll(getSpawn(arena));
		
		Sign s = this.getSignFromArena(arena);
		if(s != null){
			s.setLine(1, "§4[Ingame]");
			s.update();
		}
		
		BukkitTask id__ = null;
		id__ = Bukkit.getServer().getScheduler()
				.runTaskTimerAsynchronously(m, new Runnable() {
					@Override
					public void run() {
						rounds ++;
						
						if(rounds > rounds_per_game){
							rounds = 0;
							stop(h.get(arena), arena);
						}
						
						currentw = r.nextInt(colors.size());
						for (Player p : arenap.keySet()) {
							if (arenap.get(p).equalsIgnoreCase(arena)) {
								arenap_.put(p, arena);
								// set inventory and exp bar
								p.getInventory().clear();
								p.updateInventory();
								Wool w = new Wool();
								w.setColor(colors.get(currentw));

								ItemStack wool = new ItemStack(Material.WOOL,
										1, colors.get(currentw).getData());
								// p.getInventory().all(wool);
								for (int i = 0; i < 9; i++) {
									p.getInventory().setItem(i, wool);
								}
								p.updateInventory();
							}
						}
						// remove all wools except current one
						Bukkit.getServer().getScheduler()
								.runTaskLater(m, new Runnable() {
									// Bukkit.getServer().getScheduler().runTaskLaterAsynchronously(m,
									// new Runnable(){
									public void run() {
										removeAllExceptOne(getSpawn(arena),
												currentw);
									}
								}, 40L - n);

						// BukkitTask id =
						// Bukkit.getServer().getScheduler().runTaskLaterAsynchronously(m,
						// new Runnable() {
						BukkitTask id = Bukkit.getServer().getScheduler()
								.runTaskLater(m, new Runnable() {
									@Override
									public void run() {
										reset(getSpawn(arena));
									}
								}, 120);
						// update count
						n -= 1;
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
			final MassBlockUpdate mbu = CraftMassBlockUpdate
					.createMassBlockUpdater(this, start.getWorld());

			mbu.setRelightingStrategy(MassBlockUpdate.RelightingStrategy.NEVER);

			if (ints.size() < 1) {
				getAll(start);
			}

			int x = start.getBlockX() - 32;
			int y = start.getBlockY();
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

							// b.setType(Material.WOOL);
							// b.setData((byte)current);
						}
					}
				}
			}

			// sendClientChanges(start, test);
			mbu.notifyClients();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void removeAllExceptOne(Location start, int exception) {
		final MassBlockUpdate mbu = CraftMassBlockUpdate
				.createMassBlockUpdater(m, start.getWorld());

		mbu.setRelightingStrategy(MassBlockUpdate.RelightingStrategy.NEVER);

		int x = start.getBlockX() - 32;
		int y = start.getBlockY();
		int z = start.getBlockZ() - 32;
		Byte data = colors.get(currentw).getData();

		for (int i = 0; i < 64; i++) {
			for (int j = 0; j < 64; j++) {
				Block b = start.getWorld().getBlockAt(
						new Location(start.getWorld(), x + i, y, z + j));
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
		determineWinners(arena);
		for(Player p : arenap.keySet()){
			if(arenap.get(p).equalsIgnoreCase(arena)){
				leaveArena(p);
			}
		}
		
		Sign s = this.getSignFromArena(arena);
		if(s != null){
			s.setLine(1, "§2[Join]");
			s.setLine(3, "0/" + Integer.toString(minplayers));
			s.update();
		}
		
		h.remove(arena);
		
		reset(getSpawn(arena));
	}
	
	public void determineWinners(String arena){
		for(Player p : arenap.keySet()){
			if(arenap.get(p).equalsIgnoreCase(arena)){
				if(!lost.containsKey(p)){
					// this player is a winner
					p.sendMessage("§2You won this round, awesome man! Here, enjoy your diamond.");
					p.getInventory().addItem(new ItemStack(Material.DIAMOND));
					p.updateInventory();
				}else{
					lost.remove(p);
				}
			}
		}
	}
}
