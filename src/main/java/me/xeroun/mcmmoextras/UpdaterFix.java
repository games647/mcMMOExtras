package me.xeroun.mcmmoextras;

import com.google.common.collect.ComparisonChain;
import java.io.File;
import org.bukkit.plugin.Plugin;

/**
 * This file exists to make sure auto updater doesn't check for new updates if
 * the user has a development version.
 *
 * @see Updater
 */
public class UpdaterFix extends Updater {

    /**
     * Initialize the updater.
     *
     * @param plugin The plugin that is checking for an update.
     * @param file The file that the plugin is running from, get this from getFile() from within your main class.
     * @param projectId Curse project id
     * @param download Specify the type of update this will be.
     */
    public UpdaterFix(Plugin plugin, File file, int projectId, boolean download) {
        this(plugin, file, projectId, download, null);
    }

    /**
     * Initialize the updater with the provided callback.
     *
     * @param plugin The plugin that is checking for an update.
     * @param file The file that the plugin is running from, get this from getFile() from within your main class.
     * @param projectId Curse project id
     * @param download Specify the type of update this will be.
     * @param callback The callback instance to notify when the Updater has finished
     */
    public UpdaterFix(Plugin plugin, File file, int projectId, boolean download, UpdateCallback callback) {
        super(plugin, projectId, file, download, callback);
    }

    @Override
    public boolean shouldUpdate(String localVersion, String remoteVersion) {
        //return true if remoteVersion is higher
        return compare(localVersion, remoteVersion) > 1;
    }

        /**
     * Compares the version with checking the first three numbers
     *
     * @param expected the object to be compared.
     * @param version the object to be compared.
     * @return 1 version is higher; 0 both are equal; -1 version is lower<br>
     *          a negative integer, zero, or a positive integer as this object
     *          is less than, equal to, or greater than the specified object.
     */
    private int compare(String expected, String version) {
        final int[] expectedParts = parse(version);
        final int[] versionParts = parse(expected);

        return ComparisonChain.start()
                .compare(expectedParts[0], versionParts[0])
                .compare(expectedParts[1], versionParts[1])
                .compare(expectedParts[2], versionParts[2])
                .result();
    }

    /**
     * Separate the version into major, minor, build integers
     *
     * @param version the version that should be parsed
     * @return the version parts
     * @throws IllegalArgumentException if the version doesn't contains only positive numbers separated by max. 5 dots.
     */
    private int[] parse(String version) throws IllegalArgumentException {
        //exludes spaces which could be added by mistake and exclude build suffixes
        final String trimedVersion = version.trim().split("\\-")[0];
        if (!trimedVersion.matches("\\d+(\\.\\d+){0,5}")) {
            //check if it's a format like '1.5'
            throw new IllegalArgumentException("Invalid format: " + version);
        }

        final int[] versionParts = new int[3];

        //escape regEx and split by dots
        final String[] split = trimedVersion.split("\\.");
        //We check if the length has min 1 entry.
        for (int i = 0; i < split.length && i < versionParts.length; i++) {
            versionParts[i] = Integer.parseInt(split[i]);
        }

        return versionParts;
    }
}
