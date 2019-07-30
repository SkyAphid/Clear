package nokori.clear.windows.util;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

/**
 * This class contains some utilities for working around LWJGL's incompatibility with AWT, such as opening URL's in the users browser.
 */
public class OperatingSystemUtil {

    public MappedByteBuffer getMappedByteBuffer(File file) throws Exception {
        try (RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r")) {
            // Get file channel in read-only mode
            FileChannel fileChannel = randomAccessFile.getChannel();

            // Get direct byte buffer access using channel.map() operation
            return fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, fileChannel.size());
        }
    }

    public enum OperatingSystem {
        WINDOWS,
        MAC,
        LINUX,
        OTHER;
    }

    /**
     * This function uses Java to detect the OS that this program is running on and will return the corresponding OperatingSystem enum.
     */
    public static OperatingSystem detectOperatingSystem() {
        String os = System.getProperty("os.name").toLowerCase();

        if (os.indexOf("win") >= 0) {
            return OperatingSystem.WINDOWS;
        }

        if (os.indexOf("mac") >= 0) {
            return OperatingSystem.MAC;
        }

        if (os.indexOf("nix") >= 0 || os.indexOf("nux") >= 0) {
            return OperatingSystem.LINUX;
        }

        return OperatingSystem.OTHER;
    }


    /**
     * Opens the given URL string in the user's default browser.
     * This is a LWJGL3 alternative to Desktop.browse(), since LWJGL crashes on Mac machines if AWT is referenced at the same time.
     *
     * @param url
     * @throws IOException
     */
    public static void openURLInBrowser(String url) throws IOException {
        Runtime rt = Runtime.getRuntime();

        switch (detectOperatingSystem()) {
            case LINUX:
                String[] browsers = {"epiphany", "firefox", "mozilla", "konqueror", "netscape", "opera", "links", "lynx"};

                StringBuffer cmd = new StringBuffer();
                for (int i = 0; i < browsers.length; i++) {
                    // If the first didn't work, try the next browser and so on
                    if (i != 0) {
                        cmd.append(" || ");
                    }

                    cmd.append(String.format("%s \"%s\"", browsers[i], url));
                }

                rt.exec(new String[]{"sh", "-c", cmd.toString()});
                break;
            case MAC:
                rt.exec("open " + url);
                break;
            case WINDOWS:
                rt.exec("rundll32 url.dll,FileProtocolHandler " + url);
                break;
            case OTHER:
            default:
                System.err.println("This function will not work on this operating system.");
                break;
        }
    }
}
