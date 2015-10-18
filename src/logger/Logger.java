package logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;

public class Logger {

	private File f;
	private FileOutputStream fos;
	private PrintWriter out;
	
	public Logger(String logPathWithFile, boolean deletePossiblyExistingFile) {
		// if old file shall be deleted.
		if (deletePossiblyExistingFile) {
			new File(logPathWithFile).delete();
		}
		f = new File(logPathWithFile);

		try {
			fos = new FileOutputStream(f, true);
			out = new PrintWriter(fos);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public void writeToLog(String logtext) {
		out.append(logtext + "\n");
	}
	
	public void close() {
		out.close();
	}
}
