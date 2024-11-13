import javax.swing.*;
import java.io.*;

public class MatchCards {
    class card {
        private String cardName;
        private ImageIcon cardImageIcon;

        public card(String cardName, ImageIcon cardImageIcon) {
            this.cardName = cardName;
            this.cardImageIcon = cardImageIcon;
        }

        public String getCardName() {
            return cardName;
        }

        public void setCardName(String cardName) {
            this.cardName = cardName;
        }

        public ImageIcon getCardImageIcon() {
            return cardImageIcon;
        }

        public void setCardImageIcon(ImageIcon cardImageIcon) {
            this.cardImageIcon = cardImageIcon;
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

}
