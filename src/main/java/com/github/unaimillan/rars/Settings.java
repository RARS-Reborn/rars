package com.github.unaimillan.rars;

import java.util.HashMap;
import java.util.Observable;
import java.util.StringTokenizer;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/*
Copyright (c) 2003-2013,  Pete Sanderson and Kenneth Vollmar

Developed by Pete Sanderson (psanderson@otterbein.edu)
and Kenneth Vollmar (kenvollmar@missouristate.edu)

Permission is hereby granted, free of charge, to any person obtaining 
a copy of this software and associated documentation files (the 
"Software"), to deal in the Software without restriction, including 
without limitation the rights to use, copy, modify, merge, publish, 
distribute, sublicense, and/or sell copies of the Software, and to 
permit persons to whom the Software is furnished to do so, subject 
to the following conditions:

The above copyright notice and this permission notice shall be 
included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, 
EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF 
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. 
IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR 
ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF 
CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION 
WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

(MIT license, http://www.opensource.org/licenses/mit-license.html)
 */

/**
 * Contains various IDE settings.  Persistent settings are maintained for the
 * current user and on the current machine using
 * Java's Preference objects.  Failing that, default setting values come from
 * Settings.properties file.  If both of those fail, default values come from
 * static arrays defined in this class.  The latter can can be modified prior to
 * instantiating Settings object.
 * <p>
 * NOTE: If the Preference objects fail due to security exceptions, changes to
 * settings will not carry over from one RARS session to the next.
 * <p>
 * Actual implementation of the Preference objects is platform-dependent.
 * For Windows, they are stored in Registry.  To see, run regedit and browse to:
 * HKEY_CURRENT_USER\Software\JavaSoft\Prefs\rars
 *
 * @author Pete Sanderson
 **/

public class Settings extends Observable {
    /* Properties file used to hold default settings. */
    private static final String settingsFile = "Settings";
    private final ColorMode defaultColorMode = ColorMode.SYSTEM;

    // BOOLEAN SETTINGS...
    public enum Bool {
        /**
         * Flag to determine whether or not program being assembled is limited to
         * basic instructions and formats.
         */
        EXTENDED_ASSEMBLER_ENABLED("ExtendedAssembler", true),
        /**
         * Flag to determine whether or not a file is immediately and automatically assembled
         * upon opening. Handy when using externa editor like mipster.
         */
        ASSEMBLE_ON_OPEN("AssembleOnOpen", false),
        /**
         * Flag to determine whether all files open currently source file will be assembled when assembly is selected.
         */
        ASSEMBLE_OPEN("AssembleOpen", false),
        /**
         * Flag to determine whether files in the directory of the current source file will be assembled when assembly is selected.
         */
        ASSEMBLE_ALL("AssembleAll", false),

