package logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

public class Logger {

	private File f;
	private static final String SUCCESS = "Successful",
								FAIL = "Failed";
	
	public Logger(String logPathWithFile, boolean deletePossiblyExistingFile) {
		// if old file shall be deleted.
		if (deletePossiblyExistingFile) {
			new File(logPathWithFile).delete();
		}
		f = new File(logPathWithFile);

	}
	
	public void writeToLog(String logtext) {
			try {

				BufferedWriter out = new BufferedWriter(new FileWriter(f, true));

				out.append(logtext + "\n");
				out.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
	}
	
	public String getSuccessMessage() {
		return SUCCESS;
	}
	
	public String getFailMessage() {
		return FAIL;
	}
}
