package nokori.clear.vg.widget.text;

import java.util.HashMap;

public class TextAreaContentSpecialCaseStrings {
	public static HashMap<String, String> initDefault() {
		HashMap<String, String> specialCaseCharacters = new HashMap<>();
		initDefault(specialCaseCharacters);
		return specialCaseCharacters;
	}
	
	public static void initDefault(HashMap<String, String> specialCaseCharacters) {
		specialCaseCharacters.put("\n", "");
		specialCaseCharacters.put("\t", "    ");
	}
}