        /**
         * Default visibilty of label window (symbol table).  Default only, dynamic status
         * maintained by ExecutePane
         */
        LABEL_WINDOW_VISIBILITY("LabelWindowVisibility", false),
        /**
         * Default setting for displaying addresses and values in hexidecimal in the Execute
         * pane.
         */
        DISPLAY_ADDRESSES_IN_HEX("DisplayAddressesInHex", true),
        DISPLAY_VALUES_IN_HEX("DisplayValuesInHex", true),
        /**
         * Flag to determine whether the currently selected exception handler source file will
         * be included in each assembly operation.
         */
        EXCEPTION_HANDLER_ENABLED("LoadExceptionHandler", false),
        /**
         * Flag to determine whether or not the editor will display line numbers.
         */
        EDITOR_LINE_NUMBERS_DISPLAYED("EditorLineNumbersDisplayed", true),
        /**
         * Flag to determine whether or not assembler warnings are considered errors.
         */
        WARNINGS_ARE_ERRORS("WarningsAreErrors", false),
        /**
         * Flag to determine whether or not to display and use program arguments
         */
        PROGRAM_ARGUMENTS("ProgramArguments", false),
        /**
         * Flag to control whether or not highlighting is applied to data segment window
         */
        DATA_SEGMENT_HIGHLIGHTING("DataSegmentHighlighting", true),
        /**
         * Flag to control whether or not highlighting is applied to register windows
         */
        REGISTERS_HIGHLIGHTING("RegistersHighlighting", true),
        /**
         * Flag to control whether or not assembler automatically initializes program counter to 'main's address
         */
        START_AT_MAIN("StartAtMain", false),
        /**
         * Flag to control whether or not editor will highlight the line currently being edited
         */
        EDITOR_CURRENT_LINE_HIGHLIGHTING("EditorCurrentLineHighlighting", true),
        /**
         * Flag to control whether or not editor will provide popup instruction guidance while typing
         */
        POPUP_INSTRUCTION_GUIDANCE("PopupInstructionGuidance", true),
        /**
         * Flag to control whether or not simulator will use popup dialog for input syscalls
         */
        POPUP_SYSCALL_INPUT("PopupSyscallInput", false),
        /**
         * Flag to control whether or not to use generic text editor instead of language-aware styled editor
         */
        GENERIC_TEXT_EDITOR("GenericTextEditor", false),
        /**
         * Flag to control whether or not language-aware editor will use auto-indent feature
         */
        AUTO_INDENT("AutoIndent", true),
        /**
         * Flag to determine whether a program can write binary code to the text or data segment and
         * execute that code.
         */
        SELF_MODIFYING_CODE_ENABLED("SelfModifyingCode", false),
        /**
         * Flag to determine whether a program uses rv64i instead of rv32i
         */
        RV64_ENABLED("rv64Enabled", false),
        /**
         * Flag to determine whether to calculate relative paths from the current working directory
         * or from the RARS executable path.
         */
        DERIVE_CURRENT_WORKING_DIRECTORY("DeriveCurrentWorkingDirectory", false);

        // TODO: add option for turning off user trap handling and interrupts
        private final String name;
        private boolean value;

        Bool(String n, boolean v) {
            name = n;
            value = v;
        }

        boolean getDefault() {
            return value;
        }

        void setDefault(boolean v) {
            value = v;
        }

        String getName() {
            return name;
        }
    }
    // STRING SETTINGS.  Each array position has associated name.
    /**
     * Current specified exception handler file (a RISCV assembly source file)
     */
    public static final int EXCEPTION_HANDLER = 0;
    /**
     * Order of text segment table columns
     */
    public static final int TEXT_COLUMN_ORDER = 1;
    /**
     * State for sorting label window display
     */
    public static final int LABEL_SORT_STATE = 2;
    /**
     * Identifier of current memory configuration
     */
    public static final int MEMORY_CONFIGURATION = 3;
    /**
     * Caret blink rate in milliseconds, 0 means don't blink.
     */
    public static final int CARET_BLINK_RATE = 4;
    /**
     * Editor tab size in characters.
     */
    public static final int EDITOR_TAB_SIZE = 5;
    /**
     * Number of letters to be matched by editor's instruction guide before popup generated (if popup enabled)
     */
    public static final int EDITOR_POPUP_PREFIX_LENGTH = 6;
    // Match the above by position.
    private static final String[] stringSettingsKeys = {"ExceptionHandler", "TextColumnOrder", "LabelSortState", "MemoryConfiguration", "CaretBlinkRate", "EditorTabSize", "EditorPopupPrefixLength"};

    /**
     * Last resort default values for String settings;
     * will use only if neither the Preferences nor the properties file work.
     * If you wish to change, do so before instantiating the Settings object.
     * Must match key by list position.
     */
    private static final String[] defaultStringSettingsValues = {"", "0 1 2 3 4", "0", "", "500", "8", "2"};


