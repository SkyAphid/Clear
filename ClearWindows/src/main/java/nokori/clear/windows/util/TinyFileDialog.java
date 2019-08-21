package nokori.clear.windows.util;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

import java.io.File;

/**
 * This class is a wrapper for various TinyFileDialog utilities.
 */
public class TinyFileDialog {

    public enum InputType {
        OK("ok"),
        OK_CANCEL("okcancel"),
        YES_NO("yesno");

        String key;

        private InputType(String key) {
            this.key = key;
        }
    }

    public enum Icon {
        INFORMATION("info"),
        WARNING("warning"),
        ERROR("error"),
        QUESTION("question");

        String key;

        private Icon(String key) {
            this.key = key;
        }
    }

    ;

    /**
     * Shows a message dialog with the given window title and message.
     *
     * @param title
     * @param message
     */
    public static void showMessageDialog(String title, String message, Icon icon) {
        TinyFileDialogs.tinyfd_messageBox(title, message, InputType.OK.key, icon.key, true);
    }

    /**
     * Show a confirm dialog with the given settings.
     *
     * @param title             - window title
     * @param message           - window message
     * @param type              - confirm type (buttons used)
     * @param icon              - window icon
     * @param defaultButtonIsOK - if true, the default button highlighted will be the "yes" button.
     * @return true if "yes" or an equivalent is selected.
     */
    public static boolean showConfirmDialog(String title, String message, InputType type, Icon icon, boolean defaultButtonIsOK) {
        return TinyFileDialogs.tinyfd_messageBox(title, message, type.key, icon.key, defaultButtonIsOK);
    }

    /**
     * Show an input dialog with the given settings.
     *
     * @param title        - window title
     * @param message      - window message
     * @param defaultInput - the default input to be displayed
     * @return - the final string inputted into the dialog
     */
    public static String showInputDialog(String title, String message, String defaultInput) {
        return TinyFileDialogs.tinyfd_inputBox(title, message, defaultInput);
    }

    /**
     * Shows a dialog for selecting a folder.
     *
     * @param title       - window title
     * @param defaultPath - default filepath to start from
     * @return the selected folder path in a File object
     */
    public static File showOpenFolderDialog(String title, File defaultPath) {
        String result = TinyFileDialogs.tinyfd_selectFolderDialog(title, defaultPath.getAbsolutePath());
        return result != null ? new File(result) : null;
    }

    /**
     * Opens a file open dialog.
     *
     * @param title                            window title
     * @param defaultPath                      default file path
     * @param filterDescription                description of the accepted file extension(s)
     * @param acceptedFileExtension            the first accepted file extension (example: "txt", use * for all)
     * @param additionalAcceptedFileExtensions any additional accepted file extensions
     * @return the selected file
     */
	public static File showOpenFileDialog(String title, File defaultPath, String filterDescription, String... acceptedFileExtensions){
		
		PointerBuffer filters = MemoryStack.stackMallocPointer(acceptedFileExtensions.length);

        filters.put(MemoryStack.stackUTF8("*." + acceptedFileExtensions[0]));
        
        if (acceptedFileExtensions.length > 1) {
            for(int i = 1; i < acceptedFileExtensions.length; i++){
            	filters.put(MemoryStack.stackUTF8("*." + acceptedFileExtensions[i]));
            }
        }
        
        filters.flip();

        defaultPath = defaultPath.getAbsoluteFile();
        String defaultString = defaultPath.getAbsolutePath();
        if(defaultPath.isDirectory() && !defaultString.endsWith(File.separator)){
        	defaultString += File.separator;
        }
        
        //System.out.println(defaultString + " : exists: " + new File(defaultString).exists());
        
        String result = TinyFileDialogs.tinyfd_openFileDialog(title, defaultString, filters, filterDescription, false);
		
		return result != null ? new File(result) : null; 
	}

    /**
     * Opens a file save dialog.
     *
     * @param title             window title
     * @param defaultPath       default file path
     * @param filterDescription description of the accepted file extension(s)
     * @param fileExtension     the file extension (example: "txt")
     * @param forceExtension    the user can select any file regardless of extension. If this is set to true, then the given extension will be automatically added if the extension is wrong.
     * @return the selected file
     */
    public static File showSaveFileDialog(String title, File defaultPath, String filterDescription, String fileExtension, boolean forceExtension) {

        PointerBuffer filters = MemoryStack.stackMallocPointer(1);

        filters.put(MemoryStack.stackUTF8("*." + fileExtension)).flip();

        defaultPath = defaultPath.getAbsoluteFile();

        String defaultString = defaultPath.getAbsolutePath();

        if (defaultPath.isDirectory() && !defaultString.endsWith(File.separator)) {
            defaultString += File.separator;
        }

        //System.out.println(defaultString + " : exists: " + new File(defaultString).exists());

        String result = TinyFileDialogs.tinyfd_saveFileDialog(title, defaultString, filters, filterDescription);

        if (result == null) {
            return null;
        }

        if (forceExtension && !result.endsWith("." + fileExtension)) {
            result += "." + fileExtension;
        }
        return new File(result);
    }
}