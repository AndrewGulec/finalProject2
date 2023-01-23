import java.awt.EventQueue;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import java.awt.Font;
import java.awt.Image;

import javax.swing.SwingConstants;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.SystemColor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Timer;
import java.util.TimerTask;
import java.util.ArrayList;
import java.util.Collections;
import java.awt.Toolkit;

class Briefcase extends JLabel{
	private String moneyAmount;
	private String text;
	private JLabel moneyLevel;
	
	 public String getNum() {
		 return this.text;
	 }
	 
	 public String getMoneyAmount() {
	        return moneyAmount;
	 }
	 
	 public JLabel getMoneyLevel() {
		 return moneyLevel;
	 }
	 
	 public String getSaltTextOfMoneyLevel(JLabel moneyLevel) {
		 return moneyLevel.getText().trim().replace(" ", "").replace("$", "");
	 }
	 
	 public Briefcase(String text, ImageIcon briefcase, ArrayList<JLabel> moneyLevels) {
		 super(text);
		 this.text = text;   
		 
		 Collections.shuffle(moneyLevels);
		 this.moneyAmount = getSaltTextOfMoneyLevel(moneyLevels.get(0));
		 this.moneyLevel = moneyLevels.get(0);
		 moneyLevels.remove(0);
		 
	     setIcon(briefcase);
	     setOpaque(false);
	     setBounds(219, 129, 89, 69);
	     setCursor(new Cursor(Cursor.HAND_CURSOR));
	     setVerticalTextPosition(SwingConstants.CENTER);
	     setHorizontalTextPosition(SwingConstants.CENTER);
	     setForeground(Color.DARK_GRAY);
	     setFont(new Font("Rockwell", Font.BOLD, 20));
	     setSize(74, 69);
	 }
}

public class GameScreen extends JFrame {

	private JPanel contentPane;
	private JLabel lblGameStatus, lblOpen;
	private static GameScreen gameScreen;
	public int playerCase = 0;
	public Briefcase selectedPlayerCase;
	public int caseToOpen = 0;
	public int currentRound = 0;
	public String playerWonMoney;
	public ArrayList<Briefcase> briefcases = new ArrayList<Briefcase>();
	public ArrayList<JLabel> moneyLevels = new ArrayList<JLabel>(); 
	private static Clip clip;
	private static AudioInputStream audioIn;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					gameScreen = new GameScreen();
					gameScreen.setLocationRelativeTo(null);
					gameScreen.setUndecorated(true); //removes the window's frame
					gameScreen.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	public void makeDisabled(JLabel moneyLevel){
		moneyLevel.setBackground(SystemColor.controlDkShadow);
		moneyLevel.setForeground(SystemColor.activeCaptionBorder);
	}
	
