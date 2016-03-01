import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.sound.sampled.*;
import java.nio.*;

public class FichierSon {
	
	AudioFormat audioFormat;
	TargetDataLine targetDataLine;
	static CaptureThread CT;
	int fe;

	public float[] conversion (File fichier_note) throws IOException{
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		BufferedInputStream in = new BufferedInputStream(new FileInputStream(fichier_note.getAbsolutePath()));
		int read;
		byte[] buff = new byte[1024];
		while ((read = in.read(buff)) > 0)
		{
			out.write(buff, 0, read);
		}
		out.flush();
		in.close();
		byte[] audioBytes = out.toByteArray();
		ShortBuffer sbuf =
				ByteBuffer.wrap(audioBytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer();
		short[] audioShorts = new short[sbuf.capacity()];
		sbuf.get(audioShorts);
		float[] audioFloats = new float[audioShorts.length];
		// ecriture des valeurs flottantes dans audioFloats puis dans le fichier de sortie
		for (int i = 0; i < audioShorts.length; i++) {
			/** audioFloats va contenir les amplitudes (float) correspondant à chacun des échantillons 
			 * 	sur lequel on pourra faire tous les calculs après !! (fenêtre, fft, etc...) */
			audioFloats[i] = ((float)audioShorts[i])/0x8000;
		} 
		return audioFloats;
	}

	public void captureSon(){
		try{
			//Prépare l'enregistrement
			audioFormat = getAudioFormat();
			DataLine.Info dataLineInfo =
					new DataLine.Info(
							TargetDataLine.class,
							audioFormat);
			targetDataLine = (TargetDataLine)
					AudioSystem.getLine(dataLineInfo);
			/** Thread pour enregistrer les données micro
			 dans un fichier .wav. Il tourne jusqu'à ce 
			que le bouton Stop soit enclenché.*/
			CT =new CaptureThread();
			CT.start();
			}catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
	}

	public File enregistrement(){
		// Bouton pour commencer à enregistrer le son
		final JButton captureBtn =
				new JButton("Capture");
		// Bouton pour arrêter l'enregistrement
		final JButton stopBtn = new JButton("Stop");
		// Panneau qui apparaît au lancement du programme
		final JPanel btnPanel = new JPanel();
		final ButtonGroup btnGroup = new ButtonGroup();
		// Constructeur de Record
		// Au début, on ne peut que cliquer sur Capture
		captureBtn.setEnabled(true);
		stopBtn.setEnabled(false);
		captureBtn.addActionListener(
				new ActionListener(){
					public void actionPerformed(
							ActionEvent e){
						captureBtn.setEnabled(false);
						stopBtn.setEnabled(true);
						//Enregistrement du son jusqu'à ce que le bouton Stop soit cliqué
						captureSon();
						}
					}
				);
		stopBtn.addActionListener(
				new ActionListener(){
					public void actionPerformed(
							ActionEvent e){
						captureBtn.setEnabled(true);
						stopBtn.setEnabled(false);
						//Fin de l'enregistrement
						targetDataLine.stop();
						targetDataLine.close();
						try {
							CT.join();
						} catch (InterruptedException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						System.exit(0);
						}
					}
				);
		JFrame frame = new JFrame();
		frame.getContentPane().add(captureBtn);
		frame.getContentPane().add(stopBtn);
		frame.getContentPane().add(btnPanel);
		frame.getContentPane().setLayout(new FlowLayout());
		frame.setDefaultCloseOperation(frame.EXIT_ON_CLOSE);
		frame.setSize(300,120);
		frame.setVisible(true);
		return CT.audioFile;
	}
	
	//Cette méthode capture le son d'un micro et l'enregistre
	//dans un fichier .wav
	//Cette méthode renvoie un objet de type AudioFormat
	private AudioFormat getAudioFormat(){
		float sampleRate = 16000;// taux d'échantillonnage
		int sampleSizeInBits = 16;
		int channels = 1;
		boolean signed = true;
		boolean bigEndian = false;
		return new AudioFormat(sampleRate,
				sampleSizeInBits,
				channels,
				signed,
				bigEndian);
	}

	// Ce Thread permet d'enregistrer les données micro dans un fichier.wav
	class CaptureThread extends Thread{
		public File audioFile;
		public void run(){
			AudioFileFormat.Type fileType = null;
			File audioFile = null;
			fileType = AudioFileFormat.Type.WAVE;
			audioFile = new File("note.wav");
			try{
				targetDataLine.open(audioFormat);
				targetDataLine.start();
				AudioSystem.write(
						new AudioInputStream(targetDataLine),
						fileType,
						audioFile);
			}catch (Exception e){
				e.printStackTrace();
			}

		}
	}

}

