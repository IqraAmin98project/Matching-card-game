import javax.swing.*;
import java.io.*;
import java.util.ArrayList;
import javax.swing.text.*;

public class MatchCards {
    double currentPlayerTime = 0.0; // initialization iof time


    class Card {
        private String cardName;
        private ImageIcon cardImageIcon;

        public Card(String cardName, ImageIcon cardImageIcon) {
            this.cardName = cardName;
            this.cardImageIcon = cardImageIcon;
        }

        public String getCardName() {
            return cardName;
        }


        public ImageIcon getCardImageIcon() {
            return cardImageIcon;
        }



        public String toString() {
            return cardName;
        }
    }

    //Game configuration
    String[] cardlist = {
            "darkness",
            "double",
            "fairy",
            "fighting",
            "fire",
            "grass",
            "lighting",
            "metal",
            "psychic",
            "water"
    };
        int rows = 4;
        int columns = 5;
        int cardWidth = 90;
        int cardHeight = 128;

    // GUI COMPONENT
    ArrayList<Card> cardSet;
    ImageIcon cardBackImageIcon;
    int boardWidth = columns * cardHeight;
    JFrame frame;
    JLabel textLabel = new JLabel();
    JLabel turnLabel = new JLabel("Turn: Player 1");
    JPanel textPanel = new JPanel();
    JPanel boardPanel = new JPanel();
    JPanel restartGamePanel = new JPanel();
    JButton restartButton = new JButton("Restart Game");
    JLabel timerLabel = new JLabel("Time 0.0 seconds");


}