    // FONT SETTINGS.  Each array position has associated name.
    /**
     * Font for the text editor
     */
    public static final int EDITOR_FONT = 0;
    /**
     * Font for table even row background (text, data, register displays)
     */
    public static final int EVEN_ROW_FONT = 1;
    /**
     * Font for table odd row background (text, data, register displays)
     */
    public static final int ODD_ROW_FONT = 2;
    /**
     * Font for table odd row foreground (text, data, register displays)
     */
    public static final int TEXTSEGMENT_HIGHLIGHT_FONT = 3;
    /**
     * Font for text segment delay slot highlighted background
     */
    public static final int TEXTSEGMENT_DELAYSLOT_HIGHLIGHT_FONT = 4;
    /**
     * Font for text segment highlighted background
     */
    public static final int DATASEGMENT_HIGHLIGHT_FONT = 5;
    /**
     * Font for register highlighted background
     */
    public static final int REGISTER_HIGHLIGHT_FONT = 6;

    private static final String[] fontFamilySettingsKeys = {"EditorFontFamily", "EvenRowFontFamily",
            "OddRowFontFamily", " TextSegmentHighlightFontFamily", "TextSegmentDelayslotHighightFontFamily",
            "DataSegmentHighlightFontFamily", "RegisterHighlightFontFamily"
    };
    private static final String[] fontStyleSettingsKeys = {"EditorFontStyle", "EvenRowFontStyle",
            "OddRowFontStyle", " TextSegmentHighlightFontStyle", "TextSegmentDelayslotHighightFontStyle",
            "DataSegmentHighlightFontStyle", "RegisterHighlightFontStyle"
    };
    private static final String[] fontSizeSettingsKeys = {"EditorFontSize", "EvenRowFontSize",
            "OddRowFontSize", " TextSegmentHighlightFontSize", "TextSegmentDelayslotHighightFontSize",
            "DataSegmentHighlightFontSize", "RegisterHighlightFontSize"
    };


    /**
     * Last resort default values for Font settings;
     * will use only if neither the Preferences nor the properties file work.
     * If you wish to change, do so before instantiating the Settings object.
     * Must match key by list position shown above.
     */

    // DPS 3-Oct-2012
    // Changed default font family from "Courier New" to "Monospaced" after receiving reports that Mac were not
    // correctly rendering the left parenthesis character in the editor or text segment display.
    // See http://www.mirthcorp.com/community/issues/browse/MIRTH-1921?page=com.atlassian.jira.plugin.system.issuetabpanels:all-tabpanel
    private static final String[] defaultFontFamilySettingsValues = {"Monospaced", "Monospaced", "Monospaced",
            "Monospaced", "Monospaced", "Monospaced", "Monospaced"
    };
    private static final String[] defaultFontStyleSettingsValues = {"Plain", "Plain", "Plain", "Plain",
            "Plain", "Plain", "Plain"
    };
    private static final String[] defaultFontSizeSettingsValues = {"12", "12", "12", "12", "12", "12", "12",
    };


    // COLOR SETTINGS.  Each array position has associated name.
    /**
     * RGB color for table even row background (text, data, register displays)
     */
    public static final int EVEN_ROW_BACKGROUND = 0;
    /**
     * RGB color for table even row foreground (text, data, register displays)
     */
    public static final int EVEN_ROW_FOREGROUND = 1;
    /**
     * RGB color for table odd row background (text, data, register displays)
     */
    public static final int ODD_ROW_BACKGROUND = 2;
    /**
     * RGB color for table odd row foreground (text, data, register displays)
     */
    public static final int ODD_ROW_FOREGROUND = 3;
    /**
     * RGB color for text segment highlighted background
     */
    public static final int TEXTSEGMENT_HIGHLIGHT_BACKGROUND = 4;
    /**
     * RGB color for text segment highlighted foreground
     */
    public static final int TEXTSEGMENT_HIGHLIGHT_FOREGROUND = 5;
    /**
     * RGB color for text segment delay slot highlighted background
     */
    public static final int TEXTSEGMENT_DELAYSLOT_HIGHLIGHT_BACKGROUND = 6;
    /**
     * RGB color for text segment delay slot highlighted foreground
     */
    public static final int TEXTSEGMENT_DELAYSLOT_HIGHLIGHT_FOREGROUND = 7;
    /**
     * RGB color for text segment highlighted background
     */
    public static final int DATASEGMENT_HIGHLIGHT_BACKGROUND = 8;
    /**
     * RGB color for text segment highlighted foreground
     */
    public static final int DATASEGMENT_HIGHLIGHT_FOREGROUND = 9;
    /**
     * RGB color for register highlighted background
     */
    public static final int REGISTER_HIGHLIGHT_BACKGROUND = 10;
    /**
     * RGB color for register highlighted foreground
     */
    public static final int REGISTER_HIGHLIGHT_FOREGROUND = 11;
    /**
     * RGB background color of Editor
     */
    public static final int EDITOR_BACKGROUND = 12;
    /**
     * RGB foreground color of Editor
     */
    public static final int EDITOR_FOREGROUND = 13;
    /**
     * RGB line-highlight color of Editor
     */
    public static final int EDITOR_LINE_HIGHLIGHT = 14;
    /**
     * RGB color of text-selection in Editor
     */
    public static final int EDITOR_SELECTION_COLOR = 15;
    /**
     * RGB color of caret in Editor
     */
    public static final int EDITOR_CARET_COLOR = 16;

