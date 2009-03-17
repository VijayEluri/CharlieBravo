/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.md87.charliebravo;

import com.dmdirc.util.ConfigFile;
import com.dmdirc.util.InvalidConfigFileException;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author chris
 */
public class Config implements Runnable {
    
    private static final String FS = System.getProperty("file.separator");

    private static final String FILE = System.getProperty("user.home") + FS + ".charliebravo";

    private static final List<String> SETTINGS = Arrays.asList(new String[] {
        "eve.apikey:String", "eve.userid:int", "eve.charid:int",
        "admin.level:int",
        "internal.lastseen:int", "internal.lastuser:String"
    });

    protected final ConfigFile configfile;

    public Config() {
        final File file = new File(FILE);

        configfile = new ConfigFile(file);
        configfile.setAutomake(true);

        try {
            if (file.exists()) {
                configfile.read();
            }
        } catch (IOException ex) {
            // Ignore
        } catch (InvalidConfigFileException ex) {
            // Ignore
        }

        Runtime.getRuntime().addShutdownHook(new Thread(this));
    }

    public boolean isLegalSetting(final String key) {
        return getType(key) != null;
    }

    public String getType(final String key) {
        for (String setting : SETTINGS) {
            if (setting.startsWith(key + ":")) {
                return setting.substring(setting.indexOf(':') + 1);
            }
        }

        return null;
    }

    public boolean hasOption(final String user, final String key) {
        return configfile.isKeyDomain(user) && configfile.getKeyDomain(user).containsKey(key);
    }

    public String getOption(final String user, final String key) {
        return configfile.getKeyDomain(user).get(key);
    }

    public String setOption(final String user, final String key, final Object obj) {
        final String value = String.valueOf(obj);
        
        if (getType(key).equals("int") && !value.matches("^[0-9]+$")) {
            throw new IllegalArgumentException("That setting must be an integer");
        }

        return configfile.getKeyDomain(user).put(key, value);
    }

    public void run() {
        try {
            configfile.write();
        } catch (IOException ex) {
            // Uh oh
        }
    }

}
