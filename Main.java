import java.io.File;
import java.io.IOException;


public class Main {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		FichierSon note = new FichierSon();
		File fichierWav = note.enregistrement();
		float[] tableauNote = note.conversion(fichierWav);
		
		
	}

}
