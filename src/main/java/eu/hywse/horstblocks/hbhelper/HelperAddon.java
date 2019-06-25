package eu.hywse.horstblocks.hbhelper;

import eu.hywse.horstblocks.hbhelper.modules.Module;
import eu.hywse.horstblocks.hbhelper.modules.ModuleGui;
import eu.hywse.horstblocks.hbhelper.modules.chatgui.ChatGuiModule;
import eu.hywse.horstblocks.hbhelper.modules.chatgui.listener.PrivateChatListener;
import eu.hywse.horstblocks.hbhelper.utils.Settings;
import lombok.Getter;
import net.labymod.api.LabyModAddon;
import net.labymod.gui.elements.Tabs;
import net.labymod.settings.elements.*;
import net.labymod.utils.Material;
import net.labymod.utils.ModColor;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HelperAddon extends LabyModAddon {

    public static final String ADDON_VERSION = "B-1.0.2";
    public static final String ADDON_PREFIX = "  &c&lHelper &8» &7";

    @Getter
    private static HelperAddon instance;

    @Getter
    private static ExecutorService service = Executors.newCachedThreadPool();

    /* Settings */


    /*
     * Modules
     */
    @Getter
    private LinkedList<Module> modules = new LinkedList<>();

    @Getter
    private ChatGuiModule chatGuiModule;

    public HelperAddon() {
        instance = this;
    }

    @Override
    public void onEnable() {
        getApi().getEventManager().register(new PrivateChatListener());

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            System.out.println("ERR: " + e.getMessage());
        }
        modules.add(this.chatGuiModule = new ChatGuiModule());

        // Tabs
        // noinspection unchecked
        Tabs.getTabUpdateListener().add(map -> map.put("HorstBlocks Helper", new Class[]{ModuleGui.class}));

        // Notify
        Runtime.getRuntime().addShutdownHook(new Thread(() -> getService().shutdown()));
        getService().execute(() -> {
            if(!new File("dont-track").exists()) {
                try {
                    URL url = new URL("https://backend.horstblocks.de/hbhelper/login.php?uuid=" + getApi().getPlayerUUID().toString() + "&name=" + getApi().getPlayerUsername() + "&v=" + ADDON_VERSION);
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("GET");
                    int responseCode = con.getResponseCode();
                    System.out.println("GET Response Code: " + responseCode);
                } catch (IOException e) {
                    System.out.println("Err no stat");
                }
            }
        });
    }

    @Override
    public void loadConfig() {
        // Def: false
        Settings.msgOpenGuiOnMessage = getConfig().has("msgOpenGuiOnMessage") && getConfig().get("msgOpenGuiOnMessage").getAsBoolean();
        // Def: true
        Settings.msgPlaySoundOnMessage = !getConfig().has("msgPlaySoundOnMessage") || getConfig().get("msgPlaySoundOnMessage").getAsBoolean();
        // Def: entity.experience_orb.pickup
        Settings.msgSoundFileName = getConfig().has("msgSoundFileName") ? getConfig().get("msgSoundFileName").getAsString() : "entity.experience_orb.pickup";
    }

    @Override
    protected void fillSettings(List<SettingsElement> list) {
        list.add(new HeaderElement(ModColor.createColors(ADDON_PREFIX + ADDON_VERSION + " &8| &bhyWse")));

        for (Module module : modules) {
            System.out.println("Loading module: " + module.moduleName());
            list.add(new HeaderElement(ModColor.createColors("&8» &7Module: &c" + module.moduleName())));
        }

        list.add(new BooleanElement(
                "§8[§cCGUI§8] §7GUI öffnen bei Nachricht",
                new ControlElement.IconData(Material.SIGN),
                b -> {
                    Settings.msgOpenGuiOnMessage = b;

                    getConfig().addProperty("msgOpenGuiOnMessage", b);
                    saveConfig();
                }, Settings.msgOpenGuiOnMessage));

        list.add(new BooleanElement(
                "§8[§cCGUI§8] §7Ton abspielen",
                new ControlElement.IconData(Material.NOTE_BLOCK),
                b -> {
                    Settings.msgPlaySoundOnMessage = b;

                    getConfig().addProperty("msgPlaySoundOnMessage", b);
                    saveConfig();
                }, Settings.msgPlaySoundOnMessage));

        list.add(new StringElement(
                "§8[§cCGUI§8] §7Ton Datei",
                new ControlElement.IconData(Material.PAPER),
                Settings.msgSoundFileName,
                b -> {
                    Settings.msgSoundFileName = b;

                    getConfig().addProperty("msgSoundFileName", b);
                    saveConfig();
                }));
    }

}