    public enum ColorMode {
        DEFAULT("DEF"),
        SYSTEM("SYS"),
        CUSTOM(null);

        public final String modeKey;

        ColorMode(String modeKey) {
            this.modeKey = modeKey;
        }
    }

    private final HashMap<Bool, Boolean> booleanSettingsValues;
    private final String[] stringSettingsValues;
    private final Preferences preferences;

    /**
     * Create Settings object and set to saved values.  If saved values not found, will set
     * based on defaults stored in Settings.properties file.  If file problems, will set based
     * on defaults stored in this class.
     */

    public Settings() {
        booleanSettingsValues = new HashMap<>();
        stringSettingsValues = new String[stringSettingsKeys.length];
        // This determines where the values are actually stored.  Actual implementation
        // is platform-dependent.  For Windows, they are stored in Registry.  To see,
        // run regedit and browse to: HKEY_CURRENT_USER\Software\JavaSoft\Prefs\rars
        preferences = Preferences.userNodeForPackage(this.getClass());
        // The gui parameter, formerly passed to initialize(), is no longer needed
        // because I removed (1/21/09) the call to generate the Font object for the text editor.
        // Font objects are now generated only on demand so the "if (gui)" guard
        // is no longer necessary.  Originally added by Berkeley b/c they were running it on a
        // headless server and running in command mode.  The Font constructor resulted in Swing
        // initialization which caused problems.  Now this will only occur on demand from
        // Venus, which happens only when running as GUI.
        initialize();
    }


    /**
     * Return whether backstepping is permitted at this time.  Backstepping is ability to undo execution
     * steps one at a time.  Available only in the IDE.  This is not a persistent setting and is not under
     * RARS user control.
     *
     * @return true if backstepping is permitted, false otherwise.
     */
    public boolean getBackSteppingEnabled() {
        return (Globals.program != null && Globals.program.getBackStepper() != null && Globals.program.getBackStepper().enabled());
    }


    /**
     * Reset settings to default values, as described in the constructor comments.
     */
    public void reset() {
        initialize();
    }


    /* **************************************************************************
     This section contains all code related to syntax highlighting styles settings.
     A style includes 3 components: color, bold (t/f), italic (t/f)

    The fallback defaults will come not from an array here, but from the
    existing static method SyntaxUtilities.getDefaultSyntaxStyles()
    in the rars.venus.editors.jeditsyntax package.  It returns an array
    of SyntaxStyle objects.

    */
    private String[] syntaxStyleColorSettingsValues;
    private boolean[] syntaxStyleBoldSettingsValues;
    private boolean[] syntaxStyleItalicSettingsValues;

    private static final String SYNTAX_STYLE_COLOR_PREFIX = "SyntaxStyleColor_";
    private static final String SYNTAX_STYLE_BOLD_PREFIX = "SyntaxStyleBold_";
    private static final String SYNTAX_STYLE_ITALIC_PREFIX = "SyntaxStyleItalic_";

