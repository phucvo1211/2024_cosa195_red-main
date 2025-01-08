package DataObject;

import java.util.ArrayList;

public class CustomisationSetting {

    Cosmetic avatar;
    Cosmetic cardSkin;
    ArrayList<Cosmetic> cosmetics;

    public  CustomisationSetting() {
        avatar = new Cosmetic();
        cardSkin = new Cosmetic();
        cosmetics = new ArrayList<>();
    }
    public Cosmetic getAvatar() {return avatar;}
    public Cosmetic getCardSkin() {return cardSkin;}
    public Cosmetic changeAvatar(Cosmetic newAvatar) {return avatar;}
    public Cosmetic changeCardSkin(Cosmetic newSkin) {return cardSkin;}
}
