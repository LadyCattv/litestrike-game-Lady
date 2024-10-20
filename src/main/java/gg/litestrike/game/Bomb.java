package gg.litestrike.game;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class Bomb {
	static final int DETONATION_TIME = (20 * 40);

	public boolean is_detonated = false;
	public boolean is_broken = false;

	int timer = 0;

	BombLocation bomb_loc;

	protected static ItemStack bomb_item() {
		ItemStack item = new ItemStack(Material.POPPED_CHORUS_FRUIT);
		ItemMeta im = item.getItemMeta();

		// set item lore
		List<Component> lore = new ArrayList<Component>();
		lore.add(Component.text("This is the bomb. place it at the bomb sites").color(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
		lore.add(Component.text("** maybe some lore stuff here, ask mira or someone idk**").color(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
		im.lore(lore);

		im.setCustomModelData(3); // edit

		im.displayName(Component.text("The Bomb!!"));

		// these apis are deprecated:
		// Set<Material> can_place_set = new HashSet<Material>();
		// can_place_set.add(Material.TERRACOTTA);
		// im.setCanPlaceOn(can_place_set);

		item.setItemMeta(im);
		return item;
	}

	public void place_bomb(Block bomb_block) {
		if (!(bomb_loc instanceof InvItemBomb)) {
			Bukkit.getLogger().severe("ERROR: bomb got placed without being in a inventory before. Check the Bomb Logic!");
		}
		bomb_block.setType(Material.LIME_STAINED_GLASS);
		reset_bomb();
		bomb_loc = new PlacedBomb(bomb_block);

		Sound s = Sound.sound(Key.key("block.note_block.harp"), Sound.Source.AMBIENT, 1, 1);
		Bukkit.getServer().playSound(s, bomb_block.getX(), bomb_block.getY(), bomb_block.getZ());

		Bukkit.getServer().sendMessage(Component.text("The BOMB HAS BEEN PLACED"));

		new BukkitRunnable() {
			int timer = 0;
			@Override
			public void run() {
				if (is_broken || is_detonated || bomb_loc == null || !(bomb_loc instanceof PlacedBomb)) {
					cancel();
				}
				
				timer +=1;

				if (timer == DETONATION_TIME) {
					explode();
					cancel();
				}
			}
		}.runTaskTimer(Litestrike.getInstance(), 1, 1);
	}

	private void explode() {
		if (is_broken == true) {
			Bukkit.getLogger().severe("the bomb explode() method was called even tho it was already broken");
			return;
		}
		PlacedBomb b = (PlacedBomb) bomb_loc;
		is_detonated = true;

		// the explosion animation
		new BukkitRunnable() {
			int i = 0;
			@Override
			public void run() {
				b.block.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, b.block.getLocation(), 5);
				b.block.getWorld().playSound(Sound.sound(Key.key("block.note_block.harp"), Sound.Source.AMBIENT, 1, 1), b.block.getX(), b.block.getY(), b.block.getZ());
				for (Player p : Bukkit.getOnlinePlayers()) {
					double distance = p.getLocation().distance(b.block.getLocation());
					if (distance < 15) {
						p.setHealth(0);
					}
					if (distance < 30) {
						p.damage(30-distance);
					}
				}
				i+= 1;
				if (i == (20 * 4)) {
					cancel();
				}
			}
		}.runTaskTimer(Litestrike.getInstance(), 1, 1);
	}

	public void drop_bomb(Item item) {
		if (!(bomb_loc instanceof InvItemBomb)) {
			Bukkit.getLogger().severe("ERROR: bomb got dropped without being in a inventory before. Check the Bomb Logic!");
		}
		reset_bomb();
		bomb_loc = new DroppedBomb(item);

		item.setGlowing(true);
		item.setInvulnerable(true);
	}

	public void give_bomb(PlayerInventory inv) {
		// some sanity checks
		if (is_detonated == true || timer != 0 || bomb_loc instanceof InvItemBomb || bomb_loc instanceof PlacedBomb) {
			// bomb was in a invalid state
			Bukkit.getLogger().severe("\n\nError, the bomb was in a invalid state");
			Bukkit.getLogger().severe("tried to give the bomb" + this.toString());
			Bukkit.getLogger().severe("\n");
		}

		if (bomb_loc != null) {
			reset_bomb();
		}
		inv.addItem(bomb_item());
		bomb_loc = new InvItemBomb(inv);
	}

	public void reset_bomb() {
		bomb_loc.remove();
		bomb_loc = null;
		timer = 0;
		is_detonated = false;
		is_broken = false;
	}

	public String toString() {
		return "\nbreakdown of the bomb:\nbomb_state: " + bomb_loc.getClass() +
		"\nbomb_timer: " + timer +
		"\nis_bomb_detonated: " + is_detonated;
	}

}

// represented a location that a bomb can be at
interface BombLocation {
	public void remove();
}

class DroppedBomb implements BombLocation {
	Item item;
	public DroppedBomb(Item item) {
		this.item = item;
	}
	@Override
	public void remove() {
		item.remove();
	}
}

class PlacedBomb implements BombLocation {
	public Block block;
	public PlacedBomb(Block block) {
		this.block = block;
	}
	@Override
	public void remove() {
		block.setType(Material.AIR);
	}
}

class InvItemBomb implements BombLocation {
	PlayerInventory p_inv;
	public InvItemBomb(PlayerInventory p_inv) {
		this.p_inv = p_inv;
	}
	@Override
	public void remove() {
		p_inv.remove(Bomb.bomb_item());
	}
}