    private static String[] syntaxStyleColorSettingsKeys, syntaxStyleBoldSettingsKeys, syntaxStyleItalicSettingsKeys;
    private static String[] defaultSyntaxStyleColorSettingsValues;
    private static boolean[] defaultSyntaxStyleBoldSettingsValues;
    private static boolean[] defaultSyntaxStyleItalicSettingsValues;

    private void saveEditorSyntaxStyle(int index) {
        try {
            preferences.put(syntaxStyleColorSettingsKeys[index], syntaxStyleColorSettingsValues[index]);
            preferences.putBoolean(syntaxStyleBoldSettingsKeys[index], syntaxStyleBoldSettingsValues[index]);
            preferences.putBoolean(syntaxStyleItalicSettingsKeys[index], syntaxStyleItalicSettingsValues[index]);
            preferences.flush();
        } catch (SecurityException se) {
            // cannot write to persistent storage for security reasons
        } catch (BackingStoreException bse) {
            // unable to communicate with persistent storage (strange days)
        }
    }

    // *********************************************************************************


    ////////////////////////////////////////////////////////////////////////
    //  Setting Getters
    ////////////////////////////////////////////////////////////////////////


    /**
     * Fetch value of a boolean setting given its identifier.
     *
     * @param setting the setting to fetch the value of
     * @return corresponding boolean setting.
     * @throws IllegalArgumentException if identifier is invalid.
     */
    public boolean getBooleanSetting(Bool setting) {
        if (booleanSettingsValues.containsKey(setting)) {
            return booleanSettingsValues.get(setting);
        } else {
            throw new IllegalArgumentException("Invalid boolean setting ID");
        }
    }

    /**
     * Name of currently selected exception handler file.
     *
     * @return String pathname of current exception handler file, empty if none.
     */
    public String getExceptionHandler() {
        return stringSettingsValues[EXCEPTION_HANDLER];
    }

    /**
     * Returns identifier of current built-in memory configuration.
     *
     * @return String identifier of current built-in memory configuration, empty if none.
     */
    public String getMemoryConfiguration() {
        return stringSettingsValues[MEMORY_CONFIGURATION];
    }

    /**
     * Get number of letters to be matched by editor's instruction guide before popup generated (if popup enabled).
     * Should be 1 or 2.  If 1, the popup will be generated after first letter typed, based on all matches; if 2,
     * the popup will be generated after second letter typed.
     *
     * @return number of letters (should be 1 or 2).
     */
    public int getEditorPopupPrefixLength() {
        int length = 2;
        try {
            length = Integer.parseInt(stringSettingsValues[EDITOR_POPUP_PREFIX_LENGTH]);
        } catch (NumberFormatException nfe) {

        }
        return length;
    }


    /**
     * Get the text editor default tab size in characters
     *
     * @return tab size in characters
     */
    public int getDefaultEditorTabSize() {
        return Integer.parseInt(defaultStringSettingsValues[EDITOR_TAB_SIZE]);
    }

    /**
     * Get the saved state of the Labels Window sorting  (can sort by either
     * label or address and either ascending or descending order).
     * Default state is 0, by ascending addresses.
     *
     * @return State value 0-7, as a String.
     */
    public String getLabelSortState() {
        return stringSettingsValues[LABEL_SORT_STATE];
    }

    ////////////////////////////////////////////////////////////////////////
    //  Setting Setters
    ////////////////////////////////////////////////////////////////////////


    /**
     * Set value of a boolean setting given its id and the value.
     *
     * @param setting setting to set the value of
     * @param value   boolean value to store
     * @throws IllegalArgumentException if identifier is not valid.
     */
    public void setBooleanSetting(Bool setting, boolean value) {
        if (booleanSettingsValues.containsKey(setting)) {
            internalSetBooleanSetting(setting, value);
        } else {
            throw new IllegalArgumentException("Invalid boolean setting ID");
        }
    }

