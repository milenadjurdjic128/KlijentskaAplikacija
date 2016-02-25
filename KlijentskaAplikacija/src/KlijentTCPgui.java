import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JTextField;
import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import java.awt.Toolkit;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;

/*
 * Klijentska aplikacija koja kontaktira server, salje mu operatore i operaciju i prima rezultat
 */
public class KlijentTCPgui extends JFrame {

	private static final long serialVersionUID = 1L;

	static JComboBox<String> operacija = new JComboBox<String>();
	private JPanel contentPane;
	private JTextField textFieldrezultat;
	private JButton btnIzraunaj;
	private JPanel panel;
	private JScrollPane scrollPane;
	private JTextArea brojevi;

	//inicijalizacija promenljivih
	static boolean zavrsi = false;
	static BufferedReader ulazniTokKlijentaOperacija = null;
	static PrintStream izlazniTokKlijentaOperacija = null;
	static BufferedReader ulazniTokKlijentaOperatori = null;
	static PrintStream izlazniTokKlijentaOperatori = null;
	static Socket klijentskiSoketKontrolni = null;
	private JButton btnKrajRada;
	private JButton btnObrisiSve;
	public Socket klijentskiSoket2 = null;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					KlijentTCPgui frame = new KlijentTCPgui();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public KlijentTCPgui() {

		setIconImage(Toolkit.getDefaultToolkit().getImage(KlijentTCPgui.class.getResource("/icons/calculator.png")));
		setTitle("Kalkulator");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 333);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		operacija.setModel(new DefaultComboBoxModel<String>(new String[] {"~ odaberite operaciju ~", "sabiranje", "oduzimanje", "mnozenje", "deljenje"}));
		operacija.setBounds(31, 109, 174, 20);
		contentPane.add(operacija);

		textFieldrezultat = new JTextField();
		textFieldrezultat.setEditable(false);
		textFieldrezultat.setBounds(31, 184, 373, 20);
		contentPane.add(textFieldrezultat);
		textFieldrezultat.setColumns(10);

		JLabel lblRezultat = new JLabel("Rezultat:");
		lblRezultat.setBounds(31, 159, 75, 14);
		contentPane.add(lblRezultat);

		JLabel lblDesktopKalkulator = new JLabel("Unesite operatore sa razmakom (npr:5 2 6)");
		lblDesktopKalkulator.setBounds(31, 11, 328, 30);
		contentPane.add(lblDesktopKalkulator);
		contentPane.add(getBtnIzraunaj());
		contentPane.add(getPanel());
		contentPane.add(getBtnKrajRada());
		contentPane.add(getBtnObrisiSve());
	}
	private JButton getBtnIzraunaj() {
		if (btnIzraunaj == null) {
			btnIzraunaj = new JButton("Izra\u010Dunaj!");
			btnIzraunaj.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent args) {

					//String koji ce sadrzati neobradjene unete podatke
					String operatori = brojevi.getText().toString();

					//String operacija koji ce oznacavati koju operaciju da uradi server
					String izabranaOperacija = "";

					//ako nije izabrao operaciju govorimo korisniku da izabere
					if(operacija.getSelectedItem().toString().equals("~ odaberite operaciju ~")) {
						textFieldrezultat.setText("Odaberite operaciju");
					}
					//dodeljujem izabranu operaciju
					if(operacija.getSelectedItem().toString().equals("sabiranje")) {
						izabranaOperacija = "sabiranje";
					}
					if(operacija.getSelectedItem().toString().equals("oduzimanje")) {
						izabranaOperacija = "oduzimanje";
					}
					if(operacija.getSelectedItem().toString().equals("mnozenje")) {
						izabranaOperacija = "mnozenje";
					}
					if(operacija.getSelectedItem().toString().equals("deljenje")) {
						izabranaOperacija = "deljenje";
					}

					//ako nije uneo bar dva broja, javljamo da unese bar dva broja jer nema smisla rad sa jednim
					if(!(brojevi.getText().toString().contains(" "))) {
						textFieldrezultat.setText("Unesite bar dva broja!");
					}
					//ako korisnik nije uneo nijedan broj, javljamo mu da ih unese
					if(brojevi.getText().equals("")) {
						textFieldrezultat.setText("Unesite brojeve!");
					}

					try {
						//povezivanje klijenta sa serverom preko porta 1908, za operaciju
						klijentskiSoketKontrolni = new Socket("localhost", 1908);
						ulazniTokKlijentaOperacija = new BufferedReader(new InputStreamReader(klijentskiSoketKontrolni.getInputStream()));
						izlazniTokKlijentaOperacija = new PrintStream(klijentskiSoketKontrolni.getOutputStream());
						
						izlazniTokKlijentaOperacija.println(izabranaOperacija); //dok nije gotova komunikacija moze da salje zahteve saljemo serveru operaciju
						
						
						//saljemo podatke preko porta 128
						klijentskiSoket2 = new Socket("localhost", 3128);
						ulazniTokKlijentaOperatori = new BufferedReader(new InputStreamReader(klijentskiSoket2.getInputStream()));
						izlazniTokKlijentaOperatori = new PrintStream(klijentskiSoket2.getOutputStream());
						
						izlazniTokKlijentaOperatori.println(operatori); // saljemo serveru operatore
						
						if(operacija.getSelectedItem().toString().equals("~ odaberite operaciju ~")) {
							textFieldrezultat.setText("Odaberite operaciju");
						} else if(brojevi.getText().toString().equals("")) {
							textFieldrezultat.setText("Unesite bar dva broja!");
						} else {
							//prikazujemo odgovor servera koji smo procitali u ulaznom toku
							String rezultat = ulazniTokKlijentaOperatori.readLine();
							textFieldrezultat.setText(rezultat);
						}

					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
			btnIzraunaj.setBounds(215, 108, 189, 23);
		}
		return btnIzraunaj;
	}
	private JPanel getPanel() {
		if (panel == null) {
			panel = new JPanel();
			panel.setBounds(31, 48, 373, 50);
			panel.setLayout(null);
			panel.add(getScrollPane());
		}
		return panel;
	}
	private JScrollPane getScrollPane() {
		if (scrollPane == null) {
			scrollPane = new JScrollPane();
			scrollPane.setBounds(0, 0, 373, 50);
			scrollPane.setViewportView(getBrojevi());
		}
		return scrollPane;
	}
	private JTextArea getBrojevi() {
		if (brojevi == null) {
			brojevi = new JTextArea();
		}
		return brojevi;
	}

	private JButton getBtnKrajRada() {
		if (btnKrajRada == null) {
			btnKrajRada = new JButton("Kraj rada!");
			btnKrajRada.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					try {
						zavrsi = true;
						klijentskiSoketKontrolni.close();
						klijentskiSoket2.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
					System.exit(0);
				}
			});
			btnKrajRada.setBounds(167, 264, 111, 20);
		}
		return btnKrajRada;
	}
	private JButton getBtnObrisiSve() {
		if (btnObrisiSve == null) {
			btnObrisiSve = new JButton("Obrisi sve!");
			btnObrisiSve.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					zavrsi = false;
					brojevi.setText(null);
					textFieldrezultat.setText(null);
					operacija.setSelectedItem("~ odaberite operaciju ~");
				}
			});
			btnObrisiSve.setBounds(147, 215, 147, 38);
		}
		return btnObrisiSve;
	}
}