	public void playSound(String soundName) {
		try {
			File musicPath = new File("C:\\DealOrNoDeal\\sounds\\"+soundName);
			audioIn = AudioSystem.getAudioInputStream(musicPath);
			clip = (Clip) AudioSystem.getClip();
			clip.open(audioIn);
			clip.start();
			clip.loop(Clip.LOOP_CONTINUOUSLY);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public void stopSound() {
		try {
			clip.close();
			audioIn.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	
	public void restartGame() {
		stopSound();
		gameScreen.setVisible(false);
		main(null);
	}
	
	public void gameOver(String prize) {
		stopSound();
		playSound("won.wav");
		earnedMoney(prize);
		Timer timer = new Timer();
	    TimerTask task = new TimerTask() {
	      public void run() {
	    	  dialogPlayAgain();
	      }
	    };
	    timer.schedule(task, 2000);
	}
	
	public void dialogPlayAgain() {
		int playAgain = JOptionPane.showConfirmDialog(null, "Do you want to play again?", "Game over", JOptionPane.YES_NO_OPTION);
		if(playAgain == JOptionPane.YES_OPTION) {
			restartGame();
		}else {
			stopSound();
			System.exit(0);
		}
	}
	
	public double calculateAvrOfBriefcases(int roundNumber ,ArrayList<Briefcase> briefcases, int chosenCase) {
	    double sum = 0;
	    double moneyAmount = 0;
	    double average;
	    int remainingCases = briefcases.size();
	    for (Briefcase b : briefcases) {
	        if (Integer.valueOf(b.getText()) != chosenCase) {
	        	System.out.println("Money Amount: "+ b.getMoneyAmount());	
	        	moneyAmount = Double.parseDouble(b.getMoneyAmount().replace(",", ""));
	            sum += moneyAmount;
	        }
	    }
	    average = sum / remainingCases;
	    if(roundNumber == 1){
	        average = average * 0.8;
	        System.out.println("Round 1 (Avr. Money): "+ average);
	    }else {
	    // for the other rounds
	        average = average * (0.8 + roundNumber * 0.05);
	        System.out.println("Round "+roundNumber+" (Avr. Money): "+ average);
	    }
	    return average;
	}
	
	public void makeBankOffer() {
		DecimalFormat format = new DecimalFormat("#,###,###");
		double average = calculateAvrOfBriefcases(currentRound, briefcases, playerCase);
		int bankOffer = (int) average;
		String formattedBankOffer = format.format(bankOffer);
		int bankOfferAccepted = JOptionPane.showConfirmDialog(null, "Banker offers you "+formattedBankOffer+"$. Press YES to Deal, NO to continue the game.", "Banker Offer", JOptionPane.YES_NO_OPTION);
		if(bankOfferAccepted == JOptionPane.YES_OPTION) {
				gameOver(formattedBankOffer);
		    } else {
		    	nextRound(gameScreen.currentRound);  
		    }
	}
	
	//Round controller
	public void nextRound(int current) {
		stopSound();
		playSound("rounds.wav");
		switch(current) {
		case 0: 
			startFirstRound();
			break;
		case 1:
			startSecondRound();
			break;
		case 2:
			startThirdRound();
			break;
		case 3:
			startForthRound();
			break;
		case 4:
			finalRound();
			break;
		}
	}
	
	public void makeBankerCalling() {
		gameScreen.lblGameStatus.setText("Banker calling...");
		stopSound();
		playSound("bankers-offer.wav");
	}
	
	public void startFirstRound() {
		currentRound = 1;
		caseToOpen = 4;
		String caseToOpenNumText = "";
		gameScreen.lblGameStatus.setText("Round 1");
		gameScreen.lblOpen.setText("Open four (4) cases.");
		
		while(caseToOpen >= 1) {
			switch(caseToOpen) {
				case 4:
					caseToOpenNumText = "four";
					break;
				case 3:
					caseToOpenNumText = "three";
					break;
				case 2:
					caseToOpenNumText = "two";
					break;
				case 1:
					caseToOpenNumText = "one";
					break;
			}
			if(caseToOpen > 1)
				gameScreen.lblOpen.setText("Open "+caseToOpenNumText+" ("+caseToOpen+") cases.");
			else
				gameScreen.lblOpen.setText("Open "+caseToOpenNumText+" ("+caseToOpen+") case.");
		}
		gameScreen.lblOpen.setText(" ");
		makeBankerCalling();
		
		//After game status turned into "Banker calling...", this task will be proceeded in 3secs.
		Timer timer = new Timer();
	    TimerTask task = new TimerTask() {
	      public void run() {
	    	  makeBankOffer();
	      }
	    };
	    timer.schedule(task, 3000);
		
	}
	
	public void startSecondRound() {
		currentRound = 2;
		caseToOpen = 4;
		String caseToOpenNumText = "";
		gameScreen.lblGameStatus.setText("Round 2");
		while(caseToOpen >= 1) {
			switch(caseToOpen) {
				case 4:
					caseToOpenNumText = "four";
					break;
				case 3:
					caseToOpenNumText = "three";
					break;
				case 2:
					caseToOpenNumText = "two";
					break;
				case 1:
					caseToOpenNumText = "one";
					break;
			}
			if(caseToOpen > 1)
				gameScreen.lblOpen.setText("Open "+caseToOpenNumText+" ("+caseToOpen+") cases.");
			else
				gameScreen.lblOpen.setText("Open "+caseToOpenNumText+" ("+caseToOpen+") case.");
		}
		gameScreen.lblOpen.setText(" ");
		gameScreen.lblGameStatus.setText("Banker calling...");
		makeBankerCalling();
		
		//After game status turned into "Banker calling...", this task will be proceeded in 3secs.
		Timer timer = new Timer();
	    TimerTask task = new TimerTask() {
	      public void run() {
	    	  makeBankOffer();
	      }
	    };
	    timer.schedule(task, 3000);
		
	}
	
	public void startThirdRound() {
		currentRound = 3;
		caseToOpen = 4;
		String caseToOpenNumText = "";
		gameScreen.lblGameStatus.setText("Round 3");
		while(caseToOpen >= 1) {
			switch(caseToOpen) {
				case 4:
					caseToOpenNumText = "four";
					break;
				case 3:
					caseToOpenNumText = "three";
					break;
				case 2:
					caseToOpenNumText = "two";
					break;
				case 1:
					caseToOpenNumText = "one";
					break;
			}
			if(caseToOpen > 1)
				gameScreen.lblOpen.setText("Open "+caseToOpenNumText+" ("+caseToOpen+") cases.");
			else
				gameScreen.lblOpen.setText("Open "+caseToOpenNumText+" ("+caseToOpen+") case.");
		}
		gameScreen.lblOpen.setText(" ");
		gameScreen.lblGameStatus.setText("Banker calling...");
		makeBankerCalling();
		
		//After game status turned into "Banker calling...", this task will be proceeded in 3secs.
		Timer timer = new Timer();
	    TimerTask task = new TimerTask() {
	      public void run() {
	    	  makeBankOffer();
	      }
	    };
	    timer.schedule(task, 3000);
		
	}
	
	public void startForthRound() {
		currentRound = 4;
		caseToOpen = 2;
		String caseToOpenNumText = "";
		gameScreen.lblGameStatus.setText("Round 4");
		while(caseToOpen >= 1) {
			switch(caseToOpen) {
				case 4:
					caseToOpenNumText = "four";
					break;
				case 3:
					caseToOpenNumText = "three";
					break;
				case 2:
					caseToOpenNumText = "two";
					break;
				case 1:
					caseToOpenNumText = "one";
					break;
			}
			if(caseToOpen > 1)
				gameScreen.lblOpen.setText("Open "+caseToOpenNumText+" ("+caseToOpen+") cases.");
			else
				gameScreen.lblOpen.setText("Open "+caseToOpenNumText+" ("+caseToOpen+") case.");
		}
		gameScreen.lblOpen.setText(" ");
		gameScreen.lblGameStatus.setText("Banker calling...");
		makeBankerCalling();
		
		//After game status turned into "Banker calling...", this task will be proceeded in 3secs.
		Timer timer = new Timer();
	    TimerTask task = new TimerTask() {
	      public void run() {
	    	  makeBankOffer();
	      }
	    };
	    timer.schedule(task, 3000);
		
	}
	
	public void finalRound() {
		gameScreen.lblGameStatus.setText("Final Round");
		stopSound();
		playSound("final-round.wav");
		int playerCaseConfirmed = JOptionPane.showConfirmDialog(null, "Do you want to open your case? If you open your case, you'll get what you have in your case. Otherwise, other case will be your prize.", "Final Decision", JOptionPane.YES_NO_OPTION);
		if(playerCaseConfirmed == JOptionPane.YES_OPTION) {
			playerWonMoney = selectedPlayerCase.getMoneyAmount();
			selectedPlayerCase.setVisible(false);
			makeDisabled(selectedPlayerCase.getMoneyLevel());
			JOptionPane.showMessageDialog(null, "Your case ("+playerCase+") opened. There was "+playerWonMoney+"$ in your case.", "Info", JOptionPane.INFORMATION_MESSAGE);
		    gameOver(playerWonMoney);
		}else {
			Briefcase remainingBC = briefcases.get(0);
			playerWonMoney = remainingBC.getMoneyAmount();
			remainingBC.setVisible(false);
			makeDisabled(selectedPlayerCase.getMoneyLevel());
			JOptionPane.showMessageDialog(null, "You chose to open the other case ("+remainingBC.getNum()+") opened. There was "+playerWonMoney+"$ in case "+remainingBC.getNum()+".", "Info", JOptionPane.INFORMATION_MESSAGE);
			gameOver(playerWonMoney);
		}
	}
	
	public void earnedMoney(String quantity) {
		gameScreen.lblGameStatus.setText("Congrats! You won "+quantity+"$.");
		gameScreen.lblGameStatus.setForeground(Color.GREEN);
		
	}
	
	
	public void openCase(int caseNum, Briefcase bc) {
		//Controls if the player chooses theirs own case to open a case or not.  
		if(caseNum != playerCase) {
			 bc.setVisible(false);
			 makeDisabled(bc.getMoneyLevel());
			 JOptionPane.showMessageDialog(null, "Case "+caseNum+" opened. There was "+bc.getMoneyAmount()+"$ inside.", "Info", JOptionPane.INFORMATION_MESSAGE);
			 caseToOpen--;
			 briefcases.remove(bc);
		 }else {
			 JOptionPane.showMessageDialog(null, "You cannot open your case. Choose another one.", "Warning", JOptionPane.WARNING_MESSAGE);
		 }
		 
	}
	
	public void caseChooser(Briefcase bc, int caseNum) {
		bc.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if(playerCase == 0) {
					//Choosing Player case 
					chooseYourCase(caseNum, bc);
					
					bc.setForeground(Color.RED);
					Timer timer = new Timer();
				    TimerTask task = new TimerTask() {
				      public void run() {
				    	  nextRound(currentRound);
				    	  System.out.print("timer");
				      }
				    };
				    timer.schedule(task, 1000);
				}else if(caseToOpen != 0){
					//If player case has already chosen, then open the selected case.
					openCase(caseNum, bc);
				}else {
					
				}
			}
		});
	}
	
	public void chooseYourCase(int caseNum, Briefcase selectedPC) {
		playerCase = caseNum;
		selectedPlayerCase = selectedPC;
		briefcases.remove(selectedPC);
		gameScreen.lblGameStatus.setText("Now your case: " + String.valueOf(playerCase));
		stopSound();
	}
	
	public void generateCases(JPanel contentPanel, ArrayList<Briefcase> briefcases) {
		for (Briefcase b : briefcases) {
			  contentPanel.add(b);
			  contentPanel.revalidate();
			  contentPanel.repaint();
		  }
		 gameScreen.lblGameStatus.setText("Choose a case to start, it'll be your case.");
		 playSound("choose-your-case.wav");
	}

	/**
	 * Create the frame.
	 */
	
	public GameScreen() {
		setTitle("Deal To Win A Million");
		setIconImage(Toolkit.getDefaultToolkit().getImage(GameScreen.class.getResource("/images/icon.jpg")));		
		setBackground(Color.BLACK);
		ImageIcon briefcase = new ImageIcon(getClass().getResource("/images/briefcase.PNG"));
		Image img = briefcase.getImage();
		Image resizedImg = img.getScaledInstance(74, 60, java.awt.Image.SCALE_SMOOTH);
		briefcase = new ImageIcon(resizedImg);
		
		setResizable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 1024, 680);
		contentPane = new JPanel();
		contentPane.setBackground(Color.DARK_GRAY);
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));

		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JLabel lblMoney01 = new JLabel("$                    .01 ");
		lblMoney01.setForeground(new Color(255, 255, 255));
		lblMoney01.setBackground(new Color(255, 140, 0));
		lblMoney01.setHorizontalAlignment(SwingConstants.TRAILING);
		lblMoney01.setFont(new Font("Rockwell", Font.PLAIN, 18));
		lblMoney01.setBounds(48, 98, 150, 39);
		lblMoney01.setOpaque(true);
		contentPane.add(lblMoney01);
		moneyLevels.add(lblMoney01);
		
		JLabel lblMoney1 = new JLabel("$                       1 ");
		lblMoney1.setOpaque(true);
		lblMoney1.setHorizontalAlignment(SwingConstants.TRAILING);
		lblMoney1.setForeground(Color.WHITE);
		lblMoney1.setFont(new Font("Rockwell", Font.PLAIN, 18));
		lblMoney1.setBackground(new Color(255, 140, 0));
		lblMoney1.setBounds(48, 165, 150, 39);
		contentPane.add(lblMoney1);
		moneyLevels.add(lblMoney1);
		
		JLabel lblMoney5 = new JLabel("$                       5 ");
		lblMoney5.setOpaque(true);
		lblMoney5.setHorizontalAlignment(SwingConstants.TRAILING);
		lblMoney5.setForeground(Color.WHITE);
		lblMoney5.setFont(new Font("Rockwell", Font.PLAIN, 18));
		lblMoney5.setBackground(new Color(255, 140, 0));
		lblMoney5.setBounds(48, 232, 150, 39);
		contentPane.add(lblMoney5);
		moneyLevels.add(lblMoney5);
		
		JLabel lblMoney50 = new JLabel("$                     50 ");
		lblMoney50.setOpaque(true);
		lblMoney50.setHorizontalAlignment(SwingConstants.TRAILING);
		lblMoney50.setForeground(Color.WHITE);
		lblMoney50.setFont(new Font("Rockwell", Font.PLAIN, 18));
		lblMoney50.setBackground(new Color(255, 140, 0));
		lblMoney50.setBounds(48, 299, 150, 39);
		contentPane.add(lblMoney50);
		moneyLevels.add(lblMoney50);
		
		JLabel lblMoney100 = new JLabel("$                   100 ");
		lblMoney100.setOpaque(true);
		lblMoney100.setHorizontalAlignment(SwingConstants.TRAILING);
		lblMoney100.setForeground(Color.WHITE);
		lblMoney100.setFont(new Font("Rockwell", Font.PLAIN, 18));
		lblMoney100.setBackground(new Color(255, 140, 0));
		lblMoney100.setBounds(48, 366, 150, 39);
		contentPane.add(lblMoney100);
		moneyLevels.add(lblMoney100);
		
		JLabel lblMoney300 = new JLabel("$                   300 ");
		lblMoney300.setOpaque(true);
		lblMoney300.setHorizontalAlignment(SwingConstants.TRAILING);
		lblMoney300.setForeground(Color.WHITE);
		lblMoney300.setFont(new Font("Rockwell", Font.PLAIN, 18));
		lblMoney300.setBackground(new Color(255, 140, 0));
		lblMoney300.setBounds(48, 433, 150, 39);
		contentPane.add(lblMoney300);
		moneyLevels.add(lblMoney300);
		
		JLabel lblMoney500 = new JLabel("$                   500 ");
		lblMoney500.setOpaque(true);
		lblMoney500.setHorizontalAlignment(SwingConstants.TRAILING);
		lblMoney500.setForeground(Color.WHITE);
		lblMoney500.setFont(new Font("Rockwell", Font.PLAIN, 18));
		lblMoney500.setBackground(new Color(255, 140, 0));
		lblMoney500.setBounds(48, 500, 150, 39);
		contentPane.add(lblMoney500);
		moneyLevels.add(lblMoney500);
		
		JLabel lblMoney1000 = new JLabel("$                1,000 ");
		lblMoney1000.setOpaque(true);
		lblMoney1000.setHorizontalAlignment(SwingConstants.TRAILING);
		lblMoney1000.setForeground(Color.WHITE);
		lblMoney1000.setFont(new Font("Rockwell", Font.PLAIN, 18));
		lblMoney1000.setBackground(new Color(255, 140, 0));
		lblMoney1000.setBounds(48, 567, 150, 39);
		contentPane.add(lblMoney1000);
		moneyLevels.add(lblMoney1000);
		
		JLabel lblMoney5000 = new JLabel("$                5,000 ");
		lblMoney5000.setOpaque(true);
		lblMoney5000.setHorizontalAlignment(SwingConstants.TRAILING);
		lblMoney5000.setForeground(Color.WHITE);
		lblMoney5000.setFont(new Font("Rockwell", Font.PLAIN, 18));
		lblMoney5000.setBackground(new Color(255, 140, 0));
		lblMoney5000.setBounds(821, 98, 150, 39);
		contentPane.add(lblMoney5000);
		moneyLevels.add(lblMoney5000);
		
		JLabel lblMoney10000 = new JLabel("$              10,000 ");
		lblMoney10000.setOpaque(true);
		lblMoney10000.setHorizontalAlignment(SwingConstants.TRAILING);
		lblMoney10000.setForeground(Color.WHITE);
		lblMoney10000.setFont(new Font("Rockwell", Font.PLAIN, 18));
		lblMoney10000.setBackground(new Color(255, 140, 0));
		lblMoney10000.setBounds(821, 165, 150, 39);
		contentPane.add(lblMoney10000);
		moneyLevels.add(lblMoney10000);
		
		JLabel lblMoney30000 = new JLabel("$              30,000 ");
		lblMoney30000.setOpaque(true);
		lblMoney30000.setHorizontalAlignment(SwingConstants.TRAILING);
		lblMoney30000.setForeground(Color.WHITE);
		lblMoney30000.setFont(new Font("Rockwell", Font.PLAIN, 18));
		lblMoney30000.setBackground(new Color(255, 140, 0));
		lblMoney30000.setBounds(821, 232, 150, 39);
		contentPane.add(lblMoney30000);
		moneyLevels.add(lblMoney30000);
		
		JLabel lblMoney50000 = new JLabel("$              50,000 ");
		lblMoney50000.setOpaque(true);
		lblMoney50000.setHorizontalAlignment(SwingConstants.TRAILING);
		lblMoney50000.setForeground(Color.WHITE);
		lblMoney50000.setFont(new Font("Rockwell", Font.PLAIN, 18));
		lblMoney50000.setBackground(new Color(255, 140, 0));
		lblMoney50000.setBounds(821, 299, 150, 39);
		contentPane.add(lblMoney50000);
		moneyLevels.add(lblMoney50000);
		
		JLabel lblMoney100000 = new JLabel("$            100,000 ");
		lblMoney100000.setOpaque(true);
		lblMoney100000.setHorizontalAlignment(SwingConstants.TRAILING);
		lblMoney100000.setForeground(Color.WHITE);
		lblMoney100000.setFont(new Font("Rockwell", Font.PLAIN, 18));
		lblMoney100000.setBackground(new Color(255, 140, 0));
		lblMoney100000.setBounds(821, 366, 150, 39);
		contentPane.add(lblMoney100000);
		moneyLevels.add(lblMoney100000);
		
		JLabel lblMoney300000 = new JLabel("$            300,000 ");
		lblMoney300000.setOpaque(true);
		lblMoney300000.setHorizontalAlignment(SwingConstants.TRAILING);
		lblMoney300000.setForeground(Color.WHITE);
		lblMoney300000.setFont(new Font("Rockwell", Font.PLAIN, 18));
		lblMoney300000.setBackground(new Color(255, 140, 0));
		lblMoney300000.setBounds(821, 433, 150, 39);
		contentPane.add(lblMoney300000);
		moneyLevels.add(lblMoney300000);
		
		JLabel lblMoney500000 = new JLabel("$            500,000 ");
		lblMoney500000.setOpaque(true);
		lblMoney500000.setHorizontalAlignment(SwingConstants.TRAILING);
		lblMoney500000.setForeground(Color.WHITE);
		lblMoney500000.setFont(new Font("Rockwell", Font.PLAIN, 18));
		lblMoney500000.setBackground(new Color(255, 140, 0));
		lblMoney500000.setBounds(821, 500, 150, 39);
		contentPane.add(lblMoney500000);
		moneyLevels.add(lblMoney500000);
		
		JLabel lblMoney1000000 = new JLabel("$         1,000,000 ");
		lblMoney1000000.setOpaque(true);
		lblMoney1000000.setHorizontalAlignment(SwingConstants.TRAILING);
		lblMoney1000000.setForeground(Color.WHITE);
		lblMoney1000000.setFont(new Font("Rockwell", Font.PLAIN, 18));
		lblMoney1000000.setBackground(new Color(255, 140, 0));
		lblMoney1000000.setBounds(821, 567, 150, 39);
		contentPane.add(lblMoney1000000);
		moneyLevels.add(lblMoney1000000);
		
		Briefcase bc1 = new Briefcase("1", briefcase, moneyLevels);
		bc1.setLocation(256, 169);
		caseChooser(bc1, 1);
		Briefcase bc2 = new Briefcase("2", briefcase, moneyLevels);
		bc2.setLocation(369, 169);
		caseChooser(bc2, 2);
		Briefcase bc3 = new Briefcase("3", briefcase, moneyLevels);
		bc3.setLocation(484, 169);
		caseChooser(bc3, 3);
		Briefcase bc4 = new Briefcase("4", briefcase, moneyLevels);
		bc4.setLocation(589, 169);
		caseChooser(bc4, 4);
		Briefcase bc5 = new Briefcase("5", briefcase, moneyLevels);
		bc5.setLocation(688, 169);
		caseChooser(bc5, 5);
		Briefcase bc6 = new Briefcase("6", briefcase, moneyLevels);
		bc6.setLocation(256, 269);
		caseChooser(bc6, 6);
		Briefcase bc7 = new Briefcase("7", briefcase, moneyLevels);
		bc7.setLocation(369, 269);
		caseChooser(bc7, 7);
		Briefcase bc8 = new Briefcase("8", briefcase, moneyLevels);
		bc8.setLocation(484, 269);
		caseChooser(bc8, 8);
		Briefcase bc9 = new Briefcase("9", briefcase, moneyLevels);
		bc9.setLocation(589, 269);
		caseChooser(bc9, 9);
		Briefcase bc10 = new Briefcase("10", briefcase, moneyLevels);
		bc10.setLocation(688, 269);
		caseChooser(bc10, 10);
		Briefcase bc11 = new Briefcase("11", briefcase, moneyLevels);
		bc11.setLocation(256, 373);
		caseChooser(bc11, 11);
		Briefcase bc12 = new Briefcase("12", briefcase, moneyLevels);
		bc12.setLocation(369, 373);
		caseChooser(bc12, 12);
		Briefcase bc13 = new Briefcase("13", briefcase, moneyLevels);
		bc13.setLocation(484, 373);
		caseChooser(bc13, 13);
		Briefcase bc14 = new Briefcase("14", briefcase, moneyLevels);
		bc14.setLocation(589, 373);
		caseChooser(bc14, 14);
		Briefcase bc15 = new Briefcase("15", briefcase, moneyLevels);
		bc15.setLocation(688, 373);
		caseChooser(bc15, 15);
		Briefcase bc16 = new Briefcase("16", briefcase, moneyLevels);
		bc16.setLocation(256, 470);
		caseChooser(bc16, 16);
		
		briefcases.add(bc1);
		briefcases.add(bc2);
		briefcases.add(bc3);
		briefcases.add(bc4);
		briefcases.add(bc5);
		briefcases.add(bc6);
		briefcases.add(bc7);
		briefcases.add(bc8);
		briefcases.add(bc9);
		briefcases.add(bc10);
		briefcases.add(bc11);
		briefcases.add(bc12);
		briefcases.add(bc13);
		briefcases.add(bc14);
		briefcases.add(bc15);
		briefcases.add(bc16);
		
		Timer timer = new Timer();
		  TimerTask task = new TimerTask() {
		      public void run() {
		    	  generateCases(contentPane, briefcases);
		      }
		    };
		  timer.schedule(task, 2000);
		
		
		lblGameStatus = new JLabel("The cases are generating...");
		lblGameStatus.setHorizontalAlignment(SwingConstants.CENTER);
		lblGameStatus.setForeground(Color.ORANGE);
		lblGameStatus.setFont(new Font("Rockwell", Font.BOLD, 16));
		lblGameStatus.setBounds(256, 94, 506, 46);
		contentPane.add(lblGameStatus);
		
		JLabel exit = new JLabel("X");
		exit.setCursor(new Cursor(Cursor.HAND_CURSOR));
		exit.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				System.exit(0);
			}
		});
		exit.setFont(new Font("Rockwell", Font.BOLD, 15));
		exit.setForeground(Color.WHITE);
		exit.setHorizontalAlignment(SwingConstants.CENTER);
		exit.setBounds(974, 11, 46, 26);
		contentPane.add(exit);
		
		lblOpen = new JLabel();
		lblOpen.setHorizontalAlignment(SwingConstants.CENTER);
		lblOpen.setForeground(Color.WHITE);
		lblOpen.setFont(new Font("Rockwell", Font.BOLD, 16));
		lblOpen.setBounds(256, 560, 506, 46);
		contentPane.add(lblOpen);
		
	}
}