    /**
     * Temporarily establish boolean setting.  This setting will NOT be written to persisent
     * store!  Currently this is used only when running RARS from the command line
     *
     * @param setting the setting to set the value of
     * @param value   True to enable the setting, false otherwise.
     */
    public void setBooleanSettingNonPersistent(Bool setting, boolean value) {
        if (booleanSettingsValues.containsKey(setting)) {
            booleanSettingsValues.put(setting, value);
        } else {
            throw new IllegalArgumentException("Invalid boolean setting ID");
        }
    }

    /**
     * Set name of exception handler file and write it to persistent storage.
     *
     * @param newFilename name of exception handler file
     */
    public void setExceptionHandler(String newFilename) {
        setStringSetting(EXCEPTION_HANDLER, newFilename);
    }

    /**
     * Store the identifier of the memory configuration.
     *
     * @param config A string that identifies the current built-in memory configuration
     */

    public void setMemoryConfiguration(String config) {
        setStringSetting(MEMORY_CONFIGURATION, config);
    }

    /**
     * Set number of letters to be matched by editor's instruction guide before popup generated (if popup enabled).
     * Should be 1 or 2.  If 1, the popup will be generated after first letter typed, based on all matches; if 2,
     * the popup will be generated after second letter typed.
     *
     * @param length of letters (should be 1 or 2).
     */
    public void setEditorPopupPrefixLength(int length) {
        setStringSetting(EDITOR_POPUP_PREFIX_LENGTH, "" + length);
    }

    /**
     * Store the current order of Text Segment window table columns, so the ordering
     * can be preserved and restored.
     *
     * @param columnOrder An array of int indicating column order.
     */

    public void setTextColumnOrder(int[] columnOrder) {
        String stringifiedOrder = "";
        for (int column : columnOrder) {
            stringifiedOrder += column + " ";
        }
        setStringSetting(TEXT_COLUMN_ORDER, stringifiedOrder);
    }

    /**
     * Store the current state of the Labels Window sorter.  There are 8 possible states
     * as described in LabelsWindow.java
     *
     * @param state The current labels window sorting state, as a String.
     */

    public void setLabelSortState(String state) {
        setStringSetting(LABEL_SORT_STATE, state);
    }

    /////////////////////////////////////////////////////////////////////////
    //
    //     PRIVATE HELPER METHODS TO DO THE REAL WORK
    //
    /////////////////////////////////////////////////////////////////////////

    // Initialize settings to default values.
    // Strategy: First set from properties file.
    //           If that fails, set from array.
    //           In either case, use these values as defaults in call to Preferences.

    private void initialize() {
        applyDefaultSettings();
        if (!readSettingsFromPropertiesFile(settingsFile)) {
            System.out.println("RARS System error: unable to read Settings.properties defaults. Using built-in defaults.");
        }
        getSettingsFromPreferences();
    }

    // Default values.  Will be replaced if available from property file or Preferences object.
    private void applyDefaultSettings() {
        for (Bool setting : Bool.values()) {
            booleanSettingsValues.put(setting, setting.getDefault());
        }
        System.arraycopy(defaultStringSettingsValues, 0, stringSettingsValues, 0, stringSettingsValues.length);
    }

    // Used by all the boolean setting "setter" methods.
    private void internalSetBooleanSetting(Bool setting, boolean value) {
        if (value != booleanSettingsValues.get(setting)) {
            booleanSettingsValues.put(setting, value);
            saveBooleanSetting(setting.getName(), value);
            setChanged();
            notifyObservers();
        }
    }

    // Used by setter method(s) for string-based settings (initially, only exception handler name)
    private void setStringSetting(int settingIndex, String value) {
        stringSettingsValues[settingIndex] = value;
        saveStringSetting(settingIndex);
    }

    // Establish the settings from the given properties file.  Return true if it worked,
    // false if it didn't.  Note the properties file exists only to provide default values
    // in case the Preferences fail or have not been recorded yet.
    //
    // Any settings successfully read will be stored in both the xSettingsValues and
    // defaultXSettingsValues arrays (x=boolean,string,color).  The latter will overwrite the
    // last-resort default values hardcoded into the arrays above.
    //
    // NOTE: If there is NO ENTRY for the specified property, Globals.getPropertyEntry() returns
    // null.  This is no cause for alarm.  It will occur during system development or upon the
    // first use of a new RARS release in which new settings have been defined.
    // In that case, this method will NOT make an assignment to the settings array!
    // So consider it a precondition of this method: the settings arrays must already be
    // initialized with last-resort default values.
    private boolean readSettingsFromPropertiesFile(String filename) {
        String settingValue;
        try {
            for (Bool setting : Bool.values()) {
                settingValue = Globals.getPropertyEntry(filename, setting.getName());
                if (settingValue != null) {
                    boolean value = Boolean.valueOf(settingValue);
                    setting.setDefault(value);
                    booleanSettingsValues.put(setting, value);
                }
            }
            for (int i = 0; i < stringSettingsKeys.length; i++) {
                settingValue = Globals.getPropertyEntry(filename, stringSettingsKeys[i]);
                if (settingValue != null)
                    stringSettingsValues[i] = defaultStringSettingsValues[i] = settingValue;
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    // Get settings values from Preferences object.  A key-value pair will only be written
    // to Preferences if/when the value is modified.  If it has not been modified, the default value
    // will be returned here.
    //
    // PRECONDITION: Values arrays have already been initialized to default values from
    // Settings.properties file or default value arrays above!
    private void getSettingsFromPreferences() {
        for (Bool setting : booleanSettingsValues.keySet()) {
            booleanSettingsValues.put(setting, preferences.getBoolean(setting.getName(), booleanSettingsValues.get(setting)));
        }
        for (int i = 0; i < stringSettingsKeys.length; i++) {
            stringSettingsValues[i] = preferences.get(stringSettingsKeys[i], stringSettingsValues[i]);
        }
    }

    // Save the key-value pair in the Properties object and assure it is written to persisent storage.
    private void saveBooleanSetting(String name, boolean value) {
        try {
            preferences.putBoolean(name, value);
            preferences.flush();
        } catch (SecurityException se) {
            // cannot write to persistent storage for security reasons
        } catch (BackingStoreException bse) {
            // unable to communicate with persistent storage (strange days)
        }
    }


    // Save the key-value pair in the Properties object and assure it is written to persisent storage.
    private void saveStringSetting(int index) {
        try {
            preferences.put(stringSettingsKeys[index], stringSettingsValues[index]);
            preferences.flush();
        } catch (SecurityException se) {
            // cannot write to persistent storage for security reasons
        } catch (BackingStoreException bse) {
            // unable to communicate with persistent storage (strange days)
        }
    }


    // Save the key-value pair in the Properties object and assure it is written to persisent storage.
    private void saveFontSetting(int index, String[] settingsKeys, String[] settingsValues) {
        try {
            preferences.put(settingsKeys[index], settingsValues[index]);
            preferences.flush();
        } catch (SecurityException se) {
            // cannot write to persistent storage for security reasons
        } catch (BackingStoreException bse) {
            // unable to communicate with persistent storage (strange days)
        }
    }

    /*
     *  Private helper to do the work of converting a string containing Text
     *  Segment window table column order into int array and returning it.
     *  If a problem occurs with the parameter string, will fall back to the
     *  default defined above.
     */
    private int[] getTextSegmentColumnOrder(String stringOfColumnIndexes) {
        StringTokenizer st = new StringTokenizer(stringOfColumnIndexes);
        int[] list = new int[st.countTokens()];
        int index = 0, value;
        boolean valuesOK = true;
        while (st.hasMoreTokens()) {
            try {
                value = Integer.parseInt(st.nextToken());
            } // could be either NumberFormatException or NoSuchElementException
            catch (Exception e) {
                valuesOK = false;
                break;
            }
            list[index++] = value;
        }
        if (!valuesOK && !stringOfColumnIndexes.equals(defaultStringSettingsValues[TEXT_COLUMN_ORDER])) {
            return getTextSegmentColumnOrder(defaultStringSettingsValues[TEXT_COLUMN_ORDER]);
        }
        return list;
    }

}
